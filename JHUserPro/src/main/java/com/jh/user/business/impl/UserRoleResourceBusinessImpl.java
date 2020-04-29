package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserRoleResourceBusiness;
import com.jh.user.pojo.JdpushHistory;
import com.jh.user.pojo.Resource;
import com.jh.user.pojo.Role;
import com.jh.user.pojo.RoleResource;
import com.jh.user.pojo.RoleResourceForm;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserRole;
import com.jh.user.repository.JdpushHistoryRepository;
import com.jh.user.repository.ResourceRepository;
import com.jh.user.repository.RoleRepository;
import com.jh.user.repository.RoleResourceRepository;
import com.jh.user.repository.UserRoleRepository;


@Service
public class UserRoleResourceBusinessImpl implements UserRoleResourceBusiness{

	/*
	 * 权限管理
	 */
	@Autowired
	private ResourceRepository  resourceRepository;
	
	/*
	 * 角色权限
	 * 
	 * */
	@Autowired
	private RoleResourceRepository roleRes;
	
	
	/***
	 * 角色管理
	 * 
	 * **/
	@Autowired
	private  RoleRepository roleRepository;
	
	
	/*
	 * 
	 * 用户角色管理
	 * 
	 * */
	@Autowired
	private UserRoleRepository userRoleRepository;
	
	
	
	/*
	 *推送管理 
	 * */
	@Autowired
	private JdpushHistoryRepository jdpushHistoryRepository;
	
	@Autowired
	private EntityManager em;
	
	/*
	 * 权限管理
	 */
	@Transactional
	@Override
	public Resource saveResource(Resource resource) {
		return resourceRepository.save(resource);
	}

	@Transactional
	@Override
	public void updateResource(long id,String resourceNo,String resourceName, String url) {
		
		resourceRepository.updateResourceById(id,resourceNo, resourceName, url);
	}
	
	@Transactional
	@Override
	public Page<Resource> findAllResource(String resourceNo,String resourceName,String url,Pageable pageable) {
		// TODO Auto-generated method stub
		if(!resourceNo.equals("")&& !resourceName.equals("")&&!url.equals("")){
			return resourceRepository.findAllPageResource(resourceNo, resourceName, url, pageable);
		}else
		if(!resourceNo.equals("")&& resourceName.equals("")&&url.equals("")){
			return resourceRepository.findAllPageResourceByNo(resourceNo, pageable);
		}else
		if(resourceNo.equals("")&& !resourceName.equals("")&&url.equals("")){
			return resourceRepository.findAllPageResourceByName(resourceName, pageable);
		}else
		if(resourceNo.equals("")&& resourceName.equals("")&&!url.equals("")){
			return resourceRepository.findAllPageResourceByUrl( url, pageable);
		}else
		if(!resourceNo.equals("")&& !resourceName.equals("")&&url.equals("")){
			return resourceRepository.findAllPageResourceByNoName(resourceNo, resourceName, pageable);
		}else
		if(!resourceNo.equals("")&& resourceName.equals("")&&!url.equals("")){
			return resourceRepository.findAllPageResourceByNoUrl(resourceNo, url, pageable);
		}else
		if(resourceNo.equals("")&& !resourceName.equals("")&&!url.equals("")){
			return resourceRepository.findAllPageResourceByNameUrl(resourceNo, url, pageable);
		}
		return resourceRepository.findAll(pageable);
	}
	
	
	@Transactional
	@Override
	public List<Resource> findAllResource(long id){
		
		return resourceRepository.findAllResourceById(id);
	}

	@Transactional
	@Override
	public void delResourceById(long id) {
		// TODO Auto-generated method stub
		resourceRepository.delResourceById(id);
	}
	/*
	 * 角色权限管理
	 */
	
	//新增角色权限
	@Transactional
	@Override
	public RoleResource saveRoleResource(RoleResource roleResource) {
		// TODO Auto-generated method stub
		
		List<RoleResource> roleResourceList =roleRes.findResourceByroleid(roleResource.getRoleid(), roleResource.getResourceid());
		if(roleResourceList.size()==0)
		return roleRes.save(roleResource);
		if(roleResourceList.size()>0){
			RoleResource rr=roleResourceList.get(0);
			if(rr.getStatus().equals("1")){
				return roleRes.updateRoleResourceById(roleResource.getRoleid(), roleResource.getResourceid(), rr.getId());
			}
		}
		return roleResourceList.get(0);
	}
	
	//查询角色权限
	@Transactional
	@Override
	public Page<RoleResource> findAllRoleResourcePage(long roleid ,  long resourceid,Pageable pageable) {
		// TODO Auto-generated method stub
		if(roleid!=0 && resourceid!=0){
			return	roleRes.findAllRoleResourcePage(roleid, resourceid, pageable);
		}
		if(roleid==0 && resourceid!=0){
			return	roleRes.findAllRoleResourcePageByResourceid(resourceid, pageable);
		}
		return roleRes.findAll(pageable);
	}
	//通过角色ID查询资源
	@Transactional
	@Override
	public Page<RoleResourceForm> findAllResourcePageBYRole(long roleid ,  long resourceid,Pageable pageable){
		
		if(roleid!=0 && resourceid!=0){
			return	roleRes.findAllResourcePageById(roleid, resourceid, pageable);
		}
		if(roleid==0 && resourceid!=0){
			return	roleRes.findAllResourcePageByResourceid(resourceid, pageable);
		}
		if(roleid!=0 && resourceid==0){
			return roleRes.findAllResourcePageByRoleID(roleid, pageable);
		}
		return roleRes.findAllResourcePageByAll(pageable);
	}

