package com.jh.user.business;

import com.jh.user.pojo.BalanceTransfeRrecord;

import java.math.BigDecimal;
import java.util.Date;

public interface BalanceTransfeRrecordbusiness {

    public BigDecimal queryAmountCount(Long Id, Date start,Date end);

    public BalanceTransfeRrecord TrackRecord(BalanceTransfeRrecord projeck);
}