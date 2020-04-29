package com.jh.user.business.impl;

import com.jh.user.business.BalanceTransfeRrecordbusiness;
import com.jh.user.pojo.BalanceTransfeRrecord;
import com.jh.user.repository.BalanceTransfeRrecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class BalanceTransfeRrecordImpl implements BalanceTransfeRrecordbusiness {


    @Autowired
    BalanceTransfeRrecordRepository alanceTransfeRrecordRepository;

    public BigDecimal queryAmountCount(Long Id, Date start,Date end) {
        return alanceTransfeRrecordRepository.queryAllAmount(Id,start,end);
    }

    public BalanceTransfeRrecord TrackRecord(BalanceTransfeRrecord projeck) {
        return alanceTransfeRrecordRepository.saveAndFlush(projeck);
    }
}
