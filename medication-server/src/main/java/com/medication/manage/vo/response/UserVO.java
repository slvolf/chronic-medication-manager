package com.medication.manage.vo.response;

import lombok.Data;

/**
 * 用户信息（脱敏后返回，不包含密码）
 */
@Data
public class UserVO {
    private Long id;
    private String phone;
    private String email;
    private String name;
    private Integer age;
    private Integer gender;
}
