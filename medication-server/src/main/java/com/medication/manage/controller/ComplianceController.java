package com.medication.manage.controller;

import com.medication.manage.service.ComplianceService;
import com.medication.manage.util.UserContext;
import com.medication.manage.vo.Result;
import com.medication.manage.vo.response.ComplianceRateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 依从率统计控制器
 */
@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    @Autowired
    private ComplianceService complianceService;

    /**
     * 获取今日依从率
     * GET /api/compliance/today
     */
    @GetMapping("/today")
    public Result<ComplianceRateVO> getTodayCompliance() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(complianceService.getTodayCompliance(userId));
    }

    /**
     * 获取近7天平均依从率
     * GET /api/compliance/weekly
     */
    @GetMapping("/weekly")
    public Result<ComplianceRateVO> getWeeklyCompliance() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(complianceService.getWeeklyCompliance(userId));
    }

    /**
     * 获取指定日期范围的依从率
     * GET /api/compliance/range?startDate=2024-01-01&endDate=2024-01-07
     */
    @GetMapping("/range")
    public Result<ComplianceRateVO> getComplianceByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(complianceService.getComplianceByDateRange(userId, startDate, endDate));
    }
}
