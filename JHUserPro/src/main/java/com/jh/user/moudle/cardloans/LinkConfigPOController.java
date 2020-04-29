package com.jh.user.moudle.cardloans;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.StringUtil;

@Controller
public class LinkConfigPOController {

	private static final Logger LOG = LoggerFactory.getLogger(LinkConfigPOController.class);
	
	static final String APPLY_CARD_NAME = "信用卡申请";
	
	static final String LOAN_NAME = "贷款申请";

	@Autowired
	private ILinkConfigPOBusiness iLinkConfigPOBusiness;
	
	/**
	 * 获取所有类型
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/user/cardloans/get/linktype")
	public @ResponseBody Object getLinkType() {
		Map<String,Object> map = new HashMap<>();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		List<String> list = new ArrayList<>();
		list.add(LOAN_NAME);
		list.add(APPLY_CARD_NAME);
		map.put(CommonConstants.RESULT, list);
		return map;
	}
	
	/**
	 * 添加一个贷款或者办卡申请链接
	 * @param brandId
	 * @param linkType
	 * @param linkClassify
	 * @param linkTitle
	 * @param linkAddress
	 * @param linkHits
	 * @param linkRemark
	 * @param linkPublisher
	 * @return
	 * @throws UnsupportedEncodingException
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/user/cardloans/put/linkconfig")
	public @ResponseBody Object addLinkConfig(HttpServletRequest request,
			@RequestParam()String brandId,
//			由获取所有类型接口返回的值,中文需URL编码
			@RequestParam()String linkType,
//			分类,中文需URL编码
			@RequestParam()String linkClassify,
//			标题,中文需URL编码
			@RequestParam()String linkTitle,
//			链接地址
			@RequestParam()String linkAddress,
//			结算方式
			String clearingForm,
//			结算标准
			String settlementStandard,
//			数据形式
			String dataForm,
//			返佣总金额
			@RequestParam()String rebate,
			@RequestParam(defaultValue="0")String onOff,
//			以上为必传参数,下面是非必传参数
//			自定义观看次数
			String linkHits,
//			自定义备注
			String linkRemark,
//			自定义发表人
			String linkPublisher,
			@RequestParam(defaultValue="0")String minLimit,
			@RequestParam(defaultValue="0")String maxLimit
			) throws UnsupportedEncodingException {
		String contentType = request.getContentType();
		linkClassify = URLDecoder.decode(linkClassify, "UTF-8");
		linkType = URLDecoder.decode(linkType, "UTF-8");
		linkTitle = URLDecoder.decode(linkTitle, "UTF-8");
		clearingForm = URLDecoder.decode(clearingForm, "UTF-8");
		settlementStandard = URLDecoder.decode(settlementStandard, "UTF-8");
		dataForm = URLDecoder.decode(dataForm, "UTF-8");
		if (StringUtil.isNotNullString(linkRemark)) {
			linkRemark = URLDecoder.decode(linkRemark, "UTF-8");
		}
		if (StringUtil.isNotNullString(linkPublisher)) {
			linkPublisher = URLDecoder.decode(linkPublisher, "UTF-8");
		}
		if (APPLY_CARD_NAME.equals(linkType)) {
			linkType = APPLY_CARD_NAME;
		}else if(LOAN_NAME.equals(linkType)) {
			linkType = LOAN_NAME;
		}else {
			return ResultWrap.init(CommonConstants.FALIED,"linkType有误");
		}
		

		if (contentType.contains("multipart/form-data")) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("image");
			MultipartFile file2 = multipartRequest.getFile("detailimage");
			
			LinkConfigPO linkConfigPO = new LinkConfigPO();
			linkConfigPO.setBrandId(brandId);
			linkConfigPO.setLinkType(linkType);
			linkConfigPO.setLinkClassify(linkClassify);
			linkConfigPO.setLinkTitle(linkTitle);
			linkConfigPO.setLinkAddress(linkAddress);
			linkConfigPO.setLinkHits(StringUtil.isNullString(linkHits)?0:Integer.valueOf(linkHits));
			linkConfigPO.setLinkRemark(linkRemark);
			linkConfigPO.setLinkPublisher(linkPublisher);
			linkConfigPO.setOnOff(onOff);
			linkConfigPO.setClearingForm(clearingForm);
			linkConfigPO.setSettlementStandard(settlementStandard);
			linkConfigPO.setDataForm(dataForm);
			linkConfigPO.setRebate(new BigDecimal(rebate));
			linkConfigPO.setMinLimit(new BigDecimal(minLimit));
			linkConfigPO.setMaxLimit(new BigDecimal(maxLimit));
			linkConfigPO.setCreateTime(new Date());
			
			linkConfigPO = iLinkConfigPOBusiness.createNewOne(linkConfigPO,file,file2);
			if (linkConfigPO == null) {
				return ResultWrap.init(CommonConstants.FALIED,"设置失败");
			}
			return ResultWrap.init(CommonConstants.SUCCESS,"设置成功",linkConfigPO);
		}else {
			return ResultWrap.init(CommonConstants.FALIED,"contentType不正确,应为:multipart/form-data");
		}
	}
	
	/**
	 * 更新链接设置
	 * @param linkConfigId
	 * @param linkType
	 * @param linkClassify
	 * @param linkTitle
	 * @param linkAddress
	 * @param linkHits
	 * @param linkRemark
	 * @param linkPublisher
	 * @return
	 * <p>Description: </p>
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value="/v1.0/user/cardloans/updet/linkconfig")
	public @ResponseBody Object updateLinkConfig(HttpServletRequest request,
//			由/v1.0/user/cardloans/get/linkconfig/list这个接口返回的Id,必传
			@RequestParam()String linkConfigId,
//			以下为非必传,修改哪个传哪个
			String linkType,
			String linkClassify,
			String linkTitle,
			String linkAddress,
			String linkHits,
			String linkRemark,
			String linkPublisher,
			String clearingForm,
			String settlementStandard,
			String dataForm,
			String rebate,
			String onOff,
			String minLimit,
			String maxLimit
			) throws UnsupportedEncodingException {
		LinkConfigPO linkConfigPO = iLinkConfigPOBusiness.findById(Long.valueOf(linkConfigId));
		if (linkConfigPO == null) {
			return ResultWrap.init(CommonConstants.FALIED, "修改的数据不存在");
		}
		
		String contentType = request.getContentType();
		if (contentType.contains("multipart/form-data")) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("image");
			MultipartFile file2 = multipartRequest.getFile("detailimage");

			if (StringUtil.isNotNullString(linkType)) {
				linkType = URLDecoder.decode(linkType, "UTF-8");
				if (APPLY_CARD_NAME.equals(linkType)) {
					linkType = APPLY_CARD_NAME;
				}else if(LOAN_NAME.equals(linkType)) {
					linkType = LOAN_NAME;
				}else {
					return ResultWrap.init(CommonConstants.FALIED,"linkType有误");
				}
				linkConfigPO.setLinkType(linkType);
			}
			if (StringUtil.isNotNullString(linkClassify)) {
				linkClassify = URLDecoder.decode(linkClassify, "UTF-8");
				linkConfigPO.setLinkClassify(linkClassify);
			}
			if (StringUtil.isNotNullString(linkTitle)) {
				linkTitle = URLDecoder.decode(linkTitle, "UTF-8");
				linkConfigPO.setLinkTitle(linkTitle);
			}
			if (StringUtil.isNotNullString(linkAddress)) {
				linkConfigPO.setLinkAddress(linkAddress);
			}
			if (StringUtil.isNotNullString(linkHits)) {
				linkConfigPO.setLinkHits(Integer.valueOf(linkHits));
			}
			if (StringUtil.isNotNullString(linkRemark)) {
				linkRemark = URLDecoder.decode(linkRemark, "UTF-8");
				linkConfigPO.setLinkRemark(linkRemark);
			}
			if (StringUtil.isNotNullString(linkPublisher)) {
				linkPublisher = URLDecoder.decode(linkPublisher, "UTF-8");
				linkConfigPO.setLinkPublisher(linkPublisher);
			}
			if (StringUtil.isNotNullString(clearingForm)) {
				clearingForm = URLDecoder.decode(clearingForm, "UTF-8");
				linkConfigPO.setClearingForm(clearingForm);
			}
			if (StringUtil.isNotNullString(settlementStandard)) {
				settlementStandard = URLDecoder.decode(settlementStandard, "UTF-8");
				linkConfigPO.setSettlementStandard(settlementStandard);
			}
			if (StringUtil.isNotNullString(dataForm)) {
				dataForm = URLDecoder.decode(dataForm, "UTF-8");
				linkConfigPO.setDataForm(dataForm);
			}
			if (StringUtil.isNotNullString(rebate)) {
				linkConfigPO.setRebate(new BigDecimal(rebate));
			}
			if (StringUtil.isNotNullString(onOff)) {
				linkConfigPO.setOnOff(onOff);
			}
			if (StringUtil.isNotNullString(minLimit)) {
				linkConfigPO.setMinLimit(new BigDecimal(minLimit));
			}
			if (StringUtil.isNotNullString(maxLimit)) {
				linkConfigPO.setMaxLimit(new BigDecimal(maxLimit));			
			}
			
			linkConfigPO = iLinkConfigPOBusiness.updateLinkConfig(linkConfigPO,file,file2);
			return ResultWrap.init(CommonConstants.SUCCESS, "更新成功",linkConfigPO);
		}else {
			return ResultWrap.init(CommonConstants.FALIED,"contentType不正确,应为:multipart/form-data");
		}
	}
	
	/**
	 * 获取链接配置
	 * @param brandId
	 * @param linkType
	 * @param linkClassify
	 * @param onOff
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value = "/v1.0/user/cardloans/get/linkconfig/list")
	public @ResponseBody Object getLinkConfig(
			String brandId, 
			String linkType, 
			String linkClassify, 
//			开关:0:关,1:开
			String onOff,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Page<LinkConfigPO> linkConfigPOs= iLinkConfigPOBusiness.findList(brandId,linkType,linkClassify,onOff,pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",linkConfigPOs);
	}
	
	/**
	 * 对链接设置进行开关
	 * @param linkConfigId
	 * @param onOff
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value = "/v1.0/user/cardloans/updet/linkconfig/onoff")
	public @ResponseBody Object updateLinkConfigOnOff(
			@RequestParam()String linkConfigId,
//			0:关,1:开
			@RequestParam()String onOff
			) {
		LinkConfigPO linkConfigPO = iLinkConfigPOBusiness.findById(Long.valueOf(linkConfigId));
		if (linkConfigPO == null) {
			return ResultWrap.init(CommonConstants.FALIED,"更新的数据不存在");
		}
		linkConfigPO = iLinkConfigPOBusiness.updateLinkConfigOnOff(linkConfigPO,onOff);
		return ResultWrap.init(CommonConstants.SUCCESS, "更新成功",linkConfigPO);
	}
	
	/**
	 * 对链接设置进行删除
	 * @param linkConfigId
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value = "/v1.0/user/cardloans/delete/linkconfig")
	public @ResponseBody Object deleteLinkConfig(
			@RequestParam()String linkConfigId
			) {
		LinkConfigPO linkConfigPO = iLinkConfigPOBusiness.findById(Long.valueOf(linkConfigId));
		if (linkConfigPO == null) {
			return ResultWrap.init(CommonConstants.FALIED,"删除的数据不存在");
		}
		try {
			iLinkConfigPOBusiness.delete(linkConfigPO);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultWrap.init(CommonConstants.FALIED,"删除失败");
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "删除成功");
	}
	
}
