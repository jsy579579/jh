package com.jh.good.business.impl;

import com.jh.good.business.MomentsLikeBusiness;
import com.jh.good.business.MomentsMessageBusiness;
import com.jh.good.pojo.MomentsLike;
import com.jh.good.pojo.MomentsMessage;
import com.jh.good.repository.MomentsMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
public class MomentsMessageBusinessImpl implements MomentsMessageBusiness {

    @Autowired
    MomentsMessageRepository momentsMessageRepository;

    @Autowired
    MomentsLikeBusiness momentsLikeBusiness;

    @Override
    public Page<MomentsMessage> searchGoods(int page, int size) {
        Pageable pageable = new PageRequest(page, size);
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("status"), "1")); // 审核通过的才查询
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return momentsMessageRepository.findAll(specification, pageable);
    }

    @Override
    public void publishNews(MomentsMessage momentsMessage) {
        momentsMessageRepository.save(momentsMessage);
    }

    @Override
    @Transactional
    public void giveTheThumbsUp(Long userId, Long momentsId) {
        MomentsLike momentsLike = momentsLikeBusiness.findByUserIdAndMomentsId(userId, momentsId);
        MomentsMessage momentsMessage = momentsMessageRepository.findOne(Long.valueOf(momentsId));
        if (momentsLike != null) {
            momentsMessage.setLikes(momentsMessage.getLikes() - 1);
            momentsLikeBusiness.del(userId,momentsId);
        } else {
            momentsMessage.setLikes(momentsMessage.getLikes() + 1);
            momentsLikeBusiness.add(userId,momentsId);
        }
    }

}
