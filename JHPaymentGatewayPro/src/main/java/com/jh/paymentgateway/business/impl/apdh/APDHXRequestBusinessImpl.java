package com.jh.paymentgateway.business.impl.apdh;

import com.jh.paymentgateway.business.apdh.APDHXRequestBusiness;
import com.jh.paymentgateway.pojo.apdh.APDHCityCode;
import com.jh.paymentgateway.pojo.apdh.APDHIps;
import com.jh.paymentgateway.pojo.apdh.APDHXBindCard;
import com.jh.paymentgateway.pojo.apdh.APDHXRegister;
import com.jh.paymentgateway.repository.apdh.APAddressRepository;
import com.jh.paymentgateway.repository.apdh.APDHIpsRepository;
import com.jh.paymentgateway.repository.apdh.APDHXBindCardRepository;
import com.jh.paymentgateway.repository.apdh.APDHXRegisterRepository;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

@Service
public class APDHXRequestBusinessImpl implements APDHXRequestBusiness {

    @Autowired
    private APDHXRegisterRepository apdhxRegisterRepository;

    @Autowired
    private APDHXBindCardRepository apdhxBindCardRepository;

    @Autowired
    private APAddressRepository apAddressRepository;

    @Autowired
    private APDHIpsRepository apdhIpsRepository;

    @Autowired
    private EntityManager em;

    @Override
    public APDHXRegister findAPDHXRegisterByIdCard(String idCard) {
        em.clear();
        APDHXRegister apdhxRegister = apdhxRegisterRepository.getBQRegisterByIdCard(idCard);
        return apdhxRegister;
    }

    @Override
    public APDHXBindCard findAPDHXBindCardByBankdCard(String bankCard) {
        em.clear();
        APDHXBindCard apdhxBindCard = apdhxBindCardRepository.getBQRegisterByBankCard(bankCard);
        return apdhxBindCard;
    }

    @Override
    public APDHXBindCard findAPDHXBindCardByBankdCard1(String bankCard) {
        em.clear();
        APDHXBindCard apdhxBindCard = apdhxBindCardRepository.getBQRegisterByBankCard1(bankCard);
        return apdhxBindCard;
    }

    @Override
    public List<APDHCityCode> findAPDHCityCode() {
        em.clear();
        List<APDHCityCode> apdhCityCodes = apAddressRepository.findByCityCodeLike0();
        return apdhCityCodes;
    }

    @Override
    public List<APDHCityCode> findAPDHCityCodeByCode(String code) {
        em.clear();
        List<APDHCityCode> apdhCityCodes = apAddressRepository.findByCityCodeLike1(code);
        return apdhCityCodes;
    }

    @Transactional
    @Override
    public APDHXRegister save(String bankCard, Date date, String idCard, String merchantCode, String phone, String userName) {
        APDHXRegister apdhxRegister = new APDHXRegister();
        apdhxRegister.setUsername(userName);
        apdhxRegister.setPhone(phone);
        apdhxRegister.setMerchantCode(merchantCode);
        apdhxRegister.setIdCard(idCard);
        apdhxRegister.setCreateTime(date);
        apdhxRegister.setBankCard(bankCard);
        APDHXRegister save = apdhxRegisterRepository.save(apdhxRegister);
        em.flush();
        return apdhxRegister;
    }

    @Transactional
    @Override
    public APDHXBindCard saveAPDHXBindCard(String bankCard, Date date, String idCard, String phone, String userName, String bindSerialNo) {
        APDHXBindCard apdhxBindCard = new APDHXBindCard();
        apdhxBindCard.setBankCard(bankCard);
        apdhxBindCard.setCreateTime(date);
        apdhxBindCard.setIdCard(idCard);
        apdhxBindCard.setPhone(phone);
        apdhxBindCard.setUserName(userName);
        apdhxBindCard.setBindSerialNo(bindSerialNo);
        APDHXBindCard save = apdhxBindCardRepository.save(apdhxBindCard);
        em.flush();
        return  apdhxBindCard;
    }
    @Transactional
    @Override
    public void saveAPDHXBindCard(APDHXBindCard apdhxBindCard) {
        apdhxBindCardRepository.save(apdhxBindCard);
        em.flush();
        return;
    }

    @Transactional
    @Override
    public APDHXBindCard saveAPDHXBindCardContainsBindId(String bankCard, Date date, String idCard, String phone, String userName, String bindSerialNo, String bindId) {
        APDHXBindCard apdhxBindCard = new APDHXBindCard();
        apdhxBindCard.setBankCard(bankCard);
        apdhxBindCard.setCreateTime(date);
        apdhxBindCard.setIdCard(idCard);
        apdhxBindCard.setPhone(phone);
        apdhxBindCard.setUserName(userName);
        apdhxBindCard.setBindSerialNo(bindSerialNo);
        apdhxBindCard.setBindId(bindId);
        APDHXBindCard save = apdhxBindCardRepository.save(apdhxBindCard);
        em.flush();
        return save;
    }

    @Override
    public List<APDHIps> findIpsByCity(String s) {
        return apdhIpsRepository.findIpsByCity(s);
    }

    @Override
    public List<APDHIps> findIpsByProvince(String province) {
        return apdhIpsRepository.findIpsByProvince(province);
    }
}
