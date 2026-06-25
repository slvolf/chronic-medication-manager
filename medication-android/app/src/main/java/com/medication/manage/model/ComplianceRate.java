package com.medication.manage.model;

/**
 * 依从率统计
 */
public class ComplianceRate {
    private int plannedCount;   // 计划服药次数
    private int actualCount;    // 实际打卡次数
    private int missedCount;    // 漏服次数
    private double rate;        // 依从率百分比

    public int getPlannedCount() { return plannedCount; }
    public void setPlannedCount(int plannedCount) { this.plannedCount = plannedCount; }
    public int getActualCount() { return actualCount; }
    public void setActualCount(int actualCount) { this.actualCount = actualCount; }
    public int getMissedCount() { return missedCount; }
    public void setMissedCount(int missedCount) { this.missedCount = missedCount; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    /** 格式化百分比显示 */
    public String getRateText() {
        return String.format("%.1f%%", rate);
    }
}
