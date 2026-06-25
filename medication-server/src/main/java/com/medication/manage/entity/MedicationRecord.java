package com.medication.manage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用药记录实体类（打卡记录）
 */
@Data
@TableName("medication_record")
public class MedicationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;            // 用户ID
    private Long planId;            // 用药计划ID
    private Long reminderTimeId;    // 提醒时间ID
    private LocalDate recordDate;   // 记录日期
    private LocalTime scheduledTime; // 计划服用时间
    private LocalDateTime actualTime; // 实际打卡时间（null表示未打卡）
    private Integer status;         // 状态：0-待服药 1-已服药 2-漏服
    private String remark;          // 备注

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
