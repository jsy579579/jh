package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.jh.user.pojo.JdpushHistory;
import com.jh.user.pojo.Resource;
import com.jh.user.pojo.Role;
import com.jh.user.pojo.RoleResource;
import com.jh.user.pojo.RoleResourceForm;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserRole;
import com.jh.user.pojo.UserRoleForm;

public interface UserRoleResourceBusiness {

	/*
	 * 权限管理
	 */
	public  Resource saveResource(Resource resource);
	
	public void updateResource(long id,String resourceNo,String resourceName,String url);
	
	public Page<Resource> findAllResource(String resourceNo,String resourceName,String url,Pageable pageable);
	
	public List<Resource> findAllResource(long id);
	
	public void delResourceById(long resourceNo);
	
	
//	Resource findResourceById(String resourceNo);
	
	/*
	 * 角色权限管理
	 */
	public RoleResource saveRoleResource(RoleResource roleResource);
	
	public Page<RoleResource> findAllRoleResourcePage( long roleid ,  long resourceid,Pageable pageable);
	
	public Page<RoleResourceForm> findAllResourcePageBYRole(long roleid ,  long resourceid,Pageable pageable);
	
	public void delRoleResource(long roleid,long resourceid);
	
	public void upRoleResource(long roleid,long resourceid,long resourceno);
	
	/*
	 * 角色管理
	 */
	public Page<Role> rolepage(String rolecode,String rolename,Pageable pageable);
	
	public List<Role> findAllRole(long id);
	
	public Role addrole(Role role);
	
	public void updateRole(long id,String rolename,String rolecode);
	
	public void delRole(long id);
	
	/*
	 * 用户角色管理
	 */
	public UserRole adduserRole(UserRole role);
	
	public Page<UserRole> UserRolepage(long userId,long roleId,Pageable pageable);
	
	public Page<Object> UserRolepageByuserId(long userId,long roleId,Pageable pageable);
	
	public UserRole UserRolepageByRUid(long userId);
	
	public void upuserRole(long userId,long roleId);
	
	public void delUserRole(long userId);
	
	/**
	 * 用户权限管理
	 * 
	 * */
	public List<User> findAllUser(long id);
	
	public List<Resource>  findUserResourceByuserId(long userId);
	
	

	/***
	 * 推送管理
	 * */
	/**
	 * 推送添加
	 * **/
	public JdpushHistory addJdpushHistory(JdpushHistory jdh);
	
	/**
	 * 个人推送查询
	 * */
	public Page<JdpushHistory> findJdpushHistoryByuserId(long userId ,Pageable pageable);
	
	/***
	 * 推送删除
	 * **/
	public void delJdpushHistoryByuserId(long userId,long brandId,String type);
	
	//通过id删除推送
	public void delJdpushHistoryById(long id);
	/**
	 * 查询平台推送
	 * 
	 * */
	public Page<JdpushHistory> findJdpushHistoryByBrandId(long brandId,Pageable pageable);
	
	public void delJdpushHistoryByUid(long userid,long brandid);
	
	//查询当前权限下所有直推用户信息
	public Page<UserRole> findUserRoleByRoleid(long usersid, Pageable pageable);
	
	//查询当前用户的权限下的所有成员
	public List<UserRole> findUserRoleByUserid(long userid);
	
	public List<UserRole> findUserRoleByBrandid(long brandid);
	
	public List<UserRole> queryFindAll();
	
	public List<Object> findRoleResourceIdByuserId(long userId);
	
	public JdpushHistory addJdpushHistoryTest(JdpushHistory jdh);
}
