package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.business.TopupPayChannelBusiness;
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
import com.jh.paymentgateway.repository.*;
import com.jh.paymentgateway.repository.hq.HQNEWBindCardRepository;
import com.jh.paymentgateway.repository.hq.HQNEWRegisterRepository;
import com.jh.paymentgateway.repository.hx.HXDHXBingCardRepositor;
import com.jh.paymentgateway.repository.hx.HXDHXRegisterRepository;
import com.jh.paymentgateway.repository.kft.*;
import com.jh.paymentgateway.repository.qj.QJBindCardRepository;
import com.jh.paymentgateway.repository.qj.QJRegisterRepository;
import com.jh.paymentgateway.repository.tl.TLDHXBingCardRepositor;
import com.jh.paymentgateway.repository.tl.TLDHXRegisterRepository;
import com.jh.paymentgateway.repository.xkdhd.XKAreaRepository;
import com.jh.paymentgateway.repository.xkdhd.XKBankTypeRepository;
import com.jh.paymentgateway.repository.xkdhd.XKDHDBindCardRepository;
import com.jh.paymentgateway.repository.xkdhd.XKDHDRegisterRepository;
import com.jh.paymentgateway.repository.ypl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class TopupPayChannelBusinessImpl implements TopupPayChannelBusiness {

	@Autowired
	private BankNumCodeRepository bankNumCodeRepository;

	@Autowired
	private WFRegisterRepository wfRegisterRepository;

	@Autowired
	private WFBindCardRepository wfBindCardRepository;

	@Autowired
	private JFRegisterRepository jfRegisterRepository;

	@Autowired
	private JFBindCardRepository jfBindCardRepository;

	@Autowired
	private HQRegisterRepository hqRegisterRepository;

	@Autowired
	private HQBindCardRepository hqBindCardRepository;

	@Autowired
	private HQGRegisterRepository hqgRegisterRepository;

	@Autowired
	private HQGBindCardRepository hqgBindCardRepository;

	@Autowired
	private HQGProvinceCityRepository hqgProvinceCityRepository;

	@Autowired
	private HQBBindCardRepository hQBBindCardRepository;

	@Autowired
	private RHJFRegisterRepository rhjfRegisterRepository;

	@Autowired
	private RHJFBindCardRepository rhjfBindCardRepository;

	@Autowired
	private BQRegisterRepository bQRegisterRepository;

	@Autowired
	private BQBankCardRepository bQBankCardRepository;

	@Autowired
	private KYRegisterRepository kyRegisterRepository;

	@Autowired
	private YFJRRegisterRepository yFJRRegisterRepository;

	@Autowired
	private CJHKLRChannelWriteRepository cjhklrChannelWriteRepository;

	@Autowired
	private CJHKLRChannelCodeRelationRepository cjhklrChannelCodeRelationRepository;

	@Autowired
	private YFJRBinkCardRepository yFJRBinkCardRepository;

	@Autowired
	private MCCRepository mccRepository;

	@Autowired
	private YTJFSignCardRepository ytjfSignCardRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private KYBindCardRepository kyBindCardRepository;

	@Autowired
	private CJRegisterRepository cjRegisterRepository;

	@Autowired
	private CJBindCardRepository cjbindCardRepository;

	@Autowired
	private LDRegisterRepository ldRegisterRepository;

	@Autowired
	private BQXRegisterRepository bqxRegisterRepository;

	@Autowired
	private BQXBindCardRepository bqxBindCardRepository;

	@Autowired
	private BqxCodeRepository bqxCodeRepository;

	@Autowired
	private BqxMerchantRepository bqxMerchantRepository;

	@Autowired
	private KQRegisterRepository kqRegisterRepository;

	@Autowired
	private KQBindCardRepository kqBindCardRepository;

	@Autowired
	private YHRegisterRepository yhRegisterRepository;

	@Autowired
	private YiBaoRepository yiBaoRepository;

	@Autowired
	private HQQuickRegisterRepository hqQuickRegisterRepository;

	@Autowired
	private KFTAddressRepository kftAddressRepository;

	@Autowired
	private SSBindCardRepository ssBindCardRepository;

	@Autowired
	private HQDHRegisterRepository hqdhRegisterRepository;

	@Autowired
	private JFXBindCardRepository jFXBindCardRepository;

	@Autowired
	private JFXRegisterRepository jFXRegisterRepository;

	@Autowired
	private XSRegisterRepository xSRegisterRepository;

	@Autowired
	private XSAccountRepository xSAccountRepository;

	@Autowired
	private XSBindCardRepository xSBindCardRepository;

	@Autowired
	private XSHKProvinceRepository xSHKProvinceRepository;

	@Autowired
	private CJHKRegisterRepository cjhkRegisterRepository;;

	@Autowired
	private CJQuickBindCardRepository cjQuickBindCardRepository;

	@Autowired
	private FFZCRegisterRepository ffzcRegisterRepository;

	@Autowired
	private GHTBindCardRepository ghtBindCardRepository;

	@Autowired
	private GHTCityMerchantRepository ghtCityMerchantRepository;

	@Autowired
	private GHTXwkCityMerchantRepository ghtXwkCityMerchantRepository;

	@Autowired
	private FFZCBindCardRepository ffzcBindCardRepository;

	@Autowired
	private CJXChannelCodeRepository cjxchannelcodeRepository;

	@Autowired
	private CJQuickBindCardRepository cjqBindCardRepository;

	@Autowired
	private TYTRegisterRepository tytRegisterRepository;

	@Autowired
	private YBQuickRegisterRepository ybQuickRegisterRepository;

	@Autowired
	private KBRegisterRepository kbRegisterRepository;

	@Autowired
	private KBBindCardRepository kbBindCardRepository;

	@Autowired
	private KBErrorDescRepository kbErrorDescRepository;

	@Autowired
	private ChannelSupportDebitBankCardRepository channelSupportDebitBankCardRepository;

	@Autowired
	private HQEBindCardRepository hqeBindCardRepository;

	@Autowired
	private YBSRepository ybsRepository;

	@Autowired
	private JFSRegisterRepository jfsRegisterRepository;

	@Autowired
	private JFSBindCardRepository jfsBindCardRepository;

	@Autowired
	private HQERegionRepository hqeRegionRepository;

	@Autowired
	private HZHKBindCardRepository hzhkBindCardRepository;

	@Autowired
	private HZHKRegisterRepository hzhkRegisterRepository;

	@Autowired
	private HZHKOrderRepository hzhkOrderRepository;

	@Autowired
	private HZHKCodeRepository hzhkCodeRepository;

	@Autowired
	private RYTBindCardRepository rytBindCardRepository;

	@Autowired
	private RYTRegisterRepository rytRegisterRepository;

	@Autowired
	private RYTProvinceCityRepository rytProvinceCityRepository;

	@Autowired
	private LMRegisterRepository lmRegisterRepository;

	@Autowired
	private LMBankNumRepository lmBankNumRepository;

	@Autowired
	private LMTRegisterRepository lmtRegisterRepository;

	@Autowired
	private LMTProvinceRepository lmtProvinceRepository;

	@Autowired
	private CJHKChooseCityRepository cjhkChooseCityRepository;

	@Autowired
	private LMDHRegisterRepository lmdhRegisterRepository;

	@Autowired
	private LMDHBindCardRepository lmdhBindCardRepository;

	@Autowired
	private HZDHAddressRepository hzdhAddressRepository;

	@Autowired
	private NPRegisterRepository npRegisterRepository;

	@Autowired
	private NPBindCardRepository npBindCardRepository;

	@Autowired
	private KFTRegisterRepository kftRegisterRepository;

	@Autowired
	private KFTBindCardRepository kftBindCardRepository;

	@Autowired
	private KFTProtocolBindCardRepository kftProtocolBindCardRepository;

	@Autowired
	private KFTVerifyCardRepository kftVerifyCardRepository;

	@Autowired
	private KFTOrderRepository kftOrderRepository;

	@Autowired
	private KFTBankCodeRepository kftBankCodeRepository;

	@Autowired
	private CJHKLRBindCardRepository cjhklrBindCardRepository;

	@Autowired
	private CJHKLRRegisterRepository cjhklrRegisterRepository;

	@Autowired
	private HZLRBindCardRepository hzlrBindCardRepository;

	@Autowired
	private QJRegisterRepository qjRegisterRepository;

	@Autowired
	private QJBindCardRepository qjBindCardRepository;

	@Autowired
	private XTRegisterRepository xtRegisterRepository;

	@Autowired
	private XTBindCardRepository xtBindCardRepository;

	@Autowired
	private XTOrderCodeRepository xtOrderCodeRepository;

	@Autowired
	private HQNEWBindCardRepository hqnewBindCardRepository;

	@Autowired
	private HQNEWRegisterRepository hqnewRegisterRepository;

	@Autowired
	private HQXRegisterRepository hqxRegisterRepository;

	@Autowired
	private HQXBindCardRepository hqxBindCardRepository;

	@Autowired
	private YCMerchRepository ycMerchRepository;

	@Autowired
	private YCAccountRepository ucaccountRepository;

	@Autowired
	private YCBindCardRepository ycBindCardRepository;

	@Autowired
	private SYBAddressRepository sybAddressRepository;

	@Autowired
	private SYBMCCRepository sybmccRepository;

	@Autowired
	private SYBRegisterRepository sybRegisterRepository;

	@Autowired
	private SYBBindCardRepository sybBindCardRepository;

	@Autowired
	private HXDHXBingCardRepositor hxdhxBingCardRepositor;

	@Autowired
	private HXDHXRegisterRepository hxdhxRegisterRepository;

	@Autowired
	private XKDHDRegisterRepository xkdhdRegisterRepository;

	@Autowired
	private XKDHDBindCardRepository xkdhdBindCardRepository;

	@Autowired
	private XKBankTypeRepository xkBankTypeRepository;

	@Autowired
	private XKAreaRepository xkAreaRepository;

	@Autowired
	private TLDHXRegisterRepository tldhxRegisterRepository;

	@Autowired
	private TLDHXBingCardRepositor tldhxBingCardRepositor;

	@Autowired
	private YPLRegisterRepository yplRegisterRepository;

	@Autowired
	private CBindCardRepository cBindCardRepository;

	@Autowired
	private DBindCardRepository dBindCardRepository;

	@Autowired
	private YPLAddressRepository yplAddressRepository;

	@Autowired
	private YPLMCCRepository yplmccRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Override
	@Transactional
	public KFTBindCard saveKftBindCard(KFTBindCard card) {
		KFTBindCard kftBindCard = kftBindCardRepository.save(card);
		em.flush();
		return kftBindCard;
	}

	@Override
	public KFTBindCard findKftBindCardByBankCard(String bankCard) {
		em.clear();
		KFTBindCard kftBindCardByBankCard = kftBindCardRepository.getKftBindCardByBankCard(bankCard);
		return kftBindCardByBankCard;
	}

	@Override
	public BankNumCode getBankNumCodeByBankName(String bankName) {
		return bankNumCodeRepository.getBankNumCodeByBankName(bankName);
	}

	@Transactional
	@Override
	public WFRegister createWFRegister(WFRegister wfRegister) {
		WFRegister result = wfRegisterRepository.save(wfRegister);
		em.flush();
		return result;
	}

	@Override
	public WFRegister getWFRegisterByIdCard(String idCard) {
		em.clear();
		WFRegister result = wfRegisterRepository.getWFRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public WFBindCard createWFBindCard(WFBindCard wfBindCard) {
		WFBindCard result = wfBindCardRepository.save(wfBindCard);
		em.flush();
		return result;
	}

	@Override
	public WFBindCard getWFBindCardByBankCard(String bankCard) {
		em.clear();
		WFBindCard result = wfBindCardRepository.getWFBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public JFRegister createJFRegister(JFRegister jfRegister) {

		JFRegister result = jfRegisterRepository.save(jfRegister);
		em.flush();
		return result;
	}

	@Override
	public JFRegister getJFRegisterByIdCard(String idCard) {
		return jfRegisterRepository.getJFRegisterByIdCard(idCard);
	}

	@Transactional
	@Override
	public JFBindCard createJFBindCard(JFBindCard jfBindCard) {
		JFBindCard result = jfBindCardRepository.save(jfBindCard);
		em.flush();
		return result;
	}

//    @Override
//    public JFBindCard findJFBindCard(String bankcard) {
//	    em.clear();
//        JFBindCard result=jfBindCardRepository.
//        return null;
//    }

    @Override
	public JFBindCard getJFBindCardByBankCard(String bankCard) {
		return jfBindCardRepository.getJFBindCardByBankCard(bankCard);
	}

	@Transactional
	@Override
	public HQRegister createHQRegister(HQRegister hqRegister) {
		HQRegister result = hqRegisterRepository.save(hqRegister);
		em.flush();
		return result;
	}

	@Override
	public HQRegister getHQRegisterByIdCard(String idCard) {
		em.clear();
		HQRegister result = hqRegisterRepository.getHQRegisterByIdCard(idCard);
		return result;
	}

	@Override
	public HQRegister getHQRegisterByMerchantOrder(String merchantOrder) {
		em.clear();
		HQRegister result = hqRegisterRepository.getHQRegisterByMerchantOrder(merchantOrder);
		return result;
	}

	@Transactional
	@Override
	public HQBindCard createHQBindCard(HQBindCard hqBindCard) {
		HQBindCard result = hqBindCardRepository.save(hqBindCard);
		em.flush();
		return result;
	}

	@Override
	public HQBindCard getHQBindCardByBankCard(String bankCard) {
		em.clear();
		HQBindCard result = hqBindCardRepository.getHQBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public RHJFBindCard createRHJFBindCard(RHJFBindCard rhjfBindCard) {
		RHJFBindCard result = rhjfBindCardRepository.save(rhjfBindCard);
		em.flush();
		return result;
	}

	@Override
	public RHJFBindCard getRHJFBindCardByBankCard(String bankCard, String status) {
		em.clear();
		RHJFBindCard result = rhjfBindCardRepository.getRHJFBindCardByBankCard(bankCard, status);
		return result;
	}

	@Transactional
	@Override
	public RHJFRegister createRHJFRegister(RHJFRegister rhjfRegister) {
		RHJFRegister result = rhjfRegisterRepository.save(rhjfRegister);
		em.flush();
		return result;
	}

	@Override
	public RHJFRegister getRHJFRegisterByIdCard(String idCard) {
		em.clear();
		RHJFRegister result = rhjfRegisterRepository.getRHJFRegisterByIdCard(idCard);
		return result;
	}

	@Override
	public RHJFRegister getRHJFRegisterByMerchantNo(String merchantNo) {
		em.clear();
		RHJFRegister result = rhjfRegisterRepository.getRHJFRegisterByMerchantNo(merchantNo);
		return result;
	}

	@Override
	public BQRegister getBQRegisterByIdNum(String idNum) {
		em.clear();
		BQRegister result = bQRegisterRepository.getBQRegisterByIdCard(idNum);
		return result;
	}

	@Transactional
	@Override
	public BQRegister createBQRegister(BQRegister bQRegister) {
		BQRegister result = bQRegisterRepository.save(bQRegister);
		em.flush();
		return result;
	}

	@Transactional
	@Override
	public BQBankCard createBQBankCard(BQBankCard bQBankCard) {
		BQBankCard result = bQBankCardRepository.save(bQBankCard);
		em.flush();
		return result;
	}

	@Override
	public BQBankCard getBQBankCardByIdNum(String idNum, String acct_no) {
		em.clear();
		BQBankCard result = bQBankCardRepository.getBQRegisterByIdCard(idNum, acct_no);
		return result;
	}

	@Override
	public BQBankCard getBQBankCardByIdNumSure(String idNum, String acct_no) {
		em.clear();
		BQBankCard result = bQBankCardRepository.getBQBankCardByIdNumSure(idNum, acct_no);
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
		KYRegister result = kyRegisterRepository.getKYRegisterByIdCard(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public KYBindCard createKYBindCard(KYBindCard kyBindCard) {
		KYBindCard result = kyBindCardRepository.save(kyBindCard);
		em.flush();
		return result;
	}

	@Override
	public KYBindCard getKYBindCardByBankCard(String bankCard) {
		KYBindCard result = kyBindCardRepository.getKYBindCardBybankCard(bankCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public YFJRRegister createYFJRRegister(YFJRRegister yFJRRegister) {
		YFJRRegister result = yFJRRegisterRepository.save(yFJRRegister);
		em.flush();
		return result;
	}

	@Override
	public YFJRRegister getYFJRRegisterByIdNum(String idCard) {
		em.clear();
		YFJRRegister result = yFJRRegisterRepository.getYFJRRegisterByIdNum(idCard);
		return result;
	}

	@Transactional
	@Override
	public YFJRBinkCard createYFJRBinkCard(YFJRBinkCard yFJRBinkCard) {
		YFJRBinkCard result = yFJRBinkCardRepository.save(yFJRBinkCard);
		em.flush();
		return result;
	}

	@Override
	public YFJRBinkCard getYFJRBinkCardByIdNum(String idCard) {
		em.clear();
		YFJRBinkCard result = yFJRBinkCardRepository.getYFJRBinkCardByIdNum(idCard);
		return result;
	}

	@Override
	public YFJRBinkCard getYFJRBinkCardByAppOrderId(String appOrderId) {
		em.clear();
		YFJRBinkCard result = yFJRBinkCardRepository.getYFJRBinkCardByAppOrderId(appOrderId);
		return result;
	}

	@Override
	public YFJRBinkCard getYFJRBinkCardByIdNum(String idCard, String bankCard, String status) {
		return null;
	}

	@Transactional
	@Override
	public HQBBindCard createHQBBindCard(HQBBindCard hqbBindCard) {
		HQBBindCard result = hQBBindCardRepository.save(hqbBindCard);
		em.flush();
		return result;
	}

	@Override
	public HQBBindCard getHQBBindCardByBankCard(String bankCard) {
		em.clear();
		HQBBindCard result = hQBBindCardRepository.getHQBBindCardByBankCard(bankCard);
		return result;
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

	@Transactional
	@Override
	public CJRegister createCJRegister(CJRegister cjRegister) {
		CJRegister result = cjRegisterRepository.save(cjRegister);
		em.flush();
		return result;
	}

	@Override
	public CJRegister getCJRegisterByIdCard(String idCard) {
		em.clear();
		CJRegister result = cjRegisterRepository.getCJRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public CJBindCard createCJBindCard(CJBindCard cjBindCard) {
		CJBindCard result = cjbindCardRepository.save(cjBindCard);
		em.flush();
		return result;
	}

	@Override
	public CJBindCard getCJBindCardByBankCard(String bankCard) {
		CJBindCard result = cjbindCardRepository.getCJBindCardByBankCard(bankCard);
		return result;
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
		em.clear();
		LDRegister result = ldRegisterRepository.getLDRegisterByIdCard(idCard);
		return result;
	}

	@Override
	public String getLDRegisterByPhone(String phone) {
		em.clear();
		String result = ldRegisterRepository.getLDRegisterByPhone(phone);
		return result;

	}

	@Override
	public YTJFSignCard getYTJFSignCardByIdCard(String idCard) {
		em.clear();
		return ytjfSignCardRepository.getYTJFSignCardByIdCard(idCard);
	}

	@Override
	public YTJFSignCard getYTJFSignCardByBankCard(String bankCard) {
		em.clear();
		return ytjfSignCardRepository.getYTJFSignCardByBankCard(bankCard);
	}

	@Override
	@Transactional
	public KFTRegister createKFTRegister(KFTRegister register) {
		KFTRegister result = kftRegisterRepository.save(register);
		em.flush();
		return result;
	}

	@Override
	public List<String> findKftBankCodeByName(String name) {
		em.clear();
		List<String> kftBankCode = kftBankCodeRepository.getKFTBankCodeByName(name);
		return kftBankCode;
	}

	@Transactional
	@Override
	public YTJFSignCard createYTJFSignCard(YTJFSignCard ytjfSignCard) {

		return ytjfSignCardRepository.save(ytjfSignCard);
	}

	@Transactional
	@Override
	public BQXRegister createBQXRegister(BQXRegister bqxRegister) {
		BQXRegister result = bqxRegisterRepository.save(bqxRegister);
		em.flush();
		return result;
	}

	@Override
	public BQXRegister getBQXRegisterByIdCard(String idCard) {
		em.clear();
		BQXRegister result = bqxRegisterRepository.getBQXRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public BQXBindCard createBQXBindCard(BQXBindCard bqxBindCard) {
		BQXBindCard result = bqxBindCardRepository.save(bqxBindCard);
		em.flush();
		return result;
	}

	@Override
	public BQXBindCard getBQXBindCardByBankCard(String bankCard) {
		em.clear();
		BQXBindCard result = bqxBindCardRepository.getBQXBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public List<BqxCode> findBqxCodeProvince() {
		em.clear();
		List<BqxCode> result = bqxCodeRepository.getbqxCodeRepository();
		return result;
	}

	@Override
	public List<BqxCode> findBqxCodeCity(String provinceId) {
		em.clear();
		List<BqxCode> result = bqxCodeRepository.findBqxCodeCity(provinceId);
		return result;
	}

	@Override
	public List<BqxMerchant> findBqxMerchant() {
		em.clear();
		List<BqxMerchant> result = bqxMerchantRepository.findBqxMerchant();
		return result;
	}

	@Transactional
	@Override
	public HQQuickRegister createHQQuickRegister(HQQuickRegister hqQuickRegister) {
		HQQuickRegister result = hqQuickRegisterRepository.save(hqQuickRegister);
		em.flush();
		return result;
	}

	@Override
	public HQQuickRegister getHQQuickRegisterByIdCard(String idCard) {
		em.clear();
		HQQuickRegister result = hqQuickRegisterRepository.getHQQuickRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public HQDHRegister createHQDHRegister(HQDHRegister hqdhRegister) {
		HQDHRegister result = hqdhRegisterRepository.save(hqdhRegister);
		em.flush();
		return result;
	}

	@Override
	public HQDHRegister getHQDHRegisterByIdCard(String idCard) {
		em.clear();
		HQDHRegister result = hqdhRegisterRepository.getHQDHRegisterrByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public SSBindCard createSSBindCard(SSBindCard ssBindCard) {
		SSBindCard result = ssBindCardRepository.save(ssBindCard);
		em.flush();
		return result;
	}

	@Override
	public SSBindCard getSSBindCardByBankCard(String bankCard) {
		em.clear();
		SSBindCard result = ssBindCardRepository.getSSBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public SSBindCard getSSBindCardByBindId(String bindId) {
		em.clear();
		SSBindCard result = ssBindCardRepository.getSSBindCardByBindId(bindId);
		return result;
	}

	@Override
	public XSRegister getXSRegisterByIdCard(String idCard) {
		return xSRegisterRepository.findByIdCard(idCard);
	}

	@Override
	public XSRegister saveXSRegister(XSRegister xSRegister) {
		return xSRegisterRepository.saveAndFlush(xSRegister);
	}

	@Override
	public XSAccount saveXSAccount(XSAccount xsAccount) {
		return xSAccountRepository.saveAndFlush(xsAccount);
	}

	@Override
	public XSBindCard saveXSBindCard(XSBindCard xsBindCard) {
		return xSBindCardRepository.saveAndFlush(xsBindCard);
	}

	@Override
	public XSAccount findXSAccountByIdCard(String idcard) {
		return xSAccountRepository.findByIdCard(idcard);
	}

	@Override
	public XSBindCard findByXSBindCardByCardNo(String bankCard) {
		return xSBindCardRepository.findByCardNo(bankCard);
	}

	@Override
	public List<XSHKProvince> findXSHKProvince() {
		return xSHKProvinceRepository.findXSHKProvince();
	}

	@Override
	public List<XSHKProvince> findXSHKProvinceByProvince(String province) {
		return xSHKProvinceRepository.findByProvince(province);
	}

	@Override
	public JFXRegister getJFXRegisterByIdCard(String idCard) {
		em.clear();
		JFXRegister result = jFXRegisterRepository.getJFXRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public JFXRegister createJFXRegister(JFXRegister jfxRegister) {
		JFXRegister result = jFXRegisterRepository.save(jfxRegister);
		em.flush();
		return result;
	}

	@Override
	public JFXBindCard getJFXBindCardByBankCard(String bankCard) {
		em.clear();
		JFXBindCard result = jFXBindCardRepository.getJFXBindCardByBankCard(bankCard);
		return result;
	}

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

	@Transactional
	@Override
	public CJHKRegister createCJHKRegister(CJHKRegister cjhkRegister) {
		CJHKRegister result = cjhkRegisterRepository.save(cjhkRegister);
		em.flush();
		return result;
	}

	@Override
	public CJHKRegister getCJHKRegisterByIdCard(String idCard) {
		em.clear();
		CJHKRegister result = cjhkRegisterRepository.getCJHKRegisterByIdCard(idCard);
		return result;
	}

	@Override
	public CJXChannelCode getCJXChannelCode(String bankName) {
		em.clear();
		CJXChannelCode result = cjxchannelcodeRepository.getCJXChannelCode(bankName);
		return result;
	}

	@Transactional
	@Override
	public JFXBindCard createJFXBindCard(JFXBindCard jFXBindCard) {
		JFXBindCard result = jFXBindCardRepository.save(jFXBindCard);
		em.flush();
		return result;
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
	public List<String> getGHTCityMerchantProvince() {
		em.clear();
		List<String> result = ghtCityMerchantRepository.getGHTCityMerchantProvince();
		return result;
	}

	@Override
	public List<String> getGHTCityMerchantCityByProvince(String province) {
		em.clear();
		List<String> result = ghtCityMerchantRepository.getGHTCityMerchantCityByProvince(province);
		return result;
	}

	@Override
	public List<GHTCityMerchant> getGHTCityMerchantByProvinceAndCity(String province, String city) {
		em.clear();
		List<GHTCityMerchant> result = ghtCityMerchantRepository.getGHTCityMerchantByProvinceAndCity(province, city);
		return result;
	}

	@Override
	public List<String> getGHTCityMerchantCodeByProvinceAndCity(String province, String city) {
		em.clear();
		List<String> result = ghtCityMerchantRepository.getGHTCityMerchantCodeByProvinceAndCity(province, city);
		return result;
	}

	@Override
	public List<GHTXwkCityMerchant> getGHTXwkCityMerchantByProvinceAndCity(String province, String city) {
		em.clear();
		List<GHTXwkCityMerchant> result = ghtXwkCityMerchantRepository.getGHTXwkCityMerchantByProvinceAndCity(province,
				city);
		return result;
	}

	@Override
	public List<String> getGHTXwkCityMerchantCodeByProvinceAndCity(String province, String city) {
		em.clear();
		List<String> result = ghtXwkCityMerchantRepository.getGHTXwkCityMerchantCodeByProvinceAndCity(province, city);
		return result;
	}

	@Transactional
	@Override
	public KQRegister createKQRegister(KQRegister kqRegister) {
		KQRegister result = kqRegisterRepository.save(kqRegister);
		em.flush();
		return result;
	}

	@Override
	public KQRegister getKQRegisterByIdCard(String idCard) {
		em.clear();
		KQRegister result = kqRegisterRepository.getKQRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public KQBindCard createKQBindCard(KQBindCard kqBindCard) {
		KQBindCard result = kqBindCardRepository.save(kqBindCard);
		em.flush();
		return result;
	}

	@Override
	public KQBindCard getKQBindCardByBankCard(String bankCard) {
		em.clear();
		KQBindCard result = kqBindCardRepository.getKQBindCardByBankCard(bankCard);
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
	public YiBao createYiBao(YiBao yibao) {
		YiBao result = yiBaoRepository.save(yibao);
		em.flush();
		return result;
	}

	@Override
	public YiBao getYiBaoBymemberNo(String memberNo) {
		em.clear();
		YiBao result = yiBaoRepository.getYiBaoBymemberNo(memberNo);
		return result;
	}

	@Override
	public FFZCRegister getFFZCRegisterByIdCard(String idCard) {
		em.clear();
		FFZCRegister result = ffzcRegisterRepository.getFFZCRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public FFZCRegister createFFZCRegister(FFZCRegister fFZCRegister) {
		FFZCRegister result = ffzcRegisterRepository.save(fFZCRegister);
		em.flush();
		return result;
	}

	@Override
	public FFZCBindCard createFFZCBindCard(FFZCBindCard ffzcBindCard) {
		FFZCBindCard result = ffzcBindCardRepository.save(ffzcBindCard);
		em.flush();
		return result;
	}

	@Override
	public FFZCBindCard getFFZCBindCardByBankCard(String bankCard) {
		em.clear();
		FFZCBindCard result = ffzcBindCardRepository.getFFZCBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public List<CJQuickBindCard> findCJQuickBindCardByIdCard(String idCard) {
		em.clear();
		List<CJQuickBindCard> result = cjqBindCardRepository.findByIdCard(idCard);
		return result;
	}

	@Override
	public TYTRegister getTYTRegisterByIdCard(String idCard) {
		em.clear();
		TYTRegister result = tytRegisterRepository.getTYTRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public TYTRegister createTYTRegister(TYTRegister tytRegister) {
		TYTRegister result = tytRegisterRepository.save(tytRegister);
		em.flush();
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
	public YBQuickRegister getYBQuickRegisterByidCard(String idCard) {
		YBQuickRegister result = ybQuickRegisterRepository.getYBQuickRegister(idCard);
		em.clear();
		return result;
	}

	@Override
	public YBQuickRegister getYBQuickRegisterByPhone(String phone) {

		return ybQuickRegisterRepository.getYBQuickRegisterByPhone(phone);
	}

	@Transactional
	@Override
	public void createKBRegister(KBRegister kbRegister) {
		kbRegisterRepository.save(kbRegister);
		em.flush();
	}

	@Override
	public KBRegister getKBRegisterByIdCard(String idCard) {
		em.clear();
		KBRegister result = kbRegisterRepository.getKBRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public void createKBBindCard(KBBindCard kbBindCard) {
		kbBindCardRepository.save(kbBindCard);
		em.flush();
	}

	@Override
	public KBBindCard getKBBindCardByBankCard(String bankCard) {
		em.clear();
		KBBindCard result = kbBindCardRepository.getKBBindCardByIdCard(bankCard);
		return result;
	}

	@Override
	public String getKBErrorDescByErrorCode(String errorCode) {
		em.clear();
		String result = kbErrorDescRepository.getKBErrorDescByErrorCode(errorCode);
		return result;
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

	@Transactional
	@Override
	public void createHQEBindCard(HQEBindCard hqeBindCard) {
		hqeBindCardRepository.save(hqeBindCard);
		em.flush();
	}

	@Override
	public HQEBindCard getHQEBindCardByBankCard(String bankCard) {
		em.clear();
		HQEBindCard result = hqeBindCardRepository.getHQEBindCardByBankCard(bankCard);
		return result;
	}

	@Override
	public HQEBindCard getHQEBindCardByOrderCode(String orderCode) {
		em.clear();
		HQEBindCard result = hqeBindCardRepository.getHQEBindCardByOrderCode(orderCode);
		return result;
	}

	@Override
	public List<HQERegion> getHQERegionByParentId(String parentId) {
		em.clear();
		List<HQERegion> result = hqeRegionRepository.getHQERegionByParentId(parentId);
		return result;
	}

	@Override
	public YBQuickRegister getYBQuickRegisterByIdCard(String idCard) {
		YBQuickRegister result = ybQuickRegisterRepository.getYBQuickRegister(idCard);
		em.clear();
		return result;
	}

	@Transactional
	@Override
	public YBSRegister createYBSRegister(YBSRegister ybsRegister) {
		YBSRegister result = ybsRepository.save(ybsRegister);
		em.flush();
		return result;
	}

	@Override
	public YBSRegister getYBSRegisterByidCard(String idCard) {
		em.clear();
		YBSRegister result = ybsRepository.getYBSRegisterByidCard(idCard);
		return result;
	}

	@Override
	public JFSRegister getJFSRegisterByIdCard(String idCard) {
		em.clear();
		JFSRegister result = jfsRegisterRepository.getJFSRegisterByIdCard(idCard);
		return result;
	}

	@Transactional
	@Override
	public JFSRegister createJFSRegister(JFSRegister jfsRegister) {
		JFSRegister result = jfsRegisterRepository.save(jfsRegister);
		em.flush();
		return result;
	}

	@Override
	public JFSBindCard getJFSBindCardByBankCard(String bankCard) {
		em.clear();
		JFSBindCard result = jfsBindCardRepository.getJFSBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public JFSBindCard createJFSBindCard(JFSBindCard jfsBindCard) {
		JFSBindCard result = jfsBindCardRepository.save(jfsBindCard);
		em.flush();
		return result;
	}

	@Override
	public HZHKRegister getHZHKRegisterByidCard(String idCard) {
		em.clear();
		HZHKRegister result = hzhkRegisterRepository.getHZHKRegisterByidCard(idCard);
		return result;
	}

	@Override
	public HZHKOrder getHZHKOrderByorderCode(String orderCode) {
		em.clear();
		HZHKOrder result = hzhkOrderRepository.getHZHKOrderByorderCode(orderCode);
		return result;
	}

	@Transactional
	@Override
	public HZHKRegister createHZHKRegister(HZHKRegister hzhkRegister) {
		HZHKRegister result = hzhkRegisterRepository.save(hzhkRegister);
		em.flush();
		return result;
	}

	@Transactional
	@Override
	public HZHKOrder createHZHKOrder(HZHKOrder hzhkOrder) {
		HZHKOrder result = hzhkOrderRepository.save(hzhkOrder);
		em.flush();
		return result;
	}

	@Override
	public HZHKBindCard getHZHKBindCardByBankCard(String bankCard) {
		em.clear();
		HZHKBindCard result = hzhkBindCardRepository.getHZHKBindCardByBankCard(bankCard);
		return result;
	}

	@Transactional
	@Override
	public HZHKBindCard createHZHKBindCard(HZHKBindCard hzhkBindCard) {
		HZHKBindCard result = hzhkBindCardRepository.save(hzhkBindCard);
		em.flush();
		return result;
	}

	@Override
	public HQBBindCard getHQBBindCardByUserId(String userId) {
		em.clear();
		HQBBindCard result = hQBBindCardRepository.getHQBBindCardByUserId(userId);
		return result;
	}

	@Override
	public List<HZHKCode> findHZHKCodeProvince() {
		em.clear();
		List<HZHKCode> result = hzhkCodeRepository.gethzhkCodeRepository();
		return result;
	}

	@Override
	public List<HZHKCode> findHZHKCodeCity(String provinceId) {
		em.clear();
		List<HZHKCode> result = hzhkCodeRepository.findhzhkCodeCity(provinceId);
		return result;
	}

	@Override
	public RYTBindCard getRYTBindCardByBankCard(String bankCard) {
		RYTBindCard rytBindCard1 = rytBindCardRepository.getRYTBindCardByBankCard(bankCard);
		return rytBindCard1;

	}

	@Transactional
	@Override
	public RYTBindCard createRYTBindCard(RYTBindCard rytBindCard) {
		RYTBindCard rytBindCard1 = rytBindCardRepository.save(rytBindCard);
		return rytBindCard1;
	}

	@Override
	public RYTRegister getRYTRegisterByIdcard(String idcard) {
		RYTRegister rytRegister1 = rytRegisterRepository.getRYTRegisterByIdCard(idcard);
		return rytRegister1;
	}

	@Transactional
	@Override
	public RYTRegister createRYTRegister(RYTRegister rytRegister) {
		RYTRegister rytRegister1 = rytRegisterRepository.save(rytRegister);
		return rytRegister1;

	}

	@Override
	public RYTProvinceCity getRYTProvinceCityByNumber(String number) {

		return rytProvinceCityRepository.getRYTProvinceCityByNumber(number);

	}

	@Override
	public List<RYTProvinceCity> getRYTProvinceCityByprovince() {

		return rytProvinceCityRepository.getRYTProvinceCityByprovince();

	}

	@Override
	public List<RYTProvinceCity> getRYTProvinceCityGroupByCity(String province) {

		return rytProvinceCityRepository.getRYTProvinceCityGroupByCity(province);

	}

	@Override
	public LMRegister getLMRegisterByidCard(String idCard) {
		em.clear();
		return lmRegisterRepository.getLMRegisterByIdCard(idCard);
	}

	@Transactional
	@Override
	public LMRegister createLMRegister(LMRegister lm) {
		return lmRegisterRepository.save(lm);
	}

	@Override
	public LMBankNum getLMBankNumCodeByBankName(String bankName) {
		em.clear();
		LMBankNum result = lmBankNumRepository.getLMBankNumCodeByBankName(bankName);
		return result;
	}

	@Transactional
	@Override
	public HQGRegister createHQGRegister(HQGRegister hqRegister) {
		HQGRegister hqgRegister = hqgRegisterRepository.save(hqRegister);
		em.flush();
		return hqgRegister;
	}

	@Override
	public HQGRegister getHQGRegisterByIdCard(String idCard) {
		em.clear();
		return hqgRegisterRepository.getHQGRegisterByIdCard(idCard);
	}

	@Override
	public HQGRegister getHQGRegisterByMerchantOrder(String merchantOrder) {
		em.clear();
		return hqgRegisterRepository.getHQGRegisterByMerchantOrder(merchantOrder);
	}

	@Transactional
	@Override
	public HQGBindCard createHQGBindCard(HQGBindCard hqBindCard) {
		HQGBindCard hqgBindCard = hqgBindCardRepository.save(hqBindCard);
		em.flush();
		return hqgBindCard;
	}

	@Override
	public HQGBindCard getHQGBindCardbyMerchantOrder(String merchantOrder) {
		em.clear();
		return hqgBindCardRepository.getHQGBindCardbyMerchantOrder(merchantOrder);
	}

	@Override
	public HQGBindCard getHQGBindCardByBankCard(String bankCard) {
		em.clear();
		return hqgBindCardRepository.getHQGBindCardByBankCard(bankCard);
	}

	public List<HQGProvinceCity> getHQGProvinceCityByHkProvinceCode() {

		return hqgProvinceCityRepository.getHQGProvinceCityByHkProvinceCode();
	}

	public List<HQGProvinceCity> getHQGProvinceCityGroupByCity(String cityCode) {

		return hqgProvinceCityRepository.getHQGProvinceCityGroupByCity(cityCode);
	}

	@Override
	public List<CJHKFactory> getCJHKChooseCityIPBycityName(String name) {
		em.clear();
		return cjhkChooseCityRepository.CJHKChooseCityByName(name);
	}

	@Override
	public LMTRegister getlmtRegisterByidCard(String idCard) {
		em.clear();
		return lmtRegisterRepository.getLMTRegisterByIdCard(idCard);
	}

	@Override
	public LMTRegister createlmtRegister(LMTRegister lmt) {
		return lmtRegisterRepository.save(lmt);
	}

	@Override
	public List<LMTAddress> findLMTProvince() {
		return lmtProvinceRepository.findLMTProvince();
	}

	@Override
	public List<LMTAddress> findLMTCityByProvinceId(String provinceId) {
		return lmtProvinceRepository.findLMTCityByProvinceId(provinceId);
	}

	@Override
	public List<LMTAddress> findLMTCityByCityId(String cityId) {
		// TODO Auto-generated method stub
		return lmtProvinceRepository.findLMTCityByCityId(cityId);
	}

	@Override
	public LMTAddress getLMTProvinceCode(Long id) {
		// TODO Auto-generated method stub
		return lmtProvinceRepository.getLMTProvinceCode(id);
	}

	@Override
	public GHTXwkCityMerchant getGHTXwkCityMerchantByMerchantCode(String merchantCode) {
		em.clear();
		GHTXwkCityMerchant result = ghtXwkCityMerchantRepository.getGHTXwkCityMerchantByMerchantCode(merchantCode);
		return result;
	}

	@Override
	public GHTXwkCityMerchant getGHTXwkCityMerchantByMerchantName(String merchantName) {
		em.clear();
		GHTXwkCityMerchant result = ghtXwkCityMerchantRepository.getGHTXwkCityMerchantByMerchantName(merchantName);
		return result;
	}

	@Override
	public GHTCityMerchant getGHTCityMerchantByMerchantCode(String merchantCode) {
		em.clear();
		GHTCityMerchant result = ghtCityMerchantRepository.getGHTCityMerchantByMerchantCode(merchantCode);
		return result;
	}

	@Override
	public GHTCityMerchant getGHTCityMerchantByMerchantName(String merchantName) {
		em.clear();
		GHTCityMerchant result = ghtCityMerchantRepository.getGHTCityMerchantByMerchantName(merchantName);
		return result;
	}

	@Override
	public LMDHRegister getlmdhRegisterByidCard(String idCard) {
		em.clear();
		return lmdhRegisterRepository.getlmdhRegisterByidCard(idCard);
	}

	@Override
	public LMDHRegister createlmdhRegister(LMDHRegister lmdh) {
		return lmdhRegisterRepository.save(lmdh);
	}

	@Override
	public LMDHBindCard getLMDHBindCardByBankCard(String bankCard) {
		LMDHBindCard lmdhBindCard = lmdhBindCardRepository.getLMDHBindCardByBankCard(bankCard);
		return lmdhBindCard;
	}

	@Override
	public LMDHBindCard createLMDHBindCard(LMDHBindCard lmdhbindCard) {
		return lmdhBindCardRepository.save(lmdhbindCard);
	}

	@Override
	public LMTAddress getLMTProvinceCode(String provinceOfBank) {
		return lmtProvinceRepository.getLMTProvinceCode(provinceOfBank);
	}

	@Override
	public LMTAddress getLMTProvinceCode(String provinceOfBank, String ciCode) {
		return lmtProvinceRepository.getLMTProvinceCode(provinceOfBank, ciCode);
	}

	@Override
	public HZDHAddress getHZDHXAddress(Long areaId) {
		// TODO Auto-generated method stub
		return hzdhAddressRepository.getHZDHXAddress(areaId);
	}

	@Override
	public List<HZDHAddress> findHZDHProvince() {
		return hzdhAddressRepository.findHZDHProvince();
	}

	@Override
	public List<HZDHAddress> findHZDHMerchant(String provinceName) {
		return hzdhAddressRepository.findHZDHMerchant(provinceName);
	}

	@Override
	public HQHRegister getHQHRegisterByIdCard(String idCard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HQHBindCard getHQHBindCardByBankCard(String bankCard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HQHRegister createHQHRegister(HQHRegister hqxhmRegister) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HQHBindCard createHQHBindCard(HQHBindCard hqxgmBindCard) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HQHBindCard getHQHBindCardbyMerchantOrder(String dsorderid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional
	@Override
	public NPRegister createNPRegister(NPRegister NPRegister) {
		NPRegister = npRegisterRepository.save(NPRegister);
		em.flush();
		return NPRegister;
	}

	@Override
	public NPRegister getNPRegisterbyIdcard(String idcard) {
		em.clear();
		NPRegister NPRegister = npRegisterRepository.getNPRegisterByIdcard(idcard);
		return NPRegister;
	}

	@Override
	public List<HQERegion> getHQERegionByParentName(String name) {
		em.clear();
		List<HQERegion> result = hqeRegionRepository.getHQERegionByParentName(name);
		return result;
	}

	@Transactional
	@Override
	public NPBindCard createNPBindCard(NPBindCard npBindCard) {
		npBindCard = npBindCardRepository.save(npBindCard);
		em.flush();
		return npBindCard;
	}

	@Override
	public NPBindCard getNPBindCardbyBankCard(String bankCard) {
		em.clear();
		NPBindCard npBindCard = npBindCardRepository.getNPBindCardByBankCard(bankCard);
		return npBindCard;
	}

	@Override
	public String findKFTCityCodeByProvinceAndCityName(String cityName) {
		em.clear();
		String cityCode = kftAddressRepository.findCodeByProvinceAndCityName(cityName);
		return cityCode;
	}

	@Transactional
	@Override
	public CJHKLRRegister createRegister(CJHKLRRegister cjhk) {
		CJHKLRRegister cj = cjhklrRegisterRepository.save(cjhk);
		em.flush();
		return cj;
	}

	@Override
	public CJHKLRRegister getRegister(String idCard) {
		em.clear();
		CJHKLRRegister cj = cjhklrRegisterRepository.getCJHKLRRegisterByIdCard(idCard);
		return cj;
	}

	@Transactional
	@Override
	public CJHKLRBindCard createBindCard(CJHKLRBindCard cjhkbindcard) {
		CJHKLRBindCard cj = cjhklrBindCardRepository.save(cjhkbindcard);
		em.flush();
		return cj;
	}

	@Override
	public CJHKLRBindCard getBindCard(String bankCard) {
		em.clear();
		CJHKLRBindCard cj = cjhklrBindCardRepository.getCJHKBindCardByBankCard(bankCard);
		return cj;
	}

	@Transactional
	@Override
	public HZLRBindCard createBindCard(HZLRBindCard hzlrBindCard) {
		HZLRBindCard hz = hzlrBindCardRepository.save(hzlrBindCard);
		em.flush();
		return hz;
	}

	@Override
	public HZLRBindCard getBindCardByBankCard(String bankCard) {
		em.clear();
		HZLRBindCard hz = hzlrBindCardRepository.getHZLRBindCardByBankCard(bankCard);
		return hz;
	}

	@Override
	public QJRegister getQJRegisterByIdCard(String idCard) {
		em.clear();
		QJRegister qjRegister = qjRegisterRepository.getQJRegisterByIdCard(idCard);
		return qjRegister;
	}

	@Override
	public QJBindCard getQJBindCardByBankCard(String bankCard) {
		em.clear();
		QJBindCard qjBindCard = qjBindCardRepository.getQJBindCardByBankCard(bankCard);
		return qjBindCard;
	}

	@Transactional
	@Override
	public QJBindCard createQJBindCard(QJBindCard bindCard) {
		QJBindCard qjBindCard = qjBindCardRepository.save(bindCard);
		em.flush();
		return qjBindCard;
	}

	@Transactional
	@Override
	public QJRegister createQJRegister(QJRegister register) {
		QJRegister qjRegister = qjRegisterRepository.save(register);
		em.flush();
		return qjRegister;
	}

	@Transactional
	@Override
	public XTRegister createXTRegister(XTRegister xtRegister) {
		XTRegister xt = xtRegisterRepository.save(xtRegister);
		em.flush();
		return xt;
	}

	@Override
	public XTRegister getXTRegisterByIdCard(String idCard) {
		em.clear();
		XTRegister xt = xtRegisterRepository.getXTRegisterByIdCard(idCard);
		return xt;
	}

	@Override
	public XTBindCard getXTBindCardByBankCard(String bankCard) {
		em.clear();
		XTBindCard xt = xtBindCardRepository.getXTBindCardByBankCard(bankCard);
		return xt;
	}

	@Transactional
	@Override
	public XTBindCard createXTBindCard(XTBindCard xtBindCard) {
		XTBindCard xt = xtBindCardRepository.save(xtBindCard);
		em.flush();
		return xt;
	}

	@Transactional
	@Override
	public XTOrderCode createXTOrderCode(XTOrderCode xtOrderCode) {
		XTOrderCode xt = xtOrderCodeRepository.save(xtOrderCode);
		em.flush();
		return xt;
	}

	@Override
	public List<String> findAllXTordercode() {
		em.clear();
		List<String> result = xtOrderCodeRepository.getXTOrderCodeByStatus();
		return result;
	}

	@Override
	public XTOrderCode changextstatus(String ordercode) {
		em.clear();
		XTOrderCode xt = xtOrderCodeRepository.getxtorderbyordercode(ordercode);
		return xt;
	}

	@Transactional
	@Override
	public HQNEWBindCard createHQNEWBindCard(HQNEWBindCard hqBindCard) {
		HQNEWBindCard hqnewBindCard = hqnewBindCardRepository.save(hqBindCard);
		em.flush();
		return hqnewBindCard;
	}

	@Override
	public HQNEWBindCard getHQNEWBindCardByBankCard(String bankCard) {
		em.clear();
		HQNEWBindCard hqnewBindCard = hqnewBindCardRepository.getHQNEWBindCardByBankCard(bankCard);
		return hqnewBindCard;
	}

	@Override
	public HQNEWBindCard getHQNEWBindCardByDsorderid(String dsorderid) {
		em.clear();
		HQNEWBindCard hqnewBindCard = hqnewBindCardRepository.getHQNEWBindCardByDsorderid(dsorderid);
		return hqnewBindCard;
	}

	@Transactional
	@Override
	public HQNEWRegister createHQNEWRegister(HQNEWRegister hqRegisters) {
		HQNEWRegister hqRegister = hqnewRegisterRepository.save(hqRegisters);
		em.flush();
		return hqRegister;
	}

	@Override
	public HQNEWRegister getHQNEWRegisterByIdCard(String idCard) {
		em.clear();
		HQNEWRegister hqRegister = hqnewRegisterRepository.getHQNEWRegisterByIdCard(idCard);
		return hqRegister;
	}

	@Override
	public HQXRegister getHQXRegisterByIdCard(String idCard) {
		em.clear();
		HQXRegister hqxRegister = hqxRegisterRepository.getHQXRegisterByIdCard(idCard);
		return hqxRegister;
	}

	@Override
	public HQXBindCard getHQXBindCardByBankCard(String bankCard) {
		em.clear();
		HQXBindCard hqxBindCard = hqxBindCardRepository.getHQXBindCardByBankCard(bankCard);
		return hqxBindCard;
	}

	@Transactional
	@Override
	public HQXRegister createHQXRegister(HQXRegister hqxRegister) {
		HQXRegister hqRegister = hqxRegisterRepository.save(hqxRegister);
		em.flush();
		return hqRegister;
	}

	@Transactional
	@Override
	public HQXBindCard createHQXBindCard(HQXBindCard hqxBindCard) {
		HQXBindCard hqBindCard = hqxBindCardRepository.save(hqxBindCard);
		em.flush();
		return hqBindCard;
	}

	@Override
	public List<YCMerch> findYCMerch(String area) {
		return ycMerchRepository.getmerchbyordercode(area);
	}

	@Override
	public YCACCount findYCAccountByIdCard(String idcard) {
		return ucaccountRepository.findByIdCard(idcard);
	}

	@Transactional
	@Override
	public YCACCount createYCACCount(YCACCount ycacCount) {
		YCACCount yc = ucaccountRepository.save(ycacCount);
		em.flush();
		return yc;
	}

	@Override
	public YCBindCard findYCBindCardByCardNo(String bankCard) {
		return ycBindCardRepository.findByCardNo(bankCard);
	}

	@Transactional
	@Override
	public YCBindCard createYCBindcard(YCBindCard ycBindCard) {
		YCBindCard yc = ycBindCardRepository.save(ycBindCard);
		em.flush();
		return yc;

	}

	@Override
	public List<SYBAddress> findSYBAllprovice() {
		return sybAddressRepository.findAllprovice();
	}

	@Override
	public List<SYBAddress> findSYBarea(String province) {
		return sybAddressRepository.findarea(province);
	}

	@Override
	public List<SYBMCC> findallmcc() {
		return sybmccRepository.findAllMCC();
	}

	@Transactional
	@Override
	public SYBRegister createSYBRegister(SYBRegister sybRegister) {
		SYBRegister register = sybRegisterRepository.save(sybRegister);
		em.flush();
		return register;
	}

	@Override
	public SYBRegister findSYBRegisterbyIdcard(String idcard) {
		em.clear();
		SYBRegister sybRegister = sybRegisterRepository.getSYBRegisterByIdCard(idcard);
		return sybRegister;
	}

	@Transactional
	@Override
	public SYBBindCard createSYBBindCard(SYBBindCard sybBindCard) {
		SYBBindCard bindCard = sybBindCardRepository.save(sybBindCard);
		em.flush();
		return bindCard;
	}

	@Override
	public SYBBindCard findSYBBindCardbybankcard(String bankcard) {
		em.clear();
		SYBBindCard sybBindCard = sybBindCardRepository.getSYBBindCardByBankCard(bankcard);
		return sybBindCard;
	}

	@Override
	public HQXBindCard getHQXBindCardByOrderId(String orderId) {
		em.clear();
		HQXBindCard result = hqxBindCardRepository.getHQXBindCardByOrderId(orderId);
		return result;
	}

	@Override
	public HXDHXRegister getHXDHXRegisterByIdCard(String idCard) {
		em.clear();
		HXDHXRegister hxdhxRegister = hxdhxRegisterRepository.getHXDHXRegisterByIdCard(idCard);
		return hxdhxRegister;
	}

	@Override
	public HXDHXBindCard getHXDHXBindCardByBankCard(String bankCard) {
		em.clear();
		HXDHXBindCard hxdhxBindCard = hxdhxBingCardRepositor.getHQXBindCardByBankCard(bankCard);
		return hxdhxBindCard;
	}

	@Override
	public HXDHXRegister createHXDHXRegister(HXDHXRegister hxdhxRegister){
		em.clear();
		HXDHXRegister hxdhxRegister1 = hxdhxRegisterRepository.save(hxdhxRegister);
		return  hxdhxRegister1;
	}
	@Override
	public HXDHXBindCard createHXDHXBingCard(HXDHXBindCard hxdhxBindCard){
		em.clear();
		HXDHXBindCard hxdhxBindCard1 = hxdhxBingCardRepositor.save(hxdhxBindCard);
		return hxdhxBindCard1;
	}

	@Override
	public XKBankType getXKBankTypeByBankName(String bankName) {
		em.clear();
		XKBankType bankType = xkBankTypeRepository.getXKBankTypeByBankName(bankName);
		return bankType;
	}

	@Override
	public XKDHDRegister getXKDHDRegisterByIdCard(String idCard) {
		em.clear();
		XKDHDRegister register = xkdhdRegisterRepository.getXKDHDRegisterByIdCard(idCard);
		return register;
	}

	@Transactional
	@Override
	public XKDHDRegister createXKDHDRxegister(XKDHDRegister register) {
		em.clear();
		XKDHDRegister register1 = xkdhdRegisterRepository.save(register);
		return register1;


	}
  @Transactional
	@Override
	public XKDHDBindCard createXKDHDBindCard(XKDHDBindCard bindCard) {
		em.clear();
		XKDHDBindCard bin = xkdhdBindCardRepository.save(bindCard);
		return bin;

	}

	@Override
	public XKDHDBindCard getXKDHDBindCardByBankCard(String bankCard) {
		em.clear();
		XKDHDBindCard xkdhdBindCardByBankCard = xkdhdBindCardRepository.getXKDHDBindCardByBankCard(bankCard);
		return xkdhdBindCardByBankCard;
	}

	@Override
	public List<XKArea> getXKAreaByParentId(String parentId) {
		em.clear();
		List<XKArea> list = xkAreaRepository.getXKAreaByParentId(parentId);
		return list;
	}
	@Override
	public TLDHXRegister getTLDHXRegisterByIdCard(String idCard) {
		TLDHXRegister tldhxRegister = tldhxRegisterRepository.getTLDHXRegisterByIdCard(idCard);
		return tldhxRegister;
	}

	@Override
	public TLDHXBindCard getTLDHXBindCardByBankCard(String bankCard) {
		TLDHXBindCard tldhxBindCard = tldhxBingCardRepositor.getTLDHXBindCardByBankCard(bankCard);
		return tldhxBindCard;
	}

	@Override
	public void createTLDHXBingCard(TLDHXBindCard tldhxBindCard) {
		tldhxBingCardRepositor.save(tldhxBindCard);
	}

	@Override
	public void createTLDHXRegister(TLDHXRegister tldhxRegister) {
		tldhxRegisterRepository.save(tldhxRegister);
	}

	@Override
	public YPLRegister getYPLRegisterByIdCard(String idCard) {
		em.clear();
		YPLRegister register = yplRegisterRepository.getYPLRegisterByIdCard(idCard);
		return register;
	}

	@Override
	public DBindCard getDBindCardByDebitCardNo(String debitCardNo) {
		em.clear();
		DBindCard dBindCard = dBindCardRepository.getDBindCardByDebitCardNo(debitCardNo);
		return dBindCard;
	}

	@Override
	public CBindCard getCBindCardByBankCard(String bankCard) {
		em.clear();
		CBindCard cBindCard = cBindCardRepository.getCBindCardByBankCard(bankCard);
		return cBindCard;
	}
	@Transactional
	@Override
	public YPLRegister createYPLRegister(YPLRegister register) {
		em.clear();
		YPLRegister r = yplRegisterRepository.save(register);
		return  r;
	}
	@Transactional
	@Override
	public DBindCard createDBindCard(DBindCard dBindCard) {
		em.clear();
		DBindCard d = dBindCardRepository.save(dBindCard);
		return d;

	}
	@Transactional
	@Override
	public CBindCard createCBindCard(CBindCard cBindCard) {
		em.clear();
		CBindCard c = cBindCardRepository.save(cBindCard);
		return  c;

	}

	@Override
	public List<YPLAddress> getYPLAddressByParentId(String parentId) {
		em.clear();
		List<YPLAddress> list = yplAddressRepository.getYPLAddressByParentId(parentId);
		return list;
	}

	@Override
	public List<YPLMCC> getYPLMCCByParent(String parent) {
		em.clear();
		List<YPLMCC> list = yplmccRepository.getYPLMCCByParent(parent);
		return list;
	}

	@Override
	public List<Area> listAreaInfo(int id) {
		em.clear();
		return areaRepository.findByAreaParentId(id);
	}
	@Override
	public CJHKLRChannelWhite getChannelWhite(String channelType, String status,String channelCode) {
		CJHKLRChannelWhite list = cjhklrChannelWriteRepository.findAllByChannelTypeAndAndStatusAndChannelCode(channelType, status,channelCode);
		return list;
	}

	@Override
	public CJHKLRChannelCodeRelation getCJHKLRChannelCodeRelation(String bankCard) {
		CJHKLRChannelCodeRelation byBankCard = cjhklrChannelCodeRelationRepository.findByBankCard(bankCard);
		return byBankCard;
	}

	@Override
	public CJHKLRChannelCodeRelation createCJHKLRChannelCodeRelation(CJHKLRChannelCodeRelation channelCodeRelation) {
		return cjhklrChannelCodeRelationRepository.save(channelCodeRelation);
	}
}
