package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.jh.user.business.LoanApplicationBusiness;
import com.jh.user.pojo.LoanApplication;
import com.jh.user.util.IpAddress;
import com.jh.user.util.Util;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
@EnableAutoConfiguration
public class LoanApplicationService {

	private static final Logger LOG = LoggerFactory.getLogger(LoanApplicationService.class);

	@Autowired
	Util util;

	@Autowired
	private LoanApplicationBusiness loanApplicationBusiness;

	//private String IPAddress = "http://106.14.28.146:8888";
	//private String IPAddress = "http://106.15.56.208:8888";
	
	//private String IPAddress = "http://101.132.160.107:8888";
	private String IPAddress = IpAddress.getIpAddress();
	
	//聚金宝后台
	//private String IPAddress = "http://106.15.206.59:8888";

	private String writePath = "/usr/share/nginx/html/loanapplication/brandid";

	private String readPath = IPAddress + "/loanapplication/brandid";
	
	/*private String writePath = "I:\\brandId";

	private String readPath = "I:\\brandId";*/

	// 添加贷款申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/loanapplication/addloanapplication")
	public @ResponseBody Object addLoanapplication(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "classifiCation", required = false) String classifiCation,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "remark", required = false) String remark,
			@RequestParam(value = "previewNumber", required = false, defaultValue = "0") String previewNumber,
			@RequestParam(value = "publisher", required = false) String publisher,
			@RequestParam(value = "onOrOff", required = false, defaultValue = "1") int onOrOff,
			@RequestParam(value = "loanLimit") String loanLimit,
			@RequestParam(value = "dailyInterest") String dailyInterest,
			@RequestParam(value = "label1", required = false, defaultValue = "") String label1,
			@RequestParam(value = "label2", required = false, defaultValue = "") String label2,
			@RequestParam(value = "label3", required = false, defaultValue = "") String label3,
			@RequestParam(value = "accountArrTimeDesc") String accountArrTimeDesc,
			@RequestParam(value = "rewardAmount") String rewardAmount,
			@RequestParam(value = "settlementCycle") String settlementCycle,
			@RequestParam(value = "settlementRules") String settlementRules,
			@RequestParam(value = "spare1", required = false, defaultValue = "") String spare1,
			@RequestParam(value = "spare2", required = false, defaultValue = "") String spare2,
			@RequestParam(value = "spare3", required = false, defaultValue = "") String spare3) throws Exception {

		LoanApplication la = new LoanApplication();
		
		la.setBrandId(brandId);
		la.setClassifiCation(URLDecoder.decode(classifiCation, "UTF-8"));
		la.setTitle(URLDecoder.decode(title, "UTF-8"));
		la.setContent(URLDecoder.decode(content, "UTF-8"));

		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		StringBuffer newsb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			sb.append(random.nextInt(10));
			newsb.append(random.nextInt(10));
		}
		
		la.setLowSourceId(sb.toString());
		la.setDescPictureId(newsb.toString());
		la.setOnOff(onOrOff);
		la.setRemark(URLDecoder.decode(remark, "UTF-8"));

		if ("0".equals(previewNumber)) {

			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < 4; i++) {
				int nextInt = random.nextInt(10);
				if (i == 0 && nextInt == 0) {
					continue;
				}
				sb1.append(nextInt);
			}

			la.setPreviewNumber(sb1.toString());
		} else {

			la.setPreviewNumber(previewNumber);
		}

		la.setPublisher(URLDecoder.decode(publisher, "UTF-8"));
		la.setLoanLimit(new BigDecimal(loanLimit));
		la.setDailyInterest(URLDecoder.decode(dailyInterest, "UTF-8"));
		la.setLabel1("".equals(label1) ? label1 : URLDecoder.decode(label1, "UTF-8"));
		la.setLabel2("".equals(label2) ? label2 : URLDecoder.decode(label2, "UTF-8"));
		la.setLabel3("".equals(label3) ? label3 : URLDecoder.decode(label3, "UTF-8"));
		la.setAccountArrTimeDesc(URLDecoder.decode(accountArrTimeDesc, "UTF-8"));
		la.setRewardAmount(new BigDecimal(rewardAmount));
		la.setSettlementCycle(URLDecoder.decode(settlementCycle, "UTF-8"));
		la.setSettlementRules(URLDecoder.decode(settlementRules, "UTF-8"));
		la.setSpare1("".equals(spare1) ? spare1 : URLDecoder.decode(spare1, "UTF-8"));
		la.setSpare2("".equals(spare2) ? spare2 : URLDecoder.decode(spare2, "UTF-8"));
		la.setSpare3("".equals(spare3) ? spare3 : URLDecoder.decode(spare3, "UTF-8"));
		la.setStatus(1);

		try {
			MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> files = multiPartRequest.getFiles("image");
			File file = new File(writePath + "/" + brandId + "/" + sb.toString() + "/lowsource/");
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
					File dest = new File(writePath + "/" + brandId + "/" + sb.toString() + "/lowsource/"
							+ System.currentTimeMillis() + i + substring);

					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (Exception e) {
						LOG.error("保存预览图出错啦======");
						e.printStackTrace();

						return ResultWrap.init(CommonConstants.FALIED, "保存预览图失败!");
					}
				}
			}
			String path = null;
			if (file != null) {
				String[] list = file.list();
				if (list != null) {
					for (int j = 0; j < list.length; j++) {
						String string = list[0];
						path = readPath + "/" + brandId + "/" + sb.toString() + "/lowsource/" + string;
					}
				} else {
					path = "";
				}
				la.setLowSource(path);
			}
		} catch (Exception e1) {
			LOG.info("没有上传预览图======");
			la.setLowSource("");
		}

		try {
			MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> files1 = multiPartRequest.getFiles("picture");
			File file1 = new File(writePath + "/" + brandId + "/" + newsb.toString() + "/descpicture/");
			if (file1.mkdirs()) {
				
			} else {
				File[] listFiles = file1.listFiles();
				for (File f : listFiles) {
					f.delete();
				}
			}
			int k = 1;
			if (files1 != null && files1.size() > 0) {
				for (MultipartFile mf : files1) {
					String filename = mf.getOriginalFilename();
					String substring = filename.substring(filename.lastIndexOf("."));
					k++;
					File dest = new File(writePath + "/" + brandId + "/" + newsb.toString() + "/descpicture/"
							+ System.currentTimeMillis() + k + substring);

					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (Exception e) {
						LOG.error("保存推广返佣说明图片出错啦======");
						e.printStackTrace();

						return ResultWrap.init(CommonConstants.FALIED, "保存推广返佣说明图片失败!");
					}

				}
			}
			String path = null;
			if (file1 != null) {
				String[] list = file1.list();
				if (list != null) {
					for (int j = 0; j < list.length; j++) {
						String string = list[0];
						path = readPath + "/" + brandId + "/" + newsb.toString() + "/descpicture/" + string;
					}
				} else {
					path = "";
				}

				la.setDescPicture(path);
			}
		} catch (Exception e1) {
			LOG.info("没有上传推广返佣说明图片======");
			la.setDescPicture("");
		}
		try {
			loanApplicationBusiness.createLoanApplication(la);
		} catch (Exception e) {
			LOG.error("保存数据出错啦======");
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "保存贷款申请信息出错,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "成功");
	}

	// 分页查看整个贴牌所有贷款申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/loanapplication/getloanapplicationby/brandidandpage")
	public @ResponseBody Object getLoanApplicationByBrandIdAndPage(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "title", required = false, defaultValue = "-1") String title,
			@RequestParam(value = "type", defaultValue = "0", required = false) String type,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "500", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

		if(!"-1".equals(title)) {
			Page<LoanApplication> loanApplicationByBrandIdAndTitleAndPage = null;
			try {
				loanApplicationByBrandIdAndTitleAndPage = loanApplicationBusiness.getLoanApplicationByBrandIdAndStatusAndTitleAndPage(brandId, 1, title, pageable);
			} catch (Exception e) {
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
			}

			if (loanApplicationByBrandIdAndTitleAndPage != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", loanApplicationByBrandIdAndTitleAndPage);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", loanApplicationByBrandIdAndTitleAndPage);
			}
		} else {
			Page<LoanApplication> loanApplicationByBrandIdAndPage = null;
			try {
				loanApplicationByBrandIdAndPage = loanApplicationBusiness.getLoanApplicationByBrandIdAndStatusAndPage(brandId, 1, pageable);
			} catch (Exception e) {
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
			}

			if (loanApplicationByBrandIdAndPage != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", loanApplicationByBrandIdAndPage);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无数据", "");
			}
		}
		
	}


	// 查看单个贷款申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/loanapplication/getloanapplicationby/brandidandid")
	public @ResponseBody Object getLoanApplicationByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "id") long id) {

		LoanApplication loanApplication = null;
		try {
			loanApplication = loanApplicationBusiness.getLoanApplicationByBrandIdAndIdAndStatus(brandId, id, 1);

		} catch (Exception e) {
			LOG.error("查看单个贷款申请信息的接口有误======",e);
			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", loanApplication);
	}

	// 更改贷款申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/loanapplication/updateloanapplicationby/brandidandid")
	public @ResponseBody Object updateLoanApplicationByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "id") long id,
			@RequestParam(value = "classifiCation", required = false, defaultValue = "-1") String classifiCation,
			@RequestParam(value = "title", required = false, defaultValue = "-1") String title,
			@RequestParam(value = "content", required = false, defaultValue = "-1") String content,
			@RequestParam(value = "remark", required = false, defaultValue = "-1") String remark,
			@RequestParam(value = "previewNumber", required = false, defaultValue = "0") String previewNumber,
			@RequestParam(value = "publisher", required = false, defaultValue = "-1") String publisher,
			@RequestParam(value = "onOrOff", required = false, defaultValue = "-1") int onOrOff,
			@RequestParam(value = "loanLimit", required = false, defaultValue = "-1") String loanLimit,
			@RequestParam(value = "dailyInterest", required = false, defaultValue = "-1") String dailyInterest,
			@RequestParam(value = "label1", required = false, defaultValue = "-1") String label1,
			@RequestParam(value = "label2", required = false, defaultValue = "-1") String label2,
			@RequestParam(value = "label3", required = false, defaultValue = "-1") String label3,
			@RequestParam(value = "accountArrTimeDesc", required = false, defaultValue = "-1") String accountArrTimeDesc,
			@RequestParam(value = "rewardAmount", required = false, defaultValue = "-1") String rewardAmount,
			@RequestParam(value = "settlementCycle", required = false, defaultValue = "-1") String settlementCycle,
			@RequestParam(value = "settlementRules", required = false, defaultValue = "-1") String settlementRules,
			@RequestParam(value = "spare1", required = false, defaultValue = "-1") String spare1,
			@RequestParam(value = "spare2", required = false, defaultValue = "-1") String spare2,
			@RequestParam(value = "spare3", required = false, defaultValue = "-1") String spare3) throws Exception {

		LoanApplication la = loanApplicationBusiness.getLoanApplicationByBrandIdAndIdAndStatus(brandId, id, 1);

		if(la == null) {
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据,无法修改!");
		}
		
		la.setClassifiCation("-1".equals(classifiCation) ? la.getClassifiCation() : URLDecoder.decode(classifiCation, "UTF-8"));
		
		la.setTitle("-1".equals(title) ? la.getTitle() : URLDecoder.decode(title, "UTF-8"));
		
		la.setContent("-1".equals(content) ? la.getContent() : URLDecoder.decode(content, "UTF-8"));
		
		la.setRemark("-1".equals(remark) ? la.getRemark() : URLDecoder.decode(remark, "UTF-8"));
		
		la.setPreviewNumber("0".equals(previewNumber) ? la.getPreviewNumber() : URLDecoder.decode(previewNumber, "UTF-8"));
		
		la.setPublisher("-1".equals(publisher) ? la.getPublisher() : URLDecoder.decode(publisher, "UTF-8"));
		
		la.setOnOff("-1".equals(onOrOff) ? la.getOnOff() : onOrOff);
		
		la.setLoanLimit("-1".equals(loanLimit) ? la.getLoanLimit() : new BigDecimal(loanLimit));
		
		la.setDailyInterest("-1".equals(dailyInterest) ? la.getDailyInterest() : dailyInterest);
		
		la.setLabel1("-1".equals(label1) ? la.getLabel1() : URLDecoder.decode(label1, "UTF-8"));
		
		la.setLabel2("-1".equals(label2) ? la.getLabel2() : URLDecoder.decode(label2, "UTF-8"));
		
		la.setLabel3("-1".equals(label3) ? la.getLabel3() : URLDecoder.decode(label3, "UTF-8"));
		
		la.setAccountArrTimeDesc("-1".equals(accountArrTimeDesc) ? la.getAccountArrTimeDesc() : URLDecoder.decode(accountArrTimeDesc, "UTF-8"));
		
		la.setRewardAmount("-1".equals(rewardAmount) ? la.getRewardAmount() : new BigDecimal(rewardAmount));
		
		la.setSettlementCycle("-1".equals(settlementCycle) ? la.getSettlementCycle() : URLDecoder.decode(settlementCycle, "UTF-8"));
		
		la.setSettlementRules("-1".equals(settlementRules) ? la.getSettlementRules() : URLDecoder.decode(settlementRules, "UTF-8"));
		
		la.setSpare1("-1".equals(spare1) ? la.getSpare1() : URLDecoder.decode(spare1, "UTF-8"));
		
		la.setSpare2("-1".equals(spare2) ? la.getSpare2() : URLDecoder.decode(spare2, "UTF-8"));
		
		la.setSpare3("-1".equals(spare3) ? la.getSpare3() : URLDecoder.decode(spare3, "UTF-8"));

		try {
			List<MultipartFile> files = null;
			try {
				MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
				files = multiPartRequest.getFiles("image");
			} catch (Exception e1) {
				la.setLowSource(la.getLowSource());
			}
			File file = new File(writePath + "/" + brandId + "/" + la.getLowSourceId() + "/lowsource/");
			if (file.mkdirs()) {
			} else if(files != null && files.get(0).getSize()!=0) {
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
					File dest = new File(writePath + "/" + brandId + "/" + la.getLowSourceId() + "/lowsource/"
							+ System.currentTimeMillis() + i + substring);
					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (Exception e) {
						LOG.error("保存预览图出错啦======");
						e.printStackTrace();
						return ResultWrap.init(CommonConstants.FALIED, "保存预览图失败!");
					}
				}
				
				String path = null;
				if (file != null) {
					String[] list = file.list();
					if (list != null) {
						for (int j = 0; j < list.length; j++) {
							String string = list[0];
							path = readPath + "/" + brandId + "/" + la.getLowSourceId() + "/lowsource/" + string;
						}
					} else {
						path = "";
					}
					la.setLowSource(path);
				}
			}
		} catch (Exception e1) {
			LOG.info("没有上传预览图======");
		}

		try {
			List<MultipartFile> files1 = null;
			try {
				MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
				files1 = multiPartRequest.getFiles("picture");
			} catch (Exception e1) {
				la.setLowSource(la.getLowSource());
				la.setDescPicture(la.getDescPicture());
			}
			File file1 = new File(writePath + "/" + brandId + "/" + la.getDescPictureId() + "/descpicture/");
			if (file1.mkdirs()) {

			} else if(files1 != null && files1.get(0).getSize()!=0) {
				File[] listFiles = file1.listFiles();
				for (File f : listFiles) {
					f.delete();
				}
			}
			int k = 1;

			if (files1 != null && files1.size() > 0) {
				for (MultipartFile mf : files1) {
					String filename = mf.getOriginalFilename();
					String substring = filename.substring(filename.lastIndexOf("."));
					k++;
					File dest = new File(writePath + "/" + brandId + "/" + la.getDescPictureId() + "/descpicture/"
							+ System.currentTimeMillis() + k + substring);

					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
					} catch (Exception e) {
						LOG.error("保存推广返佣说明图片出错啦======", e);
						return ResultWrap.init(CommonConstants.FALIED, "保存推广返佣说明图片失败!");
					}
				}
				
				String path = null;
				if (file1 != null) {
					String[] list = file1.list();
					if (list != null) {
						for (int j = 0; j < list.length; j++) {
							String string = list[0];
							path = readPath + "/" + brandId + "/" + la.getDescPictureId() + "/descpicture/" + string;
						}
					} else {
						path = "";
					}

					la.setDescPicture(path);
				}
			}
		} catch (Exception e1) {
			LOG.info("没有上传推广返佣说明图片======");
		}
		
		try {
			la.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
			loanApplicationBusiness.createLoanApplication(la);
		} catch (Exception e) {
			LOG.info("修改贷款申请信息失败======", e);

			return ResultWrap.init(CommonConstants.FALIED, "修改贷款申请信息失败,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
	}

	// 删除贷款申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/loanapplication/deleteloanapplicationby/brandidandid")
	public @ResponseBody Object deleteLoanApplicationByBrandIdAndId(HttpServletRequest request,
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

		List<LoanApplication> la = loanApplicationBusiness.getLoanApplicationByBrandIdAndIdsAndStatus(brandId, l, 1);
		
		if (la != null && la.size() > 0) {
			for (LoanApplication n : la) {
				n.setStatus(0);
				n.setDeleteTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
				
				loanApplicationBusiness.createLoanApplication(n);
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}

	}

	// 控制开关的接口
	/*@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/updatenews/onoroffby/brandidandid")
	public @ResponseBody Object updateNewsOnOrOffByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "id") long Id, @RequestParam(value = "onOrOff") String onOrOff) {

		try {
			News newsByBrandIdAndId = newsBusiness.getNewsByBrandIdAndId(brandId, Id);

			newsByBrandIdAndId.setOnOff(onOrOff);

			newsBusiness.createNews(newsByBrandIdAndId);
		} catch (Exception e) {
			LOG.error("", e.getMessage());

			return ResultWrap.init(CommonConstants.FALIED, "修改失败");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "修改开关成功");
	}*/

}
