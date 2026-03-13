package com.niu.community.sensitive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sensitive_word")
public class SensitiveWordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private Integer level;
    private LocalDateTime createTime;
}
