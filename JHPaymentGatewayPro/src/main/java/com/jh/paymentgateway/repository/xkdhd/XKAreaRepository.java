package com.jh.paymentgateway.repository.xkdhd;

import com.jh.paymentgateway.pojo.xkdhd.XKArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XKAreaRepository extends JpaRepository<XKArea, String>, JpaSpecificationExecutor<XKArea> {
    @Query("select t from XKArea t where t.parentId=?1")
    List<XKArea> getXKAreaByParentId(String parentId);
}
