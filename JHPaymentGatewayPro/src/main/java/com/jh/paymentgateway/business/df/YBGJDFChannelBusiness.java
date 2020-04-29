package com.jh.paymentgateway.business.df;

import com.jh.paymentgateway.pojo.ybgjdf.YbgjdfBankCode;

public interface YBGJDFChannelBusiness {
    YbgjdfBankCode findYbgjdfBankCodeByBankName(String bankName);
}
