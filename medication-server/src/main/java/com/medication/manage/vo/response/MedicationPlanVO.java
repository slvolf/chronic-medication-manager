package com.medication.manage.vo.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 用药计划返回结果（含提醒时间列表）
 */
@Data
public class MedicationPlanVO {
    private Long id;
    private Long userId;
    private String drugName;
    private String dosage;
    private Integer frequencyMode;      // 1-每日固定次数 2-每日固定时间
    private Integer frequencyTimes;
    private LocalDate startDate;
    private LocalDate endDate;
    private String remark;
    private List<LocalTime> remindTimes; // 提醒时间列表
}
