package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.business.NewTopupPayChannelBusiness;
import com.jh.paymentgateway.pojo.hxdhd.HXDCity;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDBindCard;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDRegister;
import com.jh.paymentgateway.pojo.tldhx.TLDHXHHBindCard;
import com.jh.paymentgateway.pojo.tldhx.TLDHXHHRegister;
import com.jh.paymentgateway.repository.hx.HXDHDBingCardRepository;
import com.jh.paymentgateway.repository.hx.HXDHDCityRepository;
import com.jh.paymentgateway.repository.hx.HXDHDRegisterRepository;
import com.jh.paymentgateway.repository.tldhx.TLDHXHHBindCardRepository;
import com.jh.paymentgateway.repository.tldhx.TLDHXHHRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author huhao
 * @title: NewTopupPayChannelBusinessImpl
 * @projectName juhepay
 * @description: 新的通道业务类
 * @date 2019/8/15 11:48
 */

@Service
@Transactional
public class NewTopupPayChannelBusinessImpl implements NewTopupPayChannelBusiness {

    @Autowired
    private EntityManager em;

    @Autowired
    private HXDHDRegisterRepository hxdhdRegisterRepository;

    @Autowired
    private HXDHDBingCardRepository hxdhdBingCardRepository;

    @Autowired
    private HXDHDCityRepository hxdhdCityRepository;

    @Autowired
    private TLDHXHHRegisterRepository tldhxRegisterRepository;

    @Autowired
    private TLDHXHHBindCardRepository tldhxBindCardRepository;

    @Override
    public HXDHDRegister createHXDHDRegister(HXDHDRegister hxdhdRegister) {
        HXDHDRegister entity = hxdhdRegisterRepository.save(hxdhdRegister);
        em.flush();
        return entity;
    }

    @Override
    public HXDHDBindCard createHXDHDBindCard(HXDHDBindCard hxdhdBindCard) {
        HXDHDBindCard entity = hxdhdBingCardRepository.save(hxdhdBindCard);
        em.flush();
        return entity;
    }

    @Override
    public HXDHDRegister getHXDHDRegisterByIdCard(String idCard) {
        em.clear();
        HXDHDRegister hxdhdRegister = hxdhdRegisterRepository.getHXDHDRegisterByIdCard(idCard);
        return hxdhdRegister;
    }

    @Override
    public HXDHDBindCard getHXDHDBindCardByBankCard(String bankCard) {
        em.clear();
        HXDHDBindCard hxdhdBindCard = hxdhdBingCardRepository.getHXDHDBindCardByBankCard(bankCard);
        return hxdhdBindCard;
    }

    @Override
    public List<HXDCity> listAreaInfo(int id) {
        em.clear();
        return hxdhdCityRepository.findByPid(id);
    }

    @Override
    public HXDHDRegister getRegisterByBankCard(String accountNumber) {
        em.clear();
        return hxdhdRegisterRepository.getHXDHDRegisterByBankCard(accountNumber);
    }

    @Override
    public HXDHDRegister getHXDHDRegisterByBankCard(String bankCard) {
        return hxdhdRegisterRepository.getHXDHDRegisterByBankCard(bankCard);
    }

    @Override
    public TLDHXHHRegister getTLDHXRegisterByIdCard(String idCard) {
        return tldhxRegisterRepository.getTLDHXRegisterByIdCard(idCard);
        //return null;
    }

    @Override
    public TLDHXHHBindCard getTLDHXBindCardByBankCard(String bankCard) {
        return tldhxBindCardRepository.getTLDHXBindCardByBankCard(bankCard);
    }

    @Override
    public void createTLDHXRegister(TLDHXHHRegister tldhxhhRegister) {
        tldhxRegisterRepository.save(tldhxhhRegister);

    }

    @Override
    public void createTLDHXBindCard(TLDHXHHBindCard tldhxhhBindCard) {
        tldhxBindCardRepository.save(tldhxhhBindCard);
    }
}
