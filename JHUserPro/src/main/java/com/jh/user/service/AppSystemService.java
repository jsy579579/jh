package com.jh.user.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.jh.common.utils.DateUtil;
import com.jh.user.business.*;
import com.jh.user.business.impl.UserDTOBusinessImpl;
import com.jh.user.pojo.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v2_1.ast.Foreach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import com.jh.user.util.AliOSSUtil;
import com.jh.user.util.Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.TokenUtil;


@SuppressWarnings("ALL")
@Controller
@EnableAutoConfiguration
public class AppSystemService {
	@Autowired
	Util util;

	private static final Logger LOG = LoggerFactory.getLogger(AppSystemService.class);

	@Value("${user.appsys.uploadpath}")
	private String realnamePic;

	@Value("${user.appsys.downloadpath}")
	private String downloadPath;

	@Value("${user.pay.url}")
	private String payUrl;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	private ThirdLeveDistributionBusiness thirdLevelBusiness;

	@Autowired
	private AppBrandBusiness appBrandBusiness;

	@Autowired
	private AliOSSUtil aliOSSUtil;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UpGradeDetailBusiness upGradeDetailBusiness;

    @Autowired
	private UserRelationBusiness userRelationBusiness;

    @Autowired
    private UserRebateHistoryBusiness userRebateHistoryBusiness;

	/**
	 * 读取轮播图
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/slideshow/query/brandid")
	public @ResponseBody
	Object downloadAppSlideshow(HttpServletRequest request,
								@RequestParam(value = "brand_id") long brandid
	) {
		Map map = new HashMap();
		List<AppSlideshow> AppSlideshows = appBrandBusiness.findAppSlideshowByBrandId(brandid);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, AppSlideshows);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 删除轮播图
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/slideshow/delect/id")
	public @ResponseBody
	Object delAppSlideshow(HttpServletRequest request,
						   @RequestParam(value = "id") long id
	) {
		Map map = new HashMap();
		AppImageText appImageText = appBrandBusiness.findAppImageTextById(id);
		if (appImageText != null) {
			String ossObjectNamePrefix = AliOSSUtil.APP_SYS_SLIDESHOW + "-" + appImageText.getBrandId() + "-";
			String imgurl = appImageText.getImgurl();
			if (imgurl != null && !"".equals(imgurl) && imgurl.contains(ossObjectNamePrefix)) {
				String objectName = imgurl.substring(imgurl.indexOf(ossObjectNamePrefix), imgurl.indexOf("?"));
				aliOSSUtil.deleteFileFromOss(objectName);
			}
		}
		appBrandBusiness.delAppSlideshowById(id);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 轮播图
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/slideshow/add/{token}")
	public @ResponseBody
	Object addAppSlideshow(HttpServletRequest request,
						   @PathVariable("token") String token,
						   @RequestParam(value = "url") String url,
						   @RequestParam(value = "title") String title,
						   @RequestParam(value = "id", defaultValue = "0", required = false) long id
	) {
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
			title = URLDecoder.decode(title, "UTF-8");
			url = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			LOG.error("", e);
		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		String src = "slideshow/" + user.getBrandId();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multipartRequest.getFile("image");
		if (file == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请添加图片，图片不能为空");
			return map;
		}
		   	/*File dir = new File(realnamePic+src); 
	    	//创建目录  
	        if (dir.mkdirs()) {  
	           
	        } else {  
	        	
	        } */

		String ossObjectNamePrefix = AliOSSUtil.APP_SYS_SLIDESHOW + "-" + user.getBrandId() + "-";

		String fileName = file.getOriginalFilename();
		String prefix = fileName.substring(fileName.lastIndexOf("."));
		fileName = System.currentTimeMillis() + prefix;

