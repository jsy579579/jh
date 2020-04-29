package com.jh.user.business;

import com.jh.user.pojo.AutoRebateWithdrawConfig;

import java.util.List;

public interface AutoRebateWithdrawConfigBusiness {
    List<AutoRebateWithdrawConfig> queryConfig(Integer onOff);
}
