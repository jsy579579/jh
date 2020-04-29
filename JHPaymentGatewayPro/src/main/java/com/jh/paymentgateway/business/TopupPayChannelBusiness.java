package com.jh.paymentgateway.business;

import com.jh.paymentgateway.li.YiBao;
import com.jh.paymentgateway.pojo.*;
import com.jh.paymentgateway.pojo.hq.HQNEWBindCard;
import com.jh.paymentgateway.pojo.hq.HQNEWRegister;
import com.jh.paymentgateway.pojo.hx.HXDHXBindCard;
import com.jh.paymentgateway.pojo.hx.HXDHXRegister;
import com.jh.paymentgateway.pojo.kft.KFTBindCard;
import com.jh.paymentgateway.pojo.kft.KFTRegister;
import com.jh.paymentgateway.pojo.qj.QJBindCard;
import com.jh.paymentgateway.pojo.qj.QJRegister;
import com.jh.paymentgateway.pojo.tl.TLDHXBindCard;
import com.jh.paymentgateway.pojo.tl.TLDHXRegister;
import com.jh.paymentgateway.pojo.xkdhd.XKArea;
import com.jh.paymentgateway.pojo.xkdhd.XKBankType;
import com.jh.paymentgateway.pojo.xkdhd.XKDHDBindCard;
import com.jh.paymentgateway.pojo.xkdhd.XKDHDRegister;
import com.jh.paymentgateway.pojo.ypl.*;

import java.util.List;

public interface TopupPayChannelBusiness {

	KFTBindCard saveKftBindCard(KFTBindCard card);

	KFTBindCard findKftBindCardByBankCard(String bankCard);

	KFTRegister createKFTRegister(KFTRegister register);

	List<String> findKftBankCodeByName(String name);

	public BankNumCode getBankNumCodeByBankName(String bankName);

	public WFRegister createWFRegister(WFRegister wfRegister);

	public WFRegister getWFRegisterByIdCard(String idCard);

	public WFBindCard createWFBindCard(WFBindCard wfBindCard);

	public WFBindCard getWFBindCardByBankCard(String bankCard);

	public JFRegister createJFRegister(JFRegister jfRegister);

	public JFRegister getJFRegisterByIdCard(String idCard);

	public JFBindCard createJFBindCard(JFBindCard jfBindCard);

//	//通过银行卡号找到绑卡信息
//    public JFBindCard findJFBindCard(String bankcard);

	public JFBindCard getJFBindCardByBankCard(String bankCard);

	public RHJFRegister createRHJFRegister(RHJFRegister rhjfRegister);

	public RHJFRegister getRHJFRegisterByIdCard(String idCard);

	public RHJFBindCard createRHJFBindCard(RHJFBindCard rhjfBindCard);

	public RHJFBindCard getRHJFBindCardByBankCard(String bankCard, String status);

	public HQRegister createHQRegister(HQRegister hqRegister);

	public HQRegister getHQRegisterByIdCard(String idCard);

	public HQRegister getHQRegisterByMerchantOrder(String merchantOrder);

	public HQBindCard createHQBindCard(HQBindCard hqBindCard);

	public HQBindCard getHQBindCardByBankCard(String bankCard);

	public HQBBindCard createHQBBindCard(HQBBindCard hqbBindCard);

	public HQBBindCard getHQBBindCardByBankCard(String bankCard);

	public HQBBindCard getHQBBindCardByUserId(String userId);

	public HQGRegister createHQGRegister(HQGRegister hqRegister);

	public HQGRegister getHQGRegisterByIdCard(String idCard);

	public HQGRegister getHQGRegisterByMerchantOrder(String merchantOrder);

	public HQGBindCard createHQGBindCard(HQGBindCard hqBindCard);

	public HQGBindCard getHQGBindCardbyMerchantOrder(String merchantOrder);

	public HQGBindCard getHQGBindCardByBankCard(String bankCard);

	public List<HQGProvinceCity> getHQGProvinceCityByHkProvinceCode();

	public List<HQGProvinceCity> getHQGProvinceCityGroupByCity(String cityCode);

	public List<MCCpo> getMCCpo();

	public MCCpo getMCCpoByType(String type);

	public BQRegister createBQRegister(BQRegister bQRegister);

	public BQBankCard createBQBankCard(BQBankCard bQBankCard);

	public BQRegister getBQRegisterByIdNum(String idNum);

