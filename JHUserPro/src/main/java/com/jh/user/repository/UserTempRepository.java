package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserTemp;

@Repository
public interface UserTempRepository extends JpaRepository<UserTemp, String>,JpaSpecificationExecutor<UserTemp>{

}
