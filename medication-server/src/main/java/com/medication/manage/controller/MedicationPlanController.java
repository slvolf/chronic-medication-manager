package com.medication.manage.controller;

import com.medication.manage.service.MedicationPlanService;
import com.medication.manage.util.UserContext;
import com.medication.manage.vo.Result;
import com.medication.manage.vo.request.MedicationPlanRequest;
import com.medication.manage.vo.response.MedicationPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用药计划模块控制器
 * 处理用药计划的增删改查接口
 */
@RestController
@RequestMapping("/api/plan")
public class MedicationPlanController {

    @Autowired
    private MedicationPlanService planService;

    /**
     * 新增用药计划
     * POST /api/plan
     */
    @PostMapping
    public Result<MedicationPlanVO> createPlan(@Valid @RequestBody MedicationPlanRequest request) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(planService.createPlan(userId, request));
    }

    /**
     * 编辑用药计划
     * PUT /api/plan
     */
    @PutMapping
    public Result<MedicationPlanVO> updatePlan(@Valid @RequestBody MedicationPlanRequest request) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(planService.updatePlan(userId, request));
    }

    /**
     * 删除用药计划
     * DELETE /api/plan/{planId}
     */
    @DeleteMapping("/{planId}")
    public Result<?> deletePlan(@PathVariable Long planId) {
        Long userId = UserContext.getCurrentUserId();
        planService.deletePlan(userId, planId);
        return Result.success();
    }

    /**
     * 获取当前用户的所有用药计划列表
     * GET /api/plan/list
     */
    @GetMapping("/list")
    public Result<List<MedicationPlanVO>> getPlanList() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(planService.getUserPlans(userId));
    }

    /**
     * 获取单个用药计划详情
     * GET /api/plan/{planId}
     */
    @GetMapping("/{planId}")
    public Result<MedicationPlanVO> getPlanDetail(@PathVariable Long planId) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(planService.getPlanDetail(userId, planId));
    }
}
