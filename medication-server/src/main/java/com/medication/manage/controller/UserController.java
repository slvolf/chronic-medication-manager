package com.medication.manage.controller;

import com.medication.manage.service.UserService;
import com.medication.manage.util.UserContext;
import com.medication.manage.vo.Result;
import com.medication.manage.vo.request.LoginRequest;
import com.medication.manage.vo.request.RegisterRequest;
import com.medication.manage.vo.request.UserUpdateRequest;
import com.medication.manage.vo.response.LoginResponse;
import com.medication.manage.vo.response.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户模块控制器
 * 处理注册、登录、个人信息等接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * POST /api/user/register
     * @param request 注册参数（手机号、密码、可选姓名/年龄/性别）
     * @return 登录响应（含Token）
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录
     * POST /api/user/login
     * @param request 登录参数（手机号/邮箱 + 密码）
     * @return 登录响应（含Token）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 获取当前用户个人信息
     * GET /api/user/info
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 修改个人信息
     * PUT /api/user/info
     * @param request 修改参数（姓名/年龄/性别）
     * @return 更新后的用户信息
     */
    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(@RequestBody UserUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(userService.updateUserInfo(userId, request));
    }
}
