package top.codelong.apigatewaycore.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionResourcePool<K> {
    // 节点类 - 表示缓存中的一个连接项
    class Node {
        final K key;
        BaseConnection value;      // 连接对象
        int frequency; // 访问频率
        Node prev;    // 前一个节点指针
        Node next;    // 后一个节点指针
        NodeList list; // 所属频率链表

        Node(K key, BaseConnection value) {
            this.key = key;
            this.value = value;
            this.frequency = 1;
        }

        // 断开连接
        void closeConnection() {
            try {
                if (value != null) {
                    value.close();
                }
            } catch (Exception e) {
                // 关闭异常处理
            }
        }
    }

    // 频率链表类 - 维护相同频率的节点
    class NodeList {
        final int frequency; // 当前链表的频率值
        Node head; // 链表头 (最新访问)
        Node tail; // 链表尾 (最旧访问)
        NodeList next; // 下一个频率更高的链表
        NodeList prev; // 上一个频率更低的链表

        NodeList(int frequency) {
            this.frequency = frequency;
            head = new Node(null, null);
            tail = new Node(null, null);
            head.next = tail;
            tail.prev = head;
        }

        // 添加节点到链表头部
        void addNode(Node node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            node.list = this;
        }

        // 移除节点
        void removeNode(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.list = null;
        }

        // 是否为空链表
        boolean isEmpty() {
            return head.next == tail;
        }

        // 移除尾节点 (最旧访问)
        Node removeLastNode() {
            if (isEmpty()) return null;
            Node node = tail.prev;
            removeNode(node);
            return node;
        }
    }

    private final int capacity; // 最大容量
    private final Map<K, Node> nodeMap; // 键到节点的映射
    private NodeList minFreqList; // 最低频率链表
    private final Map<Integer, NodeList> freqListMap; // 频率到链表的映射

    // 并发控制
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    // 统计信息
    private final AtomicInteger hits = new AtomicInteger(0);
    private final AtomicInteger misses = new AtomicInteger(0);
    private final AtomicInteger evictions = new AtomicInteger(0);

    public ConnectionResourcePool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("容量必须大于0");
        }
        this.capacity = capacity;
        this.nodeMap = new HashMap<>(capacity);
        this.freqListMap = new HashMap<>();
        this.minFreqList = createList(1); // 初始最小频率为1
    }

    // 创建新频率链表并维护链表关系
    private NodeList createList(int frequency) {
        NodeList newList = new NodeList(frequency);
        freqListMap.put(frequency, newList);

        // 更新链表关系
        if (frequency == 1) {
            minFreqList = newList;
        } else {
            NodeList lowerList = freqListMap.get(frequency - 1);
            if (lowerList != null) {
                newList.prev = lowerList;
                lowerList.next = newList;
            }
        }
        return newList;
    }

    // 获取连接 - O(1) 时间复杂度
    public BaseConnection get(K key) {
        readLock.lock();
        try {
            Node node = nodeMap.get(key);
            if (node != null) {
                hits.incrementAndGet();
                return node.value;
            }
            misses.incrementAndGet();
            return null;
        } finally {
            readLock.unlock();
        }
    }

    // 获取并更新频率 - O(1) 时间复杂度
    public BaseConnection getWithFrequency(K key) {
        writeLock.lock();
        try {
            Node node = nodeMap.get(key);
            if (node != null) {
                hits.incrementAndGet();
                increaseFrequency(node);
                return node.value;
            }
            misses.incrementAndGet();
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    // 添加连接 - O(1) 平均时间复杂度
    public void put(K key, BaseConnection value) {
        writeLock.lock();
        try {
            // 如果已经存在，更新值并增加频率
            if (nodeMap.containsKey(key)) {
                Node node = nodeMap.get(key);
                node.value = value; // 更新连接
                increaseFrequency(node);
                return;
            }

            // 如果达到容量，移除最少使用的连接
            if (nodeMap.size() >= capacity) {
                evict(); // O(1) 移除操作
            }

            // 创建新节点并添加到频率为1的链表
            Node newNode = new Node(key, value);
            nodeMap.put(key, newNode);
            minFreqList.addNode(newNode); // 添加到最低频率链表的头部

            // 确保minFreqList指向频率最低的非空链表
            ensureMinFreqList();
        } finally {
            writeLock.unlock();
        }
    }

    // 增加节点访问频率 - O(1) 时间复杂度
    private void increaseFrequency(Node node) {
        // 1. 从当前链表移除节点
        node.list.removeNode(node);

        // 2. 获取或创建更高一级频率的链表
        int newFreq = node.frequency + 1;
        NodeList nextList = freqListMap.get(newFreq);
        if (nextList == null) {
            nextList = createList(newFreq);
        }

        // 3. 将节点添加到更高频率链表头部
        nextList.addNode(node);
        node.frequency = newFreq;

        // 4. 如果原始链表成为空链表且是最小频率链表，更新最小频率
        if (node.list.isEmpty() && node.list == minFreqList) {
            minFreqList = nextList;
        }

        // 5. 更新节点所属链表
        node.list = nextList;
    }

    // 移除最少使用的连接 - O(1) 时间复杂度
    private void evict() {
        // 1. 从最小频率链表中移除尾节点 (最旧访问)
        Node evictedNode = minFreqList.removeLastNode();
        if (evictedNode != null) {
            // 2. 从映射中移除
            nodeMap.remove(evictedNode.key);

            // 3. 关闭连接
            evictedNode.closeConnection();

            // 4. 更新统计
            evictions.incrementAndGet();

            // 5. 确保minFreqList指向非空链表
            ensureMinFreqList();
        }
    }

    // 确保minFreqList指向频率最低的非空链表
    private void ensureMinFreqList() {
        // 如果当前minFreqList为空，寻找下一个非空链表
        while (minFreqList.isEmpty() && minFreqList.next != null) {
            minFreqList = minFreqList.next;
        }

        // 如果所有链表都为空，重置为频率1的链表
        if (minFreqList.isEmpty()) {
            minFreqList = createList(1);
        }
    }

    // 显式移除连接
    public BaseConnection remove(K key) {
        writeLock.lock();
        try {
            Node node = nodeMap.remove(key);
            if (node != null) {
                // 从链表中移除
                node.list.removeNode(node);

                // 关闭连接
                node.closeConnection();

                // 更新最小频率链表
                if (node.list == minFreqList && node.list.isEmpty()) {
                    ensureMinFreqList();
                }

                return node.value;
            }
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    // 获取缓存统计信息
    public CacheStats getStats() {
        return new CacheStats(
                hits.get(),
                misses.get(),
                evictions.get(),
                nodeMap.size(),
                minFreqList.frequency
        );
    }

    // 清理所有连接
    public void clear() {
        writeLock.lock();
        try {
            // 清理所有节点
            for (Node node : nodeMap.values()) {
                node.closeConnection();
            }

            // 重置所有状态
            nodeMap.clear();
            freqListMap.clear();
            minFreqList = createList(1);

            // 重置统计
            hits.set(0);
            misses.set(0);
            evictions.set(0);
        } finally {
            writeLock.unlock();
        }
    }

    // 缓存统计数据结构
    public static class CacheStats {
        private final long hits;
        private final long misses;
        private final long evictions;
        private final int size;
        private final int minFreq;

        public CacheStats(long hits, long misses, long evictions, int size, int minFreq) {
            this.hits = hits;
            this.misses = misses;
            this.evictions = evictions;
            this.size = size;
            this.minFreq = minFreq;
        }

        public long hitCount() {
            return hits;
        }

        public long missCount() {
            return misses;
        }

        public long evictionCount() {
            return evictions;
        }

        public int currentSize() {
            return size;
        }

        public int minFrequency() {
            return minFreq;
        }

        public double hitRate() {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        }
    }
}