package com.jh.user.business;

import com.jh.user.pojo.UserCoinHistoryNew;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface UserCoinHistoryNewBusiness {
    List<UserCoinHistoryNew> findByUserIdList(Long[] longs, Pageable pageable);
    List<UserCoinHistoryNew> findByUserId(long id,Pageable pageable);

    List<UserCoinHistoryNew> findByOrderCode(String orderCode, Pageable pageable);

    List<UserCoinHistoryNew> findByUserIdListAndTime(Long[] longs, Date strTime, Date endTime, Pageable pageable);

    List<UserCoinHistoryNew> findByUserIdAndTime(long id, Date strdate, Date enddate, Pageable pageable);

    List<UserCoinHistoryNew> findByOrderCodeAndTime(String orderCode, Date strdate, Date enddate, Pageable pageable);
}
