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

@Repository
public interface ResourceRepository extends 
	JpaRepository<Resource,String>,JpaSpecificationExecutor<Resource>{

	/*
	 * 权限管理
	 */
	@Query("select resource from  Resource resource where resource.status='0' ")
	List<Resource> findAllResource();
	
	@Query("select resource from  Resource resource where resource.status='0' and resource.id=:id  ")
	List<Resource> findAllResourceById(@Param("id") long id);
	
	@Query("select resource from Resource resource where resource.resourceNo=:resourceNo and resource.resourceName=:resourceName and  resource.url=:url")
	Page<Resource> findAllPageResource(@Param("resourceNo")String resourceNo,@Param("resourceName")String resourceName ,@Param("url")String url , Pageable pageable);
	
	@Query("select resource from Resource resource where resource.resourceNo=:resourceNo ")
	Page<Resource> findAllPageResourceByNo(@Param("resourceNo")String resourceNo, Pageable pageable);
	
	@Query("select resource from Resource resource where resource.resourceName=:resourceName ")
	Page<Resource> findAllPageResourceByName( @Param("resourceName")String resourceName , Pageable pageable);
	
	@Query("select resource from Resource resource where  resource.url=:url")
	Page<Resource> findAllPageResourceByUrl(@Param("url")String url , Pageable pageable);
	
	@Query("select resource from Resource resource where resource.resourceNo=:resourceNo and resource.resourceName=:resourceName ")
	Page<Resource> findAllPageResourceByNoName(@Param("resourceNo")String resourceNo,@Param("resourceName")String resourceName  , Pageable pageable);
	
	@Query("select resource from Resource resource where resource.resourceNo=:resourceNo and  resource.url=:url")
	Page<Resource> findAllPageResourceByNoUrl(@Param("resourceNo")String resourceNo,@Param("url")String url , Pageable pageable);
	
	@Query("select resource from Resource resource where  resource.resourceName=:resourceName and  resource.url=:url")
	Page<Resource> findAllPageResourceByNameUrl(@Param("resourceName")String resourceNo,@Param("url")String url , Pageable pageable);
	
	
	
	
	@Modifying
	@Query("update Resource set status='1' where id=:resourceid")
	void delResource(@Param("resourceid") long resourceid);
	
	
	@Modifying
	@Query("update Resource set  resourceNo=:resourceNo,resourceName=:resourceName,url=:url where id=:id")
	void updateResourceById(@Param("id")long id ,@Param("resourceNo") String resourceNo,@Param("resourceName") String resourceName,@Param("url") String url);
	
	
	@Modifying
	@Query("delete from Resource where id=:id")
	void delResourceById(@Param("id") long id);
	
	@Modifying
	@Query("select ro.id,re from  Resource re, RoleResource  rr , Role ro ,UserRole  ur , User u   "+
//							" inner join RoleResource  rr on re.id=rr.resourceid "+
//							" inner join  Role ro on rr.roleid=ro.id "+
//							" inner join UserRole  ur on ro.id=ur.roleId "+
//							" inner join User u on ur.userId=u.id "+
							" where re.id=rr.resourceid and rr.roleid=ro.id and ro.id=ur.roleId and ur.userId=u.id and u.id=:userId"
)
	List<Resource> findUserResourceByuserId(@Param("userId") long userId);
	
//	@Query("select resource from  Resource resource where resource.resourceNo=:resourceNo")
//	Resource findResourceById(@Param("resourceNo") String resourceNo);
	
	@Modifying
	@Query("select ro.id from Resource re, RoleResource  rr , Role ro ,UserRole ur , User u "+
							" where re.id=rr.resourceid and rr.roleid=ro.id and ro.id=ur.roleId and ur.userId=u.id and u.id=:userId")
	List<Object> findRoleResourceIdByuserId(@Param("userId") long userId);
	
}
