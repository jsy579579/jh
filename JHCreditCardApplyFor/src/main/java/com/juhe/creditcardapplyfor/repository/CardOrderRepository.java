package com.juhe.creditcardapplyfor.repository;


import com.juhe.creditcardapplyfor.entity.CardOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author huhao
 * @title: CardOrderDao
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/20 002014:50
 *
 */

@Repository
public interface CardOrderRepository extends JpaRepository<CardOrderEntity,Integer>, JpaSpecificationExecutor<CardOrderEntity> {

    @Query(value = "select * from card_order where client_no = :clientNo", nativeQuery = true)
    CardOrderEntity findByClientNo(@Param("clientNo") String clientNo);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.userPhone = :phone and cardOrderEntity.cardId = :idCard")
    List<CardOrderEntity> findByPhoneAndIdCard(@Param("phone") String phone, @Param("idCard") String idCard, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.userPhone = :phone")
    List<CardOrderEntity> findByPhone(@Param("phone") String phone, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.cardId = :idCard")
    List<CardOrderEntity> findByIdCard(@Param("idCard") String idCard, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.clientNo = :clientNo")
    Page<CardOrderEntity> findByClientNo(@Param("clientNo") String clientNo, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.useId = :userId")
    Page<CardOrderEntity> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.userName = :userName")
    Page<CardOrderEntity> findByUserName(String userName, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.userPhone = :userName")
    Page<CardOrderEntity> findByUserPhone(String userPhone, Pageable pageable);

    @Query("select cardOrderEntity from CardOrderEntity cardOrderEntity where cardOrderEntity.cardId = :cardId")
    Page<CardOrderEntity> findByCardId(String cardId, Pageable pageable);
}
