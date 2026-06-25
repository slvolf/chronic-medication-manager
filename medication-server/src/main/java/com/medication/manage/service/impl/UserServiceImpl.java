package com.medication.manage.service.impl;

import com.medication.manage.entity.User;
import com.medication.manage.exception.BusinessException;
import com.medication.manage.mapper.UserMapper;
import com.medication.manage.service.UserService;
import com.medication.manage.util.JwtUtil;
import com.medication.manage.vo.request.LoginRequest;
import com.medication.manage.vo.request.RegisterRequest;
import com.medication.manage.vo.request.UserUpdateRequest;
import com.medication.manage.vo.response.LoginResponse;
import com.medication.manage.vo.response.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查手机号是否已注册
        User existing = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone()));
        if (existing != null) {
            throw new BusinessException("该手机号已被注册");
        }

        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(request, user);
        // BCrypt 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userMapper.insert(user);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(user.getId(), token, user.getName());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据手机号或邮箱查找用户
        User user = null;
        if (StringUtils.hasText(request.getPhone())) {
            user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getPhone, request.getPhone()));
        } else if (StringUtils.hasText(request.getEmail())) {
            user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail()));
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(user.getId(), token, user.getName());
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段（只更新非空值）
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        userMapper.updateById(user);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
