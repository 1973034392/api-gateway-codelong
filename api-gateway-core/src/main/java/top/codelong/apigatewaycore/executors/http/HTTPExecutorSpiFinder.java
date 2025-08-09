package top.codelong.apigatewaycore.executors.http;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import top.codelong.apigatewaycore.executors.BaseExecutor;

import java.lang.reflect.Constructor;
import java.util.ServiceLoader;

public abstract class HTTPExecutorSpiFinder implements BaseExecutor {
    private static volatile HTTPExecutor executor;

    public static HTTPExecutor getInstance(CloseableHttpAsyncClient client) {
        if (executor == null) { // 第一次检查（非同步）
            synchronized (HTTPExecutorSpiFinder.class) {
                if (executor == null) { // 第二次检查（同步）
                    try {
                        // 加载 SPI 实现类
                        ServiceLoader<HTTPExecutor> load = ServiceLoader.load(HTTPExecutor.class);
                        HTTPExecutor httpExecutor = load.findFirst().orElseThrow(() -> new IllegalStateException("未找到HTTP执行器"));

                        // 通过反射调用带参数的构造函数
                        Constructor<? extends HTTPExecutor> constructor = httpExecutor.getClass().getDeclaredConstructor();
                        constructor.setAccessible(true);
                        executor = constructor.newInstance();
                        executor.setClient(client);
                    } catch (Exception e) {
                        throw new RuntimeException("未找到HTTP执行器", e);
                    }
                }
            }
        }
        return executor;
    }
}
