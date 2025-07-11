package top.codelong.apigatewaycore.executors.dubbo;

import org.apache.dubbo.rpc.service.GenericService;
import top.codelong.apigatewaycore.executors.BaseExecutor;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class DubboExecutorSpiFinder implements BaseExecutor {
    private static volatile DubboExecutor executor;

    public static DubboExecutor getInstance(Map<String, GenericService> dubboServiceMap) {
        if (executor == null) { // 第一次检查（非同步）
            synchronized (DubboExecutorSpiFinder.class) {
                if (executor == null) { // 第二次检查（同步）
                    try {
                        // 加载 SPI 实现类
                        ServiceLoader<DubboExecutor> load = ServiceLoader.load(DubboExecutor.class);
                        DubboExecutor httpExecutorSpiFinder = load.findFirst().orElseThrow(() -> new IllegalStateException("未找到Dubbo执行器"));

                        // 通过反射调用带参数的构造函数
                        Constructor<? extends DubboExecutor> constructor = httpExecutorSpiFinder.getClass().getDeclaredConstructor();
                        constructor.setAccessible(true);
                        executor = constructor.newInstance();
                        executor.setDubboServiceMap(dubboServiceMap);
                    } catch (Exception e) {
                        throw new RuntimeException("未找到Dubbo执行器", e);
                    }
                }
            }
        }
        return executor;
    }
}
