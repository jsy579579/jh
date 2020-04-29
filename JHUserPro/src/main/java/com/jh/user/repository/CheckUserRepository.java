package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.CheckUser;

@Repository
public interface CheckUserRepository extends JpaRepository<CheckUser,String>,JpaSpecificationExecutor<CheckUser>{
	
	@Query(" from CheckUser checkuser where checkuser.id=:id")
	CheckUser queryUserById(@Param("id") long id);
	
}
