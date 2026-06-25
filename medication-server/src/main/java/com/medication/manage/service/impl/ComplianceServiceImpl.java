package com.medication.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medication.manage.entity.MedicationRecord;
import com.medication.manage.mapper.MedicationRecordMapper;
import com.medication.manage.service.ComplianceService;
import com.medication.manage.service.MedicationRecordService;
import com.medication.manage.vo.response.ComplianceRateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 用药依从率统计服务实现类
 *
 * 依从率 = 实际打卡次数 / 计划服药次数 × 100%
 * - 实际打卡次数：status = 1（已服药）的记录数
 * - 计划服药次数：status = 0（待服药）+ status = 1（已服药）+ status = 2（漏服）的总记录数
 */
@Service
public class ComplianceServiceImpl implements ComplianceService {

    @Autowired
    private MedicationRecordMapper recordMapper;

    @Autowired
    private MedicationRecordService recordService;

    @Override
    public ComplianceRateVO getTodayCompliance(Long userId) {
        // 确保今日记录已生成
        recordService.generateTodayRecords(userId);

        LocalDate today = LocalDate.now();
        return calculateCompliance(userId, today, today);
    }

    @Override
    public ComplianceRateVO getWeeklyCompliance(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 包含今天共7天
        return calculateCompliance(userId, sevenDaysAgo, today);
    }

    @Override
    public ComplianceRateVO getComplianceByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return calculateCompliance(userId, startDate, endDate);
    }

    /**
     * 计算指定日期范围内的依从率
     */
    private ComplianceRateVO calculateCompliance(Long userId, LocalDate startDate, LocalDate endDate) {
        // 查询日期范围内的所有记录
        LambdaQueryWrapper<MedicationRecord> query = new LambdaQueryWrapper<MedicationRecord>()
                .eq(MedicationRecord::getUserId, userId)
                .between(MedicationRecord::getRecordDate, startDate, endDate);

        // 统计总数
        Integer totalCount = recordMapper.selectCount(query).intValue();

        // 统计已打卡数（status = 1）
        query.eq(MedicationRecord::getStatus, 1);
        Integer takenCount = recordMapper.selectCount(query).intValue();

        // 统计漏服数（status = 2）
        query.clear(); // 重置条件
        query.eq(MedicationRecord::getUserId, userId)
                .between(MedicationRecord::getRecordDate, startDate, endDate)
                .eq(MedicationRecord::getStatus, 2);
        Integer missedCount = recordMapper.selectCount(query).intValue();

        // 计算依从率
        double rate = 0.0;
        if (totalCount > 0) {
            rate = (double) takenCount / totalCount * 100;
            // 保留一位小数
            rate = Math.round(rate * 10.0) / 10.0;
        }

        return new ComplianceRateVO(totalCount, takenCount, missedCount, rate);
    }
}
