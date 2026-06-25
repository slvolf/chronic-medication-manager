package com.medication.manage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;       // 手机号
    private String email;       // 邮箱
    private String password;    // 密码（BCrypt加密）
    private String name;        // 用户姓名
    private Integer age;        // 年龄
    private Integer gender;     // 性别：0-未知 1-男 2-女

    @TableLogic
    private Integer deleted;    // 逻辑删除：0-未删除 1-已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
