package com.medication.manage.vo.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用药记录返回结果（含药品名称）
 */
@Data
public class MedicationRecordVO {
    private Long id;
    private Long planId;
    private String drugName;        // 药品名称（关联查询）
    private String dosage;          // 剂量
    private LocalDate recordDate;   // 记录日期
    private LocalTime scheduledTime; // 计划服用时间
    private LocalDateTime actualTime; // 实际打卡时间
    private Integer status;         // 状态：0-待服药 1-已服药 2-漏服
    private String remark;
}
