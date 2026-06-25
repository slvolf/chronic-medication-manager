package com.medication.manage.vo.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 登录请求参数（支持手机号或邮箱登录）
 */
@Data
public class LoginRequest {
    private String phone;                // 手机号登录
    @Email(message = "邮箱格式不正确")
    private String email;                // 邮箱登录
    @NotBlank(message = "密码不能为空")
    private String password;
}
