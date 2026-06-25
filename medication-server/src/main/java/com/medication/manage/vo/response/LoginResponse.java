package com.medication.manage.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录返回结果
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private Long userId;     // 用户ID
    private String token;    // JWT Token
    private String name;     // 用户姓名
}
