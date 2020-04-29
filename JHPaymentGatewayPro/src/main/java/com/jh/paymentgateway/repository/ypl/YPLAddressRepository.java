package com.jh.paymentgateway.repository.ypl;

import com.jh.paymentgateway.pojo.ypl.YPLAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface YPLAddressRepository extends JpaRepository<YPLAddress, String>, JpaSpecificationExecutor<YPLAddress> {
     @Query("select ypl  from YPLAddress ypl where ypl.parentId = ?1")
     List<YPLAddress> getYPLAddressByParentId(@Param("parentId") String parentId);
}
