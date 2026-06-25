package com.medication.manage.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 基础配置
 */
@Configuration
@MapperScan("com.medication.manage.mapper")
public class MyBatisPlusConfig {
    // MyBatis-Plus 分页插件等可在此配置
}
