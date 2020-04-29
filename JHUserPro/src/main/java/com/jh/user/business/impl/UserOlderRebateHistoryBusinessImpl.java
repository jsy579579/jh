package com.jh.user.business.impl;

import com.jh.user.business.UserOlderRebateHistoryBusiness;
import com.jh.user.pojo.UserOlderRebateHistory;
import com.jh.user.repository.UserOlderRebateHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserOlderRebateHistoryBusinessImpl implements UserOlderRebateHistoryBusiness {

    @Autowired
    UserOlderRebateHistoryRepository userOlderRebateHistoryRepository;

    @Override
    public void saveUserOlderRebateHistory(UserOlderRebateHistory userOlderRebateHistory) {
        userOlderRebateHistoryRepository.save(userOlderRebateHistory);
    }
}
