package com.medication.manage.service;

import com.medication.manage.vo.request.CheckInRequest;
import com.medication.manage.vo.response.MedicationRecordVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 用药打卡服务接口
 */
public interface MedicationRecordService {

    /**
     * 生成今日的待服药记录（由计划计算今日应服药时间）
     * 每次调用会检查今日是否已生成，避免重复生成
     * @param userId 用户ID
     */
    void generateTodayRecords(Long userId);

    /**
     * 用户打卡（标记为已服药）
     * @param userId  用户ID
     * @param request 打卡参数（记录ID + 备注）
     */
    void checkIn(Long userId, CheckInRequest request);

    /**
     * 标记为漏服
     * @param userId   用户ID
     * @param recordId 记录ID
     */
    void markMissed(Long userId, Long recordId);

    /**
     * 获取今日用药列表（待服/已服/漏服）
     * @param userId 用户ID
     * @return 今日用药记录列表
     */
    List<MedicationRecordVO> getTodayRecords(Long userId);

    /**
     * 按日期查询历史打卡记录
     * @param userId 用户ID
     * @param date   查询日期
     * @return 该日期的用药记录列表
     */
    List<MedicationRecordVO> getRecordsByDate(Long userId, LocalDate date);

    /**
     * 获取指定日期范围内的用药记录
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 用药记录列表
     */
    List<MedicationRecordVO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
