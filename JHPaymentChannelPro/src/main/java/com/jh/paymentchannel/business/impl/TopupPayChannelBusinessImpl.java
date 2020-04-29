package com.jh.paymentchannel.business.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.TopupPayChannelBusiness;
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
import com.jh.paymentchannel.repository.BankInfoCodeRepository;
import com.jh.paymentchannel.repository.BankNumCodeRepository;
import com.jh.paymentchannel.repository.CJBindCardRepository;
import com.jh.paymentchannel.repository.CJHKBindCardRepository;
import com.jh.paymentchannel.repository.CJHKRegisterRepository;
import com.jh.paymentchannel.repository.CJQuickBindCardRepository;
import com.jh.paymentchannel.repository.CJRegisterRepository;
import com.jh.paymentchannel.repository.CardEvaluationHistoryRepository;
import com.jh.paymentchannel.repository.CardEvaluationRepository;
import com.jh.paymentchannel.repository.ChannelDetailRepository;
import com.jh.paymentchannel.repository.HLJCBindCardRepository;
import com.jh.paymentchannel.repository.HLJCRegisterRepository;
import com.jh.paymentchannel.repository.JPBindCardRepository;
import com.jh.paymentchannel.repository.JPRegisterRepository;
import com.jh.paymentchannel.repository.KYRegisterRepository;
import com.jh.paymentchannel.repository.LDRegisterRepository;
import com.jh.paymentchannel.repository.LFQuickRegisterRepository;
import com.jh.paymentchannel.repository.RSRegisterRepository;
import com.jh.paymentchannel.repository.RepaymentInfoMationRepository;
import com.jh.paymentchannel.repository.RepaymentSupportBankRepository;
import com.jh.paymentchannel.repository.TopupPayChannelRepository;
import com.jh.paymentchannel.repository.WLBBindCardRepository;
import com.jh.paymentchannel.repository.WLBRegisterRepository;
import com.jh.paymentchannel.repository.WMYKBindCardRepository;
import com.jh.paymentchannel.repository.WMYKChooseCityRepository;
import com.jh.paymentchannel.repository.WMYKCityMerchantRepository;
import com.jh.paymentchannel.repository.WMYKCityRepository;
import com.jh.paymentchannel.repository.WMYKNewBindCardRepository;
import com.jh.paymentchannel.repository.WMYKNewChooseCityRepository;
import com.jh.paymentchannel.repository.WMYKNewCityMerchantRepository;
import com.jh.paymentchannel.repository.WMYKNewCityRepository;
import com.jh.paymentchannel.repository.WMYKNewProvinceRepository;
import com.jh.paymentchannel.repository.WMYKProvinceRepository;
import com.jh.paymentchannel.repository.WMYKXWKCityMerchantRepository;
import com.jh.paymentchannel.repository.XJAreaCodeRepository;
import com.jh.paymentchannel.repository.XJQuickRegisterRepository;
import com.jh.paymentchannel.repository.YBHKBindCardRepository;
import com.jh.paymentchannel.repository.YBHKRegisterRepository;
import com.jh.paymentchannel.repository.YBQuickRegisterRepository;
import com.jh.paymentchannel.repository.YHRegisterRepository;
import com.jh.paymentchannel.repository.YLDZBindCardRepository;

@Service
public class TopupPayChannelBusinessImpl implements TopupPayChannelBusiness {

	@Autowired
	private TopupPayChannelRepository topupchannelrepository;

	@Autowired
	private ChannelDetailRepository channelDetailRepository;

	@Autowired
	private BankNumCodeRepository bankNumCodeRepository;

	@Autowired
	private BankInfoCodeRepository bankInfoCodeRepository;

	@Autowired
	private XJAreaCodeRepository xjAreaCodeRepository;

	@Autowired
	private XJQuickRegisterRepository xjQuickRegisterRepository;

	@Autowired
	private LDRegisterRepository ldRegisterRepository;

