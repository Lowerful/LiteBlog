package com.example.blog.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集成测试：真的启动 Spring 容器、真的连 MySQL。
 *
 * 和 ResultTest 那种纯单元测试的区别：
 * - 这里验证的是"线路通不通"：数据源配置、连接池、MyBatis 映射文件、SQL 语法
 * - 代价是慢（几秒）且依赖外部环境，所以以后只对关键链路写集成测试
 */
@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("数据库连通，且 MyBatis 能执行 SQL")
    void databaseShouldBeReachable() {
        long total = userMapper.count();
        assertTrue(total >= 0, "查询总用户数失败");

        // 这个用户名一定不存在，顺便验证 #{username} 参数绑定
        assertEquals(0, userMapper.countByUsername("__definitely_not_exists__"));
    }
}
