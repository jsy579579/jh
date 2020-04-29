package com.jh.paymentchannel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jh.paymentchannel.util.PaymentChannelConstants;

@Component
public class PaymentChannelFactory {

	@Autowired
	private UNSPayRequest UNSpayrequest;

	@Autowired
	private UMPayRequest umpayRequest;

	@Autowired
	private UMPayEMFRequest umPayEMFRequest;

	@Autowired
	private SwiftTopupRequest swifttopuprequest;

	@Autowired
	private SwiftGongZhongHaoRequest gongzhonghaoRequest;

	@Autowired
	private UMPTopupRequest umpTopupRequest;

	@Autowired
	private YBTopupService ybpay;

	@Autowired
	private YB2TopupService ybpay2;

	@Autowired
	private YHTopupRequest yhTopupRequest;

	@Autowired
	private XJTopupRequest xjTopupRequest;

	@Autowired
	private LDTopupRequest ldTopupRequest;

	@Autowired
	private WLBTopupRequest wlbTopupRequest;

	@Autowired
	private LFTopupRequest lfTopupRequest;

	@Autowired
	private HLJCTopupPage hljcpageRequest;

	@Autowired
	private YBnewTopupService ybnewTopupService;

	/*
	 * @Autowired private HLJCQuickTopupPage hljcQuickTopupPage;
	 */

	@Autowired
	private CJTopupPage cjTopupPage;

	@Autowired
	private YLXTopupRequest ylxTopupRequest;

	@Autowired
	private CJHKTopupPage cjhkTopupPage;

	@Autowired
	private YLDZTopupPage yldzTopupPage;

	@Autowired
	private WMYKTopupPage wmykTopupPage;

	@Autowired
	private CJQuickTopupPage cjQuickTopupPage;

	@Autowired
	private HLBTopupPage hlbTopupPage;

	@Autowired
	private JPTopupPage jpTopupPage;

	@Autowired
	private YBHKTopupPage ybhkTopupPage;

	@Autowired
	private ALIPAYTopupPage aliPAYTopupPage;

	@Autowired
	private WXPayTopupPage wxPayTopupPage;

	@Autowired
	private KYTopupPage kyTopupPage;

	@Autowired
	private WMYKNewTopupPage wmykNewTopupPage;

	@Autowired
	private ALIPAYAPPTopupPage alipayappTopupPage;

	@Autowired
	private RechargeToAccountTopupRequest rechargeToAccountTopupRequest;

	public TopupRequest getTopupChannelRequest(String channelcode) {

		TopupRequest paymentRequest = null;

		if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YB)) {
			paymentRequest = ybpay;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YB2)) {
			paymentRequest = ybpay2;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_SWIFT_QQ)
				|| channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_SWIFT_WEIXIN)
				|| channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_SWIFT_ALIPAY)
				|| channelcode.equalsIgnoreCase("SWIFT_ALIPAY_GONGZHONGHAO")
				|| channelcode.equalsIgnoreCase("SWIFT_QQ_GONGZHONGHAO")) {
			paymentRequest = swifttopuprequest;
		} else if (channelcode.equalsIgnoreCase("SWIFT_WEIXIN_GONGZHONGHAO")) {
			paymentRequest = gongzhonghaoRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_UMP)) {
			paymentRequest = umpTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YH)) {
			paymentRequest = yhTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_XJ)) {
			paymentRequest = xjTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_LD)) {
			paymentRequest = ldTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_WLB)) {
			paymentRequest = wlbTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_LF)) {
			paymentRequest = lfTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YB_QUICK)) {
			paymentRequest = ybnewTopupService;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_HLJC_QUICK)) {
			paymentRequest = hljcpageRequest;
		}
		/*
		 * else if(channelcode.equalsIgnoreCase(PaymentChannelConstants.
		 * CHANNEL_HLJC_QUICK1)) { paymentRequest = hljcQuickTopupPage; }
		 */
		else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YLX_QUICK)) {
			paymentRequest = ylxTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_CJ)) {
			paymentRequest = cjTopupPage;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_CJHK)) {
			paymentRequest = cjhkTopupPage;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YLDZ_QUICK)) {
			paymentRequest = yldzTopupPage;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_WMYK_QUICK)) {
			paymentRequest = wmykTopupPage;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_CJ_QUICK)) {
			paymentRequest = cjQuickTopupPage;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_RECHARGE)) {
			paymentRequest = rechargeToAccountTopupRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_JP)) {
			paymentRequest = jpTopupPage;

		}else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YBHK)) {
			paymentRequest = ybhkTopupPage;

		}else if (
				channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_HLB) ||
						channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_YL_HLB)
		) {
			paymentRequest = hlbTopupPage;

		}
		/*
		 * else
		 * if(channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_SPGZH
		 * )) { paymentRequest = spGZHTopupPage; }
		 */
		else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_SPALI)) {
			paymentRequest = aliPAYTopupPage;
		}
		else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_SPWX)) {
			paymentRequest = wxPayTopupPage;
		}

		else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_KY)
				|| channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_KY1)) {
			paymentRequest = kyTopupPage;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_WMYKNEW)) {
			paymentRequest = wmykNewTopupPage;
		}  else if(channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_ALIPAY_APP)){
			paymentRequest = alipayappTopupPage;
		}
		return paymentRequest;
	}

	public PayRequest getPayChannelRequest(String channelcode) {
		PayRequest paymentRequest = null;

		if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_UMP)) {
			paymentRequest = umpayRequest;
		} else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_UMP_PAY)) {
			paymentRequest = umPayEMFRequest;
		} /*else if (channelcode.equalsIgnoreCase(PaymentChannelConstants.CHANNEL_UNS_PAY)) {
			paymentRequest = UNSpayrequest;

		}*/
		return paymentRequest;
	}

}