	@Autowired
	private WLBRegisterRepository wlbRegisterRepository;

	@Autowired
	private WLBBindCardRepository wlbBindCardRepository;

	@Autowired
	private LFQuickRegisterRepository lfQuickRegisterRepository;

	@Autowired
	private YHRegisterRepository yhRegisterRepository;

	@Autowired
	private YBQuickRegisterRepository ybQuickRegisterRepository;

	@Autowired
	private HLJCRegisterRepository hljcRegisterRepository;

	@Autowired
	private HLJCBindCardRepository hljcBindCardRepository;

	@Autowired
	private WMYKBindCardRepository wmykBindCardRepository;

	@Autowired
	private CJHKRegisterRepository cjhkRegisterRepository;

	@Autowired
	private CJHKBindCardRepository cjhkBindCardRepository;

	@Autowired
	private CJRegisterRepository cjRegisterRepository;

	@Autowired
	private CardEvaluationRepository cardEvaluationRepository;

	@Autowired
	private CardEvaluationHistoryRepository cardEvaluationHistoryRepository;

	@Autowired
	private CJBindCardRepository cjBindCardRepository;

	@Autowired
	private WMYKCityMerchantRepository wmykCityMerchantRepository;
	
	@Autowired
	private WMYKChooseCityRepository wmykChooseCityRepository;
	
	@Autowired
	private WMYKCityRepository wmykCityRepository;
	
	@Autowired
	private WMYKProvinceRepository wmykProvinceRepository;
	
	/*@Autowired
	private CJQuickRegisterRepository cjQuickRegisterRepository;*/
	
	@Autowired
	private CJQuickBindCardRepository cjQuickBindCardRepository;
	
	@Autowired
	private RepaymentSupportBankRepository repaymentSupportBankRepository;
	
	@Autowired
	private JPRegisterRepository jpRegisterRepository;
	
	@Autowired
	private JPBindCardRepository jpBindCardRepository;
	
	@Autowired
	private YLDZBindCardRepository yldzBindCardRepository;
	
	@Autowired
	private YBHKRegisterRepository ybhkRegisterRepository;
	
	@Autowired
	private YBHKBindCardRepository ybhkBindCardRepository;
	
	@Autowired
	private KYRegisterRepository kyRegisterRepository;
	
	@Autowired
	private WMYKNewCityMerchantRepository wmykNewCityMerchantRepository;
	
	@Autowired
	private WMYKNewCityRepository wmykNewCityRepository;
	
	@Autowired
	private WMYKNewChooseCityRepository wmykNewChooseCityRepository;
	
	@Autowired
	private WMYKNewProvinceRepository wmykNewProvinceRepository;
	
	@Autowired
	private WMYKNewBindCardRepository wmykNewBindCardRepository;
	
	@Autowired
	private WMYKXWKCityMerchantRepository wmykXWKCityMerchantRepository;
	
	@Autowired
	private RSRegisterRepository rsRegisterRepository;
	
	@Autowired
	private RepaymentInfoMationRepository repaymentInfoMationRepository;
	
	@Autowired
	private EntityManager em;

	@Override
	public TopupPayChannelRoute getTopupChannelByBrandcode(String brandcode, String channelType, String channelTag) {
		return topupchannelrepository.getTopupChannelRoute(brandcode, channelType, channelTag);
	}

	@Transactional
	@Override
	public TopupPayChannelRoute saveTopupPayChannelRoute(TopupPayChannelRoute topupPayChannelRoute) {
		TopupPayChannelRoute result = topupchannelrepository.save(topupPayChannelRoute);
		em.flush();
		return result;
	}

