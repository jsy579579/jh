package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import com.jh.user.business.CreditCardApplicationBusiness;
import com.jh.user.business.CreditCardRatioBusiness;
import com.jh.user.pojo.CreditCardApplication;
import com.jh.user.pojo.CreditCardRatio;
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
import java.util.*;

@Controller
@EnableAutoConfiguration
public class CreditCardApplicationService {

	private static final Logger LOG = LoggerFactory.getLogger(CreditCardApplicationService.class);

	@Autowired
	Util util;

	@Autowired
	private CreditCardApplicationBusiness creditCardApplicationBusiness;

	@Autowired
	private CreditCardRatioBusiness creditCardRatioBusiness;

	//private String IPAddress = "http://106.14.28.146:8888";
	//private String IPAddress = "http://106.15.56.208:8888";

	//private String IPAddress = "http://101.132.160.107:8888";
	private String IPAddress = IpAddress.getIpAddress();
	
	//聚金宝后台
	//private String IPAddress = "http://106.15.206.59:8888";
	
	private String writePath = "/usr/share/nginx/html/creditcardapplication/brandid";

	private String readPath = IPAddress + "/creditcardapplication/brandid";
	
	/*private String writePath = "I:\\brandId";

	private String readPath = "I:\\brandId";*/

	// 添加信用卡申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/creditcardapplication/addcreditcardapplication")
	public @ResponseBody Object addCreditCardApplication(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "classifiCation", required = false) String classifiCation,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "remark", required = false) String remark,
			@RequestParam(value = "previewNumber", required = false, defaultValue = "0") String previewNumber,
			@RequestParam(value = "publisher", required = false) String publisher,
			@RequestParam(value = "onOrOff", required = false, defaultValue = "1") int onOrOff,
			@RequestParam(value = "label1", required = false, defaultValue = "") String label1,
			@RequestParam(value = "label2", required = false, defaultValue = "") String label2,
			@RequestParam(value = "label3", required = false, defaultValue = "") String label3,
			@RequestParam(value = "cardTypeDesc1", required = false, defaultValue = "") String cardTypeDesc1,
			@RequestParam(value = "cardTypeDesc2", required = false, defaultValue = "") String cardTypeDesc2,
			@RequestParam(value = "rewardAmount") String rewardAmount,
			@RequestParam(value = "settlementCycle") String settlementCycle,
			@RequestParam(value = "settlementRules") String settlementRules,
			@RequestParam(value = "spare1", required = false, defaultValue = "") String spare1,
			@RequestParam(value = "spare2", required = false, defaultValue = "") String spare2,
			@RequestParam(value = "spare3", required = false, defaultValue = "") String spare3) throws Exception {

		CreditCardApplication cc = new CreditCardApplication();
		
		cc.setBrandId(brandId);
		cc.setClassifiCation(URLDecoder.decode(classifiCation, "UTF-8"));
		cc.setTitle(URLDecoder.decode(title, "UTF-8"));
		cc.setContent(URLDecoder.decode(content, "UTF-8"));

		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			sb.append(random.nextInt(10));
		}
		
		cc.setLowSourceId(sb.toString());
		cc.setOnOff(onOrOff);
		cc.setRemark(URLDecoder.decode(remark, "UTF-8"));

		if ("0".equals(previewNumber)) {

			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < 4; i++) {
				int nextInt = random.nextInt(10);
				if (i == 0 && nextInt == 0) {
					continue;
				}
				sb1.append(nextInt);
			}

			cc.setPreviewNumber(sb1.toString());
		} else {

			cc.setPreviewNumber(previewNumber);
		}

		cc.setPublisher(URLDecoder.decode(publisher, "UTF-8"));
		cc.setLabel1("".equals(label1) ? label1 : URLDecoder.decode(label1, "UTF-8"));
		cc.setLabel2("".equals(label2) ? label2 : URLDecoder.decode(label2, "UTF-8"));
		cc.setLabel3("".equals(label3) ? label3 : URLDecoder.decode(label3, "UTF-8"));
		cc.setCardTypeDesc1(URLDecoder.decode(cardTypeDesc1, "UTF-8"));
		cc.setCardTypeDesc2(URLDecoder.decode(cardTypeDesc2, "UTF-8"));
		cc.setRewardAmount(new BigDecimal(rewardAmount));
		cc.setSettlementCycle("".equals(settlementCycle) ? settlementCycle : URLDecoder.decode(settlementCycle, "UTF-8"));
		cc.setSettlementRules("".equals(settlementRules) ? settlementRules : URLDecoder.decode(settlementRules, "UTF-8"));
		cc.setSpare1("".equals(spare1) ? spare1 : URLDecoder.decode(spare1, "UTF-8"));
		cc.setSpare2("".equals(spare2) ? spare2 : URLDecoder.decode(spare2, "UTF-8"));
		cc.setSpare3("".equals(spare3) ? spare3 : URLDecoder.decode(spare3, "UTF-8"));
		cc.setStatus(1);

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

				cc.setLowSource(path);
			}
			
		} catch (Exception e1) {
			LOG.info("没有上传预览图======");
			cc.setLowSource("");
		}

		try {
			creditCardApplicationBusiness.createCreditCardApplication(cc);
		} catch (Exception e) {
			LOG.error("保存数据出错啦======");
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "保存信用卡申请信息出错,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "成功");
	}

	// 分页查看整个贴牌所有信用卡申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/creditcardapplication/getcreditcardapplicationby/brandidandpage")
	public @ResponseBody Object getCreditCardApplicationByBrandIdAndPage(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "type", defaultValue = "0", required = false) String type,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "500", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

		Page<CreditCardApplication> creditCardApplicationByBrandIdAndPage = null;
		try {
			creditCardApplicationByBrandIdAndPage = creditCardApplicationBusiness.getCreditCardApplicationByBrandIdAndStatusAndPage(brandId, 1, pageable);
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
		}

		if (creditCardApplicationByBrandIdAndPage != null) {

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", creditCardApplicationByBrandIdAndPage);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据", "");
		}

	}


	// 查看单个信用卡申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/creditcardapplication/getcreditcardapplicationby/brandidandid")
	public @ResponseBody Object getCreditCardApplicationByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "id") long id) {

		CreditCardApplication creditCardApplication = null;
		try {
			creditCardApplication = creditCardApplicationBusiness.getCreditCardApplicationByBrandIdAndIdAndStatus(brandId, id, 1);

		} catch (Exception e) {
			LOG.error("查看单个贷款申请信息的接口有误======",e);
			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", creditCardApplication);
	}

	// 更改信用卡申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/creditcardapplication/updatecreditcardapplicationby/brandidandid")
	public @ResponseBody Object updateCreditCardApplicationByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") int brandId,
			@RequestParam(value = "id") long id,
			@RequestParam(value = "classifiCation", required = false, defaultValue = "-1") String classifiCation,
			@RequestParam(value = "title", required = false, defaultValue = "-1") String title,
			@RequestParam(value = "content", required = false, defaultValue = "-1") String content,
			@RequestParam(value = "remark", required = false, defaultValue = "-1") String remark,
			@RequestParam(value = "previewNumber", required = false, defaultValue = "0") String previewNumber,
			@RequestParam(value = "publisher", required = false, defaultValue = "-1") String publisher,
			@RequestParam(value = "onOrOff", required = false, defaultValue = "-1") int onOrOff,
			@RequestParam(value = "label1", required = false, defaultValue = "-1") String label1,
			@RequestParam(value = "label2", required = false, defaultValue = "-1") String label2,
			@RequestParam(value = "label3", required = false, defaultValue = "-1") String label3,
			@RequestParam(value = "cardTypeDesc1", required = false, defaultValue = "-1") String cardTypeDesc1,
			@RequestParam(value = "cardTypeDesc2", required = false, defaultValue = "-1") String cardTypeDesc2,
			@RequestParam(value = "rewardAmount", required = false, defaultValue = "-1") String rewardAmount,
			@RequestParam(value = "settlementCycle", required = false, defaultValue = "-1") String settlementCycle,
			@RequestParam(value = "settlementRules", required = false, defaultValue = "-1") String settlementRules,
			@RequestParam(value = "spare1", required = false, defaultValue = "-1") String spare1,
			@RequestParam(value = "spare2", required = false, defaultValue = "-1") String spare2,
			@RequestParam(value = "spare3", required = false, defaultValue = "-1") String spare3) throws Exception {

		CreditCardApplication cc = creditCardApplicationBusiness.getCreditCardApplicationByBrandIdAndIdAndStatus(brandId, id, 1);

		if(cc == null) {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据,无法修改!");
		}
		
		cc.setClassifiCation("-1".equals(classifiCation) ? cc.getClassifiCation() : URLDecoder.decode(classifiCation, "UTF-8"));
		
		cc.setTitle("-1".equals(title) ? cc.getTitle() : URLDecoder.decode(title, "UTF-8"));
		
		cc.setContent("-1".equals(content) ? cc.getContent() : URLDecoder.decode(content, "UTF-8"));
		
		cc.setRemark("-1".equals(remark) ? cc.getRemark() : URLDecoder.decode(remark, "UTF-8"));
		
		cc.setPreviewNumber("0".equals(previewNumber) ? cc.getPreviewNumber() : URLDecoder.decode(previewNumber, "UTF-8"));
		
		cc.setPublisher("-1".equals(publisher) ? cc.getPublisher() : URLDecoder.decode(publisher, "UTF-8"));
		
		cc.setOnOff("-1".equals(onOrOff) ? cc.getOnOff() : onOrOff);
		
		cc.setLabel1("-1".equals(label1) ? cc.getLabel1() : URLDecoder.decode(label1, "UTF-8"));
		
		cc.setLabel2("-1".equals(label2) ? cc.getLabel2() : URLDecoder.decode(label2, "UTF-8"));
		
		cc.setLabel3("-1".equals(label3) ? cc.getLabel3() : URLDecoder.decode(label3, "UTF-8"));
		
		cc.setCardTypeDesc1("-1".equals(cardTypeDesc1) ? cc.getCardTypeDesc1() : URLDecoder.decode(cardTypeDesc1, "UTF-8"));
		
		cc.setCardTypeDesc2("-1".equals(cardTypeDesc2) ? cc.getCardTypeDesc2() : URLDecoder.decode(cardTypeDesc2, "UTF-8"));
		
		cc.setRewardAmount("-1".equals(rewardAmount) ? cc.getRewardAmount() : new BigDecimal(rewardAmount));
		
		cc.setSettlementCycle("-1".equals(settlementCycle) ? cc.getSettlementCycle() : URLDecoder.decode(settlementCycle, "UTF-8"));
		
		cc.setSettlementRules("-1".equals(settlementRules) ? cc.getSettlementRules() : URLDecoder.decode(settlementRules, "UTF-8"));
		
		cc.setSpare1("-1".equals(spare1) ? cc.getSpare1() : URLDecoder.decode(spare1, "UTF-8"));
		
		cc.setSpare2("-1".equals(spare2) ? cc.getSpare2() : URLDecoder.decode(spare2, "UTF-8"));
		
		cc.setSpare3("-1".equals(spare3) ? cc.getSpare3() : URLDecoder.decode(spare3, "UTF-8"));

		try {
			List<MultipartFile> files = null;
			try {
				MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
				
				files = multiPartRequest.getFiles("image");
			} catch (Exception e1) {
				cc.setLowSource(cc.getLowSource());
			}
			File file = new File(writePath + "/" + brandId + "/" + cc.getLowSourceId() + "/lowsource/");
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
					File dest = new File(writePath + "/" + brandId + "/" + cc.getLowSourceId() + "/lowsource/"
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
							path = readPath + "/" + brandId + "/" + cc.getLowSourceId() + "/lowsource/" + string;
						}
					} else {
						path = "";
					}

					cc.setLowSource(path);
				}
				
			}

		} catch (Exception e1) {
			LOG.info("没有上传预览图======");
			
		}

		try {
			cc.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
			creditCardApplicationBusiness.createCreditCardApplication(cc);
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "修改信用卡申请信息失败,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
	}

	// 删除信用卡申请信息的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/creditcardapplication/deletecreditcardapplicationby/brandidandid")
	public @ResponseBody Object deleteCreditCardApplicationByBrandIdAndId(HttpServletRequest request,
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

		List<CreditCardApplication> cc = creditCardApplicationBusiness.getCreditCardApplicationByBrandIdAndIdsAndStatus(brandId, l, 1);
		
		if (cc != null && cc.size() > 0) {
			for (CreditCardApplication n : cc) {
				n.setStatus(0);
				n.setDeleteTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
				
				creditCardApplicationBusiness.createCreditCardApplication(n);
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

    /**
     * 根据贴牌号查询信用卡返佣比率
     * @param brandId
     * @return
     */
	@RequestMapping(method = RequestMethod.POST,value="/v1.0/user/creditcard/ratio/query")
    @ResponseBody
    public Object queryCreditCardRatio(
            @RequestParam(value="brand_id") String brandId
    ){
        Map map=new HashMap<>();
        List<CreditCardRatio> creditCardRatio=creditCardRatioBusiness.queryCreditCardRatioByBrandId(brandId);
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"查询成功");
        map.put(CommonConstants.RESULT,creditCardRatio);
        return map;
    }
}
