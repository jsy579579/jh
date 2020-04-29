package com.jh.user.business.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.ThirdLeveDistributionBusiness;
import com.jh.user.pojo.ThirdLevelDistribution;
import com.jh.user.pojo.ThirdLevelRate;
import com.jh.user.pojo.ThirdLevelRebateRatio;
import com.jh.user.pojo.ThirdLevelRebateRatioNew;
import com.jh.user.pojo.ThirdLevelRebateRatioNew2;
import com.jh.user.repository.ThirdLevelDistributionRepository;
import com.jh.user.repository.ThirdLevelRateRepository;
import com.jh.user.repository.ThirdLevelRebateRatioNew2Repository;
import com.jh.user.repository.ThirdLevelRebateRatioNewRepository;
import com.jh.user.repository.ThirdLevelRebateRatioRepository;

@Service
public class ThirdLevelDistributionBusinessImpl implements ThirdLeveDistributionBusiness {

    @Autowired
    private ThirdLevelDistributionRepository thirdLevelDistributionRepository;

    @Autowired
    private ThirdLevelRebateRatioRepository thirdLevelRebateRepository;

    @Autowired
    private ThirdLevelRebateRatioNewRepository thirdLevelRebateRatioNewRepository;

    @Autowired
    private ThirdLevelRebateRatioNew2Repository thirdLevelRebateRatioNew2Repository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ThirdLevelRateRepository thirdLevelRateRepository;

    @Override
    public List<ThirdLevelDistribution> getAllThirdLevelPrd(long brand) {
        List<ThirdLevelDistribution> ThirdLevelDistributions = new ArrayList<ThirdLevelDistribution>();
        ThirdLevelDistributions = thirdLevelDistributionRepository.findAllThirdLevel(brand);
        return ThirdLevelDistributions;
    }


    @Override
    public List<ThirdLevelRebateRatio> getAllThirdRatio(long brand) {

        return thirdLevelRebateRepository.findAllThirdLevelRatio(brand);
    }

    @Transactional
    @Override
    public ThirdLevelDistribution mergeThirdDistribution(
            ThirdLevelDistribution distribution) {

        ThirdLevelDistribution result = thirdLevelDistributionRepository.save(distribution);
        em.flush();
        return result;

    }

    @Transactional
    @Override
    public void delThirdLevelByid(long id) {


        thirdLevelDistributionRepository.delThirdLevelByid(id);
    }


    @Transactional
    @Override
    public ThirdLevelRebateRatio mergeThirdLevelRebateRatio(
            ThirdLevelRebateRatio rebateRatio) {

        ThirdLevelRebateRatio result = thirdLevelRebateRepository.save(rebateRatio);
        em.flush();
        return result;

    }


    @Override
    public ThirdLevelDistribution getThirdLevelByBrandidandgrade(long brand,
                                                                 int grade) {
        // TODO Auto-generated method stub
        return thirdLevelDistributionRepository.findAllThirdLevelByBrandidandlevelStatus(brand, grade);
    }

    @Override
    public ThirdLevelDistribution getThirdLevelByBrandidandgradeNoStatus(long brand,
                                                                         int grade) {
        return thirdLevelDistributionRepository.findAllThirdLevelByBrandidandlevel(brand, grade);
    }

    @Override
    public ThirdLevelRebateRatio getThirdRatioByBrandidandprelevel(long brand,
                                                                   String prelevel) {

        return thirdLevelRebateRepository.findAllThirdLevelRatioBybrandidAndlevel(brand, prelevel);
    }


    @Override
    public ThirdLevelDistribution queryThirdLevelDistri(long thirdlevel) {

        return thirdLevelDistributionRepository.findAllThirdLevelByid(thirdlevel);
    }

    @Override
    public List<ThirdLevelRate> findAllThirdLevelRates(long thirdlevel) {

        return thirdLevelRateRepository.findAllThirdLevelRatesBylevelid(thirdlevel);
    }

    /**
     * 查询产品指定通道的费率
     **/
    @Override
    public ThirdLevelRate findAllThirdLevelRatesBylevelidAndChannelId(long thirdlevel, long channelId) {
        ThirdLevelRate thirdLevelRate = new ThirdLevelRate();

        thirdLevelRate = thirdLevelRateRepository.findAllThirdLevelRatesBylevelidAndChannelId(thirdlevel, channelId);

        return thirdLevelRate;
    }

    @Transactional
    @Override
    public ThirdLevelRate addThirdLevelRates(ThirdLevelRate tlr) {

        ThirdLevelRate result = thirdLevelRateRepository.save(tlr);
        em.flush();
        return result;
    }


    //根据brandid查询最高等级
    @Override
    public int findThirdLevelDistributionByBrandid(long brandid) {

        return thirdLevelDistributionRepository.findThirdLevelDistributionByBrandid(brandid);
    }


