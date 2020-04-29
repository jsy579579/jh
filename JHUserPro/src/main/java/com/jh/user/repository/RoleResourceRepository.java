package com.jh.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.Resource;
import com.jh.user.pojo.RoleResource;
import com.jh.user.pojo.RoleResourceForm;

@Repository
public interface RoleResourceRepository extends 
	JpaRepository<RoleResource,String>,JpaSpecificationExecutor<RoleResource>{

	/*
	 * 权限管理
	 */
	@Query("select roleresource from  RoleResource roleresource where roleresource.status='0' ")
	List<RoleResource> findAllResource();
	
	@Query("select roleresource from  RoleResource roleresource where roleresource.roleid=:roleid and roleresource.resourceid=:resourceid ")
	List<RoleResource> findResourceByroleid(@Param("roleid") long roleid , @Param("resourceid") long resourceid);
	
	@Query("select roleresource from  RoleResource roleresource where roleresource.roleid=:roleid and roleresource.resourceid=:resourceid and roleresource.status='0' ")
	Page<RoleResource> findAllRoleResourcePage(@Param("roleid") long roleid , @Param("resourceid") long resourceid,Pageable pageable) ;
	
	
	@Query("select roleresource from  RoleResource roleresource where roleresource.roleid=:roleid and roleresource.status='0' ")
	Page<RoleResource> findAllRoleResourcePageByRoleid(@Param("roleid") long roleid ,Pageable pageable) ;
	
	@Query("select roleresource from  RoleResource roleresource where  roleresource.resourceid=:resourceid and roleresource.status='0' ")
	Page<RoleResource> findAllRoleResourcePageByResourceid(@Param("resourceid") long resourceid,Pageable pageable) ;
	
	//联合查询
	
	
	@Query("from  Resource resource, RoleResource roleresource ,Role role where  resource.id=roleresource.resourceid and roleresource.roleid=role.id")
	Page<RoleResourceForm> findAllResourcePageByAll(Pageable pageable) ;
	
	@Query("from  Resource resource, RoleResource roleresource ,Role role where  resource.id=roleresource.resourceid and roleresource.roleid=role.id and   role.id=:roleid  ")
	Page<RoleResourceForm> findAllResourcePageByRoleID(@Param("roleid") long roleid,Pageable pageable) ;
	
	@Query("from  Resource resource, RoleResource roleresource ,Role role where  resource.id=roleresource.resourceid and roleresource.roleid=role.id and   resource.id=:resourceid  ")
	Page<RoleResourceForm> findAllResourcePageByResourceid(@Param("resourceid") long resourceid,Pageable pageable) ;
	
	@Query("from  Resource resource, RoleResource roleresource ,Role role where  resource.id=roleresource.resourceid and roleresource.roleid=role.id and   role.id=:roleid  and   resource.id=:resourceid  ")
	Page<RoleResourceForm> findAllResourcePageById(@Param("roleid") long roleid , @Param("resourceid") long resourceid,Pageable pageable) ;

	
	
	
	@Query("select resource from  Resource resource, RoleResource roleresource where  roleresource.resourceid=resource.id and  roleresource.roleid=:roleid and  roleresource.status='0' ")
	Page<Resource> findAllResourcePageByRole(@Param("roleid") long roleid,Pageable pageable) ;
	
	@Modifying
	@Query("update RoleResource  set status='1' where roleid=:roleid and resourceid=:resourceid")
	public void delRoleResource(@Param("roleid") long roleid,@Param("resourceid")  long resourceid);
	
	
	@Modifying
	@Query("update RoleResource set resourceid=:resourceno where roleid=:roleid and resourceid=:resourceid")
	void updateRoleResourceByRoidReid(@Param("roleid")long roleid,@Param("resourceid") long resourceid, @Param("resourceno")long resourceno);
	
	
	@Modifying
	@Query("update RoleResource set  roleid=:roleid , resourceid=:resourceid ,status='0' where id=:id ")
	RoleResource updateRoleResourceById(@Param("roleid")long roleid,@Param("resourceid") long resourceid, @Param("id")long id);
	
}
