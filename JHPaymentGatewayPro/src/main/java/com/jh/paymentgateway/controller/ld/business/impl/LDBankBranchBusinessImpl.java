package com.jh.paymentgateway.controller.ld.business.impl;

import com.jh.paymentgateway.controller.ld.business.LDBankBranchBusiness;
import com.jh.paymentgateway.controller.ld.dao.LDBankBranchRepository;
import com.jh.paymentgateway.controller.ld.pojo.LDBankBranch;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LDBankBranchBusinessImpl implements LDBankBranchBusiness {

    @Autowired
    private LDBankBranchRepository ldBankBranchRepository;

    @Override
    public LDBankBranch findByDebitCardName(String debitCardName) {
        return ldBankBranchRepository.findByDebitCardName(debitCardName);
    }
}
