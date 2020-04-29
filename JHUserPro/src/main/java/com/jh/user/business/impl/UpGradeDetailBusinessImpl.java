package com.jh.user.business.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UpGradeDetailBusiness;
import com.jh.user.pojo.UpGradeDetail;
import com.jh.user.pojo.UpGradeDetail_;
import com.jh.user.repository.UpGradeDetailRepository;

@Service
public class UpGradeDetailBusinessImpl implements UpGradeDetailBusiness {

	
	@Autowired
	private EntityManager em;

	@Autowired
	private UpGradeDetailRepository upGradeDetailRepository;
	
	@Transactional
	@Override
	public void createUpGradeDetail(UpGradeDetail upGradeDetail) {
		upGradeDetailRepository.saveAndFlush(upGradeDetail);
		em.clear();
	}

	@Override
	public Page<UpGradeDetail> getAllUpGradeDetail(Pageable pageable) {
		
		return null;
	}

	@Override
	public Page<UpGradeDetail> getAllUpGradeDetailByBrandIdAndMore(int brandId, String phone, String modifyPhone,
			int modifyGrade, int modifyType, Date startTime, Date endTime, Pageable pageAble) {
		
		return upGradeDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
           
            predicatesList.add( criteriaBuilder.equal(root.get(UpGradeDetail_.brandId), brandId));
            
            if (isNotNull(phone)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(UpGradeDetail_.phone), phone)));
			}
            if (isNotNull(modifyPhone)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(UpGradeDetail_.modifyPhone), modifyPhone)));
			}
            if (modifyGrade != -1) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(UpGradeDetail_.modifyGrade), modifyGrade)));
			}
            if (modifyType != -1) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(UpGradeDetail_.modifyType), modifyType)));
			}
            if (startTime != null) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get(UpGradeDetail_.createTime), startTime)));
            }
            
            if (endTime != null) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get(UpGradeDetail_.createTime), endTime)));
            }
            
            return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageAble);
		
	}

	@Override
	public List<UpGradeDetail> queryUpGradeDetailByUseridAndCreateTime(Long userId, String createTime) {
		return upGradeDetailRepository.findByUserIdAndCreateTime(userId,createTime);
	}

	private static boolean isNotNull(String str) {
		return !(str == null || "".equals(str) || "null".equalsIgnoreCase(str));
	}

    @Override
    public int queryUpGradeDetailByUseridsAndCreateTime(Long[] userids, String todayTime) {
        return upGradeDetailRepository.findByUserIdsAndCreateTime(userids,todayTime);
    }

    @Override
    public int queryUpGradeDetailByUserids(Long[] userids) {
        return upGradeDetailRepository.findByUserIds(userids);
    }
	
}
