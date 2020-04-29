package com.juhe.creditcardapplyfor.business;

import com.juhe.creditcardapplyfor.entity.ConversionEntity;


public interface ConversionBusiness {

    ConversionEntity saveOrder(ConversionEntity conversionEntity);

    ConversionEntity updateOrder(ConversionEntity conversionEntity);

    ConversionEntity getOrderByClientNo(String clientNo);

}
