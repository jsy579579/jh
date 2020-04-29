package com.jh.paymentgateway.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.ChannelSupportDebitBankCard;
import com.jh.paymentgateway.pojo.GHTBindCard;
import com.jh.paymentgateway.pojo.HQERegion;
import com.jh.paymentgateway.pojo.HQGProvinceCity;
import com.jh.paymentgateway.pojo.MCCpo;
import com.jh.paymentgateway.pojo.MHGHTCityMerchant;
import com.jh.paymentgateway.pojo.MHGHTXwkCityMerchant;
import com.jh.paymentgateway.pojo.MHHQBBindCard;
import com.jh.paymentgateway.pojo.MHHQBindCard;
import com.jh.paymentgateway.pojo.MHHQDHRegister;
import com.jh.paymentgateway.pojo.MHHQEBindCard;
import com.jh.paymentgateway.pojo.MHHQGBindCard;
import com.jh.paymentgateway.pojo.MHHQGRegister;
import com.jh.paymentgateway.pojo.MHHQHBindCard;
import com.jh.paymentgateway.pojo.MHHQHRegister;
import com.jh.paymentgateway.pojo.MHHQQuickRegister;
import com.jh.paymentgateway.pojo.MHHQRegister;
import com.jh.paymentgateway.repository.BankNumCodeRepository;
import com.jh.paymentgateway.repository.ChannelSupportDebitBankCardRepository;
import com.jh.paymentgateway.repository.GHTBindCardRepository;
import com.jh.paymentgateway.repository.HQERegionRepository;
import com.jh.paymentgateway.repository.HQGProvinceCityRepository;
import com.jh.paymentgateway.repository.MCCRepository;
import com.jh.paymentgateway.repository.MHGHTCityMerchantRepository;
import com.jh.paymentgateway.repository.MHGHTXwkCityMerchantRepository;
import com.jh.paymentgateway.repository.MHHQBBindCardRepository;
import com.jh.paymentgateway.repository.MHHQBindCardRepository;
import com.jh.paymentgateway.repository.MHHQDHRegisterRepository;
import com.jh.paymentgateway.repository.MHHQEBindCardRepository;
import com.jh.paymentgateway.repository.MHHQGBindCardRepository;
import com.jh.paymentgateway.repository.MHHQGRegisterRepository;
import com.jh.paymentgateway.repository.MHHQQuickRegisterRepository;
import com.jh.paymentgateway.repository.MHHQRegisterRepository;

@Service
public class MHTopupPayChannelBusinessImpl implements MHTopupPayChannelBusiness {

	@Autowired
	private MHHQRegisterRepository hqRegisterRepository;

	@Autowired
	private MHHQBindCardRepository hqBindCardRepository;
	
	@Autowired
	private MHHQGRegisterRepository hqgRegisterRepository;
	
	@Autowired
	private MHHQGBindCardRepository hqgBindCardRepository;
	
	@Autowired
	private HQGProvinceCityRepository hqgProvinceCityRepository;

	@Autowired
	private MHHQBBindCardRepository hQBBindCardRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private MHHQQuickRegisterRepository hqQuickRegisterRepository;

	@Autowired
	private MHHQDHRegisterRepository hqdhRegisterRepository;

	@Autowired
	private GHTBindCardRepository ghtBindCardRepository;

	@Autowired
	private MHGHTCityMerchantRepository ghtCityMerchantRepository;

	@Autowired
	private MHGHTXwkCityMerchantRepository ghtXwkCityMerchantRepository;

	@Autowired
	private ChannelSupportDebitBankCardRepository channelSupportDebitBankCardRepository;

	@Autowired
	private MHHQEBindCardRepository hqeBindCardRepository;

	@Autowired
	private HQERegionRepository hqeRegionRepository;

	@Autowired
	private BankNumCodeRepository bankNumCodeRepository;
	
	@Autowired
	private MCCRepository mccRepository;
	
	@Transactional
	@Override
	public MHHQRegister createMHHQRegister(MHHQRegister hqRegister) {
		MHHQRegister result = hqRegisterRepository.save(hqRegister);
		em.flush();
		return result;
	}

	@Override
	public MHHQRegister getMHHQRegisterByIdCard(String idCard) {
		em.clear();
		MHHQRegister result = hqRegisterRepository.getMHHQRegisterByIdCard(idCard);
		return result;
	}

	@Override
	public MHHQRegister getMHHQRegisterByMerchantOrder(String merchantOrder) {
		em.clear();
		MHHQRegister result = hqRegisterRepository.getMHHQRegisterByMerchantOrder(merchantOrder);
		return result;
	}

	@Transactional
	@Override
	public MHHQBindCard createMHHQBindCard(MHHQBindCard hqBindCard) {
		MHHQBindCard result = hqBindCardRepository.save(hqBindCard);
		em.flush();
		return result;
	}

