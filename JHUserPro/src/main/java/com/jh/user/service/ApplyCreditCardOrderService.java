package com.jh.user.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.user.business.ApplyCreditCardOrderBusiness;
import com.jh.user.business.ThirdLeveDistributionBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRealtionBusiness;
import com.jh.user.pojo.ApplyCreditCardOrder;
import com.jh.user.pojo.ApplyCreditCardOrderExcelModel;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.ThirdLevelDistribution;
import com.jh.user.pojo.ThirdLevelRebateRatioNew;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserRealtion;
import com.jh.user.util.ExcelReader;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExcelUtil;
import cn.jh.common.utils.UUIDGenerator;

@Controller
@EnableAutoConfiguration
@RequestMapping(value="/v1.0/user")
public class ApplyCreditCardOrderService {
	
	@Autowired
	private ApplyCreditCardOrderBusiness applyCreditCardOrderBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	private ThirdLeveDistributionBusiness thirdLeveDistributionBusiness;
	
	@Autowired
	private UserRealtionBusiness userRealtionBusiness;
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	@Autowired
	private BrandMangeService brandMangeService;
	
	@Autowired
	private UserRebateService userRebateService;
	
	@RequestMapping(method=RequestMethod.POST,value="/put/apply/credit/card/order")
	public @ResponseBody Object putApplyCreditCardOrder(
			@RequestParam(value="userId")String userId,
//			姓名
			@RequestParam(value="name")String name,
//			身份证号
			@RequestParam(value="idcard")String idcard,
//			手机号
			@RequestParam(value="phone")String phone,
//			银行名称
			@RequestParam(value="bankName")String bankName
			){
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		if(user == null){
			return ResultWrap.init(CommonConstants.FALIED, "未查询到该用户!");
		}
		
		ApplyCreditCardOrder applyCreditCardOrder = applyCreditCardOrderBusiness.findByBankNameAndIdcardAndOrderStatus(bankName,idcard,0);
		if (applyCreditCardOrder != null) {
			return ResultWrap.init(CommonConstants.FALIED, "已有该身份证号待审核申请,请等待审核后再提交!");
		}
		
		String regex1 = "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
		String regex2 = "^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$";
		if(!idcard.matches(regex1) && !idcard.matches(regex2)){
			return ResultWrap.init(CommonConstants.FALIED, "身份证号有误,请检查是否正确!");
		}
		
		applyCreditCardOrder = new ApplyCreditCardOrder();
		applyCreditCardOrder.setCreateTime(new Date());
		applyCreditCardOrder.setIdcard(idcard);
		applyCreditCardOrder.setUserId(Integer.valueOf(userId));
		applyCreditCardOrder.setName(name);
		applyCreditCardOrder.setBankName(bankName);
		applyCreditCardOrder.setPhone(phone);
		applyCreditCardOrder.setOrderCode(UUIDGenerator.getDateTimeOrderCode());
		applyCreditCardOrder.setBrandId(user.getBrandId());
		applyCreditCardOrder = applyCreditCardOrderBusiness.save(applyCreditCardOrder);
		return ResultWrap.init(CommonConstants.SUCCESS, "提交成功!", applyCreditCardOrder);
	}
	
//	请求所有申请订单的分类
	@RequestMapping(value="/get/all/apply/credit/card/order/bankname")
	public @ResponseBody Object getApplyCreditCardOrderGoupByBankName() {
		List<ApplyCreditCardOrder> applyCreditCardOrders =  applyCreditCardOrderBusiness.findGroupByBankName();
		List<String> bankNames = new ArrayList<>();
		for (ApplyCreditCardOrder applyCreditCardOrder : applyCreditCardOrders) {
			bankNames.add(applyCreditCardOrder.getBankName());
		}
		bankNames = bankNames.stream().filter(str -> str != null).collect(Collectors.toList());
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功 ",bankNames);
	}
	
//	根据条件筛选获取申请订单
	@RequestMapping(value="/get/apply/credit/card/order")
	public @ResponseBody Object getApplyCreditCardOrder(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="brandId")String brandId,
//			orderStatus: 0 待审核申请;1 审核成功申请;2 审核拒绝申请 3:全部申请订单
			@RequestParam(value="orderStatus",required=false,defaultValue="0")String orderStatus,
			@RequestParam(value="orderCode",required=false)String orderCode,
			@RequestParam(value="name",required=false)String name,
			@RequestParam(value="phone",required=false)String phone,
			String bankName,
			String isDownload,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<ApplyCreditCardOrder> applyCreditCardOrder = null;
		/*if (!this.isEmpty(name) && this.isEmpty(phone)) {
			applyCreditCardOrder = applyCreditCardOrderBusiness.findByNameLike(name, pageable);
		} else if (this.isEmpty(name) && !this.isEmpty(phone)) {
			applyCreditCardOrder = applyCreditCardOrderBusiness.findByPhoneLike(phone, pageable);
		} else if (!this.isEmpty(name) && !this.isEmpty(phone)) {
			applyCreditCardOrder = applyCreditCardOrderBusiness.findByNameLikeAndPhoneLike(name,phone,pageable);
		} else if (this.isEmpty(name) && this.isEmpty(phone)) {
			applyCreditCardOrder = applyCreditCardOrderBusiness.findByBrandIdAndOrderStatus(Long.valueOf(brandId),Integer.valueOf(orderStatus), orderCode, pageable);
		}*/
		applyCreditCardOrder = applyCreditCardOrderBusiness.findByConditio(brandId,orderStatus,orderCode,name,phone,bankName,pageable);
		if (isDownload == null || !"1".equals(isDownload)) {
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", applyCreditCardOrder);
		}else if(applyCreditCardOrder != null && applyCreditCardOrder.getContent().size() > 0){
			try {
				File file = new File("/temp.xlsx");
				if (!file.exists()) {
					file.createNewFile();
				}
				String[] names = new String[] {"订单号","姓名","身份证号","手机号","申请类别","返利金额","订单状态(0:待审核,1:已成功,2:已拒绝)","申请时间"};
				List<ApplyCreditCardOrder> content = applyCreditCardOrder.getContent();
				List<ApplyCreditCardOrderExcelModel> excelModels = new ArrayList<>();
				for (ApplyCreditCardOrder applyCreditCardOrder2 : content) {
					ApplyCreditCardOrderExcelModel model = new ApplyCreditCardOrderExcelModel();
					BeanUtils.copyProperties(applyCreditCardOrder2, model);
					excelModels.add(model);
				}
				new ExcelUtil().writeExcel(file, excelModels,Arrays.asList(names));
				FileInputStream  fis = new FileInputStream(file);  
				String filename=URLEncoder.encode(file.getName(),"utf-8"); //解决中文文件名下载后乱码的问题  
				byte[] b = new byte[fis.available()];  
				fis.read(b);  
				fis.close();
				response.setCharacterEncoding("utf-8");  
				response.setHeader("Content-Disposition","attachment; filename="+filename+"");  
				//获取响应报文输出流对象  
				ServletOutputStream  out =response.getOutputStream();  
				//输出  
				out.write(b);  
				out.flush();  
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
				return ResultWrap.init(CommonConstants.FALIED, "下载失败");
			}  
			return null;
		}else {
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据");
		}
		
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/get/apply/credit/card/order/by/userid")
	public @ResponseBody Object getApplyCreditCardOrderByuserId(
			@RequestParam(value="userId")String userId,
//			orderStatus: 0 待审核申请;1 审核成功申请;2 审核拒绝申请 3:全部申请订单
			@RequestParam(value="orderStatus",required=false,defaultValue="0")String orderStatus,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<ApplyCreditCardOrder> applyCreditCardOrder = applyCreditCardOrderBusiness.findByUserId(Integer.valueOf(userId),Integer.valueOf(orderStatus),pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", applyCreditCardOrder);
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/update/apply/credit/card/order")
	public @ResponseBody Object updateApplyCreditCardOrder(HttpServletRequest request,
			@RequestParam(value="orderCode")String orderCode,
//			orderStatus: 1 审核成功,此时需传rebate, 2 审核拒绝
			@RequestParam(value="orderStatus")String orderStatus,
			@RequestParam(value="rebate",required=false)String rebate
			){
		ApplyCreditCardOrder applyCreditCardOrder = applyCreditCardOrderBusiness.findByOrderCode(orderCode);
		if(applyCreditCardOrder == null){
			return ResultWrap.init(CommonConstants.FALIED, "无该订单号订单!");
		}
		
		if(1 == applyCreditCardOrder.getOrderStatus()){
			return ResultWrap.init(CommonConstants.FALIED, "订单已成功!");
		}
		
		int userId = applyCreditCardOrder.getUserId();
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		String grade = user.getGrade();
		if("1".equals(orderStatus)){
			long manageid;
			if(rebate == null || "".equals(rebate)){
				return ResultWrap.init(CommonConstants.FALIED, "返利金额不能为空");
			}else{
				Map<String,Object> brandMap = (Map<String, Object>) brandMangeService.queryBrandById(request,user.getBrandId());
				Brand brand = (Brand) brandMap.get(CommonConstants.RESULT);
				manageid = brand.getManageid();
				boolean greaterThanAmount = this.isGreaterThanAmount(request, manageid+"", new BigDecimal(rebate));
				if (!greaterThanAmount) {
					return ResultWrap.init(CommonConstants.FALIED, "账户余额不足,无法进行审核,请及时充值!");
				}
			}
			
			ThirdLevelDistribution thirdLevelDistribution = thirdLeveDistributionBusiness.getThirdLevelByBrandidandgrade(user.getBrandId(), (Integer.valueOf(grade)==0?1:Integer.valueOf(grade)));
			List<ThirdLevelRebateRatioNew> allThirdRatio = thirdLeveDistributionBusiness.getAllThirdRatio(user.getBrandId(), Integer.valueOf(thirdLevelDistribution.getId()+""));
			Map<String,BigDecimal> thirdLevelRebateRatioNews = new HashMap<>();
			for (ThirdLevelRebateRatioNew thirdLevelRebateRatioNew : allThirdRatio) {
				thirdLevelRebateRatioNews.put(thirdLevelRebateRatioNew.getPreLevel(), thirdLevelRebateRatioNew.getCreditRatio());
			}
			BigDecimal firstRatio = BigDecimal.ZERO;
			if (!"0".equals(grade.trim())) {
				firstRatio = thirdLevelRebateRatioNews.get(grade);
				BigDecimal realRebate = firstRatio.multiply(new BigDecimal(rebate));
				if(BigDecimal.ZERO.compareTo(realRebate) < 0){
					Map<String, Object> subtractAccountBlanceAndAddAccountRebate = this.subtractAccountBlanceAndAddAccountRebate(request, manageid+"", userId+"", realRebate, orderCode+0);
					if (!CommonConstants.SUCCESS.equals(subtractAccountBlanceAndAddAccountRebate.get(CommonConstants.RESP_CODE))) {
						return subtractAccountBlanceAndAddAccountRebate;
					}
					System.out.println("=====userId:"+userId+"=====获得分润:"+realRebate+"=====");
				}
			}
			
			
			
			System.out.println(thirdLevelRebateRatioNews);
			List<UserRealtion> preUserRealtion = userRealtionBusiness.findByFirstUserId(user.getId());
			System.out.println(preUserRealtion);
			int i = 1;
			for (UserRealtion userRealtion : preUserRealtion) {
				if (userRealtion.getPreUserId().longValue() == manageid) {
					break;
				}
				Long preUserId = userRealtion.getPreUserId();
				User preUser = userLoginRegisterBusiness.queryUserById(preUserId);
				String preGrade = preUser.getGrade();
				BigDecimal rebateRatio = thirdLevelRebateRatioNews.get(preGrade).subtract(firstRatio);
				BigDecimal realRebate = rebateRatio.multiply(new BigDecimal(rebate));
				if(BigDecimal.ZERO.compareTo(realRebate) < 0){
					Map<String, Object> subtractAccountBlanceAndAddAccountRebate = this.subtractAccountBlanceAndAddAccountRebate(request, manageid+"", preUserId+"", realRebate, orderCode+i);
					if (!CommonConstants.SUCCESS.equals(subtractAccountBlanceAndAddAccountRebate.get(CommonConstants.RESP_CODE))) {
						return subtractAccountBlanceAndAddAccountRebate;
					}
					System.out.println("=====userId:"+preUserId+"=====获得分润:"+realRebate+"=====");
					firstRatio = thirdLevelRebateRatioNews.get(preGrade);
				}
				i++;
			}
			applyCreditCardOrder.setOrderStatus(Integer.valueOf(orderStatus));
			applyCreditCardOrder.setRebate(new BigDecimal(rebate));
		}else if("2".equals(orderStatus)){
			applyCreditCardOrder.setOrderStatus(Integer.valueOf(orderStatus));
		}
		applyCreditCardOrder = applyCreditCardOrderBusiness.save(applyCreditCardOrder);
		return ResultWrap.init(CommonConstants.SUCCESS, "审核成功",applyCreditCardOrder);
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
	 * 	 * 减少一个用户的余额,增加另一个用户的分润
	 * 	 * @author Robin-QQ/WX:354476429
	 * 	 * @date 2018年6月12日
	 * 	 * @param request
	 * 	 * @param grantUserId
	 * 	 * @param acqUserId
	 * 	 * @param amount
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
		return (Map<String, Object>) userBalanceService.updateUserAccount(request, Long.valueOf(userId), amount, "2", orderCode);
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
		userRebateService.updateUserAccount(request, Long.valueOf(userId), amount, orderCode, "0","0");
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/get/apply/credit/card/order/excel")
	public @ResponseBody Object getExcelTemplate(HttpServletRequest request,HttpServletResponse response
			) throws Exception{
		//处理请求  
        //读取要下载的文件  
        File f = new File("/product/deploy/passList.xlsx");
        if(f.exists()){  
            FileInputStream  fis = new FileInputStream(f);  
            String filename=URLEncoder.encode(f.getName(),"utf-8"); //解决中文文件名下载后乱码的问题  
            byte[] b = new byte[fis.available()];  
            fis.read(b);  
            response.setCharacterEncoding("utf-8");  
            response.setHeader("Content-Disposition","attachment; filename="+filename+"");  
            //获取响应报文输出流对象  
            ServletOutputStream  out =response.getOutputStream();  
            //输出  
            out.write(b);  
            out.flush();  
            out.close();  
        }     
		
		return null;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/upload/apply/credit/card/order/excel")
	public @ResponseBody Object getApplyCreditCardOrdertest(HttpServletRequest request
			) throws FileNotFoundException, IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile files = multipartRequest.getFile("excel");
		ArrayList<ArrayList<String>> xlsx_reader;
		try {
			xlsx_reader = ExcelReader.xlsx_reader(files.getInputStream());
		} catch (Exception e1) {
			e1.printStackTrace();
			return ResultWrap.init(CommonConstants.FALIED, "上传文件格式有误!");
		}
		System.out.println(xlsx_reader);
		List<Map<String,Object>> errorList = new ArrayList<>();
		for (ArrayList<String> arrayList : xlsx_reader) {
			Map<String,Object> errorMap = new HashMap<>();
			String bankName = arrayList.get(0).trim();
			String phone = arrayList.get(1).trim();
			String name = arrayList.get(2).trim();
			String rebate = arrayList.get(3).trim();
			ApplyCreditCardOrder applyCreditCardOrder;
			try {
				applyCreditCardOrder = applyCreditCardOrderBusiness.findByBankNameAndPhoneLikeAndNameLike(bankName, phone, name);
			} catch (Exception e) {
				errorMap.put("bankName", bankName);
				errorMap.put("phone", phone);
				errorMap.put("name", name);
				errorMap.put("rebate", rebate);
				errorMap.put(CommonConstants.RESP_MESSAGE, "未匹配到申请记录!");
				errorList.add(errorMap);
				e.printStackTrace();
				continue;
			}
			if (applyCreditCardOrder == null) {
				errorMap.put("bankName", bankName);
				errorMap.put("phone", phone);
				errorMap.put("name", name);
				errorMap.put("rebate", rebate);
				errorMap.put(CommonConstants.RESP_MESSAGE, "未匹配到申请记录!!");
				errorList.add(errorMap);
				continue;
			}
			String orderCode = applyCreditCardOrder.getOrderCode();
			Map<String,Object> object = (Map<String, Object>) this.updateApplyCreditCardOrder(request, orderCode, "1", rebate);
			System.out.println("返佣结果=====" + object);
			if (!CommonConstants.SUCCESS.equals(object.get(CommonConstants.RESP_CODE))) {
				errorMap.put("bankName", bankName);
				errorMap.put("phone", phone);
				errorMap.put("name", name);
				errorMap.put("rebate", rebate);
				errorMap.put(CommonConstants.RESP_MESSAGE,object.get(CommonConstants.RESP_MESSAGE));
				errorList.add(errorMap);
			}
		}
		Map<String,Object> map = new HashMap<>();
		map.put("errorOrder", errorList);
		return ResultWrap.init(CommonConstants.SUCCESS, "提交成功", map);
	}
			
}
