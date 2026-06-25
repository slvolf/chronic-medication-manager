package com.medication.manage.model;

import java.util.List;

/**
 * 用药计划
 */
public class MedicationPlan {
    private Long id;
    private Long userId;
    private String drugName;
    private String dosage;
    private Integer frequencyMode;  // 1-每日固定次数 2-每日固定时间
    private Integer frequencyTimes;
    private String startDate;
    private String endDate;
    private String remark;
    private List<String> remindTimes; // "HH:mm" 格式

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public Integer getFrequencyMode() { return frequencyMode; }
    public void setFrequencyMode(Integer frequencyMode) { this.frequencyMode = frequencyMode; }
    public Integer getFrequencyTimes() { return frequencyTimes; }
    public void setFrequencyTimes(Integer frequencyTimes) { this.frequencyTimes = frequencyTimes; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<String> getRemindTimes() { return remindTimes; }
    public void setRemindTimes(List<String> remindTimes) { this.remindTimes = remindTimes; }
}
