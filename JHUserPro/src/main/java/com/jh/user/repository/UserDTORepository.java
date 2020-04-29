package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserDTO;
@Repository
public interface UserDTORepository  extends JpaRepository<UserDTO,Long>,JpaSpecificationExecutor<UserDTO>{
	
	@Query("select u from  UserDTO u where u.realNameStatus in (0,1)")
	List<UserDTO> findByRealNameStatus();
	@Query("select u from UserDTO u where u.phone=:phone")
    UserDTO findByphone(@Param("phone") String phone);
}
