package cn.jh.clearing.business.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import cn.jh.clearing.business.DistributionBusiness;
import cn.jh.clearing.pojo.DistributionRecord;
import cn.jh.clearing.pojo.ProfitRecordPo;
import cn.jh.clearing.repository.DistributionRecordRepository;

@Service
public class DistributionBusinessImpl implements DistributionBusiness{
	@Autowired
	DistributionRecordRepository drr;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public Page<DistributionRecord> findDistributionRecordByordercode(String order, Date strTime, Date endTime,
			Pageable page) {
		// TODO Auto-generated method stub
		return drr.findDistributionRecordByordercode(order, strTime, endTime, page);
	}

	@Override
	public Page<DistributionRecord> findAllDistributionByPhone(String acqphone, Date strTime, Date endTime,
			Pageable page) {
		// TODO Auto-generated method stub
		return drr.findAllDistributionByPhone(acqphone, strTime, endTime, page);
	}

	@Override
	public Page<DistributionRecord> findAllDistributionByoriPhone(String oriphone,String[] order, Date strTime, Date endTime,
			Pageable page) {
		// TODO Auto-generated method stub
		return drr.findAllDistributionByoriPhone(oriphone,order, strTime, endTime, page);
	}

	@Override
	public Page<DistributionRecord> findAllDistributionByPhoneAndOrder(String oriphone, String order, Date strTime,
			Date endTime, Pageable page) {
		// TODO Auto-generated method stub
		return drr.findAllDistributionByPhoneAndOrder(oriphone, order, strTime, endTime, page);
	}

	@Override
	public Page<DistributionRecord> findAllDistributionByPhoneAndacqOrder(String acqphone, String order, Date strTime,
			Date endTime, Pageable page) {
		// TODO Auto-generated method stub
		return drr.findAllDistributionByPhoneAndacqOrder(acqphone, order, strTime, endTime, page);
	}

	@Override
	public Page<DistributionRecord> findAllDistributionByPhoneAndoriOrder(String acqphone, String oriphone,
			Date strTime, Date endTime, Pageable page) {
		// TODO Auto-generated method stub
		return drr.findAllDistributionByPhoneAndoriOrder(acqphone, oriphone, strTime, endTime, page);
	}

	@Override
	public Page<DistributionRecord> findByAllParams(String acqphone, String oriphone, String order, Date strTime,
			Date endTime, Pageable page) {
		// TODO Auto-generated method stub
		return drr.findByAllParams(acqphone, oriphone, order, strTime, endTime, page);
	}

	@Override
	public BigDecimal queryDistributionSumAcqAmountByPhone(String phone, String startTime, String endTime) {

		StringBuffer sql = new StringBuffer("select sum(acq_amount) from t_distribution_record where acq_phone=" + phone);
		
		if (startTime != null && !"".equals(startTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
		}
		if (endTime != null && !"".equals(endTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
		}
		
		Map<String, Object> distributionSumAmount = jdbcTemplate.queryForMap(sql.toString());
		BigDecimal big = null;
		if (distributionSumAmount.get("sum(acq_amount)") != null) {
			big = (BigDecimal) distributionSumAmount.get("sum(acq_amount)");
		} else {
			double d = 0.00;
			big = big.valueOf(d);
		}
		
		return big;
		
	}

	@Override
	public List<Object> getDistributionRecordByAcqUserId(long userId, Date strTime) {
		List<Object> result = drr.getDistributionRecordByAcqUserId(userId, strTime);
		return result;
	}

	@Override
	public BigDecimal getSumDistributionRecordByDate(long userId, String startTime, String endTime) {
		BigDecimal result = drr.getSumDistributionRecordByDate(userId, startTime, endTime);
		return result;
	}

	@Override
	public Map getDistributionRecordByUserIdAndDate(long userId, String startTime, String endTime, Pageable pageable) {
		Map object = new HashMap();
		
		StringBuffer sql = new StringBuffer("from t_distribution_record where acq_user_id='" + userId + "' and create_time>='" + startTime + "' and create_time<='" + endTime + "' order by create_time desc");
		
		StringBuffer sqlCount = new StringBuffer("select count(*) as count ").append(sql);
		
		int count = Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString());
		
		int pageNum = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		List<ProfitRecordPo> list = jdbcTemplate.query(new StringBuffer("select * ").append(sql).toString(), new RowMapper<ProfitRecordPo>() {

			@Override
			public ProfitRecordPo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfitRecordPo pr = new ProfitRecordPo();
				String before = rs.getString("ori_phone").substring(0, 3);
				String after = rs.getString("ori_phone").substring(7);
				pr.setOriphone(before + "****" + after);
				pr.setRemark("返佣收益");
				pr.setAcqAmount(new BigDecimal(rs.getString("acq_amount")));
				pr.setCreateTime(rs.getString("create_time").substring(11,19));
				
				return pr;
			}
			
		});
		
		object.put("pageNum", pageNum); // 每页显示条数
		object.put("currentPage", currentPage); // 当前页
		object.put("totalElements", count); // 总条数
		if (pageNum != 0) {
			object.put("totalPages", count / pageable.getPageSize()); // 总页数
		}
		object.put("content", list);
		
		return object;
	}

	@Override
	public DistributionRecord addDistribution(DistributionRecord dbr) {
		return drr.save(dbr);
	}
}
