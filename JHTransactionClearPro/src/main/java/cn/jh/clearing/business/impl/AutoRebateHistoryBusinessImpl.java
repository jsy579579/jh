package cn.jh.clearing.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.jh.clearing.business.AutoRebateHistoryBusiness;
import cn.jh.clearing.pojo.AutoRebateHistory;
import cn.jh.clearing.repository.AutoRebateHistoryRepository;
@Service
public class AutoRebateHistoryBusinessImpl implements AutoRebateHistoryBusiness {

	@Autowired
	private AutoRebateHistoryRepository autoRebateHistoryRespository;
	
	@Transactional
	@Override
	public AutoRebateHistory createNewHistory(AutoRebateHistory model) {
		return autoRebateHistoryRespository.save(model);
	}

}