		String ossObjectName = ossObjectNamePrefix + fileName;
		try {
			aliOSSUtil.uploadStreamToOss(ossObjectName, file.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
			LOG.error(ExceptionUtil.errInfo(e1));
		}
	    	
	    	/*File  dest = new File(realnamePic+src+"/"+fileName);
	    	try {
				file.transferTo(dest);
			} catch (IllegalStateException e) {
				e.printStackTrace();LOG.error("",e);
			} catch (IOException e) {
				e.printStackTrace();LOG.error("",e);
			}*/

		String fileUrl = aliOSSUtil.getFileUrl(ossObjectName);

		AppSlideshow appSlideshow = appBrandBusiness.findAppSlideshowById(id);
		if (appSlideshow == null)
			appSlideshow = new AppSlideshow();
		appSlideshow.setBrandId(user.getBrandId());
		appSlideshow.setTitle(title);
		appSlideshow.setCreateTime(new Date());
		appSlideshow.setUrl(url);
//		   	appSlideshow.setImgurl(downloadPath+src+"/"+dest.getName());
		appSlideshow.setImgurl(fileUrl);
		appSlideshow.setCreateTime(new Date());
		appSlideshow.setUpdatetime(new Date());

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, appBrandBusiness.addAppSlideshow(appSlideshow));
		return map;
	}


	/***=======================================图文库================================
	 * @throws UnsupportedEncodingException ****/


	/**
	 * 删除轮播图
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/imagetext/delect/id")
	public @ResponseBody
	Object delAppText(HttpServletRequest request,
					  @RequestParam(value = "id") long id
	) {
		Map map = new HashMap();
		AppImageText appImageText = appBrandBusiness.findAppImageTextById(id);
		String ossObjectNamePrefix = AliOSSUtil.APP_SYS_SLIDESHOW + "-" + appImageText.getBrandId() + "-";
		List<String> listFiles = new ArrayList<String>();
		if (appImageText.getImgurl() != null && !appImageText.getImgurl().equals("") && appImageText.getImgurl().contains(ossObjectNamePrefix)) {
			String[] urls = appImageText.getImgurl().split(",");
			for (String string : urls) {
				String objectName = string.substring(string.indexOf(ossObjectNamePrefix), string.indexOf("?"));
				listFiles.add(objectName);
			}
			if (listFiles != null && listFiles.size() > 0) {
				for (String fileName : listFiles) {
					aliOSSUtil.deleteFileFromOss(fileName);
				}
			}
		}

		appBrandBusiness.delAppImageTextById(id);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/****
	 * 图文库=文
	 *
	 * ***/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/imagetext/add/txt/{token}")
	public @ResponseBody
	Object addAppText(HttpServletRequest request,
					  @PathVariable("token") String token,
					  @RequestParam(value = "content") String content,
					  @RequestParam(value = "title") String title,
					  @RequestParam(value = "remarks", defaultValue = "", required = false) String remarks,
					  @RequestParam(value = "remarks1", defaultValue = "", required = false) String remarks1,
					  @RequestParam(value = "id", defaultValue = "0", required = false) long id
	) {
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
			title = URLDecoder.decode(title, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			LOG.error("", e);
		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		AppImageText appImageText = appBrandBusiness.findAppImageTextById(id);
		if (appImageText == null) {
			appImageText = new AppImageText();
			appImageText.setRemarks(remarks);
			appImageText.setRemarks1(remarks1);
		} else {
			appImageText.setRemarks(remarks == null || remarks.equals("") ? appImageText.getRemarks() : remarks);
			appImageText.setRemarks1(remarks1 == null || remarks1.equals("") ? appImageText.getRemarks1() : remarks1);
		}

		appImageText.setBrandId(user.getBrandId());
		appImageText.setTitle(title);
		appImageText.setContent(content);
		appImageText.setCreateTime(new Date());
		appImageText.setUpdatetime(new Date());

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, appBrandBusiness.addAppImageText(appImageText));
		return map;
	}

	/****
	 * 图文库=图
	 *
	 * ***/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/imagetext/add/image/{token}")
	public @ResponseBody
	Object addAppImage(HttpServletRequest request,
					   @PathVariable("token") String token,
					   @RequestParam(value = "id", defaultValue = "0", required = false) long id
	) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;

		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		String src = "imagetext/" + user.getBrandId();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<MultipartFile> files = multipartRequest.getFiles("image");
		if (files == null || files.size() == 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请添加图片，图片不能为空");
			return map;
		}
		   	/*File dir = new File(realnamePic+src); 
	    	//创建目录  
	        if (dir.mkdirs()) {  
	           
	        } else {  
	        }*/

		String ossObjectNamePrefix = AliOSSUtil.APP_SYS_IMAGETEXT + "-" + user.getBrandId() + "-";
		String ossObjectName = "";

		int i = 1;
		String imgurls = "";
		if (files != null && files.size() > 0) {
			for (MultipartFile file : files) {
				String fileName = file.getOriginalFilename();
				if (!fileName.contains(".")) {
					continue;
				}
				String prefix = fileName.substring(fileName.lastIndexOf("."));
				fileName = System.currentTimeMillis() + i + prefix;

				ossObjectName = ossObjectNamePrefix + fileName;

				try {
					aliOSSUtil.uploadStreamToOss(ossObjectName, file.getInputStream());
				} catch (IOException e1) {
					e1.printStackTrace();
					LOG.error(ExceptionUtil.errInfo(e1));
				}

				String fileUrl = aliOSSUtil.getFileUrl(ossObjectName);
				imgurls = imgurls + fileUrl + ",";

				i++;
			    	/*File  dest = new File(realnamePic+src+"/"+fileName);
//			    	imgurls+=downloadPath+src+"/"+dest.getName()+",";
			    	try {
						file.transferTo(dest);
					} catch (IllegalStateException e) {
						e.printStackTrace();LOG.error("",e);
					} catch (IOException e) {
						e.printStackTrace();LOG.error("",e);
					}*/
			}
		}
		AppImageText appImageText = null;
		if (id == 0) {
			List<AppImageText> appImageTexts = appBrandBusiness.findAppImageTextByBrandId(user.getBrandId());
			if (appImageTexts != null && appImageTexts.size() > 0) {
				appImageText = appImageTexts.get(0);
			}

		} else {
			appImageText = appBrandBusiness.findAppImageTextById(id);
		}
		if (appImageText == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
		appImageText.setImgurl(imgurls.substring(0, imgurls.length() - 1));
		appImageText.setUpdatetime(new Date());

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, appBrandBusiness.addAppImageText(appImageText));
		return map;
	}

	/**
	 * 读取图文库
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/imagetext/query/brandid")
	public @ResponseBody
	Object downloadAppImageText(HttpServletRequest request,
								@RequestParam(value = "brand_id") long brandid
	) {
		Map map = new HashMap();
		List<AppImageText> appImageTexts = appBrandBusiness.findAppImageTextByBrandId(brandid);
		List<Map> filepaths = new ArrayList<Map>();
		for (AppImageText appImageText : appImageTexts) {
			Map appImageText_ = new HashMap();
			appImageText_.put("id", appImageText.getId());
			appImageText_.put("title", appImageText.getTitle());
			appImageText_.put("content", appImageText.getContent());
			appImageText_.put("remarks", appImageText.getRemarks());
			appImageText_.put("remarks1", appImageText.getRemarks1());
			appImageText_.put("img_url", appImageText.getImgurl() != null && !appImageText.getImgurl().equals("") ? appImageText.getImgurl().split(",") : "");
			appImageText_.put("brand_id", appImageText.getBrandId());
			appImageText_.put("create_time", appImageText.getCreateTime());
			appImageText_.put("update_time", appImageText.getUpdatetime());
			filepaths.add(appImageText_);
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, filepaths);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	/****===============================店铺管理=================================***/

	/**
	 * 读取信息
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/{token}")
	public @ResponseBody
	Object downloadAppUsersys(HttpServletRequest request,
							  @PathVariable("token") String token
	) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;

		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		/***
		 * 1、获取当前用户所在贴牌的等级信息
		 * 2、获取用户的直推userIDs
		 * 3、获取用户的所有间推用户Ids
		 * 4、循环赋予各级别的直推间推个数
		 * 5、判断未实名未实名人数，已实名人数
		 * **/
		List<ThirdLevelDistribution> thirdLevelDistributions = thirdLevelBusiness.getAllThirdLevelPrd(user.getBrandId());
		Long[] preids = {user.getId()};
		Long[] sonIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(preids);
		String grandsonIds = "";
		Long[] preuids = sonIds;
		if (sonIds != null && sonIds.length != 0 && sonIds[0] > 0) {
			for (int i = 0; true; i++) {
				Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(preuids);
				if (pids != null && pids.length > 0 && pids[0] > 0) {
					preuids = new Long[pids.length];
					preuids = pids;
					for (Long uid : pids) {
						grandsonIds += uid + ",";
					}
				} else {
					break;
				}
			}
		}

		if (grandsonIds.length() > 0) {
			grandsonIds = grandsonIds.substring(0, grandsonIds.length() - 1);
			String[] str1 = grandsonIds.split(",");
			preuids = new Long[str1.length];
			for (int j = 0; j < str1.length; j++) {
				preuids[j] = Long.valueOf(str1[j]);
			}
		} else {
			preuids = null;
		}
		/****
		 *管理等级
		 * ***/
		List<Map> gradeList = new ArrayList<Map>();
		if (thirdLevelDistributions != null)
			for (ThirdLevelDistribution thirdLevelDistribution : thirdLevelDistributions) {
				Map grade = new HashMap();
				grade.put("grade", thirdLevelDistribution.getGrade());
				grade.put("name", thirdLevelDistribution.getName());
				grade.put("TrueFalseBuy", thirdLevelDistribution.getTrueFalseBuy());
				grade.put("url", "");
				if (sonIds != null && sonIds.length > 0 && sonIds[0] > 0) {
					Long[] GradesonIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade(thirdLevelDistribution.getGrade() + "", sonIds);
					if (GradesonIds != null && GradesonIds.length > 0 && GradesonIds[0] > 0) {
						grade.put("GradesonIds", GradesonIds.length);
					} else {
						grade.put("GradesonIds", 0);
					}

				} else {
					grade.put("GradesonIds", 0);
				}
				if (preuids != null && preuids.length > 0 && preuids[0] > 0) {
					Long[] Gradepreuids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade(thirdLevelDistribution.getGrade() + "", preuids);
					if (Gradepreuids != null && Gradepreuids.length > 0 && Gradepreuids[0] > 0) {
						grade.put("Gradepreuids", Gradepreuids.length);
					} else {
						grade.put("Gradepreuids", 0);
					}

				} else {
					grade.put("Gradepreuids", 0);
				}
				gradeList.add(grade);
			}
		{
			Map grade = new HashMap();
			grade.put("grade", 0);
			grade.put("name", "普通用户");
			grade.put("url", "");
			if (sonIds != null & sonIds.length > 0 && sonIds[0] > 0) {
				Long[] GradesonIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade(0 + "", sonIds);
				if (GradesonIds != null && GradesonIds.length > 0 && GradesonIds[0] > 0) {
					grade.put("GradesonIds", GradesonIds.length);
				} else {
					grade.put("GradesonIds", 0);
				}

			} else {
				grade.put("GradesonIds", 0);
			}
			if (preuids != null && preuids.length > 0 && preuids[0] > 0) {
				Long[] Gradepreuids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade(0 + "", preuids);
				if (Gradepreuids != null && Gradepreuids.length > 0 && Gradepreuids[0] > 0) {
					grade.put("Gradepreuids", Gradepreuids.length);
				} else {
					grade.put("Gradepreuids", 0);
				}

			} else {
				grade.put("Gradepreuids", 0);
			}

			gradeList.add(grade);
		}

		//易百管家定制
		String vip = "VIP";
		String vipName = null;
		Integer vipGrade = null;
		for (Map newGrade : gradeList) {
			String name = newGrade.get("name").toString();
			if (vip.equals(name)) {
				vipName = name;
				vipGrade = new Integer(newGrade.get("grade").toString());
				break;
			}
		}
		if (vipName != null && vipGrade != null) {
			Integer vipGradesonIds = 0;
			Integer vipGradepreuids = 0;
			Map vipFrade = new HashMap<>();
			for (Map newGrade : gradeList) {
				if (new Integer(newGrade.get("grade").toString()) >= vipGrade) {
					vipGradesonIds += new Integer(newGrade.get("GradesonIds").toString());
					vipGradepreuids += new Integer(newGrade.get("Gradepreuids").toString());
				}
			}
			for (Map newGrade : gradeList) {
				if (newGrade.get("name").toString().equals(vip)) {
					vipFrade.put("GradesonIds", vipGradesonIds);
					vipFrade.put("name", vip);
					vipFrade.put("grade", newGrade.get("grade"));
					vipFrade.put("Gradepreuids", vipGradepreuids);
					vipFrade.put("url", "");
					vipFrade.put("TrueFalseBuy", newGrade.get("TrueFalseBuy"));
				}
			}
			gradeList.remove(gradeList.size() - 1);
			gradeList.remove(gradeList.size() - 1);
			gradeList.add(vipFrade);
			{
				Map grade = new HashMap();
				grade.put("grade", 0);
				grade.put("name", "普通用户");
				grade.put("url", "");
				if (sonIds != null & sonIds.length > 0 && sonIds[0] > 0) {
					Long[] GradesonIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade(0 + "", sonIds);
					if (GradesonIds != null && GradesonIds.length > 0 && GradesonIds[0] > 0) {
						grade.put("GradesonIds", GradesonIds.length);
					} else {
						grade.put("GradesonIds", 0);
					}

				} else {
					grade.put("GradesonIds", 0);
				}
				if (preuids != null && preuids.length > 0 && preuids[0] > 0) {
					Long[] Gradepreuids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade(0 + "", preuids);
					if (Gradepreuids != null && Gradepreuids.length > 0 && Gradepreuids[0] > 0) {
						grade.put("Gradepreuids", Gradepreuids.length);
					} else {
						grade.put("Gradepreuids", 0);
					}

				} else {
					grade.put("Gradepreuids", 0);
				}

				gradeList.add(grade);
			}

		}
		String talluid = "";
		if (sonIds != null) {
			for (Long uid : sonIds) {
				talluid += uid + ",";
			}
		}
		if (preuids != null) {
			for (Long uid : preuids) {
				talluid += uid + ",";
			}
		}
		Long[] talluidl = {};
		if (talluid.length() > 0) {
			talluid = talluid.substring(0, talluid.length() - 1);
			String[] str1 = talluid.split(",");
			talluidl = new Long[str1.length];
			for (int j = 0; j < str1.length; j++) {
				talluidl[j] = Long.valueOf(str1[j]);
			}
		}
		Map truefalse = new HashMap();
		Long[] truenum = null;
		if (talluidl != null && talluidl.length > 0 && talluidl[0] > 0) {
			truenum = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", talluidl);
		}


		truefalse.put("truenum", truenum == null ? 0 : truenum.length);
		truefalse.put("falsenum", truenum == null ? talluidl.length : talluidl.length - truenum.length);
		truefalse.put("thirdLevelDistribution", gradeList);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, truefalse);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}


	/**
	 * 我的社群-------壹佰管家
	 * 粉丝
	 * 社群
	 * 实名
	 * vip
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/ybgj/{token}")
	public @ResponseBody
	Object downloadAppUsersysForYBGJ(HttpServletRequest request,
							  @PathVariable("token") String token) {
		/**
		 * 1、获取用户id
		 * 2、获取用户信息
		 * 3、获取直推用户
		 * 4、获取间推用户
		 * 5、统计数据
		 */
		Map<String,Object> map = new HashMap<>();
		LOG.info("我的社群===============读取信息=============================");
		LOG.info("请求token值================================="+token);
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
			LOG.info("获取用户ID==============="+userId);
		} catch (Exception e) {
			LOG.info("===========token无效==========");
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);
		LOG.info("获取用户信息：====================="+user);
		Map<String, Object> result = new HashMap<>();
		Long[] userIds = {userId};
		// 获取直推的用户id
		Long[] downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(userIds);
		result.put("downUserCount",downUserIds.length);
		long personCount = downUserIds.length; //直推+间推用户总人数
		long realNameCount = 0;//直推+间推 实名统计
		long VIPCount = 0;//VIP人数统计

		if(downUserIds.length != 0){
			//获取直推的实名人数
			Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
			realNameCount += realNameUserIds.length;

			//获取直推的VIP人数
			Long[] ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade("0", downUserIds);
			int i = downUserIds.length - ordinaryPerson.length;
			VIPCount += i;

			//获取直推下级用户的id
			Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(downUserIds);
			while (true) {
				//判断下级是否为空
				if (pids != null && pids.length > 0 && pids[0] > 0) {
					//加下级的人数
					personCount += pids.length;
					//获得下级实名的人数
					realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", pids);
					realNameCount += realNameUserIds.length;
					//获得下级VIP的人数
					ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade("0", pids);
					i = pids.length - ordinaryPerson.length;
					VIPCount += i;
					//寻找下级的下级id
					pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(pids);
					/*downUserIds = new Long[pids.length];
					downUserIds = pids;*/
				} else {
					break;
				}
			}
		}
		result.put("personCount",personCount);
		result.put("realNameCount",realNameCount);
		result.put("VIPCount",VIPCount);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, result);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		LOG.info("RESULT:========================="+map);
		return map;
	}


	/**
	 * 直推用户列表--------壹佰管家
	 * @param request
	 * @param token
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/ybgj/fans/{token}")
	public @ResponseBody
	Object downloadAppFansUsersysForYBGJ(HttpServletRequest request,
									 @PathVariable("token") String token,
										 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
										 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
										 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
										 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		LOG.info("直推用户列表===============读取信息=============================");
		LOG.info("请求token值===============" + token);
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
			LOG.info("获取用户ID=========================================" + userId);
		} catch (Exception e) {
			LOG.info("===========token无效==========");
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);
		LOG.info("获取用户信息：====================="+user);
		Long[] userIds = {userId};
		// 获取直推的用户id
		Long[] downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(userIds);

		if (downUserIds.length != 0) {
			PageRequest pageRequest = new PageRequest(page, size, new Sort(direction, sortProperty));
			List<User> infoUsers = userLoginRegisterBusiness.findInfoUsersPageable(downUserIds, pageRequest);

			for (User u : infoUsers) {
				Map<String, Object> resultItem = new HashMap<>();
				Long[] ids = new Long[]{u.getId()};
				// 获取直推的用户id
				downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(ids);
				resultItem.put("downUserCount", downUserIds.length);
				long personCount = downUserIds.length; //直推+间推用户总人数
				long realNameCount = 0;//直推+间推 实名统计
				long VIPCount = 0;//VIP人数统计

				if (downUserIds.length != 0) {

					//获取直推的实名人数
					Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
					realNameCount += realNameUserIds.length;

					//获取直推的VIP人数
					Long[] ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade("0", downUserIds);
					int i = downUserIds.length - ordinaryPerson.length;
					VIPCount += i;

					//获取直推下级用户的id
					Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(downUserIds);

					while (true) {
						//判断下级是否为空
						if (pids != null && pids.length > 0 && pids[0] > 0) {
							//加下级的人数
							personCount += pids.length;
							//获得下级实名的人数
							realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", pids);
							realNameCount += realNameUserIds.length;
							//获得下级VIP的人数
							ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGrade("0", pids);
							i = pids.length - ordinaryPerson.length;
							VIPCount += i;
							//寻找下级的下级id
							pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(pids);
					/*downUserIds = new Long[pids.length];
					downUserIds = pids;*/
						} else {
							break;
						}
					}
				}
				resultItem.put("personCount", personCount);
				resultItem.put("realNameCount", realNameCount);
				resultItem.put("VIPCount", VIPCount);
				resultItem.put("userInfo", u);
				result.add(resultItem);

			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, result);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		map.put(CommonConstants.RESULT, result);
		map.put(CommonConstants.RESP_MESSAGE, "下级用户为空");
		return map;
	}

	/**
	 * 今日/昨日详情-------多多生活
	 * 粉丝
	 * 社群
	 * 实名
	 * vip
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/ddsh/{token}")
	public @ResponseBody Object downloadAppUsersysForDDSH(HttpServletRequest request,
									 @PathVariable("token") String token) {
		/**
		 * 1、获取用户id
		 * 2、获取用户信息
		 * 3、获取直推用户
		 * 4、获取间推用户
		 * 5、统计数据
		 */
		Map<String,Object> map = new HashMap<>();
		LOG.info("今日/昨日详情===============读取信息=============================");
		LOG.info("请求token值================================="+token);
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
			LOG.info("获取用户ID==============="+userId);
		} catch (Exception e) {
			LOG.info("===========token无效==========");
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		//User user = userLoginRegisterBusiness.queryUserById(userId);
		//LOG.info("获取用户信息：====================="+user);
		Map<String, Object> result = new HashMap<>();
		Long[] userIds = {userId};
		// 获取直推的用户id
		//Long[] downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(userIds);


		//获取所有的直推用户
		Long[] alldownUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(userIds);


		//获取每天的直推用户id
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		LOG.info("今天时间：" + date);
		Long[] downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(userIds,date);
		LOG.info("当天的直推用户id：" + downUserIds);
		result.put("todayDownUserCount",downUserIds.length);


		long personCount = downUserIds.length; //直推+间推用户总人数
		long realNameCount = 0;//直推+间推 实名统计
		long VIPCount = 0;//VIP人数统计

		String url = "http://paymentchannel/v1.0/paymentchannel/realname/find/userids/and/date";

		MultiValueMap<String, Object> requestEntity;
		String resultJSON;
		JSONObject jsonObject;

		if(alldownUserIds.length != 0){
			//获取直推的实名人数
			requestEntity = new LinkedMultiValueMap<>();
			requestEntity.add("userIds",alldownUserIds);
			requestEntity.add("date",date);
			resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(resultJSON);
			//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
			realNameCount += Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
			/*Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatusnew("1",userIds);
			realNameCount += realNameUserIds.length;*/

			//获取直推的VIP人数
			Long[] ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", alldownUserIds);
			for (Long userid : ordinaryPerson){
				boolean b = queryVip(userid, date);
				if (b){
					VIPCount ++ ;
				}
			}

			//获取直推下级用户的id
			Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(alldownUserIds,date);
			alldownUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(alldownUserIds);
			while (true) {
				//判断下级是否为空
				if (pids != null && pids.length > 0 && pids[0] > 0) {
					//加下级的人数
					personCount += pids.length;
					//获得下级实名的人数
					requestEntity = new LinkedMultiValueMap<>();
					requestEntity.add("userIds",alldownUserIds);
					requestEntity.add("date",date);
					resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
					jsonObject = JSONObject.fromObject(resultJSON);
					//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
					realNameCount += Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
					//获得下级VIP的人数
					ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", alldownUserIds);
					for (Long userid : ordinaryPerson){
						boolean b = queryVip(userid, date);
						if (b){
							VIPCount ++ ;
						}
					}
					//寻找下级的下级id
					pids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(pids,date);
					alldownUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(alldownUserIds);
					/*downUserIds = new Long[pids.length];
					downUserIds = pids;*/
				} else {
					break;
				}
			}
		}
		result.put("todayPersonCount",personCount);
		result.put("todayRealNameCount",realNameCount);
		result.put("todayVIPCount",VIPCount);




		//获取昨天的直推用户id
		date = new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime() - 86400000);
		LOG.info("昨天时间:" + date);
		alldownUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(userIds);
		downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(userIds,date);
		LOG.info("昨天的直推用户id：" + downUserIds);

		result.put("yesterdayDownUserCount",downUserIds.length);
		personCount = downUserIds.length; //直推+间推用户总人数
		realNameCount = 0;//直推+间推 实名统计
		VIPCount = 0;//VIP人数统计

		requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("userIds",alldownUserIds);
		requestEntity.add("date",date);
		if(alldownUserIds.length != 0){
			//获取直推的实名人数
			requestEntity = new LinkedMultiValueMap<>();
			requestEntity.add("userIds",alldownUserIds);
			requestEntity.add("date",date);
			resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(resultJSON);
			//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
			realNameCount += Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
			/*Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatusnew("1",userIds);
			realNameCount += realNameUserIds.length;*/

			//获取直推的VIP人数
			Long[] ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", alldownUserIds);
			for (Long userid : ordinaryPerson){
				boolean b = queryVip(userid, date);
				if (b){
					VIPCount ++ ;
				}
			}

			//获取直推下级用户的id
			Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(alldownUserIds,date);
			alldownUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(alldownUserIds);
			while (true) {
				//判断下级是否为空
				if (pids != null && pids.length > 0 && pids[0] > 0) {
					//加下级的人数
					personCount += pids.length;
					//获得下级实名的人数
					requestEntity = new LinkedMultiValueMap<>();
					requestEntity.add("userIds",alldownUserIds);
					requestEntity.add("date",date);
					resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
					jsonObject = JSONObject.fromObject(resultJSON);
					//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
					realNameCount += Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
					//获得下级VIP的人数
					ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", alldownUserIds);
					for (Long userid : ordinaryPerson){
						boolean b = queryVip(userid, date);
						if (b){
							VIPCount ++ ;
						}
					}
					//寻找下级的下级id
					pids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(pids,date);
					alldownUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIds(alldownUserIds);
					/*downUserIds = new Long[pids.length];
					downUserIds = pids;*/
				} else {
					break;
				}
			}
		}
		result.put("yesterdayPersonCount",personCount);
		result.put("yesterdayRealNameCount",realNameCount);
		result.put("yesterdayVIPCount",VIPCount);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, result);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		LOG.info("RESULT:=========================" + map);
		return map;
	}
	/**
	 * 今日直推用户列表--------多多生活
	 * @param request
	 * @param token
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/ddsh/fans/today/{token}")
	public @ResponseBody
	Object downloadAppFansUsersysForDDSHBytoday(HttpServletRequest request,
										 @PathVariable("token") String token,
										 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
										 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
										 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
										 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		LOG.info("直推用户列表===============读取信息=============================");
		LOG.info("请求token值===============" + token);
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
			LOG.info("获取用户ID=========================================" + userId);
		} catch (Exception e) {
			LOG.info("===========token无效==========");
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);
		LOG.info("获取用户信息：====================="+user);
		Long[] userIds = {userId};
		// 获取今日直推的用户id
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		Long[] downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(userIds,date);

		if (downUserIds.length != 0) {
			LOG.info("今日直推用户id========================" + downUserIds);
			PageRequest pageRequest = new PageRequest(page, size, new Sort(direction, sortProperty));
			List<User> infoUsers = userLoginRegisterBusiness.findInfoUsersPageable(downUserIds, pageRequest);
			LOG.info("今日直推用户信息========================" + infoUsers);

			String url = "http://paymentchannel/v1.0/paymentchannel/realname/findby/userids";
			MultiValueMap<String, Object> requestEntity;
			String resultJSON;
			JSONObject jsonObject;

			for (User u : infoUsers) {
				Map<String, Object> resultItem = new HashMap<>();
				Long[] ids = new Long[]{u.getId()};
				LOG.info("ids=========================================" + ids);
				// 获取直推的用户id
				downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(ids,date);
				LOG.info("直推id===========================" + downUserIds);
				resultItem.put("downUserCount", downUserIds.length);
				long personCount = downUserIds.length; //直推+间推用户总人数
				long realNameCount = 0;//直推+间推 实名统计
				long VIPCount = 0;//VIP人数统计
				requestEntity = new LinkedMultiValueMap<>();
				requestEntity.add("userIds",downUserIds);
				requestEntity.add("date",date);

				LOG.info("downUserIds.length===========================" + downUserIds.length);

				if (downUserIds.length != 0) {

					//获取直推的实名人数
					resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
					jsonObject = JSONObject.fromObject(resultJSON);
					//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
					Integer realNameUserIds = Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
					realNameCount += realNameUserIds;

					//获取直推的VIP人数
					Long[] ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", downUserIds);
					for (Long userid : ordinaryPerson){
						boolean flag = queryVip(userid, date);
						if (flag){
							VIPCount ++ ;
						}
					}

					//获取直推下级用户的id
					Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(downUserIds,date);

					while (true) {
						//判断下级是否为空
						if (pids != null && pids.length > 0 && pids[0] > 0) {
							//加下级的人数
							personCount += pids.length;
							//获得下级实名的人数
							requestEntity = new LinkedMultiValueMap<>();
							requestEntity.add("userIds",pids);
							requestEntity.add("date",date);
							resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
							jsonObject = JSONObject.fromObject(resultJSON);
							//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
							realNameUserIds = Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
							realNameCount += realNameUserIds;
							//获得下级VIP的人数
							ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", pids);
							for (Long userid : ordinaryPerson){
								boolean b = queryVip(userid, date);
								if (b){
									VIPCount ++ ;
								}
							}
							//寻找下级的下级id
							pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(pids);
					/*downUserIds = new Long[pids.length];
					downUserIds = pids;*/
						} else {
							break;
						}
					}
				}
				resultItem.put("personCount", personCount);
				resultItem.put("realNameCount", realNameCount);
				resultItem.put("VIPCount", VIPCount);
				resultItem.put("userInfo", u);
				LOG.info("resultItem==================================================" + resultItem);
				result.add(resultItem);
			}
			LOG.info("RESULT:=========================" + result);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, result);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		map.put(CommonConstants.RESULT, result);
		map.put(CommonConstants.RESP_MESSAGE, "下级用户为空");
		return map;
	}

	/**
	 * 作日直推用户列表--------多多生活
	 * @param request
	 * @param token
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/ddsh/fans/yesterday/{token}")
	public @ResponseBody
	Object downloadAppFansUsersysForDDSHByyesterday(HttpServletRequest request,
												@PathVariable("token") String token,
												@RequestParam(value = "page", defaultValue = "0", required = false) int page,
												@RequestParam(value = "size", defaultValue = "20", required = false) int size,
												@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
												@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		LOG.info("直推用户列表===============读取信息=============================");
		LOG.info("请求token值===============" + token);
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
			LOG.info("获取用户ID=========================================" + userId);
		} catch (Exception e) {
			LOG.info("===========token无效==========");
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);
		LOG.info("获取用户信息：====================="+user);
		Long[] userIds = {userId};
		// 获取作日直推的用户id
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime() - 86400000);
		Long[] downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(userIds,date);

		if (downUserIds.length != 0) {
			LOG.info("今日直推用户id========================" + downUserIds);
			PageRequest pageRequest = new PageRequest(page, size, new Sort(direction, sortProperty));
			List<User> infoUsers = userLoginRegisterBusiness.findInfoUsersPageable(downUserIds, pageRequest);
			LOG.info("今日直推用户信息========================" + infoUsers);

			String url = "http://paymentchannel/v1.0/paymentchannel/realname/findby/userids";
			MultiValueMap<String, Object> requestEntity;
			String resultJSON;
			JSONObject jsonObject;

			for (User u : infoUsers) {
				Map<String, Object> resultItem = new HashMap<>();
				Long[] ids = new Long[]{u.getId()};
				LOG.info("ids=========================================" + ids);
				// 获取直推的用户id
				downUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(ids,date);
				LOG.info("直推id===========================" + downUserIds);
				resultItem.put("downUserCount", downUserIds.length);
				long personCount = downUserIds.length; //直推+间推用户总人数
				long realNameCount = 0;//直推+间推 实名统计
				long VIPCount = 0;//VIP人数统计
				requestEntity = new LinkedMultiValueMap<>();
				requestEntity.add("userIds",downUserIds);
				requestEntity.add("date",date);

				LOG.info("downUserIds.length===========================" + downUserIds.length);

				if (downUserIds.length != 0) {

					//获取直推的实名人数
					resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
					jsonObject = JSONObject.fromObject(resultJSON);
					//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
					Integer realNameUserIds = Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
					realNameCount += realNameUserIds;

					//获取直推的VIP人数
					Long[] ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", downUserIds);
					for (Long userid : ordinaryPerson){
						boolean flag = queryVip(userid, date);
						if (flag){
							VIPCount ++ ;
						}
					}

					//获取直推下级用户的id
					Long[] pids = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndCreateTime(downUserIds,date);

					while (true) {
						//判断下级是否为空
						if (pids != null && pids.length > 0 && pids[0] > 0) {
							//加下级的人数
							personCount += pids.length;
							//获得下级实名的人数
							requestEntity = new LinkedMultiValueMap<>();
							requestEntity.add("userIds",pids);
							requestEntity.add("date",date);
							resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
							jsonObject = JSONObject.fromObject(resultJSON);
							//Long[] realNameUserIds = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndrealnameStatus("1", downUserIds);
							realNameUserIds = Integer.valueOf(jsonObject.getInt(CommonConstants.RESULT));
							realNameCount += realNameUserIds;
							//获得下级VIP的人数
							ordinaryPerson = userLoginRegisterBusiness.queryUserIdBypreUserIdsAndGradenew("0", pids);
							for (Long userid : ordinaryPerson){
								boolean b = queryVip(userid, date);
								if (b){
									VIPCount ++ ;
								}
							}
							//寻找下级的下级id
							pids = userLoginRegisterBusiness.queryUserIdBypreUserIds(pids);
					/*downUserIds = new Long[pids.length];
					downUserIds = pids;*/
						} else {
							break;
						}
					}
				}
				resultItem.put("personCount", personCount);
				resultItem.put("realNameCount", realNameCount);
				resultItem.put("VIPCount", VIPCount);
				resultItem.put("userInfo", u);
				LOG.info("resultItem==================================================" + resultItem);
				result.add(resultItem);
			}
			LOG.info("RESULT:=========================" + result);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, result);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		map.put(CommonConstants.RESULT, result);
		map.put(CommonConstants.RESP_MESSAGE, "下级用户为空");
		return map;
	}


    /**
     * 我的社群
     * 所有直推，社群，实名，vip 人数统计
     *
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/myCommunity/{token}")
    public @ResponseBody
    Object queryMyCommunity(HttpServletRequest request,@PathVariable("token") String token) {
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        LOG.info("直推，社群，实名，vip 人数统计===============读取信息=============================");
        LOG.info("请求token值================================="+token);
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
            LOG.info("获取用户ID==============="+userId);
        } catch (Exception e) {
            LOG.info("===========token无效==========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        int allDownUserCount =0;
        int downUserCount =0;
        int vipCount =0;
        try {
            //所有下级用户id
            Long[] allDownUsers = userRelationBusiness.findByPreUserId(userId);
            //直属下级用户id
            Long[] downUserIds = userRelationBusiness.queryFansByPreUserIdAndLevel(userId, 1);
            result.put("realNameCount",0);

            if(null != allDownUsers && allDownUsers.length>0){
                allDownUserCount = allDownUsers.length;
                //查询实名人数 /v1.0/paymentchannel/realname/findRealNameCounts/userids
                String url = "http://paymentchannel/v1.0/paymentchannel/realname/findRealNameCounts/userids";
                MultiValueMap<String, Object> requestEntity;
                requestEntity = new LinkedMultiValueMap<>();
                requestEntity.add("userIds",allDownUsers);

                String resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("所有实名信息：=============="+resultJSON);
                JSONObject object = JSONObject.fromObject(resultJSON);

                if(CommonConstants.SUCCESS.equals(object.getString(CommonConstants.RESP_CODE))){
                    String realNameCount = object.getString(CommonConstants.RESULT);
                    //实名人数
                    result.put("realNameCount",Integer.valueOf(realNameCount));
                }
                //查询vip人数
                vipCount = userLoginRegisterBusiness.findVipCount(allDownUsers);

            }
            if(null != allDownUsers){
                downUserCount = downUserIds.length;
            }
            //所有社群人数
            result.put("allDownUserCount",allDownUserCount);
            //直推人数
            result.put("downUserCount",downUserCount);
            //vip人数
            result.put("vipCount",vipCount);

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "查询成功");
            map.put(CommonConstants.RESULT, result);

        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询失败");
            return map;
        }
        return map;
    }
    /**
     * 我的社群
     * 昨日 直推，间推，实名，vip
     *
     * 昨日收益，今日收益
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/app/usersys/query/myCommunityTodayAndYestoday/{token}")
    public @ResponseBody
    Object myCommunityTodayAndYestoday(HttpServletRequest request,@PathVariable("token") String token) {
        /**
         * 1、获取用户id
         * 2、获取用户信息
         * 3、获取直推用户
         * 4、获取间推用户
         * 5、统计数据
         */
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        LOG.info("今日/昨日详情===============读取信息=============================");
        LOG.info("请求token值================================="+token);
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
            LOG.info("获取用户ID==============="+userId);
        } catch (Exception e) {
            LOG.info("===========token无效==========");
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        String orderType = "1,0,2,3";
        String[] orderTypeA = orderType.split(",");
        //当日收益和昨日收益
        //今日收益
        Date startTimeDate = DateUtil.getDateFromStr(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        Date endTimeDate=null;
        Calendar calendar   =   new   GregorianCalendar();
        calendar.setTime(startTimeDate);
        calendar.add(Calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
        endTimeDate=calendar.getTime();   //这个时间就是日期往后推一天的结果

        BigDecimal sumTodayRebate =userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, startTimeDate, endTimeDate);
        //昨天收益
        Date startTimeYestoday = DateUtil.getDateFromStr(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        Date endTimeDateYestoday =null;
        Calendar   calendarYestoday   =   new   GregorianCalendar();
        calendarYestoday.setTime(startTimeYestoday);
        calendarYestoday.add(Calendar.DATE,-1);//把日期往后增加一天.整数往后推,负数往前移动
        startTimeYestoday=calendarYestoday.getTime();   //这个时间就是日期往后推一天的结果
        calendarYestoday   =   new   GregorianCalendar();
        calendarYestoday.setTime(startTimeYestoday);
        calendarYestoday.add(Calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
        endTimeDateYestoday=calendarYestoday.getTime();   //这个时间就是日期往后推一天的结果
        BigDecimal sumYestodayRebate=userRebateHistoryBusiness.findsumRebateHistoryByUseridAnd(userId, orderTypeA, startTimeYestoday, endTimeDateYestoday);
        if(null==sumTodayRebate){
            sumTodayRebate= new BigDecimal(0);
        }
        if(null==sumYestodayRebate){
            sumYestodayRebate= new BigDecimal(0);
        }
        result.put("todayRebate",sumTodayRebate);
        result.put("yesterdayRebate",sumYestodayRebate);

        try {
            //今天
            String todayTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Long[] downUserIds = userRelationBusiness.queryFansByPreUserIdAndLevelAndCreateTime(userId,1,todayTime);
            LOG.info("今日直推人数：======="+downUserIds.length);
            Long[] personCount = userRelationBusiness.queryAllByPreUserIdAndCreateTime(userId,todayTime);
            LOG.info("今日社群人数：======="+personCount.length);
            result.put("todayDownUserCount",downUserIds.length);
            result.put("todayIntermediateUserCount",personCount.length - downUserIds.length);
            String url = "http://paymentchannel/v1.0/paymentchannel/realname/find/userids/and/createtime/new";
            MultiValueMap<String, Object> requestEntity;
            requestEntity = new LinkedMultiValueMap<>();
            requestEntity.add("userIds",personCount);
            requestEntity.add("date",todayTime);

            String resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("今日实名信息：=============="+resultJSON);
            JSONObject object = JSONObject.fromObject(resultJSON);

            if(CommonConstants.SUCCESS.equals(object.getString(CommonConstants.RESP_CODE))){
                String realNameCount = object.getString(CommonConstants.RESULT);
                result.put("todayRealNameCount",Integer.valueOf(realNameCount));
            }
            Long[] userids = userRelationBusiness.findByPreUserId(userId);
            if (userids.length == 0){
                LOG.info("该用户没有下级====================================");
                //直推人数
                result.put("todayDownUserCount",downUserIds.length);
                //间推人数
                result.put("todayIntermediateUserCount",personCount.length - downUserIds.length);
                result.put("todayRealNameCount",0);
                result.put("todayVIPCount",0);
                result.put("yesterdayDownUserCount", 0);
                result.put("yesterdayIntermediateUserCount", 0);
                result.put("yesterdayRealNameCount", 0);
                result.put("yesterdayVIPCount", 0);
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "查询成功");
                map.put(CommonConstants.RESULT, result);
            }else {
                int VIPCount = upGradeDetailBusiness.queryUpGradeDetailByUseridsAndCreateTime(userids, todayTime);
                LOG.info("今日VIP人数：==============" + VIPCount);
                result.put("todayVIPCount", VIPCount);
                String yestDayTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime() - 86400000);

                //昨天
                downUserIds = userRelationBusiness.queryFansByPreUserIdAndLevelAndCreateTime(userId, 1, yestDayTime);
                LOG.info("昨日直推人数：=======" + downUserIds.length);
                personCount = userRelationBusiness.queryAllByPreUserIdAndCreateTime(userId, yestDayTime);
                LOG.info("昨日社群人数：=======" + personCount.length);
                LOG.info("昨日间推人数：=======" + (personCount.length- downUserIds.length));

                result.put("yesterdayDownUserCount", downUserIds.length);
                result.put("yesterdayIntermediateUserCount", personCount.length-downUserIds.length);
                requestEntity = new LinkedMultiValueMap<>();
                requestEntity.add("userIds", personCount);
                requestEntity.add("date", yestDayTime);

                resultJSON = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("昨日实名信息：==============" + resultJSON);
                object = JSONObject.fromObject(resultJSON);

                if (CommonConstants.SUCCESS.equals(object.getString(CommonConstants.RESP_CODE))) {
                    String realNameCount = object.getString(CommonConstants.RESULT);
                    result.put("yesterdayRealNameCount", Integer.valueOf(realNameCount));
                }
                userids = userRelationBusiness.findByPreUserId(userId);
                VIPCount = upGradeDetailBusiness.queryUpGradeDetailByUseridsAndCreateTime(userids, yestDayTime);
                LOG.info("昨日VIP人数：==============" + VIPCount);
                result.put("yesterdayVIPCount", VIPCount);

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "查询成功");
                map.put(CommonConstants.RESULT, result);
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询失败");
            return map;
        }
        return map;
    }




    public boolean queryVip(Long userId,String date){
		boolean flag = false;
		try {
			List<UpGradeDetail> upGradeDetails = upGradeDetailBusiness.queryUpGradeDetailByUseridAndCreateTime(userId, date);
			if(upGradeDetails.size() != 0 ){
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

}
