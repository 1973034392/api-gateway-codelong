package top.codelong.findsdk.annotation;

import java.lang.annotation.*;

/**
 * 自定义接口注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ApiInterface {
    String interfaceName() default "";
}
