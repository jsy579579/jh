package com.jh.user.moudle.cardloans;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.User;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.StringUtil;
import net.sf.json.JSONObject;

@Controller
public class CardLoansOrderPOController {

	private static final Logger LOG = LoggerFactory.getLogger(CardLoansOrderPOController.class);
	
	@Autowired
	private ICardLoansOrderPOBusiness iCardLoansOrderPOBusiness;
	
	@Autowired
	private ILinkConfigPOBusiness iLinkConfigPOBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	RestTemplate restTemplate;
	
	static final String ORDER_STATUS_WAIT = "0";
	
	static final String ORDER_STATUS_SUCCESS = "1";
	
	static final String ORDER_STATUS_FAIL = "2";
	
	/**
	 * 获取所有办卡申请和贷款申请的分润比例
	 * @param brandId
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value ="/v1.0/user/cardloans/get/ratios/list")
	public @ResponseBody Object getCardLoansRatiosList(
//			贴牌号:必传
			@RequestParam()String brandId
			) {
		List<CardLoansRatioPO> ratios =  iCardLoansOrderPOBusiness.findRatiosByBrandId(brandId);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",ratios);
	}
	
	
	/**
	 * 添加一条分润比例
	 * @param brandId
	 * @param preGrade
	 * @param ratio
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value ="/v1.0/user/cardloans/add/ratios")
	public @ResponseBody Object addCardLoansRatio(
			@RequestParam()String brandId,
//			等级
			@RequestParam()String preGrade,
//			分润比例:0-1之间
			@RequestParam()String ratio
			) {
		try {
			BigDecimal bigRatio = new BigDecimal(ratio);
			if (BigDecimal.ONE.compareTo(bigRatio) < 0 || BigDecimal.ZERO.compareTo(bigRatio) > 0) {
				return ResultWrap.init(CommonConstants.FALIED, "分润比例需大于0小于1");
			}
			
		} catch (Exception e) {
			return ResultWrap.init(CommonConstants.FALIED, "分润比例有误");
		}
		CardLoansRatioPO cardLoansRatioPO = iCardLoansOrderPOBusiness.findRatiosByBrandIdAndPreGrade(brandId,preGrade);
		if (cardLoansRatioPO != null) {
			return ResultWrap.init(CommonConstants.FALIED, "该等级分润比例已存在");
		}
		cardLoansRatioPO = iCardLoansOrderPOBusiness.createCardLoansRatioPO(brandId,preGrade,ratio);
		return ResultWrap.init(CommonConstants.SUCCESS, "添加成功",cardLoansRatioPO);
	}
	
	/**
	 * 更新或删除一条记录
	 * @param cardLoansRatioId
	 * @param ratio
	 * @param isDelete
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value ="/v1.0/user/cardloans/update/ratios")
	public @ResponseBody Object updateRatio(
			@RequestParam()String cardLoansRatioId,
//			分润比例
			String ratio,
//			是否删除,如要删除,传:1
			String isDelete
			) {
		CardLoansRatioPO cardLoansRatioPO = iCardLoansOrderPOBusiness.findRatiosById(Long.valueOf(cardLoansRatioId));
		if (cardLoansRatioPO == null) {
			return ResultWrap.init(CommonConstants.FALIED, "修改的数据不存在");
		}
		if (StringUtil.isNotNullString(isDelete) && ORDER_STATUS_SUCCESS.equals(isDelete)) {
			iCardLoansOrderPOBusiness.deleteCardLoansRatioPO(cardLoansRatioPO);
			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功");
		}else {
			if (StringUtil.isNullString(ratio)) {
				return ResultWrap.init(CommonConstants.FALIED, "分润比例不能为空");
			}
			BigDecimal bigRatio = BigDecimal.ZERO;
			try {
				bigRatio = new BigDecimal(ratio);
				if (BigDecimal.ONE.compareTo(bigRatio) < 0 || BigDecimal.ZERO.compareTo(bigRatio) > 0) {
					return ResultWrap.init(CommonConstants.FALIED, "分润比例需大于0小于1");
				}
				
			} catch (Exception e) {
				return ResultWrap.init(CommonConstants.FALIED, "分润比例有误");
			}
			cardLoansRatioPO = iCardLoansOrderPOBusiness.updateCardLoansRatioPO(cardLoansRatioPO,bigRatio);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "修改成功",cardLoansRatioPO);
	}
	
	
	/**
	 * 前端提交一笔申请订单
	 * @param linkConfigId
	 * @param userId
	 * @param loanAmount
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/user/cardloans/put/applyorder")
	public @ResponseBody Object putCardLoansOrder(
//			这个参数由上一个界面带入进来
			@RequestParam()String linkConfigId,
			@RequestParam()String userId,
//			如果是贷款类型,需传入贷款金额
			String loanAmount
			) {
		LinkConfigPO linkConfigPO = iLinkConfigPOBusiness.findById(Long.valueOf(linkConfigId));
		if (linkConfigPO == null) {
			return ResultWrap.init(CommonConstants.FALIED, "提交失败");
		}
		if (LinkConfigPOController.LOAN_NAME.equals(linkConfigPO.getLinkType())) {
			if (StringUtil.isNullString(loanAmount)) {
				return ResultWrap.init(CommonConstants.FALIED, "贷款申请需填写贷款金额");
			}
		}
		JSONObject userRealName = this.getUserRealName(userId);
		userRealName = userRealName.getJSONObject("realname");
		if (userRealName == null || userRealName.isEmpty() || userRealName.isNullObject()) {
			return ResultWrap.init(CommonConstants.FALIED, "无实名信息");
		}
		String realname = userRealName.getString("realname");
		String idcard = userRealName.getString("idcard");
		
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		String phone = user.getPhone();
		CardLoansOrderPO cardLoansOrderPO = iCardLoansOrderPOBusiness.createOne(linkConfigPO,userId,phone,realname,idcard,loanAmount);
		return ResultWrap.init(CommonConstants.SUCCESS, "提交成功",cardLoansOrderPO);
	}
	
	/**
	 * 根据条件获取申请订单
	 * @param brandId
	 * @param phone
	 * @param name
	 * @param idcard
	 * @param orderType
	 * @param classify
	 * @param orderStatus
	 * @param orderCode
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/user/cardloans/get/applyorder/list")
	public @ResponseBody Object getCardLoansOrderPOList(
//			都为非必传,都不传为查询全部
			String brandId,
			String phone,
			String name,
			String idcard,
//			订单类型,贷款申请或者是办卡申请
			String orderType,
//			分类
			String classify,
//			订单状态: 0:待审核,1:已通过,2:已拒绝
			String orderStatus,
//			订单号
			String orderCode,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Page<CardLoansOrderPO> cardLoansOrderPOs =  iCardLoansOrderPOBusiness.findList(brandId,phone,name,idcard,orderType,classify,orderStatus,orderCode,pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",cardLoansOrderPOs);
	}
	
	/**
	 * APP端获取自己提交的申请
	 * @param userId
	 * @param orderType
	 * @param classify
	 * @param orderStatus
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/user/cardloans/get/user/applyorder/list")
	public @ResponseBody Object getUserCardLoansOrderPOList(
//			必传
			@RequestParam()String userId,
			String brandId,
//			下面为非必传,订单类型
			String orderType,
//			分类
			String classify,
//			订单状态
			String orderStatus,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Page<CardLoansOrderPO> cardLoansOrderPOs =  iCardLoansOrderPOBusiness.findList(userId,orderStatus,orderType,classify,brandId,pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",cardLoansOrderPOs);
	}
	
	/**
	 * 后台审核接口
	 * @param cardLoansOrderId
	 * @param orderStatus
	 * @param rebate
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/user/cardloans/set/applyorder/status")
	public @ResponseBody Object setCardLoansOrderOrderStatus(
//			申请单的Id,必传
			@RequestParam()String cardLoansOrderId,
//			审核订单的状态,1:通过,2:拒绝
			@RequestParam()String orderStatus,
//			如果是通过,需要输入 分润金额,大于0
			String rebate
			) {
		if (!ORDER_STATUS_SUCCESS.equals(orderStatus) && !ORDER_STATUS_FAIL.equals(orderStatus)) {
			return ResultWrap.init(CommonConstants.FALIED, "修改状态不正确");
		}
		if (ORDER_STATUS_SUCCESS.equals(orderStatus) && StringUtil.isNullString(rebate)) {
			return ResultWrap.init(CommonConstants.FALIED, "审核失败,缺少分润总金额");
		}
		
		CardLoansOrderPO cardLoansOrderPO = iCardLoansOrderPOBusiness.findById(Long.valueOf(cardLoansOrderId));
		if (cardLoansOrderPO == null) {
			return ResultWrap.init(CommonConstants.FALIED, "修改的数据不存在");
		}
		
		if (ORDER_STATUS_WAIT.equals(cardLoansOrderPO.getOrderStatus())) {
			cardLoansOrderPO = iCardLoansOrderPOBusiness.setOrderStatus(cardLoansOrderPO,orderStatus,rebate);
			return ResultWrap.init(CommonConstants.SUCCESS, "修改成功",cardLoansOrderPO);
		}else {
			return ResultWrap.init(CommonConstants.FALIED, "修改失败,非审核状态");
		}
	}
	
	@RequestMapping(value="/v1.0/user/cardloans/put/feedback/picture")
	public @ResponseBody Object putLoansOrderFeedbackPicture(HttpServletRequest request,
			@RequestParam()String cardLoansOrderId,
			String data1,
			String data2,
			String data3
			) {
		CardLoansOrderPO cardLoansOrderPO = iCardLoansOrderPOBusiness.findById(Long.valueOf(cardLoansOrderId));
		if (cardLoansOrderPO == null) {
			return ResultWrap.init(CommonConstants.FALIED, "修改的数据不存在");
		}
		
		String contentType = request.getContentType();
		if (contentType.contains("multipart/form-data")) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file1 = multipartRequest.getFile("image1");
			MultipartFile file2 = multipartRequest.getFile("image2");
			MultipartFile file3 = multipartRequest.getFile("image3");
			cardLoansOrderPO = iCardLoansOrderPOBusiness.putFeedbackPicture(cardLoansOrderPO,file1,file2,file3);
		}else {
			cardLoansOrderPO = iCardLoansOrderPOBusiness.putFeedbackPicture(cardLoansOrderPO,data1,data2,data3);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "上传成功",cardLoansOrderPO);
	}
	
	@RequestMapping(value="/v1.0/user/cardloans/get/rebate/history")
	public @ResponseBody Object getCardLoansRebateHistory(
			@RequestParam String userId,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Page<CardLoansRebateHistoryPO> cardLoansRebateHistoryPOs =  iCardLoansOrderPOBusiness.findCardLoansRebateHistoryPOByReceiveUserId(userId,pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",cardLoansRebateHistoryPOs);
	}
	
	private JSONObject getUserRealName(String userId) {
		String url = "http://paymentchannel/v1.0/paymentchannel/realname/userid";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("userid", userId);
		return restTemplate.postForObject(url, requestEntity, JSONObject.class);
	}
	
}
