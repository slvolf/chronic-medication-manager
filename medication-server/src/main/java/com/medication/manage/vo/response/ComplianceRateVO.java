package com.medication.manage.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 依从率统计结果
 */
@Data
@AllArgsConstructor
public class ComplianceRateVO {
    private Integer plannedCount;    // 计划服药次数
    private Integer actualCount;     // 实际打卡次数
    private Integer missedCount;     // 漏服次数
    private Double rate;             // 依从率（百分比，如 75.0）
}
