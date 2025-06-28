package top.codelong.apigatewaycenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiGatewayCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayCenterApplication.class, args);
        System.out.println("●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●\n" +
                "                _        _                   \n" +
                "   ___ ___   __| | ___  | | ___  _ __   __ _ \n" +
                "  / __/ _ \\ / _` |/ _ \\ | |/ _ \\| '_ \\ / _` |\n" +
                " | (_| (_) | (_| |  __/ | | (_) | | | | (_| |\n" +
                "  \\___\\___/ \\__,_|\\___| |_|\\___/|_| |_|\\__, |\n" +
                "                                       |___/ \n" +
                "★★★★★★★★★★★★项目启动成功★★★★★★★★★★★★★\n");
    }
}
