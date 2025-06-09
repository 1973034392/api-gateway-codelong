package top.codelong;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestProviderApplication.class, args);
    }
}
