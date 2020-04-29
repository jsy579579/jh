package com.jh.good.business.impl;

import com.jh.good.business.CarouselBusiness;
import com.jh.good.pojo.Carousel;
import com.jh.good.repository.CarouselRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 轮播图
 */
@Service
public class CarouselBusinessImpl implements CarouselBusiness {

    @Autowired
    CarouselRepository carouselRepository;

    /**
     * 获取轮播图
     * @return
     */
    @Override
    public List<Carousel> findNeed() {
        Specification spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("status"), 0));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return carouselRepository.findAll(spec);
    }
}
