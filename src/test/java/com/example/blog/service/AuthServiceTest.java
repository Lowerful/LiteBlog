package com.example.blog.service;

import com.example.blog.common.ResultCode;
import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.RegisterRequest;
import com.example.blog.dto.response.LoginResponse;
import com.example.blog.dto.response.UserVO;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.impl.AuthServiceImpl;
import com.example.blog.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证服务的单元测试：Mapper 用 Mockito 打桩，不起 Spring 容器、不连数据库。
 * 测试聚焦"业务规则对不对"：重复校验、密码加密、登录判定。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil = new JwtUtil("service-test-secret-0123456789abcdef0123456789abcdef", 1);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtUtil);
    }

    private User savedUser(String rawPassword) {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname("alice");
        return user;
    }

    @Test
    @DisplayName("注册成功：密码已加密入库，且不是明文")
    void registerShouldEncodePassword() {
        when(userMapper.countByUsername("alice")).thenReturn(0);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("plain-123456");

        UserVO vo = authService.register(request);

        assertEquals("alice", vo.getUsername());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User inserted = captor.getValue();
        assertNotEquals("plain-123456", inserted.getPassword());
        assertTrue(inserted.getPassword().startsWith("$2"), "BCrypt 密文以 $2 开头");
    }

    @Test
    @DisplayName("用户名已存在：抛冲突异常，且不执行插入")
    void registerShouldRejectDuplicateUsername() {
        when(userMapper.countByUsername("alice")).thenReturn(1);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("plain-123456");

        BusinessException e = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getResultCode().getCode());
        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("登录成功：返回可用 token 和用户信息")
    void loginShouldReturnToken() {
        when(userMapper.findByUsername("alice")).thenReturn(savedUser("correct-pass"));

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("correct-pass");

        LoginResponse response = authService.login(request);

        assertEquals(1L, jwtUtil.parseUserId(response.getToken()));
        assertEquals("alice", response.getUser().getUsername());
    }

    @Test
    @DisplayName("密码错误：抛业务异常，且提示不区分用户是否存在")
    void loginShouldRejectWrongPassword() {
        when(userMapper.findByUsername("alice")).thenReturn(savedUser("correct-pass"));

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrong-pass");

        BusinessException e = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals("用户名或密码错误", e.getMessage());

        LoginRequest ghost = new LoginRequest();
        ghost.setUsername("nobody");
        ghost.setPassword("whatever");
        BusinessException e2 = assertThrows(BusinessException.class, () -> authService.login(ghost));
        assertEquals(e2.getMessage(), e.getMessage(), "用户不存在与密码错误的提示必须一致");
    }
}
