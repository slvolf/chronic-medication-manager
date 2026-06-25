package com.medication.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medication.manage.entity.MedicationPlan;
import com.medication.manage.entity.ReminderTime;
import com.medication.manage.exception.BusinessException;
import com.medication.manage.mapper.MedicationPlanMapper;
import com.medication.manage.mapper.ReminderTimeMapper;
import com.medication.manage.service.MedicationPlanService;
import com.medication.manage.vo.request.MedicationPlanRequest;
import com.medication.manage.vo.response.MedicationPlanVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用药计划服务实现类
 */
@Service
public class MedicationPlanServiceImpl implements MedicationPlanService {

    @Autowired
    private MedicationPlanMapper planMapper;

    @Autowired
    private ReminderTimeMapper reminderTimeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicationPlanVO createPlan(Long userId, MedicationPlanRequest request) {
        // 校验同一用户同一药品名不可重复
        checkDuplicateDrugName(userId, null, request.getDrugName());

        // 创建用药计划
        MedicationPlan plan = new MedicationPlan();
        BeanUtils.copyProperties(request, plan);
        plan.setUserId(userId);
        planMapper.insert(plan);

        // 批量保存提醒时间
        saveRemindTimes(plan.getId(), request.getRemindTimes());

        return buildPlanVO(plan, request.getRemindTimes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicationPlanVO updatePlan(Long userId, MedicationPlanRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("计划ID不能为空");
        }

        // 校验计划归属
        MedicationPlan plan = planMapper.selectById(request.getId());
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException("用药计划不存在");
        }

        // 校验药品名重复（排除自身）
        checkDuplicateDrugName(userId, request.getId(), request.getDrugName());

        // 更新计划字段
        BeanUtils.copyProperties(request, plan);
        plan.setUserId(userId);
        planMapper.updateById(plan);

        // 删除旧提醒时间，重新插入
        reminderTimeMapper.delete(new LambdaQueryWrapper<ReminderTime>()
                .eq(ReminderTime::getPlanId, plan.getId()));
        saveRemindTimes(plan.getId(), request.getRemindTimes());

        return buildPlanVO(plan, request.getRemindTimes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long userId, Long planId) {
        MedicationPlan plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException("用药计划不存在");
        }
        // 逻辑删除计划（MyBatis-Plus @TableLogic 自动处理）
        planMapper.deleteById(planId);
        // 逻辑删除关联的提醒时间
        reminderTimeMapper.delete(new LambdaQueryWrapper<ReminderTime>()
                .eq(ReminderTime::getPlanId, planId));
    }

    @Override
    public List<MedicationPlanVO> getUserPlans(Long userId) {
        // 查询用户的所有用药计划
        List<MedicationPlan> plans = planMapper.selectList(
                new LambdaQueryWrapper<MedicationPlan>()
                        .eq(MedicationPlan::getUserId, userId));

        // 为每个计划组装提醒时间
        return plans.stream().map(this::buildPlanVOWithTimes).collect(Collectors.toList());
    }

    @Override
    public MedicationPlanVO getPlanDetail(Long userId, Long planId) {
        MedicationPlan plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException("用药计划不存在");
        }
        return buildPlanVOWithTimes(plan);
    }

    /**
     * 校验同一用户下药品名是否重复
     * @param userId   用户ID
     * @param planId   当前计划ID（编辑时传入，排除自身）
     * @param drugName 药品名称
     */
    private void checkDuplicateDrugName(Long userId, Long planId, String drugName) {
        LambdaQueryWrapper<MedicationPlan> query = new LambdaQueryWrapper<MedicationPlan>()
                .eq(MedicationPlan::getUserId, userId)
                .eq(MedicationPlan::getDrugName, drugName);
        if (planId != null) {
            query.ne(MedicationPlan::getId, planId);
        }
        if (planMapper.selectCount(query) > 0) {
            throw new BusinessException("已存在该药品的用药计划，请勿重复添加");
        }
    }

    /**
     * 保存提醒时间列表
     */
    private void saveRemindTimes(Long planId, List<LocalTime> remindTimes) {
        for (LocalTime time : remindTimes) {
            ReminderTime rt = new ReminderTime();
            rt.setPlanId(planId);
            rt.setRemindTime(time);
            reminderTimeMapper.insert(rt);
        }
    }

    /**
     * 构建 VO（从数据库查询提醒时间）
     */
    private MedicationPlanVO buildPlanVOWithTimes(MedicationPlan plan) {
        List<ReminderTime> times = reminderTimeMapper.selectList(
                new LambdaQueryWrapper<ReminderTime>()
                        .eq(ReminderTime::getPlanId, plan.getId()));
        List<LocalTime> remindTimes = times.stream()
                .map(ReminderTime::getRemindTime)
                .collect(Collectors.toList());
        return buildPlanVO(plan, remindTimes);
    }

    /**
     * 构建 VO（直接使用传入的提醒时间列表）
     */
    private MedicationPlanVO buildPlanVO(MedicationPlan plan, List<LocalTime> remindTimes) {
        MedicationPlanVO vo = new MedicationPlanVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setRemindTimes(remindTimes);
        return vo;
    }
}
