package com.example.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LiteBlog 启动类。
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * 它会自动扫描本类所在包（com.example.blog）及其子包下的所有组件，
 * 所以所有业务代码都必须放在这个包下面，否则不会被 Spring 管理。
 */
@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

}
