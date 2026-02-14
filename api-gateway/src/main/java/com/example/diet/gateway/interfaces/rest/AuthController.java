package com.example.diet.gateway.interfaces.rest;

import com.example.diet.gateway.infrastructure.jwt.JwtUtil;
import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import com.example.diet.gateway.interfaces.dto.*;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.ApiResponse;
import com.example.diet.user.api.UserApi;
import com.example.diet.user.api.command.ChangePasswordCommand;
import com.example.diet.user.api.command.CreateUserCommand;
import com.example.diet.user.api.query.GetUserByEmailQuery;
import com.example.diet.user.api.query.GetUserByOpenidQuery;
import com.example.diet.user.api.query.GetUserByUsernameQuery;
import com.example.diet.user.api.query.GetUserQuery;
import com.example.diet.user.api.query.VerifyPasswordQuery;
import com.example.diet.user.api.response.UserResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器
 * 处理登录、注册、登出、修改密码等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @DubboReference
    private UserApi userApi;

    @Value("${wechat.appid:}")
    private String wechatAppId;

    @Value("${wechat.secret:}")
    private String wechatSecret;

    @Value("${wechat.login-url:https://api.weixin.qq.com/sns/jscode2session}")
    private String wechatLoginUrl;

    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BusinessException(400, "用户名不能为空");
        }

        // 通过用户名获取用户
        UserResponse user = userApi.getUserByUsername(
                GetUserByUsernameQuery.builder().username(request.getUsername()).build()
        );

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证密码
        boolean passwordValid = userApi.verifyPassword(
                VerifyPasswordQuery.builder()
                        .identifier(request.getUsername())
                        .password(request.getPassword())
                        .build()
        );

        if (!passwordValid) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证角色
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "该账号不是管理员账号");
        }

        // 检查状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        // 生成 Token
        LoginResponse response = generateLoginResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 用户登录 (邮箱)
     */
    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<LoginResponse>> userLogin(@Valid @RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException(400, "邮箱不能为空");
        }

        // 通过邮箱获取用户
        UserResponse user = userApi.getUserByEmail(
                GetUserByEmailQuery.builder().email(request.getEmail()).build()
        );

        if (user == null) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        // 验证密码
        boolean passwordValid = userApi.verifyPassword(
                VerifyPasswordQuery.builder()
                        .identifier(request.getEmail())
                        .password(request.getPassword())
                        .build()
        );

        if (!passwordValid) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        // 验证角色
        if (!"USER".equals(user.getRole())) {
            throw new BusinessException(403, "该账号不是普通用户账号");
        }

        // 检查状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        // 生成 Token
        LoginResponse response = generateLoginResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 微信登录
     */
    @PostMapping("/wechat-login")
    public ResponseEntity<ApiResponse<LoginResponse>> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        try {
            // 请求微信 API
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wechatLoginUrl, wechatAppId, wechatSecret, request.getCode());

            String responseBody = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // 检查错误
            if (rootNode.has("errcode") && rootNode.get("errcode").asInt() != 0) {
                throw new BusinessException(401, "微信登录失败: " + rootNode.get("errmsg").asText());
            }

            String openid = rootNode.get("openid").asText();

            // 查找用户
            UserResponse user = userApi.getUserByOpenid(
                    GetUserByOpenidQuery.builder().openid(openid).build()
            );

            // 如果用户不存在，自动创建
            if (user == null) {
                CreateUserCommand command = CreateUserCommand.builder()
                        .username("wx_" + UUID.randomUUID().toString().substring(0, 8))
                        .email(openid + "@wx.placeholder.com")
                        .password(UUID.randomUUID().toString())
                        .role("USER")
                        .openid(openid)
                        .build();
                user = userApi.createUser(command);
            }

            // 检查状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new BusinessException(403, "账号已被封禁，请联系管理员");
            }

            // 生成 Token
            LoginResponse response = generateLoginResponse(user);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new BusinessException(500, "微信登录过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        CreateUserCommand command = CreateUserCommand.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .role("USER")
                .build();

        UserResponse user = userApi.createUser(command);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtUtil.blacklistToken(token);
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        throw new BusinessException(400, "无效的请求");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Long userId = SecurityContextUtil.getCurrentUserId();

        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }

        UserResponse user = userApi.getUser(GetUserQuery.builder().userId(userId).build());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {

        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }

        // 获取用户信息验证旧密码
        UserResponse user = userApi.getUser(GetUserQuery.builder().userId(userId).build());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证旧密码
        String identifier = user.getEmail() != null ? user.getEmail() : user.getUsername();
        boolean valid = userApi.verifyPassword(
                VerifyPasswordQuery.builder()
                        .identifier(identifier)
                        .password(request.getOldPassword())
                        .build()
        );

        if (!valid) {
            throw new BusinessException(401, "旧密码不正确");
        }

        // 修改密码
        userApi.changePassword(
                ChangePasswordCommand.builder()
                        .userId(userId)
                        .newPassword(request.getNewPassword())
                        .build()
        );

        // 使当前 Token 失效
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtUtil.blacklistToken(authHeader.substring(7));
        }

        return ResponseEntity.ok(ApiResponse.success(true));
    }

    /**
     * 生成登录响应
     */
    private LoginResponse generateLoginResponse(UserResponse user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        claims.put("userId", user.getId());

        String token = jwtUtil.generateToken(claims, user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .userInfo(user)
                .build();
    }
}
