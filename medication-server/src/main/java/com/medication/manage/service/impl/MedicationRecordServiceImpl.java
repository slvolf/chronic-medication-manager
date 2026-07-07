package com.medication.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medication.manage.entity.MedicationPlan;
import com.medication.manage.entity.MedicationRecord;
import com.medication.manage.entity.ReminderTime;
import com.medication.manage.exception.BusinessException;
import com.medication.manage.mapper.MedicationPlanMapper;
import com.medication.manage.mapper.MedicationRecordMapper;
import com.medication.manage.mapper.ReminderTimeMapper;
import com.medication.manage.service.MedicationRecordService;
import com.medication.manage.vo.request.CheckInRequest;
import com.medication.manage.vo.response.MedicationRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用药打卡服务实现类
 */
@Service
public class MedicationRecordServiceImpl implements MedicationRecordService {

    @Autowired
    private MedicationRecordMapper recordMapper;

    @Autowired
    private MedicationPlanMapper planMapper;

    @Autowired
    private ReminderTimeMapper reminderTimeMapper;

    @Override
    public void generateTodayRecords(Long userId) {
        LocalDate today = LocalDate.now();

        // 查询用户当前生效的用药计划
        List<MedicationPlan> activePlans = planMapper.selectList(
                new LambdaQueryWrapper<MedicationPlan>()
                        .eq(MedicationPlan::getUserId, userId)
                        .le(MedicationPlan::getStartDate, today)
                        .and(w -> w.isNull(MedicationPlan::getEndDate)
                                .or().ge(MedicationPlan::getEndDate, today)));

        // 每个计划独立生成，一个失败不影响其他
        for (MedicationPlan plan : activePlans) {
            try {
                // 检查该计划今日是否已有记录
                int existingCount = recordMapper.selectCount(
                        new LambdaQueryWrapper<MedicationRecord>()
                                .eq(MedicationRecord::getPlanId, plan.getId())
                                .eq(MedicationRecord::getRecordDate, today)).intValue();
                if (existingCount > 0) {
                    continue;
                }
                // 获取该计划的提醒时间列表
                List<ReminderTime> reminderTimes = reminderTimeMapper.selectList(
                        new LambdaQueryWrapper<ReminderTime>()
                                .eq(ReminderTime::getPlanId, plan.getId()));

                for (ReminderTime rt : reminderTimes) {
                    MedicationRecord record = new MedicationRecord();
                    record.setUserId(userId);
                    record.setPlanId(plan.getId());
                    record.setReminderTimeId(rt.getId());
                    record.setRecordDate(today);
                    record.setScheduledTime(rt.getRemindTime());
                    record.setStatus(0);
                    recordMapper.insert(record);
                }

                // 每日固定次数模式且无提醒时间时的回退逻辑
                if (plan.getFrequencyMode() == 1 && reminderTimes.isEmpty() && plan.getFrequencyTimes() != null
                        && plan.getFrequencyTimes() > 0) {
                    int times = plan.getFrequencyTimes();
                    for (int i = 0; i < times; i++) {
                        int hour = 24 / times * i;
                        MedicationRecord record = new MedicationRecord();
                        record.setUserId(userId);
                        record.setPlanId(plan.getId());
                        record.setRecordDate(today);
                        record.setScheduledTime(LocalTime.of(hour, 0));
                        record.setStatus(0);
                        recordMapper.insert(record);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long userId, CheckInRequest request) {
        MedicationRecord record = recordMapper.selectById(request.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        // 只允许对待服药状态的记录进行打卡
        if (record.getStatus() != 0) {
            throw new BusinessException("该记录已处理，无需重复打卡");
        }
        record.setStatus(1);                              // 已服药
        record.setActualTime(LocalDateTime.now());         // 记录实际打卡时间
        record.setRemark(request.getRemark());
        recordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMissed(Long userId, Long recordId) {
        MedicationRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该记录已处理");
        }
        record.setStatus(2); // 漏服
        recordMapper.updateById(record);
    }

    @Override
    public List<MedicationRecordVO> getTodayRecords(Long userId) {
        // 确保今日记录已生成
        generateTodayRecords(userId);

        LocalDate today = LocalDate.now();
        List<MedicationRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<MedicationRecord>()
                        .eq(MedicationRecord::getUserId, userId)
                        .eq(MedicationRecord::getRecordDate, today)
                        .orderByAsc(MedicationRecord::getScheduledTime));

        return convertToVOList(records);
    }

    @Override
    public List<MedicationRecordVO> getRecordsByDate(Long userId, LocalDate date) {
        // 如果是查询今天的记录，确保今日记录已生成
        if (date.equals(LocalDate.now())) {
            generateTodayRecords(userId);
        }

        List<MedicationRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<MedicationRecord>()
                        .eq(MedicationRecord::getUserId, userId)
                        .eq(MedicationRecord::getRecordDate, date)
                        .orderByAsc(MedicationRecord::getScheduledTime));

        return convertToVOList(records);
    }

    @Override
    public List<MedicationRecordVO> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // 如果日期范围内包含今天，确保今日记录已生成
        LocalDate today = LocalDate.now();
        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            generateTodayRecords(userId);
        }

        List<MedicationRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<MedicationRecord>()
                        .eq(MedicationRecord::getUserId, userId)
                        .between(MedicationRecord::getRecordDate, startDate, endDate)
                        .orderByAsc(MedicationRecord::getRecordDate)
                        .orderByAsc(MedicationRecord::getScheduledTime));

        return convertToVOList(records);
    }

    /**
     * 将数据库记录转换为 VO（关联查询药品名称）
     */
    private List<MedicationRecordVO> convertToVOList(List<MedicationRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有 planId 并批量查询计划信息（性能优化）
        Set<Long> planIds = records.stream()
                .map(MedicationRecord::getPlanId)
                .collect(Collectors.toSet());
        Map<Long, MedicationPlan> planMap = planMapper.selectBatchIds(planIds)
                .stream()
                .collect(Collectors.toMap(MedicationPlan::getId, p -> p,
                        (existing, replacement) -> existing));

        return records.stream()
                .map(record -> {
            MedicationRecordVO vo = new MedicationRecordVO();
            vo.setId(record.getId());
            vo.setPlanId(record.getPlanId());
            vo.setRecordDate(record.getRecordDate());
            vo.setScheduledTime(record.getScheduledTime());
            vo.setActualTime(record.getActualTime());
            vo.setStatus(record.getStatus());
            vo.setRemark(record.getRemark());

            // 填充药品名称和剂量
            MedicationPlan plan = planMap.get(record.getPlanId());
            if (plan != null) {
                vo.setDrugName(plan.getDrugName());
                vo.setDosage(plan.getDosage());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
