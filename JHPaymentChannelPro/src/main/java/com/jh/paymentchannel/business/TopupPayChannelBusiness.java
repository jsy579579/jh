package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.BankInfoCode;
import com.jh.paymentchannel.pojo.BankNumCode;
import com.jh.paymentchannel.pojo.CJBindCard;
import com.jh.paymentchannel.pojo.CJHKBindCard;
import com.jh.paymentchannel.pojo.CJHKRegister;
import com.jh.paymentchannel.pojo.CJQuickBindCard;
import com.jh.paymentchannel.pojo.CJRegister;
import com.jh.paymentchannel.pojo.CardEvaluation;
import com.jh.paymentchannel.pojo.CardEvaluationHistory;
import com.jh.paymentchannel.pojo.ChannelDetail;
import com.jh.paymentchannel.pojo.HLJCBindCard;
import com.jh.paymentchannel.pojo.HLJCRegister;
import com.jh.paymentchannel.pojo.JPBindCard;
import com.jh.paymentchannel.pojo.JPRegister;
import com.jh.paymentchannel.pojo.KYRegister;
import com.jh.paymentchannel.pojo.LDRegister;
import com.jh.paymentchannel.pojo.LFQuickRegister;
import com.jh.paymentchannel.pojo.RSRegister;
import com.jh.paymentchannel.pojo.RepaymentInfoMation;
import com.jh.paymentchannel.pojo.RepaymentSupportBank;
import com.jh.paymentchannel.pojo.TopupPayChannelRoute;
import com.jh.paymentchannel.pojo.WLBBindCard;
import com.jh.paymentchannel.pojo.WLBRegister;
import com.jh.paymentchannel.pojo.WMYKBindCard;
import com.jh.paymentchannel.pojo.WMYKChooseCity;
import com.jh.paymentchannel.pojo.WMYKCity;
import com.jh.paymentchannel.pojo.WMYKNewBindCard;
import com.jh.paymentchannel.pojo.WMYKNewChooseCity;
import com.jh.paymentchannel.pojo.WMYKNewCity;
import com.jh.paymentchannel.pojo.WMYKNewCityMerchant;
import com.jh.paymentchannel.pojo.WMYKNewProvince;
import com.jh.paymentchannel.pojo.WMYKProvince;
import com.jh.paymentchannel.pojo.WMYKXWKCityMerchant;
import com.jh.paymentchannel.pojo.XJQuickRegister;
import com.jh.paymentchannel.pojo.YBHKBindCard;
import com.jh.paymentchannel.pojo.YBHKRegister;
import com.jh.paymentchannel.pojo.YBQuickRegister;
import com.jh.paymentchannel.pojo.YHQuickRegister;
import com.jh.paymentchannel.pojo.YLDZBindCard;

public interface TopupPayChannelBusiness {

	public TopupPayChannelRoute getTopupChannelByBrandcode(String brandcode, String channelType, String channelTag);

	public TopupPayChannelRoute saveTopupPayChannelRoute(TopupPayChannelRoute topupPayChannelRoute);

	public List<TopupPayChannelRoute> getTopupChannelByBrandId(String brandcode, String channelType, String channelTag);

	public List<TopupPayChannelRoute> getPayChannelByBrandcode(String brandcode);

	public ChannelDetail getChannelDetailByTag(String channelTag);
	
	public List<ChannelDetail> getChannelDetailByNO(String channelNo);

	public List<ChannelDetail> getChannelDetail();

	public TopupPayChannelRoute configTopupPayChannelRoute(TopupPayChannelRoute topupPayChannelRoute);

	public BankInfoCode getBankInfoCodeByBankName(String bankName);
	
	public List<BankInfoCode> getBankInfoCodeByThree(String banknum, String bankprivince, String bankcity);
	
	public BankNumCode getBankNumCodeByBankName(String bankName);
	
	public String getXJAreaCode(String areaName, String areaLevel);
	
