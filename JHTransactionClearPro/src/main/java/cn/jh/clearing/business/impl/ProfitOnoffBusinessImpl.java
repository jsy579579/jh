package cn.jh.clearing.business.impl;

import cn.jh.clearing.business.ProfitOnoffBusiness;
import cn.jh.clearing.pojo.ProfitOnoff;
import cn.jh.clearing.repository.ProfitOnoffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

/**
 * @title: ProfitOnoffBusinessImpl
 * @projectName: juhepayment
 * @description: TODO
 * @author: huhao
 * @date: 2019/9/9 9:39
 */
@Service
public class ProfitOnoffBusinessImpl implements ProfitOnoffBusiness {

    @Autowired
    private ProfitOnoffRepository profitOnoffRepository;

    @Autowired
    private EntityManager em;

    @Override
    public ProfitOnoff getProfitOnoffByBrandId(String brandId) {
        em.clear();
        return profitOnoffRepository.findByBrandId(brandId);
    }
}