	@Override
	public MHHQBindCard getMHHQBindCardByBankCard(String bankCard) {
		em.clear();
		MHHQBindCard result = hqBindCardRepository.getMHHQBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public MHHQBBindCard createMHHQBBindCard(MHHQBBindCard hqbBindCard) {
		MHHQBBindCard result = hQBBindCardRepository.save(hqbBindCard);
		em.flush();
		return result;
	}

	@Override
	public MHHQBBindCard getMHHQBBindCardByBankCard(String bankCard) {
		em.clear();
		MHHQBBindCard result = hQBBindCardRepository.getMHHQBBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public MHHQQuickRegister createMHHQQuickRegister(MHHQQuickRegister hqQuickRegister) {
		MHHQQuickRegister result = hqQuickRegisterRepository.save(hqQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public MHHQQuickRegister getMHHQQuickRegisterByIdCard(String idCard) {
		em.clear();
		MHHQQuickRegister result = hqQuickRegisterRepository.getMHHQQuickRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public MHHQDHRegister createMHHQDHRegister(MHHQDHRegister hqdhRegister) {
		MHHQDHRegister result = hqdhRegisterRepository.save(hqdhRegister);
		em.flush();
		return result;
	}

	@Override
	public MHHQDHRegister getMHHQDHRegisterByIdCard(String idCard) {
		em.clear();
		MHHQDHRegister result = hqdhRegisterRepository.getMHHQDHRegisterrByIdCard(idCard);
		return result;
	}

	@Override
	public List<String> getMHGHTCityMerchantProvince() {
		em.clear();
		List<String> result = ghtCityMerchantRepository.getMHGHTCityMerchantProvince();
		return result;
	}

	@Override
	public List<String> getMHGHTCityMerchantCityByProvince(String province) {
		em.clear();
		List<String> result = ghtCityMerchantRepository.getMHGHTCityMerchantCityByProvince(province);
		return result;
	}

	@Override
	public List<MHGHTCityMerchant> getMHGHTCityMerchantByProvinceAndCity(String province, String city) {
		em.clear();
		List<MHGHTCityMerchant> result = ghtCityMerchantRepository.getMHGHTCityMerchantByProvinceAndCity(province, city);
		return result;
	}

	@Override
	public List<String> getMHGHTCityMerchantCodeByProvinceAndCity(String province, String city) {
		em.clear();
		List<String> result = ghtCityMerchantRepository.getMHGHTCityMerchantCodeByProvinceAndCity(province, city);
		return result;
	}

	@Override
	public List<MHGHTXwkCityMerchant> getMHGHTXwkCityMerchantByProvinceAndCity(String province, String city) {
		em.clear();
		List<MHGHTXwkCityMerchant> result = ghtXwkCityMerchantRepository.getMHGHTXwkCityMerchantByProvinceAndCity(province,
				city);
		return result;
	}

	@Override
	public List<String> getMHGHTXwkCityMerchantCodeByProvinceAndCity(String province, String city) {
		em.clear();
		List<String> result = ghtXwkCityMerchantRepository.getMHGHTXwkCityMerchantCodeByProvinceAndCity(province, city);
		return result;
	}

	@Transactional
	@Override
	public void createMHHQEBindCard(MHHQEBindCard hqeBindCard) {
		hqeBindCardRepository.save(hqeBindCard);
		em.flush();
	}

	@Override
	public MHHQEBindCard getMHHQEBindCardByBankCard(String bankCard) {
		em.clear();
		MHHQEBindCard result = hqeBindCardRepository.getMHHQEBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public MHHQEBindCard getMHHQEBindCardByOrderCode(String orderCode) {
		em.clear();
		MHHQEBindCard result = hqeBindCardRepository.getMHHQEBindCardByOrderCode(orderCode);
		return result;
	}

	@Override
	public MHHQBBindCard getMHHQBBindCardByUserId(String userId) {
		em.clear();
		MHHQBBindCard result = hQBBindCardRepository.getMHHQBBindCardByUserId(userId);
		return result;
	}

	@Transactional
	@Override
	public MHHQGRegister createMHHQGRegister(MHHQGRegister hqRegister) {
		MHHQGRegister hqgRegister=hqgRegisterRepository.save(hqRegister);
		em.flush();
		return hqgRegister;
	}

	@Override
	public MHHQGRegister getMHHQGRegisterByIdCard(String idCard) {
		em.clear();
		return hqgRegisterRepository.getMHHQGRegisterByIdCard(idCard);
	}

	@Override
	public MHHQGRegister getMHHQGRegisterByMerchantOrder(String merchantOrder) {
		em.clear();
		return hqgRegisterRepository.getMHHQGRegisterByMerchantOrder(merchantOrder);
	}
	
	@Transactional
	@Override
	public MHHQGBindCard createMHHQGBindCard(MHHQGBindCard hqBindCard) {
		MHHQGBindCard hqgBindCard=hqgBindCardRepository.save(hqBindCard);
		em.flush();
		return hqgBindCard;
	}

	@Override
	public MHHQGBindCard getMHHQGBindCardbyMerchantOrder(String merchantOrder) {
		em.clear();
		return hqgBindCardRepository.getMHHQGBindCardbyMerchantOrder(merchantOrder);
	}
	
	@Override
	public MHHQGBindCard getMHHQGBindCardByBankCard(String bankCard) {
		em.clear();
		return hqgBindCardRepository.getMHHQGBindCardByBankCard(bankCard);
	}
	
	@Override
	public MHGHTXwkCityMerchant getMHGHTXwkCityMerchantByMerchantCode(String merchantCode) {
		em.clear();
		MHGHTXwkCityMerchant result = ghtXwkCityMerchantRepository.getMHGHTXwkCityMerchantByMerchantCode(merchantCode);
		return result;
	}

	@Override
	public MHGHTXwkCityMerchant getMHGHTXwkCityMerchantByMerchantName(String merchantName) {
		em.clear();
		MHGHTXwkCityMerchant result = ghtXwkCityMerchantRepository.getMHGHTXwkCityMerchantByMerchantName(merchantName);
		return result;
	}

	@Override
	public MHGHTCityMerchant getMHGHTCityMerchantByMerchantCode(String merchantCode) {
		em.clear();
		MHGHTCityMerchant result = ghtCityMerchantRepository.getMHGHTCityMerchantByMerchantCode(merchantCode);
		return result;
	}

	@Override
	public MHGHTCityMerchant getMHGHTCityMerchantByMerchantName(String merchantName) {
		em.clear();
		MHGHTCityMerchant result = ghtCityMerchantRepository.getMHGHTCityMerchantByMerchantName(merchantName);
		return result;
	}

	@Override
	public MHHQHRegister getMHHQHRegisterByIdCard(String idCard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MHHQHBindCard getMHHQHBindCardByBankCard(String bankCard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MHHQHBindCard getMHHQHBindCardbyMerchantOrder(String dsorderid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional
	@Override
	public GHTBindCard createGHTBindCard(GHTBindCard ghtBindCard) {
		GHTBindCard result = ghtBindCardRepository.save(ghtBindCard);
		em.flush();
		return result;
	}

	@Override
	public GHTBindCard getGHTBindCardByBankCard(String bankCard) {
		em.clear();
		GHTBindCard result = ghtBindCardRepository.getGHTBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public GHTBindCard getGHTBindCardByOrderCode(String orderCode) {
		em.clear();
		GHTBindCard result = ghtBindCardRepository.getGHTBindCardByOrderCode(orderCode);
		return result;
	}

	@Override
	public BankNumCode getBankNumCodeByBankName(String bankName) {
		return bankNumCodeRepository.getBankNumCodeByBankName(bankName);
	}

	public List<HQGProvinceCity> getHQGProvinceCityByHkProvinceCode(){
		
		return  hqgProvinceCityRepository.getHQGProvinceCityByHkProvinceCode();
	}
	
	public List<HQGProvinceCity> getHQGProvinceCityGroupByCity(String cityCode){
		
		return hqgProvinceCityRepository.getHQGProvinceCityGroupByCity(cityCode);
	}

	@Override
	public ChannelSupportDebitBankCard getChannelSupportDebitBankCardByChannelTagAndBankName(String channelTag,
			String bankName) {
		em.clear();
		ChannelSupportDebitBankCard result = channelSupportDebitBankCardRepository
				.getChannelSupportDebitBankCardByChannelTagAndBankName(channelTag, bankName);
		return result;
	}

	@Override
	public List<String> getChannelSupportDebitBankCardByChannelTag(String channelTag) {
		em.clear();
		List<String> result = channelSupportDebitBankCardRepository
				.getChannelSupportDebitBankCardByChannelTag(channelTag);
		return result;
	}

	@Override
	public List<HQERegion> getHQERegionByParentId(String parentId) {
		em.clear();
		List<HQERegion> result = hqeRegionRepository.getHQERegionByParentId(parentId);
		return result;
	}

	@Override
	public MHHQHRegister createMHHQHRegister(MHHQHRegister hqxhmRegister) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MHHQHBindCard createMHHQHBindCard(MHHQHBindCard hqxgmBindCard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MCCpo> getMCCpo() {
		em.clear();
		List<MCCpo> result = mccRepository.getMCCpo();
		return result;
	}

	@Override
	public MCCpo getMCCpoByType(String type) {
		em.clear();
		MCCpo result = mccRepository.getMCCpoByType(type);
		return result;
	}
	
}
