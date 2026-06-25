package com.medication.manage.model;

/**
 * 用药记录（打卡记录）
 */
public class MedicationRecord {
    private Long id;
    private Long planId;
    private String drugName;        // 药品名称
    private String dosage;          // 剂量
    private String recordDate;      // 记录日期
    private String scheduledTime;   // 计划服用时间 "HH:mm"
    private String actualTime;      // 实际打卡时间
    private int status;             // 0-待服药 1-已服药 2-漏服
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getActualTime() { return actualTime; }
    public void setActualTime(String actualTime) { this.actualTime = actualTime; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    /** 返回状态文字描述 */
    public String getStatusText() {
        switch (status) {
            case 0: return "待服药";
            case 1: return "已服药";
            case 2: return "漏服";
            default: return "未知";
        }
    }
}
