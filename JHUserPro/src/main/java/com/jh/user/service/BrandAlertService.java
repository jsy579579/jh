package com.jh.user.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.user.business.BrandAlertBusiness;
import com.jh.user.business.NewsBusiness;
import com.jh.user.pojo.BrandAlert;
import com.jh.user.pojo.News;
import com.jh.user.pojo.NewsClassifiCation;
import com.jh.user.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;

@Controller
@EnableAutoConfiguration
public class BrandAlertService {

	private static final Logger LOG = LoggerFactory.getLogger(BrandAlertService.class);

	@Autowired
	Util util;

	@Autowired
	private BrandAlertBusiness brandAlertBusiness;


	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandalert/addorupdate")
	public @ResponseBody Object addOrUpdateBrandAlert(HttpServletRequest request,
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "type") String type,
			@RequestParam(value = "status", required = false, defaultValue = "1") int status
			) throws Exception {

		BrandAlert brandAlertByBrandIdAndTypeAndStatus = brandAlertBusiness.getBrandAlertByBrandIdAndType(brandId, type);
		
		if(brandAlertByBrandIdAndTypeAndStatus == null) {
			BrandAlert brandAlert = new BrandAlert();
			brandAlert.setBrandId(brandId);
			brandAlert.setBtype(type);
			brandAlert.setStatus(status);
			
			brandAlertBusiness.createBrandAlert(brandAlert);
		
		}else {
			
			brandAlertByBrandIdAndTypeAndStatus.setStatus(status);
			brandAlertByBrandIdAndTypeAndStatus.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH-mm-ss"));
			
			brandAlertBusiness.createBrandAlert(brandAlertByBrandIdAndTypeAndStatus);
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "成功");
	}

	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/brandalert/getby/brandidandtype")
	public @ResponseBody Object getNewsByBrandIdAndPage(HttpServletRequest request,
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "type") String type
			) {

		BrandAlert brandAlertByBrandIdAndType = brandAlertBusiness.getBrandAlertByBrandIdAndType(brandId, type);
		
		String alert = null;
		if(brandAlertByBrandIdAndType == null) {
			alert = "1";//alert为1代表弹窗
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", alert);
		}else {
			if(brandAlertByBrandIdAndType.getStatus() == 0) {
				alert = "1";//alert为1代表弹窗
				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", alert);
			}else {
				alert = "0";//alert为0代表不弹窗
				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", alert);
			}
		}
		
	}

	

	

	
}
