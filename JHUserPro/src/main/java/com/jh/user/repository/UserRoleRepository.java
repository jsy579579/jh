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

import com.jh.user.pojo.UserRole;
import com.jh.user.pojo.UserRoleForm;

@Repository
public interface UserRoleRepository extends 
	JpaRepository<UserRole,String>,JpaSpecificationExecutor<UserRole>{

	/*
	 * 权限管理
	 */
	@Query("select userrole from  UserRole userrole where userrole.status='0' ")
	List<UserRole> findAllUserRole();
	
	@Query("select userrole from  UserRole userrole where userrole.status='0'and userrole.userId=:userId and userrole.roleId=:roleId ")
	Page<UserRole> findAllUserRole(@Param("userId") long userId,@Param("roleId")long roleId,Pageable pageable);
	
	@Query("select userrole from  UserRole userrole where userrole.userId=:userId ")
	List<UserRole> findAllUserRoleByuserId(@Param("userId") long userId);
	
	@Query("select userrole from  UserRole userrole where userrole.status='0'and userrole.userId=:userId ")
	Page<UserRole> findAllUserRoleByuserId(@Param("userId") long userId,Pageable pageable);
	
	
	@Query("select userrole from  UserRole userrole where userrole.status='0' and userrole.roleId=:roleId ")
	Page<UserRole> findAllUserRoleByroleId(@Param("roleId")long roleId,Pageable pageable);
	
	@Query(" from Role role,UserRole userrole,User user where role.id=userrole.roleId and userrole.userId=user.id ")
	Page<Object> UserRolepageALL(Pageable pageable);
	
	@Query("select userrole from  UserRole userrole where  userrole.userId=:userId")
	UserRole UserRolepageByRUid(@Param("userId") long userId);
	
	@Query(" from Role role,UserRole userrole,User user where role.id=userrole.roleId and userrole.userId=user.id  and user.id=:userId ")
	Page<Object> UserRolepageByuserId(@Param("userId")long userId,Pageable pageable);
	
	@Query("from Role role,UserRole userrole,User user where role.id=userrole.roleId and userrole.userId=user.id  and role.id=:roleId ")
	Page<Object> UserRolepageByroleId(@Param("roleId")long roleId,Pageable pageable);
	
	@Query("select userrole from UserRole userrole where userrole.certigierUserId=:usersid")
	Page<UserRole> findUserRoleByRoleid(@Param("usersid") long usersid,Pageable pageable);
	
	@Query("select userrole from UserRole userrole where userrole.certigierUserId=:userid")
	List<UserRole> findUserRoleByUserid(@Param("userid") long userid);
	
	@Modifying
	@Query("update UserRole  set status='1' where userId=:userId")
	public void delUserRole(@Param("userId") long userId);
	
	
	@Modifying
	@Query("update UserRole set roleId=:roleId where userId=:userId ")
	void updateUserRoleByUserId(@Param("userId") long userId,@Param("roleId") long roleId);
	
	
	@Modifying
	@Query("update UserRole set roleId=:roleId,status='0' where userId=:userId ")
	UserRole updateUserRoleByStart(@Param("userId") long userId,@Param("roleId") long roleId);
	
	@Modifying
	@Query("from UserRole ur where ur.userId=:userId")
	UserRole findUserRoleByUserId(@Param("userId") long userId);
	
	//注销用户记录
	@Modifying
	@Query("delete from UserRole ur where ur.userId=:userid")
	void delUserRoleByUserid(@Param("userid") long userid);
	
	//临时
	@Query("select ur.userId from UserRole ur")
	List queryFindAll();
	
	@Query("select ur from UserRole ur where ur.brandId=:brandid")
	List<UserRole> findUserRoleByBrandid(@Param("brandid") long brandid);
	
	//临时
	@Modifying
	@Query("update UserRole set certigierUserId=:brandManage, brandId=:brandid where userId=:userid")
	void updateByAll(@Param("brandManage") long brandManage, @Param("userid") long userid, @Param("brandid") long brandid);
	
}
