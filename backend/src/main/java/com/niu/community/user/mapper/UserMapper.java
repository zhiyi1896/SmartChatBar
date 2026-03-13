package com.niu.community.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.niu.community.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
