package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateDTO {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度不能小于6位")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
} 