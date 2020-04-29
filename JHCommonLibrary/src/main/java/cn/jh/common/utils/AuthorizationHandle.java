package cn.jh.common.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationHandle {
	
	/**
	 * 验证String是否是空字符串或null
	 * @param fileds
	 * @return
	 */
	public static Map<String,Object> verifyStringFiledIsNull(String ...fileds){
		Map<String,Object> map = new HashMap<String,Object>();
		for(String str:fileds){
			if(str!=null){
				str = str.trim();
			}
			if(str==null||"".equals(str)){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,"参数不能为空");
				return map;
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证通过");
		return map;
	}
	
	/**
	 * 验证是否是合法时间日期并返回指定格式的时间日期
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static Map<String,Object> verifyDate(String dateStr,String pattern){
		Map<String,Object> map = new HashMap<String,Object>();
		Date date = null;
		try {
			date = DateUtil.getDateStringConvert(new Date(), dateStr, pattern);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您输入的日期格式不正确,请重新输入!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证成功");
		map.put(CommonConstants.RESULT, date);
		return map;
	}
	
	/**
	 * 验证是否是合法金额并返回指定BigDecimal格式数据
	 * @param amountStr
	 * @return
	 */
	public static Map<String,Object> verifyMoney(String amountStr,int scale,int bigDecimalRound){
		Map<String,Object>map = new HashMap<String,Object>();
		amountStr = amountStr.trim();
		BigDecimal amount;
		try {
			amount = new BigDecimal(amountStr).setScale(scale, bigDecimalRound);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您输入的金额有误,请重新输入!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证成功");
		map.put(CommonConstants.RESULT, amount);
		return map;
	}
}
