package com.medication.manage.service;

import com.medication.manage.vo.request.MedicationPlanRequest;
import com.medication.manage.vo.response.MedicationPlanVO;

import java.util.List;

/**
 * 用药计划服务接口
 */
public interface MedicationPlanService {

    /**
     * 新增用药计划
     * @param userId  用户ID
     * @param request 计划参数
     * @return 创建的用药计划
     */
    MedicationPlanVO createPlan(Long userId, MedicationPlanRequest request);

    /**
     * 编辑用药计划
     * @param userId  用户ID
     * @param request 计划参数（含ID）
     * @return 更新后的用药计划
     */
    MedicationPlanVO updatePlan(Long userId, MedicationPlanRequest request);

    /**
     * 删除用药计划（逻辑删除）
     * @param userId 用户ID
     * @param planId 计划ID
     */
    void deletePlan(Long userId, Long planId);

    /**
     * 获取用户的用药计划列表
     * @param userId 用户ID
     * @return 用药计划列表
     */
    List<MedicationPlanVO> getUserPlans(Long userId);

    /**
     * 获取单个用药计划详情
     * @param userId 用户ID
     * @param planId 计划ID
     * @return 用药计划详情
     */
    MedicationPlanVO getPlanDetail(Long userId, Long planId);
}
