package com.medication.manage.service;

import com.medication.manage.entity.User;
import com.medication.manage.vo.request.LoginRequest;
import com.medication.manage.vo.request.RegisterRequest;
import com.medication.manage.vo.request.UserUpdateRequest;
import com.medication.manage.vo.response.LoginResponse;
import com.medication.manage.vo.response.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param request 注册参数
     * @return 登录响应（含 Token）
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录参数（手机号/邮箱 + 密码）
     * @return 登录响应（含 Token）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息
     * @param userId 用户ID
     * @return 用户信息（脱敏）
     */
    UserVO getUserInfo(Long userId);

    /**
     * 修改个人信息
     * @param userId  用户ID
     * @param request 修改参数
     * @return 更新后的用户信息
     */
    UserVO updateUserInfo(Long userId, UserUpdateRequest request);
}
