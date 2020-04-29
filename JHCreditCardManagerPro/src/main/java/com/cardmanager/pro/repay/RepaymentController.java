/*
package com.cardmanager.pro.repay;

import com.alibaba.fastjson.JSONArray;
import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.util.RestTemplateUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class RepaymentController {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RestTemplateUtil util;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

    @Autowired
    private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;


    public Object repayment(){
        List<String> userIds = getUserId();
        Map<String,String> returnMap = new HashMap<>();
        String url = "http://user/v1.0/user/find/by/userid";
        LinkedMultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
        requestEntity.add("userIds", userIds);
        JSONObject resultJSONObject;
        try {
            String resultString = restTemplate.postForObject(url, requestEntity, String.class);
            resultJSONObject = JSONObject.fromObject(resultString);
        } catch (Exception e) {
            LOG.error("查询用户卡号，身份证号失败，详情：",e.getMessage());
            throw new RuntimeException(e);
        }
        if(resultJSONObject.get("respDode").toString().equals("000000")){
            LOG.info("查询用户卡号，身份证号失败");
            returnMap.put("respCode", "999999");
            returnMap.put("respMessage", "查询用户卡号，身份证号失败");
            return returnMap;
        }
        String bankInfo = resultJSONObject.get("result").toString();
        JSONArray objects = JSONArray.parseArray(bankInfo);
        for ( int i = 0; i<objects.size();i++){
            com.alibaba.fastjson.JSONObject jsonObject = objects.getJSONObject(i);
            consumeTaskPOJOBusiness.find

        }

    }

    private static List<String> getUserId(){
        String sql = "select * from t_user_temp";
        List<String> objects = new JdbcTemplate().queryForList(sql, String.class);
        return  objects;
    }
}
*/
