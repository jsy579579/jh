package com.jh.paymentgateway.controller.yxe.dao;

import com.jh.paymentgateway.controller.yxe.pojo.YXEAddress;
import com.jh.paymentgateway.controller.yxe.pojo.YXEBankBin;
import com.jh.paymentgateway.controller.yxe.pojo.YXERegister;
import com.jh.paymentgateway.controller.yxe.repository.YXEAddressRepository;
import com.jh.paymentgateway.controller.yxe.repository.YXEBankBinRepository;
import com.jh.paymentgateway.controller.yxe.repository.YXERegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;


@Service
public class YXEBusinesslmpl implements YXEBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    YXERegisterRepository yxeRegisterRepository;

    @Autowired
    YXEAddressRepository yxeAddressRepository;

    @Autowired
    YXEBankBinRepository yxeBankBinRepository;

    // 保存 or 修改用户绑卡信息
    @Transactional
    @Override
    public void createYXERegister(YXERegister yxeRegister) {
        yxeRegisterRepository.save(yxeRegister);
        em.flush();
    }
    // 获取用户绑卡信息
    @Override
    public YXERegister getYXERegisterByIdCard(String idCard, String bankCard) {
        em.clear();
        YXERegister result = yxeRegisterRepository.getYXERegisterByIdCard(idCard,bankCard);
        return result;
    }
    // 获取银行标识码
    @Override
    public YXEBankBin getYXEBankBinByBankName(String creditCardBankName) {
        List<YXEBankBin> bankBinList = yxeBankBinRepository.findAll(new Specification<YXEBankBin>() {
            @Override
            public Predicate toPredicate(Root<YXEBankBin> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("bankName"), creditCardBankName);
            }
        });
        if(bankBinList!=null && !bankBinList.isEmpty()){
            return bankBinList.get(0);
        }else{
            return null;
        }
    }
    // 获取地区标识码
    @Override
    public YXEAddress getYXEAddressByCityName(String cityName) {
        return yxeAddressRepository.findOne(new Specification<YXEAddress>() {
            @Override
            public Predicate toPredicate(Root<YXEAddress> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("cityName"),cityName);
            }
        });
    }

    @Override
    public YXERegister getYXERegisterByIdCard(String bankCard) {
        return yxeRegisterRepository.findOne(new Specification<YXERegister>() {
            @Override
            public Predicate toPredicate(Root<YXERegister> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("bankCard"),bankCard);
            }
        });
    }

    @Override
    public List<YXEAddress> findByCity(String provinceId) {
        return yxeAddressRepository.findAll(new Specification<YXEAddress>() {
            @Override
            public Predicate toPredicate(Root<YXEAddress> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("provinceId"),provinceId);
            }
        });
    }
}
