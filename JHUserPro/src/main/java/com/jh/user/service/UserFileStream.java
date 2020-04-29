package com.jh.user.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.stream.FileImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserBankInfoBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.InfoUser;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserBankInfo;
import com.jh.user.util.AliOSSUtil;
import com.jh.user.util.Util;

import cn.jh.common.tools.Base64;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.PhotoCompressUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class UserFileStream {
	private static final Logger LOG = LoggerFactory.getLogger(UserFileStream.class);

	@Value("${user.realname.uploadpath}")
	private String realnamePic;

	@Value("${user.realname.downloadpath}")
	private String downloadPath;

	@Autowired
	private AliOSSUtil aliOSSUtil;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	private UserBankInfoBusiness userBankInfoBusiness;

	@Autowired
	private BrandManageBusiness brandManageBusiness;

	@Autowired
	Util util;

	/**
	 * 用户资质获取总入口
	 * 
	 * @author lirui
	 * 
	 * @param brandId
	 * @param phone
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/getPicture")
	public @ResponseBody Object getPicture(@RequestParam(value = "phone") String phone) throws Exception {
		Map<String, Object> maps = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		User user = userLoginRegisterBusiness.queryUserByPhone(phone);
		long sbrandId = user.getBrandId();
		String brandId = String.valueOf(sbrandId);
		maps = (Map<String, Object>) this.downloadUserPic(brandId, phone);
		LOG.info("*******************获取用户图片***********************");
		List<String> nginxList = new ArrayList<>();
		if (null != maps.get("result")) {
			map = (Map<String, Object>) this.getFileStream(brandId, phone);
			if ("999999".equals(map.get("resp_code"))) {
				File file = null;
				JSONArray jsonarry = JSONArray.fromObject(maps.get("result"));
				for (int i = 0; i < jsonarry.size(); i++) {
					String pictureURL = jsonarry.getString(i);
					LOG.info("========下标" + i + "图片地址");
					try {
						String str = "";
						str = str + (char) (Math.random() * 26 + 'A');
						String zipPath = "/" + phone + str + ".jpg";
						file = new File(zipPath);
						if (file.exists()) {
							file.delete();
						}
						PhotoCompressUtil.compressPhoto(pictureURL, zipPath, 0.1f);
						byte[] nginxFile = this.imageTobyte(zipPath, file);
						String buffer = Base64.encode(nginxFile);
						nginxList.add(buffer);
					} catch (Exception e) {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, "用户认证图片误删或路径有误!");
						return maps;
					} finally {
						file.delete();
					}

				}
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "nginx图片查询记录：" + nginxList.size() + "条");
				maps.put(CommonConstants.RESULT, nginxList);
				maps.put("pictureRecord", nginxList.size());// 图片记录
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "oss图片查询记录：" + map.get("pictureRecord") + "条");
				maps.put(CommonConstants.RESULT, map.get("result"));
				maps.put("pictureRecord", map.get("pictureRecord"));// 图片记录
				return maps;
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "用户暂无实名认证图片!");
			return maps;
		}

	}

	/**
	 * 获取oss用户资质
	 * @author lirui
	 * @param brandId
	 * @param phone
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/filestream/get")
	public @ResponseBody Object getFileStream(@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "phone") String phone) throws Exception {

		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + brandId + "-" + phone + "-";
		Map<String, Object> maps = new HashMap<>();
		List<String> fileNames = new ArrayList<>();
		List<String> baseFile = new ArrayList<>();
		byte[] result = null;
		fileNames = aliOSSUtil.listFiles(ossObjectNamePrefix);
		if (null != fileNames && fileNames.size() > 0) {
			for (String objectName : fileNames) {
				result = aliOSSUtil.getFileStream(objectName);
				LOG.info("读取文件流：" + result);
				String buffer = Base64.encode(result);
				baseFile.add(buffer);
			}
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "图片查询记录：" + baseFile.size() + "条");
			maps.put(CommonConstants.RESULT, baseFile);
			maps.put("pictureRecord", baseFile.size());// 图片记录
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "图片未找到");
		}
		return maps;

	}

	/**
	 * 读取用户文件
	 * 
	 * @param request
	 * @param sbrandId
	 * @param phone
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/download/realname2")
	public @ResponseBody Object downloadUserPic(
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
			@RequestParam(value = "phone") String phone) {
		Map<String, Object> map = new HashMap<>();
		long brandId = -1;
		User user;
		Brand brand = null;
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e) {
			brandId = -1;
		}
		if (brandId == -1) {
			user = userLoginRegisterBusiness.queryUserByPhone(phone);
		} else {
			brand = brandManageBusiness.findBrandById(brandId);
			if (brand != null && "6".equals(brand.getBrandType())) {
				user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
			} else {
				user = userLoginRegisterBusiness.queryUserByPhone(phone);
			}
		}

		if (user == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESULT, null);
			map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
			return map;

		}

		brandId = user.getBrandId();

		/** 获取身份证实名信息 */
		URI uri = util.getServiceUrl("paymentchannel", "error url request!");
		String url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
		MultiValueMap<String, Long> requestEntity = new LinkedMultiValueMap<String, Long>();
		requestEntity.add("userid", user.getId());

		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================/v1.0/paymentchannel/realname/userid" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject authObject = jsonObject.getJSONObject("realname");
		InfoUser infouser = new InfoUser();

		// 系统编号
		infouser.setUserid(user.getId());

		infouser.setBrandId(user.getBrandId());

		infouser.setBrandName(user.getBrandname());
		// 用户手机号
		infouser.setPhone(user.getPhone());

		// 真是姓名
		infouser.setRealname(authObject.getString("realname"));
		// 身份证号
		infouser.setIdcard(authObject.getString("idcard"));
		UserBankInfo ubi = new UserBankInfo();
		ubi = userBankInfoBusiness.queryDefUserBankInfoByUserid(user.getId());

		if (ubi != null) {

			// 银行卡名称
			infouser.setBankName(ubi.getBankName());
			// 银行卡号
			infouser.setCardNo(ubi.getCardNo());

		} else {
			// 银行卡名称
			infouser.setBankName(null);
			// 银行卡号
			infouser.setCardNo(null);
		}

		// 用户性别
		infouser.setSex(user.getSex());

		// 实名状态
		infouser.setRealnameStatus(user.getRealnameStatus());
		// 级别
		infouser.setGrade(user.getGrade());
		// 注册时间
		infouser.setCreateTime(user.getCreateTime());
		List<String> filepaths = new ArrayList<String>();
		String src = "/" + user.getBrandId() + "/realname/" + phone;

		String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
		String ossObjectName = "";

		File file = new File(realnamePic + src);
		String[] filelist = file.list();
		if (filelist != null && filelist.length > 0) {
			for (int i = 0; i < filelist.length; i++) {
				filepaths.add(downloadPath + src + "/" + filelist[i]);
			}
		} else {
			List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
			if (listFiles != null && listFiles.size() > 0) {
				for (String fileName : listFiles) {
					String fileUrl = aliOSSUtil.getFileUrl(fileName);
					filepaths.add(fileUrl);
				}
			}
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, filepaths);
		map.put("infouser", infouser);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 根据文件流生成文件
	 * 
	 * @author lirui
	 * 
	 * @param filename
	 * @param data
	 * @throws Exception
	 */
	public void saveFile(String filename, byte[] data) throws Exception {
		if (data != null) {
			String filepath = "D:\\" + filename;
			File file = new File(filepath);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data, 0, data.length);
			fos.flush();
			fos.close();
			System.out.println("生成文件：D:\\" + filename);
		}
	}

	/**
	 * 根据路径读取图片流
	 * 
	 * @param path
	 * @return
	 */
	public static byte[] imageTobyte(String path, File file) {
		byte[] data = null;
		FileImageInputStream input = null;
		try {
			input = new FileImageInputStream(new File(path));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int numBytesRead = 0;
			while ((numBytesRead = input.read(buf)) != -1) {
				output.write(buf, 0, numBytesRead);
			}
			data = output.toByteArray();
			output.close();
			input.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

}