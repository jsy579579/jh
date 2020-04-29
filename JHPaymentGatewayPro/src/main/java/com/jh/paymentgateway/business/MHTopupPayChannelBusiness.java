package com.jh.paymentgateway.business;

import java.util.List;

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


public interface MHTopupPayChannelBusiness {

	public BankNumCode getBankNumCodeByBankName(String bankName);

	public MHHQRegister createMHHQRegister(MHHQRegister hqRegister);

	public MHHQRegister getMHHQRegisterByIdCard(String idCard);

	public MHHQRegister getMHHQRegisterByMerchantOrder(String merchantOrder);
	
	public MHHQBindCard createMHHQBindCard(MHHQBindCard hqBindCard);

	public MHHQBindCard getMHHQBindCardByBankCard(String bankCard);

	public MHHQBBindCard createMHHQBBindCard(MHHQBBindCard hqbBindCard);

	public MHHQBBindCard getMHHQBBindCardByBankCard(String bankCard);

	public MHHQBBindCard getMHHQBBindCardByUserId(String userId);
	
	public MHHQGRegister createMHHQGRegister(MHHQGRegister hqRegister);

	public MHHQGRegister getMHHQGRegisterByIdCard(String idCard);

	public MHHQGRegister getMHHQGRegisterByMerchantOrder(String merchantOrder);
	
	public MHHQGBindCard createMHHQGBindCard(MHHQGBindCard hqBindCard);
	
	public MHHQGBindCard getMHHQGBindCardbyMerchantOrder(String merchantOrder);

	public MHHQGBindCard getMHHQGBindCardByBankCard(String bankCard);
	
	public List<HQGProvinceCity> getHQGProvinceCityByHkProvinceCode();
	
	public List<HQGProvinceCity> getHQGProvinceCityGroupByCity(String cityCode);
	
	public MHHQQuickRegister createMHHQQuickRegister(MHHQQuickRegister hqQuickRegister);
	
	public MHHQQuickRegister getMHHQQuickRegisterByIdCard(String idCard);
	
	public MHHQDHRegister createMHHQDHRegister(MHHQDHRegister hqdhRegister);
	
	public MHHQDHRegister getMHHQDHRegisterByIdCard(String idCard);
		
	public GHTBindCard createGHTBindCard(GHTBindCard ghtBindCard);
	
	public GHTBindCard getGHTBindCardByBankCard(String bankCard);
	
	public GHTBindCard getGHTBindCardByOrderCode(String orderCode);
	
	public List<String> getMHGHTCityMerchantProvince();
	
	public List<String> getMHGHTCityMerchantCityByProvince(String province);
	
	public List<MHGHTCityMerchant> getMHGHTCityMerchantByProvinceAndCity(String province, String city);
	
	public List<String> getMHGHTCityMerchantCodeByProvinceAndCity(String province, String city);
	
	public List<MHGHTXwkCityMerchant> getMHGHTXwkCityMerchantByProvinceAndCity(String province, String city);
	
	public List<String> getMHGHTXwkCityMerchantCodeByProvinceAndCity(String province, String city);
		
	public ChannelSupportDebitBankCard getChannelSupportDebitBankCardByChannelTagAndBankName(String channelTag, String bankName);
	
	public List<String> getChannelSupportDebitBankCardByChannelTag(String channelTag);
		
	public void createMHHQEBindCard(MHHQEBindCard hqeBindCard);
	
	public MHHQEBindCard getMHHQEBindCardByBankCard(String bankCard);
	
	public MHHQEBindCard getMHHQEBindCardByOrderCode(String orderCode);
	
	public List<HQERegion> getHQERegionByParentId(String parentId);
	
	public MHGHTXwkCityMerchant getMHGHTXwkCityMerchantByMerchantCode(String merchantCode);

	public MHGHTXwkCityMerchant getMHGHTXwkCityMerchantByMerchantName(String merchantName);
	
	public MHGHTCityMerchant getMHGHTCityMerchantByMerchantCode(String merchantCode);
	
	public MHGHTCityMerchant getMHGHTCityMerchantByMerchantName(String merchantName);

	public MHHQHRegister getMHHQHRegisterByIdCard(String idCard);

	public MHHQHBindCard getMHHQHBindCardByBankCard(String bankCard);

	public MHHQHRegister createMHHQHRegister(MHHQHRegister hqxhmRegister);

	public MHHQHBindCard createMHHQHBindCard(MHHQHBindCard hqxgmBindCard);

	public MHHQHBindCard getMHHQHBindCardbyMerchantOrder(String dsorderid);

	public List<MCCpo> getMCCpo();

	public MCCpo getMCCpoByType(String type);
	
}
