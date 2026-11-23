package com.example.blog.dto.response;

import com.example.blog.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图对象（VO）：对外暴露的用户数据。
 *
 * 和 entity.User 的区别：没有 password 字段。这是"Entity 不出接口"原则的落地——
 * 字段暴露范围在类型层面锁死，就算手滑也漏不出密码。
 */
@Data
@Builder
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private LocalDateTime createdAt;

    public static UserVO from(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
