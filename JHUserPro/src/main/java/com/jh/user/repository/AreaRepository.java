package com.jh.user.repository;

import com.jh.user.pojo.AreaNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<AreaNew,Integer>, JpaSpecificationExecutor<AreaNew> {

    @Query("select area from AreaNew area where area.areaParentId = :areaParentId")
    List<AreaNew> findByAreaParentId(@Param("areaParentId")int areaParentId);

}
