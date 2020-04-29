package com.jh.paymentgateway.controller.ld.business;

import com.jh.paymentgateway.controller.ld.pojo.LDBankBranch;

public interface LDBankBranchBusiness {
    LDBankBranch findByDebitCardName(String debitCardName);
}
