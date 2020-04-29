package com.jh.user.service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.jh.user.util.*;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.*;
import com.jh.user.business.*;
import com.jh.user.pojo.*;
import com.jh.user.util.Util;
import com.jh.user.util.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import org.springframework.web.bind.annotation.*;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;



import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserBankInfoBusiness;
import com.jh.user.business.UserCoinBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.InfoUser;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserBankInfo;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.FileUtils;
import cn.jh.common.utils.Md5Util;
import cn.jh.common.utils.PhotoCompressUtil;
import cn.jh.common.utils.TokenUtil;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;



@Controller
@EnableAutoConfiguration
public class UserRealnameAuthService {

	private static final Logger LOG = LoggerFactory.getLogger(UserRealnameAuthService.class);

	private static final String realNameReadPathUploadPath="/usr/share/nginx/html";

	@Autowired
	private FaceAuthCL faceAuthCL;

	@Value("${user.realname.uploadpath}")
	private String realnamePic;

	@Value("${user.realname.downloadpath}")
	private String downloadPath;

	@Value("${baidu.url}")
	private String baiDuUrl;

	@Value("${baidu.ak}")
	private String AK;

	@Value("${baidu.sk}")
	private String SK;

	@Autowired
	private UserBankInfoBusiness userBankInfoBusiness;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	private UserCoinBusiness userCoinBusiness;

	@Autowired
	private BrandManageBusiness brandManageBusiness;

	@Autowired
	private UserJpushService userJpushService;

	@Autowired
	private UserAwardService userAwardService;

	@Autowired
	private AliOSSUtil aliOSSUtil;

	@Autowired
	private RealNameTypeBusiness realNameTypeBusiness;

	@Autowired
	Util util;

	@Autowired
	private RestTemplate restTemplate;

	/**读取用户的文件*/
	@Deprecated
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/download/realname")
	public @ResponseBody Object downloadUserPic(HttpServletRequest request,
												@RequestParam(value="brandId",required=false,defaultValue="-1")String sbrandId,
												@RequestParam(value = "phone") String phone
	){
		Map<String,Object> map = new HashMap<>();
		long brandId = -1;
		User user;
		Brand brand = null;
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e) {
			brandId = -1;
		}
		if(brandId==-1){
			user =userLoginRegisterBusiness.queryUserByPhone(phone);
		}else{
			brand = brandManageBusiness.findBrandById(brandId);
			if(brand != null && "6".equals(brand.getBrandType())){
				user =userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
			}else{
				user =userLoginRegisterBusiness.queryUserByPhone(phone);
			}
		}



		if(user==null){
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESULT, null);
			map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
			return map;

		}

		brandId = user.getBrandId();

		/**获取身份证实名信息*/
		URI uri = util.getServiceUrl("paymentchannel", "error url request!");
		String url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
		MultiValueMap<String, Long> requestEntity  = new LinkedMultiValueMap<String, Long>();
		requestEntity.add("userid", user.getId());

		RestTemplate restTemplate=new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================/v1.0/paymentchannel/realname/userid" + result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject authObject  =  jsonObject.getJSONObject("realname");
		InfoUser infouser= new InfoUser();

		//系统编号
		infouser.setUserid(user.getId());

		infouser.setBrandId(user.getBrandId());

		infouser.setBrandName(user.getBrandname());
		//用户手机号
		infouser.setPhone(user.getPhone());

		//真实姓名
		infouser.setRealname(authObject.getString("realname"));
		//身份证号
		infouser.setIdcard(authObject.getString("idcard"));
		UserBankInfo ubi=new UserBankInfo();
		ubi=userBankInfoBusiness.queryDefUserBankInfoByUserid(user.getId());

		if(ubi!=null){

			//银行卡名称
			infouser.setBankName(ubi.getBankName());
			//银行卡号
			infouser.setCardNo(ubi.getCardNo());

		}else{
			//银行卡名称
			infouser.setBankName(null);
			//银行卡号
			infouser.setCardNo(null);
		}

		//用户性别
		infouser.setSex(user.getSex());

		//实名状态
		infouser.setRealnameStatus(user.getRealnameStatus());
		//级别
		infouser.setGrade(user.getGrade());
		//注册时间
		infouser.setCreateTime(user.getCreateTime());
		List<String> filepaths  = new ArrayList<String>();
		String src="/"+user.getBrandId()+ "/realname/"+phone;

		String readPath="/realname/"+user.getBrandId()+"/"+phone;
//		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
//		String ossObjectName = "";

		File file = new File(realNameReadPathUploadPath + readPath);
		String[] filelist = file.list();
		if (filelist != null && filelist.length > 0) {
			for (int i = 0; i < filelist.length; i++) {
				filepaths.add(downloadPath + readPath + "/" + filelist[i]);
			}
		}
