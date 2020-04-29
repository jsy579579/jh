package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserAwardConfig;

@Repository
public interface UserAwardConfigRepository extends JpaRepository<UserAwardConfig, Long>,JpaSpecificationExecutor<UserAwardConfig> {

	UserAwardConfig findByBrandId(String brandId);

}
