package cn.jh.clearing.business.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.jh.clearing.business.ConsumeRankBusiness;
import cn.jh.clearing.pojo.ConsumeRank;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.repository.ConsumeRankRepository;

@Service
public class ConsumeRankBusinessImpl implements ConsumeRankBusiness{
	@Autowired
	ConsumeRankRepository crr;
	@Override
	public List<PaymentOrder> QueryConsumeRank(long brandid, String strTime, String endTime) {
		// TODO Auto-generated method stub
		return crr.QueryConsumeRank(brandid, strTime, endTime);
	}
	@Override
	public List<Object[]> QueryConsumeRank2(long brandid, String strTime) {
		// TODO Auto-generated method stub
		return crr.QueryConsumeRank2(brandid, strTime);
	}
	@Override
	public List<Object[]> QueryConsumeTeambychannelId(long brandid, String[] phone, String[] type, Date strTime, Date endTime) {
		// TODO Auto-generated method stub
		return crr.QueryConsumeTeambychannelId(brandid, phone, type, strTime, endTime);
	}
	
	@Override
	public Object[] QueryConsumeTeam(long brandid, String[] phone, String[] type, Date strTime, Date endTime) {
		// TODO Auto-generated method stub
		return crr.QueryConsumeTeam(brandid, phone, type, strTime, endTime);
	}
	
}
