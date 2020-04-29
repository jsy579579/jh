package com.juhe.creditcardapplyfor.business;


import com.juhe.creditcardapplyfor.entity.CardOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardOrderBusiness {

    CardOrderEntity saveOrder(CardOrderEntity cardOrderEntity);

    CardOrderEntity updateOrder(CardOrderEntity cardOrderEntity);

    CardOrderEntity getOrderByClientNo(String clientNo);

    List<CardOrderEntity> listOrderByPhoneAndIdCard(String phone, String idCard, Pageable pageable);

    List<CardOrderEntity> listOrderByPhone(String phone, Pageable pageable);

    List<CardOrderEntity> listOrderByIdCard(String idCard, Pageable pageable);

    Page<CardOrderEntity> listOrder(Pageable pageable);

    Page<CardOrderEntity> listOrderByClientNo(String clientNo, Pageable pageable);

    Page<CardOrderEntity> listOrderByUserId(String userId, Pageable pageable);

    Page<CardOrderEntity> listOrderByUserName(String userName, Pageable pageable);

    Page<CardOrderEntity> listOrderByUserPhone(String userPhone, Pageable pageable);

    Page<CardOrderEntity> listOrderByCardId(String cardId, Pageable pageable);
}