	@Override
	public List<TopupPayChannelRoute> getTopupChannelByBrandId(String brandcode, String channelType,
			String channelTag) {
		List<TopupPayChannelRoute> topupPayChannelRouteList = new ArrayList<TopupPayChannelRoute>();
		if (brandcode != null && !brandcode.equals("")) {
			if (channelType != null && !channelType.equals("")) {
				if (channelTag != null && !channelTag.equals("")) {
					TopupPayChannelRoute topupPayChannelRoute = new TopupPayChannelRoute();
					topupPayChannelRoute = topupchannelrepository.getTopupChannelRoute(brandcode, channelType,
							channelTag);
					if (topupPayChannelRoute != null) {
						topupPayChannelRouteList.add(topupPayChannelRoute);
					}
				} else {
					topupPayChannelRouteList = topupchannelrepository.getPayChannelRoute(brandcode, channelType);
				}

			} else if (channelTag != null && !channelTag.equals("")) {
				topupPayChannelRouteList = topupchannelrepository.getPayChannelRouteChannelTag(brandcode, channelTag);

			} else {
				topupPayChannelRouteList = topupchannelrepository.getPayChannelRouteBybrandcode(brandcode);
			}

		} else if (channelType != null && !channelType.equals("")) {

			if (channelTag != null && !channelTag.equals("")) {

				topupPayChannelRouteList = topupchannelrepository.getPayChannelRouteBychannelcode(channelTag,
						channelType);
			} else {

				topupPayChannelRouteList = topupchannelrepository.getPayChannelRoute(channelType);
			}

		} else if (channelTag != null && !channelTag.equals("")) {

			topupPayChannelRouteList = topupchannelrepository.getPayChannelRouteBychannelTag(channelTag);
		} else {

			topupPayChannelRouteList = topupchannelrepository.getPayChannelRoute();
		}

		return topupPayChannelRouteList;

	}

	@Override
	public List<TopupPayChannelRoute> getPayChannelByBrandcode(String brandcode) {
		return topupchannelrepository.getPayChannelRouteBybrandcode(brandcode);
	}

	/***
	 * 通过标识获取后台通道数据
	 * 
	 **/
	@Override
	public ChannelDetail getChannelDetailByTag(String channelTag) {
		return channelDetailRepository.getChannelDetailByTag(channelTag);
	}

	/***
	 * 通过标识获取后台通道数据
	 * 
	 **/
	@Override
	public List<ChannelDetail> getChannelDetail() {
		return channelDetailRepository.getChannelDetail();
	}

	// 一键配置通道路由
	@Transactional
	@Override
	public TopupPayChannelRoute configTopupPayChannelRoute(TopupPayChannelRoute topupPayChannelRoute) {
		TopupPayChannelRoute result = (TopupPayChannelRoute) topupchannelrepository.save(topupPayChannelRoute);
		em.flush();
		em.clear();
		return result;
	}

	/*
	 * //鑫佳宇通道
	 * 
	 * @Transactional
	 * 
	 * @Override public XJYApiRegister createXJYApiRegister(XJYApiRegister
	 * xjyApiRegister) { XJYApiRegister result =
	 * xjyApiRepository.save(xjyApiRegister); em.flush(); return result; }
	 * 
	 * @Override public XJYApiRegister getXJYApiRegister(String bankCard) { return
	 * xjyApiRepository.getXJYApiRegisterByBankCard(bankCard); }
	 */

	@Override
	public BankInfoCode getBankInfoCodeByBankName(String bankName) {
		return bankInfoCodeRepository.getBankInfoCodeByBankName(bankName);
	}

	@Override
	public BankNumCode getBankNumCodeByBankName(String bankName) {
		return bankNumCodeRepository.getBankNumCodeByBankName(bankName);
	}

	@Override
	public String getXJAreaCode(String areaName, String areaLevel) {
		return xjAreaCodeRepository.getXJAreaCodeByName(areaName, areaLevel);
	}

