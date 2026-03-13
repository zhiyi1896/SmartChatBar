package com.niu.community.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer status;
    private String role;
    private Integer receivedLikeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
