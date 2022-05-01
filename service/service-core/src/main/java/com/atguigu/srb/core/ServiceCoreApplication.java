package com.atguigu.srb.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author coderxdh
 * @create 2022-04-17 20:30
 */
@SpringBootApplication
@ComponentScan({"com.atguigu.srb"})  // 扫描整个包范围，这样后续在服务之间调用的时候可以直接注入
@EnableSwagger2
public class ServiceCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class, args);
    }
}