	public BQBankCard getBQBankCardByIdNum(String idNum, String acct_no);

	public BQBankCard getBQBankCardByIdNumSure(String idNum, String acct_no);

	public YFJRRegister createYFJRRegister(YFJRRegister yFJRRegister);

	public YFJRRegister getYFJRRegisterByIdNum(String idNum);

	public YFJRBinkCard createYFJRBinkCard(YFJRBinkCard yFJRBinkCard);

	public YFJRBinkCard getYFJRBinkCardByIdNum(String idCard);

	public YFJRBinkCard getYFJRBinkCardByIdNum(String idCard, String bankCard, String status);

	public YFJRBinkCard getYFJRBinkCardByAppOrderId(String appOrderId);

	public RHJFRegister getRHJFRegisterByMerchantNo(String merchantNo);

	public KYRegister createKYRegister(KYRegister kyRegister);

	public KYRegister getKYRegisterByIdCard(String idCard);

	public KYBindCard createKYBindCard(KYBindCard kyBindCard);

	public KYBindCard getKYBindCardByBankCard(String bankCard);

	public CJRegister createCJRegister(CJRegister cjRegister);

	public CJRegister getCJRegisterByIdCard(String idCard);

	public CJBindCard createCJBindCard(CJBindCard cjBindCard);

	public CJBindCard getCJBindCardByBankCard(String bankCard);

	public LDRegister createLDRegister(LDRegister ldRegister);

	public LDRegister getLDRegisterByIdCard(String idCard);

	public String getLDRegisterByPhone(String phone);

	public BQXRegister createBQXRegister(BQXRegister bqxRegister);

	public BQXRegister getBQXRegisterByIdCard(String idCard);

	public BQXBindCard createBQXBindCard(BQXBindCard bqxBindCard);

	public BQXBindCard getBQXBindCardByBankCard(String bankCard);

	public List<BqxCode> findBqxCodeProvince();

	public List<BqxCode> findBqxCodeCity(String areaId);

	public List<BqxMerchant> findBqxMerchant();

	public KQRegister createKQRegister(KQRegister kqRegister);

	public KQRegister getKQRegisterByIdCard(String idCard);

	public KQBindCard createKQBindCard(KQBindCard kqBindCard);

	public KQBindCard getKQBindCardByBankCard(String bankCard);

	public YHQuickRegister createYHQuickRegister(YHQuickRegister yhQuickRegister);

	public YHQuickRegister getYHQuickRegisterByIdCard(String idCard);

	public YiBao createYiBao(YiBao yibao);

	public YiBao getYiBaoBymemberNo(String memberNo);

	public YTJFSignCard getYTJFSignCardByIdCard(String idCard);

	public YTJFSignCard getYTJFSignCardByBankCard(String bankCard);

	public YTJFSignCard createYTJFSignCard(YTJFSignCard ytjfSignCard);

	public SSBindCard createSSBindCard(SSBindCard ssBindCard);

	public SSBindCard getSSBindCardByBankCard(String bankCard);

	public SSBindCard getSSBindCardByBindId(String bindId);

	public HQQuickRegister createHQQuickRegister(HQQuickRegister hqQuickRegister);

	public HQQuickRegister getHQQuickRegisterByIdCard(String idCard);

	public HQDHRegister createHQDHRegister(HQDHRegister hqdhRegister);

	public HQDHRegister getHQDHRegisterByIdCard(String idCard);

	public JFXRegister getJFXRegisterByIdCard(String idCard);

	public JFXRegister createJFXRegister(JFXRegister jfxRegister);

	public JFXBindCard getJFXBindCardByBankCard(String bankCard);

	public JFXBindCard createJFXBindCard(JFXBindCard jFXBindCard);

	public FFZCRegister getFFZCRegisterByIdCard(String idCard);

	public FFZCRegister createFFZCRegister(FFZCRegister fFZCRegister);

	public XSRegister getXSRegisterByIdCard(String idCard);

	public XSRegister saveXSRegister(XSRegister xSRegister);

	public XSAccount saveXSAccount(XSAccount xsAccount);

	public XSBindCard saveXSBindCard(XSBindCard xsBindCard);

	public XSAccount findXSAccountByIdCard(String idcard);

	public XSBindCard findByXSBindCardByCardNo(String bankCard);

	public List<XSHKProvince> findXSHKProvince();

	public List<XSHKProvince> findXSHKProvinceByProvince(String province);

