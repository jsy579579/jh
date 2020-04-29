package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class QYSHRegisterlmpl implements QYSHRegisterBusiness {

    @Autowired
    QYSHRegisterRepository qyshRegisterRepository;

    @Autowired
    QYSHBindcardRepository qyshBindcardRepository;

    @Autowired
    QYSHBankBranchRepository qyshBankBranchRepository;

    @Autowired
    QYSHmerchantRepository qysHmerchantRepository;

    @Autowired
    EntityManager em;


    @Transactional
    @Override
    public QYSHRegister create(QYSHRegister qyshRegister) {
        QYSHRegister result=qyshRegisterRepository.save(qyshRegister);
        em.flush();
        return result;
    }

    @Override
    public QYSHRegister getQYSHRegisterByIdCard(String idcard) {
        em.clear();
        QYSHRegister register=qyshRegisterRepository.getQYSHRegisterByIdCard(idcard);
        return register;
    }

    @Override
    public QYSHBindCard getQYSHBindCardByBankCard(String bankcard) {
        em.clear();
        QYSHBindCard result=qyshBindcardRepository.getQYSHBindCardByBankCard(bankcard);
        return result;
    }
    @Transactional
    @Override
    public QYSHBindCard create(QYSHBindCard qyshBindCard) {
        QYSHBindCard result=qyshBindcardRepository.save(qyshBindCard);
        em.flush();
        return result;
    }

    @Override
    public QYSHBankBranch getQYSHbanbranch(String bankname) {
        em.clear();
        QYSHBankBranch result=qyshBankBranchRepository.getBankBranchNo(bankname);
        return result;
    }

    @Override
    public List<QYSHmerchant> getHCMerchantProvince() {
        em.clear();
        List<QYSHmerchant> list=qysHmerchantRepository.getAllProvince();
        return list;
    }

    @Override
    public List<QYSHmerchant> getHCMerchantCity(String province) {
        em.clear();
        List<QYSHmerchant> result=qysHmerchantRepository.getAllCityByProvince(province);
        return result;
    }

    @Override
    public List<QYSHmerchant> getHCMerchantcounty(String city) {
        em.clear();
        List<QYSHmerchant> result=qysHmerchantRepository.getAllcountyByCity(city);
        return result;
    }

    @Override
    public List<QYSHmerchant> getHCMerchantmerabbreviation(String county) {
        em.clear();
        List<QYSHmerchant> result=qysHmerchantRepository.getAllMerabbreviationByCounty(county);
        return result;
    }

    @Override
    public QYSHmerchant merchantmerabbreviation(String merchantmerabbreviation) {
        em.clear();
        QYSHmerchant result=qysHmerchantRepository.getMerabbreviation(merchantmerabbreviation);
        return result;
    }

    @Override
    public List<QYSHmerchant> getindustryType(String county) {
        List<QYSHmerchant> result=qysHmerchantRepository.getAllindustryTypebyCounty(county);
        return result;
    }

    @Override
    public List<QYSHmerchant> getAllconsumeType(String county, String type) {
        em.clear();
        List<QYSHmerchant> result=qysHmerchantRepository.getAllindustryType(county,type);
        return result;
    }

    @Override
    public List<QYSHmerchant> getAll() {
        em.clear();
        List<QYSHmerchant> result=qysHmerchantRepository.getAll();
        return result;
    }

    @Override
    public List<QYSHmerchant> gettang(String businesslicense) {
        em.clear();
        List<QYSHmerchant> result=qysHmerchantRepository.gettang(businesslicense);
        return result;
    }

    @Transactional
    @Override
    public void deleteByUserAndPointIndecs(QYSHmerchant a) {
        qysHmerchantRepository.delete(a);
        em.flush();
    }

    @Override
    public void delet(ABC c) {

    }


}
