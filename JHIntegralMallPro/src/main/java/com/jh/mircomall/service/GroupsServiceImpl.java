package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.Groups;
import com.jh.mircomall.dao.GoodsDao;
import com.jh.mircomall.dao.GroupsDao;

@Service
public class GroupsServiceImpl implements GroupsService {
	@Autowired
	private GroupsDao groupsDao;
	
	@Override
	public List<Groups> getAllGruops(String brandId) {
		return groupsDao.selectAllGroups(brandId);
	}

	@Override
	public int addGroups(Groups groups) {
		return groupsDao.addGroups(groups);
	}

	@Override
	public int modifyGroups(Groups groups) {
		return groupsDao.updateGroups(groups);
	}

	@Override
	public Groups getGroupsByName(String groupsName, String brandId) {
		return groupsDao.selectGroupsByName(groupsName, brandId);
	}

	@Override
	public int deleteGroups(int groupsId) {
		return groupsDao.deleteGroups(groupsId);
	}

}