//		else {
//			List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//			if (listFiles != null && listFiles.size() >0) {
//				for (String fileName : listFiles) {
//					String fileUrl = aliOSSUtil.getFileUrl(fileName);
//					filepaths.add(fileUrl);
//				}
//			}else {
//				ossObjectNamePrefix = AliOSSUtil.REAL_NAME_PHOTO + "-" + user.getBrandId() + "-" + phone + "-";
//				listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//				if (listFiles != null && listFiles.size() >0) {
//					for (String fileName : listFiles) {
//						String fileUrl = aliOSSUtil.getFileUrl(fileName);
//						filepaths.add(fileUrl);
//					}
//				}
//			}
//		}


		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, filepaths);
		map.put("infouser", infouser);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}


	/**更新用户的状态*/
	@Deprecated
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/updatestatus/status")
	public @ResponseBody Object updateRealnameStatus(HttpServletRequest request,
													 @RequestParam(value="brandId",required=false,defaultValue="-1")String sbrandId,
													 @RequestParam(value = "phone") String phone,
													 @RequestParam(value = "status") String status
	){

		Map<String,Object> map = new HashMap<>();
		long brandId = -1;
		User user;
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e) {
			brandId = -1;
		}
		if(brandId==-1){
			user = userLoginRegisterBusiness.queryUserByPhone(phone);
		}else{
			if("6".equals(brandManageBusiness.findBrandById(brandId).getBrandType())){
				user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
			}else{
				user = userLoginRegisterBusiness.queryUserByPhone(phone);
			}
		}
		/*
		if (!"1".equals(status.trim()) &&user.getRealnameStatus().trim().equals("1")) {
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,该功能维护中");
		}*/
		user.setRealnameStatus(status);
		//调用实名注册接口，将真实姓名存入用户表
		if(status.equals("1")){
			URI uri = util.getServiceUrl("paymentchannel", "error url request!");
			String url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
			MultiValueMap<String, Long> requestEntity  = new LinkedMultiValueMap<String, Long>();
			requestEntity.add("userid", user.getId());

			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject authObject  =  jsonObject.getJSONObject("realname");

			user.setFullname(authObject.getString("realname"));


			try {
				Map<String, Object> sendUserAward = userAwardService.sendUserAward(user.getId()+"");
				LOG.info("发放用户实名奖励结果=====" + sendUserAward);
			} catch (Exception e) {
				LOG.error("", e);
			}

		}
		user = userLoginRegisterBusiness.saveUser(user);
		if(status.equals("1")){
			User preuser=userLoginRegisterBusiness.queryUserById(user.getPreUserId());
			if(preuser != null) {
				preuser.setEncourageNum(preuser.getEncourageNum()+1);
				userLoginRegisterBusiness.saveUser(preuser);
			}
		}


		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, user);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		/**
		 * 推送信息
		 * *URL：/v1.0/user/jpush/tset
		 * **/
		String alert ="实名认证提示";
		String content = "亲爱的会员，实名认证已通过审核";
		//1表示审核通过  2表示审核拒绝
		if(status.equals("2")){
			content = "亲爱的会员，实名认证审核失败";
		}
		String btype="realnameStatus";
		String btypeval=status;

		userJpushService.setJpushtest(request, user.getId(), alert, content, btype, btypeval);
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upload/user/real/photo/{token}")
	public @ResponseBody Object uploadUserPhotoAndRealName(HttpServletRequest request,
														   @PathVariable(value="token")String token,
														   String number,
														   String name,
														   String date,
														   String office
	) {
		LOG.info("进入上传实名图片==============");
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultWrap.init(CommonConstants.FALIED, "校验失败");
		}
		try {
			LOG.info(name);
			LOG.info(office);
			name = URLDecoder.decode(name, "UTF-8");
			office = URLDecoder.decode(office, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ResultWrap.init(CommonConstants.FALIED, "校验失败");
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);
		String phone = user.getPhone();
		if ("0".equals(user.getRealnameStatus()) || "1".equals(user.getRealnameStatus())) {
			return ResultWrap.init(CommonConstants.FALIED, "请勿重复提交实名信息!");
		}
		//获取当前实名类型
//		RealNameType realNameType=realNameTypeBusiness.queryRealNameType();
//		if(!"0".equals(realNameType.getRealnameType())) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile frontImg = multipartRequest.getFile("frontImg");
			MultipartFile backImg = multipartRequest.getFile("backImg");
			List<MultipartFile> images = multipartRequest.getFiles("image");
			LOG.info("文件===============" + images);
			//保存路径
			String path = "/realname/" + user.getBrandId() + "/" + phone;
			File mkdir = new File(realNameReadPathUploadPath + path);
			if (!mkdir.exists()) {
				mkdir.mkdirs();
			}
//		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
//		List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//		if (listFiles != null && listFiles.size() > 0) {
//			for (String fileName : listFiles) {
//				aliOSSUtil.deleteFileFromOss(fileName);
//			}
//		}

//		this.uploadPhotoToALiOSS(frontImg, "frontImg", ossObjectNamePrefix);
//		this.uploadPhotoToALiOSS(backImg, "backImg", ossObjectNamePrefix);
//		int i = 1;
			if (images != null && images.size() > 0) {
				for (MultipartFile file : images) {
					//this.uploadPhotoToALiOSS(file, i+"", ossObjectNamePrefix);
					String filename = file.getOriginalFilename();
					System.out.println("文件名=====" + filename);
					//String substring = filename.substring(filename.lastIndexOf("."));
					File dest = new File(mkdir + "/" + filename);
					System.out.println(dest);
					try {
						if (dest.exists()) {
							dest.delete();
						}
						file.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (Exception e) {
						LOG.error("保存实名图片出错啦======");
						e.printStackTrace();

						return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
					}
				}
			}
//		}

		String url = "http://paymentchannel/v1.0/paymentchannel/realname/add/realname";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("userId", userId+"");
		requestEntity.add("realname", name);
		requestEntity.add("idCard", number);
		requestEntity.add("result", "1");
		requestEntity.add("message", "匹配");
		restTemplate.postForEntity(url, requestEntity, String.class);
		this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "1");
		return ResultWrap.init(CommonConstants.SUCCESS, "实名认证成功");

	}

	private void uploadPhotoToALiOSS(MultipartFile file,String ossName,String ossObjectNamePrefix) {
		String fileName = file.getOriginalFilename();
		String prefix=fileName.substring(fileName.lastIndexOf("."));
		fileName = ossName+prefix;
		String ossObjectName = ossObjectNamePrefix + fileName;
		OutputStream os = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = null;
		try {
			PhotoCompressUtil.compressPhoto(file.getInputStream(), os, 0.2f);
			inputStream = FileUtils.parse(os);
			aliOSSUtil.uploadStreamToOss(ossObjectName,inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
			throw new RuntimeException(e);
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
	}

	/**上传 用户的照片
	 * @throws  */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upload/realname")
	public @ResponseBody Object uploadUserPic(HttpServletRequest request,
											  @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
											  @RequestParam(value = "phone") String phone) {

		Map<String, Object> map = new HashMap<>();
		long brandId = -1;
		User user;
		Brand brand = null;
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e1) {
			brandId = -1;
		}
		if (brandId == -1) {
			user = userLoginRegisterBusiness.queryUserByPhone(phone); // 如果brandid=-1通过手机号查询到用户
			LOG.info("用户信息=========="+user);
		} else {
			brand = brandManageBusiness.findBrandById(brandId); // 如果brandid!=-1通过贴牌id找到贴牌
			if (brand != null && "6".equals(brand.getBrandType())) { // 如果贴牌类型=6
				user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId); // 通过手机号和贴牌id找到user
			} else {
				user = userLoginRegisterBusiness.queryUserByPhone(phone); // 如果贴牌类型!=6
				// 通过手机号找到user
			}
		}

		if ("0".equals(user.getRealnameStatus()) || "1".equals(user.getRealnameStatus())) {
			return ResultWrap.init(CommonConstants.FALIED, "请勿重复提交实名信息!");
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> files = multipartRequest.getFiles("image");

		//保存路径
		String path="/realname/"+user.getBrandId()+"/"+phone;
		File mkdir=new File(realNameReadPathUploadPath+path);
		if(!mkdir.exists()){
			mkdir.mkdirs();
		}
//		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
//		List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//		if (listFiles != null && listFiles.size() > 0) {
//			for (String fileName : listFiles) {
//				aliOSSUtil.deleteFileFromOss(fileName);
//			}
//		}

//		this.uploadPhotoToALiOSS(frontImg, "frontImg", ossObjectNamePrefix);
//		this.uploadPhotoToALiOSS(backImg, "backImg", ossObjectNamePrefix);
		int i = 0;
		for (MultipartFile file : files) {
			//this.uploadPhotoToALiOSS(file, i+"", ossObjectNamePrefix);
			String filename = file.getOriginalFilename();
			System.out.println("文件名====="+filename);
			String substring = filename.substring(filename.lastIndexOf("."));
			if(i==0){
				File dest = new File(mkdir +"/"+"idCardImage"+substring);
				System.out.println(dest);
				try {
					if(dest.exists()){
						dest.delete();
					}
					file.transferTo(dest);
					Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
				} catch (Exception e) {
					LOG.error("保存实名图片出错啦======");
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
				}
			}else if(i==1){
				File dest = new File(mkdir +"/"+"idCardBackImage"+substring);
				System.out.println(dest);
				try {
					if(dest.exists()){
						dest.delete();
					}
					file.transferTo(dest);
					Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
				} catch (Exception e) {
					LOG.error("保存实名图片出错啦======");
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
				}
			}else{
				File dest = new File(mkdir +"/"+"liveImage"+substring);
				System.out.println(dest);
				try {
					if(dest.exists()){
						dest.delete();
					}
					file.transferTo(dest);
					Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
				} catch (Exception e) {
					LOG.error("保存实名图片出错啦======");
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
				}
			}


			i++;
		}

//		//获取当前实名认证类型 0 上传图片后直接通过
//		RealNameType realNameType=realNameTypeBusiness.queryRealNameType();
//		if("0".equals(realNameType.getRealnameType())){
//			String url = "http://paymentchannel/v1.0/paymentchannel/realname/add/realname";
//			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
//			requestEntity.add("userId", user.getId()+"");
//			requestEntity.add("realname", name);
//			requestEntity.add("idCard", idNum);
//			requestEntity.add("result", "1");
//			requestEntity.add("message", "匹配");
//			restTemplate.postForEntity(url, requestEntity, String.class);
//			userRealnameAuthService.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "1");
//
//			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
//			map.put(CommonConstants.RESP_MESSAGE,"实名认证成功");
//			return map;
//		}

		//调用三方人脸识别API接口
		Map realNameResult=faceAuthCL.FaceAuth(request,phone);
		LOG.info("实名结果==============="+realNameResult);
		if(CommonConstants.SUCCESS.equals(realNameResult.get("resp_code"))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "实名认证成功");
			return map;
		}
//		String src="/"+user.getBrandId()+ "/realname/"+phone;

//		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
//		String ossObjectName = "";

		/*File dir = new File(realnamePic +src );
		// 创建目录
		if (dir.mkdirs()) {
			System.out.println("创建目录" + realnamePic +src  + "成功！");

		} else {
			File[] tempfiles = dir.listFiles();
			if (tempfiles != null && tempfiles.length >0) {
				for (File file : tempfiles) {
					file.delete();
				}
			}else {

			}

			System.out.println("创建目录" + realnamePic +src  + "失败！");
		}*/
//		List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
//		if (listFiles != null && listFiles.size() > 0) {
//			for (String fileName : listFiles) {
//				aliOSSUtil.deleteFileFromOss(fileName);
//			}
//		}

//		int i = 1;
//		if (files != null && files.size() > 0) {
//			for (MultipartFile file : files) {
//
//				String fileName = file.getOriginalFilename();
//		    	String prefix=fileName.substring(fileName.lastIndexOf("."));
//		    	fileName = System.currentTimeMillis()+i+prefix;
//				ossObjectName = ossObjectNamePrefix + fileName;
//				OutputStream os = new ByteArrayOutputStream();
//				ByteArrayInputStream inputStream = null;
//				try {
//					PhotoCompressUtil.compressPhoto(file.getInputStream(), os, 0.2f);
//					inputStream = FileUtils.parse(os);
//					aliOSSUtil.uploadStreamToOss(ossObjectName,inputStream);
//				} catch (Exception e1) {
//					e1.printStackTrace();LOG.error(ExceptionUtil.errInfo(e1));
//				}finally{
//					try {
//						os.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//						os = null;
//					}
//					try {
//						inputStream.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//						inputStream = null;
//					}
//				}
//
				/*File dest = new File(realnamePic +src + "/" + fileName);
		    	dest.setExecutable(true, false);
		    	dest.setReadable(true, false);
		    	dest.setWritable(true, false);
		    	dest.setWritable(true);
		    	dest.setExecutable(true);
		    	dest.setReadable(true);
				try {
					file.transferTo(dest);
					Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
				} catch (IllegalStateException e) {
					e.printStackTrace();LOG.error("",e);
				} catch (IOException e) {
					e.printStackTrace();LOG.error("",e);
				}*/
//				i++;
//			}
//		}

		/*else {
			File dir = new File(realnamePic +src );
			// 创建目录
			if (dir.mkdirs()) {
				System.out.println("创建目录" + realnamePic +src  + "成功！");

			} else {

				File[] tempfiles = dir.listFiles();
				for (File file : tempfiles) {
					file.delete();
				}

				System.out.println("创建目录" + realnamePic +src  + "失败！");
			}

			if (files != null && files.size() > 0) {
				int i = 1;
				for (MultipartFile file : files) {

					String fileName = file.getOriginalFilename();
			    	String prefix=fileName.substring(fileName.lastIndexOf("."));
			    	fileName = System.currentTimeMillis()+i+prefix;
					ossObjectName = ossObjectNamePrefix + fileName;
					try {
						aliOSSUtil.uploadStreamToOss(ossObjectName,file.getInputStream());
					} catch (IOException e1) {
						e1.printStackTrace();LOG.error(ExceptionUtil.errInfo(e1));
					}

					File dest = new File(realnamePic + src + "/" + fileName);
			    	dest.setExecutable(true, false);
			    	dest.setReadable(true, false);
			    	dest.setWritable(true, false);
			    	dest.setWritable(true);
			    	dest.setExecutable(true);
			    	dest.setReadable(true);
					try {
						file.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (IllegalStateException e) {
						e.printStackTrace();LOG.error("",e);
					} catch (IOException e) {
						e.printStackTrace();LOG.error("",e);
					}
					i++;
				}
			}
		}*/

		user.setRealnameStatus("0");
		user = userLoginRegisterBusiness.saveUser(user);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "上传成功，等待审核");

		/**
		 * 推送消息 /v1.0/user/jpush/tset
		 */

		/*String alert = "实名认证提示";
		String content = "亲爱的会员，实名认证正在审核中....";
		String btype = "realnameStatus";
		String btypeval = "0";

		*//** 获取身份证实名信息 *//*
		userJpushService.setJpushtest(request, user.getId(), alert, content, btype, btypeval);*/

		//关闭自动审核（）

		/*if(files.size()==3){
			this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "0");
		}else{
			this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "2");
		}*/
		return map;
	}


	/**
	 *
	 * 百度人脸识别
	 * @param request
	 * @param sbrandId    贴牌id
	 * @param phone       手机号
	 * @param idCard      身份证号
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upload/realname/baidu")
	public @ResponseBody Object uploadUserPic2(HttpServletRequest request,
											  @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
											  @RequestParam(value = "phone") String phone,
											   @RequestParam(value = "idCard") String idCard,
											   @RequestParam(value = "userName") String userName) throws UnsupportedEncodingException {

		Map<String, Object> map = new HashMap<>();
		long brandId = -1;
		User user;
		Brand brand = null;
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e1) {
			brandId = -1;
		}
		if (brandId == -1) {
			user = userLoginRegisterBusiness.queryUserByPhone(phone); // 如果brandid=-1通过手机号查询到用户
			LOG.info("用户信息=========="+user);
		} else {
			brand = brandManageBusiness.findBrandById(brandId); // 如果brandid!=-1通过贴牌id找到贴牌
			if (brand != null && "6".equals(brand.getBrandType())) { // 如果贴牌类型=6
				user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId); // 通过手机号和贴牌id找到user
			} else {
				user = userLoginRegisterBusiness.queryUserByPhone(phone); // 如果贴牌类型!=6
				// 通过手机号找到user
			}
		}

		if(user == null){
			return ResultWrap.init(CommonConstants.FALIED, "用户信息不存在！");
		}
		LOG.info("user.getRealnameStatus()================"+user.getRealnameStatus());
		if ("0".equals(user.getRealnameStatus()) || "1".equals(user.getRealnameStatus())) {
			return ResultWrap.init(CommonConstants.FALIED, "请勿重复提交实名信息!");
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> files = multipartRequest.getFiles("image");

		LOG.info("files============"+user.getRealnameStatus());
		//保存路径
		String path="/realname/" + user.getBrandId()+"/"+phone;
		File mkdir=new File(realNameReadPathUploadPath + path);
		if(!mkdir.exists()){
			mkdir.mkdirs();
		}

		String idCardPath = null;
		String personPath = null;
		File dest1 = null;
		File dest2 = null;
		int i = 0;
		for (MultipartFile file : files) {
			//this.uploadPhotoToALiOSS(file, i+"", ossObjectNamePrefix);
			String filename = file.getOriginalFilename();
			System.out.println("文件名=========" + filename);
			String substring = filename.substring(filename.lastIndexOf("."));
			if(i==0){
				idCardPath = mkdir +"/"+"idCardImage" + substring;
				//idCardPath = "E://Download" +"/"+"idCardImage" + substring ;
				dest1 = new File(idCardPath);
				System.out.println("dest:====="+ dest1);
				try {
					if(dest1.exists()){
						dest1.delete();
					}
					file.transferTo(dest1);
					Runtime.getRuntime().exec("chmod 777 " + dest1.getAbsolutePath());
				} catch (Exception e) {
					LOG.error("保存实名图片出错啦======");
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
				}
			}else if(i==1){
				personPath = mkdir +"/"+"personImage" + substring ;
				//personPath = "E://Download" +"/"+"personImage" + substring ;
				dest2 = new File(personPath);
				System.out.println(dest2);
				try {
					if(dest2.exists()){
						dest2.delete();
					}
					file.transferTo(dest2);
					Runtime.getRuntime().exec("chmod 777 " + dest2.getAbsolutePath());
				} catch (Exception e) {
					LOG.error("保存实名图片出错啦======");
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
				}
			}else{
				File dest = new File(mkdir +"/"+"liveImage"+substring);
				System.out.println(dest);
				try {
					if(dest.exists()){
						dest.delete();
					}
					file.transferTo(dest);
					//Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
				} catch (Exception e) {
					LOG.error("保存实名图片出错啦======");
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
				}
			}
			i++;
		}

		//调用三方人脸识别API接口
		String realNameResult = match(dest1.toString(), dest2.toString());
		JSONObject jsonObject = JSONObject.fromObject(realNameResult);
		if ("0".equals(jsonObject.getString("error_code"))){
			JSONObject resultJSONObject = JSONObject.fromObject(jsonObject.getString("result"));
			double score = Double.valueOf(resultJSONObject.getString("score"));
			LOG.info("百度人脸相似度：===========" + score);
			if(score > 80){
				user.setRealnameStatus("0");
				userName = URLDecoder.decode(userName, "UTF-8");
				user.setRealname(userName);
				/*user = userLoginRegisterBusiness.saveUser(user);*/
				LOG.info("用户信息：===========" + user);
				Map<String, Object> resultMap = updateRealNameStatus(user, idCard, request);
				LOG.info("更新用户实名状态信息：===========" + resultMap.get(CommonConstants.RESP_MESSAGE));
				if (CommonConstants.SUCCESS.equals(resultMap.get(CommonConstants.RESP_CODE))){
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "实名认证成功");
					return map;
				}
			}
		}

		/*//调用三方人脸识别API接口
		Map realNameResult=faceAuthCL.FaceAuth(request,phone);
		LOG.info("实名结果==============="+realNameResult);
		if(CommonConstants.SUCCESS.equals(realNameResult.get("resp_code"))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "实名认证成功");
			return map;
		}*/


		//user.setRealnameStatus("0");
		user = userLoginRegisterBusiness.saveUser(user);


		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "上传成功，等待审核");

		/**
		 * 推送消息 /v1.0/user/jpush/tset
		 */

		/*String alert = "实名认证提示";
		String content = "亲爱的会员，实名认证正在审核中....";
		String btype = "realnameStatus";
		String btypeval = "0";

		*//** 获取身份证实名信息 *//*
		userJpushService.setJpushtest(request, user.getId(), alert, content, btype, btypeval);*/

		//关闭自动审核（）

		/*if(files.size()==3){
			this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "0");
		}else{
			this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "2");
		}*/
		return map;
	}

	/**
	 * 请求百度人脸API
	 * @return
	 */
	public String match(String idCardPath,String personPath) {
		// 请求url
		String url = baiDuUrl;
		try {
			LOG.info("idCardPath:" + idCardPath);
			LOG.info("personPath:" + personPath);
			byte[] idCardByte = FileUtil.readFileByBytes(idCardPath);
			byte[] personByte = FileUtil.readFileByBytes(personPath);
			/*String idCardImage = new String (Base64.encodeBase64(idCardByte));
			String personImage = new String (Base64.encodeBase64(personByte));*/
			String idCardImage = Base64Util.encode(idCardByte);
			String personImage = Base64Util.encode(personByte);

			/*LOG.info("===========idCardImageBASE64==============" + idCardImage);
			LOG.info("===========personImageBASE64==============" + personImage);*/

			List<Map<String, Object>> images = new ArrayList<>();

			Map<String, Object> map1 = new HashMap<>();
			map1.put("image", personImage);
			map1.put("image_type", "BASE64");
			map1.put("face_type", "LIVE");
			map1.put("quality_control", "NONE");
			map1.put("liveness_control", "NONE");

			Map<String, Object> map2 = new HashMap<>();
			map2.put("image", idCardImage);
			map2.put("image_type", "BASE64");
			map2.put("face_type", "CERT");
			map2.put("quality_control", "NONE");
			map2.put("liveness_control", "NONE");

			images.add(map1);
			images.add(map2);

			String param = GsonUtils.toJson(images);

			// 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
			String accessToken = this.getAuth(AK,SK);

			String result = HttpUtil.post(url, accessToken, "application/json", param);
			LOG.info("百度实名认证结果：=============="+result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取API访问token
	 * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
	 * @param ak - 百度云官网获取的 API Key
	 * @param sk - 百度云官网获取的 Securet Key
	 * @return assess_token 示例：
	 * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
	 */
	public String getAuth(String ak, String sk) {
		// 获取token地址
		String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
		String getAccessTokenUrl = authHost
				// 1. grant_type为固定参数
				+ "grant_type=client_credentials"
				// 2. 官网获取的 API Key
				+ "&client_id=" + ak
				// 3. 官网获取的 Secret Key
				+ "&client_secret=" + sk;
		try {
			URL realUrl = new URL(getAccessTokenUrl);
			// 打开和URL之间的连接
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			/*// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.err.println(key + "--->" + map.get(key));
			}*/
			// 定义 BufferedReader输入流来读取URL的响应
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String result = "";
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			/**
			 * 返回结果示例
			 */
			JSONObject jsonObject = JSONObject.fromObject(result);
			String access_token = jsonObject.getString("access_token");
			return access_token;
		} catch (Exception e) {
			LOG.info("获取token失败！");
			e.printStackTrace(System.err);
		}
		return null;
	}


	// 实名认证
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upload/realname/idCard")
	public @ResponseBody Object realnameCertification(HttpServletRequest request,
													  @RequestParam(value = "brandId", required = false, defaultValue = "-1") String brandIds,
													  @RequestParam(value = "picNum") String picNum, @RequestParam(value = "phone") String phone,
													  @RequestParam(value = "userId") long userId) {
		long brandid = Long.valueOf(brandIds);
		Map<String,Object> resultMap = new HashMap<String, Object>();
		User user;
		Brand brand = null;
		if (brandid == -1) {
			user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
		} else {
			brand = brandManageBusiness.findBrandById(brandid);
			if (brand != null && "6".equals(brand.getBrandType())) {
				user = userLoginRegisterBusiness.findUserByIdAndBrandId(userId, brandid);
			} else {
				user = userLoginRegisterBusiness.queryUserById(userId);
			}
		}
//		String src="/"+user.getBrandId()+ "/realname/"+phone;
		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
		String ossObjectName = "";

		// 判断用户的实名状态
		String status = userLoginRegisterBusiness.findStatusByUserId(userId);
		if (status.equals("0") || status.equals("1")) {
			resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			resultMap.put(CommonConstants.RESP_MESSAGE, "请勿重复实名");
		} else {

			if (status.equals("2") || status.equals("3")) {
				List<UserBankInfo> bankInfos = userBankInfoBusiness.queryUserBankInfoByUserid(userId);
				if (bankInfos != null) {
					userBankInfoBusiness.updateBankCardByUserId(userId);
				}
			}
			MultipartHttpServletRequest multipartRequest;

			List<MultipartFile> files = null;
			try {
				multipartRequest = (MultipartHttpServletRequest) request;
				files = multipartRequest.getFiles("image");

			} catch (Exception e1) {
				LOG.info(e1.getMessage());
			}

			/*File dir = new File(realnamePic + src );
			// 创建目录
			if (dir.mkdirs()) {
				System.out.println("创建目录" + realnamePic +src+ "成功！");

			} else {

				File[] tempfiles = dir.listFiles();
				for (File file : tempfiles) {
					// file.delete();
				}

				System.out.println("创建目录" + realnamePic + src+ "失败！");
			}*/

			/**
			 * 推送消息 /v1.0/user/jpush/tset
			 */

			/*String alert = "实名认证提示";
			String content = "亲爱的会员，实名认证正在审核中....";
			String btype = "realnameStatus";
			String btypeval = "0";

			*//** 获取身份证实名信息 *//*
			userJpushService.setJpushtest(request, user.getId(), alert, content, btype, btypeval);*/

			String host = "https://dm-51.data.aliyun.com";
			String path = "/rest/160601/ocr/ocr_idcard.json";
			String method = "POST";
			String appcode = "a5d1104a6f73467f85a372562ea69d55";
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "APPCODE " + appcode);
			headers.put("Content-Type", "application/json; charset=UTF-8");
			Map<String, String> querys = new HashMap<String, String>();
			boolean flag = false;

			Map<String, String> map = new HashMap<String, String>();
			if (picNum.equals("0")) {
				// 对正面图片进行base64编码
				String faceimgBase64 = "";
				try {
					MultipartFile fileFace = files.get(0);
//					byte[] content = files.get(0).getBytes();
					OutputStream os = new ByteArrayOutputStream();
					PhotoCompressUtil.compressPhoto(files.get(0).getInputStream(), os, 0.2f);
					ByteArrayInputStream inputStream = FileUtils.parse(os);
					byte[] content = IOUtils.toByteArray(inputStream);
					faceimgBase64 = new String(Base64.encodeBase64(content));
				} catch (Exception e) {
					LOG.error("",e);
					resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					resultMap.put(CommonConstants.RESP_MESSAGE, "图片编码时出现异常");
					return resultMap;
				}

				String bodys = "{\"inputs\":[{\"image\":{\"dataType\":50,\"dataValue\":" + "\"" + faceimgBase64 + "\""
						+ "}," + "\"configure\":{\"dataType\":50,\"dataValue\":\"{\\\"side\\\":\\\"face\\\"}\"}}]}";
				HttpResponse response;
				try {
					response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
					LOG.info("=====================身份证正面验证状态====================="+ response.getStatusLine().getStatusCode());
					if (response.getStatusLine().getStatusCode() == 200) {
						System.out.println(response.toString());
						String str = EntityUtils.toString(response.getEntity());
						JSONObject jsonObject = JSONObject.fromObject(str);
						String resultobj = jsonObject.getString("outputs");
						LOG.info("身份证识别返回====="+resultobj);
						JSONArray jsonArray = JSONArray.fromObject(resultobj);

						String output = jsonArray.getJSONObject(0).getJSONObject("outputValue").getString("dataValue"); // 取出结果json字符串
						JSONObject out = JSONObject.fromObject(output);
						String address = out.getString("address");
						String realname = out.getString("name");
						String idCard = out.getString("num");
						String birth = out.getString("birth");
						map.put("address", address);
						map.put("name", realname);
						map.put("idCard", idCard);
						map.put("birth", birth);
						String birthDate =  birth.substring(0,4);
						int birthYear = Integer.valueOf(birthDate);
						int nowYear = Integer.valueOf(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy"));
						if (nowYear - birthYear < 18 || nowYear - birthYear > 80) {
							resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							resultMap.put(CommonConstants.RESP_MESSAGE, "很抱歉,仅对年龄18岁~80岁用户进行实名认证!");
							return resultMap;
						} else {
							URI uri = util.getServiceUrl("paymentchannel", "error url request!");
							String url = uri.toString() + "/v1.0/paymentchannel/realname/auth/backstage";
							/** 根据的用户userid查询用户的基本信息 */
							MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("userid", userId + "");
							requestEntity.add("realname", realname);
							requestEntity.add("idcard", idCard);
							RestTemplate restTemplate = new RestTemplate();
							String result = restTemplate.postForObject(url, requestEntity, String.class);
							JSONObject fs = JSONObject.fromObject(result);
							System.out.println(fs.getString("resp_code"));
							String msgResult = fs.getJSONObject("result").getString("result");
							if (fs.getString("resp_code").equals("000000") && msgResult.equals("1")) {
								resultMap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
								resultMap.put("faceMsg", map);
								resultMap.put(CommonConstants.RESP_MESSAGE, "成功");
								flag = true;
							} else {
								resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
								resultMap.put(CommonConstants.RESP_MESSAGE, "审核失败");
								LOG.info("=====================身份证正面审核错误" + response.getStatusLine().getStatusCode()
										+ "========================");
								return resultMap;
							}
						}
					}
				} catch (Exception e) {
					LOG.error("",e);
					resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					resultMap.put(CommonConstants.RESP_MESSAGE, "身份证正面审核出现异常");
					return resultMap;
				}

			}

			if (picNum.equals("1")) {
				// 对反面图片进行base64编码
				String backimgBase64 = "";
				try {
//					byte[] content = files.get(0).getBytes();
					OutputStream os = new ByteArrayOutputStream();
					PhotoCompressUtil.compressPhoto(files.get(0).getInputStream(), os, 0.2f);
					ByteArrayInputStream inputStream = FileUtils.parse(os);
					byte[] content = IOUtils.toByteArray(inputStream);

					backimgBase64 = new String(Base64.encodeBase64(content));
					String bodys2 = "{\"inputs\":[{\"image\":{\"dataType\":50,\"dataValue\":" + "\"" + backimgBase64
							+ "\"" + "},"
							+ "\"configure\":{\"dataType\":50,\"dataValue\":\"{\\\"side\\\":\\\"back\\\"}\"}}]}";
					HttpResponse responseBk = HttpUtils.doPost(host, path, method, headers, querys, bodys2);
					LOG.info("=====================身份证反面验证状态====================="
							+ responseBk.getStatusLine().getStatusCode());
					JSONObject backJson = null;
					if (responseBk.getStatusLine().getStatusCode() == 200) {
						flag = true;
						String str = EntityUtils.toString(responseBk.getEntity());
						JSONObject jsonObject = JSONObject.fromObject(str);
						String resultobj = jsonObject.getString("outputs");
						LOG.info("身份证识别返回====="+resultobj);
						JSONArray jsonArray = JSONArray.fromObject(resultobj);

						String output = jsonArray.getJSONObject(0).getJSONObject("outputValue").getString("dataValue"); // 取出结果json字符串
						JSONObject out = JSONObject.fromObject(output);
						String startDate = out.getString("start_date");
						String endDate = out.getString("end_date");
						String issue = out.getString("issue");
						map.put("startDate", startDate);
						map.put("endDate", endDate);
						map.put("issue", issue);
						// 修改用户实名状态
						userLoginRegisterBusiness.updateUserStatusByUserId(userId);
						resultMap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						resultMap.put("backMsg", map);
						resultMap.put(CommonConstants.RESP_MESSAGE, "成功");
						flag = true;

					} else {
						resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						resultMap.put(CommonConstants.RESP_MESSAGE, "审核失败");
						LOG.info("=====================身份证正面审核错误" + responseBk.getStatusLine().getStatusCode()
								+ "========================");
					}
				} catch (IOException e1) {
					LOG.error("",e1);
					resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					resultMap.put(CommonConstants.RESP_MESSAGE, "身份证背面审核出现异常");
					return resultMap;
				} catch (Exception e) {
					LOG.error("",e);
					resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					resultMap.put(CommonConstants.RESP_MESSAGE, "身份证背面审核出现异常");
					return resultMap;
				}
			}

			List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
			if (listFiles != null && listFiles.size() > 1) {
				for (String fileName : listFiles) {
					aliOSSUtil.deleteFileFromOss(fileName);
				}
			}

			if (files != null && files.size() > 0 && flag) {
				int i = 1;
				for (MultipartFile file : files) {
					String fileName = file.getOriginalFilename();
					String prefix=fileName.substring(fileName.lastIndexOf("."));
					fileName = System.currentTimeMillis()+i+prefix;
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

					/*File dest = new File(realnamePic + src+ "/" + fileName);
			    	dest.setExecutable(true, false);
			    	dest.setReadable(true, false);
			    	dest.setWritable(true, false);
			    	dest.setWritable(true);
			    	dest.setExecutable(true);
			    	dest.setReadable(true);
					try {
						file.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (IllegalStateException e) {
						e.printStackTrace();LOG.error("",e);
						resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						resultMap.put(CommonConstants.RESP_MESSAGE, "参数非法异常");
						return resultMap;
					} catch (IOException e) {
						e.printStackTrace();LOG.error("",e);
						resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						resultMap.put(CommonConstants.RESP_MESSAGE, "IO异常");
						return resultMap;
					}*/
					i++;
				}
			}

			// 将图片保存在服务器中
			/*if (flag) {
				if (files != null && files.size() > 0) {
					for (MultipartFile file : files) {
						String photoName = null;
						String fileName = file.getOriginalFilename();
						int index  = fileName.lastIndexOf(".");
						String lastName = fileName.substring(index, fileName.length());
					    if(picNum.equals("0")){
					    	 photoName = "face"+lastName;
					    }else{
					       	 photoName = "back"+lastName;
					    }

						File dest = new File(realnamePic + src  + "/" + photoName);
				    	dest.setExecutable(true, false);
				    	dest.setReadable(true, false);
				    	dest.setWritable(true, false);
				    	dest.setWritable(true);
				    	dest.setExecutable(true);
				    	dest.setReadable(true);
				    	if(dest.exists()) {
							dest.delete();
						}
						try {
							file.transferTo(dest);
							Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
						} catch (IllegalStateException e) {
							e.printStackTrace();LOG.error("",e);
							resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							resultMap.put(CommonConstants.RESP_MESSAGE, "保存图片时参数异常");
							return resultMap;
						} catch (IOException e) {
							e.printStackTrace();LOG.error("",e);
							resultMap.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							resultMap.put(CommonConstants.RESP_MESSAGE, "上传照片发生异常");
							return resultMap;
						}
					}
				}
			}*/
			/**
			 * OCR
			 * **/
			if ("1".equals(picNum)) {
				if(listFiles.size()>= 1){
					this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "1");
				}else{
					this.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "2");
				}
			}
		}
		return resultMap;
	}

	//根据brandid和status获取一定时间段内的信息
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/query/querynamebystatus")
	public Object queryFullNameByStatus(HttpServletRequest request,
										@RequestParam(value = "brand_id",defaultValue="0",required=false) long brandId,
										@RequestParam(value = "realnameStatus",defaultValue="0,1,2,3",required=false) ArrayList<String> status,
										@RequestParam(value = "start_time",required=false) String startTime,
										@RequestParam(value = "end_time",required=false) String endTime) {
		Map<String,Object> map = new HashMap<String,Object>();
		try {

			Date startTimeDate = null;
			if (startTime != null && !startTime.equalsIgnoreCase("")) {
				try {
					startTimeDate = DateUtil.getDateFromStr(startTime);
				} catch (Exception e) {
					startTimeDate = DateUtil.getDateFromStr("2017-05-01");
				}
			} else {
				startTimeDate = DateUtil.getDateFromStr("2017-05-01");
			}
			Date endTimeDate = null;
			if (endTime != null && !endTime.equalsIgnoreCase("")) {
				endTimeDate = DateUtil.getDateFromStr(endTime);
			} else {
				endTimeDate = new Date();
			}

			int number = 0;
			if (brandId == 0) {
				number = userCoinBusiness.findFullNameByStatus(status, startTimeDate, endTimeDate);
			}else {
				number = userCoinBusiness.findFullNameByStatus(brandId, status, startTimeDate, endTimeDate);
			}

			if (number == 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "查询成功，无相关数据");
				map.put(CommonConstants.RESULT, 0);
			}else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "查询成功");
				map.put(CommonConstants.RESULT, number);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
		}
		return map;
	}

	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/get/udun/realname/info")
	public @ResponseBody Object getUDunRealNameInfo(HttpServletRequest request,
													String userId
	) {
		return ResultWrap.init(CommonConstants.SUCCESS, "获取成功",getUDunRealNameInfo());
	}

	private static Map<String,Object> getUDunRealNameInfo() {
		Map<String,Object> map = new HashMap<>();
		// 订单号商户自己生成：不超过36位，非空，不能重复
		String partner_order_id = UUIDGenerator.getUUID();
		//商户pub_key ： 开户时通过邮件发送给商户
		//String pubKey = "a4f09bb0-556f-4173-892d-df21031edadf";		//百也特
		String pubKey = "a9b9ed00-df11-48da-9404-16c52a2527e7";			//易百管家
		//签名时间：有效期5分钟，请每次重新生成 :签名时间格式：yyyyMMddHHmmss
		String sign_time = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHmmss");
		// 商户 security_key  ：  开户时通过邮件发送给商户
		//String security_key = "9ee4017c-d7ee-4501-a6a7-fe6d9f46f466"; //全易生活
		String security_key = "84aa97c5-1b7d-42b1-90d7-85bd89eea5ab";	//易百管家
		// 签名规则
		String singStr = "pub_key=" + pubKey + "|partner_order_id=" + partner_order_id + "|sign_time=" + sign_time + "|security_key=" + security_key;
		//生成 签名
		String sign = Md5Util.getMD5(singStr);
		/** 以上签名 请在服务端生成，防止key泄露 */
		map.put("pubKey", pubKey);
		map.put("partner_order_id", partner_order_id);
		map.put("sign_time", sign_time);
		map.put("sign", sign);
		return map;
	}


	public Map<String, Object> updateRealNameStatus(User user,String idCard,HttpServletRequest request) {
		String url = "http://paymentchannel/v1.0/paymentchannel/realname/add/realname";
		Map<String, Object> map = new HashMap<>();
		if (user == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "user为空！");
			return map;
		}
		try {
			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
			requestEntity.add("userId", user.getId() + "");
			requestEntity.add("realname", user.getRealname());
			requestEntity.add("idCard", idCard);
			requestEntity.add("result", "1");
			requestEntity.add("message", "匹配");
			restTemplate.postForEntity(url, requestEntity, String.class);
			this.updateRealnameStatus(request, user.getBrandId() + "", user.getPhone(), "1");
		} catch (RestClientException e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "实名信息更新失败！");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "实名信息更新成功！");
		return map;
	}
	/**
	 * 查询当前实名认证类型，0为直接通过 1 创蓝  2 Face++ 3 百度API
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET,value="/v1.0/user/realname/findOnOff")
	@ResponseBody
	public Object realNameOnOff(){
		Map map=new HashMap<>();
		RealNameType realNameType=realNameTypeBusiness.queryRealNameType();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE,"查询成功");
		map.put(CommonConstants.RESULT,realNameType);
		return map;
	}
}
