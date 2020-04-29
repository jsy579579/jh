package com.jh.paymentgateway.business.impl;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

import org.springframework.stereotype.Service;

import com.jh.paymentgateway.business.TopupRequestBusiness;

import java.util.Map;
@Service
public class TopupRequestBusinessImpl implements TopupRequestBusiness{

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		return ResultWrap.init(CommonConstants.SUCCESS, "请求成功");
	}


}
