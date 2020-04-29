package com.jh.user.business.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRealtionBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.InfoUser;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserBankInfo;
import com.jh.user.pojo.UserOld;
import com.jh.user.pojo.UserRoleForm;
import com.jh.user.pojo.UserShops;
import com.jh.user.repository.BrandRateRepository;
import com.jh.user.repository.BrandRepository;
import com.jh.user.repository.ChannelRateRepository;
import com.jh.user.repository.JdpushHistoryRepository;
import com.jh.user.repository.UserAccountFreezeHistoryRepository;
import com.jh.user.repository.UserAccountRepository;
import com.jh.user.repository.UserBalanceRepository;
import com.jh.user.repository.UserBankInfoRepository;
import com.jh.user.repository.UserCoinRepository;
import com.jh.user.repository.UserOldRepository;
import com.jh.user.repository.UserRebateFreezeHistoryRepository;
import com.jh.user.repository.UserRebateHistoryRepository;
import com.jh.user.repository.UserRepository;
import com.jh.user.repository.UserRoleRepository;
import com.jh.user.repository.UserShopsRepository;
import com.jh.user.util.DataEncrypt;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Service
public class UserLoginRegisterBusinessImpl implements UserLoginRegisterBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(UserLoginRegisterBusinessImpl.class);

	@Autowired
	private UserRepository userRepository;

	// 商铺管理
	@Autowired
	private UserShopsRepository userShopRepository;
	
	@Autowired
	private UserOldRepository userOldRepository;

	@Autowired
	private BrandRateRepository brandRateRepository;

	@Autowired
	private UserBankInfoRepository userBankInfoRepository;

	@Autowired
	private UserAccountRepository accountRepository;

	@Autowired
	private ChannelRateRepository channelRateRepository;

	@Autowired
	private JdpushHistoryRepository jdpushHistoryRepository;

	@Autowired
	private UserAccountFreezeHistoryRepository userAccountFreezeHistoryRepository;

	@Autowired
	private UserBalanceRepository userBalanceRepository;

	@Autowired
	private UserCoinRepository userCoinRepository;

	@Autowired
	private UserRebateFreezeHistoryRepository userRebateFreezeHistoryRepository;

	@Autowired
	private UserRebateHistoryRepository userRebateHistoryRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;
	
	@Autowired
	private UserRealtionBusiness UserRealtionBusiness;

	@Autowired
	private BrandRepository brandRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	Util util;

	@Autowired
	private EntityManager em;

	@Transactional
	@Override
	public User saveUser(User user) {
		if(user.getId() == 0L){
			UserRealtionBusiness.updateRealtion(user);
		}
		
		Brand brand = brandRepository.findBrandByid(user.getBrandId());
		UserOld model = new UserOld();
		
		if (brand != null && "6".equals(brand.getBrandType())) {
			user = userRepository.save(user);
		} else {
			user = userRepository.save(user);
			BeanUtils.copyProperties(user, model);
			model = userOldRepository.save(model);
		}
		em.flush();
		return user;
	}

	@Override
	@Deprecated
	public User isValidUser(String phone, String password) {
		User model = new User();
		UserOld userOld = userOldRepository.findUserByPhoneAndPassword(phone, password);
		if(userOld!=null){
			BeanUtils.copyProperties(userOld, model);
		}else{
			model = null;
		}
		return model;
	}

	/** 判断是否登陆 */
	@Override
	public User isValidUser(String phone, String password, long brandId) {
		if (brandId == -1) {
			User model = new User();
			UserOld userOld = userOldRepository.findUserByPhoneAndPassword(phone, password);
			if(userOld!=null){
				BeanUtils.copyProperties(userOld, model);
			}else{
				model = null;
			}
			return model;
		} else {
			return userRepository.findUserByPhoneAndPassword(phone, password, brandId);
		}

	}

	@Override
	public User queryUserById(long userid) {
		return userRepository.findUserById(userid);
	}

	/** 根据brandid和时间获取当前时间的人数 */
	@Override
	public int queryUserNumByBidandtime(long brandId, Date StartTimeDate, Date endTimeDate) {
		if (brandId == -1) {
			return userRepository.queryUserNumBytime(StartTimeDate, endTimeDate);
		}
		return userRepository.queryUserNumByBidandtime(brandId, StartTimeDate, endTimeDate);
	}

	@Transactional
	@Override
	public User createNewUser(User user) {
		Brand brand = brandRepository.findBrandByid(user.getBrandId());
		UserOld model = new UserOld();
		if (brand != null && "6".equals(brand.getBrandType())) {
			user = userRepository.save(user);
		} else {
			user = userRepository.save(user);
			BeanUtils.copyProperties(user, model);
			model = userOldRepository.save(model);
		}
//		em.flush();
//		em.clear();
//		user = userRepository.findUserByPhoneAndBrandId(user.getPhone(), user.getBrandId());
		UserAccount account = new UserAccount();
		account.setUserId(user.getId());
		account.setBalance(BigDecimal.ZERO);
		account.setFreezeBalance(BigDecimal.ZERO);
		account.setCoin(0);
		account.setFreezerebateBalance(BigDecimal.ZERO);
		account.setRebateBalance(BigDecimal.ZERO);
		accountRepository.save(account);
		UserRealtionBusiness.updateRealtion(user);
		em.flush();
		em.clear();
		/**
		 * 推送消息 /v1.0/user/jpush/tset
		 */
		String alert = "注册推送";
		String content = user.getPhone() + "已成功注册成为您的下级会员！";
		String btype = "register";
		String btypeval = "";
		/** 获取身份证实名信息 */
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/jpush/tset";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", user.getPreUserId() + "");
		requestEntity.add("alert", alert + "");
		requestEntity.add("content", content + "");
		requestEntity.add("btype", btype + "");
		requestEntity.add("btypeval", btypeval + "");
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(url, requestEntity, String.class);

		return user;
	}

	@Override
	public User isPaypassValid(long userid, String paypass) {
		User user = new User();
		user = userRepository.findUserByUseridAndPayPass(userid, paypass);
		return user;
	}

	@Override
	@Deprecated
	public User queryUserByPhone(String phone) {
		em.clear();
		User model = new User();
		UserOld userOld = userOldRepository.findUserByPhone(phone);
		if(userOld!=null){
			BeanUtils.copyProperties(userOld, model);
		}else{
			model = null;
		}
		return model;
	}

	/** 根据users的手机号码获取用户 */
	@Override
	public List<User> queryUsersByPhone(String phone) {
		List<User> users = new ArrayList<User>();
		users = userRepository.findUsersByPhone(phone);
		return users;
	}

	@Override
	public List<User> queryUserByBrandId(long brandId) {

		List<User> users = new ArrayList<User>();
		users = userRepository.findUserByBrandId(brandId);
		return users;

	}

	/*** 根据等级和贴牌查询 ***/
	@Override
	public List<User> queryUserByGrade(long brandid, String grade) {
		List<User> users = new ArrayList<User>();
		if (brandid == 0) {
			users = userRepository.queryUserByGrade(grade);
		} else {
			users = userRepository.queryUserByGradeAndBrandId(brandid, grade);
		}

		return users;

	}

	/*** 根据等级和贴牌查询 ***/
	@Override
	public List<User> queryUserByStatus(long brandid, String status) {
		List<User> users = new ArrayList<User>();
		users = userRepository.queryUserByGradeAndStatus(brandid, status);
		return users;
	}

	/*** 贴牌审核状态查询 ***/
	@Override
	public List<User> queryUserByStatus(String status) {
		List<User> users = new ArrayList<User>();
		users = userRepository.queryUserByGradeAndStatus(status);
		return users;
	}

	@Override
	public User queryUserByOpenid(String openid) {
		User user = new User();
		user = userRepository.findUserByOpenid(openid);
		return user;
	}

	@Override
	public List<User> findAfterUsers(long userid) {
		List<User> users = new ArrayList<User>();
		users = userRepository.findAfterUserById(userid);
		return users;
	}

	@Override
	public List<User> findInfoUsers(Long[] userIds) {
		List<User> users = new ArrayList<User>();
		users = userRepository.findAfterUserByIds(userIds);
		return users;
	}

	/** 获取改用户的下级会员 */
	@Override
	public List<User> findInfoUsersPageable(Long[] userIds, Pageable pageable) {
		List<User> users = new ArrayList<User>();
		users = userRepository.findAfterUserByIdsPageable(userIds, pageable);
		return users;
	}

	/**
	 * 获取用户信息 phone cardNo银行卡号 brandId通道ID brandname通道名称 fullname全称 realname真名
	 * realnameStatus审核状态
	 ***/
	@Override
	public Map findInfoUserByall(long brandId, String fullname, String realnameStatus, String shopsStatus,
			Date StartTimeDate, Date endTimeDate, Pageable pageable) {
		List<User> users = new ArrayList<User>();
		Map<String, Object> object = new HashMap<String, Object>();

		if (fullname != null && !fullname.equals("")) {
			Page<User> p = userRepository.findAfterUserallByfullname(brandId, "%"+fullname+"%", pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		if (realnameStatus != null && !realnameStatus.equals("")) {
			Page<User> p = userRepository.findAfterUserallByrealnameStatus(brandId, realnameStatus, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;

		}
		if (shopsStatus != null && !shopsStatus.equals("")) {
			Page<User> p = userRepository.findAfterUserallByshopsStatus(brandId, shopsStatus, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;

		}

		if (StartTimeDate != null && endTimeDate != null) {
			Page<User> p = userRepository.findAfterUserallBystartendTime(brandId, StartTimeDate, endTimeDate, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		if (StartTimeDate == null && endTimeDate != null) {
			Page<User> p = userRepository.findAfterUserallByendTime(brandId, endTimeDate, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		if (StartTimeDate != null && endTimeDate == null) {
			Page<User> p = userRepository.findAfterUserallByStartTimeDate(brandId, StartTimeDate, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		Page<User> p = userRepository.findAfterUserall(brandId, pageable);
		object.put("number", p.getNumber());
		object.put("numberOfElements", p.getNumberOfElements());
		object.put("totalElememts", p.getTotalElements());
		object.put("totalpages", p.getTotalPages());
		users = p.getContent();
		if(users.size()==0) {
			object.put("content", null);
		}else {
			object.put("content", this.findInfoUserByall(users));
		}
		return object;

	}

	@Override
	public Map findInfoUserByall(String fullname, String realnameStatus, String shopsStatus, Date StartTimeDate,
			Date endTimeDate, Pageable pageable) {
		List<User> users = new ArrayList<User>();
		Map<String, Object> object = new HashMap<String, Object>();

		if (fullname != null && !fullname.equals("")) {
			Page<User> p = userRepository.findAfterUserallByfullname(fullname, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		if (realnameStatus != null && !realnameStatus.equals("")) {
			Page<User> p = userRepository.findAfterUserallByrealnameStatus(realnameStatus, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;

		}
		if (shopsStatus != null && !shopsStatus.equals("")) {
			Page<User> p = userRepository.findAfterUserallByshopsStatus(shopsStatus, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;

		}
		if (StartTimeDate != null && endTimeDate != null) {
			Page<User> p = userRepository.findAfterUserallBystartendTime(StartTimeDate, endTimeDate, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		if (StartTimeDate == null && endTimeDate != null) {
			Page<User> p = userRepository.findAfterUserallByendTime(endTimeDate, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		if (StartTimeDate != null && endTimeDate == null) {
			Page<User> p = userRepository.findAfterUserallByStartTimeDate(StartTimeDate, pageable);
			object.put("number", p.getNumber());
			object.put("numberOfElements", p.getNumberOfElements());
			object.put("totalElememts", p.getTotalElements());
			object.put("totalpages", p.getTotalPages());
			users = p.getContent();
			object.put("content", this.findInfoUserByall(users));
			return object;
		}
		Page<User> p = userRepository.findAfterUserall(pageable);
		object.put("number", p.getNumber());
		object.put("numberOfElements", p.getNumberOfElements());
		object.put("totalElememts", p.getTotalElements());
		object.put("totalpages", p.getTotalPages());
		users = p.getContent();
		object.put("content", this.findInfoUserByall(users));
		return object;

	}

	/***
	 * 下级会员条件查询
	 **/
	public Page<User> findUserInfoByall(Long[] str2, String fullname, String realnameStatus, String grade,
			Date StartTimeDate, Date endTimeDate, Pageable pageable) {
//		List<User> users = new ArrayList<User>();
		Page<User> users = null;
		if (fullname != null && !fullname.equals("")) {
			users = userRepository.findAfterUserallByfullname(str2, fullname, pageable);
			return users;
		}
		if (grade != null && !grade.equals("")) {
			//users = userRepository.findAfterUserallByGrade(str2, grade, pageable);
			//易百管家定制
			if("0".equals(grade)){
				users = userRepository.findAfterUserallByGrade(str2, grade, pageable);
			}else{
				users = userRepository.findAfterUserallByGradeYibaiguanjia(str2, grade, pageable);
			}
			return users;
		}
		if (realnameStatus != null && !realnameStatus.equals("")) {
			users = userRepository.findAfterUserallByrealnameStatus(str2, realnameStatus, pageable);
			return users;
		}
		if (StartTimeDate != null && endTimeDate != null) {
			users = userRepository.findAfterUserallBystartendTime(str2, StartTimeDate, endTimeDate, pageable);
			return users;
		}
		if (StartTimeDate == null && endTimeDate != null) {
			users = userRepository.findAfterUserallByendTime(str2, endTimeDate, pageable);
			return users;
		}
		if (StartTimeDate != null && endTimeDate == null) {
			users = userRepository.findAfterUserallByStartTimeDate(str2, StartTimeDate, pageable);
			return users;
		}
		users = userRepository.findAfterUserByIdsu(str2, pageable);

		return users;
	}

	public List<InfoUser> findInfoUserByall(List<User> users) {

		List<InfoUser> infoUsers = new ArrayList<InfoUser>();
		StringBuffer sb = new StringBuffer();
		JSONObject authObject = null;
		for (int i = 0; i < users.size(); i++) {
			sb.append(users.get(i).getId());
			if (i != users.size() - 1) {
				sb.append(",");
			}
		}
		JSONObject realNameJson = getRealNameJSONObjectByUserIds(sb.toString());
		JSONObject sumProfitRecoders = getSumProfitByUserIds(sb.toString());
		JSONObject sumType2Json = getSumPayTypeJSONObjectByUserIds(sb.toString(),"2");
		JSONObject sumType0Json = getSumPayTypeJSONObjectByUserIds(sb.toString(),"0");
		for (User user : users) {
			UserBankInfo ubi = userBankInfoRepository.queryDefUserBankInfoByUserid(user.getId());
			UserAccount userAccount = accountRepository.findUserAccountByUserid(user.getId());
			// if (ubi == null) {
			// /** 获取身份证实名信息 */
			// URI uri = util.getServiceUrl("paymentchannel", "error url
			// request!");
			// String url = uri.toString() +
			// "/v1.0/paymentchannel/realname/userid";
			// MultiValueMap<String, Long> requestEntity = new
			// LinkedMultiValueMap<String, Long>();
			// requestEntity.add("userid", user.getId());
			// RestTemplate restTemplate = new RestTemplate();
			// String result = restTemplate.postForObject(url, requestEntity,
			// String.class);
			// LOG.info("RESULT================/v1.0/paymentchannel/realname/userid"
			// + result);
			// JSONObject jsonObject = JSONObject.fromObject(result);
			// authObject = jsonObject.getJSONObject("realname");
			// }

			InfoUser infouser = new InfoUser();

			// 系统编号
			infouser.setUserid(user.getId());

			// 用户手机号
			infouser.setPhone(user.getPhone());

			infouser.setFullname(user.getFullname()==null?"":user.getFullname());

			// 用户性别
			infouser.setSex(user.getSex());
			
			infouser.setRemarks(user.getRemarks()==null?"":user.getRemarks());

			// 商铺状态
			infouser.setUsershopStatus(user.getShopsStatus());
			if (user.getShopsStatus().equals("1")) {
				UserShops uShop = userShopRepository.findUserShopsByUid(user.getId());
				infouser.setUserShopName(uShop.getName());
				infouser.setUserShopAddress(uShop.getAddress());
			}
			if (ubi != null && ubi.getUserName() != null) {
				infouser.setBankName(ubi.getBankName());
				infouser.setBankName(ubi.getBankName());
				infouser.setCardNo(ubi.getCardNo());
				// 真是姓名
				infouser.setRealname(ubi.getUserName());
				// 身份证号
				infouser.setIdcard(ubi.getIdcard());

			} else {
				String realNameStatus = user.getRealnameStatus();
//				if ("1".equals(realNameStatus) || "3".equals(realNameStatus)) {
					authObject = realNameJson.getJSONObject(user.getId() + "");
					if (authObject != null && !"null".equals(authObject) && !authObject.isNullObject()) {

						// 真是姓名
						infouser.setRealname(authObject.getString("realname") == null
								|| authObject.getString("realname").equals("null") ? null
										: authObject.getString("realname"));
						// 身份证号
						infouser.setIdcard(
								authObject.getString("idcard") == null || authObject.getString("idcard").equals("null")
										? null : authObject.getString("idcard"));
					} else {
						infouser.setRealname(null);
						infouser.setIdcard(null);
					}
//				} else {
//					infouser.setRealname(null);
//					infouser.setIdcard(null);
//				}
			}
			// 实名状态
			infouser.setRealnameStatus(user.getRealnameStatus());
			infouser.setBrandId(user.getBrandId());
			infouser.setBrandName(user.getBrandname());
			if (userAccount != null) {
				infouser.setBalance(userAccount.getBalance());
				infouser.setFreezeBalance(userAccount.getFreezeBalance());
				infouser.setCoin(userAccount.getCoin());
				infouser.setRebateBalance(userAccount.getRebateBalance());
				infouser.setFreezerebateBalance(userAccount.getFreezerebateBalance());
			} else {
				infouser.setBalance(null);
				infouser.setFreezeBalance(null);
				infouser.setCoin(0);
				infouser.setRebateBalance(null);
				infouser.setFreezerebateBalance(null);
			}
			String rechargeSum = null;
			String withdrawSum = null;
			try {
				rechargeSum = sumType0Json.getString(user.getId()+"");
			} catch (Exception e1) {
				rechargeSum = null;
			}
			
			try {
				withdrawSum = sumType2Json.getString(user.getId()+"");
			} catch (Exception e1) {
				withdrawSum = null;
			}
			infouser.setRechargeSum("".equals(rechargeSum)||"null".equals(rechargeSum)||rechargeSum==null?BigDecimal.ZERO:new BigDecimal(rechargeSum));
			infouser.setWithdrawSum("".equals(withdrawSum)||"null".equals(withdrawSum)||withdrawSum==null?BigDecimal.ZERO:new BigDecimal(withdrawSum));
			String userSumProfit = null;
			try {
				userSumProfit = sumProfitRecoders.getString(user.getId()+"");
			} catch (Exception e) {
				userSumProfit = null;
			}
			infouser.setRebateSum(userSumProfit==null||"".equals(userSumProfit)||"null".equals(userSumProfit)?BigDecimal.ZERO:new BigDecimal(userSumProfit));
			// 提款费
			BrandRate randRate = brandRateRepository.findBrandRateBybrandidAndChannelid(user.getBrandId(), 1);
			if (randRate != null && randRate.getWithdrawFee() != null) {
				infouser.setWithdrawFee(randRate.getWithdrawFee().setScale(2, BigDecimal.ROUND_DOWN));
			} else {
				infouser.setWithdrawFee(new BigDecimal("2.00"));
			}

			// 级别
			infouser.setGrade(user.getGrade());
			// 注册时间
			infouser.setCreateTime(user.getCreateTime());
			infoUsers.add(infouser);
		}

		return infoUsers;
	}

	// 优化后
	private JSONObject getRealNameJSONObjectByUserIds(String userIds) {
		URI uri = util.getServiceUrl("paymentchannel", "error url request!");
		String url = uri.toString() + "/v1.0/paymentchannel/realname/findby/userids";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userIds", userIds);
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================/v1.0/paymentchannel/realname/findby/userids:" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		return jsonObject.getJSONObject(CommonConstants.RESULT);
	}

	// 优化后
	private JSONObject getSumProfitByUserIds(String acqUserIds) {
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/profit/query/byuserids";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("acqUserIds", acqUserIds);
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================/v1.0/transactionclear/profit/query/byuserids:" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		return jsonObject.getJSONObject(CommonConstants.RESULT);
	}
	//	优化后
	private JSONObject getSumPayTypeJSONObjectByUserIds(String userIds,String type){
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/byuserids";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userIds", userIds);
		requestEntity.add("type", type);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================/v1.0/transactionclear/payment/query/byuserids:" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		return jsonObject.getJSONObject(CommonConstants.RESULT);
	}

	private BigDecimal getSumPayType(long userid, String type) {

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/userid";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid + "");
		requestEntity.add("type", type);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		String resultsum = jsonObject.getString("result");
		if (resultsum != null && !resultsum.equals("") && !resultsum.equalsIgnoreCase("null")) {
			return new BigDecimal(resultsum);
		} else {
			return new BigDecimal("0.00");
		}
	}

	private BigDecimal getSumProfit(long userid) {

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/profit/query/userid";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("acq_user_id", userid + "");
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		String resultsum = jsonObject.getString("result");
		if (resultsum != null && !resultsum.equals("") && !resultsum.equalsIgnoreCase("null")) {
			return new BigDecimal(resultsum);
		} else {
			return new BigDecimal("0.00");
		}
	}

	@Override
	public Long[] queryUserIdByGradeAndBrandId(long brandid, String grade) {

		return userRepository.queryUserIdByGradeAndBrandId(brandid, grade);
	}

	@Override
	public Long[] queryUserIdBypreUserIds(Long[] userIds) {

		return userRepository.queryUserIdBypreUserIds(userIds);
	}

	@Override
	public Long[] queryUserIdBypreUserIdsAndGrade(String grade, Long[] userIds) {

		return userRepository.queryUserIdBypreUserIdsAndGrade(userIds, grade);
	}

	@Override
	public Long[] queryUserIdBypreUserIdsAndrealnameStatus(String realnameStatus, Long[] userIds) {

		return userRepository.queryUserIdBypreUserIdsAndrealnameStatus(userIds, realnameStatus);
	}

	// 根据省市县模糊查询用户信息
	@Override
	public List<User> queryUserByProvince(String province, String city, String county, Pageable pageable) {

		if (province != null && !province.equals("")) {
			if (city != null && !city.equals("")) {
				if (county != null && county.equals("")) {
					return userRepository.queryUserByProvince1(province, city, county, pageable);
				} else {
					return userRepository.queryUserByProvince2(province, city, pageable);
				}
			} else {
				if (county != null && !county.equals("")) {
					return userRepository.queryUserByProvince4(province, county, pageable);
				} else {
					return userRepository.queryUserByProvince5(province, pageable);
				}
			}
		} else {
			if (city != null && !city.equals("")) {
				if (county != null && !county.equals("")) {
					return userRepository.queryUserByProvince3(city, county, pageable);
				} else {
					return userRepository.queryUserByProvince6(city, pageable);
				}
			} else {
				return userRepository.queryUserByProvince7(county, pageable);
			}
		}

	}

	@Override
	public User queryUserByPhoneAndBrandid(String phone, long brandid) {
		em.clear();
		User user = new User();
		UserOld model;
		if (brandid == -1) {
			LOG.info("-1进入");
			model = userOldRepository.findUserByPhone(phone);
			if(model!=null){
				BeanUtils.copyProperties(model, user);
			}else{
				user = null;
			}
		} else {
			user = userRepository.findUserByPhoneAndBrandID(phone, brandid);
		}
		return user;
	}

	@Override
	public int queryBrandUserCount(long brandId) {
		return userRepository.queryCountByBrandId(brandId);
	}

	@Override
	public User queryUserByPhoneBrandid(String phone, long brandid) {
		User user = new User();
		UserOld model;
		if (brandid == -1) {
			LOG.info("-1进入");
			model = userOldRepository.findUserByPhone(phone);
			if(model!=null){
				BeanUtils.copyProperties(model, user);
			}else{
				user = null;
			}
		} else {
			user = userRepository.findUserByPhoneAndBrandID(phone, brandid);
		}
		em.clear();
		return user;
	}

	@Transactional
	@Override
	public User createOutNewUser(User user) {
		Brand brand = brandRepository.findBrandByid(user.getBrandId());
		UserOld model = new UserOld();
		if (brand != null && "6".equals(brand.getBrandType())) {
			user = userRepository.save(user);
		} else {
			user = userRepository.save(user);
			BeanUtils.copyProperties(user, model);
			model = userOldRepository.save(model);
		}
		UserAccount account = new UserAccount();
		account.setUserId(user.getId());
		account.setBalance(BigDecimal.ZERO);
		account.setFreezeBalance(BigDecimal.ZERO);
		account.setCoin(0);
		account.setFreezerebateBalance(BigDecimal.ZERO);
		account.setRebateBalance(BigDecimal.ZERO);
		accountRepository.save(account);
		return user;
	}
	
	@Override
	public int queryUserAfterTodayCount(long userId, String todayStr) {
		return userRepository.queryUserAfterTodayCount(userId, todayStr);
	}

	@Override
	public int queryUserAfterYesterdayCount(long userId, String yesterday) {
		return userRepository.queryUserAfterYesterdayCount(userId, yesterday);
	}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
	@Transactional
	@Override
	public void delUserByUserid(long userid) {
		accountRepository.delUserAccount(userid);
		userBankInfoRepository.delBankInfoByUserid(userid);
		channelRateRepository.delChannelRateByUserid(userid);
		jdpushHistoryRepository.delJdpushHistoryByUserid(userid);
		userAccountFreezeHistoryRepository.delUserAccountHistoryByUserid(userid);
		userBalanceRepository.delUserBalanceHistoryByUserid(userid);
		userCoinRepository.delUserCoinByUserid(userid);
		userRebateFreezeHistoryRepository.delUserRebateFreeHistoryByUserid(userid);
		userRebateHistoryRepository.delUserRebateHistoryByUserid(userid);
		userRoleRepository.delUserRoleByUserid(userid);
		userShopRepository.delUserShopsByUserid(userid);
	}

	@Transactional
	@Override
	public void delUserByUserId(long userid) {
		Brand brand = brandRepository.findBrandByid(userRepository.findUserById(userid).getBrandId());
		if (brand != null && "6".equals(brand.getBrandType())) {
			userRepository.delUserByUserid(userid);
		} else {
			userRepository.delUserByUserid(userid);
			userOldRepository.delUserByUserid(userid);
		}
	}

	@Override
	public User findUserByIdAndBrandId(long userId, long brandId) {
		User user = userRepository.queryUserByIdAndBrandId(userId, brandId);
		return user;
	}

	@Override
	public Map<String,Object> findInfoUserByallSql(long brandid, String grade, String fullname, String realnameStatus,
			String shopsStatus, Date StartTimeDate, Date endTimeDate, Pageable pageable,String isDownload) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		StringBuffer sql = new StringBuffer("from t_user where 1=1 ");
		// 如果条件不为空，往后面添加
		if (brandid != 0) {
			sql.append("and brand_id='" + brandid + "' ");
		}
		if (grade != null && !grade.equals("")) {
			sql.append("and grade='" + grade + "' ");
		}
		if (fullname != null && !fullname.equals("")) {
			sql.append("and fullname like '%" + fullname + "%' ");
		}
		if (realnameStatus != null && !realnameStatus.equals("")) {
			sql.append("and real_name_status='" + realnameStatus + "' ");
		}
		if (shopsStatus != null && !shopsStatus.equals("")) {
			sql.append("and shops_status='" + shopsStatus + "' ");
		}
		if (StartTimeDate != null) {
			String starttimeDate = sdf.format(StartTimeDate);
			sql.append("and create_time>='" + starttimeDate + "' ");
		}
		if (endTimeDate != null) {
			String endtimeDate = sdf.format(endTimeDate);
			sql.append("and create_time<='" + endtimeDate + "' ");
		}
		//定义一个新的字符串用来拼接之前的字符串
	  	StringBuffer sqlCount=new StringBuffer("select COUNT(*) as count ").append(sql);
	  	//将字符串变成sql语句查询到的结果转成int类型
		int count=Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString()) ;
		int size = pageable.getPageSize();
		int page = pageable.getPageNumber();
		// 定义一个新的字符串用来表示sql语句
		StringBuffer sqlList = new StringBuffer("select * ").append(sql).append(" order by create_time desc limit "+page*size+","+size);
		List<User> list = jdbcTemplate.query(sqlList.toString(), new RowMapper<User>() {
			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setCreateTime(DateUtil.getDateStringConvert(new Date(), (rs.getString("create_time")), "yyyy-MM-dd HH:mm:ss"));
				user.setId(rs.getLong("id"));
				user.setPhone(rs.getString("phone"));
				user.setPassword(rs.getString("password"));
				user.setPaypass(rs.getString("pay_password"));
				user.setFullname(rs.getString("fullname"));
				user.setRealname(rs.getString("fullname"));
				user.setGrade(rs.getString("grade"));
				user.setInviteCode(rs.getString("invite_code"));
				user.setBrandId(rs.getLong("brand_id"));
				user.setBrandname(rs.getString("brand_name"));
				user.setPreUserId(rs.getLong("pre_user_id"));
				user.setPreUserPhone(rs.getString("pre_user_phone"));
				user.setValidStatus(rs.getInt("valid_status"));
				user.setRealnameStatus(rs.getString("real_name_status"));
				user.setVerifyStatus(rs.getString("verify_status"));
				user.setVdynastType(rs.getString("vdynast_type"));
				user.setEncourageNum(rs.getInt("encourage_num"));
				user.setBankCardManagerStatus(rs.getInt("bank_card_manager_status"));
				user.setShopsStatus(rs.getString("shops_status"));
				return user;
			}
		});
		
		//List<User> list1=DataEncrypt.userDataEncrypt(list);
		Map<String,Object> object = new HashMap<>();
		object.put("number", page);
		object.put("numberOfElements", size);  //每页显示条数
		object.put("totalElememts", count); //总条数
		if(size!=0){
			object.put("totalpages", count/size+1);  //总页数
		}else{
			object.put("totalpages", 1);  //总页数
		}
		if ("0".equals(isDownload)) {
			object.put("content", this.findInfoUserByall(list));
		}else {
			object.put("content", list);
		}
		return object;
	}
	
	@Override
	public Map<String,Object> findInfoUserByallSql(String grade, String fullname, String realnameStatus, String shopsStatus,Date StartTimeDate, Date endTimeDate, Pageable pageable,String isDownload) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		StringBuffer sql = new StringBuffer("from t_user where 1=1 ");
		// 如果条件不为空，往后面添加
		if (grade != null && !grade.equals("")) {
			sql.append("and grade='" + grade + "' ");
		}
		if (fullname != null && !fullname.equals("")) {
			sql.append("and fullname like '%" + fullname + "%' ");
		}
		if (realnameStatus != null && !realnameStatus.equals("")) {
			sql.append("and real_name_status='" + realnameStatus + "' ");
		}
		if (shopsStatus != null && !shopsStatus.equals("")) {
			sql.append("and shops_status='" + shopsStatus + "' ");
		}
		if (StartTimeDate != null) {
			String starttimeDate = sdf.format(StartTimeDate);
			sql.append("and create_time>='" + starttimeDate + "' ");
		}
		if (endTimeDate != null) {
			String endtimeDate = sdf.format(endTimeDate);
			sql.append("and create_time<='" + endtimeDate + "' ");
		}
		//定义一个新的字符串用来拼接之前的字符串
	  	StringBuffer sqlCount=new StringBuffer("select COUNT(*) as count ").append(sql);
	  	//将字符串变成sql语句查询到的结果转成int类型
		int count=Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString()) ;
		int size = pageable.getPageSize();
		int page = pageable.getPageNumber();
		// 定义一个新的字符串用来表示sql语句
		StringBuffer sqlList = new StringBuffer("select * ").append(sql).append(" order by create_time desc limit "+page*size+","+size);
		List<User> list = jdbcTemplate.query(sqlList.toString(), new RowMapper<User>() {
			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setCreateTime(DateUtil.getDateStringConvert(new Date(), (rs.getString("create_time")), "yyyy-MM-dd HH:mm:ss"));
				user.setId(rs.getLong("id"));
				user.setPhone(rs.getString("phone"));
				user.setPassword(rs.getString("password"));
				user.setPaypass(rs.getString("pay_password"));
				user.setFullname(rs.getString("fullname"));
				user.setRealname(rs.getString("fullname"));
				user.setGrade(rs.getString("grade"));
				user.setInviteCode(rs.getString("invite_code"));
				user.setBrandId(rs.getLong("brand_id"));
				user.setBrandname(rs.getString("brand_name"));
				user.setPreUserId(rs.getLong("pre_user_id"));
				user.setPreUserPhone(rs.getString("pre_user_phone"));
				user.setValidStatus(rs.getInt("valid_status"));
				user.setRealnameStatus(rs.getString("real_name_status"));
				user.setVerifyStatus(rs.getString("verify_status"));
				user.setVdynastType(rs.getString("vdynast_type"));
				user.setEncourageNum(rs.getInt("encourage_num"));
				user.setBankCardManagerStatus(rs.getInt("bank_card_manager_status"));
				user.setShopsStatus(rs.getString("shops_status"));
				return user;
			}
		});
		//对查询数据进行脱敏处理
		//List list1=DataEncrypt.userDataEncrypt(list);
		Map<String,Object> object = new HashMap();
		object.put("number", page);
		object.put("numberOfElements", size);  //每页显示条数
		object.put("totalElememts", count); //总条数
		if(size!=0){
			object.put("totalpages", count/size+1);  //总页数
		}else{
			object.put("totalpages", 1);  //总页数
		}
		if ("0".equals(isDownload)) {
			object.put("content", this.findInfoUserByall(list));
		}else {
			object.put("content", list);
		}
		return object;
	}
	
	@Override
	public String findStatusByUserId(long userId) {
		// TODO Auto-generated method stub
		String status = userRepository.queryUserStatusByUserId(userId);
		return status;
	}

	@Transactional
	@Override
	public void updateUserStatusByUserId(long userId) {
		userRepository.updateUserStatusByUserId(userId);
	}

	@Override
	public Brand findBrandByBrandid(long brandid) {
		return brandRepository.findBrandByBrand(brandid);
	}

	@Transactional
	@Override
	public void updateByAll(long brandManage, long userid, long brandid) {
		userRoleRepository.updateByAll(brandManage, userid, brandid);
	}

	@Override
	public List<UserRoleForm> findUserPreList(long brandid, String startTime, String endTime, final int size) {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer("select u.id as id ,u.phone as phone,u.fullname as fullname ,COUNT(u.pre_user_phone) as size from t_user u JOIN  t_user tu ON u.phone=tu.pre_user_phone where 1=1   ");
		if(brandid!=-1) {
			sql.append(" and u.brand_id="+brandid);
		}
		if(startTime!=null&&startTime.trim().length()>0) {
			sql.append("  and tu.create_time >="+startTime);
		}
		if(endTime!=null&&endTime.trim().length()>0) {
			sql.append("  and tu.create_time <="+endTime);
		}
		
		sql.append("GROUP BY u.phone ORDER BY COUNT(u.pre_user_phone) DESC LIMIT 0,"+size);
		List<UserRoleForm> list = jdbcTemplate.query(sql.toString(), new RowMapper<UserRoleForm>() {
			@Override
			public UserRoleForm mapRow(ResultSet rs, int rowNum) throws SQLException {
				UserRoleForm user = new UserRoleForm();
				user.setUserId(rs.getLong("id"));
				String phone=rs.getString("phone");
				if(phone.trim().length()>0&&phone.trim().length()==11) {
					phone=phone.substring(0, 3) + "****" + phone.substring(7, phone.length());
				}
				user.setPhone(phone );
				String nickName =rs.getString("fullname");
				if(nickName!=null&&(nickName.trim().length()==2||nickName.trim().length()>2)){
					if(nickName.trim().length()==2)
						nickName=nickName.substring(0, 1) + "*";
					if(nickName.trim().length()>3)
						nickName=nickName.substring(0, 1) + "*"+nickName.trim().substring(2, nickName.length());
				}
				user.setNickName(nickName);
				user.setSize(size);
				return user;
			}
		});
		return list;
	}

	@Override
	public Page<User> queryUserByIdIn(Long[] id, Pageable pageable) {
		return userRepository.queryUserByIdIn(id,pageable);
	}
	
	@Override
	public List<User> queryUserByIdIn(Long[] id){
		return userRepository.queryUserByIdIn(id);
	}

	@Override
	public User findByPhone(String phone) {
		return userRepository.findByPhone(phone);
	}

	@Override
	public List<User> findByIdInAndBrandId(Long[] ids, long brandid) {
		return userRepository.findByIdInAndBrandId(ids, brandid);
	}

	@Transactional
	@Override
	public void updatePreUserPhoneByPreUserId(String preUserPhone, long preUserId) {
		userRepository.updatePreUserPhoneByPreUserId(preUserPhone, preUserId);
		userOldRepository.updatePreUserPhoneByPreUserIdOld(preUserPhone, preUserId);
		em.flush();
	}

	@Override
	public List<User> queryUserByBrandIdAndRealname(Long brandId,String realnameStatus, Pageable pageable) {
		return userRepository.queryUserByBrandIdAndRealname(brandId,realnameStatus,pageable);
	}

	@Override
	public BigDecimal findBrandNumByBrandidAndRealAuth(Long brandId) {
		return userRepository.findBrandNumByBrandidAndRealAuth(brandId);
	}

	@Override
	public BigDecimal queryVipNumByBrandId(Long brandId) {
		return userRepository.queryVipNumByBrandId(brandId);
	}

	@Override
	public Page<User> queryUserByBrandIdAndOlder(Long brandId, Pageable pageable) {
		return userRepository.queryUserByBrandIdAndOlder(brandId,pageable);
	}

	@Override
	public Long[] queryUserIdBypreUserIdsAndCreateTime(Long[] userIds, String date) {
		return userRepository.queryUserIdBypreUserIdsAndCreateTime(userIds,date);
	}

	@Override
	public Long[] findUserIds() {
		return userRepository.findUserIds();
	}

	@Override
	public Long[] queryUserIdBypreUserIdsAndGradenew(String grade, Long[] downUserIds) {
		return userRepository.queryUserIdBypreUserIdsAndGradenew(grade,downUserIds);
	}

	@Override
	public Long[] queryUserIdBypreUserIdsAndrealnameStatusnew(String s, Long[] downUserIds) {
		return new Long[0];
	}

	@Override
	public List<User> findUserByBrandIdAndOlder(Long brandId) {
		return userRepository.findUserByBrandIdAndOlder(brandId);
	}

    @Override
    public int findVipCount(Long[] userIds) {
        return userRepository.findVipCount(userIds);
    }

}