	public GHTBindCard createGHTBindCard(GHTBindCard ghtBindCard);

	public GHTBindCard getGHTBindCardByBankCard(String bankCard);

	public GHTBindCard getGHTBindCardByOrderCode(String orderCode);

	public List<String> getGHTCityMerchantProvince();

	public List<String> getGHTCityMerchantCityByProvince(String province);

	public List<GHTCityMerchant> getGHTCityMerchantByProvinceAndCity(String province, String city);

	public List<String> getGHTCityMerchantCodeByProvinceAndCity(String province, String city);

	public List<GHTXwkCityMerchant> getGHTXwkCityMerchantByProvinceAndCity(String province, String city);

	public List<String> getGHTXwkCityMerchantCodeByProvinceAndCity(String province, String city);

	public CJHKRegister createCJHKRegister(CJHKRegister cjhkRegister);

	public CJHKRegister getCJHKRegisterByIdCard(String idCard);

	public CJQuickBindCard createCJQuickBindCard(CJQuickBindCard cjQuickBindCard);

	public CJQuickBindCard getCJQuickBindCardByBankCard(String bankCard);

	public CJXChannelCode getCJXChannelCode(String bankName);

	public FFZCBindCard createFFZCBindCard(FFZCBindCard ffzcBindCard);

	public FFZCBindCard getFFZCBindCardByBankCard(String bankCard);

	public void createKBRegister(KBRegister kbRegister);

	public KBRegister getKBRegisterByIdCard(String idCard);

	public void createKBBindCard(KBBindCard kbBindCard);

	public KBBindCard getKBBindCardByBankCard(String bankCard);

	public String getKBErrorDescByErrorCode(String errorCode);

	public ChannelSupportDebitBankCard getChannelSupportDebitBankCardByChannelTagAndBankName(String channelTag,
                                                                                             String bankName);

	public List<String> getChannelSupportDebitBankCardByChannelTag(String channelTag);

	public List<CJQuickBindCard> findCJQuickBindCardByIdCard(String idCard);

	public TYTRegister getTYTRegisterByIdCard(String idCard);

	public TYTRegister createTYTRegister(TYTRegister tytRegister);

	public YBQuickRegister createYBQuickRegister(YBQuickRegister ybQuickRegister);

	public YBQuickRegister getYBQuickRegisterByidCard(String idCard);

	public YBQuickRegister getYBQuickRegisterByPhone(String phone);

	public void createHQEBindCard(HQEBindCard hqeBindCard);

	public HQEBindCard getHQEBindCardByBankCard(String bankCard);

	public HQEBindCard getHQEBindCardByOrderCode(String orderCode);

	public List<HQERegion> getHQERegionByParentId(String parentId);

	public YBSRegister createYBSRegister(YBSRegister ybsRegister);

	public YBSRegister getYBSRegisterByidCard(String idCard);

	public YBQuickRegister getYBQuickRegisterByIdCard(String idCard);

	public HZHKRegister getHZHKRegisterByidCard(String idCard);

	public HZHKOrder getHZHKOrderByorderCode(String orderCode);

	public HZHKRegister createHZHKRegister(HZHKRegister hzhkRegister);

	public HZHKOrder createHZHKOrder(HZHKOrder hzhkOrder);

	public HZHKBindCard getHZHKBindCardByBankCard(String bankCard);

	public HZHKBindCard createHZHKBindCard(HZHKBindCard hzhkBindCard);

	public List<HZHKCode> findHZHKCodeProvince();

	public List<HZHKCode> findHZHKCodeCity(String areaId);

	public JFSRegister getJFSRegisterByIdCard(String idCard);

	public JFSRegister createJFSRegister(JFSRegister jfsRegister);

	public JFSBindCard getJFSBindCardByBankCard(String bankCard);

	public JFSBindCard createJFSBindCard(JFSBindCard jfsBindCard);

	public RYTBindCard getRYTBindCardByBankCard(String bankCard);

	public RYTBindCard createRYTBindCard(RYTBindCard rytBindCard);

	public RYTRegister getRYTRegisterByIdcard(String idcard);

	public RYTRegister createRYTRegister(RYTRegister rytRegister);

	public RYTProvinceCity getRYTProvinceCityByNumber(String number);

	public List<RYTProvinceCity> getRYTProvinceCityByprovince();

	public List<RYTProvinceCity> getRYTProvinceCityGroupByCity(String province);

