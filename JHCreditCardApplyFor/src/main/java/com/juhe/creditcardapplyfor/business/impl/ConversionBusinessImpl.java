package com.juhe.creditcardapplyfor.business.impl;


import com.juhe.creditcardapplyfor.business.ConversionBusiness;
import com.juhe.creditcardapplyfor.entity.ConversionEntity;
import com.juhe.creditcardapplyfor.repository.ConversionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author huhao
 * @title: LoanOrderServiceImpl
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002311:05
 */
@Transactional
@Service
public class ConversionBusinessImpl implements ConversionBusiness {


    @Autowired
    private ConversionRepository conversionRepository;


    @Override
    public ConversionEntity saveOrder(ConversionEntity conversionEntity) {
        return conversionRepository.save(conversionEntity);
    }

    @Override
    public ConversionEntity updateOrder(ConversionEntity conversionEntity) {
        return conversionRepository.save(conversionEntity);
    }

    @Override
    public ConversionEntity getOrderByClientNo(String clientNo) {
        return conversionRepository.findByClientNo(clientNo);
    }
}
