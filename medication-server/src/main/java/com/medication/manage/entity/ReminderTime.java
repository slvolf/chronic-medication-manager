package com.medication.manage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒时间实体类
 * 存储用药计划的每日具体提醒时间点
 */
@Data
@TableName("reminder_time")
public class ReminderTime {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;        // 用药计划ID
    private LocalTime remindTime; // 提醒时间（如08:00、12:30）

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
