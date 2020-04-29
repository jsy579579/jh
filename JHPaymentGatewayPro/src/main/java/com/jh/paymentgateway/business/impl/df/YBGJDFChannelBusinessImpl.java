package com.jh.paymentgateway.business.impl.df;

import com.jh.paymentgateway.business.df.YBGJDFChannelBusiness;
import com.jh.paymentgateway.pojo.ybgjdf.YbgjdfBankCode;
import com.jh.paymentgateway.repository.df.ybgjdf.YBGJDFBankCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class YBGJDFChannelBusinessImpl implements YBGJDFChannelBusiness {
    @Autowired
    private YBGJDFBankCodeRepository ybgjdfBankCodeRepository;
    @Autowired
    private EntityManager em;

    @Override
    public YbgjdfBankCode findYbgjdfBankCodeByBankName(String bankName) {
        em.clear();
        YbgjdfBankCode cjBindCard = ybgjdfBankCodeRepository.getCJBindCardByBankName(bankName);
        return cjBindCard;
    }
}
