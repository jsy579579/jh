package com.jh.user.business.impl;

import com.jh.user.business.UserManageHistoryBusiness;
import com.jh.user.pojo.UserManageHistory;
import com.jh.user.repository.UserManageHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

@Service
public class UserManageHistoryBusinessImpl implements UserManageHistoryBusiness {


  @Autowired
    UserManageHistoryRepository userManageHistoryRepository;

    @Autowired
    private EntityManager em;

    @Transactional
    @Override
    public UserManageHistory save(UserManageHistory userManageHistory) {
        UserManageHistory result = userManageHistoryRepository.save(userManageHistory);
        em.flush();
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandId(Long brandId, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandId(brandId,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndPrePhone(Long brandId, String preUserPhone, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndPrePhone(brandId,preUserPhone,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndFirPhone(Long brandId, String firstUserPhone, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndFirPhone(brandId,firstUserPhone,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndFirPhoneAndPrePhone(Long brandId, String firstUserPhone, String preUserPhone, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndFirPhoneAndPrePhone(brandId,firstUserPhone,preUserPhone,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndTime(Long brandId, Date strdate, Date enddate, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndTime(brandId,strdate,enddate,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndPrePhoneAndTime(Long brandId, Date strdate, Date enddate, String prePhone, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndPrePhoneAndTime(brandId,strdate,enddate,prePhone,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndFirPhoneAndTime(Long brandId, Date strdate, Date enddate, String firPhone, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndFirPhoneAndTime(brandId,strdate,enddate,firPhone,pageable);
        return result;
    }

    @Override
    public List<UserManageHistory> findBybrandIdAndFirPhoneAndPrePhoneAndTime(Long brandId, Date strdate, Date enddate, String firPhone, String prePhone, Pageable pageable) {
        List<UserManageHistory> result=userManageHistoryRepository.findBybrandIdAndFirPhoneAndPrePhoneAndTime(brandId,strdate,enddate,firPhone,prePhone,pageable);
        return result;
    }
}
