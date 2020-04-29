package com.jh.user.repository;


import com.jh.user.pojo.UserManageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserManageConfigRepository extends JpaRepository<UserManageConfig,Long>, JpaSpecificationExecutor<UserManageConfig> {

    @Query("select u from UserManageConfig u where u.status=:status")
    List<UserManageConfig> findAllStatus(@Param("status") int status);
    @Query("select u from UserManageConfig u where u.brandId=:brandId")
    UserManageConfig findByBrandId(@Param("brandId") Long brandId);
}
