package com.medication.manage.vo.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 打卡请求参数
 */
@Data
public class CheckInRequest {

    @NotNull(message = "记录ID不能为空")
    private Long recordId;      // 用药记录ID

    private String remark;      // 备注（选填）
}
