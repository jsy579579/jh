	package com.jh.user.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.ChannelRateBusiness;
import com.jh.user.business.UserBankInfoBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.Channel;
import com.jh.user.pojo.ChannelRate;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserBankInfo;
import com.jh.user.repository.ChannelRepository;
import com.jh.user.util.Util;
import com.jh.user.util.ValueComparator;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
@EnableScheduling
public class ChannelService {

	private static final Logger LOG = LoggerFactory.getLogger(ChannelService.class);

	@Autowired
	private ChannelRateBusiness channelRateBusiness;

	@Autowired
	private ChannelRepository channelRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;

	@Autowired
	private BrandManageBusiness brandMangeBusiness;

	@Autowired
	private UserBankInfoBusiness userBankInfoBusiness;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	Util util;

	/** 创建一个渠道 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/add")
	public @ResponseBody Object addChannel(HttpServletRequest request,
			@RequestParam(value = "channel_tag") String channelTag,
			@RequestParam(value = "channel_no", defaultValue = "", required = false) String channelNo,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "sub_channel_tag", defaultValue = "", required = false) String subChannelTag,
			@RequestParam(value = "sub_name", defaultValue = "", required = false) String subName,
			@RequestParam(value = "single_min_limit", defaultValue = "", required = false) String singleMinLimit,
			@RequestParam(value = "single_max_limit", defaultValue = "", required = false) String singleMaxLimit,
			@RequestParam(value = "every_day_max_limit", defaultValue = "", required = false) String everyDayMaxLimit,
			@RequestParam(value = "start_time", defaultValue = "", required = false) String startTime,
			@RequestParam(value = "end_time", defaultValue = "", required = false) String endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		Channel channel = channelRateBusiness.findChannelByTag(subChannelTag);
		if (channel == null) {

			channel = new Channel();

		}

		channel.setChannelNo(channelNo);
		channel.setChannelTag(channelTag);
		channel.setCreateTime(new Date());
		channel.setName(name);
		channel.setSubName(subName);
		channel.setSubChannelTag(subChannelTag);
		if (endTime != null && !endTime.equalsIgnoreCase("")) {
			try {
				DateUtil.getHHMMSSDateFormat(endTime);
			} catch (Exception e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加通道失败,通道结束使用时间不能为:" + endTime + ",格式应为00:00:00");
				return map;
			}
		}
		channel.setEndTime(endTime);
		if (startTime != null && !startTime.equalsIgnoreCase("")) {
			try {
				DateUtil.getHHMMSSDateFormat(startTime);

			} catch (Exception e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加通道失败,通道开始使用时间不能为:" + startTime + ",格式应为00:00:00");
				return map;
			}
		}
		channel.setStartTime(startTime);
		if (everyDayMaxLimit != null && !everyDayMaxLimit.equalsIgnoreCase("")) {
			channel.setEveryDayMaxLimit(new BigDecimal(everyDayMaxLimit));
		}

		if (singleMaxLimit != null && !singleMaxLimit.equalsIgnoreCase("")) {
			channel.setSingleMaxLimit(new BigDecimal(singleMaxLimit));
		}

		if (singleMinLimit != null && !singleMinLimit.equalsIgnoreCase("")) {
			channel.setSingleMinLimit(new BigDecimal(singleMinLimit));
		}

		channel = channelRateBusiness.mergeChannel(channel);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channel);
		return map;
	}

	/** 修改渠道信息 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/update")
	public @ResponseBody Object addChannel(HttpServletRequest request,
			/** 渠道id */
			@RequestParam(value = "channelid") long channelid,
			/** 渠道商家标号 0:反扫；1正扫；2快捷；3公众号；4：隐藏 */
			@RequestParam(value = "channel_no", required = false) String channelNo,
			/** 渠道名字 */
			@RequestParam(value = "name") String name,
			/** 渠道类别如：微信；支付宝；快捷；公众号 */
			@RequestParam(value = "sub_name", required = false) String subName,
			/** 是否到余额 0：到余额；1到卡 */
			@RequestParam(value = "auto_clearing", defaultValue = "0", required = false) String autoclearing,
			/** 通道备注 */
			@RequestParam(value = "remarks", required = false) String remarks,
			/** 通道备注 */
			@RequestParam(value = "extended_field", required = false) String extendedField,
			/** 单次最低限额 */
			@RequestParam(value = "single_min_limit", required = false) String singleMinLimit,
			/** 单次最高额度 */
			@RequestParam(value = "single_max_limit", required = false) String singleMaxLimit,
			/** 单日最高额度 */
			@RequestParam(value = "every_day_max_limit", required = false) String everyDayMaxLimit,
			/** 通道开始可以使用的时间 */
			@RequestParam(value = "start_time", required = false) String startTime,
			/** 状态 */
			@RequestParam(value = "status", required = false) String status,
			/** 通道结算的时间 **/
			@RequestParam(value = "end_time", required = false) String endTime,
			/** 成本提现费 */
			@RequestParam(value = "min_with_draw_fee", defaultValue = "1.00", required = false) BigDecimal minwithdrawFee,
			/** 成本额外费用 */
			@RequestParam(value = "min_extra_fee", defaultValue = "0.00", required = false) BigDecimal minextrafee) {
		Map<String, Object> map = new HashMap<String, Object>();
		Channel channel = channelRateBusiness.findChannelById(channelid);
		if (channel == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
			map.put(CommonConstants.RESP_MESSAGE, "通道不存在");
			map.put(CommonConstants.RESULT, channel);
			return map;
		}
		channel.setChannelNo(channelNo != null && !channelNo.equals("") ? channelNo : channel.getChannelNo());

		channel.setCreateTime(new Date());

		channel.setAutoclearing(
				autoclearing != null && !autoclearing.equals("") ? autoclearing : channel.getAutoclearing());
		channel.setStatus(status != null && !status.equals("") ? status : channel.getStatus());
		channel.setRemarks(remarks != null && !remarks.equals("") ? remarks : channel.getRemarks());
		channel.setExtendedField(
				extendedField != null && !extendedField.equals("") ? extendedField : channel.getExtendedField());
		channel.setName(name != null && !name.equals("") ? name : channel.getName());
		channel.setSubName(subName != null && !subName.equals("") ? subName : channel.getSubName());
		if (endTime != null && !endTime.equalsIgnoreCase("")) {
			try {
				DateUtil.getHHMMSSDateFormat(endTime);
			} catch (Exception e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加通道失败,通道结束使用时间不能为:" + endTime + ",格式应为00:00:00");
				return map;
			}
			channel.setEndTime(endTime);
		}

		if (startTime != null && !startTime.equalsIgnoreCase("")) {
			try {
				DateUtil.getHHMMSSDateFormat(startTime);
			} catch (Exception e) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加通道失败,通道开始使用时间不能为:" + startTime + ",格式应为00:00:00");
				return map;
			}
			channel.setStartTime(startTime);
		}

		if (everyDayMaxLimit != null && !everyDayMaxLimit.equalsIgnoreCase("")) {
			channel.setEveryDayMaxLimit(new BigDecimal(everyDayMaxLimit));
		} else {
			channel.setEveryDayMaxLimit(channel.getEveryDayMaxLimit());
		}

		if (singleMaxLimit != null && !singleMaxLimit.equalsIgnoreCase("")) {
			channel.setSingleMaxLimit(new BigDecimal(singleMaxLimit));
		} else {
			channel.setSingleMaxLimit(channel.getSingleMaxLimit());
		}

		if (singleMinLimit != null && !singleMinLimit.equalsIgnoreCase("")) {
			channel.setSingleMinLimit(new BigDecimal(singleMinLimit));
		} else {
			channel.setSingleMinLimit(channel.getSingleMinLimit());
		}

		channel = channelRateBusiness.mergeChannel(channel);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channel);
		return map;
	}

	/** 生成一个渠道费率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/rate/add")
	public @ResponseBody Object addChannelRate(HttpServletRequest request,
			@RequestParam(value = "user_id", defaultValue = "", required = false) String userId,
			@RequestParam(value = "brand_id") long brandId, @RequestParam(value = "channel_id") long channelId,
			@RequestParam(value = "rate") BigDecimal rate,
			@RequestParam(value = "extra_fee", required = false) BigDecimal extraFee,
			@RequestParam(value = "with_draw_fee", defaultValue = "2", required = false) BigDecimal withdrawFee) {

		BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandId, channelId);
		if (brandRate != null) {
			if (rate.doubleValue() < brandRate.getMinrate().doubleValue()) {
				Map map = new HashMap();
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率小于该通道的最低费率" + brandRate.getMinrate());
				return map;
			}
			if (rate.doubleValue() > brandRate.getRate().doubleValue()) {
				Map map = new HashMap();
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率大于该通道的普通会员的费率" + brandRate.getRate());
				return map;
			}

		} else {
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的贴牌未设置初始费率");
			return map;

		}

		ChannelRate channelRate = null;
		if (userId != null && !userId.equalsIgnoreCase("")) {
			/*
			 * User user=userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));
			 * ChannelRate prechannelRate =
			 * channelRateBusiness.findChannelRateByUserid(user.getPreUserId(), channelId);
			 * if(prechannelRate!=null){
			 * if(prechannelRate.getRate().doubleValue()<rate.doubleValue()){ Map map = new
			 * HashMap(); map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			 * map.put(CommonConstants.RESP_MESSAGE,
			 * "添加失败,该费率已经小于他的上级费率"+prechannelRate.getRate()); return map; } }else{
			 * if(brandRate.getRate().doubleValue()<rate.doubleValue()){ Map map = new
			 * HashMap(); map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			 * map.put(CommonConstants.RESP_MESSAGE,
			 * "添加失败,该费率已经小于他的上级费率"+prechannelRate.getRate()); return map; } }
			 */
			channelRate = channelRateBusiness.findChannelRateByUserid(Long.parseLong(userId), channelId);
		}
		if (channelRate == null) {

			channelRate = new ChannelRate();

		}

		channelRate.setChannelId(channelId);
		channelRate.setCreateTime(new Date());
		channelRate.setExtraFee(extraFee);
		channelRate.setRate(rate);
		channelRate.setWithdrawFee(withdrawFee);
		channelRate.setBrandId(brandId);
		if (userId != null && !userId.equalsIgnoreCase("")) {
			channelRate.setUserId(Long.parseLong(userId));
		}

		channelRate = channelRateBusiness.mergeChannelRate(channelRate);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channelRate);
		return map;

	}

	/** 生成一个渠道费率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/grade/rate/add")
	public @ResponseBody Object addChannelGradeRate(HttpServletRequest request,
			@RequestParam(value = "user_id", defaultValue = "", required = false) String userId,
			@RequestParam(value = "brand_id") long brandId, @RequestParam(value = "channel_id") long channelId,
			@RequestParam(value = "rate") BigDecimal rate,
			@RequestParam(value = "extra_fee", required = false) BigDecimal extraFee,
			@RequestParam(value = "with_draw_fee", defaultValue = "2", required = false) BigDecimal withdrawFee) {
		Map<String, Object> map = new HashMap<String, Object>();
		BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandId, channelId);
		if (brandRate != null) {
			if (rate.doubleValue() < brandRate.getMinrate().doubleValue()) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率小于该通道的最低费率" + brandRate.getMinrate());
				return map;
			}
			if (rate.doubleValue() > brandRate.getRate().doubleValue()) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率大于该通道的普通会员的费率" + brandRate.getRate());
				return map;
			}

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的贴牌未设置初始费率");
			return map;

		}

		ChannelRate channelRate = null;
		if (userId != null && !userId.equalsIgnoreCase("")) {
			channelRate = channelRateBusiness.findChannelRateByUserid(Long.parseLong(userId), channelId);
		}
		if (channelRate == null) {

			channelRate = new ChannelRate();

		}

		channelRate.setChannelId(channelId);
		channelRate.setCreateTime(new Date());
		channelRate.setExtraFee(brandRate.getExtraFee());
		channelRate.setRate(rate);
		channelRate.setWithdrawFee(brandRate.getWithdrawFee());
		channelRate.setBrandId(brandId);
		if (userId != null && !userId.equalsIgnoreCase("")) {
			channelRate.setUserId(Long.parseLong(userId));
		}

		channelRate = channelRateBusiness.mergeChannelRate(channelRate);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channelRate);
		return map;

	}

	/** 根据渠道标识 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/query")
	public @ResponseBody Object queryChannelByTag(HttpServletRequest request,
			@RequestParam(value = "channel_tag") String channelTag) {
		Map<String, Object> map = new HashMap<String, Object>();

		Channel channel = channelRateBusiness.findChannelByTag(channelTag);

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channel);
		return map;

	}

	/** 根据渠道标识 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/query/all")
	public @ResponseBody Object queryAllChannel(HttpServletRequest request) {
		List<Channel> channels = channelRateBusiness.findAllChannel();
		List<Channel> channelss = new ArrayList<>();
		for (Channel channel : channels) {
			if (!"0".equals(channel.getStatus())) {
				channelss.add(channel);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channelss);
		return map;

	}

	/** 传递平台id将所有平台信息和费率都取回来 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/query/all/brandid")
	public @ResponseBody Object queryAllChannelByBrandId(HttpServletRequest request,
			@RequestParam(value = "user_id") String suserId,

			@RequestParam(value = "access_type", defaultValue = "1", required = false) String saccesstype) {
		Map<String, Object> map = new HashMap<String, Object>();
		long userId;
		try {
			if ("".equals(suserId.trim())) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数userId不能为'" + suserId + "',请检查后重试!");
				return map;
			}
			userId = Long.valueOf(suserId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数userId不能为'" + suserId + "',请检查后重试!");
			return map;
		}
		long accesstype;
		try {
			if ("".equals(saccesstype)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数accesstype不能为'" + saccesstype + "',请检查后重试!");
				return map;
			}
			accesstype = Long.valueOf(saccesstype);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数accesstype不能为'" + saccesstype + "',请检查后重试!");
			return map;
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);

		LOG.info("用户编号=" + userId);
		List<Channel> channels;
		try {
			channels = channelRateBusiness.findAllChannelByBrandid(user.getBrandId());
			LOG.info("用户数据:" + user);
		} catch (Exception e) {
			LOG.info("用户数据:" + user);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,请检查后重试!");
			return map;
		}

		List<Channel> result = new ArrayList<Channel>();
		for (Channel channel : channels) {
			Channel temp = channel;
			ChannelRate channelRate = channelRateBusiness.findChannelRateByUserid(userId, channel.getId());
			BrandRate brandRate = null;
			if (channelRate == null) {
				brandRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), channel.getId());
				temp.setRate(brandRate.getRate());
				temp.setWithdrawFee(brandRate.getWithdrawFee());
				if (temp.getId() != 82 && temp.getId() != 90)
					temp.setExtraFee(
							brandRate.getExtraFee().add(brandRate.getWithdrawFee()).setScale(2, BigDecimal.ROUND_DOWN));
				temp.setStartTime(temp.getStartTime().substring(0, temp.getStartTime().lastIndexOf(":")));
				temp.setEndTime(temp.getEndTime().substring(0, temp.getEndTime().lastIndexOf(":")));
			} else {
				temp.setRate(channelRate.getRate());
				if (temp.getId() != 82 && temp.getId() != 90)
					temp.setExtraFee(
							channel.getExtraFee().add(channel.getWithdrawFee()).setScale(2, BigDecimal.ROUND_DOWN));
				temp.setStartTime(temp.getStartTime().substring(0, temp.getStartTime().lastIndexOf(":")));
				temp.setEndTime(temp.getEndTime().substring(0, temp.getEndTime().lastIndexOf(":")));
			}
			result.add(temp);
		}

		if (accesstype == 1) {
			List<Channel> channels2 = result;
			result = new ArrayList<Channel>();
			for (Channel channel : channels2) {
				if (!channel.getStatus().equals("1")) {
					channel.setChannelNo("4");
					result.add(channel);
				} else {
					result.add(channel);
				}
			}
		}
		/*
		 * Collections.sort(result,new Comparator<Channel>(){ public int compare(Channel
		 * arg0, Channel arg1) { return
		 * arg1.getChannelNo().compareTo(arg0.getChannelNo()); } });
		 */

		result.sort(new Comparator<Channel>() {

			@Override
			public int compare(Channel o1, Channel o2) {

				return Integer.valueOf(o1.getSort()) - Integer.valueOf(o2.getSort());
			}

		});

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, result);
		return map;

	}

	/** 后台传递平台id将所有平台信息和费率都取回来 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/query/all/brandid/pc")
	public @ResponseBody Object queryAllChannelByBrandIdPC(HttpServletRequest request,
			@RequestParam(value = "user_id") String suserId,

			@RequestParam(value = "access_type", defaultValue = "1", required = false) String saccesstype) {
		Map<String, Object> map = new HashMap<String, Object>();
		long userId;
		try {
			if ("".equals(suserId)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数userId不能为'" + suserId + "',请检查后重试!");
				return map;
			}
			userId = Long.valueOf(suserId);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数userId不能为'" + suserId + "',请检查后重试!");
			return map;
		}
		long accesstype;
		try {
			if ("".equals(saccesstype)) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数accesstype不能为'" + saccesstype + "',请检查后重试!");
				return map;
			}
			accesstype = Long.valueOf(saccesstype);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败,参数accesstype不能为'" + saccesstype + "',请检查后重试!");
			return map;
		}

		User user = userLoginRegisterBusiness.queryUserById(userId);

		List<Channel> channels = channelRateBusiness.findAllChannelByBrandid(user.getBrandId());

		List<Channel> result = new ArrayList<Channel>();
		for (Channel channel : channels) {
			Channel temp = channel;
			ChannelRate channelRate = channelRateBusiness.findChannelRateByUserid(userId, channel.getId());
			BrandRate brandRate = null;
			if (channelRate == null) {
				brandRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), channel.getId());
				temp.setRate(brandRate.getRate());
				temp.setWithdrawFee(brandRate.getWithdrawFee());
				if (temp.getId() != 82 && temp.getId() != 90)
					temp.setExtraFee(brandRate.getExtraFee().setScale(2, BigDecimal.ROUND_DOWN));
				temp.setStartTime(temp.getStartTime().substring(0, temp.getStartTime().lastIndexOf(":")));
				temp.setEndTime(temp.getEndTime().substring(0, temp.getEndTime().lastIndexOf(":")));
			} else {
				temp.setRate(channelRate.getRate());
				if (temp.getId() != 82 && temp.getId() != 90)
					temp.setExtraFee(channel.getExtraFee().setScale(2, BigDecimal.ROUND_DOWN));
				temp.setStartTime(temp.getStartTime().substring(0, temp.getStartTime().lastIndexOf(":")));
				temp.setEndTime(temp.getEndTime().substring(0, temp.getEndTime().lastIndexOf(":")));
			}
			result.add(temp);
		}

		if (accesstype == 1) {
			List<Channel> channels2 = result;
			result = new ArrayList<Channel>();
			for (Channel channel : channels2) {
				if (!channel.getStatus().equals("1")) {
					channel.setChannelNo("4");
					result.add(channel);
				} else {
					result.add(channel);
				}
			}
		}
		Collections.sort(result, new Comparator<Channel>() {
			public int compare(Channel arg0, Channel arg1) {
				return arg1.getChannelNo().compareTo(arg0.getChannelNo());
			}
		});
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, result);
		return map;

	}

	/** 获取渠道费率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/rate/query/brandid")
	public @ResponseBody Object queryChannelRateByBrandid(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandId, @RequestParam(value = "channel_id") long channelId) {

		List<ChannelRate> channelRate = channelRateBusiness.findChannelRateByBrandid(brandId, channelId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channelRate);
		return map;

	}

	/** 修改用户的渠道费率 **/
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/rate/update/userid")
	public @ResponseBody Object queryChannelRateByuserid(HttpServletRequest request,
			@RequestParam(value = "user_id") long userId, @RequestParam(value = "channel_id") long channelId,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extra_fee", defaultValue = "0", required = false) String extraFee,
			@RequestParam(value = "withdraw_fee", defaultValue = "2", required = false) String withdrawFee) {
		User user = userLoginRegisterBusiness.queryUserById(userId);
		BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), channelId);
		if (brandRate != null) {
			if (Double.parseDouble(rate) < brandRate.getMinrate().doubleValue()) {
				Map map = new HashMap();
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率小于改通道的最低费率" + brandRate.getMinrate());
				return map;
			}
			if (Double.parseDouble(rate) > brandRate.getRate().doubleValue()) {
				Map map = new HashMap();
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的设置的费率大于改通道的普通会员的费率" + brandRate.getRate());
				return map;
			}

		} else {
			Map map = new HashMap();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加失败,您的贴牌未设置初始费率");
			return map;

		}

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");

		ChannelRate channelRate = channelRateBusiness.findChannelRateByUserid(userId, channelId);

		if (channelRate == null) {
			channelRate = new ChannelRate();
			channelRate.setUserId(userId);
			channelRate.setBrandId(user.getBrandId());
			channelRate.setChannelId(channelId);

		}

		channelRate.setCreateTime(new Date());
		channelRate.setRate(new BigDecimal(rate));

		if (!extraFee.equalsIgnoreCase("")) {
			channelRate.setExtraFee(new BigDecimal(extraFee));
		}

		if (!withdrawFee.equalsIgnoreCase("")) {
			channelRate.setWithdrawFee(new BigDecimal(withdrawFee));
		}

		map.put(CommonConstants.RESULT, channelRateBusiness.mergeChannelRate(channelRate));
		return map;
	}

	/** 获取所有渠道费率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/rate/query/all")
	public @ResponseBody Object queryAllChannelRatesChannelRates(HttpServletRequest request) {

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, channelRateBusiness.findChannelRates());
		return map;
	}

	/** 获取渠道费率 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/rate/query/userid")
	public @ResponseBody Object queryChannelRateByuserid(HttpServletRequest request,
			@RequestParam(value = "user_id") long userId, @RequestParam(value = "channel_id") long channelId) {
		Map<String, Object> map = new HashMap<String, Object>();
		User user = null;
		try {
			user = userLoginRegisterBusiness.queryUserById(userId);
		} catch (Exception e) {
			LOG.error("获取用户异常(可能原因:userId无效)==========");
			e.printStackTrace();
			ExceptionUtil.errInfo(e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交易繁忙,请稍后重试");
			return map;
		}

		if (user == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无用户数据");
			return map;
		}

		try {
			ChannelRate channelRate = null;
			BrandRate brandRate = null;
			channelRate = channelRateBusiness.findChannelRateByUserid(userId, channelId);
			brandRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), channelId);
			// LOG.info("userId=:"+userId+"channelId="+channelId);
			if (channelRate == null) {
				if (brandRate == null) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "无该渠道费率!");
					return map;
				}
				map.put(CommonConstants.RESULT, brandRate);
			} else {
				channelRate.setExtraFee(brandRate.getExtraFee());
				channelRate.setWithdrawFee(brandRate.getWithdrawFee());
				map.put(CommonConstants.RESULT, channelRate);
			}
		} catch (Exception e) {
			LOG.error("获取渠道费率异常(可能原因:没有配置channel路由)==========");
			e.printStackTrace();
			ExceptionUtil.errInfo(e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "交易繁忙,请稍后重试");
			return map;
		}
		map.put("user", user);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 根据channelTag获取费率
	 * 
	 * @param request
	 * @param userId
	 * @param channelId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/rate/query/by/channelTag")
	public @ResponseBody Object queryChannelRateByChannelTagAndUserId(HttpServletRequest request,
			@RequestParam(value = "userId") long userId, @RequestParam(value = "channelTag") String channelTag) {
		Channel channel = channelRateBusiness.findChannelByTag(channelTag);
		if (channel == null) {
			return ResultWrap.init(CommonConstants.FALIED, "无该渠道配置");
		}
		long channelId = channel.getId();
		Map<String, Object> map = (Map<String, Object>) this.queryChannelRateByuserid(request, userId, channelId);
		map.put("channel", channel);
		return map;
	}

	/**
	 * 通过品牌Id查询通道
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/query/brandid")
	public @ResponseBody Object findAllChannelByBrandid(HttpServletRequest request,
			@RequestParam(value = "brand_id") long brandid) {

		List<Channel> resources = channelRateBusiness.findAllChannelByBrandid(brandid);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resources);
		return map;
	}

	// /**
	// * 统一设定直属下级分润
	// * */
	// @RequestMapping(method=RequestMethod.POST,value="/v1.0/user/channel/rate/")
	// public @ResponseBody Object findAllChannelByBrandid(HttpServletRequest
	// request,
	// @RequestParam(value = "brand_id") long brandid
	// ){

	// 根据ChannelNo查询通道
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/channelno")
	public @ResponseBody Object findChannelByChannelNo(HttpServletRequest request,
			@RequestParam(value = "channel_no", defaultValue = "3", required = false) String channelNo) {
		List<Channel> findChannelByChannelNo = channelRateBusiness.findChannelByChannelNo(channelNo);
		String channelTags = "";
		if (findChannelByChannelNo != null && findChannelByChannelNo.size() > 0)
			for (Channel channel : findChannelByChannelNo) {
				channelTags += channel.getChannelTag() + ",";
			}

		if (channelTags != null && !channelTags.equals("")) {
			channelTags = channelTags.substring(0, channelTags.length() - 1);
		}

		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, channelTags);
		map.put(CommonConstants.RESP_MESSAGE, "成功");

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/find/by/channelid")
	public @ResponseBody Object getChannelByChannelId(HttpServletRequest request,
			@RequestParam(value = "channelId") String channelId) {
		Map<String, Object> map = new HashMap<>();
		Channel channel = channelRateBusiness.findByChannelId(Long.valueOf(channelId).longValue());
		if (channel == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无该通道信息!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, channel);
		return map;
	}

	// 修改通道的状态,0为满额,1为可用
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/update/paymentstatus")
	public @ResponseBody Object ChangePaymentChannelStatus(HttpServletRequest request,
			@RequestParam(value = "channelTag") String channelTag,
			@RequestParam(value = "paymentStatus", defaultValue = "0", required = false) String paymentStatus) {
		Map map = new HashMap();

		Channel channel = channelRateBusiness.findChannelByTag(channelTag);
		if (channel != null) {
			channel.setPaymentStatus(paymentStatus);
			channelRepository.save(channel);
			em.clear();
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "无此通道或修改失败");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");
		return map;
	}

	@Scheduled(cron = "0 00 01 * * ?")
	public void findPaymentOrder() {

		Map map = new HashMap();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		date = calendar.getTime();

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/finduccess";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("starttime", sdf.format(date));
		requestEntity.add("endtime", sdf.format(new Date()));
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject = null;
		JSONArray resultObj = null;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONArray("result");
		} catch (Exception e) {

		}

		for (int i = 0; i < resultObj.size(); i++) {

			JSONObject jsonObject2 = resultObj.getJSONObject(i);

			long channelId = Long.valueOf(jsonObject2.getString("channelId"));
			long allNumber = Long.valueOf(jsonObject2.getString("allNumber"));
			long successNumber = Long.valueOf(jsonObject2.getString("successNumber"));

			if (allNumber != 0) {

				BigDecimal bigAllNumber = new BigDecimal(allNumber);
				BigDecimal bigSuccessNumber = new BigDecimal(successNumber);

				String divide = bigSuccessNumber.divide(bigAllNumber, 2, BigDecimal.ROUND_UP)
						.multiply(new BigDecimal("100")).toString();

				map.put(channelId, Double.valueOf(divide));

			}

		}

		ValueComparator bvc = new ValueComparator(map);
		TreeMap<Long, Object> treeMap = new TreeMap<Long, Object>(bvc);
		treeMap.putAll(map);

		Set<Long> keySet = treeMap.keySet();

		Iterator<Long> it = keySet.iterator();

		int i = 0;
		while (it.hasNext()) {
			Long next = it.next();

			Channel channel = null;
			try {
				channel = channelRateBusiness.findByChannelId(next);
				channel.setSort(++i + "");

				channelRateBusiness.mergeChannel(channel);
			} catch (Exception e) {
				continue;
			}

		}

	}

	// 根据银行卡和金额获取支持的通道
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/getchannel/bybankcard/andamount")
	public @ResponseBody Object getChannelByBankCardAndAmount(HttpServletRequest request,
			@RequestParam(value = "userId") long userId, 
			@RequestParam(value = "brandId") long brandId,
			@RequestParam(value = "bankCard") String bankCard, 
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "recommend", required = false, defaultValue = "2") String recommend,
			@RequestParam(value = "channelNo", required = false, defaultValue = "2") String channelNo,
			@RequestParam(value = "autoClearing", required = false, defaultValue = "1") String autoClearing,
			@RequestParam(value = "status", required = false, defaultValue = "1") String status) throws Exception {
		// 根据userId查询用户的信息
		User user = userLoginRegisterBusiness.queryUserById(userId);
		// 根据信用卡卡号和userId查询用户的绑卡信息
		List<UserBankInfo> queryUserBankInfoByCardno = userBankInfoBusiness
				.queryUserBankInfoByCardnoAndTypeAndUserId(bankCard, "0", userId);
		UserBankInfo userBankInfo = null;
		if (queryUserBankInfoByCardno != null && queryUserBankInfoByCardno.size() > 0) {
			userBankInfo = queryUserBankInfoByCardno.get(0);
		}
		String creditIdCard = userBankInfo.getIdcard();
		String creditCardType = userBankInfo.getCardType();
		String creditNature = userBankInfo.getNature();

		if (creditCardType.contains("借贷")) {

			return ResultWrap.init(CommonConstants.FALIED, "抱歉,快捷收款通道暂不支持借贷合一卡,请重新选择其他信用卡继续交易!");
		}

		if (creditNature.contains("借记")) {

			return ResultWrap.init(CommonConstants.FALIED, "抱歉,快捷收款通道无法使用借记卡进行交易,请重新选择信用卡继续交易!");
		}
		// 根据userId查询用户的默认提现到账卡信息
		UserBankInfo queryDefUserBankInfoByUserid = null;
		try {
			queryDefUserBankInfoByUserid = userBankInfoBusiness.queryDefUserBankInfoByUserid(userId);
		} catch (Exception e) {
			LOG.error("查询用户默认提现卡出现异常===========", e);
			return ResultWrap.init(CommonConstants.FALIED, "系统检测到您绑定的默认结算卡(储蓄卡)有异常,请及时联系客服人员!");
		}

		if (queryDefUserBankInfoByUserid == null) {

			return ResultWrap.init(CommonConstants.FALIED, "您在系统内没有绑定默认结算卡(储蓄卡),请绑定默认结算卡(储蓄卡)继续交易!");
		}

		String debitIdCard = queryDefUserBankInfoByUserid.getIdcard();

		if (!creditIdCard.equalsIgnoreCase(debitIdCard)) {
			LOG.error("出款卡和到账卡身份证号码不一致======");
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,您在系统内的充值卡(信用卡)和默认结算卡(储蓄卡)的身份证号码不一致,无法继续交易!");
		}

		String url = "http://risk/v1.0/risk/blackwhite/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", user.getPhone());
		requestEntity.add("operation_type", "1");// 0 表示登陆无法进行 1表示无法充值 2 表示无法提现 // 3 无法支付
		JSONObject jsonObject;
		String rescode = "";
		String result;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/risk/blackwhite/query/phone--RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			rescode = jsonObject.getString("resp_code");
		} catch (Exception e) {
			LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);

			return ResultWrap.init(CommonConstants.FALIED, "查询用户黑白名单出现异常,请稍后重试!");
		}
		if (rescode != null) {
			if (!CommonConstants.SUCCESS.equalsIgnoreCase(rescode)) {

				return ResultWrap.init(CommonConstants.ERROR_USER_BLACK, "用户在黑名单中");
			}
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "查询用户黑白名单出现异常,请稍后重试!");
		}

		List<String> list = new ArrayList<String>();
		BigDecimal bigAmount = new BigDecimal(amount);

		List<Channel> channels = channelRateBusiness.findChannelByChannelNoAndStatusAndPaymentStatus(channelNo,autoClearing, status, "1");
		int j = 0;
		int k = 0;
		BigDecimal tempMin = BigDecimal.ZERO;
		BigDecimal tempMax = BigDecimal.ZERO;
		int q = 0;
		if (channels != null && channels.size() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			Date now = sdf.parse(sdf.format(new Date()));
			for (Channel c : channels) {
				q++;
				BigDecimal singleMinLimit = c.getSingleMinLimit();
				BigDecimal singleMaxLimit = c.getSingleMaxLimit();

				Date beginTime = null;
				Date endTime = null;
				try {
					beginTime = sdf.parse(c.getStartTime());
					endTime = sdf.parse(c.getEndTime());
				} catch (ParseException e) {
					LOG.error("转换时间格式有误======", e);
					return ResultWrap.init(CommonConstants.FALIED, "交易时间转换异常,请稍后重试!");
				}
				if (singleMinLimit.compareTo(bigAmount) <= 0 && singleMaxLimit.compareTo(bigAmount) >= 0) {
					// 判断当前时间是否在交易时间段内
					Boolean flag = belongCalendar(now, beginTime, endTime);
					if (flag) {
						BrandRate findRateByBrandAndChannel = brandMangeBusiness.findRateByBrandAndChannel(brandId,
								c.getId());
						if (findRateByBrandAndChannel != null) {
							if ("1".equals(findRateByBrandAndChannel.getStatus())) {

								list.add(c.getChannelTag());
							}else {
								k++;
							}
						}else {
							k++;
						}
					} else {
						k++;
					}
				} else {
					if(q == 1) {
						tempMin = singleMinLimit;
						tempMax = singleMaxLimit;
					}else{
						if(tempMin.compareTo(singleMinLimit) > 0) {
							tempMin = singleMinLimit;
						}
						if(tempMax.compareTo(singleMaxLimit) < 0) {
							tempMax = singleMaxLimit;
						}
					}
					k++;
					j++;
				}
			}

			if (j == channels.size() && list.size() <= 0) {

				return ResultWrap.init(CommonConstants.FALIED, "快捷收款通道单笔交易限额为: " + tempMin.toString().substring(0,tempMin.toString().indexOf(".")+3) + "~" + tempMax.toString().substring(0,tempMax.toString().indexOf(".")+3) + " 元,请重新输入金额继续交易!");
			}
			if (k == channels.size() && list.size() <= 0) {

				return ResultWrap.init(CommonConstants.FALIED, "该时间段内不支持快捷收款交易,敬请留意快捷收款的交易时间!");
			}

			LOG.info("list的长度======" + list.size());
			LOG.info("list======" + list);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "系统暂无快捷通道,请及时联系客服!");
		}

		url = "http://paymentchannel/v1.0/paymentchannel/querysupportbank/byname";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("bankName", Util.queryBankNameByBranchName(userBankInfo.getBankName()));
		requestEntity.add("type", "信用卡");
		requestEntity.add("channelTag", list.toString().substring(1, list.toString().length() - 1));
		JSONArray jsonArray = null;
		String respCode = null;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString("resp_code");
		} catch (Exception e) {

			return ResultWrap.init(CommonConstants.FALIED, "获取通道支持银行出现异常,请稍后重试!");
		}

		if (CommonConstants.SUCCESS.equals(respCode)) {
			jsonArray = jsonObject.getJSONArray("result");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, jsonObject.getString("resp_message"));
		}

		List<Channel> results = new ArrayList<Channel>();
		List<String> list1 = new ArrayList<String>();

		if (jsonArray != null && jsonArray.size() > 0) {

			LOG.info("jsonArray======" + jsonArray);
			LOG.info("jsonArray.size()======" + jsonArray.size());
			for (int i = 0; i < jsonArray.size(); i++) {

				JSONObject json = (JSONObject) jsonArray.get(i);
				String singleMaxLimit = json.getString("singleMaxLimit");
				String singleMinLimit = json.getString("singleMinLimit");

				if (new BigDecimal(singleMaxLimit).compareTo(bigAmount) >= 0 && new BigDecimal(singleMinLimit).compareTo(bigAmount) <= 0) {

					String channelTag = json.getString("channelTag");

					if("准贷记卡".equals(creditNature)) {
						if("JF_QUICK".equals(channelTag) || "JFX_QUICK1".equals(channelTag) || "JFT_QUICK2".equals(channelTag) || "TYT_QUICK1".equals(channelTag) || "HZN_QUICK".equals(channelTag) || "JFS_QUICK".equals(channelTag)) {
							continue;
						}else {
							list1.add(channelTag);
						}
					}else {
						
						list1.add(channelTag);
					}
				}
			}

			LOG.info("list1======" + list1);
			LOG.info("list1.size()======" + list1.size());
			if (list1 != null && list1.size() > 0) {

				for (int i = 0; i < list1.size(); i++) {
					String channelTag = list1.get(i);
					Channel channel = channelRateBusiness.findChannelByTag(channelTag);
					Channel temp = channel;
					ChannelRate channelRate = channelRateBusiness.findChannelRateByUserid(userId, channel.getId());
					if(channelRate == null){
						BrandRate brandRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), channel.getId());		
						if(brandRate != null) {
							temp.setRate(brandRate.getRate());
							temp.setWithdrawFee(brandRate.getWithdrawFee());
							if(temp.getId()!=82&&temp.getId()!=90)
							temp.setExtraFee(brandRate.getExtraFee().add(brandRate.getWithdrawFee()).setScale(2, BigDecimal.ROUND_DOWN));
							temp.setStartTime(temp.getStartTime().substring(0,temp.getStartTime().lastIndexOf(":")));
							temp.setEndTime(temp.getEndTime().substring(0,temp.getEndTime().lastIndexOf(":")));
						}else {
							
							return ResultWrap.init(CommonConstants.FALIED, "您所在的贴牌暂未配置费率,请联系客服!");
						}
					}else{
						temp.setRate(channelRate.getRate());
						if(temp.getId()!=82&&temp.getId()!=90)
						temp.setExtraFee(channel.getExtraFee().add(channel.getWithdrawFee()).setScale(2, BigDecimal.ROUND_DOWN));
						temp.setStartTime(temp.getStartTime().substring(0,temp.getStartTime().lastIndexOf(":")));
						temp.setEndTime(temp.getEndTime().substring(0,temp.getEndTime().lastIndexOf(":")));
					}
					
					results.add(temp);
				}
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该信用卡或交易金额支持的快捷通道,请更换信用卡或重新输入交易金额!");
			}
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无该信用卡或交易金额支持的快捷通道,请更换信用卡或重新输入交易金额!");
		}

		results.sort(new Comparator<Channel>() {

			@Override
			public int compare(Channel o1, Channel o2) {

				return Integer.valueOf(o1.getSort()) - Integer.valueOf(o2.getSort());
			}

		});

		if ("2".equals(recommend)) {
			LOG.info("userId为：" + userId + " & 根据银行卡和金额筛选返回所有支持的快捷通道=======");

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", results);
		} else {

			//User queryUserById = userLoginRegisterBusiness.queryUserById(userId);

			List<Channel> result1 = new ArrayList<Channel>();
			Channel channel = results.get(0);
			result1.add(channel);
			
			LOG.info("userId为：" + userId + " & 根据银行卡和金额自动匹配返回一条默认的快捷通道=======" + channel);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", result1);
			
			/*String channelTag = channel.getChannelTag();

			LOG.info("channelTag=======" + channelTag);

			url = "http://facade/v1.0/facade/topup/new";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", queryUserById.getPhone());
			requestEntity.add("userId", userId + "");
			requestEntity.add("amount", amount);
			requestEntity.add("channeTag", channelTag);
			requestEntity.add("orderDesc", channel.getName());
			requestEntity.add("brandId", brandId + "");
			requestEntity.add("bankCard", bankCard);
			requestEntity.add("creditBankName", creditBankName);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				respCode = jsonObject.getString("resp_code");

				if ("000000".equals(respCode)) {

					return ResultWrap.init(CommonConstants.SUCCESS, "请求成功!", jsonObject.getString("result"));
				} else {

					return ResultWrap.init(CommonConstants.FALIED, jsonObject.getString("resp_message"));
				}

			} catch (Exception e) {

				return ResultWrap.init(CommonConstants.FALIED, "请求支付异常,请稍后重试!");
			}*/

		}

	}

	// 查询用户在通道的单日成功交易的总额
	public BigDecimal getSumTransactionAmount(long userId, String channelTag) {

		String url = "http://transactionclear/v1.0/transactionclear/payment/querytransaction/sumamount";
		/** 根据的用户手机号码查询用户的基本信息 */
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId + "");
		requestEntity.add("orderStatus", "1");
		requestEntity.add("channelTag", channelTag);
		restTemplate = new RestTemplate();
		LOG.info("接口/v1.0/transactionclear/payment/querytransaction/sumamount--参数================"
				+ requestEntity.toString());
		long everyDayTransaction;
		try {
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/transactionclear/payment/querytransaction/sumamount--RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			String respCode = jsonObject.getString("resp_code");

			if ("000000".equals(respCode)) {
				everyDayTransaction = jsonObject.getLong("result");
			} else {
				everyDayTransaction = 100;
			}

			BigDecimal bigDecimal = new BigDecimal(everyDayTransaction);

			return bigDecimal;
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/querytransaction/sumamount===========" + e);

			return BigDecimal.ZERO;
		}

	}

	// 判断是否在通道交易时间段内
	public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
		Calendar date = Calendar.getInstance();
		date.setTime(nowTime);

		Calendar begin = Calendar.getInstance();
		begin.setTime(beginTime);

		Calendar end = Calendar.getInstance();
		end.setTime(endTime);

		if (date.after(begin) && date.before(end)) {
			return true;
		} else {
			return false;
		}
	}

	// 判断用户的到账卡是否支持
	public Map getChannelDebitBankCard(String channelTag, String bankName) {

		RestTemplate restTemplate = new RestTemplate();
		// String url =
		// "http://101.132.255.217/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname";
		String url = "http://localhost/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("channelTag", channelTag);
		requestEntity.add("bankName", util.changeBankName(bankName));
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info(
				"/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname  RESULT================"
						+ result);
		try {
			JSONObject jsonObject = JSONObject.fromObject(result);

			if (jsonObject.getString("resp_code").equals("888888")) {

				String string = jsonObject.getString("result");

				string = string.substring(1, string.length() - 1).replace("\"", "").replace(",", "、");

				LOG.info("string======" + string);

				return ResultWrap.init(CommonConstants.FALIED,
						"该通道目前支持的到账卡银行: [" + string + "], 请及时更换默认到账卡为以上银行后重新发起交易!");
			} else if (jsonObject.getString("resp_code").equals("000000")) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!");
			} else {

				return ResultWrap.init(CommonConstants.FALIED, jsonObject.getString("resp_message"));
			}
		} catch (Exception e) {
			LOG.error("查询通道支持默认结算卡出错======", e);

			return ResultWrap.init(CommonConstants.FALIED, "查询通道支持默认结算卡异常,请稍后重试!");
		}

	}

	// lx 写好了 强制修改rate和minRate 为costRate
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/updateCostRate")
	public @ResponseBody Object updateCostRate(HttpServletRequest request,
			@RequestParam(value = "channel_id") long channelId) {
		Map map = new HashMap();
		Channel channel = null;

		List<ChannelRate> channelRateList = new ArrayList<ChannelRate>();
		List<BrandRate> brandRateList = new ArrayList<BrandRate>();
		// 先去查询出costRate 就是最低的那个
		channel = channelRateBusiness.findChannelById(channelId);
		String costRateStr = null;
		if (channel != null) {
			costRateStr = channel.getCostRate();
		}
		BigDecimal costRate = new BigDecimal(costRateStr);
		// 根据条件查询出比costRate小的rate数据
		channelRateList = channelRateBusiness.findChannelRateByChannelId(channelId, costRate);
		if (channelRateList != null) {
			for (ChannelRate channelRate : channelRateList) {
				channelRate = new ChannelRate();
				channelRate.setChannelId(channelId);
				channelRate.setRate(costRate);

				channelRateBusiness.mergeChannelRate(channelRate);

			}

		}
		// 根据条件查询出比costRate小的minrate数据
		brandRateList = brandMangeBusiness.findMinRateByChannelId(channelId, costRate);

		if (brandRateList != null) {
			for (BrandRate brandRate : brandRateList) {
				brandRate.setChannelId(channelId);
				brandRate.setMinrate(costRate);
				brandMangeBusiness.mergeBrandRate(brandRate);
			}

		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "更新成功!");

		return map;
	}

	/***
	 * lx 一键修改费率 给钟
	 * 
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/channel/updateRateByOnce")
	public @ResponseBody Object add1Channel(HttpServletRequest request,
			@RequestParam(value = "channel_tag") String channelTag, @RequestParam(value = "brand_id") long brandId) {

		Map<String, Object> map = new HashMap<String, Object>();
		// 这个是查询通道底价 高
		Channel channel = null;
		channel = channelRateBusiness.findChannelByTag(channelTag);
		BigDecimal costRate = null;
		long channelId = 0;

		if (channel != null) {

			String costRateStr = channel.getCostRate();
			channelId = channel.getId();
			costRate = new BigDecimal(costRateStr);
		}

		// 查询贴牌商底价 低
		BrandRate brandRate = null;
		brandRate = brandMangeBusiness.findRateByBrandAndChannel(brandId, channelId);
		BigDecimal minRate = null;
		if (brandRate != null) {
			minRate = brandRate.getMinrate();
		}
		// 然后应该将costRate的值传到minRate这张表中

		if (costRate.doubleValue() <= minRate.doubleValue()) {
			brandRate.setMinrate(costRate);
			brandRate = brandMangeBusiness.mergeBrandRate(brandRate);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "修改费率底价完成!");
			map.put(CommonConstants.RESULT, brandRate);
			return map;
		} else {
			brandRate.setMinrate(minRate);
			brandRate = brandMangeBusiness.mergeBrandRate(brandRate);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功!");
			map.put(CommonConstants.RESULT, brandRate);
			return map;
		}
	}

}
