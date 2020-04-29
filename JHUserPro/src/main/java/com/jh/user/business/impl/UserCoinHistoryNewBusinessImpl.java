package com.jh.user.business.impl;

import com.jh.user.business.UserCoinHistoryNewBusiness;
import com.jh.user.pojo.UserCoinHistoryNew;
import com.jh.user.repository.UserCoinNewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserCoinHistoryNewBusinessImpl implements UserCoinHistoryNewBusiness {

    @Autowired
    private UserCoinNewRepository userCoinNewRepository;

    @Override
    public List<UserCoinHistoryNew> findByUserIdList(Long[] longs, Pageable pageable) {
        return userCoinNewRepository.findByUserIdList(longs,pageable);
    }

    @Override
    public List<UserCoinHistoryNew> findByUserId(long id,Pageable pageable) {
        return userCoinNewRepository.findByUserId(id,pageable);
    }

    @Override
    public List<UserCoinHistoryNew> findByOrderCode(String orderCode, Pageable pageable) {
        return userCoinNewRepository.findByOrderCode(orderCode,pageable);
    }

    @Override
    public List<UserCoinHistoryNew> findByUserIdListAndTime(Long[] longs, Date strdate, Date enddate, Pageable pageable) {
        return userCoinNewRepository.findByUserIdListAndTime(longs,strdate,enddate,pageable);
    }

    @Override
    public List<UserCoinHistoryNew> findByUserIdAndTime(long id, Date strdate, Date enddate, Pageable pageable) {
        return userCoinNewRepository.findByUserIdAndTime(id,strdate,enddate,pageable);
    }

    @Override
    public List<UserCoinHistoryNew> findByOrderCodeAndTime(String orderCode, Date strdate, Date enddate, Pageable pageable) {
        return userCoinNewRepository.findByOrderCodeAndTime(orderCode,strdate,enddate,pageable);
    }
}
