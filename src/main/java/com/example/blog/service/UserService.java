package com.example.blog.service;

import com.example.blog.dto.response.UserVO;

public interface UserService {

    /** 按 id 查询用户公开信息（不含密码） */
    UserVO getUserById(Long userId);
}
