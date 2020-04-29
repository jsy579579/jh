package com.jh.good.business.impl;

import com.jh.good.business.MomentsLikeBusiness;
import com.jh.good.business.MomentsMessageImgBusiness;
import com.jh.good.pojo.MomentsLike;
import com.jh.good.repository.MomentsLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


@Service
public class MomentsLikeBusinessImpl implements MomentsLikeBusiness {

    @Autowired
    MomentsLikeRepository momentsLikeRepository;

    @Override
    public MomentsLike findByUserIdAndMomentsId(Long userId, Long momentsId) {
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("userId"), userId));
                predicates.add(cb.equal(root.get("momentsId"), momentsId));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return momentsLikeRepository.findOne(specification);
    }

    @Override
    public void del(Long userId, Long momentsId) {
        momentsLikeRepository.del(userId,momentsId);
    }

    @Override
    public void add(Long userId, Long momentsId) {
        MomentsLike momentsLike = new MomentsLike(userId, momentsId);
        momentsLikeRepository.save(momentsLike);
    }
}
