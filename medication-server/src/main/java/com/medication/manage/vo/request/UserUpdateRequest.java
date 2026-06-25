package com.medication.manage.vo.request;

import lombok.Data;

/**
 * 修改个人信息请求参数
 */
@Data
public class UserUpdateRequest {
    private String name;     // 用户姓名
    private Integer age;     // 年龄
    private Integer gender;  // 性别
}
