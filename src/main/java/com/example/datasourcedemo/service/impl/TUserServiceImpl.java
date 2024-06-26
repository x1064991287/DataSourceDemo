package com.example.datasourcedemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.datasourcedemo.domain.TUser;
import com.example.datasourcedemo.mapper.TUserMapper;
import com.example.datasourcedemo.service.TUserService;
import org.springframework.stereotype.Service;

/**
 * @author bxy
 * @description 针对表【t_user】的数据库操作Service实现
 * @createDate 2024-06-03 16:51:51
 */
@Service
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements TUserService {

}




