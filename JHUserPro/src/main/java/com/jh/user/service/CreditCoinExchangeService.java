package com.jh.user.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.user.business.CreditCoinExchangeBankBusiness;
import com.jh.user.business.CreditCoinExchangeOrderBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRealtionBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.CreditCoinExchangeBank;
import com.jh.user.pojo.CreditCoinExchangeOrder;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserRealtion;
import com.jh.user.util.AliOSSUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.FileUtils;
import cn.jh.common.utils.PhotoCompressUtil;
import cn.jh.common.utils.UUIDGenerator;

@Controller
@EnableAutoConfiguration
@RequestMapping("/v1.0/user")
public class CreditCoinExchangeService {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	private BrandMangeService brandMangeService;
	
	@Autowired
	private CreditCoinExchangeOrderBusiness creditCoinExchangeOrderBusiness;
	
	@Autowired
	private UserRealtionBusiness userRealtionBusiness;
	
	@Autowired
	private CreditCoinExchangeBankBusiness creditCoinExchangeBankBusiness;
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private AliOSSUtil aliOSSUtil;
	
	@Autowired
	private UserRebateService userRebateService;
	
	@Value("${user.exchangecoin.downloadpath}")
	private String downloadPath;
	
	@Value("${user.exchangecoin.uploadpath}")
	private String uploadPath;
	
//	查询银行兑换描述
	@RequestMapping(method=RequestMethod.POST,value="/query/bankcoin/description")
	public @ResponseBody Object getBankDescription(
			@RequestParam(value="bankCode")String bankCode
			){
		/*String[][] bankNames = this.getBanksLisk();
		for (int i = 0; i < bankNames.length; i++) {
			bankNames[i][2] = bankNames[i][2] + "积分起兑";
			bankNames[i][3] = "每1万积分价格为" + bankNames[i][3] + "元";
			bankNames[i][4] = "每1万积分价格为" + bankNames[i][4] + "元";
			bankNames[i][5] = "每1万积分价格为" + bankNames[i][5] + "元";
		}
		String[] result = null;
		for (int i = 0; i < bankNames.length; i++) {
			if (bankNames[i][6].equals(bankCode)) {
				result = bankNames[i];
			}
		}*/
		
		CreditCoinExchangeBank creditCoinExchangeBank = this.getByBankCode(bankCode);
		if (creditCoinExchangeBank == null) {
			return ResultWrap.init(CommonConstants.FALIED, "bankCode有误,请重新输入!");
		}
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("bankName", creditCoinExchangeBank.getBankName());
		resultMap.put("description",creditCoinExchangeBank.getBankDetail());
		resultMap.put("exchangeLimit", creditCoinExchangeBank.getMinExchangeCoin() + "积分起兑");
		resultMap.put("grade0", "每1万积分价格为" + creditCoinExchangeBank.getOneLevel() + "元");
		resultMap.put("grade1", "每1万积分价格为" + creditCoinExchangeBank.getTwoLevel() + "元");
		resultMap.put("grade2", "每1万积分价格为" + creditCoinExchangeBank.getThreeLevel() + "元");
		resultMap.put("bankCode", creditCoinExchangeBank.getBankCode());
		
		return ResultWrap.init(CommonConstants.SUCCESS,"查询成功", resultMap);
	}
//	计算出兑换金额
	@RequestMapping(method=RequestMethod.POST,value="/calculate/exchange/money")
	public @ResponseBody Object getExchangeMoney(
			@RequestParam(value="bankCode")String bankCode,
			@RequestParam(value="grade")String grade,
			@RequestParam(value="coin")String coin
			){
		/*String[][] bankNames = this.getBanksLisk();
		
		String[] result = null;
		for (int i = 0; i < bankNames.length; i++) {
			if (bankNames[i][6].equals(bankCode)) {
				result = bankNames[i];
			}
		}*/
		
		CreditCoinExchangeBank creditCoinExchangeBank = this.getByBankCode(bankCode);
		if (creditCoinExchangeBank == null) {
			return ResultWrap.init(CommonConstants.FALIED, "bankCode有误,请重新输入!");
		}
		
		if (Integer.valueOf(creditCoinExchangeBank.getMinExchangeCoin()) > Integer.valueOf(coin)) {
			String resultString = creditCoinExchangeBank.getBankName() +"最小积分起兑单位为:"+ creditCoinExchangeBank.getMinExchangeCoin() + "积分";
			return ResultWrap.init(CommonConstants.FALIED, "您要兑换的积分没有达到起兑数量!",resultString);
		}
		
		BigDecimal amountString = BigDecimal.ZERO;
		if ("0".equals(grade)) {
			amountString = creditCoinExchangeBank.getOneLevel();
		}else if ("1".equals(grade)) {
			amountString = creditCoinExchangeBank.getTwoLevel();
		}else if ("2".equals(grade)) {
			amountString = creditCoinExchangeBank.getThreeLevel();
		}else if("3".equals(grade)) {
			amountString = creditCoinExchangeBank.getFourLevel();

		}
		BigDecimal amount = new BigDecimal(coin).divide(BigDecimal.valueOf(10000)).multiply(amountString).setScale(2,BigDecimal.ROUND_DOWN);
		
		return ResultWrap.init(CommonConstants.SUCCESS, "计算成功", amount);
	}
//	提交兑换订单
	@RequestMapping(method=RequestMethod.POST,value="/upload/exchange/order")
	public @ResponseBody Object uploadExchangeOrder(HttpServletRequest request,
			@RequestParam(value="userId")String userId,
			@RequestParam(value="exchangeKey")String exchangeKey,
			@RequestParam(value="bankCode")String bankCode,
			@RequestParam(value="orderType",required=false)String orderType,
			@RequestParam(value="remark",required=false)String remark
			){
		String contentType = request.getContentType();
		Map<String, Object> verifyStringFiledIsNull = AuthorizationHandle.verifyStringFiledIsNull(exchangeKey);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNull.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNull;
		}
		
		
		try {
			if (orderType != null) {
				orderType = URLDecoder.decode(orderType,"UTF-8");
			}
			if (remark != null) {
				remark = URLDecoder.decode(remark,"UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();LOG.error("",e);
		}
		
		/*String[][] bankNames = this.getBanksLisk();
		String[] result = null;
		for (int i = 0; i < bankNames.length; i++) {
			if (bankNames[i][6].equals(bankCode)) {
				result = bankNames[i];
			}
		}*/
		
		CreditCoinExchangeBank creditCoinExchangeBank = this.getByBankCode(bankCode);
		if (creditCoinExchangeBank == null) {
			return ResultWrap.init(CommonConstants.FALIED, "bankCode有误,请重新输入!");
		}
		
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		if (user == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该用户不存在!");
		}
		
		CreditCoinExchangeOrder creditCoinExchangeOrder = new CreditCoinExchangeOrder();
		creditCoinExchangeOrder.setPhone(user.getPhone());
		creditCoinExchangeOrder.setExchangeGrade(user.getGrade());
		creditCoinExchangeOrder.setBankName(creditCoinExchangeBank.getBankName());
		creditCoinExchangeOrder.setBankCode(bankCode);
		creditCoinExchangeOrder.setExchangeKey(exchangeKey);
		creditCoinExchangeOrder.setUserId(userId);
		creditCoinExchangeOrder.setBrandId(user.getBrandId()+"");
		creditCoinExchangeOrder.setOrderCode(UUIDGenerator.getDateTimeOrderCode());
		creditCoinExchangeOrder.setOrderType(orderType);
		creditCoinExchangeOrder.setRemark(remark);
		creditCoinExchangeOrder.setUpdateTime(new Date());
		creditCoinExchangeOrder.setCreateTime(new Date());
		creditCoinExchangeOrder = creditCoinExchangeOrderBusiness.save(creditCoinExchangeOrder);
		
		if (contentType.contains("multipart/form-data")) {
			String ossObjectNamePrefix = AliOSSUtil.EXCHANGECOIN + "-" + user.getBrandId() + "-" + user.getPhone() + "-" + creditCoinExchangeOrder.getOrderCode() + "-";
			String ossObjectName = "";
			
			String uploadPaths = uploadPath + "/"+userId+"/"+creditCoinExchangeOrder.getOrderCode();
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> files = multipartRequest.getFiles("image");
			if (files != null && files.size() > 0) {
				File dir = new File(uploadPaths);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				int index = 1;
				for (MultipartFile file : files) {
					String fileName = file.getOriginalFilename();
					fileName = fileName.substring(fileName.lastIndexOf("."), fileName.length());
					fileName = index + fileName;
					ossObjectName = ossObjectNamePrefix + fileName;
					
					OutputStream os = new ByteArrayOutputStream();
					ByteArrayInputStream inputStream = null;
					try {
						PhotoCompressUtil.compressPhoto(file.getInputStream(), os, 0.2f);
						inputStream = FileUtils.parse(os);
						aliOSSUtil.uploadStreamToOss(ossObjectName,inputStream);
					} catch (Exception e1) {
						e1.printStackTrace();LOG.error(ExceptionUtil.errInfo(e1));
					}finally{
						try {
							os.close();
						} catch (IOException e) {
							e.printStackTrace();
							os = null;
						}
						try {
							inputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
							inputStream = null;
						}
					}
					
//					File dest = new File(uploadPaths + "/" + fileName);
//			    	dest.setExecutable(true, false);
//			    	dest.setReadable(true, false);
//			    	dest.setWritable(true, false);
//			    	dest.setWritable(true);
//			    	dest.setExecutable(true);
//			    	dest.setReadable(true);
//					try {
//						file.transferTo(dest);
//						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
//					} catch (IllegalStateException e) {
//						e.printStackTrace();
//						LOG.error("",e);
//					} catch (IOException e) {
//						e.printStackTrace();
//						LOG.error("",e);
//					}
					index ++;
				}
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "下单成功", creditCoinExchangeOrder);
	}
//	读取订单图片
	@RequestMapping(method=RequestMethod.POST,value="/download/exchange/order/photo")
	public @ResponseBody Object getExchangeOrderPhoto(HttpServletRequest request,
			@RequestParam(value="orderCode")String orderCode
			){
		String downloadPaths = downloadPath;
		String uploadPaths = uploadPath;
		CreditCoinExchangeOrder creditCoinExchangeOrder = creditCoinExchangeOrderBusiness.findByOrderCodeAndExchangeType(orderCode, 0);
		if (creditCoinExchangeOrder == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该订单不存在!");
		}
		
		String ossObjectNamePrefix = AliOSSUtil.EXCHANGECOIN + "-" + creditCoinExchangeOrder.getBrandId() + "-" + creditCoinExchangeOrder.getPhone() + "-" + creditCoinExchangeOrder.getOrderCode() + "-";
		String ossObjectName = "";
		List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
		List<String> photoUrl = new ArrayList<String>();
		if (listFiles != null && listFiles.size() >0) {
			for (String fileName : listFiles) {
				String fileUrl = aliOSSUtil.getFileUrl(fileName);
				photoUrl.add(fileUrl);
			}
		}else {
			return ResultWrap.init(CommonConstants.FALIED, "该订单无图片!",new ArrayList<String>());
		}
		
		/*String userId = creditCoinExchangeOrder.getUserId();
		uploadPaths = uploadPaths + "/" + userId + "/" + orderCode;
		downloadPaths = downloadPaths + "/" + userId + "/" + orderCode;
		File file = new File(uploadPaths);
		if (!file.exists() || file.list().length == 0) {
			return ResultWrap.init(CommonConstants.FALIED, "该订单无图片!",new ArrayList<String>());
		}
		for (int i = 0; i < file.list().length; i++) {
			photoUrl.add(downloadPaths + "/" +file.list()[i]);
		}*/
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", photoUrl);
	}
//	审核订单
	@RequestMapping(method=RequestMethod.POST,value="/update/exchange/order")
	public @ResponseBody Object updateExchangeOrder(HttpServletRequest request,
			@RequestParam(value="orderStatus")String orderStatus,
			@RequestParam(value="orderId")String orderId,
			@RequestParam(value="exchangeCoin",required=false)String exchangeCoin,
			@RequestParam(value="remark",required=false)String remark
			){
		CreditCoinExchangeOrder creditCoinExchangeOrder = creditCoinExchangeOrderBusiness.findById(orderId);
		
		if (creditCoinExchangeOrder == null) {
			return ResultWrap.init(CommonConstants.FALIED, "无该订单!");
		}
		
		if (creditCoinExchangeOrder.getOrderStatus() != 0) {
			return ResultWrap.init(CommonConstants.FALIED, "该订单已处理!");
		}
		
		Map<String,Object> brandMap = (Map<String, Object>) brandMangeService.queryBrandById(request, Long.valueOf(creditCoinExchangeOrder.getBrandId()));
		Brand brand = (Brand) brandMap.get(CommonConstants.RESULT);
		
		Map<String, Object> exchangeMoney = (Map<String, Object>) this.getExchangeMoney(creditCoinExchangeOrder.getBankCode(), "3", exchangeCoin);
		BigDecimal totalRetrunAmount = (BigDecimal) exchangeMoney.get(CommonConstants.RESULT);
		
		boolean greaterThanAmount = this.isGreaterThanAmount(request, brand.getManageid()+"", totalRetrunAmount);
		if (!greaterThanAmount) {
			return ResultWrap.init(CommonConstants.FALIED, "账户余额不足,无法进行审核,请及时充值!");
		}
		
		creditCoinExchangeOrder.setOrderStatus(Integer.valueOf(orderStatus));
		creditCoinExchangeOrder.setUpdateTime(new Date());
		creditCoinExchangeOrder = creditCoinExchangeOrderBusiness.save(creditCoinExchangeOrder);

		if (!"1".equals(orderStatus)) {
			creditCoinExchangeOrder.setRemark(remark);
			creditCoinExchangeOrder = creditCoinExchangeOrderBusiness.save(creditCoinExchangeOrder);
			return ResultWrap.init(CommonConstants.SUCCESS, "拒绝成功!", creditCoinExchangeOrder);
		}
		
		
		if (creditCoinExchangeOrder.getExchangeType() == 0) {
			String bankCode = creditCoinExchangeOrder.getBankCode();
			String bankName = creditCoinExchangeOrder.getBankName();
			String orderCode = creditCoinExchangeOrder.getOrderCode();
			String exchangeGrade = creditCoinExchangeOrder.getExchangeGrade();
			String exchangeKey = creditCoinExchangeOrder.getExchangeKey();
			String userId = creditCoinExchangeOrder.getUserId();
			exchangeMoney = (Map<String, Object>) this.getExchangeMoney(bankCode, creditCoinExchangeOrder.getExchangeGrade(), exchangeCoin);
			if (!CommonConstants.SUCCESS.equals(exchangeMoney.get(CommonConstants.RESP_CODE))) {
				return exchangeMoney;
			}
			BigDecimal firstAmount = (BigDecimal) exchangeMoney.get(CommonConstants.RESULT);
			
			creditCoinExchangeOrder.setExchangeCoin(exchangeCoin);
			creditCoinExchangeOrder.setExchangeMoney(firstAmount);
			Map<String, Object> subtractAccountBlanceAndAddAccountRebate = this.subtractAccountBlanceAndAddAccountRebate(request, brand.getManageid()+"", userId, firstAmount, orderCode);
			if (!CommonConstants.SUCCESS.equals(subtractAccountBlanceAndAddAccountRebate.get(CommonConstants.RESP_CODE))) {
				return subtractAccountBlanceAndAddAccountRebate;
			}
			CreditCoinExchangeOrder coinExchangeOrder = creditCoinExchangeOrderBusiness.save(creditCoinExchangeOrder);
			
			String lastGrade = exchangeGrade;
			if (Integer.valueOf(exchangeGrade).intValue() < 3) {
				List<UserRealtion> perentList = userRealtionBusiness.findByFirstUserId(Long.valueOf(userId));
				if (perentList != null && perentList.size()>0) {
					for (int i = 0; i < perentList.size(); i++) {
						User user = userLoginRegisterBusiness.queryUserById(perentList.get(i).getPreUserId());
						String grade = user.getGrade();
						if (Integer.valueOf(grade).intValue() <= Integer.valueOf(lastGrade).intValue()) {
							continue;
						}else if (Integer.valueOf(lastGrade).intValue() == 3) {
							break;
						}
						
						exchangeMoney = (Map<String, Object>) this.getExchangeMoney(bankCode,grade,exchangeCoin);
						BigDecimal retrunAmount = (BigDecimal) exchangeMoney.get(CommonConstants.RESULT);
						retrunAmount = retrunAmount.subtract(firstAmount);
						coinExchangeOrder = new CreditCoinExchangeOrder();
						coinExchangeOrder.setExchangeType(1);
						coinExchangeOrder.setOrderCode(orderCode);
						coinExchangeOrder.setExchangeCoin(exchangeCoin);
						coinExchangeOrder.setExchangeKey(exchangeKey);
						coinExchangeOrder.setExchangeGrade(grade);
						coinExchangeOrder.setExchangeMoney(retrunAmount);
						coinExchangeOrder.setBankName(bankName);
						coinExchangeOrder.setBankCode(bankCode);
						coinExchangeOrder.setPhone(user.getPhone());
						coinExchangeOrder.setUserId(user.getId()+"");
						coinExchangeOrder.setRemark("获得下级兑换积分返利");
						coinExchangeOrder.setUpdateTime(new Date());
						coinExchangeOrder.setCreateTime(new Date());
						coinExchangeOrder.setOrderStatus(1);
						subtractAccountBlanceAndAddAccountRebate = this.subtractAccountBlanceAndAddAccountRebate(request, brand.getManageid()+"", user.getId()+"", retrunAmount, orderCode);
						if (!CommonConstants.SUCCESS.equals(subtractAccountBlanceAndAddAccountRebate.get(CommonConstants.RESP_CODE))) {
							return subtractAccountBlanceAndAddAccountRebate;
						}
						coinExchangeOrder = creditCoinExchangeOrderBusiness.save(coinExchangeOrder);
						firstAmount = retrunAmount;
						lastGrade = grade;
					}
				}
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "审核成功", creditCoinExchangeOrder);
	}
//	查询所有订单
	@RequestMapping(method=RequestMethod.POST,value="/list/all/exchange/order")
	public @ResponseBody Object listAllExchangeOrder(HttpServletRequest request,
//			orderStatus:0 未完成  1:已成功 2:已失败 3:全部
			@RequestParam(value="orderStatus",required=false,defaultValue="0")String orderStatus,
			@RequestParam(value="exchangeType",required=false,defaultValue="0")int exchangeType,
			@RequestParam(value="brandId")String brandId,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<CreditCoinExchangeOrder> creditCoinExchangeOrders = creditCoinExchangeOrderBusiness.findByOrderStatusAndExchangeTypeAndBrandId(orderStatus,exchangeType,brandId,pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",creditCoinExchangeOrders);
	}
//	查询用户订单
	@RequestMapping(method=RequestMethod.POST,value="/list/user/exchange/order")
	public @ResponseBody Object listUserExchangeOrder(HttpServletRequest request,
//			orderStatus:0 未完成  1:已成功 2:已失败 3:全部
			@RequestParam(value="orderStatus",required=false,defaultValue="0")String orderStatus,
			@RequestParam(value="exchangeType",required=false,defaultValue="0")int exchangeType,
			@RequestParam(value="userId")String userId,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		if (user == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该用户不存在!");
		}
		Page<CreditCoinExchangeOrder> creditCoinExchangeOrders = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId(orderStatus,userId,exchangeType,user.getBrandId()+"",pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",creditCoinExchangeOrders);
	}
//	查询用户订单数量
	@RequestMapping(method=RequestMethod.POST,value="/list/user/exchange/order/count")
	public @ResponseBody Object getUserOrderCount(HttpServletRequest request,
			@RequestParam(value="userId")String userId,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		if (user == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该用户不存在!");
		}
		Page<CreditCoinExchangeOrder> creditCoinExchangeOrders0 = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId("0", userId, 0, user.getBrandId()+"",pageable);
		Page<CreditCoinExchangeOrder> creditCoinExchangeOrders1 = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId("0", userId, 1, user.getBrandId()+"",pageable);
		long uncompletedCount = creditCoinExchangeOrders0.getTotalElements();
		uncompletedCount = uncompletedCount + creditCoinExchangeOrders1.getTotalElements();
		
		creditCoinExchangeOrders0 = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId("1", userId, 0, user.getBrandId()+"",pageable);
		creditCoinExchangeOrders1 = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId("1", userId, 1, user.getBrandId()+"",pageable);
		long completedCount = creditCoinExchangeOrders0.getTotalElements();
		completedCount = completedCount + creditCoinExchangeOrders1.getTotalElements();
		
		creditCoinExchangeOrders0 = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId("2", userId, 0, user.getBrandId()+"",pageable);
		creditCoinExchangeOrders1 = creditCoinExchangeOrderBusiness.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId("2", userId, 1, user.getBrandId()+"",pageable);
		long failedCount = creditCoinExchangeOrders0.getTotalElements();
		failedCount = failedCount + creditCoinExchangeOrders1.getTotalElements();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("uncompletedCount", uncompletedCount);
		map.put("completedCount", completedCount);
		map.put("failedCount", failedCount);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", map);
	}
	
//	根据orderCode查询订单
	@RequestMapping(method=RequestMethod.POST,value="/find/exchange/order/by/ordercode")
	public @ResponseBody Object getUserOrderCount(HttpServletRequest request,
			@RequestParam(value="orderCode")String orderCode,
			@RequestParam(value="exchangeType",required=false,defaultValue="0")int exchangeType
			){
		List<CreditCoinExchangeOrder> coinExchangeOrders = null;
		if ("0".equals(exchangeType)) {
			CreditCoinExchangeOrder coinExchangeOrder = creditCoinExchangeOrderBusiness.findByOrderCodeAndExchangeType(orderCode, exchangeType);
			if (coinExchangeOrder != null) {
				coinExchangeOrders = new ArrayList<CreditCoinExchangeOrder>();
				coinExchangeOrders.add(coinExchangeOrder);
			}
		}else {
			coinExchangeOrders = creditCoinExchangeOrderBusiness.findListByOrderCodeAndExchangeType(orderCode, exchangeType);
		}
		
	return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", coinExchangeOrders);	
	}
	
	private String[][] getBanksLisk(){
		String[][] bankNames = new String[16][7];
		bankNames[0][0] = "工商银行";
		bankNames[1][0] = "建设银行";
		bankNames[2][0] = "中国银行";
		bankNames[3][0] = "招商银行-星巴克";
		bankNames[4][0] = "招商银行-积分全清";
		bankNames[5][0] = "交通银行-星巴克";
		bankNames[6][0] = "光大银行";
		bankNames[7][0] = "浦发银行";
		bankNames[8][0] = "中信银行";
		bankNames[9][0] = "平安银行";
		bankNames[10][0] = "中国移动";
		bankNames[11][0] = "中国联通";
		bankNames[12][0] = "北京银行";
		bankNames[13][0] = "汇丰银行";
		bankNames[14][0] = "华夏银行";
		bankNames[15][0] = "平安小额";

		bankNames[0][1] = "【工商银行】拨打工商银行信用卡电话：95588，转人工服务，向工商银行信用卡中心的客服提出查询积分的要求即可。";
		bankNames[1][1] = "【建设银行】使用银行预留的手机号码，编辑短信“CXJF”发到95533，查询积分。、";
		bankNames[2][1] = "【中国银行】使用银行预留的手机号码，编辑短信“17#卡号后四位”发送到号码95566";
		bankNames[3][1] = "【招商银行-星巴克】使用银行预留的手机号码，联通、电信用户发送#JF到95555，移动用户发送#JF到1065795555";
		bankNames[4][1] = "【招商银行-积分全清】使用银行预留的手机号码，联通、电信用户发送#JF到95555，移动用户发送#JF到1065795555";
		bankNames[5][1] = "【交通银行-星巴克】使用银行预留的手机号码，编辑短信“cc积分#卡号后四位”发送至95559";
		bankNames[6][1] = "【光大银行】使用银行预留的手机号码，发送“积分”至95595查询积分余额";
		bankNames[7][1] = "【浦发银行】使用银行预留的手机号码，编辑短信“JFCX+空格+卡号后四位”发送至95528（+号不用填写）";
		bankNames[8][1] = "【中信银行】使用银行预留的手机号码，编辑短信“JF卡号末四位”，移动联通电信用户发送短信到106980095558";
		bankNames[9][1] = "【平安银行】拨打平安银行信用卡客服电话：95511转2，转人工服务，要求客服专员为您查询平安银行信用卡积分情况";
		bankNames[10][1] = "【中国移动】发送短信JF到10086,即可查询到号码截至到上个月月底产生的积分";
		bankNames[11][1] = "【中国联通】发送105或cxjf到10010即可查询积分，如无法查询，请拨打电话查询 ";
		bankNames[12][1] = "【北京银行】主卡持卡人可致电北京银行信用卡中心客户服务热线400-660-1169查询即时累计积分 ";
		bankNames[13][1] = "【汇丰银行】使用银行预留手机号拨打汇丰中国信用卡24小时客服热线4008695366，转人工服务查询。";
		bankNames[14][1] = "【华夏银行】使用银行预留手机号拨打华夏信用卡24小时客服热线4006695577，根据语音提示找到积分查询。5";
		bankNames[15][1] = "【平安小额】拨打平安银行信用卡客服电话：95511转2，转人工服务，要求客服专员为您查询平安银行信用卡积分情况";

		bankNames[0][2] = "10000";
		bankNames[1][2] = "54000";
		bankNames[2][2] = "5000";
		bankNames[3][2] = "799";
		bankNames[4][2] = "999";
		bankNames[5][2] = "21000";
		bankNames[6][2] = "57800";
		bankNames[7][2] = "55200";
		bankNames[8][2] = "95000";
		bankNames[9][2] = "50000";
		bankNames[10][2] = "2850";
		bankNames[11][2] = "1020";
		bankNames[12][2] = "6000";
		bankNames[13][2] = "27500";
		bankNames[14][2] = "60000";
		bankNames[15][2] = "5000";

		bankNames[0][3] = "9.00";
		bankNames[1][3] = "13.00";
		bankNames[2][3] = "16.00";
		bankNames[3][3] = "220.00";
		bankNames[4][3] = "120.00";
		bankNames[5][3] = "8.00";
		bankNames[6][3] = "6.40";
		bankNames[7][3] = "4.70";
		bankNames[8][3] = "5.30";
		bankNames[9][3] = "16.40";
		bankNames[10][3] = "72.00";
		bankNames[11][3] = "62.00";
		bankNames[12][3] = "13.30";
		bankNames[13][3] = "13.10";
		bankNames[14][3] = "4.80";
		bankNames[15][3] = "16.00";

		bankNames[0][4] = "10.00";
		bankNames[1][4] = "14.00";
		bankNames[2][4] = "16.50";
		bankNames[3][4] = "240.00";
		bankNames[4][4] = "125.00";
		bankNames[5][4] = "8.60";
		bankNames[6][4] = "6.80";
		bankNames[7][4] = "5.00";
		bankNames[8][4] = "5.90";
		bankNames[9][4] = "16.80";
		bankNames[10][4] = "75.00";
		bankNames[11][4] = "68.00";
		bankNames[12][4] = "13.80";
		bankNames[13][4] = "14.40";
		bankNames[14][4] = "5.40";
		bankNames[15][4] = "16.80";

		bankNames[0][5] = "12.00";
		bankNames[1][5] = "16.00";
		bankNames[2][5] = "17.00";
		bankNames[3][5] = "260.00";
		bankNames[4][5] = "130.00";
		bankNames[5][5] = "9.00";
		bankNames[6][5] = "7.20";
		bankNames[7][5] = "5.40";
		bankNames[8][5] = "6.30";
		bankNames[9][5] = "17.60";
		bankNames[10][5] = "80.00";
		bankNames[11][5] = "72.00";
		bankNames[12][5] = "14.20";
		bankNames[13][5] = "16.40";
		bankNames[14][5] = "5.80";
		bankNames[15][5] = "17.60";

		bankNames[0][6] = "GSYH";
		bankNames[1][6] = "JSYH";
		bankNames[2][6] = "ZGYH";
		bankNames[3][6] = "ZSXBK";
		bankNames[4][6] = "ZSJFQQ";
		bankNames[5][6] = "JTXBK";
		bankNames[6][6] = "GDYH";
		bankNames[7][6] = "PFYH";
		bankNames[8][6] = "ZXYH";
		bankNames[9][6] = "PAYH";
		bankNames[10][6] = "ZGYD";
		bankNames[11][6] = "ZGLT";
		bankNames[12][6] = "BJYH";
		bankNames[13][6] = "HFYH";
		bankNames[14][6] = "HXYH";
		bankNames[15][6] = "PAXE";
		
		return bankNames;
	}

	private CreditCoinExchangeBank getByBankCode(String bankCode){
		return creditCoinExchangeBankBusiness.findByBankCode(bankCode);
	}
	
	/**
	 * 判断余额是否充足
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param request
	 * @param userId
	 * @param amount
	 * @return
	 */
	private boolean isGreaterThanAmount(HttpServletRequest request,String userId,BigDecimal amount){
		Map<String, Object> map = (Map<String, Object>) userBalanceService.queryUserAccountByUserId(request, Long.valueOf(userId));
		if (!CommonConstants.SUCCESS.equals(map.get(CommonConstants.RESP_CODE))) {
			return false;
		}
		
		UserAccount userAccount = (UserAccount) map.get(CommonConstants.RESULT);
		if (userAccount.getBalance().compareTo(amount) < 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 减少一个用户的余额,增加另一个用户的分润
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param request
	 * @param grantUserId
	 * @param acqUserId
	 * @param amount
	 * @param orderCode
	 * @return
	 */
	private Map<String,Object> subtractAccountBlanceAndAddAccountRebate(HttpServletRequest request,String grantUserId,String acqUserId,BigDecimal amount,String orderCode){
		Map<String, Object> map = this.subtractAccountBlance(request, grantUserId, amount, orderCode);
		if (!CommonConstants.SUCCESS.equals(map.get(CommonConstants.RESP_CODE))) {
			return map;
		}
		this.addAccountRebate(request, acqUserId, amount, orderCode);
		return ResultWrap.init(CommonConstants.SUCCESS, "发放分润成功!");
	}
	
	/**
	 * 减少用户余额
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param request
	 * @param userId
	 * @param amount
	 * @param orderCode
	 * @return
	 */
	private Map<String,Object> subtractAccountBlance(HttpServletRequest request,String userId,BigDecimal amount,String orderCode){
		boolean greaterThanAmount = this.isGreaterThanAmount(request, userId, amount);
		if (!greaterThanAmount) {
			return ResultWrap.init(CommonConstants.FALIED, "账户余额不足,无法下发分润!");
		}
		return (Map<String, Object>) userBalanceService.updateUserAccount(request, Long.valueOf(userId), amount, "1", orderCode);
	}
	
	/**
	 * 发放用户分润
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param request
	 * @param userId
	 * @param amount
	 * @param orderCode
	 */
	private void addAccountRebate(HttpServletRequest request,String userId,BigDecimal amount,String orderCode){
		userRebateService.updateUserAccount(request, Long.valueOf(userId), amount, orderCode, "0", CommonConstants.ORDER_TYPE_EXCHANGE_COIN);
	}

}
