package cn.jh.clearing.business;

import java.util.Date;
import java.util.List;

import cn.jh.clearing.pojo.ConsumeRank;
import cn.jh.clearing.pojo.PaymentOrder;

public interface ConsumeRankBusiness {
	List<PaymentOrder> QueryConsumeRank(long brandid, String strTime, String endTime);
	
	List<Object[]> QueryConsumeRank2(long brandid, String strTime);
	
	Object[] QueryConsumeTeam(long brandid,String[] phone,String[] type,Date strTime,Date endTime);
	
	List<Object[]> QueryConsumeTeambychannelId(long brandid, String[] phone, String[] type, Date strTime, Date endTime);
	
}