	public LMRegister getLMRegisterByidCard(String idCard);

	public LMRegister createLMRegister(LMRegister lm);

	public LMBankNum getLMBankNumCodeByBankName(String bankName);

	public List<CJHKFactory> getCJHKChooseCityIPBycityName(String name);

	public LMTRegister getlmtRegisterByidCard(String idCard);

	public LMTRegister createlmtRegister(LMTRegister lmt);

	public List<LMTAddress> findLMTProvince();

	public List<LMTAddress> findLMTCityByProvinceId(String provinceId);

	public List<LMTAddress> findLMTCityByCityId(String cityId);

	public LMTAddress getLMTProvinceCode(Long id);

	public GHTXwkCityMerchant getGHTXwkCityMerchantByMerchantCode(String merchantCode);

	public GHTXwkCityMerchant getGHTXwkCityMerchantByMerchantName(String merchantName);

	public GHTCityMerchant getGHTCityMerchantByMerchantCode(String merchantCode);

	public GHTCityMerchant getGHTCityMerchantByMerchantName(String merchantName);

	public LMDHRegister getlmdhRegisterByidCard(String idCard);

	public LMDHRegister createlmdhRegister(LMDHRegister lmdh);

	public LMDHBindCard getLMDHBindCardByBankCard(String bankCard);

	public LMDHBindCard createLMDHBindCard(LMDHBindCard lmdhbindCard);

	public LMTAddress getLMTProvinceCode(String provinceOfBank);

	public LMTAddress getLMTProvinceCode(String areaOfBank, String ciCode);

	public HZDHAddress getHZDHXAddress(Long areaId);

	public List<HZDHAddress> findHZDHProvince();

	public List<HZDHAddress> findHZDHMerchant(String provinceName);

	public HQHRegister getHQHRegisterByIdCard(String idCard);

	public HQHBindCard getHQHBindCardByBankCard(String bankCard);

	public HQHRegister createHQHRegister(HQHRegister hqxhmRegister);

	public HQHBindCard createHQHBindCard(HQHBindCard hqxgmBindCard);

	public HQHBindCard getHQHBindCardbyMerchantOrder(String dsorderid);

	public NPRegister createNPRegister(NPRegister NPRegister);

	public NPRegister getNPRegisterbyIdcard(String idcard);

	public NPBindCard createNPBindCard(NPBindCard npBindCard);

	public NPBindCard getNPBindCardbyBankCard(String bankCard);

	String findKFTCityCodeByProvinceAndCityName(String cityName);

	// 畅捷新无卡还款注册表
	public CJHKLRRegister createRegister(CJHKLRRegister cjhk);

	// 获取畅捷新无卡还款注册表信息
	public CJHKLRRegister getRegister(String idCard);

	// 畅捷新无卡还款绑定信用卡表
	public CJHKLRBindCard createBindCard(CJHKLRBindCard cjhkbindcard);

	// 获取畅捷新无卡还款绑定信用卡表
	public CJHKLRBindCard getBindCard(String bankCard);

	// 和众落地快捷鉴权入网表
	public HZLRBindCard createBindCard(HZLRBindCard hzlrBindCard);

	// 和众落地快捷
	public HZLRBindCard getBindCardByBankCard(String bankCard);

	//钱嘉快捷本地进件
	QJRegister getQJRegisterByIdCard(String idCard);

	//钱嘉快捷本地绑卡
	QJBindCard getQJBindCardByBankCard(String bankCard);

	//钱嘉快捷本地绑卡
	QJBindCard createQJBindCard(QJBindCard bindCard);

	//钱嘉快捷本地进件
	QJRegister createQJRegister(QJRegister register);

	//信通入网
	public XTRegister createXTRegister(XTRegister xtRegister);
	//获取信通入网信息
	public XTRegister getXTRegisterByIdCard(String idCard);
	//信通查询绑卡
	public XTBindCard getXTBindCardByBankCard(String bankCard);
	//信通绑卡保存
	public XTBindCard createXTBindCard(XTBindCard xtBindCard);

	//保存信通订单
	public XTOrderCode createXTOrderCode(XTOrderCode xtOrderCode);
	//查询所有信通订单状态为0的订单号
	public List<String> findAllXTordercode();

	//信通成功订单更改状态
	public XTOrderCode changextstatus(String ordercode);

