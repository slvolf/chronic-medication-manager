package com.medication.manage.service;

import com.medication.manage.vo.response.ComplianceRateVO;

import java.time.LocalDate;

/**
 * 用药依从率统计服务接口
 */
public interface ComplianceService {

    /**
     * 获取今日依从率
     * @param userId 用户ID
     * @return 依从率统计结果
     */
    ComplianceRateVO getTodayCompliance(Long userId);

    /**
     * 获取近7天平均依从率
     * @param userId 用户ID
     * @return 依从率统计结果（近7天合并）
     */
    ComplianceRateVO getWeeklyCompliance(Long userId);

    /**
     * 获取指定日期范围的依从率
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 依从率统计结果
     */
    ComplianceRateVO getComplianceByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
