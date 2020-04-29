package cn.jh.clearing.business.impl;

import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import com.netflix.discovery.converters.Auto;
import cn.jh.clearing.business.SwiftpassOrderBusiness;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.pojo.SwiftpassOrder;
import cn.jh.clearing.repository.SwiftpassOrderRepository;
import cn.jh.clearing.util.XmlUtils;
import cn.jh.common.utils.DateUtil;

@Service
public class SwiftpassOrderBusinessImpl implements SwiftpassOrderBusiness{	 

		@Autowired
		private JdbcTemplate jdbcTemplate;

	    @Autowired
	    private SwiftpassOrderRepository swiftpassOrderRepository;
	    @Autowired
	    private EntityManager em;
	    
	    
	    /**下载威富通订单*/
	    public List<SwiftpassOrder> downloadSwiftpass(String billDate,String mchId,String key)throws Exception {
			SortedMap<String, String> map = new TreeMap<String, String>();
			map.put("service","pay.bill.merchant");
			map.put("bill_date", billDate);
			map.put("bill_type","ALL");
			map.put("mch_id", mchId);
			String nonceStr = String.valueOf(new Date().getTime());
			map.put("nonce_str",nonceStr);
			Map<String, String> params = paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			buildPayParams(buf, params, false);
			String preStr = buf.toString();
			String sign = cn.jh.clearing.util.MD5.sign(preStr, "&key=" + key, "utf-8");
			map.put("sign", sign);
			CloseableHttpResponse response = null;
			CloseableHttpClient client = null;
				HttpPost httpPost = new HttpPost("https://download.swiftpass.cn/gateway");
				StringEntity entityParams = new StringEntity(XmlUtils.parseXML(map), "utf-8");
				httpPost.setEntity(entityParams);
				httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
				client = HttpClients.createDefault();
				response = client.execute(httpPost);
		     	SwiftpassOrder swiftpass = new SwiftpassOrder();
			    List<SwiftpassOrder> models=new ArrayList<SwiftpassOrder>();
				if(response != null && response.getEntity() != null) {				
				String result =  EntityUtils.toString(response.getEntity(),"UTF-8");
				result = result.replace("`", "");
			    String[] str = result.split("\r\n");
			    int length=str.length;
	      	  	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    for(int i=1;i<length-2;i++){
	        	  System.out.println(str[i]);
	        	  String[] swiftpassOrderArray=str[i].split(",");
	        	  SwiftpassOrder swiftpassOrders=new SwiftpassOrder();
	        	  swiftpassOrders.setCreateTime(sdf.parse(swiftpassOrderArray[0]));
	        	  swiftpassOrders.setTirdOrderCode(swiftpassOrderArray[8]);
	        	  swiftpassOrders.setOpenid(swiftpassOrderArray[1]);
	        	  swiftpassOrders.setThirdMchId(swiftpassOrderArray[2]);
	        	  swiftpassOrders.setPreMchId(swiftpassOrderArray[3]);
	        	  swiftpassOrders.setMchId(swiftpassOrderArray[4]);
	        	  swiftpassOrders.setDeviceInfo(swiftpassOrderArray[5]);
	        	  swiftpassOrders.setOrderCode(swiftpassOrderArray[6]);
	        	  swiftpassOrders.setMchOrderCode(swiftpassOrderArray[8]);
	        	  swiftpassOrders.setSubAppId(swiftpassOrderArray[9]);
	        	  swiftpassOrders.setRefundType(swiftpassOrderArray[20]);
	        	  swiftpassOrders.setService(swiftpassOrderArray[10]);
	        	  swiftpassOrders.setStatus(swiftpassOrderArray[11]);
	        	  swiftpassOrders.setBankType(swiftpassOrderArray[12]);
	        	  swiftpassOrders.setMoneyType(swiftpassOrderArray[13]);
	        	  swiftpassOrders.setTotalFee(swiftpassOrderArray[14]);
	        	  swiftpassOrders.setEntRedEnvelope(swiftpassOrderArray[15]);
	        	  swiftpassOrders.setOutrefundNo(swiftpassOrderArray[17]);
	        	  swiftpassOrders.setRefundId(swiftpassOrderArray[16]);
	        	  swiftpassOrders.setRefundFee(swiftpassOrderArray[18]);
	        	  swiftpassOrders.setExtraFee(swiftpassOrderArray[24]);
	        	  swiftpassOrders.setRate(swiftpassOrderArray[25]);
	        	  swiftpassOrders.setMchcreate_type(swiftpassOrderArray[26]);
	        	  swiftpassOrders.setRefundStatus(swiftpassOrderArray[21]);
	        	  swiftpassOrders.setMchName(swiftpassOrderArray[22]);
	        	  swiftpassOrders.setRelaFeel(swiftpassOrderArray[33]);
	        	  swiftpassOrders.setMchNum(swiftpassOrderArray[28]);    
	        	  models.add(swiftpassOrders); 
	        	  swiftpassOrderRepository.save(swiftpassOrders);
	        	  em.clear();
				}
				}
		    return  models;
		}
	    
