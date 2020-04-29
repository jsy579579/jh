package com.jh.user.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.jh.user.business.CreditLoanBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.CreditLoan;
import com.jh.user.pojo.User;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
@RequestMapping("/v1.0/user")
public class CreditLoanService {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CreditLoanBusiness creditLoanBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
//	用户提交贷款申请
	@RequestMapping(method=RequestMethod.POST,value="/credit/loan/add/one")
	public @ResponseBody Object addCreditLoan(
			@RequestParam(value="userId")String userId,
//			姓名
			@RequestParam(value="name")String name,
//			手机号
			@RequestParam(value="phone")String phone,
//			身份证号
			@RequestParam(value="idCard")String idCard,
//			地址
			@RequestParam(value="address")String address,
//			贷款用途
			@RequestParam(value="description")String description,
//			贷款金额
			@RequestParam(value="amount")String amountStr
			){
		Map<String, Object> verifyMoney = AuthorizationHandle.verifyMoney(amountStr, 2, BigDecimal.ROUND_HALF_UP);
		if (!CommonConstants.SUCCESS.equals(verifyMoney.get(CommonConstants.RESP_CODE))) {
			return verifyMoney;
		}
		
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		if (user == null) {
			return ResultWrap.init(CommonConstants.FALIED, "提交失败,无该用户信息!");
		}
		
		BigDecimal amount = (BigDecimal) verifyMoney.get(CommonConstants.RESULT);
		CreditLoan model = new CreditLoan();
		model.setBrandId(user.getBrandId());
		model.setUserId(user.getId());
		model.setAmount(amount);
		model.setPhone(phone);
		model.setName(name);
		model.setDescription(description);
		model.setAddress(address);
		model.setIdCard(idCard);
		model.setCreateTime(new Date());
		model =	creditLoanBusiness.save(model);
		return ResultWrap.init(CommonConstants.SUCCESS, "提交成功!", model);
	}
	
//	后台展示贷款申请
	@RequestMapping(method=RequestMethod.POST,value="/credit/loan/get/list")
	public @ResponseBody Object getCreditLoanList(
			@RequestParam(value="brandId")String brandId,
//			status:0 待审核,1 已通过 2已拒绝
			@RequestParam(value="status",required=false,defaultValue="0")String statusStr,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
			){
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<CreditLoan> models = creditLoanBusiness.findByBrandIdAndStatus(Long.valueOf(brandId),Integer.valueOf(statusStr),pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", models);
	}
	
//	审核用户的贷款申请
	@RequestMapping(method=RequestMethod.POST,value="/credit/loan/update")
	public @ResponseBody Object updateCreditLoan(
//			1 :审核通过  2:审核拒绝
			@RequestParam(value="status")String statusStr,
			@RequestParam(value="id")String id
			){
		CreditLoan model = creditLoanBusiness.findById(Long.valueOf(id));
		if (model == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该记录不存在");
		}
		
		if (model.getStatus() != 0) {
			return ResultWrap.init(CommonConstants.FALIED, "该记录已处理");
		}
		
		model.setStatus(Integer.valueOf(statusStr));
		model = creditLoanBusiness.save(model);
		return ResultWrap.init(CommonConstants.SUCCESS, "审核成功",model);
	}
	
	
	
}