	public XJQuickRegister createXJQuickRegister(XJQuickRegister xjQuickRegister);
	
	public XJQuickRegister getXJQuickRegister(String idCard);
	
	public LDRegister createLDRegister(LDRegister ldRegister);
	
	public LDRegister getLDRegisterByIdCard(String idCard);
	
	public WLBRegister createWLBRegister(WLBRegister wlbRegister);
	
	public WLBRegister getWLBRegisterByIdCard(String idCard);
	
	public WLBBindCard createWLBBindCard(WLBBindCard wlbBindCard);
	
	public WLBBindCard getWLBBindCardByBankCard(String bankCard);
	
	public LFQuickRegister createLFQuickRegister(LFQuickRegister lfQuickRegister);
	
	public LFQuickRegister getLFQuickRegisterByIdCard(String idCard);
	
	public YHQuickRegister createYHQuickRegister(YHQuickRegister yhQuickRegister);
	
	public YHQuickRegister getYHQuickRegisterByIdCard(String idCard);
	
	public YBQuickRegister createYBQuickRegister(YBQuickRegister ybQuickRegister);
	
	public YBQuickRegister getYBQuickRegisterByIdCard(String idCard);
	
	public YBQuickRegister getYBQuickRegisterByPhone(String phone);
	
	public String getLFQuickRegisterByPhone(String phone);
	
	public String getLDRegisterByPhone(String phone);
	
	public String getWLBRegisterByPhone(String phone);
	
	public String getXJRegisterByPhone(String phone);
	
	public String getYHRegisterByPhone(String phone);
	
	public HLJCRegister createHLJCRegister(HLJCRegister hljcRegister);
	
	public HLJCRegister getHLJCRegisterByBankCard(String bankCard);
	
	public HLJCBindCard createHLJCBindCard(HLJCBindCard hljcBindCard);
	
	public HLJCBindCard getHLJCBindCardByBankCard(String bankCard);
	
	public HLJCBindCard getHLJCBindCardByOrderCode(String orderCode);
	
/*	public YSBRegister createYSBRegister(YSBRegister ysbRegister);
	
	public YSBRegister getYSBRegisterByBankCard(String bankCard);*/
	
	public CJHKRegister createCJHKRegister(CJHKRegister cjhkRegister);
	
	public CJHKRegister	getCJHKRegisterByBankCard(String bankCard);
	
	public CJHKRegister getCJHKRegisterByIdCard(String idCard);
	
	public CJHKRegister getCJHKRegisterByIdCardAndVersion(String idCard, String version);
	
	public CJHKRegister getCJHKRegisterByIdCardAndVersions(String idCard, String versions);
	
	public CJHKBindCard createCJHKBindCard(CJHKBindCard cjhkBindCard);
	
	public CJHKBindCard getCJHKBindCardByBankCard(String bankCard);
	
	public WMYKBindCard createWMYKBindCard(WMYKBindCard wmykBindCard);
	
	public WMYKBindCard getWMYKBindCardByBankCard(String bankCard);
	
	public List<String> getWMYKCityMerchantNameByCityCode(String cityCode);
	
	public List<String> getWMYKCityMerchantCodeByCityCode(String cityCode);
	
	public WMYKChooseCity createWMYKChooseCity(WMYKChooseCity wmykChooseCity);
	
	public WMYKChooseCity getWMYKChooseCityByBankCard(String bankCard);
	
	public List<WMYKProvince> getWMYKProvince();
	
	public List<WMYKCity> getWMYKCityByProvinceCode(String provinceCode); 
	
	public List<WMYKCity> getWMYKCity(); 
	
/*	public HLJCQuickRegister createHLJCQuickRegister(HLJCQuickRegister hljcQuickRegister);
	
	public HLJCQuickRegister getHLJCQuickRegisterByIdCard(String idCard);
	
	public HLJCQuickBindCard createHLJCQuickBindCard(HLJCQuickBindCard hljcQuickBindCard);
	
	public HLJCQuickBindCard getHLJCQuickBindCardByBankCard(String bankCard);*/
	
