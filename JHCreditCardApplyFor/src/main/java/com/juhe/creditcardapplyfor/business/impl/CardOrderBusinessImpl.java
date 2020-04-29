package com.juhe.creditcardapplyfor.business.impl;



import com.juhe.creditcardapplyfor.business.CardOrderBusiness;
import com.juhe.creditcardapplyfor.entity.CardOrderEntity;
import com.juhe.creditcardapplyfor.repository.CardOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author huhao
 * @title: CardOrderServiceImpl
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/20 002020:07
 *
 */
@Transactional
@Service
public class CardOrderBusinessImpl implements CardOrderBusiness {

    private final CardOrderRepository cardOrderRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    public CardOrderBusinessImpl(CardOrderRepository cardOrderRepository) {
        this.cardOrderRepository = cardOrderRepository;
    }

    @Override
    public CardOrderEntity saveOrder(CardOrderEntity cardOrderEntity) {
        CardOrderEntity entity = cardOrderRepository.save(cardOrderEntity);
        em.flush();
        return entity;

    }

    @Override
    public CardOrderEntity updateOrder(CardOrderEntity cardOrderEntity) {
        CardOrderEntity entity = cardOrderRepository.save(cardOrderEntity);
        em.flush();
        return entity;
    }

    @Override
    public CardOrderEntity getOrderByClientNo(String clientNo) {
        return cardOrderRepository.findByClientNo(clientNo);
    }


    @Override
    public List<CardOrderEntity> listOrderByPhoneAndIdCard(String phone, String idCard, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByPhoneAndIdCard(phone,idCard,pageable);
    }

    @Override
    public List<CardOrderEntity> listOrderByPhone(String phone, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByPhone(phone,pageable);
    }

    @Override
    public List<CardOrderEntity> listOrderByIdCard(String idCard, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByIdCard(idCard,pageable);
    }

    @Override
    public Page<CardOrderEntity> listOrder(Pageable pageable) {
        em.clear();
        return cardOrderRepository.findAll(pageable);
    }

    @Override
    public Page<CardOrderEntity> listOrderByClientNo(String clientNo, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByClientNo(clientNo,pageable);
    }

    @Override
    public Page<CardOrderEntity> listOrderByUserId(String userId, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByUserId(userId,pageable);
    }

    @Override
    public Page<CardOrderEntity> listOrderByUserName(String userName, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByUserName(userName,pageable);
    }

    @Override
    public Page<CardOrderEntity> listOrderByUserPhone(String userPhone, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByUserPhone(userPhone,pageable);
    }

    @Override
    public Page<CardOrderEntity> listOrderByCardId(String cardId, Pageable pageable) {
        em.clear();
        return cardOrderRepository.findByCardId(cardId,pageable);
    }

}
