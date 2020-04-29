package cn.jh.clearing.business.impl;

import java.net.URI;
import java.util.Date;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.CoinRecordBusiness;
import cn.jh.clearing.pojo.CoinRecord;
import cn.jh.clearing.repository.CoinRecordRepository;
import cn.jh.clearing.service.ProfitService;
import cn.jh.clearing.util.Util;
import net.sf.json.JSONObject;

@Service
public class CoinRecordBusinessImpl implements CoinRecordBusiness{
	private static final Logger LOG = LoggerFactory.getLogger(ProfitService.class);
	@Autowired
	Util util;
	
	@Autowired
	private EntityManager em;
	
	
	@Autowired
    private CoinRecordRepository coinRecordrepository;

	@Override
	public Page<CoinRecord> findCoinRecordByUserid(long userid,
			Pageable pageAble) {
		return coinRecordrepository.findCoinRecordByUserid(userid, pageAble);
	}

	@Transactional
	@Override
	public CoinRecord mergeCoinRecord(CoinRecord coinRecord) {
		CoinRecord result =  coinRecordrepository.save(coinRecord);
		em.flush();
		return result;
	}

	@Override
	public Page<CoinRecord> findCoinRecordByUserid(String userid, String grade, Date startTime,
			Date endTime, Pageable pageAble) {
		long brandid=0l;
		if(grade==null||grade.equals("")){
			if(userid != null && !userid.equalsIgnoreCase("")){
				if(startTime != null){
					if(endTime != null){
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, endTime, pageAble);
					}else{
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, pageAble);
					}
				}else{
					return coinRecordrepository.findCoinRecordByUserid(Long.parseLong(userid), pageAble);
				}
			}else{
				return coinRecordrepository.findAll(pageAble);
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
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, endTime, pageAble);
					}else{
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, pageAble);
					}
				}else{
					return coinRecordrepository.findCoinRecordByUserid(Long.parseLong(userid), pageAble);
				}
			}else{
				return coinRecordrepository.findAllCoinRecord(uids,pageAble);
			}
			
		}
		
	}
	
	@Override
	public Page<CoinRecord> findCoinRecordByBrandid(String userid,long brandid,String grade, Date startTime,
			Date endTime, Pageable pageAble) {
		if(grade==null||grade.equals("")){
			Long[] uids=this.findUserByBrandId(brandid);
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
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, endTime, pageAble);
					}else{
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, pageAble);
					}
				}else{
					return coinRecordrepository.findCoinRecordByUserid(Long.parseLong(userid), pageAble);
				}
			}else{
				return coinRecordrepository.findAllCoinRecord(uids,pageAble);
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
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, endTime, pageAble);
					}else{
						return coinRecordrepository.findAllCoinRecord(Long.parseLong(userid), startTime, pageAble);
					}
				}else{
					return coinRecordrepository.findCoinRecordByUserid(Long.parseLong(userid), pageAble);
				}
			}else{
				return coinRecordrepository.findAllCoinRecord(uids,pageAble);
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
	public Long[]  findUserByBrandId(long brandid) {
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/brandid";
		/**根据的用户手机号码查询用户的基本信息*/
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
		}
		return str2;
	}
	
}
