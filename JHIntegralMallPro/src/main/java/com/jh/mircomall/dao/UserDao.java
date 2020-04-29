package com.jh.mircomall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.User;

public interface UserDao {
	User getUserInfo(@Param("id") int id);
}
