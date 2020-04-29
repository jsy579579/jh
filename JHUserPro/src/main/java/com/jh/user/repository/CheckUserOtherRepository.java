package com.jh.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.CheckUserOther;

@Repository
public interface CheckUserOtherRepository extends JpaRepository<CheckUserOther, String>, JpaSpecificationExecutor<CheckUserOther>{
	
	@Query("select checkuser from CheckUserOther checkuser")
	Page<CheckUserOther> queryUserById(Pageable pageAble);
	
	@Query("select checkuser from CheckUserOther checkuser where checkuser.userid=:userid")
	List<CheckUserOther> queryUserByIds(@Param("userid") long userid);
	
	
}
