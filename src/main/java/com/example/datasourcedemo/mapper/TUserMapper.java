package com.example.datasourcedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.datasourcedemo.domain.TUser;
import org.apache.ibatis.annotations.Mapper;

/**
* @author bxy
* @description 针对表【t_user】的数据库操作Mapper
* @createDate 2024-06-03 16:51:51
* @Entity generator.domain.TUser
*/
@Mapper
public interface TUserMapper extends BaseMapper<TUser> {

}