	HQNEWBindCard createHQNEWBindCard(HQNEWBindCard hqBindCard);


	HQNEWBindCard getHQNEWBindCardByBankCard(String bankCard);

	HQNEWBindCard getHQNEWBindCardByDsorderid(String dsorderid);

	HQNEWRegister createHQNEWRegister(HQNEWRegister hqRegister);

	HQNEWRegister getHQNEWRegisterByIdCard(String idCard);

	//环球省市地区编码
	public List<HQERegion> getHQERegionByParentName(String name);

   //环球小额获取register对象
    HQXRegister getHQXRegisterByIdCard(String idCard);
	//环球小额获取binkCard对象
	HQXBindCard getHQXBindCardByBankCard(String bankCard);

	HQXRegister createHQXRegister(HQXRegister hqxRegister);
    //保存hqxBindCard对象
	HQXBindCard createHQXBindCard(HQXBindCard hqxBindCard);

	//查找溢诚交易商户号
	public List<YCMerch> findYCMerch(String area);

	//查询溢城签约信息
	public YCACCount findYCAccountByIdCard(String idcard);

	//保存溢城签约信息
	public YCACCount createYCACCount(YCACCount ycacCount);

	//查询溢城绑卡信息
	public YCBindCard findYCBindCardByCardNo(String bankCard);


	//保存溢城绑卡信息
	public YCBindCard createYCBindcard(YCBindCard ycBindcard);

	//查收银宝的省
	public List<SYBAddress> findSYBAllprovice();


	//查收银宝的市
	public List<SYBAddress> findSYBarea(String province);

	//查询收银宝所有的商户
	public List<SYBMCC> findallmcc();

	//保存收银宝用户进件信息
	public SYBRegister createSYBRegister(SYBRegister sybRegister);

	//通过身份证查收银宝用户进件信息
	public SYBRegister findSYBRegisterbyIdcard(String idcard);

	//保存收银宝用户银行卡信息
	public SYBBindCard createSYBBindCard(SYBBindCard sybBindCard);

	//通过银行卡查询收银宝用户银行卡信息
	public SYBBindCard findSYBBindCardbybankcard(String bankcard);
	
	public HQXBindCard getHQXBindCardByOrderId(String orderId);

	//通过身份证查环迅进件信息
	HXDHXRegister getHXDHXRegisterByIdCard(String idCard);

	//通过银行卡号查环迅绑卡信息
	HXDHXBindCard getHXDHXBindCardByBankCard(String bankCard);

	//保存环迅用户信息
	HXDHXRegister createHXDHXRegister(HXDHXRegister hxdhxRegister);

	//保存环迅银行卡信息
	HXDHXBindCard createHXDHXBingCard(HXDHXBindCard hxdhxBindCard);

	XKBankType getXKBankTypeByBankName(String bankName);

	XKDHDRegister getXKDHDRegisterByIdCard(String idCard);

	XKDHDRegister createXKDHDRxegister(XKDHDRegister register);

	XKDHDBindCard createXKDHDBindCard(XKDHDBindCard bindCard);

	XKDHDBindCard getXKDHDBindCardByBankCard(String bankCard);

	List<XKArea> getXKAreaByParentId(String parentId);

	TLDHXRegister getTLDHXRegisterByIdCard(String idCard);

	TLDHXBindCard getTLDHXBindCardByBankCard(String bankCard);

	void createTLDHXBingCard(TLDHXBindCard tldhxBindCard);

	void createTLDHXRegister(TLDHXRegister tldhxRegister);

	YPLRegister getYPLRegisterByIdCard(String idCard);

	DBindCard getDBindCardByDebitCardNo(String debitCardNo);

	CBindCard getCBindCardByBankCard(String bankCard);

	YPLRegister createYPLRegister(YPLRegister register);

	DBindCard createDBindCard(DBindCard dBindCard);

	CBindCard createCBindCard(CBindCard cBindCard);

	List<YPLAddress> getYPLAddressByParentId(String parentId);

	List<YPLMCC> getYPLMCCByParent(String parent);


    List<Area> listAreaInfo(int id);

	CJHKLRChannelWhite getChannelWhite(String channelType, String status,String channelCode);

	CJHKLRChannelCodeRelation getCJHKLRChannelCodeRelation(String bankCard);

	CJHKLRChannelCodeRelation createCJHKLRChannelCodeRelation(CJHKLRChannelCodeRelation channelCodeRelation);
}