	    public  Map<String, String> paraFilter(Map<String, String> sArray) {
			Map<String, String> result = new HashMap<String, String>(sArray.size());
			if (sArray == null || sArray.size() <= 0) {
				return result;
			}
			for (String key : sArray.keySet()) {
				String value = sArray.get(key);
				if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
					continue;
				}
				result.put(key, value);
			}
			return result;
		}

		public  void buildPayParams(StringBuilder sb, Map<String, String> payParams, boolean encoding) {
			List<String> keys = new ArrayList<String>(payParams.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				sb.append(key).append("=");
				if (encoding) {
					sb.append(urlEncode(payParams.get(key)));
				} else {
					sb.append(payParams.get(key));
				}
				sb.append("&");
			}
			sb.setLength(sb.length() - 1);
		}

		public  String urlEncode(String str) {
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (Throwable e) {
				return str;
			}
		}
	    
	    
		/**多条件查询信息*/
		@Override
		public Map findSwiftpassOrderAll(String tirdOrderCode, String thridMchId, String preMchId, String mchId,
				String orderCode, String mchOrderCode, Pageable pageAble) {
			
		     StringBuffer sql = new StringBuffer("from t_swiftpass_order where 1=1 ");

		     //如果条件不为空，往后面添加
		     if(tirdOrderCode!=null&&!tirdOrderCode.equals("")){
		    	 sql.append("and tird_order_code='"+tirdOrderCode+"' ");  	 
		     }
		     
			 if(thridMchId!=null&&!thridMchId.equals("")){
			  sql.append("and third_mch_id='"+thridMchId+"' ");
			 }
			 
			 if(preMchId!=null&&preMchId.equals("")){
				  sql.append("and pre_mch_id='"+preMchId+"' ");	 
			 }
			 
			 if(mchId!=null&&mchId.equals("")){
				sql.append("and mch_id='"+mchId+"' ");
			 }
			 
			 if(orderCode!=null&&orderCode.equals("")){
				sql.append("and order_code='"+orderCode+"' ");
			 }
			 
			 if(mchOrderCode!=null&&mchOrderCode.equals("")){
				 sql.append("and mch_order_code='"+mchOrderCode+"' ");
			 }
			
			    //定义一个新的字符串用来拼接之前的字符串
		  	 StringBuffer sqlCount=new StringBuffer("select COUNT(*) as count ").append(sql);
		  
		  	//将字符串变成sql语句查询到的结果转成int类型
			int count=Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString()) ;
	          //定义一个Map集合存放查询的数据
			 Map<String,Object> querySwiftpassOrder = jdbcTemplate.queryForMap(sqlCount.toString());
			 	try {
					
				} catch (Exception e) {
					// TODO: handle exception
				}
			    int pageNum = pageAble.getPageSize();
				int currentPage = pageAble.getPageNumber();
				
				//定义一个新的字符串用来表示分页的sql语句
				StringBuffer sqlList=new StringBuffer("select * ").append(sql).append(" order by create_time desc limit "+(currentPage)*pageNum+","+pageNum);
			 
				List<SwiftpassOrder> list = jdbcTemplate.query(sqlList.toString(), new RowMapper<SwiftpassOrder>(){
	            
					@Override
					public SwiftpassOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
						 SwiftpassOrder spo = new SwiftpassOrder();
						 spo.setCreateTime(DateUtil.getYYMMHHMMSSDateFromStr(rs.getString("create_time")));
						 spo.setTirdOrderCode(rs.getString("tird_order_code"));
						 spo.setOpenid(rs.getString("openid"));
						 spo.setThirdMchId(rs.getString("third_mch_id"));
						 spo.setPreMchId(rs.getString("pre_mch_id"));
						 spo.setMchId(rs.getString("mch_id"));
						 spo.setDeviceInfo(rs.getString("device_info"));
						 spo.setOrderCode(rs.getString("order_code"));
						 spo.setMchOrderCode(rs.getString("mch_order_code"));
						 spo.setSubAppId(rs.getString("sub_appid"));
						 spo.setRefundType(rs.getString("refund_type"));
						 spo.setService(rs.getString("service"));
						 spo.setStatus(rs.getString("status"));
						 spo.setBankType(rs.getString("bank_type"));
						 spo.setMoneyType(rs.getString("money_type"));
						 spo.setTotalFee(rs.getString("total_fee"));
						 spo.setEntRedEnvelope(rs.getString("enterprise_red_envelope"));
						 spo.setOutrefundNo(rs.getString("out_refund_no"));
						 spo.setRefundId(rs.getString("refund_id"));
						 spo.setRefundFee(rs.getString("refund_fee"));
						 spo.setExtraFee(rs.getString("extra_fee"));
						 spo.setRate(rs.getString("rate"));
						 spo.setMchcreate_type(rs.getString("mch_create_type"));
						 spo.setRefundStatus(rs.getString("refund_status"));
						 spo.setMchName(rs.getString("mch_name"));
						 spo.setRelaFeel(rs.getString("rela_feel"));					
						 spo.setMchNum(rs.getString("mch_num"));
						return spo;			
					}			
				});
				Map object = new HashMap();
				object.put("pageNum", pageNum);  //每页显示条数
				object.put("currentPage", currentPage);   //当前页
				object.put("totalElements", count); //总条数
				if(pageNum!=0){
					object.put("totalPages", count/pageAble.getPageSize());  //总页数
				}
				object.put("content", list);

				return object;

			} 
		 
	
	@Override
	@Transactional
	public SwiftpassOrder save(SwiftpassOrder swiftpassOrder) {
		SwiftpassOrder result =  swiftpassOrderRepository.save(swiftpassOrder);
		em.flush();
		em.clear();
		return result;
	}

	@Override
	@Transactional
	public List<SwiftpassOrder> saveall(List<SwiftpassOrder> swiftpassOrders){
		List<SwiftpassOrder> models = swiftpassOrderRepository.save(swiftpassOrders);
		 return  models;
		
	}

	@Override
	public int findCountByCreateTime(String createTime) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startTime = sdf.parse(createTime+" 00:00:00");
		Date endTime = sdf.parse(createTime + " 23:59:59");
		return swiftpassOrderRepository.findCountByCreateTime(startTime, endTime);
	}

	@Override
	public List<SwiftpassOrder> findByOrderCode(String orderCode) {
		return swiftpassOrderRepository.findByOrderCode(orderCode);
	}

//	@Override
//	public void delete(SwiftpassOrder swiftpassOrder) {
//		swiftpassOrderRepository.delete(swiftpassOrder);
//		
//	}

	@Override
	public List<SwiftpassOrder> findByCreateTime(String createTime) throws Exception  {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startTime = sdf.parse(createTime+" 00:00:00");
		Date endTime = sdf.parse(createTime + " 23:59:59");
		return swiftpassOrderRepository.findByCreateTime(startTime, endTime);
	}
	@Transactional
	@Override
	public void delete(SwiftpassOrder model) {
		swiftpassOrderRepository.delete(model);
	}
	
	
	
}

