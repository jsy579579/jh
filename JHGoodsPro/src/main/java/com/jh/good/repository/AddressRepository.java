package com.jh.good.repository;

import com.jh.good.pojo.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AddressRepository extends JpaRepository<Address,String>, JpaSpecificationExecutor<Address> {

    @Modifying
    @Transactional
    @Query("delete from Address ad where ad.id = :id")
    void deleteById(@Param("id")Long id);

}
