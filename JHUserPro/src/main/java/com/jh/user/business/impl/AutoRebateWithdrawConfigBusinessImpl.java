package com.jh.user.business.impl;

import com.jh.user.business.AutoRebateWithdrawConfigBusiness;
import com.jh.user.pojo.AutoRebateWithdrawConfig;
import com.jh.user.repository.AutoRebateWithdrawConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoRebateWithdrawConfigBusinessImpl implements AutoRebateWithdrawConfigBusiness {

    @Autowired
    private AutoRebateWithdrawConfigRepository autoRebateWithdrawConfigRepository;
    @Override
    public List<AutoRebateWithdrawConfig> queryConfig(Integer onOff) {
        return autoRebateWithdrawConfigRepository.queryConfig(onOff);
    }
}
