package com.jh.user.repository;

import com.jh.user.pojo.VIPGiftOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGiftRepository extends JpaRepository<VIPGiftOrder,Integer>, JpaSpecificationExecutor<VIPGiftOrder> {

    @Query("from VIPGiftOrder vgo where vgo.userId = :userId")
    VIPGiftOrder findByUserId(@Param("userId") long userId);

    @Query("select vgo from VIPGiftOrder vgo where vgo.userPhone = :phone and vgo.orderCode = :orderCode")
    List<VIPGiftOrder> findOrderInfoPageByPhoneAndOrderCode(@Param("phone")String phone, @Param("orderCode")String orderCode, Pageable pageable);

    @Query("select vgo from VIPGiftOrder vgo where vgo.userPhone = :phone")
    List<VIPGiftOrder> findOrderInfoPageByPhone(@Param("phone")String phone, Pageable pageable);

    @Query("select vgo from VIPGiftOrder vgo where vgo.orderCode = :orderCode")
    List<VIPGiftOrder> findOrderInfoPageByOrderCode(@Param("orderCode")String orderCode, Pageable pageable);

    @Query("select vgo from VIPGiftOrder vgo")
    List<VIPGiftOrder> findAllPageable(Pageable pageable);

    @Query("select vgo from VIPGiftOrder vgo where vgo.userPhone = :phone and vgo.userName = :userName")
    List<VIPGiftOrder> findOrderInfoPageByPhoneAndUserName(@Param("phone")String phone, @Param("userName")String name, Pageable pageable);

    @Query("select vgo from VIPGiftOrder vgo where vgo.userName = :userName")
    List<VIPGiftOrder> findOrderInfoPageByUserName(@Param("userName")String name, Pageable pageable);
}
