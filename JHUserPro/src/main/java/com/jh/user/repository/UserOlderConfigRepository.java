package com.jh.user.repository;


import com.jh.user.pojo.UserOlderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOlderConfigRepository extends JpaRepository<UserOlderConfig,Long>, JpaSpecificationExecutor<UserOlderConfig> {



}
