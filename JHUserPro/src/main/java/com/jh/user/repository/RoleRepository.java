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

import com.jh.user.pojo.Role;
import com.jh.user.pojo.User;

@Repository
public interface RoleRepository extends 
	JpaRepository<Role,String>,JpaSpecificationExecutor<Role>{

	/*
	 * 角色管理
	 */
	@Query("select role from  Role role  ")
	List<Role> findAllRole();
	
	@Query("select role from  Role role where role.id=:id ")
	List<Role> findAllRoleById(@Param("id") long id);
	
	@Query("select user from  User user where user.id=:userid")
	List<User> findUserById(@Param("userid") long userid);
	
	@Query("select role from  Role role where role.rolename=:rolename and role.rolecode=:rolecode ")
	Page<Role> findAllRole(@Param("rolecode") String rolecode,@Param("rolename") String rolename,Pageable pageable);
	
	@Query("select role from  Role role where role.rolename=:rolename  ")
	Page<Role> findAllRoleByRolename(@Param("rolename") String rolename,Pageable pageable);
	
	@Query("select role from  Role role where  role.rolecode=:rolecode ")
	Page<Role> findAllRoleByRolecode(@Param("rolecode") String rolecode,Pageable pageable);
	
	@Modifying
	@Query("update Role set rolename=:rolename ,rolecode=:rolecode where id=:id")
	void updateRoleById(@Param("id") long id,@Param("rolename") String rolename ,@Param("rolecode") String rolecode);
	
	
	@Modifying
	@Query("update Role set status='1' where id=:id")
	void delRoleById(@Param("id") long id);
	
	
}
