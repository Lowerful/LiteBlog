package com.example.blog.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 注册 PageHelper 分页插件。
 *
 * 官方 pagehelper-spring-boot-starter 只适配到 Spring Boot 3，Boot 4 尚无适配版。
 * 但 starter 做的事本质上就一件：把 PageInterceptor 挂到 MyBatis 的
 * SqlSessionFactory 上——而 mybatis-spring-boot-starter 会自动收集容器里
 * 所有 Interceptor 类型的 Bean。所以手动注册一个 Bean 即可，功能完全等价。
 *
 * 插件原理一句话：它拦截 MyBatis 执行查询的 Executor，检测到
 * PageHelper.startPage() 留下的 ThreadLocal 标记时，把原 SQL 改写成
 * LIMIT 子句，并补发一条 COUNT 语句拿总数。
 */
@Configuration
public class MybatisPluginConfig {

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties props = new Properties();
        // 方言：改写 LIMIT 时按 MySQL 语法生成
        props.setProperty("helperDialect", "mysql");
        // 页码越界时回到第 1 页而不是返回空列表（前端乱传 page=9999 也不至于白屏）
        props.setProperty("reasonable", "true");
        interceptor.setProperties(props);
        return interceptor;
    }
}
