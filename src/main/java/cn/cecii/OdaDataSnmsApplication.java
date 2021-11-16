package cn.cecii;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 二级节点标识解析服务
 */

@SpringBootApplication
@EnableScheduling
public class OdaDataSnmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OdaDataSnmsApplication.class, args);
    }

}
