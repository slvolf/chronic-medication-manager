package com.medication.manage.vo.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @javax.validation.constraints.Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    private String name;     // 用户姓名（选填）
    private Integer age;     // 年龄（选填）
    private Integer gender;  // 性别（选填）
}