	//删除权限（物理删除）
	@Transactional
	@Override
	public void delRoleResource(long roleid, long resourceid) {
		// TODO Auto-generated method stub
		 roleRes.delRoleResource(roleid, resourceid);
	}

	
	//修改角色权限
	@Transactional
	@Override
	public void upRoleResource(long roleid, long resourceid, long resourceno) {
		// TODO Auto-generated method stub
		roleRes.updateRoleResourceByRoidReid(roleid, resourceid, resourceno);
	}

	/*
	 * 
	 *角色管理
	 * ***/	
	//查询角色信息
	@Transactional
	@Override
	public Page<Role> rolepage(String rolecode,String rolename,Pageable pageable) {
		// TODO Auto-generated method stub
		if(!rolecode.equals("")&&!rolename.equals("")){
			roleRepository.findAllRole(rolecode, rolename, pageable);
			
		}
		if(rolecode.equals("")&&!rolename.equals("")){
			roleRepository.findAllRoleByRolename(rolename, pageable);
			
		}
		if(!rolecode.equals("")&&rolename.equals("")){
			roleRepository.findAllRoleByRolecode(rolecode, pageable);
			
		}
		return roleRepository.findAll(pageable);
	}

	@Transactional
	@Override
	public List<Role> findAllRole(long id){
		
		return roleRepository.findAllRoleById(id);
	}
	
	//新增角色
	@Transactional
	@Override
	public Role addrole(Role role) {
		
		return roleRepository.save(role);
	}

	//修改角色
	@Transactional
	@Override
	public void updateRole(long id, String rolename,String rolecode) {
		// TODO Auto-generated method stub
		roleRepository.updateRoleById(id,rolename,rolecode );
	}

	
	//删除角色
	@Transactional
	@Override
	public void delRole(long id) {
		// TODO Auto-generated method stub
		roleRepository.delRoleById(id);
	}
	/**
	 * 用户角色管理
	 * **/
	//添加用户角色
	@Transactional
	@Override
	public UserRole adduserRole(UserRole role) {
		
		return userRoleRepository.save(role);
	}
	//查询用户角色
	@Transactional
	@Override
	public Page<UserRole> UserRolepage(long userId,long roleId,Pageable pageable) {
		if(userId!=0&&roleId!=0){
			
			return userRoleRepository.findAllUserRole(userId, roleId, pageable);
		}
		if(userId==0&&roleId!=0){
			
			return userRoleRepository.findAllUserRoleByroleId(roleId, pageable);
		}
		if(userId!=0&&roleId==0){
			return userRoleRepository.findAllUserRoleByuserId(userId, pageable);
		}
		return userRoleRepository.findAll(pageable) ;
	}
	
	//查询用户角色
	public Page<Object> UserRolepageByuserId(long userId,long roleId,Pageable pageable){
		
		if(userId!=0&&roleId!=0|userId!=0&&roleId==0){
					
			return userRoleRepository.UserRolepageByuserId( userId, pageable);
		}
		if(userId==0&&roleId!=0){
			
			return userRoleRepository.UserRolepageByuserId(roleId, pageable);
		}
		return userRoleRepository.UserRolepageALL(pageable);
	}
	
	public UserRole UserRolepageByRUid(long userId){
		UserRole userRole=new UserRole();
		userRole=userRoleRepository.UserRolepageByRUid(userId);
		return userRole;
	}
	
	//修改用户角色
	@Transactional
	@Override
	public void upuserRole(long userId, long roleId) {
		// TODO Auto-generated method stub
		userRoleRepository.updateUserRoleByUserId(userId, roleId);
	}
    
	//删除用户角色
	@Transactional
	@Override
	public void delUserRole(long userId) {
		// TODO Auto-generated method stub
		userRoleRepository.delUserRole(userId);
	}
	
	/**
	 * 用户权限
	 * 
	 * **/
	
	public List<User> findAllUser(long id){
		return roleRepository.findUserById(id);
	}
	//权限查询
	@Transactional
	@Override
	public List<Resource> findUserResourceByuserId(long userId) {
		// TODO Auto-generated method stub
		
		System.out.println("你好--------------------------"+userId);
		return resourceRepository.findUserResourceByuserId(userId);
	}
	
	
	/**
	 * 推送管理
	 * 
	 * ***/
	@Transactional
	@Override
	public JdpushHistory addJdpushHistory(JdpushHistory jdh){
		JdpushHistory jdpushHistory = jdpushHistoryRepository.save(jdh);
		em.flush();
		em.clear();
		return jdpushHistory;
	}
	
