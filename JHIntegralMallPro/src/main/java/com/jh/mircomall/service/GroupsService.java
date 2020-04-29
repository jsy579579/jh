package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.Groups;

public interface GroupsService {
	/**
	 * 获取所有分组
	 * 
	 * @return
	 */
	public List<Groups> getAllGruops(String brandId);

	/**
	 * 添加分組
	 * 
	 * @param groups
	 * @return
	 */
	public int addGroups(Groups groups);

	/**
	 * 自定义修改分组(包括删除)
	 * 
	 * @param groups
	 * @return
	 */
	public int modifyGroups(Groups groups);

	/**
	 * 查询分组是否重复
	 * 
	 * @param groupsName
	 * @param brandId
	 * @return
	 */
	public Groups getGroupsByName(String groupsName, String brandId);
	
	public int deleteGroups(int groupsId);

}
