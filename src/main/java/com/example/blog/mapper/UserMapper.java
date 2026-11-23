package com.example.blog.mapper;

import com.example.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口。
 *
 * 约定：接口只声明"要什么"，具体 SQL 写在 resources/mapper/UserMapper.xml。
 * 这样 SQL 可读、可 diff、可用 DBA 工具直接跑，比注解里塞 SQL 更好维护。
 */
@Mapper
public interface UserMapper {

    /** 按用户名统计，用于注册时判断重复 */
    int countByUsername(@Param("username") String username);

    /** 按用户名查询（登录时用） */
    User findByUsername(@Param("username") String username);

    /** 插入用户，成功后 userId 会被回填到入参对象的 id 上（useGeneratedKeys） */
    int insert(User user);

    /** 按主键查询 */
    User findById(@Param("id") Long id);

    /** 总用户数，第 2 步用于验证数据库连通性 */
    long count();
}
