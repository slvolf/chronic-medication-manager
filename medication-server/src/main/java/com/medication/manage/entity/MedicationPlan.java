package com.medication.manage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用药计划实体类
 */
@Data
@TableName("medication_plan")
public class MedicationPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;            // 用户ID
    private String drugName;        // 药品名称
    private String dosage;          // 服用剂量
    private Integer frequencyMode;  // 频次模式：1-每日固定次数 2-每日固定时间
    private Integer frequencyTimes; // 每日次数（mode=1时使用）
    private LocalDate startDate;    // 开始日期
    private LocalDate endDate;      // 结束日期（null表示长期）
    private String remark;          // 备注

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
