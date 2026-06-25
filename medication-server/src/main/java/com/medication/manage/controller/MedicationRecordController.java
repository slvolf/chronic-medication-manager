package com.medication.manage.controller;

import com.medication.manage.service.MedicationRecordService;
import com.medication.manage.util.UserContext;
import com.medication.manage.vo.Result;
import com.medication.manage.vo.request.CheckInRequest;
import com.medication.manage.vo.response.MedicationRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 用药打卡模块控制器
 * 处理每日提醒生成、打卡、历史记录查询
 */
@RestController
@RequestMapping("/api/record")
public class MedicationRecordController {

    @Autowired
    private MedicationRecordService recordService;

    /**
     * 手动生成今日待服药记录
     * POST /api/record/generate
     */
    @PostMapping("/generate")
    public Result<?> generateTodayRecords() {
        Long userId = UserContext.getCurrentUserId();
        recordService.generateTodayRecords(userId);
        return Result.success();
    }

    /**
     * 打卡（标记为已服药）
     * POST /api/record/checkin
     */
    @PostMapping("/checkin")
    public Result<?> checkIn(@Valid @RequestBody CheckInRequest request) {
        Long userId = UserContext.getCurrentUserId();
        recordService.checkIn(userId, request);
        return Result.success();
    }

    /**
     * 标记为漏服
     * PUT /api/record/missed/{recordId}
     */
    @PutMapping("/missed/{recordId}")
    public Result<?> markMissed(@PathVariable Long recordId) {
        Long userId = UserContext.getCurrentUserId();
        recordService.markMissed(userId, recordId);
        return Result.success();
    }

    /**
     * 获取今日用药列表（待服/已服/漏服）
     * GET /api/record/today
     */
    @GetMapping("/today")
    public Result<List<MedicationRecordVO>> getTodayRecords() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(recordService.getTodayRecords(userId));
    }

    /**
     * 按日期查询打卡记录
     * GET /api/record/date?date=2024-01-15
     */
    @GetMapping("/date")
    public Result<List<MedicationRecordVO>> getRecordsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(recordService.getRecordsByDate(userId, date));
    }

    /**
     * 按日期范围查询打卡记录
     * GET /api/record/range?startDate=2024-01-01&endDate=2024-01-15
     */
    @GetMapping("/range")
    public Result<List<MedicationRecordVO>> getRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(recordService.getRecordsByDateRange(userId, startDate, endDate));
    }
}
