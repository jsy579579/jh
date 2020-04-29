package com.jh.paymentgateway.business.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.NPpageRequest;
import com.jh.paymentgateway.pojo.NPRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

/**
 * 
 * <p>Title: XSTopupPage</p>  
 * <p>Description: 新生支付通道入口类</p>  
 * @author Robin(WX/QQ:354476429)
 * @date 2018年10月16日
 */
@Service
public class NPTopupPag extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(NPTopupPag.class);

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	
	@Autowired
	private NPpageRequest nppageRequest;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String > wechatreturn  = new HashMap<String, String >();
		String bankCard=bean.getBankCard() ;
		String idCard= bean.getIdCard();
		String userName= bean.getUserName();
		String rate=bean.getRate();
		String extraFee= new BigDecimal(bean.getExtraFee()).setScale(2, BigDecimal.ROUND_DOWN).toString();
		NPRegister  np =topupPayChannelBusiness.getNPRegisterbyIdcard(idCard);
		if(np==null) {
			map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/registerpage?orderCode="
					+bean.getOrderCode() );
		}else if(!np.getStatus().equals("1")) {
			wechatreturn=(Map<String, String>) nppageRequest.queryMer(idCard, np.getPhone(), np.getBankCard(), userName);
			String status=wechatreturn.get("status");
			String msg=wechatreturn.get("msg");
			if(status.equals("SUCCESS")) {
				String  merchantid=wechatreturn.get("merchantid");
				String  signKey=wechatreturn.get("key");
				np.setMerId(merchantid);
				np.setSignKey(signKey);
				np.setStatus("1");
				if(!np.getRateDo().equals(rate)||!np.getFeesDo().equals(extraFee)){
					wechatreturn=(Map<String, String>) nppageRequest.updateFees(np.getMerId(), rate, extraFee, np.getSignKey());
					status=wechatreturn.get("status");
					String result_code=wechatreturn.get("result_code");
					if(result_code.equals("SUCCESS")&&status.equals("0")) {
						np.setFeesDo(extraFee);
						np.setRateDo(rate);
						map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
								+bean.getOrderCode() );
					}else {
						np.setRemark(result_code);
						map=ResultWrap.init(CommonConstants.FALIED, "修改费率有误");
					}
				}else if(!np.getBankCard().equals(bankCard)) {
					map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/registerpage?orderCode="
							+bean.getOrderCode() );
				}else {
					map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
							+bean.getOrderCode() );
				} 
			}else {
				np.setRemark(msg);
				map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
						+bean.getOrderCode() );
			}
		}else {
			if(!np.getRateDo().equals(rate)||!np.getFeesDo().equals(extraFee)){
				wechatreturn=(Map<String, String>) nppageRequest.updateFees(np.getMerId(), rate, extraFee, np.getSignKey());
				String status=wechatreturn.get("status");
				String result_code=wechatreturn.get("result_code");
				if(result_code.equals("SUCCESS")&&status.equals("0")) {
					np.setFeesDo(extraFee);
					np.setRateDo(rate);
					map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
							+bean.getOrderCode() );
				}else {
					np.setRemark(result_code);
					map=ResultWrap.init(CommonConstants.FALIED, "修改费率有误");
				}
			}else if(!np.getBankCard().equals(bankCard)) {
				map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/registerpage?orderCode="
						+bean.getOrderCode() );
			} else {
				map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/np/paypage?orderCode="
						+bean.getOrderCode() );
			}
		}
		if(np!=null)
		np=topupPayChannelBusiness.createNPRegister(np);
		LOG.info(bean.getOrderCode() + "=====请求支付返回=====" + map);
		return map;
	}

}
