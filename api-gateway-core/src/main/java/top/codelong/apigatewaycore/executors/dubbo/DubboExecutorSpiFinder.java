package top.codelong.apigatewaycore.executors.dubbo;

import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Dubbo执行器SPI查找器
 */
public class DubboExecutorSpiFinder {
    private static volatile DubboExecutor executor;

    private DubboExecutorSpiFinder() {
        // 私有构造函数，防止实例化
    }

    public static DubboExecutor getInstance(Map<String, GenericService> dubboServiceMap) {
        if (executor == null) { // 第一次检查（非同步）
            synchronized (DubboExecutorSpiFinder.class) {
                if (executor == null) { // 第二次检查（同步）
                    try {
                        // 加载 SPI 实现类
                        ServiceLoader<DubboExecutor> load = ServiceLoader.load(DubboExecutor.class);
                        DubboExecutor dubboExecutor = load.findFirst()
                            .orElseThrow(() -> new IllegalStateException("未找到Dubbo执行器"));

                        // 通过反射调用无参构造函数
                        Constructor<? extends DubboExecutor> constructor = dubboExecutor.getClass().getDeclaredConstructor();
                        constructor.setAccessible(true);
                        executor = constructor.newInstance();
                        executor.setDubboServiceMap(dubboServiceMap);
                    } catch (Exception e) {
                        throw new RuntimeException("初始化Dubbo执行器失败", e);
                    }
                }
            }
        }
        return executor;
    }
}
