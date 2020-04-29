package com.jh.user.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.CommonConstants;

import com.jh.user.business.UserRoleResourceBusiness;
import com.jh.user.pojo.Resource;
import com.jh.user.pojo.Role;
import com.jh.user.pojo.RoleResource;
import com.jh.user.pojo.UserRole;



@Controller
@EnableAutoConfiguration
public class UserRoleResourceService {

	
private static final Logger LOG = LoggerFactory.getLogger(UserRoleResourceService.class);
	
	@Autowired 
	private UserRoleResourceBusiness userRoleResourceBusiness;
	
	//新增权限
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/resource/create")
	public @ResponseBody Object createResource(HttpServletRequest request,   
			@RequestParam(value = "resource_no") String resourceNo,
			@RequestParam(value = "resource_name") String resourceName,	
			@RequestParam(value = "url") String url				
			){		
		
		
		Resource resource = new Resource();
		resource.setCreateTime(new Date());
		resource.setResourceName(resourceName);
		resource.setResourceNo(resourceNo);
		resource.setUrl(url);
		resource.setStatus("0");
		
		resource = userRoleResourceBusiness.saveResource(resource);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, resource);
		return map;
		
		
	}
	
	//修改权限
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/resource/update")
	public @ResponseBody Object updateResource(HttpServletRequest request,
			@RequestParam(value = "id") long id,
			@RequestParam(value = "resourceno") String resourceNo,
			@RequestParam(value = "resourcename") String resourceName,
			@RequestParam(value = "url") String url	
			){
		Map map = new HashMap();
		try {
			userRoleResourceBusiness.updateResource(id ,resourceNo,resourceName, url);
		} catch (Exception e) {
			// TODO: handle exception
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	
	//查询权限
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/resource/query")  
	public @ResponseBody Object pageResource(HttpServletRequest request,  
					 @RequestParam(value = "resourceno",defaultValue="", required = false) String resourceNo,
					 @RequestParam(value = "resourcename",defaultValue="", required = false) String resourceName,
					 @RequestParam(value = "url",defaultValue="", required = false) String url	,
					 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
					 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
					 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
					 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
					){
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		return userRoleResourceBusiness.findAllResource(resourceNo,resourceName,url,pageable);
	}
	
	//删除权限（物理删除）
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/user/resource/del")
	public @ResponseBody Object delResource(HttpServletRequest request,   
			@RequestParam(value = "id") long id			
			){	
		Map map = new HashMap();
		try {
			userRoleResourceBusiness.delResourceById(id);
		} catch (Exception e) {
			// TODO: handle exception
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/*//按照权限编号查询权限
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/resource/update")
	public @ResponseBody Object findResourceById(HttpServletRequest request,   
			@RequestParam(value = "resourceno") String resourceNo){
		return userRoleResourceBusiness.findResourceById(resourceNo);
	}*/
	
	/*
	 * 角色权限管理
	 */
	
	//新增角色权限
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/resource/add")  
	public @ResponseBody Object saveRoleResource(HttpServletRequest request,  
					 @RequestParam(value = "roleid") long roleid,
					 @RequestParam(value = "resourceid") long resourceid,
					 @RequestParam(value = "status", defaultValue = "0",required = false) String status,
					 @RequestParam(value = "createTime", defaultValue = "createTime", required = false) String createTime			
					){
		Map map = new HashMap();
		if( userRoleResourceBusiness.findAllResource(resourceid).size()>0&&userRoleResourceBusiness.findAllRole(roleid).size()>0){
			
			RoleResource roleResource=new RoleResource();
			roleResource.setRoleid(roleid);
			roleResource.setResourceid(resourceid);
			roleResource.setStatus(status);
			roleResource.setCreateTime(new Date());
			
			roleResource=userRoleResourceBusiness.saveRoleResource(roleResource);
			
		
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, roleResource);
			
		}
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
		map.put(CommonConstants.RESP_MESSAGE, "失败");
		
		return map;
	}
	
	//查询角色权限
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/resource/query")  
		public @ResponseBody Object findAllroleResource(HttpServletRequest request,  
						 @RequestParam(value = "roleid" , defaultValue = "0", required = false) long roleid,
						 @RequestParam(value = "resourceid", defaultValue = "0", required = false) long resourceid,
						 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
						 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
						 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
						 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
						){
			Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
			
			return userRoleResourceBusiness.findAllResourcePageBYRole( roleid ,  resourceid,pageable);
			
		}
		
	//删除角色权限
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/resource/del")  
		public @ResponseBody Object delroleResource(HttpServletRequest request,  
						 @RequestParam(value = "roleid") long roleid,
						 @RequestParam(value = "resourceid") long resourceid){
			Map map = new HashMap();
			try {
				userRoleResourceBusiness.delRoleResource(roleid, resourceid);
			} catch (Exception e) {
				// TODO: handle exception
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "失败");
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
			
		}
		
	//修改角色权限
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/resource/up")  
		public @ResponseBody Object uproleResource(HttpServletRequest request,  
						 @RequestParam(value = "roleid") long roleid,
						 @RequestParam(value = "resourceid") long resourceid,
						 @RequestParam(value = "resourceno") long resourceno){
			System.out.println(roleid+":"+resourceid+":"+resourceno);
			/*
			 * roleid角色编号
			 * resourceid权限编号（条件）
			 * resourceno权限编号（修改为哪个权限）
			 */
			Map map = new HashMap();
			try {
					userRoleResourceBusiness.upRoleResource(roleid,resourceid,resourceno);
			} catch (Exception e) {
				// TODO: handle exception
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "失败");
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		
		
	/*
	 * 
	 *角色管理
	 * ***/	
	//查询角色信息
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/find")  
		public @ResponseBody Object findrole(HttpServletRequest request,  
				@RequestParam(value = "rolecode" ,defaultValue = "", required = false) String rolecode,
				@RequestParam(value = "rolename" ,defaultValue = "", required = false) String rolename,
				@RequestParam(value = "page", defaultValue = "0", required = false) int page,
				@RequestParam(value = "size", defaultValue = "20", required = false) int size,
				@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
				@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty	){
			Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
			return userRoleResourceBusiness.rolepage(rolecode,rolename,pageable);
		}
		
	//新增角色
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/add")  
		public @ResponseBody Object addrole(HttpServletRequest request,  
						 @RequestParam(value = "rolecode") String rolecode,
						 @RequestParam(value = "rolename") String rolename,
						 @RequestParam(value = "status", defaultValue = "0",required = false) String status,
						 @RequestParam(value = "createTime", defaultValue = "createTime", required = false) String createTime			
						){
			Role role=new Role();
			role.setRolecode(rolecode);
			role.setRolename(rolename);
			role.setStatus(status);
			role.setCreateTime(new Date());
			
			role=userRoleResourceBusiness.addrole(role);
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, role);
			return map;
		}
		
		//修改角色
			@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/up")  
			public @ResponseBody Object uprole(HttpServletRequest request,  
							 @RequestParam(value = "id") long id,
							 @RequestParam(value = "rolename") String rolename,
							 @RequestParam(value = "rolecode") String rolecode
							){
				Map map = new HashMap();
					try {
						userRoleResourceBusiness.updateRole(id, rolename,rolecode);
					} catch (Exception e) {
						map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "失败");
						return map;
					}
					map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "成功");
					return map;
					
				}
		
		//删除角色
			@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/role/del")  
			public @ResponseBody Object delrole(HttpServletRequest request,  
							@RequestParam(value = "id") long id
							){
				
				Map map = new HashMap();
				try {
					userRoleResourceBusiness.delRole(id);
				} catch (Exception e) {
					// TODO: handle exception
					map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "失败");
					return map;
				}
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				return map;
			}
			
	/*
	 * 用户角色管理
	 * 
	 */		
	//添加用户角色
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/userrole/add")  
		public @ResponseBody Object adduserrole(HttpServletRequest request,  
					@RequestParam(value = "userId") long userId,
					@RequestParam(value = "roleId") long roleId,
					@RequestParam(value = "status" ,defaultValue = "0", required = false) String status,
					@RequestParam(value = "createTime" ,defaultValue = "createTime", required = false) String createTime){
			Map map = new HashMap();
			UserRole userRole=  userRoleResourceBusiness.UserRolepageByRUid(userId);
			if(userRole==null){
				userRole=new UserRole();
			}
			userRole.setUserId(userId);
			userRole.setRoleId(roleId);
			userRole.setStatus(status);
			userRole.setCreateTime(new Date());
			userRole=userRoleResourceBusiness.adduserRole(userRole);
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, userRole);
			return map;
		}
		
	//查询用户角色
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/userrole/find")  
		public @ResponseBody Object finduserrole(HttpServletRequest request,  
				@RequestParam(value = "userId" ,defaultValue = "0", required = false) long userId,
				@RequestParam(value = "roleId" ,defaultValue = "0", required = false) long roleId,
				@RequestParam(value = "page", defaultValue = "0", required = false) int page,
				 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
				 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
				 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty	){
			Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
			return userRoleResourceBusiness.UserRolepageByuserId(userId,roleId,pageable);
		}	
		
	//修改用户角色
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/userrole/up")  
		public @ResponseBody Object upuserrole(HttpServletRequest request,  
				@RequestParam(value = "userId") long userId,
				@RequestParam(value = "roleId") long roleId){
			Map map = new HashMap();
			try {
				userRoleResourceBusiness.upuserRole(userId, roleId);
			} catch (Exception e) {
				// TODO: handle exception
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "失败");
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		
	//删除用户角色
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/userrole/del")  
		public @ResponseBody Object deluserrole(HttpServletRequest request,  
				@RequestParam(value = "userId") long userId){
			Map map = new HashMap();
			try {
				System.out.println("传入的用户ID为"+userId);
				userRoleResourceBusiness.delUserRole(userId);
			} catch (Exception e) {
				// TODO: handle exception
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "失败");
				return map;
			}
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		
		
		/*
		 * 
		 * 用户权限管理
		 * 
		 */
		//查询用户权限
		
		@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/user/resource/find")
		public @ResponseBody Object finduserroleresource(HttpServletRequest request,  
				@RequestParam(value = "userId") long userId){
			List<Resource> brands = userRoleResourceBusiness.findUserResourceByuserId(userId);	
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, brands);
			return map; 
			
		}
		
		
	
}
