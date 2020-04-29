package com.jh.user.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserShopsBusiness;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserShops;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.TokenUtil;

@Controller
@EnableAutoConfiguration
public class UserShopsService {
	@Autowired
	Util util;

	private static final Logger LOG = LoggerFactory.getLogger(UserShopsService.class);

	@Value("${user.shops.uploadpath}")
	private String realnamePic;

	@Value("${user.shops.downloadpath}")
	private String downloadPath;

	@Value("${user.pay.url}")
	private String payUrl;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	private UserShopsBusiness userShopsBusiness;
	
	@Autowired
	private UserJpushService userJpushService;

	/** 读取商铺 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/shops/query/uid")
	public @ResponseBody Object downloadUserPic(HttpServletRequest request,
			@RequestParam(value = "userid") long userid) {
		Map map = new HashMap();
		UserShops us = userShopsBusiness.findUserShopsByUid(userid);
		if (us == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "该用户未上传商铺信息");
			return map;
		}
		List<String> filepaths = new ArrayList<String>();
		File file = new File(realnamePic + us.getUserId());

		String[] filelist = file.list();
		if (filelist != null) {
			for (int i = 0; i < filelist.length; i++) {
				filepaths.add(downloadPath + us.getUserId() + "/" + filelist[i]);
			}

		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, us);
		map.put("filepaths", filepaths);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 更改店铺状态
	 **/
	/** 读取商铺 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/shops/update/uid")
	public @ResponseBody Object downloadUserPic(HttpServletRequest request, /**
																			 * 用户Id编号
																			 */
			@RequestParam(value = "userid") long userid,
			/**
			 * 状态 0、未审核；1、审核成功；2：审核失败
			 */
			@RequestParam(value = "status") String status) {
		Map map = new HashMap();
		UserShops us = userShopsBusiness.findUserShopsByUid(userid);
		User user = userLoginRegisterBusiness.queryUserById(userid);
		if (us == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "该用户未上传商铺信息");
			return map;
		}
		if (status != null && !status.equals("")) {
			if (Integer.parseInt(status) == 1) {
				us.setSrc(payUrl + "?phone=" + user.getPhone());
			} else {
				us.setSrc(null);
			}
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "该用户未上传商铺信息");
			return map;

		}
		us.setStatus(status);

		us.setCreateTime(new Date());
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, userShopsBusiness.addUserShops(us));
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");

		user.setShopsStatus(status);
		userLoginRegisterBusiness.saveUser(user);
		/**
		 * 推送消息 /v1.0/user/jpush/tset
		 */

		String alert = "店铺认证提示";
		String content = "亲爱的会员，店铺认证已通过审核";
		// 1表示审核通过 2表示审核拒绝
		if (status.equals("2")) {
			content = "亲爱的会员，店铺认证审核失败";
		}
		String btype = "shopStatus";
		String btypeval = status;
		/** 获取身份证实名信息 */
