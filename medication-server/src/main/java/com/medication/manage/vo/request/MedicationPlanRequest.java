package com.medication.manage.vo.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 新增/编辑用药计划请求参数
 */
@Data
public class MedicationPlanRequest {
    private Long id;                    // 编辑时传入ID

    @NotBlank(message = "药品名称不能为空")
    private String drugName;            // 药品名称

    private String dosage;              // 服用剂量

    @NotNull(message = "频次模式不能为空")
    private Integer frequencyMode;      // 频次模式：1-每日固定次数 2-每日固定时间

    private Integer frequencyTimes;     // 每日次数（mode=1时使用）

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;        // 开始日期

    private LocalDate endDate;          // 结束日期（null表示长期）

    private String remark;              // 备注

    @NotNull(message = "提醒时间列表不能为空")
    private List<LocalTime> remindTimes; // 提醒时间列表
}
