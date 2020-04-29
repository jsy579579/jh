package cn.jh.clearing.service;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.ConsumeRankBusiness;
import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class ConsumeRankService {
	@Autowired
	ConsumeRankBusiness crb;
	@Autowired
	PaymentOrderBusiness pob;
	@Autowired
	Util util;
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/rank/team")
	public @ResponseBody Object QueryConsumeTeam(HttpServletRequest req,
			@RequestParam(value="phone") String phone,
			@RequestParam(value="brandid") String brandid,
			@RequestParam(value="type",required=false) String[] type,
			@RequestParam(value = "strTime") String strTime,
			@RequestParam(value = "endTime") String endTime
			) {
		if(type==null||type.length==0) {
			type = new String[3];
			type[0] = "0";
			type[1] = "2";
			type[2] = "10";
		}
		Map map = new HashMap();
		Date strDate = DateUtil.getDateFromStr(strTime);
		Date endDate = DateUtil.getDateFromStr(endTime);
		List<PaymentOrder> lis = pob.findPaymentOrderByTimeAndPhone(phone, strDate, endDate);
		long userid;
		if(lis.size()==0) {
			map.put("resp_code", "999999");
			map.put("resp_message", "没有交易记录");
			return map;
		}else {
			 userid = lis.get(0).getUserid();
		}
		RestTemplate rt = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/realtion/find/user/all/sons";
		MultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", String.valueOf(userid));
		String result = rt.postForObject(url, requestEntity, String.class);
		JSONObject jsonObj = JSONObject.fromObject(result);
		if(!jsonObj.containsKey("result")) {
			map.put("resp_code", "999999");
			map.put("resp_message", "没有下级用户");
			return map;
		}
		JSONArray list = jsonObj.getJSONArray("result");
		if(list.size()==0) {
			map.put("resp_code", "999999");
			map.put("resp_message", "没有下级用户");
			return map;
		}
		String[] userids = new String[list.size()+1];
		for(int i=0;i<list.size();i++) {
			jsonObj = JSONObject.fromObject(list.get(i));
			userids[i] = jsonObj.getString("firstUserPhone");
		}
		userids[list.size()] = phone;
		Object[] money = crb.QueryConsumeTeam(Long.valueOf(brandid), userids, type, strDate, endDate);
		Map<String ,Object> mapp = new HashMap<String,Object>();
		mapp.put("amount",money[0]);
		mapp.put("times", money[1]);
		
		map.put("resp_code", "000000");
		map.put("resp_message", "查询成功");
		map.put("result", money[0]);
		return map;
	}
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/transactionclear/rank/consume")
	public @ResponseBody Object QueryConsumeRank(HttpServletRequest req,
			@RequestParam(value="brandid") String brandid,
			@RequestParam(value="strTime",required=false) String strTime) {
		Map map = new HashMap();
		if(null==brandid) {
			map.put("resp_code", "999999");
			map.put("resp_message", "参数不能为空");
		}
		List<Object[]> list = new ArrayList<Object[]>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if(null==strTime) {
				strTime = sdf.format(new Date());
			}
			list = crb.QueryConsumeRank2(Long.valueOf(brandid),strTime);
			System.out.println(list.toString());
			if(list.isEmpty()) {
				map.put("resp_code", "999999");
				map.put("resp_message", "暂无排名信息");
				return map;
			}
			List<Map<String,Object>> lists = new ArrayList<Map<String,Object>>();
			int i=0;
			for(Object[] objs:list) {
				Map<String,Object> mapp = new HashMap<String,Object>();
				mapp.put("name", "");
				mapp.put("phone", objs[0].toString().substring(0, 3)+"****"+objs[0].toString().substring(7, 11));
				mapp.put("rebate", objs[2]);
				lists.add(mapp);
				i++;
				if(i==10)
					break;
			}
			map.put("resp_code", "000000");
			map.put("resp_message", "查询成功");
			map.put("result", lists);
		} catch (Exception e) {
			map.put("resp_code", "999999");
			map.put("resp_message", e.getMessage());
		}
		return map;
	}
}
