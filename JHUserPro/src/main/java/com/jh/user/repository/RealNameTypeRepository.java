package com.jh.user.repository;

import com.jh.user.pojo.RealNameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RealNameTypeRepository extends JpaRepository<RealNameType,String>, JpaSpecificationExecutor<RealNameType> {

    @Query("select r from RealNameType r")
    RealNameType queryRealNameType();
}
