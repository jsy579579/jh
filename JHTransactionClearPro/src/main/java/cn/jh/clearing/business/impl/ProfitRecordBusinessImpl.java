package cn.jh.clearing.business.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.ProfitRecordBusiness;
import cn.jh.clearing.pojo.DistributionRecord;
import cn.jh.clearing.pojo.DistributionRecordCopy;
import cn.jh.clearing.pojo.ProfitRecord;
import cn.jh.clearing.pojo.ProfitRecordCopy;
import cn.jh.clearing.pojo.ProfitRecordPo;
import cn.jh.clearing.repository.DistributionRecordCopyRepository;
import cn.jh.clearing.repository.DistributionRecordRepository;
import cn.jh.clearing.repository.PaymentOrderRepository;
import cn.jh.clearing.repository.ProfitRecordCopyRepository;
import cn.jh.clearing.repository.ProfitRecordRepository;
import cn.jh.clearing.service.ProfitService;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class ProfitRecordBusinessImpl implements ProfitRecordBusiness{
	
	private static final Logger LOG = LoggerFactory.getLogger(ProfitService.class);
	@Autowired
    private ProfitRecordRepository profitRecordRepository;
	
	@Autowired
    private DistributionRecordRepository disRecordRepository;
	
	@Autowired
    private PaymentOrderRepository paymentOrderRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private ProfitRecordCopyRepository profitRecordCopyRepository;
	
	@Autowired
	private DistributionRecordCopyRepository distributionRecordCopyRepository;
	
	@Autowired
	Util util;

	@Override
	public Page<ProfitRecord> findProfitByUserid(long userid, Pageable pageAble) {
		return profitRecordRepository.findProfitByUserid(userid, pageAble);
	}

	@Transactional
	@Override
	public ProfitRecord merge(ProfitRecord profitRecord) {
		ProfitRecord  record =  profitRecordRepository.save(profitRecord);
		em.flush();
		em.clear();
		return record;
	}

	@Override
	public Page<ProfitRecord> findProfitByUserid(String userid,String grade , Date startTime,
			Date endTime, Pageable pageAble) {
		long brandid=0l;
		if(grade==null||grade.equals("")){
			
			if(userid != null && !userid.equalsIgnoreCase("")){
				if(startTime != null){
					
					if(endTime != null){
						
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, endTime, pageAble);
						
					}else{
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, pageAble);
					}
					
				}else{
					
					
					return profitRecordRepository.findProfitByUserid(Long.parseLong(userid), pageAble);
					
				}
				
			}else{
				
				
				return profitRecordRepository.findAll(pageAble);
				
			}
			
		}else{
			Long[] uids=this.findUserByGrade(grade,brandid);
			if(userid != null && !userid.equalsIgnoreCase("")){
				long useid=Long.parseLong(userid);
				String start="n";
				for(long uid:uids){
					if(uid==useid){
						start="y";
					}
				}
				if(start.equals("n")){
					userid="0";
				}
				
				if(startTime != null){
					
					if(endTime != null){
						
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, endTime, pageAble);
						
					}else{
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, pageAble);
					}
					
				}else{
					
					
					return profitRecordRepository.findProfitByUserid(Long.parseLong(userid), pageAble);
					
				}
				
			}else{
				
				
				return profitRecordRepository.findProfitByBrandId(uids,pageAble);
				
			}
			
			
			
		}	
		
	
	}
	
	@Override
	public Page<ProfitRecord> findProfitByBrandId(long brandid,String userid,String grade , Date startTime,Date endTime, Pageable pageAble) {
		if(grade==null||grade.equals("")){
			if(userid != null && !userid.equalsIgnoreCase("")){
				JSONObject resulJSON = this.findUserByUserId(userid);
				resulJSON = resulJSON.getJSONObject(CommonConstants.RESULT);
				String brandId = resulJSON.getString("brandId");
				String start="n";
				if (brandId.equals(String.valueOf(brandid).trim())) {
					start="y";
				}
				if(start.equals("n")){
					userid="0";
				}
				if(startTime != null){
					if(endTime != null){
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, endTime, pageAble);
					}else{
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, pageAble);
					}
				}else{
					return profitRecordRepository.findProfitByUserid(Long.parseLong(userid), pageAble);
				}
			}else{
				return profitRecordRepository.findProfitByBrandId(brandid+"",pageAble);
			}
		}else{
			Long[] uids=this.findUserByGrade(grade,brandid);
			if(userid != null && !userid.equalsIgnoreCase("")){
				long useid=Long.parseLong(userid);
				String start="n";
				for(long uid:uids){
					if(uid==useid){
						start="y";
					}
				}
				if(start.equals("n")){
					userid="0";
				}
				if(startTime != null){
					if(endTime != null){
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, endTime, pageAble);
					}else{
						return profitRecordRepository.findAllProfitRecord(Long.parseLong(userid), startTime, pageAble);
					}
				}else{
					return profitRecordRepository.findProfitByUserid(Long.parseLong(userid), pageAble);
				}
			}else{
				return profitRecordRepository.findProfitByBrandId(brandid+"",pageAble);
			}
			
		}
		
	
	}
	public Long[]  findUserByGrade(String  grade,long brandid) {
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/grade";
		/**根据的用户手机号码查询用户的基本信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("grade", grade);
		requestEntity.add("brand_id", brandid+"");
		RestTemplate restTemplate=new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		Long[] str2 = new Long[]{0l};
		JSONObject object =  jsonObject.getJSONObject("result");
		String ids=object.getString("uids");
		if(ids.length()>0){
			ids = ids.substring(0, ids.length()-1);
			String[] str1 = ids.split(",");
	        str2 = new Long[str1.length];
	        for (int j = 0; j < str1.length; j++) {
	            str2[j] = Long.valueOf(str1[j]);
	        }
		}
		return str2;
	}
	public JSONObject  findUserByUserId(String userId) {
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/id";
		/**根据brand_id查询用户的基本信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("id", userId);
		RestTemplate restTemplate=new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		
		
		/*URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/brandid";
		*//**根据brand_id查询用户的基本信息*//*
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("brand_id", brandid+"");
		RestTemplate restTemplate=new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		Long[] str2 = new Long[]{0l};
		JSONObject object =  jsonObject.getJSONObject("result");
		String ids=object.getString("uids");
		if(ids.length()>0){
			ids = ids.substring(0, ids.length()-1);
			String[] str1 = ids.split(",");
			str2 = new Long[str1.length];
			for (int j = 0; j < str1.length; j++) {
				str2[j] = Long.valueOf(str1[j]);
			}
		}*/
		return jsonObject;
	}
	
	@Override
	public ProfitRecord queryProfitRecordByordercode(String ordercode, String type) {
		return profitRecordRepository.findProfitRecordByordercodeAndtype(ordercode, type);
	}
	@Override
	public  BigDecimal findsumProfitRecord(long acquserid){
		
		
		return profitRecordRepository.findsumProfitRecord(acquserid);
	}
	/**-----------------------------返佣-------------------------**/
	@Transactional
	@Override
	public DistributionRecord merge(DistributionRecord profitRecord) {
		DistributionRecord record =  disRecordRepository.save(profitRecord);
		em.flush();
		return record;
	}

	@Override
	public Page<DistributionRecord> findDistributionRecordByUserid(
			long userid, Date startTime, Date endTime, Pageable pageAble) {
			if(startTime!=null){
				if(endTime!=null){
					return disRecordRepository.findDistributionRecordByUserId(userid, startTime, endTime, pageAble);
				}else{
					return disRecordRepository.findDistributionRecordByUserIdstart(userid, startTime, pageAble);
				}
				
			}else if(endTime!=null){
				return disRecordRepository.findDistributionRecordByUserIdend(userid, startTime, pageAble);
				
			}else{
				return disRecordRepository.findDistributionRecordByUserid(userid, pageAble);
			}
		
	}

	@Override
	public Page<DistributionRecord> findDistributionRecordByPlatform(long brandid, String userid, String order_code, Date startTime,
			Date endTime, Pageable pageAble) {
		if(brandid==-1){
			if(userid!=null&&!userid.equals("")){
				if(order_code!=null&&!order_code.equals("")){
					if(startTime!=null){
						if(endTime!=null){
							return disRecordRepository.findDistributionRecordByUseridOrdercodestartend(Long.parseLong(userid), order_code, startTime, endTime, pageAble);
							
						}else{
							return disRecordRepository.findDistributionRecordByUseridOrdercodestart(Long.parseLong(userid), order_code, startTime, pageAble);
						}
						
					}else if(endTime!=null){
						
						return disRecordRepository.findDistributionRecordByUseridOrdercodeend(Long.parseLong(userid), order_code, endTime, pageAble);
					}else{
						
						return disRecordRepository.findDistributionRecordByUseridOrdercode(Long.parseLong(userid), order_code, pageAble);
					}
					
				}else if(startTime!=null){
						if(endTime!=null){
							return disRecordRepository.findDistributionRecordByUserId(Long.parseLong(userid), startTime, endTime, pageAble);
						}else{
							return disRecordRepository.findDistributionRecordByUserIdstart(Long.parseLong(userid), startTime, pageAble);
						}
				}else if(endTime!=null){
					return disRecordRepository.findDistributionRecordByUserIdend(Long.parseLong(userid), startTime, pageAble);
					
				}else{
					return disRecordRepository.findDistributionRecordByUserid(Long.parseLong(userid), pageAble);
				}
				
			}else if(order_code!=null&&!order_code.equals("")){
				return disRecordRepository.findDistributionRecordByOrdercode(order_code, pageAble);
			}else if(startTime!=null){
				if(endTime!=null ){
					return disRecordRepository.findDistributionRecordBystartend(startTime, endTime, pageAble);
				}
				
			}else{
				
				return disRecordRepository.findAllDistributionRecord( pageAble);	
				
			}
			
		}else{
			String[] ordercodes=this.getpaymentOrdercodes(brandid, "1", "1");
			boolean retn=false;
			if(userid!=null&&!userid.equals("")){
				if(order_code!=null&&!order_code.equals("")){
					if(ordercodes==null){
						return null;
					}
					for(String ordercode:ordercodes ){
						if(order_code.equals(ordercode)){
							retn=true;
							break;
						}
					}
					if(retn){
						if(startTime!=null){
							if(endTime!=null){
								return disRecordRepository.findDistributionRecordByUseridOrdercodestartend(Long.parseLong(userid), order_code, startTime, endTime, pageAble);
								
							}else{
								return disRecordRepository.findDistributionRecordByUseridOrdercodestart(Long.parseLong(userid), order_code, startTime, pageAble);
							}
							
						}else if(endTime!=null){
							
							return disRecordRepository.findDistributionRecordByUseridOrdercodeend(Long.parseLong(userid), order_code, endTime, pageAble);
						}else{
							
							return disRecordRepository.findDistributionRecordByUseridOrdercode(Long.parseLong(userid), order_code, pageAble);
						}
					}else{
						return null;
					}
					
					
				}else if(startTime!=null){
						if(endTime!=null){
							return disRecordRepository.findDistributionRecordByUserId(ordercodes,Long.parseLong(userid), startTime, endTime, pageAble);
						}else{
							return disRecordRepository.findDistributionRecordByUserIdstart(ordercodes,Long.parseLong(userid), startTime, pageAble);
						}
				}else if(endTime!=null){
					return disRecordRepository.findDistributionRecordByUserIdend(ordercodes,Long.parseLong(userid), startTime, pageAble);
					
				}else{
					return disRecordRepository.findDistributionRecordByUserid(ordercodes,Long.parseLong(userid), pageAble);
				}
				
			}else if(order_code!=null&&!order_code.equals("")){
				if(ordercodes==null){
					return null;
				}
				for(String ordercode:ordercodes ){
					if(order_code.equals(ordercode)){
						retn=true;
						break;
					}
				}
				if(retn){
					return disRecordRepository.findDistributionRecordByOrdercode(order_code, pageAble);
				}else{
					return null;
				}
				
			}else if(startTime!=null){
				if(endTime!=null ){
					return disRecordRepository.findDistributionRecordBystartend(ordercodes,startTime, endTime, pageAble);
				}
				
			}else{
				
				return disRecordRepository.findAllDistributionRecord(ordercodes, pageAble);	
				
			}
			
		}
		return disRecordRepository.findAllDistributionRecord( pageAble);
	}
	/***
	 * 获取brandid数据
	 * paymentOrderRepository
	 * */
	public String[]  getpaymentOrdercodes(long brandid , String type ,String status ){
		String[] str1 = type.split(",");
		String[] str2 = status.split(",");
		return paymentOrderRepository.findAllPaymentOrderordercodesbyBrandid(brandid, str1,str2);
	}

	//获取当前phone的数据库记录(oriphone为需要查询的手机号)
	@Override
	public List<ProfitRecord> queryProfitByOriPhone(String oriphone, String ordercode) {
		return profitRecordRepository.queryProfitByOriPhone(oriphone, ordercode);
	}

	//依据ordercode获取分润表中的订单信息
	@Override
	public List<ProfitRecord> queryProfitAmount(String ordercode) {
		return profitRecordRepository.queryProfitAmount(ordercode);
	}

	@Override
	public List<DistributionRecord> findAllDistributionByPhone(String acqphone,Date startTimeDate, Date endTimeDate, Pageable pageable) {
		Page<DistributionRecord> page = disRecordRepository.findAllDistributionByPhone(acqphone, startTimeDate, endTimeDate, pageable);
		return page.getContent();
	}

	@Override
	public Page<ProfitRecord> findProfitByUserid(String orderCode, Date startTimeDate, Date endTimeDate,
			Pageable pageable) {
		return profitRecordRepository.finByOrderCode(orderCode,startTimeDate,endTimeDate,pageable);
	}

	@Override
	public List<Object[]> findsumProfitRecordByAcqUserIds(long[] acqUserIds) {
		return profitRecordRepository.findsumProfitRecordByAcqUserIds(acqUserIds);
	}

	@Transactional
	@Override
	public void createNewProfitRecord(BigDecimal rebate, String preUserPhone, BigDecimal preRate, Long preUserId,
			BigDecimal amount, String ordercode, Long firstUserId, String firstUserPhone, BigDecimal firstRate,
			String type, BigDecimal scale, String description, String brandId, long level, String firstUserName, String preUserName) {
		ProfitRecord profitRecord = new ProfitRecord();
		profitRecord.setAcqAmount(rebate);
		profitRecord.setBrandId(brandId);
		profitRecord.setAcqphone(preUserPhone);
		profitRecord.setAcqrate(preRate);
		profitRecord.setAcquserid(preUserId);
		profitRecord.setAmount(amount);
		profitRecord.setOrdercode(ordercode);
		profitRecord.setOriphone(firstUserPhone);
		profitRecord.setOriuserid(firstUserId);
		profitRecord.setOrirate(firstRate);
		profitRecord.setRemark(description);
		profitRecord.setScale(BigDecimal.ONE);
		profitRecord.setType(type);
		profitRecord.setCreateTime(new Date());
		profitRecord.setLevel(level);
		profitRecord.setOriUserName(firstUserName);
		profitRecord.setAcqUserName(preUserName);
		profitRecordRepository.saveAndFlush(profitRecord);
		em.flush();
		em.clear();
	}

	@Override
	public Page<ProfitRecord> queryProfitAmountByPhone(String phone,String[] order,  String[] type, Date startTime, Date endTime,
			Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.queryProfitAmountByPhone(phone, order,type, startTime, endTime, pageable);
	}

	@Override
	public Page<ProfitRecord> queryProfitAmountByGetPhone(String phone, String[] type, Date startTime, Date endTime,
			Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.queryProfitAmountByGetPhone(phone, type, startTime, endTime, pageable);
	}

	

	
	@Override
	public Page<ProfitRecord> finByManyParams(String orderCode, String[] type, Date startTimeDate, Date endTimeDate,
			Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.finByManyParams(orderCode, type, startTimeDate, endTimeDate, pageable);
	}

	@Override
	public Page<ProfitRecord> queryProfitAmountByDoublePhone(String phone, String getphone, String[] type,
			Date startTime, Date endTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.queryProfitAmountByDoublePhone(phone, getphone, type, startTime, endTime, pageable);
	}

	@Override
	public Page<ProfitRecord> queryProfitAmountByOderGetPhone(String getphone, String order, String[] type,
			Date startTime, Date endTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.queryProfitAmountByOderGetPhone(getphone, order, type, startTime, endTime, pageable);
	}

	@Override
	public Page<ProfitRecord> queryProfitAmountByOderPhone(String phone, String order, String[] type, Date startTime,
			Date endTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.queryProfitAmountByOderPhone(phone, order, type, startTime, endTime, pageable);
				
	}

	@Override
	public Page<ProfitRecord> queryByAllParams(String phone, String getphone, String order, String[] type,
			Date startTime, Date endTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.finByManyParams(order, type, startTime, endTime, pageable);
	}

	@Override
	public Object queryProfitAll(String phone, String[] type, Date startTime, Date endTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return profitRecordRepository.queryProfitAll(phone, type, startTime, endTime, pageable);
	}

	@Override
	public BigDecimal queryProfitRecordSumAcqAmountByPhone(String phone, String startTime, String endTime) {
		
		
		StringBuffer sql = new StringBuffer("select sum(acq_amount) from t_profit_record where acq_phone=" + phone);
		
		if (startTime != null && !"".equals(startTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
		}
		if (endTime != null && !"".equals(endTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
		}
		
		Map<String, Object> profitSumAmount = jdbcTemplate.queryForMap(sql.toString());
		BigDecimal big = null;
		if (profitSumAmount.get("sum(acq_amount)") != null) {
			big = (BigDecimal) profitSumAmount.get("sum(acq_amount)");
		} else {
			double d = 0.00;
			big = big.valueOf(d);
		}
		
		return big;
	}

	@Override
	public List<Object> getProfitRecordByAcqUserId(long userId, Date startTime) {
		em.clear();
		List<Object> result = profitRecordRepository.getProfitRecordByAcqUserId(userId, startTime);
		return result;
	}

	@Override
	public BigDecimal getSumProfitRecordByDate(long userId, String startTime, String endTime) {
		em.clear();
		BigDecimal result = profitRecordRepository.getSumProfitRecordByDate(userId, startTime, endTime);
		return result;
	}

	@Override
	public Map getProfitRecordByUserIdAndDate(long userId, String startTime, String endTime,
			Pageable pageable) {
		Map<String,Object> object = new HashMap<>();
		
		StringBuffer sql = new StringBuffer("from t_profit_record where acq_user_id='" + userId + "' and create_time>='" + startTime + "' and create_time<='" + endTime + "' order by create_time desc");
		StringBuffer sql1 = new StringBuffer("from t_distribution_record where acq_user_id='" + userId + "' and create_time>='" + startTime + "' and create_time<='" + endTime + "' order by create_time desc");

		
		StringBuffer sqlCount = new StringBuffer("select count(*) as count ").append(sql);
		StringBuffer sqlCount1 = new StringBuffer("select count(*) as count ").append(sql1);
		
		
		int count = Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString());
		int count1 = Integer.parseInt(jdbcTemplate.queryForMap(sqlCount1.toString()).get("count").toString());

		
		int pageNum = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		List<ProfitRecordPo> list = jdbcTemplate.query(new StringBuffer("select * ").append(sql).toString(), new RowMapper<ProfitRecordPo>() {

			@Override
			public ProfitRecordPo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfitRecordPo pr = new ProfitRecordPo();
				
				String before = rs.getString("ori_phone").substring(0, 3);
				String after = rs.getString("ori_phone").substring(7);
				pr.setOriphone(before + "****" + after);
				pr.setRemark("分润收益");
				pr.setAcqAmount(new BigDecimal(rs.getString("acq_amount").substring(0, rs.getString("acq_amount").indexOf(".") + 3)));
				pr.setCreateTime(rs.getString("create_time").substring(11,19));
				
				return pr;
			}
			
		});
		
		List<ProfitRecordPo> list1 = jdbcTemplate.query(new StringBuffer("select * ").append(sql1).toString(), new RowMapper<ProfitRecordPo>() {

			@Override
			public ProfitRecordPo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProfitRecordPo pr = new ProfitRecordPo();
				
				String before = rs.getString("ori_phone").substring(0, 3);
				String after = rs.getString("ori_phone").substring(7);
				pr.setOriphone(before + "****" + after);
				pr.setRemark("返佣收益");
				pr.setAcqAmount(new BigDecimal(rs.getString("acq_amount").substring(0, rs.getString("acq_amount").indexOf(".") + 3)));
				pr.setCreateTime(rs.getString("create_time").substring(11,19));
				
				return pr;
			}
			
		});
		
		list.addAll(list1);
		
		object.put("content", list);
		
		return object;
	}

	@Transactional
	@Override
	public void createProfitRecordCopy(BigDecimal rebate, String preUserPhone, BigDecimal preRate, Long preUserId,
			BigDecimal amount, String ordercode, Long firstUserId, String firstUserPhone, BigDecimal firstRate,
			String type, BigDecimal scale, String description, String brandId, long level, String firstUserName,
			String preUserName) {
		ProfitRecordCopy profitRecord = new ProfitRecordCopy();
		profitRecord.setAcqAmount(rebate);
		profitRecord.setBrandId(brandId);
		profitRecord.setAcqphone(preUserPhone);
		profitRecord.setAcqrate(preRate);
		profitRecord.setAcquserid(preUserId);
		profitRecord.setAmount(amount);
		profitRecord.setOrdercode(ordercode);
		profitRecord.setOriphone(firstUserPhone);
		profitRecord.setOriuserid(firstUserId);
		profitRecord.setOrirate(firstRate);
		profitRecord.setRemark(description);
		profitRecord.setScale(BigDecimal.ONE);
		profitRecord.setType(type);
		profitRecord.setCreateTime(new Date());
		profitRecord.setLevel(level);
		profitRecord.setOriUserName(firstUserName);
		profitRecord.setAcqUserName(preUserName);
		profitRecordCopyRepository.saveAndFlush(profitRecord);
		em.flush();
		em.clear();
		
	}

	@Transactional
	@Override
	public DistributionRecordCopy mergeCopy(DistributionRecordCopy profitRecord) {
		DistributionRecordCopy result = distributionRecordCopyRepository.save(profitRecord);
		em.flush();
		return result;
	}

	@Override
	public Page<ProfitRecordCopy> getProfitRecordCopyByOrderCode(String orderCode, Pageable pageAble) {
		em.clear();
		Page<ProfitRecordCopy> result = profitRecordCopyRepository.getProfitRecordCopyByOrderCode(orderCode, pageAble);
		return result;
	}

	@Override
	public Page<DistributionRecordCopy> getDistributionRecordCopyByOrderCode(String orderCode, Pageable pageAble) {
		em.clear();
		Page<DistributionRecordCopy> result = distributionRecordCopyRepository.getDistributionRecordCopyByOrderCode(orderCode, pageAble);
		return result;
	}

	@Override
	public Page<ProfitRecord> findProfitByBrandIdAndTime(String brandId, Date createTime, Date endTime, Pageable pageAble) {
		em.clear();
		return profitRecordRepository.findProfitByBrandIdAndTime(brandId,createTime,endTime,pageAble);
	}

	@Override
	public Page<ProfitRecord> findBrandProfitByBrandId(String brandId, Pageable pageAble) {
		em.clear();
		return profitRecordRepository.findBrandProfitByBrandId(brandId,pageAble);
	}

	@Override
	public Object queryProfitAllByBrandId(String brandId) {
		em.clear();
		return profitRecordRepository.queryProfitAllByBrandId(brandId);
	}


	@Override
	public cn.jh.clearing.util.Page<Object[]> listProfitByUserId(long userId,int start,int size) {

		PageRequest pageRequest = new PageRequest(start,size);//借助计算起始位置
		int total = profitRecordRepository.countAllByUserId(userId);// 计算数据总条数
		List<Object[]> records=profitRecordRepository.findProfitInfoByUserId(userId,pageRequest.getOffset(),pageRequest.getPageSize());// 获取分页数据
		cn.jh.clearing.util.Page<Object[]> page= new cn.jh.clearing.util.Page<Object[]>(records,size,start,total);
		return page;

	}

	@Override
	public int countByUserId(long userId) {
		em.clear();
		return profitRecordRepository.countAllByUserId(userId);
	}

	@Override
	public Page<ProfitRecord> findBrandProfitByBrandIdAndType(String brandId, String type, Pageable pageAble) {
		return profitRecordRepository.findBrandProfitByBrandIdAndType(brandId,type,pageAble);
	}

	@Override
	public Object queryProfitAllByBrandIdAndType(String brandId, String type) {
		return profitRecordRepository.queryProfitAllByBrandIdAndType(brandId,type);
	}

	@Override
	public Page<ProfitRecord> findBrandProfitByBrandIdAndTypeAndTime(String brandId, String type, Date startDate, Date endDate, Pageable pageAble) {
		return profitRecordRepository.findBrandProfitByBrandIdAndTypeAndTime(brandId,type,startDate,endDate,pageAble);
	}

	@Override
	public Object queryProfitAllByBrandIdAndTypeAndTime(String brandId, String type, Date startDate, Date endDate) {
		return profitRecordRepository.queryProfitAllByBrandIdAndTypeAndTime(brandId,type,startDate,endDate);
	}

	@Override
	public List queryRebateType() {
		return profitRecordRepository.queryRebateType();
	}

	@Override
	public Object queryProfitAllByBrandIdAndTime(String brandId, Date startDate, Date endDate) {
		em.clear();
		return profitRecordRepository.queryProfitAllByBrandIdAndTime(brandId, startDate, endDate);

	}
}
