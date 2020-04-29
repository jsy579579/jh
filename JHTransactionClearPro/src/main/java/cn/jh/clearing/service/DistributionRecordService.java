package cn.jh.clearing.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import cn.jh.clearing.business.DistributionBusiness;
import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.business.ProfitRecordBusiness;
import cn.jh.clearing.pojo.DistributionRecord;
import cn.jh.clearing.pojo.DistributionRecordCopy;
import cn.jh.clearing.pojo.DistributionRecordExcel;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.pojo.ProfitRecordCopy;
import cn.jh.clearing.util.Util;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.DownloadExcelUtil;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class DistributionRecordService {

	private static final Logger LOG = LoggerFactory.getLogger(ProfitService.class);
	
	@Autowired
	private ProfitRecordBusiness  profitRecordBusiness;
	
	@Autowired
	private DistributionBusiness  distributionBusiness;
	
	@Autowired
	private PaymentOrderBusiness paymentOrderBusiness;
	
	@Autowired
	Util util;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/query/all/profit")
	public @ResponseBody Object queryallProfit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value = "ordercode",required=false) String order,
			@RequestParam(value = "phone",required=false) String phone,
			@RequestParam(value = "getphone",required=false) String getphone,
			@RequestParam(value = "strTime") String strTime,
			@RequestParam(value = "endTime") String endTime,
			@RequestParam(value = "type", required=false) String[] type,
			@RequestParam(value = "isDownload", defaultValue = "0", required = false) String isDownload,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "ordercode", required = false) String sortProperty){
		Map<String,Object> map = new HashMap<>();
		if(page < 0){
			page = 0;
		}
		
		if(size > 1000){
			size = 1000;
		}else if("1".equals(isDownload)){
			size = 2000;
		}
		
		if("".equals(order))
			order = null;
		if("".equals(phone))
			phone = null;
		if("".equals(getphone))
			getphone = null;
		if("".equals(strTime))
			strTime = null;
		if("".equals(endTime))
			endTime = null;
		if(null==phone&&null==getphone&&null==order) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "参数不能为空");
			return map;
		}
		Date strdate = DateUtil.getDateFromStr("2016-01-01");
		Date enddate = new Date();
		if(null!=strTime)
			 strdate = DateUtil.getDateFromStr(strTime);
		if(null!=endTime)
			enddate = DateUtil.getDateFromStr(endTime);
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<DistributionRecord> pageProfit = null;
		//List<ProfitRecord> listProfit = new ArrayList<ProfitRecord>();
		if(null==phone&&null==getphone) {
			pageProfit = distributionBusiness.findDistributionRecordByordercode(order,  strdate, enddate, pageable);
			if(pageProfit != null&& pageProfit.getSize() > 0){
				if("1".equals(isDownload)){
					List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}
					if(downloadFile == null){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}else{
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			return map;
		}
		if(null==order&&null==getphone) {
			List<PaymentOrder> list = paymentOrderBusiness.findPaymentOrderByTimeAndPhone(phone, strdate, enddate);
			if(list.size()==0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "此用户没有返佣数据");
				return map;
			}
			String[] str = new String[list.size()];
			int i = 0;
			for(PaymentOrder po:list) {
				str[i] = po.getOrdercode();
				i++;
			}
			pageProfit = distributionBusiness.findAllDistributionByoriPhone(phone, str,strdate, enddate, pageable);
			/*List<DistributionRecord> lis = pageProfit.getContent();
			List<DistributionRecord> liss = new ArrayList<DistributionRecord>();
			for(DistributionRecord pr:lis) {
				order = pr.getOrdercode();
				pageProfit = distributionBusiness.findDistributionRecordByordercode(order,  strdate, enddate, pageable);
				List<DistributionRecord> lists = pageProfit.getContent();
				if(lists.size()>1) {
					liss.add(lists.get(0));
					for(int ii=1;ii<lists.size();ii++) {
						lists.get(ii).setOriphone(null);
						lists.get(ii).setAmount(null);
						liss.add(lists.get(ii));
					}
				}
			}*/
			
			if(pageProfit != null&& pageProfit.getSize() > 0){
				if("1".equals(isDownload)){
					List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}
					if(downloadFile == null){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}else{
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			return map;
		}
		if(null==order&&null==phone) {
			pageProfit = distributionBusiness.findAllDistributionByPhone(getphone,strdate, enddate, pageable);
			
			if(pageProfit != null&& pageProfit.getSize() > 0){
				if("1".equals(isDownload)){
					List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}
					if(downloadFile == null){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}else{
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			return map;
		}
		if(null==phone) {
			pageProfit = distributionBusiness.findAllDistributionByPhoneAndacqOrder(getphone, order, strdate, enddate, pageable);
			if(pageProfit != null&& pageProfit.getSize() > 0){
				if("1".equals(isDownload)){
					List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}
					if(downloadFile == null){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}else{
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			return map;
		}
		if(null==getphone) {
			pageProfit = distributionBusiness.findAllDistributionByPhoneAndOrder(phone, order, strdate, enddate, pageable);
			if(pageProfit != null&& pageProfit.getSize() > 0){
				if("1".equals(isDownload)){
					List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}
					if(downloadFile == null){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}else{
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			return map;
		}
		if(null==order) {
			pageProfit = distributionBusiness.findAllDistributionByPhoneAndoriOrder(getphone, phone, strdate, enddate, pageable);
			if(pageProfit != null&& pageProfit.getSize() > 0){
				if("1".equals(isDownload)){
					List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}
					if(downloadFile == null){
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					}else{
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			return map;
		}
		pageProfit = distributionBusiness.findByAllParams(getphone, phone, order, strdate, enddate, pageable);
		if(null==pageProfit||!pageProfit.hasNext()) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "此笔订单没有产生返佣");
			return map;
		}
		if(pageProfit != null&& pageProfit.getSize() > 0){
			if("1".equals(isDownload)){
				List<DistributionRecord> DistributionRecords = new ArrayList<>(pageProfit.getContent());
				String downloadFile;
				try {
					downloadFile = DownloadExcelUtil.downloadFile(request, response, DistributionRecords,new DistributionRecordExcel());
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
					return map;
				}
				if(downloadFile == null){
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
					return map;
				}else{
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
					map.put(CommonConstants.RESULT, downloadFile);
					return map;
				}
			}
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, pageProfit);
		return map;
		
	}
	
	/**获取用户的返佣明细**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/record/query/{token}")
	public @ResponseBody Object pageRecordQuery(HttpServletRequest request, 
			@PathVariable("token") String token,
			 @RequestParam(value = "start_time",  required = false) String  startTime,
			 @RequestParam(value = "end_time",  required = false) String endTime,
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		Map map = new HashMap();
		long userId;
		try{
			userId = TokenUtil.getUserId(token);
		}catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Date StartTimeDate = null;
		if(startTime != null  && !startTime.equalsIgnoreCase("")){
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;
		
		if(endTime != null  && !endTime.equalsIgnoreCase("")){
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}
		String grade =null;
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, profitRecordBusiness.findDistributionRecordByUserid(userId, StartTimeDate, endTimeDate,  pageable));
		return map;
	}
	
	/**平台的返佣明细**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/record/query/all")
	public @ResponseBody Object pageAllRecordQuery(HttpServletRequest request, 
			 //获取人手机号
			 @RequestParam(value = "phone", required = false) String phone,
			 //用户编号
			 @RequestParam(value = "userid", required = false) String userid,
			//用户编号
			 @RequestParam(value = "order_code", required = false) String ordercode,
			 //起始时间
			 @RequestParam(value = "start_time",  required = false) String  startTime,
			 //截止时间
			 @RequestParam(value = "end_time",  required = false) String endTime,
			 //贴牌id
			 @RequestParam(value = "brand_id",defaultValue = "-1", required = false) String  brandid,
			 
			 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
			 @RequestParam(value = "size", defaultValue = "20", required = false) int size,
			 @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			 @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty			
			){
		Map<String,Object> map = new HashMap<String, Object>();
		if(phone!=null&&!phone.equals("")){
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/query/phone";
			/**根据的用户手机号码查询用户的基本信息*/
			long brandId = -1;
			try {
				brandId = Long.valueOf(brandid);
			} catch (NumberFormatException e) {
				brandId = -1;
			}
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandId+"");
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
			
			if(resultObj.containsKey("id")){
				userid  = resultObj.getString("id");
			}else{
				userid="0";
			}	
		}
		
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		Date StartTimeDate = null;
		if(startTime != null  && !startTime.equalsIgnoreCase("")){
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;
		
		if(endTime != null  && !endTime.equalsIgnoreCase("")){
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}
		
		Page<DistributionRecord> distributionRecord=null;
		distributionRecord=profitRecordBusiness.findDistributionRecordByPlatform(Long.valueOf(brandid),userid, ordercode,StartTimeDate, endTimeDate,  pageable);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, distributionRecord);
		return map;
	}
	
	
	/**查询返佣总金额*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/record/query/userid")
	public @ResponseBody Object findAllChannelByuserid(HttpServletRequest request,
			@RequestParam(value = "acq_user_id") long  acquserid
			){
		
		BigDecimal sumAcquserid = profitRecordBusiness.findsumProfitRecord(acquserid);
		
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, sumAcquserid==null?"0.00":sumAcquserid.setScale(2, BigDecimal.ROUND_DOWN));
		return map;
	}
	
	// 查询所有返佣信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/distributionrecord/querybyphone")
	public @ResponseBody Object queryShouQian(HttpServletRequest request,
			@RequestParam(value = "acq_phone") String acqphone, 
			@RequestParam(value = "start_time" , required = false) String startTime,
			@RequestParam(value = "end_time", required = false) String endTime,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction order,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sort) {
		Map map = new HashMap();
		List<Map> listMap = new ArrayList<Map>();
		Pageable pageable = new PageRequest(page, size, new Sort(order, sort));
		Date startTimeDate = null;
		if (startTime != null && !startTime.equalsIgnoreCase("2017-05-01")) {
			startTimeDate = DateUtil.getDateFromStr(startTime);
		} else {
			startTimeDate = DateUtil.getDateFromStr("2017-05-01");
		}
		Date endTimeDate = null;
		if (endTime != null && !endTime.equalsIgnoreCase("")) {
			endTimeDate = DateUtil.getDateFromStr(endTime);
		} else {
			endTimeDate = new Date();
		}
		try {
			List<DistributionRecord> listDistribution = profitRecordBusiness.findAllDistributionByPhone(acqphone,startTimeDate, endTimeDate, pageable);
			DistributionRecord distribution;
			for (int i = 0; i < listDistribution.size(); i++) {
				distribution = listDistribution.get(i);
				Map map1 = new HashMap();
				map1.put("acq_phone", distribution.getAcqphone());
				map1.put("acq_amount", distribution.getAcqAmount());
				map1.put("creat_time", distribution.getCreateTime());
				map1.put("ori_phone", distribution.getOriphone());
				map1.put("amount", distribution.getAmount());
				listMap.add(map1);

			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "数据查询成功");
			map.put(CommonConstants.RESULT, listMap);
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
		return map;
	}

	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/query/profitrecord/byordercode")
	public @ResponseBody Object queryProfitRecordByOrderCode(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value = "orderCode",required=false) String orderCode,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "ordercode", required = false) String sortProperty){
		Map<String,Object> map = new HashMap<>();
		if(page < 0){
			page = 0;
		}
		
		Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));
	
		Page<ProfitRecordCopy> profitRecordCopyByOrderCode = profitRecordBusiness.getProfitRecordCopyByOrderCode(orderCode, pageAble);
		
		if(profitRecordCopyByOrderCode != null) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", profitRecordCopyByOrderCode);
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}
		
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/query/distribution/byordercode")
	public @ResponseBody Object queryDistributionByOrderCode(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value = "orderCode",required=false) String orderCode,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "ordercode", required = false) String sortProperty){
		Map<String,Object> map = new HashMap<>();
		if(page < 0){
			page = 0;
		}
		
		Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));
	
		Page<DistributionRecordCopy> distributionRecordCopyByOrderCode = profitRecordBusiness.getDistributionRecordCopyByOrderCode(orderCode, pageAble);
		
		if(distributionRecordCopyByOrderCode != null) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", distributionRecordCopyByOrderCode);
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}
		
	}

	/**
	 * 根据手机号查询用户返佣总和
	 * @param phone
	 * @return
	 */
	@RequestMapping(method =RequestMethod.POST,value="/v1.0/transactionclear/query/distributionSum/byPhone")
	@ResponseBody
	public Object querySumDisByUserId(
			@RequestParam(value="phone")String phone
	){
		Map map=new HashMap<>();
		Date strdate = DateUtil.getDateFromStr("2016-01-01");
		Date endDate=new Date();
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String startTime=format.format(strdate);
		String endTime=format.format(endDate);
		BigDecimal sum=distributionBusiness.queryDistributionSumAcqAmountByPhone(phone,startTime,endTime);
		map.put(CommonConstants.RESULT,sum);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE,"查询成功");
		return map;
	}
}