	/**
	 * 推送查询
	 * */
	@Override
	public Page<JdpushHistory> findJdpushHistoryByuserId(long userId ,Pageable pageable){
		return jdpushHistoryRepository.findJdpushHistoryByuserId(userId,pageable);
		
	}
	
	/***
	 * 推送删除
	 * **/
	@Transactional
	@Override
	public void delJdpushHistoryByuserId(long userId,long brandId,String type){
		if(type.equals("androidVersion")){
			jdpushHistoryRepository.delJdpushHistoryByBrandId(brandId,type);
		}
		
		
	}
	
	//通过id删除推送
	@Transactional
	@Override
	public void delJdpushHistoryById(long id) {
		jdpushHistoryRepository.delJdpushHistoryById(id);
	}
	
	/**
	 * 推送查询
	 * */
	@Override
	public Page<JdpushHistory> findJdpushHistoryByBrandId(long brandId ,Pageable pageable){
		
		return jdpushHistoryRepository.findJdpushHistoryByBrandId(brandId,pageable);
		
	}
	
	/**
	 * 删除用户冗余数据
	 * 
	 * **/
	@Transactional
	public void delJdpushHistoryByUid(long userid,long brandid){
		long[]  jdpids=jdpushHistoryRepository.findJdpushHistorytop30(userid,brandid);
		if(jdpids!=null){
			if(jdpids.length>30){
				for(int i = 30;i < jdpids.length-30;i++) {
					try {
						this.deleteById(jdpids[i]);
					} catch (ObjectOptimisticLockingFailureException e) {
						continue;
					}
				}
			}
		}
		
		
	}
	
	@Transactional
	public void deleteById(Long id) {
		jdpushHistoryRepository.delete(id);
		jdpushHistoryRepository.flush();
	}

	@Override
	public Page<UserRole> findUserRoleByRoleid(long usersid, Pageable pageable) {
		return userRoleRepository.findUserRoleByRoleid(usersid, pageable);
	}

	@Override
	public List<UserRole> findUserRoleByUserid(long userid) {
		return userRoleRepository.findUserRoleByUserid(userid);
	}

	@Override
	public List queryFindAll() {
		return userRoleRepository.queryFindAll();
	}

	@Override
	public List<UserRole> findUserRoleByBrandid(long brandid) {
		return userRoleRepository.findUserRoleByBrandid(brandid);
	}

	@Override
	public List<Object> findRoleResourceIdByuserId(long userId) {
		return resourceRepository.findRoleResourceIdByuserId(userId);
	}
	
	/*@Override
	public Resource findResourceById(String resourceNo) {
		// TODO Auto-generated method stub
		return resourceRepository.findResourceById(resourceNo);
	}*/
	
	/*
	 * 角色权限管理
	 */
	/*@Transactional
	@Override
	public RoleResource saveRoleResource(RoleResource roleResource) {
		return roleRes.save(roleResource);
	}

	@Override
	public Page<RoleResource> findAllRoleResourcePage(Pageable pageable) {
		// TODO Auto-generated method stub
		return roleRes.findAllRoleResourcePage(pageable);
	}
*/
	/*@Transactional
	@Override
	public void delRoleResource(long roleid, long resourceid) {
		// TODO Auto-generated method stub
		roleRes.delRoleResource(roleid, resourceid);
	}*/

	/*@Transactional
	@Override
	public void upRoleResource(long roleid, long resourceid,long resourceno) {
		// TODO Auto-generated method stub
		roleRes.upRoleResource(roleid, resourceid,resourceno);
	}
	
	
	 * 用户角色管理
	 
	@Override
	public Page<Role> rolepage(Pageable pageable) {
		// TODO Auto-generated method stub
		return rolemanage.findAll(pageable);
	}

	@Override
	public Role addrole(Role role) {
		// TODO Auto-generated method stub
		return rolemanage.save(role);
	}*/

	/*@Transactional
	@Override
	public void updateRole(long id, String rolename) {
		// TODO Auto-generated method stub
		rolemanage.updateRoleById(id, rolename);
	}

	@Transactional
	@Override
	public void delRole(long id) {
		// TODO Auto-generated method stub
		rolemanage.delRoleById(id);
	}*/

	/*
	 * 用户角色管理
	 */
	/*@Override
	public UserRole adduserRole(UserRole role) {
		// TODO Auto-generated method stub
		return userrole.save(role);
	}

	@Override
	public Page<UserRole> UserRolepage(Pageable pageable) {
		// TODO Auto-generated method stub
		return userrole.findAll(pageable);
	}*/

	/*@Transactional
	@Override
	public void upuserRole(long userId,long roleId) {
		// TODO Auto-generated method stub
		userrole.upuserRole(userId, roleId);
	}

	@Transactional
	@Override
	public void delUserRole(long userId) {
		// TODO Auto-generated method stub
		userrole.deluserRole(userId);
	}
*/
	/**
	 * 推送管理
	 * 
	 * ***/
	@Transactional
	@Override
	public JdpushHistory addJdpushHistoryTest(JdpushHistory jdh){
		em.clear();
		return jdpushHistoryRepository.save(jdh);
	}

	
}
