package com.jh.user.business;

import com.jh.user.pojo.UserManageHistory;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface UserManageHistoryBusiness {


    UserManageHistory save(UserManageHistory userManageHistory);


    List<UserManageHistory> findBybrandId(Long brandId, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndPrePhone(Long brandId, String preUserPhone, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndFirPhone(Long brandId, String firstUserPhone, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndFirPhoneAndPrePhone(Long brandId, String firstUserPhone, String preUserPhone, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndTime(Long brandId, Date strdate, Date enddate, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndPrePhoneAndTime(Long brandId, Date strdate, Date enddate, String prePhone, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndFirPhoneAndTime(Long brandId, Date strdate, Date enddate, String firPhone, Pageable pageable);

    List<UserManageHistory> findBybrandIdAndFirPhoneAndPrePhoneAndTime(Long brandId, Date strdate, Date enddate, String firPhone, String prePhone, Pageable pageable);
}