    //根据grade删除
    @Transactional
    @Override
    public void deleteThirdLevelDistributionByGrade(int grade, long brandId) {
        thirdLevelRebateRatioNewRepository.deleteThirdLevelRebateByBrandIdAndGrade1(grade + "", new Long(brandId));
        thirdLevelRebateRatioNewRepository.deleteThirdLevelRebateByBrandIdAndGrade2(grade, new Long(brandId));
        thirdLevelRebateRatioNew2Repository.deleteThirdLevelRebate2ByBrandIdAndGrade2(grade, new Long(brandId));
        thirdLevelRebateRatioNew2Repository.deleteThirdLevelRebate2ByBrandIdAndGrade1(grade + "", new Long(brandId));
        thirdLevelDistributionRepository.deleteThirdLevelDistributionByGrade(grade, brandId);
    }

    @Override
    public List<ThirdLevelRebateRatioNew> getAllThirdRatio(long brandid, Integer thirdLevelId) {
        return thirdLevelRebateRatioNewRepository.findByBrandIdAndThirdLevelId(brandid, thirdLevelId);
    }

    @Override
    public ThirdLevelRebateRatioNew getByBrandIdAndPreLevelAndThirdLevelId(Long brandid, Integer thirdLevelId,
                                                                           String preLevel) {
        return thirdLevelRebateRatioNewRepository.findByBrandIdAndPreLevelAndThirdLevelId(brandid, preLevel, thirdLevelId);
    }

    @Transactional
    @Override
    public ThirdLevelRebateRatioNew mergeThirdLevelRebateRatio(ThirdLevelRebateRatioNew ratioModel) {
        ThirdLevelRebateRatioNew model = thirdLevelRebateRatioNewRepository.saveAndFlush(ratioModel);
        em.clear();
        return model;
    }


    @Override
    public List<ThirdLevelRebateRatioNew> getAllThirdRatioByBrandId(Long brandid) {
        return thirdLevelRebateRatioNewRepository.findByBrandId(brandid);
    }


    @Override
    public ThirdLevelRebateRatioNew getThirdRatioById(Long id) {
        return thirdLevelRebateRatioNewRepository.findById(id);
    }


    @Override
    public List<ThirdLevelRate> queryThirdLevelRatesBylevelidAndChannelId(long[] thirdlevel, long channelId) {
        return thirdLevelRateRepository.queryThirdLevelRatesBylevelidAndChannelId(thirdlevel, channelId);
    }


    @Transactional
    @Override
    public void deleteThirdLevelThirdLevelRatesBylevelidAndChannelId(long[] thirdlevel, long channelId) {
        thirdLevelRateRepository.deleteThirdLevelThirdLevelRatesBylevelidAndChannelId(thirdlevel, channelId);
    }

    @Override
    public List<ThirdLevelRebateRatioNew2> getAllThirdRatio2(long brandid, Integer thirdLevelId) {
        return thirdLevelRebateRatioNew2Repository.findByBrandIdAndThirdLevelId(brandid, thirdLevelId);
    }

    @Override
    public ThirdLevelRebateRatioNew2 getThirdLevelRebateRatioNew2ByBrandIdAndPreLevelAndThirdLevelId(Long brandid, Integer thirdLevelId,
                                                                                                     String preLevel) {
        return thirdLevelRebateRatioNew2Repository.findByBrandIdAndPreLevelAndThirdLevelId(brandid, preLevel, thirdLevelId);
    }

    @Transactional
    @Override
    public ThirdLevelRebateRatioNew2 mergeThirdLevelRebateRatio2(ThirdLevelRebateRatioNew2 ratioModel) {
        ThirdLevelRebateRatioNew2 model = thirdLevelRebateRatioNew2Repository.saveAndFlush(ratioModel);
        em.clear();
        return model;
    }

    @Override
    public List<ThirdLevelRebateRatioNew2> getAllThirdRatio2ByBrandId(Long brandid) {
        return thirdLevelRebateRatioNew2Repository.findByBrandId(brandid);
    }

    @Override
    public ThirdLevelRebateRatioNew2 getThirdRatio2ById(Long id) {
        return thirdLevelRebateRatioNew2Repository.findById(id);
    }

    @Override
    public ThirdLevelDistribution findByPrice(String price) {
        List<ThirdLevelDistribution> thirdLevelDistributionList = thirdLevelDistributionRepository.findAll(new Specification<ThirdLevelDistribution>() {
            @Override
            public Predicate toPredicate(Root<ThirdLevelDistribution> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("money"), price);
            }
        });
        if (thirdLevelDistributionList != null) {
            return thirdLevelDistributionList.get(0);
        } else {
            return null;
        }
    }

}
