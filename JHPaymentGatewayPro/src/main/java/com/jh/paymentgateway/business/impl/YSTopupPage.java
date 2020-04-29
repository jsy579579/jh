package com.jh.paymentgateway.business.impl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStreamReader;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.ys.YSUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

import org.apache.commons.lang3.StringUtils;
import net.sf.json.JSONObject;

@Service
public class YSTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(YSTopupPage.class);
	
	
	@Value("${payment.ipAddress}")
	private String ip;
	
	//银商平台接口地址
	@Value("${ys.url}")
	private String APIurl;
	
	//商户号
	@Value("${ys.mid}")
	private String mid;
	
	//终端号
	@Value("${ys.tid}")
	private String tid;
	
	//机构商户号
	@Value("${ys.instMid}")
	private String instMid;
	
	//来源系统
	@Value("${ys.msgSrc}")
	private String msgSrc;
	

	//通讯秘钥
	@Value("${ys.key}")
	private String key;
	

	//消息类型:获取二维码
	@Value("${ys.msgType_getQRCode}")
	private String msgType_getQRCode;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
        //组织请求报文
        JSONObject json = new JSONObject();
        json.put("mid", mid);
        json.put("tid", tid);
        json.put("msgType", msgType_getQRCode);
        json.put("msgSrc", msgSrc);
        json.put("instMid", instMid);
        json.put("billNo", bean.getOrderCode());

        //是否要在商户系统下单，看商户需求  createBill() /v1.0/paymentgateway/topup/ys/payCallback
        json.put("billDate",DateFormatUtils.format(new Date(),"yyyy-MM-dd"));
        json.put("totalAmount", new BigDecimal(bean.getAmount()).multiply(new BigDecimal("100")).setScale(0).toString());
        json.put("notifyUrl",ip+"/v1.0/paymentgateway/topup/ys/payCallback" );
        json.put("requestTimestamp", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));

        Map<String, String> paramsMap = YSUtil.jsonToMap(json);
        paramsMap.put("sign", YSUtil.makeSign(key, paramsMap));
        System.out.println("paramsMap："+paramsMap);

        String strReqJsonStr = JSON.toJSONString(paramsMap);
        System.out.println("strReqJsonStr:"+strReqJsonStr);

        //调用银商平台获取二维码接口
        HttpURLConnection httpURLConnection = null;
        BufferedReader in = null;
        PrintWriter out = null;
        
        Map<String,Object> resultMap = new HashMap<String,Object>();
        if (!StringUtils.isNotBlank(APIurl)) {
            resultMap.put("errCode","URLFailed");
        }else{
        	try {
                URL url = new URL(APIurl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content_Type","application/json");
                httpURLConnection.setRequestProperty("Accept_Charset","UTF-8");
                httpURLConnection.setRequestProperty("contentType","UTF-8");
                //发送POST请求参数
                out = new PrintWriter(httpURLConnection.getOutputStream());
//                out = new OutputStreamWriter(httpURLConnection.getOutputStream(),"utf-8");
                out.write(strReqJsonStr);
//                out.println(strReqJsonStr);
                out.flush();

                //读取响应
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuffer content = new StringBuffer();
                    String tempStr = null;
                    in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((tempStr=in.readLine()) != null){
                        content.append(tempStr);
                    }
                    System.out.println("content:"+content.toString());

                    //转换成json对象
                    com.alibaba.fastjson.JSONObject respJson = JSON.parseObject(content.toString());
                    String resultCode = respJson.getString("errCode");
                    resultMap.put("errCode",resultCode);
                    if (resultCode.equals("SUCCESS")) {
                        String billQRCode = (String) respJson.get("billQRCode");
                        resultMap.put("billQRCode",billQRCode);
                        resultMap.put("respStr",respJson.toString());
                    }else {
                        resultMap.put("respStr",respJson.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("errCode","HttpURLException");
                resultMap.put("msg","调用银商接口出现异常："+e.toString());
                return resultMap;
            }finally {
                if (out != null) {
                    out.close();
                }
                httpURLConnection.disconnect();
            }
        	
        }
       LOG.info("resultStr:"+JSONObject.fromObject(resultMap).toString());
       if(resultMap.containsKey("billQRCode")){
    	   resultMap=ResultWrap.init(CommonConstants.SUCCESS,"成功",resultMap.get("billQRCode"));
       }else{
    	   
    	   resultMap=ResultWrap.err(LOG, CommonConstants.FALIED, resultMap.toString());
       }
      
        return resultMap;
		
		
	}
	
	
	
	public static String sortParam(Map<String, String> mapData) {

        return
                "bussId=" + mapData.get("bussId") +
                "&cardCvn2=" + mapData.get("cardCvn2") +
                "&cardExpire=" + mapData.get("cardExpire") +
                "&certNo=" + mapData.get("certNo") +
                "&merchantId=" + mapData.get("merchantId") +
                "&merOrderNum=" + mapData.get("merOrderNum") +
                "&tranAmt=" + mapData.get("tranAmt") +
                "&userAcctNo=" + mapData.get("userAcctNo") +
                "&userId=" + mapData.get("userId") +
                "&userIp=" + mapData.get("userIp") +
                "&userName=" + mapData.get("userName") +
                "&userPhone=" + mapData.get("userPhone") + "&";
    }
}