	public CJRegister createCJRegister(CJRegister cjRegister);
	
	public CJRegister getCJRegisterByIdCard(String idCard);
	
	public CJBindCard createCJBindCard(CJBindCard cjBindCard);
	
	public CJBindCard getCJBindCardByBankCard(String bankCard);
	
	public CardEvaluation createCardEvaluation(CardEvaluation cardEvaluation);
	
	public CardEvaluation getCardEvaluationByBankCard(String bankCard);
	
	public CardEvaluationHistory createCardEvaluationHistory(CardEvaluationHistory cardEvaluationHistory);
	
	public List<CardEvaluationHistory> getCardEvaluationHistoryByUserId(String userId);
	
	/*public CJQuickRegister createCJQuickRegister(CJQuickRegister cjQuickRegister);
	
	public CJQuickRegister getCJQuickRegisterByIdCard(String idCard);*/
	
	public CJQuickBindCard createCJQuickBindCard(CJQuickBindCard cjQuickBindCard);
	
	public CJQuickBindCard getCJQuickBindCardByBankCard(String bankCard);
	
	public CJQuickBindCard getCJQuickBindCardByOrderCode(String orderCode);
	
	public List<RepaymentSupportBank> getRepaymentSupportBankByVersion(String version);
	
	public JPRegister createJPRegister(JPRegister jpRegister);
	
	public JPRegister getJPRegisterByIdCard(String idCard);
	
	public JPBindCard createJPBindCard(JPBindCard jpBindCard);
	
	public JPBindCard getJPBindCardByBankCard(String bankCard);
	
	public YLDZBindCard createYLDZBindCard(YLDZBindCard yldzBindCard);
	
	public YLDZBindCard getYLDZBindCardByBankCard(String bankCard);	

	public RSRegister createRSRegister(RSRegister rsRegister);
	
	public RSRegister getRSRegisterByIdCard(String idCard);
	
	public YBHKRegister createYBHKRegister(YBHKRegister ybhkRegister);
	
	public YBHKRegister getYBHKRegisterByIdCard(String idCard);
	
	public YBHKBindCard createYBHKBindCard(YBHKBindCard ybhkBindCard);
	
	public YBHKBindCard getYBHKBindCardByBankCard(String bankCard);
	
	public KYRegister createKYRegister(KYRegister kyRegister);
	
	public KYRegister getKYRegisterByIdCard(String idCard);
	
	public List<WMYKNewCityMerchant> getWMYKNewCityMerchantByCityCode(String cityCode);
	
	public List<String> getWMYKNewCityMerchantCodeByCityCode(String cityCode);
	
	public List<WMYKNewCity> getWMYKNewCityByProvinceCode(String provinceCode);
	
	public List<WMYKNewCity> getWMYKNewCity(); 
	
	public WMYKNewChooseCity createWMYKNewChooseCity(WMYKNewChooseCity wmykNewChooseCity);
	
	public WMYKNewChooseCity getWMYKNewChooseCityByBankCard(String bankCard);
	
	public List<WMYKNewProvince> getWMYKNewProvince();
	
	public WMYKNewBindCard createWMYKNewBindCard(WMYKNewBindCard wmykNewBindCard);
	
	public WMYKNewBindCard getWMYKNewBindCardByBankCard(String bankCard);
	
	public List<WMYKXWKCityMerchant> getWMYKXWKCityMerchantByCityCode(String cityCode);
	
	public List<String> getWMYKXWKCityMerchantCodeByCityCode(String cityCode);
	
	public WMYKXWKCityMerchant getWMYKXWKCityMerchantByMerchantName(String merchantName);
	
	public WMYKXWKCityMerchant getWMYKXWKCityMerchantByMerchantCode(String merchantCode);
	
	public List<RepaymentInfoMation> getRepaymentInfoMationAll();
	
	
}