//		URI uri = util.getServiceUrl("user", "error url request!");
//		String url = uri.toString() + "/v1.0/user/jpush/tset";
//		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//		requestEntity.add("userId", userid + "");
//		requestEntity.add("alert", alert + "");
//		requestEntity.add("content", content + "");
//		requestEntity.add("btype", btype + "");
//		requestEntity.add("btypeval", btypeval + "");
//		RestTemplate restTemplate = new RestTemplate();
//		restTemplate.postForObject(url, requestEntity, String.class);
		userJpushService.setJpushtest(request, userid, alert, content, btype, btypeval);
		return map;
	}

	/**
	 * 店铺添加数据
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/shops/add/{token}")
	public @ResponseBody Object addUserPic(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "name") String name, @RequestParam(value = "address") String address,
			@RequestParam(value = "management_form", required = false, defaultValue = "") String ManagementForm,
			@RequestParam(value = "shopsaddress") String shopsaddress) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;

		}
		try {
			name = URLDecoder.decode(name, "UTF-8");
			address = URLDecoder.decode(address, "UTF-8");
			shopsaddress = URLDecoder.decode(shopsaddress, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();LOG.error("",e);
		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		long src = user.getId();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> files = multipartRequest.getFiles("image");

		File dir = new File(realnamePic + src);
		// 创建目录
		if (dir.mkdirs()) {

		} else {

			File[] tempfiles = dir.listFiles();
			for (File file : tempfiles) {
				file.delete();
			}

			System.out.println("创建目录" + realnamePic + src + "失败！");
		}
		int i = 1;

		if (files != null && files.size() > 0) {
			for (MultipartFile file : files) {

				String fileName = file.getOriginalFilename();
				String prefix = fileName.substring(fileName.lastIndexOf("."));
				i++;
				File dest = new File(realnamePic + src + "/" + System.currentTimeMillis() + i + prefix);
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
			}
		}
		UserShops us = userShopsBusiness.findUserShopsByUid(userId);
		if (us == null)
			us = new UserShops();
		us.setUserId(userId);
		us.setName(name);
		us.setAddress(address);
		us.setShopsaddress(shopsaddress);
		us.setManagementForm(ManagementForm);
		us.setSrc(src + "");
		us.setStatus("0");
		us.setCreateTime(new Date());

		user.setShopsStatus(us.getStatus());
		userLoginRegisterBusiness.saveUser(user);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, userShopsBusiness.addUserShops(us));
		return map;
	}

	/**
	 * 店铺添加数据
	 *//*
		 * @RequestMapping(method=RequestMethod.POST,value=
		 * "/v1.0/user/shops/add",produces="text/html;charset=UTF-8")
		 * public @ResponseBody Object uploadUserPic(HttpServletRequest request,
		 * 
		 * @RequestParam(value = "name") String name,
		 * 
		 * @RequestParam(value = "address") String address,
		 * 
		 * @RequestParam(value = "shopsaddress") String shopsaddress ){ long src
		 * =System.currentTimeMillis(); long userId=12312312; try {
		 * URLDecoder.decode(name, "UTF-8"); URLDecoder.decode(address,
		 * "UTF-8"); URLDecoder.decode(address, "UTF-8"); } catch
		 * (UnsupportedEncodingException e) { // TODO Auto-generated catch block
		 * e.printStackTrace();LOG.error("",e); } UserShops
		 * us=userShopsBusiness.findUserShopsByUid(userId); if(us==null) us=new
		 * UserShops(); us.setName(name); us.setAddress(address);
		 * us.setUserId(userId); us.setShopsaddress(shopsaddress);
		 * us.setSrc(src+""); us.setCreateTime(new Date());
		 * 
		 * 
		 * Map map = new HashMap();
		 * map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		 * map.put(CommonConstants.RESP_MESSAGE, "成功");
		 * map.put(CommonConstants.RESULT, userShopsBusiness.addUserShops(us));
		 * return map; }
		 */

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/shops/find/province")
	public @ResponseBody Object findProvince(HttpServletRequest request,
			@RequestParam(name = "userId") String suserId) {
		Map map = new HashMap();
		long userId;
		UserShops model = null;
		try {
			userId = Long.valueOf(suserId);

			model = userShopsBusiness.findUserShopsByUid(userId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲,查询出错了哦");
			return map;
		}

		// List<UserShops> models = userShopsBusiness.findUserShops();
		if (model != null) {

			String province = null;
			String city = null;
			// if (models.size() > 0) {
			// LOG.info("" + models.size());
			String str = null;
			String[] strs = null;
			// for (UserShops model : models) {
			str = model.getAddress();
			strs = str.split("-");
			if (strs.length > 1) {
				// LOG.info("**********");
				// LOG.info(strs[0]);
				// LOG.info(strs[1]);
				province = strs[0];
				city = strs[1];
				// LOG.info("**********");
			} else {
				strs = str.split("省");
				if (strs.length > 1) {
					// LOG.info("**********");
					// LOG.info(strs[0]+"省");
					province = strs[0] + "省";
					str = strs[1];
					if ((strs = str.split("市")).length > 1) {
						// LOG.info(strs[0]+"市");
						city = strs[0] + "市";
					}
					// LOG.info("**********");
				} else {
					strs = str.split("区");
					if (strs.length > 1) {
						// LOG.info("**********");
						// LOG.info(strs[0] + "区");
						province = strs[0] + "区";
						str = strs[1];
						if ((strs = str.split("市")).length > 1) {
							// LOG.info(strs[0] + "市");
							city = strs[0] + "市";
						} else {
							// LOG.info(str + "市");
							city = strs[0] + "市";
						}
						LOG.info("**********");
					} else {
						strs = str.split("市");
						if (strs.length > 1) {
							// LOG.info(strs[0] + "市");
							province = strs[0] + "市";
							city = province;
						}
					}
				}
			}
//			LOG.info(province);
//			LOG.info(city);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询省份城市成功");
			map.put("province", province);
			map.put("city", city);
			return map;
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲,没有找到查询的商户哦!");
			return map;
		}
	}
}
