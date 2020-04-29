package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.CJHKLRChannelWhite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @title: CJHKLRChannelWriteRepository
 * @projectName: DDSH
 * @description: TODO
 * @author: huhao
 * @date: 2019/12/5 12:56
 */
@Repository
public interface CJHKLRChannelWriteRepository extends JpaRepository<CJHKLRChannelWhite,Integer> , JpaSpecificationExecutor<CJHKLRChannelWhite> {
    CJHKLRChannelWhite findAllByChannelTypeAndAndStatusAndChannelCode(String channelType, String status, String channelCode);
}
