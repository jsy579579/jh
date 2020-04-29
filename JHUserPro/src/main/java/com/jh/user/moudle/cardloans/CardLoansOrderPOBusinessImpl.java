package com.jh.user.moudle.cardloans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserBalanceBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRealtionBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserRealtion;
import com.jh.user.util.AliOSSUtil;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.StringUtil;
import net.sf.json.JSONObject;

@Service
public class CardLoansOrderPOBusinessImpl implements ICardLoansOrderPOBusiness {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private ICardLoansOrderPORepository iCardLoansOrderPORepository;
	
	@Autowired
	private ICardLoansRatioPORepository iCardLoansRatioPORepository;
	
	@Autowired
	private UserRealtionBusiness userRealtionBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired 
	private UserBalanceBusiness userBalanceBusiness;
	
	@Autowired 
	private BrandManageBusiness brandManageBusiness;
	
	@Autowired 
	private ICardLoansRebateHistoryPORepository cardLoansRebateHistoryPORepository;
	
	@Autowired
	private AliOSSUtil aliOSSUtil;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Transactional
	private CardLoansOrderPO save(CardLoansOrderPO cardLoansOrderPO) {
		return iCardLoansOrderPORepository.saveAndFlush(cardLoansOrderPO);
	}

	@Override
	@Transactional
	public CardLoansOrderPO createOne(LinkConfigPO linkConfigPO, String userId, String phone, String realname,String idcard, String loanAmount) {
		CardLoansOrderPO cardLoansOrderPO = new CardLoansOrderPO();
		cardLoansOrderPO.setUserId(userId);
		cardLoansOrderPO.setBrandId(linkConfigPO.getBrandId());
		cardLoansOrderPO.setClearingForm(linkConfigPO.getClearingForm());
		cardLoansOrderPO.setPhone(phone);
		cardLoansOrderPO.setName(realname);
		cardLoansOrderPO.setIdcard(idcard);
		cardLoansOrderPO.setOrderCode(DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHmmssSSS")+new Random().nextInt(9));
		if (StringUtil.isNotNullString(loanAmount)) {
			cardLoansOrderPO.setLoanAmount(new BigDecimal(loanAmount));
		}
		cardLoansOrderPO.setCreateTime(new Date());
		cardLoansOrderPO.setOrderType(linkConfigPO.getLinkType());
		cardLoansOrderPO.setClassify(linkConfigPO.getLinkClassify());
		return this.save(cardLoansOrderPO);
	}

	@Override
	public Page<CardLoansOrderPO> findList(String brandId, String phone, String name, String idcard, String orderType,String classify, String orderStatus,String orderCode, Pageable pageable) {
		return iCardLoansOrderPORepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (StringUtil.isNotNullString(brandId)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.brandId), brandId)));
			}
            if (StringUtil.isNotNullString(name)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.name), name)));
			}
            if (StringUtil.isNotNullString(phone)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.phone), phone)));
            }
            if (StringUtil.isNotNullString(idcard)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.idcard), idcard)));
			}
            if (StringUtil.isNotNullString(orderType)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.orderType), orderType)));
			}
            if (StringUtil.isNotNullString(classify)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.classify), classify)));
            }
            if (StringUtil.isNotNullString(orderStatus)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.orderStatus), orderStatus)));
            }
            if (StringUtil.isNotNullString(orderCode)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.orderCode), orderCode)));
			}
            return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageable);
	}

	@Override
	public Page<CardLoansOrderPO> findList(String userId, String orderStatus, String orderType, String classify,String brandId,Pageable pageable) {
		return iCardLoansOrderPORepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (StringUtil.isNotNullString(brandId)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.brandId), brandId)));
			}
            if (StringUtil.isNotNullString(userId)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.userId), userId)));
            }
            if (StringUtil.isNotNullString(orderStatus)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.orderStatus), orderStatus)));
			}
            if (StringUtil.isNotNullString(orderType)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.orderType), orderType)));
			}
            if (StringUtil.isNotNullString(classify)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(CardLoansOrderPO_.classify), classify)));
            }
            return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageable);
	}

	@Override
	public CardLoansOrderPO findById(Long cardLoansOrderId) {
		return iCardLoansOrderPORepository.findOne(cardLoansOrderId);
	}

	@Override
	@Transactional
	public CardLoansOrderPO setOrderStatus(CardLoansOrderPO cardLoansOrderPO, String orderStatus, String rebate) {
		cardLoansOrderPO.setOrderStatus(orderStatus);
		if (CardLoansOrderPOController.ORDER_STATUS_SUCCESS.equals(orderStatus)) {
			cardLoansOrderPO.setRebate(new BigDecimal(rebate));
			this.sendCardLoansOrderRebate(cardLoansOrderPO);
		}
		cardLoansOrderPO = this.save(cardLoansOrderPO);
		return cardLoansOrderPO;
	}
	
	private void sendCardLoansOrderRebate(CardLoansOrderPO cardLoansOrderPO) {
		List<CardLoansRatioPO> cardLoansRatioPOs = iCardLoansRatioPORepository.findByBrandId(cardLoansOrderPO.getBrandId());
		Brand brand = brandManageBusiness.findBrandById(2l);
		long manageId = brand.getManageid();
		
		Map<String,BigDecimal> ratios = new HashMap<>();
		for (CardLoansRatioPO cardLoansRatioPO : cardLoansRatioPOs) {
			ratios.put(cardLoansRatioPO.getPreGrade(), cardLoansRatioPO.getRatio());
		}
		List<UserRealtion> userRealtions = userRealtionBusiness.findByFirstUserId(Long.valueOf(cardLoansOrderPO.getUserId()));
		BigDecimal ratio = BigDecimal.ZERO;
		BigDecimal preRatio = BigDecimal.ZERO;
		List<Map<String,String>> list = new ArrayList<>();
		UserRealtion firstUserRealtion = new UserRealtion();
		firstUserRealtion.setFirstUserId(Long.valueOf(cardLoansOrderPO.getUserId()));
		firstUserRealtion.setFirstUserPhone(cardLoansOrderPO.getPhone());
		firstUserRealtion.setPreUserId(Long.valueOf(cardLoansOrderPO.getUserId()));
		firstUserRealtion.setPreUserPhone(cardLoansOrderPO.getPhone());
		userRealtions.add(0, firstUserRealtion);
		for (Iterator iterator = userRealtions.iterator(); iterator.hasNext();) {
			UserRealtion userRealtion = (UserRealtion) iterator.next();
			if (userRealtion.getPreUserId().equals(manageId)) {
				break;
			}
			User user = userLoginRegisterBusiness.queryUserById(userRealtion.getPreUserId());
			if (user == null) {
				continue;
			}
			ratio = ratios.get(user.getGrade());
			if (ratio == null) {
				continue;
			}
			BigDecimal preRebate = cardLoansOrderPO.getRebate().multiply(ratio.subtract(preRatio)).setScale(2, BigDecimal.ROUND_HALF_UP);
			if (BigDecimal.ZERO.compareTo(preRebate) >= 0) {
				continue;
			}
			preRatio = ratio;
			Map<String,String> map = new HashMap<>();
			map.put("userId", user.getId()+"");
			map.put("preRebate", preRebate.toString());
			map.put("rate", ratio.subtract(preRatio).toString());
			map.put("sourceUserId", cardLoansOrderPO.getUserId());
			map.put("sourcePhone", cardLoansOrderPO.getPhone());
			map.put("receivePhone", user.getPhone());
			list.add(map);
		}
		if (list.size() > 0) {
			this.updateUserBalance(list, manageId,cardLoansOrderPO.getOrderCode(),cardLoansOrderPO.getOrderType());
		}
	}
	
	@Transactional
	private void updateUserBalance(List<Map<String,String>> list,Long manageId,String orderCode,String orderType) {
		for (Map<String, String> map : list) {
			String userId = map.get("userId");
			String preRebate = map.get("preRebate");
			String rate = map.get("rate");
			String sourceUserId = map.get("sourceUserId");
			String sourcePhone = map.get("sourcePhone");
			String receivePhone = map.get("receivePhone");
			try {
//				userBalanceBusiness.updateUserAccount(Long.valueOf(manageId), new BigDecimal(preRebate), "1", orderCode);
//				userBalanceBusiness.updateUserAccount(Long.valueOf(userId), new BigDecimal(preRebate), "0", orderCode);
				this.addPaymentOrderAndUpdatePaymentOrder(userId, preRebate, "JIEFUBAO", "获得下级用户"+orderType+"奖励", manageId+"");
				
				CardLoansRebateHistoryPO cardLoansRebateHistoryPO = new CardLoansRebateHistoryPO();
				cardLoansRebateHistoryPO.setSourcePhone(sourcePhone);
				cardLoansRebateHistoryPO.setSourceUserId(sourceUserId);
				cardLoansRebateHistoryPO.setReceivePhone(receivePhone);
				cardLoansRebateHistoryPO.setReceiveUserId(userId);
				cardLoansRebateHistoryPO.setRate(new BigDecimal(rate));
				cardLoansRebateHistoryPO.setReceiveAmount(new BigDecimal(preRebate));
				this.save(cardLoansRebateHistoryPO);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	private void addPaymentOrderAndUpdatePaymentOrder(String userId,String amount,String channelTag,String description,String manageid) {
		JSONObject paymentOrder = this.addPaymentOrder(userId, amount, channelTag, description);
		paymentOrder = paymentOrder.getJSONObject(CommonConstants.RESULT);
		String orderCode = paymentOrder.getString("ordercode");
		paymentOrder = this.updatePaymentOrder(orderCode,manageid);
	}
	
	private JSONObject addPaymentOrder(String userId,String amount,String channelTag,String description) {
		String url = "http://transactionclear/v1.0/transactionclear/payment/type1/add";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("userid", userId);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channelTag);
		requestEntity.add("desc", description);
		requestEntity.add("desc_code", "RedPayment");
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("http://transactionclear/v1.0/transactionclear/payment/type1/add=====" + result);
		return JSONObject.fromObject(result);
	}
	
	private JSONObject updatePaymentOrder(String orderCode,String manageid) {
		String url = "http://transactionclear/v1.0/transactionclear/payment/type1/update";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("order_code", orderCode);
		requestEntity.add("manageid", manageid);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("http://transactionclear/v1.0/transactionclear/payment/type1/update=====" + result);
		return JSONObject.fromObject(result);
	}
	
	@Transactional
	private CardLoansRebateHistoryPO save(CardLoansRebateHistoryPO cardLoansRebateHistoryPO) {
		return cardLoansRebateHistoryPORepository.saveAndFlush(cardLoansRebateHistoryPO);
	}

	@Override
	public List<CardLoansRatioPO> findRatiosByBrandId(String brandId) {
		return iCardLoansRatioPORepository.findByBrandId(brandId);
	}

	@Override
	public CardLoansRatioPO createCardLoansRatioPO(String brandId, String preGrade, String ratio) {
		CardLoansRatioPO cardLoansRatioPO = new CardLoansRatioPO();
		cardLoansRatioPO.setBrandId(brandId);
		cardLoansRatioPO.setPreGrade(preGrade);
		cardLoansRatioPO.setRatio(new BigDecimal(ratio));
		cardLoansRatioPO.setCreateTime(new Date());
		return this.save(cardLoansRatioPO);
	}
	
	@Transactional
	private CardLoansRatioPO save(CardLoansRatioPO cardLoansRatioPO) {
		return iCardLoansRatioPORepository.saveAndFlush(cardLoansRatioPO);
	}

	@Override
	public CardLoansRatioPO findRatiosById(Long id) {
		return iCardLoansRatioPORepository.findOne(id);
	}

	@Override
	@Transactional
	public void deleteCardLoansRatioPO(CardLoansRatioPO cardLoansRatioPO) {
		iCardLoansRatioPORepository.delete(cardLoansRatioPO);		
	}

	@Override
	public CardLoansRatioPO updateCardLoansRatioPO(CardLoansRatioPO cardLoansRatioPO, BigDecimal bigRatio) {
		cardLoansRatioPO.setRatio(bigRatio);
		return this.save(cardLoansRatioPO);
	}

	@Override
	public CardLoansRatioPO findRatiosByBrandIdAndPreGrade(String brandId, String preGrade) {
		return iCardLoansRatioPORepository.findRatiosByBrandIdAndPreGrade(brandId, preGrade);
	}

	@Override
	public Page<CardLoansRebateHistoryPO> findCardLoansRebateHistoryPOByReceiveUserId(String userId,
			Pageable pageable) {
		return cardLoansRebateHistoryPORepository.findByReceiveUserId(userId,pageable);
	}
	
	private String getFeedBackPicturePrefixUrl(String brandId,String cardLoansOrderId) {
		return AliOSSUtil.CARD_LOANS_ORDER_PICTURES+"-"+brandId+"-"+cardLoansOrderId+"-"+System.currentTimeMillis();
	}
	
	private String uploadFileToOSS(MultipartFile file,String brandId,String cardLoansOrderId) {
		String fileName = file.getOriginalFilename();
		fileName = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		String ossObjectName = this.getFeedBackPicturePrefixUrl(brandId,cardLoansOrderId)+fileName;
		try {
			return  this.uploadFileToOSS(ossObjectName, file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String uploadFileToOSS(InputStream inputStream,String suffix,String brandId,String cardLoansOrderId) {
		String ossObjectName = this.getFeedBackPicturePrefixUrl(brandId,cardLoansOrderId)+"."+suffix;
		return  this.uploadFileToOSS(ossObjectName, inputStream);
	}
	
	private String uploadFileToOSS(String ossObjectName,InputStream inputStream) {
		aliOSSUtil.uploadStreamToOss(ossObjectName, inputStream);
		return aliOSSUtil.getFileUrl(ossObjectName);
	}

	@Override
	public CardLoansOrderPO putFeedbackPicture(CardLoansOrderPO cardLoansOrderPO, MultipartFile file1,MultipartFile file2, MultipartFile file3) {
		String feedbackPicture = "";
		if (file1 != null && !file1.isEmpty()) {
			String url = this.uploadFileToOSS(file1, cardLoansOrderPO.getBrandId(), cardLoansOrderPO.getId()+"");
			feedbackPicture = feedbackPicture + url + ",";
		}
		if (file2 != null && !file2.isEmpty()) {
			String url = this.uploadFileToOSS(file2, cardLoansOrderPO.getBrandId(), cardLoansOrderPO.getId()+"");
			feedbackPicture = feedbackPicture + url + ",";
		}
		if (file3 != null && !file3.isEmpty()) {
			String url = this.uploadFileToOSS(file3, cardLoansOrderPO.getBrandId(), cardLoansOrderPO.getId()+"");
			feedbackPicture = feedbackPicture + url + ",";
		}
		if (StringUtil.isNotNullString(feedbackPicture)) {
			feedbackPicture = feedbackPicture.substring(0, feedbackPicture.length()-1);
		}
		cardLoansOrderPO.setFeedbackPicture(feedbackPicture);
		return this.save(cardLoansOrderPO);
	}

	
	private InputStream str2InputStream(String str) {
		InputStream inputStream = null;
		if (str != null && str.contains(";") && str.contains(",")) {
			str = str.substring(str.indexOf(",")+1,str.length());
	        //Base64解码
	        byte[] b = Base64.getDecoder().decode(str);
	        for(int i=0;i<b.length;++i){
	            if(b[i]<0){
	                //调整异常数据
	                b[i]+=256;
	            }
	        }
	        inputStream = new ByteArrayInputStream(b);
		}
		return inputStream;
	}
	
	private String getFileSuffix(String str) {
		String suffix = null;
		if (str != null && str.contains(";") && str.contains(",")) {
			suffix = str.substring(str.indexOf("/")+1,str.indexOf(";"));
		}
		return suffix;
	}
	
	@Override
	public CardLoansOrderPO putFeedbackPicture(CardLoansOrderPO cardLoansOrderPO, String data1, String data2,String data3) {
		String feedbackPicture = "";
		if (data1 != null && StringUtil.isNotNullString(data1)) {
			InputStream inputStream = this.str2InputStream(data1);
			if (inputStream != null) {
				String fileSuffix = this.getFileSuffix(data1);
				String url = this.uploadFileToOSS(inputStream,fileSuffix, cardLoansOrderPO.getBrandId(), cardLoansOrderPO.getId()+"");
				feedbackPicture = feedbackPicture + url + ",";
			}
		}
		if (data2 != null && StringUtil.isNotNullString(data2)) {
			InputStream inputStream = this.str2InputStream(data2);
			if (inputStream != null) {
				String fileSuffix = this.getFileSuffix(data2);
				String url = this.uploadFileToOSS(inputStream,fileSuffix, cardLoansOrderPO.getBrandId(), cardLoansOrderPO.getId()+"");
				feedbackPicture = feedbackPicture + url + ",";
			}
		}
		if (data3 != null && StringUtil.isNotNullString(data3)) {
			InputStream inputStream = this.str2InputStream(data3);
			if (inputStream != null) {
				String fileSuffix = this.getFileSuffix(data3);
				String url = this.uploadFileToOSS(inputStream,fileSuffix, cardLoansOrderPO.getBrandId(), cardLoansOrderPO.getId()+"");
				feedbackPicture = feedbackPicture + url + ",";
			}
		}
		if (StringUtil.isNotNullString(feedbackPicture)) {
			feedbackPicture = feedbackPicture.substring(0, feedbackPicture.length()-1);
		}
		cardLoansOrderPO.setFeedbackPicture(feedbackPicture);
		return this.save(cardLoansOrderPO);
	}
	
}
