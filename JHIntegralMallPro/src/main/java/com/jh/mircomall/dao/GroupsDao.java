package com.jh.mircomall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.Groups;

public interface GroupsDao {
	int addGroups(Groups groups);

	List<Groups> selectAllGroups(@Param("brandId") String brandId);

	int updateGroups(Groups groups);

	int deleteGroups(@Param("groupsId") int groupsId);

	Groups selectGroupsByName(@Param("groupsName") String groupsName, @Param("brandId") String brandId);
}
