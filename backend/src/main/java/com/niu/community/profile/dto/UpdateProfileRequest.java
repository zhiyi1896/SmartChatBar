package com.niu.community.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度在2-20之间")
    private String nickname;

    @Size(max = 255, message = "简介不能超过255字")
    private String bio;

    private String avatar;
}
