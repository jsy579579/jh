package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.CJHKLRChannelCodeRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @title: CJHKLRChannelCodeRelationRepository
 * @projectName: DDSH
 * @description: TODO
 * @author: huhao
 * @date: 2019/12/6 15:14
 */
@Repository
public interface CJHKLRChannelCodeRelationRepository extends JpaRepository<CJHKLRChannelCodeRelation,Long>, JpaSpecificationExecutor<CJHKLRChannelCodeRelation> {
    CJHKLRChannelCodeRelation findByBankCard(String bankcard);
}
