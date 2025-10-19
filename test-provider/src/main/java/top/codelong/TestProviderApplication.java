package top.codelong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"top.codelong", "top.codelong.sendsdk"})
public class TestProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestProviderApplication.class, args);
    }
}