	@Transactional
	@Override
	public XJQuickRegister createXJQuickRegister(XJQuickRegister xjQuickRegister) {
		XJQuickRegister result = xjQuickRegisterRepository.save(xjQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public XJQuickRegister getXJQuickRegister(String idCard) {
		XJQuickRegister result = xjQuickRegisterRepository.getXJQuickRegister(idCard);
		em.clear();
		return result;
	}

	@Override
	public List<BankInfoCode> getBankInfoCodeByThree(String banknum, String bankprivince, String bankcity) {
		return bankInfoCodeRepository.getBankInfoCodeByThree(banknum, bankprivince, bankcity);
	}

	@Transactional
	@Override
	public LDRegister createLDRegister(LDRegister ldRegister) {
		LDRegister result = ldRegisterRepository.save(ldRegister);
		em.flush();
		return result;
	}

	@Override
	public LDRegister getLDRegisterByIdCard(String idCard) {
		LDRegister result = ldRegisterRepository.getLDRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public WLBRegister createWLBRegister(WLBRegister wlbRegister) {
		WLBRegister result = wlbRegisterRepository.save(wlbRegister);
		em.flush();
		return result;
	}

	@Override
	public WLBRegister getWLBRegisterByIdCard(String idCard) {
		WLBRegister result = wlbRegisterRepository.getWLBRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public WLBBindCard createWLBBindCard(WLBBindCard wlbBindCard) {
		WLBBindCard result = wlbBindCardRepository.save(wlbBindCard);
		em.flush();
		return result;
	}

	@Override
	public WLBBindCard getWLBBindCardByBankCard(String bankCard) {
		WLBBindCard result = wlbBindCardRepository.getWLBBindCardBybankCard(bankCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public LFQuickRegister createLFQuickRegister(LFQuickRegister lfQuickRegister) {
		LFQuickRegister result = lfQuickRegisterRepository.save(lfQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public LFQuickRegister getLFQuickRegisterByIdCard(String idCard) {
		LFQuickRegister result = lfQuickRegisterRepository.getLFQuickRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public YHQuickRegister createYHQuickRegister(YHQuickRegister yhQuickRegister) {
		YHQuickRegister result = yhRegisterRepository.save(yhQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public YHQuickRegister getYHQuickRegisterByIdCard(String idCard) {
		YHQuickRegister result = yhRegisterRepository.getYHQuickRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public YBQuickRegister createYBQuickRegister(YBQuickRegister ybQuickRegister) {
		YBQuickRegister result = ybQuickRegisterRepository.save(ybQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public YBQuickRegister getYBQuickRegisterByIdCard(String idCard) {
		YBQuickRegister result = ybQuickRegisterRepository.getYBQuickRegister(idCard);
		em.clear();
		return result;
	}

	@Override
	public String getLFQuickRegisterByPhone(String phone) {
		String result = lfQuickRegisterRepository.getLFRegisterByPhone(phone);
		em.clear();
		return result;
	}

	@Override
	public String getLDRegisterByPhone(String phone) {
		String result = ldRegisterRepository.getLDRegisterByPhone(phone);
		em.clear();
		return result;
	}

	@Override
	public String getWLBRegisterByPhone(String phone) {
		String result = wlbRegisterRepository.getWLBRegisterByPhone(phone);
		em.clear();
		return result;
	}

	@Override
	public String getXJRegisterByPhone(String phone) {
		String result = xjQuickRegisterRepository.getXJRegisterByPhone(phone);
		em.clear();
		return result;
	}

	@Override
	public String getYHRegisterByPhone(String phone) {
		String result = yhRegisterRepository.getYHRegisterByPhone(phone);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public HLJCRegister createHLJCRegister(HLJCRegister hljcRegister) {
		HLJCRegister result = hljcRegisterRepository.save(hljcRegister);
		em.flush();
		return result;
	}

	@Override
	public HLJCRegister getHLJCRegisterByBankCard(String bankCard) {
		HLJCRegister result = hljcRegisterRepository.getHLJCRegisterByBankCard(bankCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public HLJCBindCard createHLJCBindCard(HLJCBindCard hljcBindCard) {
		HLJCBindCard result = hljcBindCardRepository.save(hljcBindCard);
		em.flush();
		return result;
	}

	@Override
	public HLJCBindCard getHLJCBindCardByBankCard(String bankCard) {
		HLJCBindCard result = hljcBindCardRepository.getHLJCBindCardByBankCard(bankCard);
		em.clear();
		return result;
	}

	@Override
	public HLJCBindCard getHLJCBindCardByOrderCode(String orderCode) {
		HLJCBindCard result = hljcBindCardRepository.getHLJCBindCardByOrderCode(orderCode);
		em.clear();
		return result;
	}

	/*@Transactional
	@Override
	public YSBRegister createYSBRegister(YSBRegister ysbRegister) {
		YSBRegister result = ysbRegisterRepository.save(ysbRegister);
		em.flush();
		return result;
	}

	@Override
	public YSBRegister getYSBRegisterByBankCard(String bankCard) {
		YSBRegister result = ysbRegisterRepository.getYSBRegisterByBankCard(bankCard);
		em.clear();
		return result;
	}*/

	@Override
	public YBQuickRegister getYBQuickRegisterByPhone(String phone) {

		return ybQuickRegisterRepository.getYBQuickRegisterByPhone(phone);
	}

	@Transactional
	@Override
	public CJHKRegister createCJHKRegister(CJHKRegister cjhkRegister) {
		CJHKRegister result = cjhkRegisterRepository.save(cjhkRegister);
		em.flush();
		return result;
	}

	@Override
	public CJHKRegister getCJHKRegisterByBankCard(String bankCard) {
		CJHKRegister result = cjhkRegisterRepository.getCJHKRegisterByBankCard(bankCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public CJHKBindCard createCJHKBindCard(CJHKBindCard cjhkBindCard) {
		CJHKBindCard result = cjhkBindCardRepository.save(cjhkBindCard);
		em.flush();
		return result;
	}

	@Override
	public CJHKBindCard getCJHKBindCardByBankCard(String bankCard) {
		CJHKBindCard result = cjhkBindCardRepository.getCJHKBindCardByBankCard(bankCard);
		em.clear();
		return result;
	}

	@Override
	public CJHKRegister getCJHKRegisterByIdCard(String idCard) {
		CJHKRegister result = cjhkRegisterRepository.getCJHKRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public WMYKBindCard createWMYKBindCard(WMYKBindCard wmykBindCard) {
		WMYKBindCard result = wmykBindCardRepository.save(wmykBindCard);
		em.flush();
		return result;
	}

	@Override
	public WMYKBindCard getWMYKBindCardByBankCard(String bankCard) {
		WMYKBindCard result = wmykBindCardRepository.getWMYKBindCardByBankCard(bankCard);
		em.clear();
		return result;
	}

	/*@Transactional
	@Override
	public HLJCQuickRegister createHLJCQuickRegister(HLJCQuickRegister hljcQuickRegister) {
		HLJCQuickRegister result = hljcQuickRegisterRepository.save(hljcQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public HLJCQuickRegister getHLJCQuickRegisterByIdCard(String idCard) {
		HLJCQuickRegister result = hljcQuickRegisterRepository.getHLJCQuickRegisterByBankCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public HLJCQuickBindCard createHLJCQuickBindCard(HLJCQuickBindCard hljcQuickBindCard) {
		HLJCQuickBindCard result = hljcQuickBindCardRepository.save(hljcQuickBindCard);
		em.flush();
		return result;
	}

	@Override
	public HLJCQuickBindCard getHLJCQuickBindCardByBankCard(String bankCard) {
		HLJCQuickBindCard result = hljcQuickBindCardRepository.getHLJCQuickBindCardByBankCard(bankCard);
		em.clear();
		return result;
	}*/

	@Transactional
	@Override
	public CJRegister createCJRegister(CJRegister cjRegister) {
		CJRegister result = cjRegisterRepository.save(cjRegister);
		em.flush();
		return result;
	}

	@Override
	public CJRegister getCJRegisterByIdCard(String idCard) {
		CJRegister result = cjRegisterRepository.getCJRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public CardEvaluation createCardEvaluation(CardEvaluation cardEvaluation) {
		CardEvaluation result = cardEvaluationRepository.save(cardEvaluation);
		em.flush();
		return result;
	}

	@Override
	public CardEvaluation getCardEvaluationByBankCard(String bankCard) {
		CardEvaluation result = cardEvaluationRepository.getCardEvaluationByBankCard(bankCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public CardEvaluationHistory createCardEvaluationHistory(CardEvaluationHistory cardEvaluationHistory) {
		CardEvaluationHistory result = cardEvaluationHistoryRepository.save(cardEvaluationHistory);
		em.flush();
		return result;
	}

	@Override
	public List<CardEvaluationHistory> getCardEvaluationHistoryByUserId(String userId) {
		return cardEvaluationHistoryRepository.getCardEvaluationHistoryByUserId(userId);
	}

	@Transactional
	@Override
	public CJBindCard createCJBindCard(CJBindCard cjBindCard) {
		CJBindCard result = cjBindCardRepository.save(cjBindCard);
		em.flush();
		return result;
	}

	@Override
	public CJBindCard getCJBindCardByBankCard(String bankCard) {
		CJBindCard result = cjBindCardRepository.getCJBindCardByBankCard(bankCard);
		em.clear();
		return result;
	}

	@Override
	public List<String> getWMYKCityMerchantNameByCityCode(String cityCode) {
		em.clear();
		List<String> result = wmykCityMerchantRepository.getWMYKCityMerchantNameByCityCode(cityCode);
		return result;
	}

	@Override
	public List<String> getWMYKCityMerchantCodeByCityCode(String cityCode) {
		em.clear();
		List<String> result = wmykCityMerchantRepository.getWMYKCityMerchantCodeByCityCode(cityCode);
		return result;
	}

	@Transactional
	@Override
	public WMYKChooseCity createWMYKChooseCity(WMYKChooseCity wmykChooseCity) {
		WMYKChooseCity result = wmykChooseCityRepository.save(wmykChooseCity);
		em.flush();
		return result;
	}

	@Override
	public WMYKChooseCity getWMYKChooseCityByBankCard(String bankCard) {
		em.clear();
		WMYKChooseCity result = wmykChooseCityRepository.getWMYKChooseCityByBankCard(bankCard);
		return result;
	}


	/*@Transactional
	@Override
	public CJQuickRegister createCJQuickRegister(CJQuickRegister cjQuickRegister) {
		CJQuickRegister result = cjQuickRegisterRepository.save(cjQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public CJQuickRegister getCJQuickRegisterByIdCard(String idCard) {
		em.clear();
		CJQuickRegister result = cjQuickRegisterRepository.getCJQuickRegisterByIdCard(idCard);
		return result;
	}*/

	@Transactional
	@Override
	public CJQuickBindCard createCJQuickBindCard(CJQuickBindCard cjQuickBindCard) {
		CJQuickBindCard result = cjQuickBindCardRepository.save(cjQuickBindCard);
		em.flush();
		return result;
	}

	@Override
	public CJQuickBindCard getCJQuickBindCardByBankCard(String bankCard) {
		em.clear();
		CJQuickBindCard result = cjQuickBindCardRepository.getCJQuickBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public List<RepaymentSupportBank> getRepaymentSupportBankByVersion(String version) {
		em.clear();
		return repaymentSupportBankRepository.getRepaymentSupportBankByVersion(version);
	}

	@Override
	public List<WMYKProvince> getWMYKProvince() {
		em.clear();
		List<WMYKProvince> result = wmykProvinceRepository.getWMYKProvince();
		return result;
	}

	@Override
	public List<WMYKCity> getWMYKCityByProvinceCode(String provinceCode) {
		em.clear();
		List<WMYKCity> result = wmykCityRepository.getWMYKCityByProvinceCode(provinceCode);
		return result;
	}

	@Override
	public List<WMYKCity> getWMYKCity() {
		em.clear();
		List<WMYKCity> result = wmykCityRepository.getWMYKCity();
		return result;
	}

	@Transactional
	@Override
	public JPRegister createJPRegister(JPRegister jpRegister) {
		JPRegister result = jpRegisterRepository.save(jpRegister);
		em.flush();
		return result;
	}

	@Override
	public JPRegister getJPRegisterByIdCard(String idCard) {
		em.clear();
		JPRegister result = jpRegisterRepository.getJPRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public JPBindCard createJPBindCard(JPBindCard jpBindCard) {
		JPBindCard result = jpBindCardRepository.save(jpBindCard);
		em.flush();
		return result;
	}

	@Override
	public JPBindCard getJPBindCardByBankCard(String bankCard) {
		em.clear();
		JPBindCard result = jpBindCardRepository.getJPBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public CJHKRegister getCJHKRegisterByIdCardAndVersion(String idCard, String version) {
		em.clear();
		CJHKRegister result = cjhkRegisterRepository.getCJHKRegisterByIdCardAndVersion(idCard, version);
		return result;
	}

	@Override
	public CJHKRegister getCJHKRegisterByIdCardAndVersions(String idCard, String versions) {
		em.clear();
		CJHKRegister result = cjhkRegisterRepository.getCJHKRegisterByIdCardAndVersions(idCard, versions);
		return result;
	}

	@Override
	public CJQuickBindCard getCJQuickBindCardByOrderCode(String orderCode) {
		em.clear();
		CJQuickBindCard result = cjQuickBindCardRepository.getCJQuickBindCardByOrderCode(orderCode);
		return result;
	}

	@Transactional
	@Override
	public YLDZBindCard createYLDZBindCard(YLDZBindCard yldzBindCard) {
		YLDZBindCard result = yldzBindCardRepository.save(yldzBindCard);
		em.flush();
		return result;
	}

	@Override
	public YLDZBindCard getYLDZBindCardByBankCard(String bankCard) {
		em.clear();
		YLDZBindCard result = yldzBindCardRepository.getYLDZBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public YBHKRegister createYBHKRegister(YBHKRegister ybhkRegister) {
		YBHKRegister result = ybhkRegisterRepository.save(ybhkRegister);
		em.flush();
		return result;
	}

	@Override
	public YBHKRegister getYBHKRegisterByIdCard(String idCard) {
		em.clear();
		YBHKRegister result = ybhkRegisterRepository.getYBHKRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public YBHKBindCard createYBHKBindCard(YBHKBindCard ybhkBindCard) {
		YBHKBindCard result = ybhkBindCardRepository.save(ybhkBindCard);
		em.flush();
		return result;
	}

	@Override
	public YBHKBindCard getYBHKBindCardByBankCard(String bankCard) {
		em.clear();
		YBHKBindCard result = ybhkBindCardRepository.getYBHKBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public KYRegister createKYRegister(KYRegister kyRegister) {
		KYRegister result = kyRegisterRepository.save(kyRegister);
		em.flush();
		return result;
	}

	@Override
	public KYRegister getKYRegisterByIdCard(String idCard) {
		em.clear();
		KYRegister result = kyRegisterRepository.getKYRegisterByIdCard(idCard);
		return result;
	}

	@Override
	public List<WMYKNewCity> getWMYKNewCity() {
		em.clear();
		List<WMYKNewCity> result = wmykNewCityRepository.getWMYKNewCity();
		return result;
	}

	@Override
	public List<WMYKNewCityMerchant> getWMYKNewCityMerchantByCityCode(String cityCode) {
		em.clear();
		List<WMYKNewCityMerchant> result = wmykNewCityMerchantRepository.getWMYKNewCityMerchantByCityCode(cityCode);
		return result;
	}

	@Transactional
	@Override
	public WMYKNewChooseCity createWMYKNewChooseCity(WMYKNewChooseCity wmykNewChooseCity) {
		WMYKNewChooseCity result = wmykNewChooseCityRepository.save(wmykNewChooseCity);
		em.flush();
		return result;
	}

	@Override
	public WMYKNewChooseCity getWMYKNewChooseCityByBankCard(String bankCard) {
		em.clear();
		WMYKNewChooseCity result = wmykNewChooseCityRepository.getWMYKNewChooseCityByBankCard(bankCard);
		return result;
	}

	@Override
	public List<String> getWMYKNewCityMerchantCodeByCityCode(String cityCode) {
		em.clear();
		List<String> result = wmykNewCityMerchantRepository.getWMYKNewCityMerchantCodeByCityCode(cityCode);
		return result;
	}

	@Override
	public List<WMYKNewCity> getWMYKNewCityByProvinceCode(String provinceCode) {
		em.clear();
		List<WMYKNewCity> result = wmykNewCityRepository.getWMYKNewCityByProvinceCode(provinceCode);
		return result;
	}

	@Override
	public List<WMYKNewProvince> getWMYKNewProvince() {
		em.clear();
		List<WMYKNewProvince> result = wmykNewProvinceRepository.getWMYKNewProvince();
		return result;
	}

	@Transactional
	@Override
	public WMYKNewBindCard createWMYKNewBindCard(WMYKNewBindCard wmykNewBindCard) {
		WMYKNewBindCard result = wmykNewBindCardRepository.save(wmykNewBindCard);
		em.flush();
		return result;
	}

	@Override
	public WMYKNewBindCard getWMYKNewBindCardByBankCard(String bankCard) {
		em.clear();
		WMYKNewBindCard result = wmykNewBindCardRepository.getWMYKNewBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public List<WMYKXWKCityMerchant> getWMYKXWKCityMerchantByCityCode(String cityCode) {
		em.clear();
		List<WMYKXWKCityMerchant> result = wmykXWKCityMerchantRepository.getWMYKXWKCityMerchantByCityCode(cityCode);
		return result;
	}

	@Override
	public List<String> getWMYKXWKCityMerchantCodeByCityCode(String cityCode) {
		em.clear();
		List<String> result = wmykXWKCityMerchantRepository.getWMYKXWKCityMerchantCodeByCityCode(cityCode);
		return result;
	}

	@Transactional
	@Override
	public RSRegister createRSRegister(RSRegister rsRegister) {
		RSRegister  result = rsRegisterRepository.save(rsRegister);
		return  result;
	}

	@Override
	public RSRegister getRSRegisterByIdCard(String idCard) {
		RSRegister  result = rsRegisterRepository.getRSRegisterByIdCard(idCard);
		return  result;
	}

	@Override
	public WMYKXWKCityMerchant getWMYKXWKCityMerchantByMerchantName(String merchantName) {
		em.clear();
		WMYKXWKCityMerchant result = wmykXWKCityMerchantRepository.getWMYKXWKCityMerchantByMerchantName(merchantName);
		return result;
	}

	@Override
	public WMYKXWKCityMerchant getWMYKXWKCityMerchantByMerchantCode(String merchantCode) {
		em.clear();
		WMYKXWKCityMerchant result = wmykXWKCityMerchantRepository.getWMYKXWKCityMerchantByMerchantCode(merchantCode);
		return result;
	}

	@Override
	public List<RepaymentInfoMation> getRepaymentInfoMationAll() {
		em.clear();
		List<RepaymentInfoMation> result = repaymentInfoMationRepository.getRepaymentInfoMationAll();
		return result;
	}

	@Override
	public List<ChannelDetail> getChannelDetailByNO(String channelNo) {
		return channelDetailRepository.getChannelDetailByNo(channelNo);
	}

	
}
