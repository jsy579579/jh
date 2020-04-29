package cn.jh.clearing.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.pojo.ProfitRecord;
import cn.jh.clearing.pojo.ProfitRecordExcel;
import cn.jh.clearing.pojo.ProfitRecordPo;
import cn.jh.clearing.util.Util;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.DownloadExcelUtil;
import cn.jh.common.utils.RandomUtils;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class ProfitService {

	private static final Logger LOG = LoggerFactory.getLogger(ProfitService.class);

	@Autowired
	private ProfitRecordBusiness profitRecordBusiness;

	@Autowired
	private PaymentOrderBusiness paymentOrderBusiness;

	@Autowired
	private DistributionBusiness distributionBusiness;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	Util util;

	/** 获取用户的分润明细 **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/query/{token}")
	public @ResponseBody Object pageProfitQuery(HttpServletRequest request, @PathVariable("token") String token,
			@RequestParam(value = "start_time", required = false) String startTime,
			@RequestParam(value = "end_time", required = false) String endTime,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}

		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Date StartTimeDate = null;
		if (startTime != null && !startTime.equalsIgnoreCase("")) {
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		}
		Date endTimeDate = null;

		if (endTime != null && !endTime.equalsIgnoreCase("")) {
			endTimeDate = DateUtil.getDateFromStr(endTime);
		}
		String grade = null;
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT,
				profitRecordBusiness.findProfitByUserid(userId + "", grade, StartTimeDate, endTimeDate, pageable));
		return map;
	}

	/** 获取用户的分润明细 **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/query/all")
	public @ResponseBody Object pageAllProfitQuery(HttpServletRequest request,
			// 用户手机号
			@RequestParam(value = "phone", defaultValue = "", required = false) String phone,
			// 用户编号
			@RequestParam(value = "userid", defaultValue = "0", required = false) long userid,
			// 起始时间
			@RequestParam(value = "start_time", required = false) String startTime,
			// 截止时间
			@RequestParam(value = "end_time", required = false) String endTime,
			// 贴牌id
			@RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandid,
			// 等级
			@RequestParam(value = "grade", required = false) String grade,
			// 身份证号
			@RequestParam(value = "idcard", required = false) String idcard,
			// 银行卡号
			@RequestParam(value = "cardNo", required = false) String cardNo,
			// 交易订单号
			@RequestParam(value = "orderCode", required = false, defaultValue = "") String orderCode,

			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Map<String, Object> map = new HashMap<String, Object>();

		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Date StartTimeDate = null;
		if (startTime != null && !startTime.equalsIgnoreCase("")) {
			StartTimeDate = DateUtil.getDateFromStr(startTime);
		} else {
			StartTimeDate = DateUtil.getDateFromStr("1970-1-1");
		}
		Date endTimeDate = null;

		if (endTime != null && !endTime.equalsIgnoreCase("")) {
			endTimeDate = DateUtil.getDateFromStr(endTime);
		} else {
			endTimeDate = new Date();
		}
		String userId = "";
		if (phone != null && !phone.equals("")) {
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/query/phone";
			/** 根据的用户手机号码查询用户的基本信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandid + "");
			RestTemplate restTemplate = new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");

			if (resultObj.containsKey("id")) {
				userId = resultObj.getString("id");
			} else {
				userId = "0";

			}
		}

		if (userid != 0) {
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/query/id";
			/** 根据的用户手机号码查询用户的基本信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("id", userid + "");
			RestTemplate restTemplate = new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");

			if (resultObj.containsKey("id")) {
				userId = resultObj.getString("id");
			} else {
				userId = "0";
			}
		}
		/***
		 * 身份证不为空判定
		 **/
		if (idcard != null && !idcard.equals("")) {

			/** 获取身份证实名信息 */
			URI uri = util.getServiceUrl("paymentchannel", "error url request!");
			String url = uri.toString() + "/v1.0/paymentchannel/realname/idcard";

			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("idcard", idcard);
			RestTemplate restTemplate = new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			JSONObject jsonObject = JSONObject.fromObject(result);
			if (result == null) {
				userId = "0";
			} else {
				userId = jsonObject.getString("userId");
			}
		}
		/***
		 * 银行卡不为空判定
		 **/
		if (idcard != null && !idcard.equals("")) {

			/** 获取银行卡信息 */
			URI uri = util.getServiceUrl("paymentchannel", "error url request!");
			String url = uri.toString() + "/v1.0/user/bank/default/cardno";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("cardno", cardNo);
			RestTemplate restTemplate = new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			JSONObject jsonObject = JSONObject.fromObject(result);
			if (result == null) {
				userId = "0";
			} else {
				userId = jsonObject.getString("userId");
			}

		}

		Page<ProfitRecord> ProfitRecord = null;
		// 没贴牌ID
		if ("".equals(orderCode) || null == orderCode) {
			if (brandid == -1) {

				ProfitRecord = profitRecordBusiness.findProfitByUserid(userId, grade, StartTimeDate, endTimeDate,
						pageable);
			} else {

				ProfitRecord = profitRecordBusiness.findProfitByBrandId(brandid, userId, grade, StartTimeDate,
						endTimeDate, pageable);
			}
		} else {
			ProfitRecord = profitRecordBusiness.findProfitByUserid(orderCode, StartTimeDate, endTimeDate, pageable);
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, ProfitRecord);
		return map;
	}

	/**
	 * 查询提现总金额
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/query/userid")
	public @ResponseBody Object findAllChannelByuserid(HttpServletRequest request,
			@RequestParam(value = "acq_user_id") long acquserid) {

		BigDecimal sumAcquserid = profitRecordBusiness.findsumProfitRecord(acquserid);

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT,
				sumAcquserid == null ? "0.00" : sumAcquserid.setScale(2, BigDecimal.ROUND_DOWN));
		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/query/byuserids")
	public @ResponseBody Object findAllChannelByUserIds(HttpServletRequest request,
			@RequestParam(value = "acqUserIds") String[] sacqUserIds) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> sumProfitMap = new HashMap<String, Object>();
		long[] acqUserIds = null;
		if (sacqUserIds.length > 0) {
			acqUserIds = new long[sacqUserIds.length];
			for (int i = 0; i < sacqUserIds.length; i++) {
				acqUserIds[i] = Long.valueOf(sacqUserIds[i]);
			}
		}
		List<Object[]> models = profitRecordBusiness.findsumProfitRecordByAcqUserIds(acqUserIds);
		if (models != null && models.size() != 0) {
			for (int i = 0; i < models.size(); i++) {
				sumProfitMap.put(models.get(i)[0] + "", models.get(i)[1]);
			}
		}
		// map.put("models", sumProfitMap);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, sumProfitMap);
		return map;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/queryall/profit")
	public @ResponseBody Object queryallProfit(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode", required = false) String order,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "getphone", required = false) String getphone,
			@RequestParam(value = "strTime", required = false) String strTime,
											   @RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "type", required = false) String[] type,
			@RequestParam(value = "isDownload", defaultValue = "0", required = false) String isDownload,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "ordercode", required = false) String sortProperty) {
		if (page < 0) {
			page = 0;
		}
		if (size > 1000) {
			size = 1000;
		} else if ("1".equals(isDownload)) {
			size = 2000;
		}

		Map<String, Object> map = new HashMap<>();
		if ("".equals(order))
			order = null;
		if ("".equals(phone))
			phone = null;
		if ("".equals(getphone))
			getphone = null;
		if ("".equals(strTime))
			strTime = null;
		if ("".equals(endTime))
			endTime = null;
		if (null == phone && null == getphone && null == order) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "参数不能为空");
			return map;
		}
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<ProfitRecord> pageProfit = null;
		// List<ProfitRecord> listProfit = new ArrayList<ProfitRecord>();
		Date strdate = DateUtil.getDateFromStr("2016-01-01");
		Date enddate = new Date();
		if (null != strTime)
			strdate = DateUtil.getDateFromStr(strTime);
		if (null != endTime)
			enddate = DateUtil.getDateFromStr(endTime);
		if (null == type || type.length == 0) {
			type = new String[6];
			type[0] = "0";
			type[1] = "1";
			type[2] = "2";
			type[3] = "3";
			type[4] = "5";
			type[5] = "6"; // 会员直推分润
		}
		if (null == phone && null == getphone) {
			pageProfit = profitRecordBusiness.finByManyParams(order, type, strdate, enddate, pageable);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			if ("1".equals(isDownload)) {
				if (pageProfit != null && pageProfit.getSize() > 0) {
					List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
								new ProfitRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败");
						return map;
					}
					if (downloadFile == null) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
					return map;
				}
			}
			return map;
		}
		if (null == order && null == getphone) {
			List<PaymentOrder> list = paymentOrderBusiness.findPaymentOrderByTimeAndPhone(phone, strdate, enddate);
			if (list.size() == 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "此用户没有分润数据");
				return map;
			}
			String[] str = new String[list.size()];
			int i = 0;
			for (PaymentOrder po : list) {
				str[i] = po.getOrdercode();
				i++;
			}
			pageProfit = profitRecordBusiness.queryProfitAmountByPhone(phone, str, type, strdate, enddate, pageable);
			/*
			 * List<ProfitRecord> lis = pageProfit.getContent(); List<ProfitRecord> liss =
			 * new ArrayList<ProfitRecord>(); for(ProfitRecord pr:lis) { order =
			 * pr.getOrdercode(); pageProfit = profitRecordBusiness.finByManyParams(order,
			 * type, strdate, enddate, pageable); List<ProfitRecord> lists =
			 * pageProfit.getContent(); if(lists.size()>1) { liss.add(lists.get(0)); for(int
			 * ii=1;ii<lists.size();ii++) { lists.get(ii).setOriphone(null);
			 * lists.get(ii).setOrirate(null); lists.get(ii).setAmount(null);
			 * liss.add(lists.get(ii)); } } } Map mapp = new HashMap(); mapp.put("content",
			 * liss); mapp.put("last", true); mapp.put("totalPages", 1+liss.size()/size);
			 * mapp.put("totalElements", liss.size()); mapp.put("number", page);
			 * mapp.put("size", size); mapp.put("first", true); mapp.put("numberOfElements",
			 * liss.size());//此处liss.size()可能大于size List<Map> lil = new ArrayList<Map>();
			 * Map mam = new HashMap(); mam.put("direction", "DESC"); mam.put("property",
			 * "ordercode"); mam.put("ignoreCase", false); mam.put("nullHandling",
			 * "NATIVE"); mam.put("ascending", false); lil.add(mam); mapp.put("sort", lil);
			 */
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);

			if ("1".equals(isDownload)) {
				if (pageProfit != null && pageProfit.getSize() > 0) {
					List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
								new ProfitRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败");
						return map;
					}
					if (downloadFile == null) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
					return map;
				}
			}

			return map;
		}
		if (null == order && null == phone) {
			pageProfit = profitRecordBusiness.queryProfitAmountByGetPhone(getphone, type, strdate, enddate, pageable);
			Object obj = profitRecordBusiness.queryProfitAll(getphone, type, strdate, enddate, pageable);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			if ("1".equals(isDownload)) {
				if (pageProfit != null && pageProfit.getSize() > 0) {
					List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
								new ProfitRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败");
						return map;
					}
					if (downloadFile == null) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
					return map;
				}
			}
			map.put("sum", obj);
			return map;
		}
		if (null == phone) {
			pageProfit = profitRecordBusiness.queryProfitAmountByOderGetPhone(getphone, order, type, strdate, enddate,
					pageable);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			if ("1".equals(isDownload)) {
				if (pageProfit != null && pageProfit.getSize() > 0) {
					List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
								new ProfitRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败");
						return map;
					}
					if (downloadFile == null) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
					return map;
				}
			}
			return map;
		}
		if (null == getphone) {
			pageProfit = profitRecordBusiness.queryProfitAmountByOderPhone(phone, order, type, strdate, enddate,
					pageable);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			if ("1".equals(isDownload)) {
				if (pageProfit != null && pageProfit.getSize() > 0) {
					List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
								new ProfitRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败");
						return map;
					}
					if (downloadFile == null) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
					return map;
				}
			}
			return map;
		}
		if (null == order) {
			pageProfit = profitRecordBusiness.queryProfitAmountByDoublePhone(phone, getphone, type, strdate, enddate,
					pageable);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "查询成功");
			map.put(CommonConstants.RESULT, pageProfit);
			if ("1".equals(isDownload)) {
				if (pageProfit != null && pageProfit.getSize() > 0) {
					List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
					String downloadFile;
					try {
						downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
								new ProfitRecordExcel());
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败");
						return map;
					}
					if (downloadFile == null) {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
						return map;
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
						map.put(CommonConstants.RESULT, downloadFile);
						return map;
					}
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
					return map;
				}
			}
			return map;
		}
		pageProfit = profitRecordBusiness.queryByAllParams(phone, getphone, order, type, strdate, enddate, pageable);
		if (null == pageProfit || !pageProfit.hasNext()) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "此笔订单没有产生分润");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, pageProfit);
		if ("1".equals(isDownload)) {
			if (pageProfit != null && pageProfit.getSize() > 0) {
				List<ProfitRecord> profitRecords = new ArrayList<>(pageProfit.getContent());
				String downloadFile;
				try {
					downloadFile = DownloadExcelUtil.downloadFile(request, response, profitRecords,
							new ProfitRecordExcel());
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "下载失败");
					return map;
				}
				if (downloadFile == null) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
					return map;
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
					map.put(CommonConstants.RESULT, downloadFile);
					return map;
				}
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
				return map;
			}
		}
		return map;
	}

	/*
	 * 根据手机号和brandid查询分润额
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/gradebrand/byphone")
	public @ResponseBody Object queryGradeBrandSchedule(HttpServletRequest request,
			// 用户手机号
			@RequestParam(value = "phone") String phone,
			// token
			@RequestParam(value = "brand_id") long brandid,
			// 起始时间
			@RequestParam(value = "start_time", required = false) String startTime,
			// 结束时间
			@RequestParam(value = "end_time", required = false) String endTime,
			// 用户订单号
			@RequestParam(value = "order_code", required = false) String ordercode,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction order,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sort) {
		Map map = new HashMap();
		Pageable pageable = new PageRequest(page, size, new Sort(order, sort));
		Date startTimeDate = null;
		if (startTime != null && !startTime.equalsIgnoreCase("")) {
			startTimeDate = DateUtil.getDateFromStr(startTime);
		} else {
			startTimeDate = DateUtil.getDateFromStr("2016-05-01");
		}
		Date endTimeDate = null;
		if (endTime != null && !endTime.equalsIgnoreCase("")) {
			endTimeDate = DateUtil.getDateFromStr(endTime);
		} else {
			endTimeDate = new Date();
		}
		try {
			if (phone != null && brandid != -1) {
				PaymentOrder paymentObject;
				List<ProfitRecord> AcqPhone;
				ProfitRecord profitObject;
				String oriphone;
				List<PaymentOrder> paymentpojo = paymentOrderBusiness.findOrderByphoneAndbrandid(phone, brandid,
						startTimeDate, endTimeDate, pageable);
				List<Map> listMap1 = null;
				List<Map> listMap2 = new ArrayList<Map>();
				Map map1 = null;
				for (int i = 0; i < paymentpojo.size(); i++) {
					listMap1 = new ArrayList<Map>();
					map1 = new HashMap();
					paymentObject = paymentpojo.get(i);
					phone = paymentObject.getPhone();
					ordercode = paymentObject.getOrdercode();
					map.put("phone", paymentObject.getPhone());
					map.put("rate", paymentObject.getRate());
					map1.put("amount", paymentObject.getAmount());
					map1.put("order", ordercode);
					List<ProfitRecord> listProfit = profitRecordBusiness.queryProfitAmount(ordercode);
					if (listProfit.size() == 0) {
						continue;
					}
					for (int j = 0; j < listProfit.size(); j++) {
						Map map2 = new HashMap();
						oriphone = phone;
						AcqPhone = profitRecordBusiness.queryProfitByOriPhone(oriphone, ordercode);
						if (AcqPhone.size() == 1) {
							profitObject = AcqPhone.get(0);
							map2.put("acqphone", profitObject.getAcqphone());
							map2.put("acqamount", profitObject.getAcqAmount());
							map2.put("acqrate", profitObject.getAcqrate());
							map2.put("remark", profitObject.getRemark());
							phone = profitObject.getAcqphone();
							listMap1.add(map2);
						} else {
							for (int k = 0; k < AcqPhone.size(); k++) {
								map2 = new HashMap();
								profitObject = AcqPhone.get(k);
								map2.put("acqphone", profitObject.getAcqphone());
								map2.put("acqamount", profitObject.getAcqAmount());
								map2.put("acqrate", profitObject.getAcqrate());
								map2.put("remark", profitObject.getRemark());
								phone = profitObject.getAcqphone();
								listMap1.add(map2);
							}
						}
						map1.put("brand", listMap1);
					}
					listMap2.add(map1);
				}
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "数据查询成功");
				map.put(CommonConstants.RESULT, listMap2);
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
				map.put(CommonConstants.RESP_MESSAGE, "手机号不存在");
			}
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
		return map;
	}

	/** 获取用户的分润明细 **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/week")
	public @ResponseBody Object pageProfitQuery(HttpServletRequest request,
			@RequestParam(value = "start_time", defaultValue = "2018-03-05", required = false) String startTime,
			@RequestParam(value = "end_time", defaultValue = "2018-03-19", required = false) String endTime) {

		LOG.info("周提现分润结算");

		Date todayDate = null;

		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");

		Date endDate = null;
		try {
			endDate = dft.parse(startTime);
			todayDate = dft.parse(endTime);

		} catch (ParseException e) {
			e.printStackTrace();
			LOG.error("",e);
		}
		// 自清充值返利
		List<PaymentOrder> brandWithdrawClearRebates = paymentOrderBusiness.queryWeekBrandWithdrawClearRebate(endDate,
				todayDate);
		if (brandWithdrawClearRebates != null) {
			LOG.info("RESULT===============日期=" + endDate + "=======清算数量=" + brandWithdrawClearRebates.size());
			for (PaymentOrder withdrawRebate : brandWithdrawClearRebates) {

				ProfitRecord profitRecord = profitRecordBusiness
						.queryProfitRecordByordercode(withdrawRebate.getOrdercode(), 1 + "");
				if (profitRecord != null)
					continue;
				/*** 获取平台的管理员账号 */
				long brandid = withdrawRebate.getBrandid();
				String ordercode = withdrawRebate.getOrdercode();
				RestTemplate restTemplate = new RestTemplate();
				URI uri = util.getServiceUrl("user", "error url request!");
				String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
				String result = restTemplate.getForObject(url, String.class);
				/** 根据的渠道标识或去渠道的相关信息 */
				LOG.info("RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				if (!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)) {
					LOG.info("失败返利订单================" + withdrawRebate.getOrdercode() + "============brand_id==="
							+ brandid);
					continue;
				}
				JSONObject resultObj = jsonObject.getJSONObject("result");
				String manageid = resultObj.getString("manageid");
				/** 根据manageid获取用户 */
				uri = util.getServiceUrl("user", "error url request!");
				url = uri.toString() + "/v1.0/user/query/id";
				/** 根据的渠道标识或去渠道的相关信息 */
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("id", manageid);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				if (!jsonObject.getString("resp_code").equals(CommonConstants.SUCCESS)) {
					LOG.info("失败返利订单================" + withdrawRebate.getOrdercode() + "============manageid==="
							+ manageid);
					continue;
				}
				resultObj = jsonObject.getJSONObject("result");
				String phone = resultObj.getString("phone");
				/** 根据订单号获取订单 */
				PaymentOrder paymentOrder = paymentOrderBusiness.queryPaymentOrderBycode(ordercode);
				profitRecord = profitRecordBusiness.queryProfitRecordByordercode(ordercode, "1");
				if (profitRecord != null) {
					continue;
				} else {
					BigDecimal AcqAmount = new BigDecimal("0.00").setScale(2, BigDecimal.ROUND_DOWN);
					try {
						AcqAmount = paymentOrder.getExtraFee().subtract(paymentOrder.getCostfee()).setScale(2,
								BigDecimal.ROUND_DOWN);
					} catch (Exception e) {
						LOG.info("RESULT=============getExtraFee===" + paymentOrder.getExtraFee()
								+ "====================getCostfee===" + paymentOrder.getCostfee());
						continue;
					}
					if (AcqAmount.compareTo(new BigDecimal("0.00")) <= 0) {
						continue;
					}
					/** 存贮分润记录明细 */
					profitRecord = new ProfitRecord();
					profitRecord.setId(Long.parseLong(RandomUtils.generateNumString(8)));
					profitRecord.setAcqAmount(AcqAmount);
					profitRecord.setAcqphone(phone);
					profitRecord.setBrandId(withdrawRebate.getBrandid()+"");
					profitRecord.setAcquserid(Long.parseLong(manageid));
					profitRecord.setAmount(paymentOrder.getAmount());
					profitRecord.setCreateTime(new Date());
					profitRecord.setOrdercode(ordercode);
					profitRecord.setOriphone(paymentOrder.getPhone());
					profitRecord.setOrirate(paymentOrder.getRate());
					profitRecord.setOriuserid(paymentOrder.getUserid());
					profitRecord.setRemark("品牌自清分润");
					profitRecord.setType("1");
					profitRecord.setScale(BigDecimal.ONE);
					profitRecordBusiness.merge(profitRecord);
					/** 存储 用户的分润记录 */
					try {
						restTemplate = new RestTemplate();
						uri = util.getServiceUrl("user", "error url request!");
						url = uri.toString() + "/v1.0/user/rebate/update";
						/** 根据的渠道标识或去渠道的相关信息 */
						requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("rebate_amount", AcqAmount.toString());
						requestEntity.add("user_id", manageid);
						requestEntity.add("order_code", ordercode);
						result = restTemplate.postForObject(url, requestEntity, String.class);
					} catch (Exception e) {
						LOG.info("RESULT===============返利疑似失败====================order_code===" + ordercode);
						e.printStackTrace();
						LOG.error("",e);
						continue;
					}

				}
			}
		}
		return null;
	}

	
	// 查询每个月获得收益总额的接口
	@SuppressWarnings("deprecation")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/sumofmonth/queryby/acquserid")
	public @ResponseBody Object queryProfitSumOfMonthByAcqUserId(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size) throws Exception {

		boolean hasKey = false;
		String key = "/v1.0/transactionclear/profit/sumofmonth/queryby/acquserid:userId=" + userId + ";page=" + page + ";size="
				+ size;
		ValueOperations<String, Object> operations;
		try {
			operations = redisTemplate.opsForValue();
			hasKey = redisTemplate.hasKey(key);
			if (hasKey) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", operations.get(key));
			}
		} catch (Exception e1) {
			LOG.info("redis获取数据出错======", e1.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "当前查询人数过多,请稍后重试!");
		}
		
		
		Calendar cale = Calendar.getInstance();
		int year = cale.get(Calendar.YEAR);
		int month = cale.get(Calendar.MONTH) + 2;

		Date d1 = new SimpleDateFormat("yyyy-MM").parse("2017-1");// 定义起始日期
		Date d2 = new SimpleDateFormat("yyyy-MM").parse(year + "-" + month);// 定义结束日期
		
		/*Date d1 = new SimpleDateFormat("yyyy-MM").parse(startDate);// 定义起始日期
		Date d2 = new SimpleDateFormat("yyyy-MM").parse(Integer.parseInt(s));// 定义结束日期
*/		
		Calendar dd = Calendar.getInstance();// 定义日期实例
		dd.setTime(d1);// 设置日期起始时间

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		TreeMap<String, String> map = new TreeMap<String, String>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        // 降序排序
                        return obj2.compareTo(obj1);
                    }
                });
		
		while (dd.getTime().before(d2)) {// 判断是否到结束日期

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

			String str = sdf.format(dd.getTime());

			LOG.info("获取到的日期======" + str);// 输出日期结果

			String before = str.substring(0, 4);
			String after = str.substring(5);
			
			BigDecimal sumProfitRecordByDate = profitRecordBusiness.getSumProfitRecordByDate(Long.parseLong(userId), this.getFirstDayOfMonth(Integer.parseInt(before), Integer.parseInt(after))+ " 00:00:00", this.getLastDayOfMonth(Integer.parseInt(before), Integer.parseInt(after)) + " 23:59:59");
			BigDecimal sumDistributionRecordByDate = distributionBusiness.getSumDistributionRecordByDate(Long.parseLong(userId), this.getFirstDayOfMonth(Integer.parseInt(before), Integer.parseInt(after))+ " 00:00:00", this.getLastDayOfMonth(Integer.parseInt(before), Integer.parseInt(after)) + " 23:59:59");

			String add = null;
			if (sumProfitRecordByDate != null && sumDistributionRecordByDate != null) {
				add = sumProfitRecordByDate.add(sumDistributionRecordByDate) + "";
			} else if (sumProfitRecordByDate != null) {
				if (sumDistributionRecordByDate != null) {
					add = sumProfitRecordByDate.add(sumDistributionRecordByDate) + "";
				} else {
					add = sumProfitRecordByDate + "";
				}
			} else if (sumDistributionRecordByDate != null) {
				if (sumProfitRecordByDate != null) {
					add = sumDistributionRecordByDate.add(sumProfitRecordByDate) + "";
				} else {
					add = sumDistributionRecordByDate + "";
				}
			}else {
				add = "0.00";
			}
			
			map.put(str, add);

			dd.add(Calendar.MONTH, 1);// 进行当前日期月份加1

		}
		
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		
		while(it.hasNext()) {
			String next = it.next();
			
			jsonObject.put("date", next);
			jsonObject.put("profit", map.get(next));
			
			jsonArray.add(jsonObject);
		}
		
		List<Object> list = JSONArray.toList(jsonArray);
		
		Map<String, Object> object = new HashMap<String, Object>();
		Map<Object, Object> map1 = (Map<Object, Object>) fenye(list, size);
		List<String> l = (List<String>) map1.get(page);

		int k = 0;
		if (list.size() != 0 && size != 0) {
			k = list.size() / size + (list.size() % size != 0 ? 1 : 0);
		}

		object.put("pageNum", size); // 每页显示条数
		object.put("currentPage", page); // 当前页
		object.put("totalElements", list.size()); // 总条数
		if (size != 0) {
			object.put("totalPages", k); // 总页数
		}
		object.put("content", JSONArray.fromObject(l));
		
		//int nowToTheNextDay = getNowToTheNextDay();
		//LOG.info("剩余时间====" + nowToTheNextDay + "秒");
		//operations.set(key, object, nowToTheNextDay, TimeUnit.SECONDS);
		operations.set(key, object, 1, TimeUnit.HOURS);
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", object);
	}


	// 根据获得收益用户的userId分页查询获得收益日期和收益总和的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/queryby/acquserid")
	public @ResponseBody Object queryProfitByAcqUserId(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size) throws Exception {

		boolean hasKey = false;
		String key = "/v1.0/transactionclear/profit/queryby/acquserid:userId=" + userId + ";date=" + date + ";page=" + page + ";size="
				+ size;
		ValueOperations<String, Object> operations;
		try {
			operations = redisTemplate.opsForValue();
			hasKey = redisTemplate.hasKey(key);
			if (hasKey) {
				// operations.get(key);

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", operations.get(key));
			}
		} catch (Exception e1) {
			LOG.info("redis获取数据出错======", e1.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "当前查询人数过多,请稍后重试!");
		}

		String yearParam = date.substring(0, 4);
		String monthParam = date.substring(5);
		
		List<String> list = new ArrayList<String>();
		Calendar aCalendar = Calendar.getInstance(Locale.CHINA);
		aCalendar.set(Integer.parseInt(yearParam), Integer.parseInt(monthParam) - 1, 1);
		int year = aCalendar.get(Calendar.YEAR);// 年份
		int month = aCalendar.get(Calendar.MONTH) + 1;// 月份
		int day = aCalendar.getActualMaximum(Calendar.DATE);
		
		Calendar cale = Calendar.getInstance();
		int day1 = cale.get(Calendar.DATE);
		
		for (int i = 1; i <= day; i++) {
			String aDate = null;
			if (month < 10 && i < 10) {
				aDate = String.valueOf(year) + "-0" + month + "-0" + i;
			}
			if (month < 10 && i >= 10) {
				aDate = String.valueOf(year) + "-0" + month + "-" + i;
			}
			if (month >= 10 && i < 10) {
				aDate = String.valueOf(year) + "-" + month + "-0" + i;
			}
			if (month >= 10 && i >= 10) {
				aDate = String.valueOf(year) + "-" + month + "-" + i;
			}

			list.add(aDate);
		}
		
		List<String> lists = new ArrayList<String>();
		
		if(date.equals(year + "-" + month)) {
			
			for(int i = 0; i<day1; i++) {
				lists.add(list.get(i));
			}
			
			LOG.info("lists======" + lists);
			
		}else {
			
			lists.addAll(list);
			
			LOG.info("lists======" + lists);
		}
		
		lists.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				try {
					return o2.compareTo(o1);
				} catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
			
			}
		});
		
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			JSONObject jsonObject = new JSONObject();
			JSONArray JSONArray = new JSONArray();
			String add = null;
			for (String str : lists) {
				String str1 = str + " 00:00:00";
				String str2 = str + " 23:59:59";
				BigDecimal sumProfitRecordByDate = profitRecordBusiness.getSumProfitRecordByDate(Long.parseLong(userId),
						str1, str2);
				BigDecimal sumDistributionRecordByDate = distributionBusiness
						.getSumDistributionRecordByDate(Long.parseLong(userId), str1, str2);

				if (sumProfitRecordByDate != null && sumDistributionRecordByDate != null) {
					add = sumProfitRecordByDate.add(sumDistributionRecordByDate) + "";
				} else if (sumProfitRecordByDate != null) {
					if (sumDistributionRecordByDate != null) {
						add = sumProfitRecordByDate.add(sumDistributionRecordByDate) + "";
					} else {
						add = sumProfitRecordByDate + "";
					}
				} else if (sumDistributionRecordByDate != null) {
					if (sumProfitRecordByDate != null) {
						add = sumDistributionRecordByDate.add(sumProfitRecordByDate) + "";
					} else {
						add = sumDistributionRecordByDate + "";
					}
				}else {
					add = "0.00";
				}

				if (sdf.format(new Date()).equals(str)) {
					jsonObject.put("date", "今天");
					jsonObject.put("rebate", "+" + add);
				} else if (getYesterDay().equals(str)) {
					jsonObject.put("date", "昨天");
					jsonObject.put("rebate", "+" + add);
				} else {
					jsonObject.put("date", str);
					jsonObject.put("rebate", "+" + add);
				}

				JSONArray.add(jsonObject);
			}

			Map<String, Object> object = new HashMap<String, Object>();
			
			int k = 0;
			if (list.size() != 0 && size != 0) {
				k = list.size() / size + (list.size() % size != 0 ? 1 : 0);
			}

			object.put("pageNum", size); // 每页显示条数
			object.put("currentPage", page); // 当前页
			object.put("totalElements", list.size()); // 总条数
			if (size != 0) {
				object.put("totalPages", k); // 总页数
			}
			object.put("content", JSONArray.toString());

			int nowToTheNextDay = getNowToTheNextDay();
			LOG.info("剩余时间====" + nowToTheNextDay + "秒");
			operations.set(key, object, nowToTheNextDay, TimeUnit.SECONDS);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", object);
		
		/*} else {

			object.put("content", "[]");
			return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据!", object);
		}*/

	}

	// 根据获得收益用户的userId和具体日期分页查询获得收益日期明细的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/queryby/acquserid/anddate")
	public @ResponseBody Object queryProfitByAcqUserIdAndDate(HttpServletRequest request,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "date") String date,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction order,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sort) throws Exception {

		Pageable pageable = new PageRequest(page, size, new Sort(order, sort));

		boolean hasKey = false;
		String key = "/v1.0/transactionclear/profit/queryby/acquserid/anddate:userId=" + userId + ";date=" + date
				+ ";page=" + page + ";size=" + size + ";order=" + order + ";sort=" + sort;
		ValueOperations<String, Object> operations;
		try {
			operations = redisTemplate.opsForValue();
			hasKey = redisTemplate.hasKey(key);
			if (hasKey) {
				// return operations.get(key);

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", operations.get(key));
			}
		} catch (Exception e2) {
			LOG.info("redis获取数据出错======", e2);
			return ResultWrap.init(CommonConstants.FALIED, "当前查询人数过多,请稍后重试!");
		}

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		if ("今天".equals(date.trim())) {
			date = sdf1.format(new Date());
		}
		if ("昨天".equals(date.trim())) {
			date = getYesterDay();
		}

		String str1 = date + " 00:00:00";
		String str2 = date + " 23:59:59";

		Map profitRecordByUserIdAndDate = null;
		List<ProfitRecordPo> list = null;
		try {
			profitRecordByUserIdAndDate = profitRecordBusiness.getProfitRecordByUserIdAndDate(Long.parseLong(userId),
					str1, str2, pageable);
			list = (List<ProfitRecordPo>) profitRecordByUserIdAndDate.get("content");
		} catch (Exception e1) {
			LOG.info("根据userId和日期查询收益明细出错啦======", e1.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "当前查询人数过多,请稍后重试!");
		}

		Map<String, Object> object = new HashMap<String, Object>();
		if (list != null && list.size() > 0) {

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			list.sort(new Comparator<ProfitRecordPo>() {

				@Override
				public int compare(ProfitRecordPo o1, ProfitRecordPo o2) {
					try {
						return sdf.parse(o2.getCreateTime()).compareTo(sdf.parse(o1.getCreateTime()));
					} catch (ParseException e) {
						e.printStackTrace();
						return 0;
					}
				}

			});

			Map<Object, Object> map = (Map<Object, Object>) fenye(list, size);
			List<String> l = (List<String>) map.get(page);

			int k = 0;
			if (list.size() != 0 && size != 0) {
				k = list.size() / size + (list.size() % size != 0 ? 1 : 0);
			}

			object.put("pageNum", size); // 每页显示条数
			object.put("currentPage", page); // 当前页
			object.put("totalElements", list.size()); // 总条数
			if (size != 0) {
				object.put("totalPages", k); // 总页数
			}
			object.put("content", JSONArray.fromObject(l));
			operations.set(key, object, 30, TimeUnit.MINUTES);
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", object);
		} else {

			object.put("content", new JSONArray());
			return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据!", object);
		}

	}

	// 分页的方法
	public static Object fenye(List list, int pagesize) {

		int totalcount = list.size();
		int pagecount = 0;
		int m = totalcount % pagesize;
		if (m > 0) {
			pagecount = totalcount / pagesize + 1;
		} else {
			pagecount = totalcount / pagesize;
		}

		int j = 0;
		Map<Object, Object> map = new HashMap<Object, Object>();
		for (int i = 1; i <= pagecount; i++) {
			if (m == 0) {
				List<Integer> subList = list.subList((i - 1) * pagesize, pagesize * (i));
				map.put(j, subList);
			} else {
				if (i == pagecount) {
					List<Integer> subList = list.subList((i - 1) * pagesize, totalcount);
					map.put(j, subList);
				} else {
					List<Integer> subList = list.subList((i - 1) * pagesize, pagesize * (i));
					map.put(j, subList);
					j++;
				}
			}
		}

		return map;

	}

	// 获取昨天时间的方法
	public String getYesterDay() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

		return yesterday;
	}

	// 获取一段时间内每一天日期的方法
	public static List<String> findDates(Date startDate, Date endDate) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		List<String> lDate = new ArrayList<String>();
		lDate.add(sdf.format(startDate));
		Calendar calBegin = Calendar.getInstance();
		// 使用给定的 Date设置此 Calendar的时间
		calBegin.setTime(startDate);
		Calendar calEnd = Calendar.getInstance();
		// 使用给定的 Date设置此 Calendar的时间
		calEnd.setTime(endDate);
		// 测试此日期是否在指定日期之后
		while (endDate.after(calBegin.getTime())) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			String format = sdf.format(calBegin.getTime());
			lDate.add(format);
		}

		return lDate;
	}

	// 获取上个月第一天的日期的方法
	public static String getFirstDayOfLastMonth() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.MONTH, -1);
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		String firstDay = sdf.format(calendar1.getTime());
		return firstDay;
	}

	// 获取当前时间距离第二天凌晨0点剩余多少分钟的方法
	public static int getNowToTheNextDay() {
		Calendar midnight = Calendar.getInstance();
		midnight.setTime(new Date());
		midnight.add(midnight.DAY_OF_MONTH, 1);
		midnight.set(midnight.HOUR_OF_DAY, 0);
		midnight.set(midnight.MINUTE, 0);
		midnight.set(midnight.SECOND, 0);
		midnight.set(midnight.MILLISECOND, 0);
		Integer seconds = (int) ((midnight.getTime().getTime() - new Date().getTime()) / 1000);

		return seconds;
	}

	// 获取某个月最后一天的方法
	public static String getLastDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime());
	}

	// 获取某个月第一天的方法
	public static String getFirstDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime());
	}

	/**
	 * 每日收益汇总列表
	 * @param userId
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/profit/query/list")
	public @ResponseBody Object listProfitByUserId(
			@RequestParam("userId") Long userId,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction order,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sort){
		Map<String, Object> map = new HashMap<>();
		//Pageable pageable = new PageRequest(page, size);
		cn.jh.clearing.util.Page<Object[]> list = profitRecordBusiness.listProfitByUserId(userId,page,size);

		LOG.info("Result====================" + list);
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE,"查询每日分润明细成功！");
		map.put(CommonConstants.RESULT,list);
		return map;
	}

}
