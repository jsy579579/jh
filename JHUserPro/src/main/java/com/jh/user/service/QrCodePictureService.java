package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.jh.user.business.QrCodePictureBusiness;
import com.jh.user.pojo.QrCodePicture;
import com.jh.user.util.IpAddress;
import com.jh.user.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
@EnableAutoConfiguration
public class QrCodePictureService {

	private static final Logger LOG = LoggerFactory.getLogger(QrCodePictureService.class);

	@Autowired
	Util util;

	@Autowired
	private QrCodePictureBusiness qrCodePictureBusiness;

	//private String IPAddress = "http://106.14.28.146:8888";
	//private String IPAddress = "http://101.132.160.107:8888"; //老系统后台
	private String IPAddress = IpAddress.getIpAddress();
	//聚金宝ip
	//private String IPAddress = "http://106.15.206.59:8888";
	private String writePath = "/usr/share/nginx/html/qrcode/brandid";


	private String readPath = IPAddress + "/qrcode/brandid";
	
	/*private String writePath = "I:\\brandId";

	private String readPath = "I:\\brandId";*/

	// 添加二维码图片的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/qrcodepicture/addqrcodepicture")
	public @ResponseBody Object addQrCodePicture(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId
			) throws Exception {

		List<QrCodePicture> qrCodePictureByBrandId = qrCodePictureBusiness.getQrCodePictureByBrandIdAndStatus(brandId, 1);
		
		if(qrCodePictureByBrandId.size() >= 15) {
			
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,最多只能上传15张图片,如有需要可以删除之前上传的图片!");
		}
		
		QrCodePicture qrCodePicture = new QrCodePicture();
		
		qrCodePicture.setBrandId(brandId);
		
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int nextInt = random.nextInt(10);
			sb.append(nextInt);
		}
		
		qrCodePicture.setQrcodeId(sb.toString());

		//List<String> filepaths = null;
		try {
			MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;

			List<MultipartFile> files = multiPartRequest.getFiles("image");

			File file = new File(writePath + "/" + brandId + "/" + sb.toString());

			if (file.mkdirs()) {

			} else {
				File[] listFiles = file.listFiles();
				for (File f : listFiles) {
					f.delete();
				}
			}
			int i = 1;

			if (files != null && files.size() > 0) {
				for (MultipartFile mf : files) {
					String filename = mf.getOriginalFilename();
					String substring = filename.substring(filename.lastIndexOf("."));
					i++;
					File dest = new File(writePath + "/" + brandId + "/" + sb.toString() + "/"
							+ System.currentTimeMillis() + i + substring);

					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (Exception e) {
						LOG.error("保存二维码图片出错啦======");
						e.printStackTrace();

						return ResultWrap.init(CommonConstants.FALIED, "保存二维码图片失败!");
					}

				}
			}

			//filepaths = new ArrayList<String>();
			
			String path = null;
			if (file != null) {
				String[] list = file.list();
				if (list != null) {
					for (int j = 0; j < list.length; j++) {
						/*String string = list[j];
						filepaths.add(readPath + "/" + brandId + "/" + sb.toString() + "/" + string);*/
						String string = list[0];
						path = readPath + "/" + brandId + "/" + sb.toString() + "/" + string;
					}
				} else {
					path = "";
				}

				qrCodePicture.setQrcodeUrl(path);
			}
		} catch (Exception e1) {
			LOG.info("没有上传二维码图片======");
			qrCodePicture.setQrcodeUrl("");
		}

		try {
			qrCodePictureBusiness.createQrCodePicture(qrCodePicture);
		} catch (Exception e) {
			LOG.error("保存数据出错啦======");
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "保存二维码图片出错,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "成功");
	}

	
	// 查看所有上传二维码信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/qrcodepicture/getqrcodepictureby/brandid")
	public @ResponseBody Object getQrCodePictureByBrandId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "status", required = false, defaultValue = "1") int status) {

		List<QrCodePicture> qrCodePictureByBrandIdAndStatus = qrCodePictureBusiness.getQrCodePictureByBrandIdAndStatus(brandId, status);

		if (qrCodePictureByBrandIdAndStatus != null) {

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", qrCodePictureByBrandIdAndStatus);
		} else {

			return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", qrCodePictureByBrandIdAndStatus);
		}

	}	
	
	// 查看所有二维码图片的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/qrcodepicture/getqrcodeurlby/brandid")
	public @ResponseBody Object getQrCodeUrlByBrandId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "status", required = false, defaultValue = "1") int status
			) {

		List<String> qrCodePictureUrlByBrandId = qrCodePictureBusiness.getQrCodeUrlByBrandIdAndStatus(brandId, status);

		if (qrCodePictureUrlByBrandId != null) {

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", qrCodePictureUrlByBrandId);
		} else {

			return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", qrCodePictureUrlByBrandId);
		}

	}


	// 删除二维码图片的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/qrcodepicture/deleteqrcodepictureby/brandidandid")
	public @ResponseBody Object deleteQrCodePictureByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "id") String Id) {

		long[] l = null;
		if (Id.contains(",")) {

			String[] split = Id.split(",");
			l = new long[split.length];

			for (int i = 0; i < split.length; i++) {
				l[i] = Long.parseLong(split[i]);
			}

		} else {

			String[] split = { Id };
			l = new long[split.length];

			for (int i = 0; i < split.length; i++) {
				l[i] = Long.parseLong(split[i]);
			}

		}

		List<QrCodePicture> qrCodePictureByBrandIdAndId = qrCodePictureBusiness.getQrCodePictureByBrandIdAndId(brandId, l);

		if (qrCodePictureByBrandIdAndId != null && qrCodePictureByBrandIdAndId.size() > 0) {
			for (QrCodePicture qc : qrCodePictureByBrandIdAndId) {
				qc.setStatus(0);
				qc.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), ""));
				
				qrCodePictureBusiness.createQrCodePicture(qc);
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}

	}
}
