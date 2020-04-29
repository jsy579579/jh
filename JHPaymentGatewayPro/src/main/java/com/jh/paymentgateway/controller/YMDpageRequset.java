package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.ymd.HTTPClientUtils;
import com.jh.paymentgateway.util.ymd.RsaUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
@Controller
@EnableAutoConfiguration
public class YMDpageRequset extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	@Value("${jftx.requestURL}")
	private String requestURL;
	
	@Value("${jftx.partnerNo}")
	private String partnerNo;
	
	@Value("${jftx.public_key}")
	private String publicKey="D:\\certs\\jfdf_rsa_pub_key.pem";
	
	@Value("${jftx.private_key}")
	private String privateKey="D:\\certs\\xinli_rsa_private_key_pkcs8.pem";
	private static final Logger LOG = LoggerFactory.getLogger(YMDpageRequset.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static String Daifu_REQUEST_URL = "https://gwapi.yemadai.com/transfer/transferFixed";   //正式环境请求地址
	private static String CHECK_BALANCE_URL = "https://gwapi.yemadai.com/checkBalance";   //正式环境请求地址

	// 进件注册
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ymd/register")
	public @ResponseBody Object getRegister(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		String requestURL = "https://payfor.jfpays.com/rest/v1/api/";
    	
		JSONObject resJson = JSON.parseObject(requestURL);
		return resJson;

	}

	/**
	 *
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ymd/callback")
	public void JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		LOG.info("/v1.0/paymentgateway/quick/ymd/callback异步回调进来了======");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		String rCode="ok";
		Map<String, String[]> parameterMaps = request.getParameterMap();
		Map<String, String> parameterMap =new HashMap<String, String>();
		Set<String> keySet = parameterMaps.keySet();
		for (String key : keySet) {
			String[] strings = parameterMaps.get(key);
			LOG.info(key+"="+strings[0]);
			parameterMap.put(key, strings[0]);
		}
		LOG.info("parameterMap=========="+parameterMap);
		
		try {
			if(parameterMap!=null){
				String orderCode=parameterMap.get("MerBillNo");
	    	    String platformId=parameterMap.get("BillNo");
	    	    String respMsg=parameterMap.get("Result");
	    	    PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(orderCode);
	            if(parameterMap.get("Succeed").equals("00")){
	            	this.updateSuccessPaymentOrder(bean.getIpAddress(), orderCode,platformId);
	            }else if(parameterMap.get("Succeed").equals("11")){
	            	this.addOrderCauseOfFailure(orderCode, respMsg, bean.getIpAddress());
	            	this.updateStatusPaymentOrder(bean.getIpAddress(), "2", orderCode, platformId);
	            }else{
	            	rCode="no";
	            }
			}
		} catch (Exception e) {
			rCode="no";
		}
		PrintWriter pw = response.getWriter();
		pw.print(rCode);
		pw.close();
		
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ymd/callback1")
	public void testpay(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		
		RsaUtils rsaUtils = RsaUtils.getInstance();
        String transId="TEST"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String accountNumber="44703";
        String cardNo="6212261001038982085";
        String amount="1.00";
        String plain ="transId="+transId+"&accountNumber="+accountNumber+"&cardNo="+cardNo+"&amount="+amount;
        String prikey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKmqAcHVhWhZIPW3sH6PYjk9YzkAlnsxgqF/G2X/"
        		+ "RwXAUjjivKA9moEJYC4t3anbdaN+T0PmH4fn7wGsAKaDys/Ycwsxs+IbiFuvTuyrp190Igef4xgEnWjvg/FSWhqoO/"
        		+ "nUM7fQu2ckCWeOYDqun1mlq2pG9nxmEi7lpbPbF5bZAgMBAAECgYEAn2Ltmam8VUX917hD3vFWRg2sa/"
        		+ "1oYL0nSg39sDk8AGQnGfevWh3lSrmDGH7QEWTww7HCRzglgOhbOMfcKJIA8Oq1H85hdbVyrIaA88VFiW+aKzQ676SBkP/"
        		+ "3WOzeDMgYH7jPVKbnwPr6FPXkTzLspqxb2kaDz8e/Bfj2PAGnsc0CQQDzvFnBqcxeZE6Nz/"
        		+ "pnavm4SxYElb0MPmAeaghfoAOMyp6ylnJpFx3UUH2omidOqvxwUDzhQc0J9AEHHHg7VjsXAkEAsjODGzfArXNpkGTR7rUPy00M6krfnoGQq9fr+48LD4fCq/"
        		+ "YjwKoUPBiQ8rzLY6oHG3Dc+okJV6P1PqYnBXEzjwJAW+fB0Iy+Szl3hXHO8gAceamWe9QanOtIQy+oSKaFsCkW/"
        		+ "jBMo+Pbk5tmRpUaDUfcOF2JF0dAkWg4mv3ZOEajsQJAZrrliQpSDGwtXi2RDLDawxPPLz4svf7pfPeENkhXUwOryWCjac1izuZGoXkPM4xwfnRJIDFh7mE533z/"
        		+ "n9aLpwJASEVcIStMFcCMI3JqC7YTbc8u5wBaR0BswZnXmleGQ/n83vApk8ylZKpkqr39KiirpbDNcQtFqwEg+xqX8WYZAQ==";
        String signInfo = rsaUtils.signData(plain, prikey);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        stringBuffer.append("<yemadai>");
        stringBuffer.append("<accountNumber>"+accountNumber+"</accountNumber>");
        stringBuffer.append("<signType>RSA</signType>");
        stringBuffer.append("<notifyURL>"+ip+"/v1.0/paymentgateway/quick/ymd/callback</notifyURL>");
        stringBuffer.append("<tt>0</tt>");
        stringBuffer.append("<transferList>");
	        stringBuffer.append("<transId>"+ transId +"</transId>");
	        stringBuffer.append("<bankCode>工商银行</bankCode>");
	        stringBuffer.append("<provice>北京</provice>");
	        stringBuffer.append("<city>北京</city>");
	        stringBuffer.append("<branchName>南山路支行</branchName>");
	        stringBuffer.append("<accountName>钟守韩</accountName>");
	        stringBuffer.append("<cardNo>"+cardNo+"</cardNo>");
	        stringBuffer.append("<amount>"+amount+"</amount>");
	        stringBuffer.append("<remark>123</remark>");
	        stringBuffer.append("<secureCode>" + signInfo + "</secureCode>");
        stringBuffer.append("</transferList>");
        stringBuffer.append("</yemadai>");
        LOG.info(""+stringBuffer);
        Base64 base64 = new Base64();
        
        List<NameValuePair> nvps = new ArrayList<NameValuePair>(1);
		nvps.add(new BasicNameValuePair("transData", base64.encodeToString(stringBuffer.toString().getBytes("UTF-8"))));
        LOG.info(""+nvps);
        String httpPost = this.connect(nvps, Daifu_REQUEST_URL);
        LOG.info(new String(base64.decode(httpPost), "utf-8"));
	}
	
	//余额查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ymd/CheckBalance")
	public @ResponseBody Object CheckBalance(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		
		RsaUtils rsaUtils = RsaUtils.getInstance();
        String requestTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String plain = "44703" + requestTime;
        String prikey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKmqAcHVhWhZIPW3sH6PYjk9YzkAlnsxgqF/G2X/"
        		+ "RwXAUjjivKA9moEJYC4t3anbdaN+T0PmH4fn7wGsAKaDys/Ycwsxs+IbiFuvTuyrp190Igef4xgEnWjvg/FSWhqoO/"
        		+ "nUM7fQu2ckCWeOYDqun1mlq2pG9nxmEi7lpbPbF5bZAgMBAAECgYEAn2Ltmam8VUX917hD3vFWRg2sa/"
        		+ "1oYL0nSg39sDk8AGQnGfevWh3lSrmDGH7QEWTww7HCRzglgOhbOMfcKJIA8Oq1H85hdbVyrIaA88VFiW+aKzQ676SBkP/"
        		+ "3WOzeDMgYH7jPVKbnwPr6FPXkTzLspqxb2kaDz8e/Bfj2PAGnsc0CQQDzvFnBqcxeZE6Nz/"
        		+ "pnavm4SxYElb0MPmAeaghfoAOMyp6ylnJpFx3UUH2omidOqvxwUDzhQc0J9AEHHHg7VjsXAkEAsjODGzfArXNpkGTR7rUPy00M6krfnoGQq9fr+48LD4fCq/"
        		+ "YjwKoUPBiQ8rzLY6oHG3Dc+okJV6P1PqYnBXEzjwJAW+fB0Iy+Szl3hXHO8gAceamWe9QanOtIQy+oSKaFsCkW/"
        		+ "jBMo+Pbk5tmRpUaDUfcOF2JF0dAkWg4mv3ZOEajsQJAZrrliQpSDGwtXi2RDLDawxPPLz4svf7pfPeENkhXUwOryWCjac1izuZGoXkPM4xwfnRJIDFh7mE533z/"
        		+ "n9aLpwJASEVcIStMFcCMI3JqC7YTbc8u5wBaR0BswZnXmleGQ/n83vApk8ylZKpkqr39KiirpbDNcQtFqwEg+xqX8WYZAQ==";
        String signInfo = rsaUtils.signData(plain, prikey);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        stringBuffer.append("<CheckBalanceRequest>");
        stringBuffer.append("<MerNo>44703</MerNo>");
        stringBuffer.append("<signType>RSA</signType>");
        stringBuffer.append("<SignInfo>" + signInfo + "</SignInfo>");
        stringBuffer.append("<RequestTime>" + requestTime + "</RequestTime>");
        stringBuffer.append("</CheckBalanceRequest>");
        Base64 base64 = new Base64();
        List<NameValuePair> nvps = new ArrayList<NameValuePair>(1);
		nvps.add(new BasicNameValuePair("requestDomain", base64.encodeToString(stringBuffer.toString().getBytes("UTF-8"))));
        System.out.println(nvps);
        String httpPost = this.connect(nvps, CHECK_BALANCE_URL);
        
        HashMap<String, Object> getresult=getparse(httpPost);
        LOG.info("一麻袋余额查询："+getresult);
        String account ="0.00";
        if(getresult.get("respCode").equals("0000")){
        	account=(String) getresult.get("availableBalance");
        }
        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",account);
	}
	
	/**
	 * 连接类
	 * 
	 * @param nvps
	 * @param requestURL
	 * @return
	 */
	public String connect(List<NameValuePair> nvps, String requestURL) {
		try {
			
			HTTPClientUtils h = new HTTPClientUtils();
			String httpPost = h.httpPostPara(nvps, requestURL);
			return httpPost;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
                int readLen = 0;
                int readLengthThisTime = 0;
                byte[] message = new byte[contentLen];
                try {
                    while (readLen != contentLen) {
                        readLengthThisTime = is.read(message, readLen, contentLen- readLen);
                        if (readLengthThisTime == -1) {// Should not happen.
                           break;
                        }
                        readLen += readLengthThisTime;
                    }
                    return message;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return new byte[] {};
	}
	
	
	static HashMap<String, Object> result = new HashMap<String, Object>();
	
	public  static  HashMap<String,Object> getparse(String xml){
		xml=xml.substring(xml.indexOf("<?xml"), xml.length());
		System.out.println(xml);
		/***********************解析String****************************/
		StringReader read = new StringReader(xml);
		InputSource source = new InputSource(read);
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = (Document) sb.build(source);
			Element root = doc.getRootElement();
			result.put(root.getName(),root.getText());
			parse(root);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static HashMap<String,Object> parse(Element root){
		List nodes = root.getChildren();
		int len = nodes.size();
		if(len==0){
			result.put(root.getName(),root.getText());
		} else {
			for(int i=0;i<len;i++){
				Element element = (Element) nodes.get(i);//循环依次得到子元素
				result.put(element.getName(),element.getText());
				parse(element);
			}
		}
		return result;
	}
	/*public static void main(String[] args) {
		String xml = "HTTP/1.1 200 OK+Status+<?xml version='1.0' encoding='UTF-8' standalone='yes'?><CheckBalanceResponse><respCode>0000</respCode><respMsg>查询成功</respMsg><availableBalance>51943.00</availableBalance><unSettleBalance>0.00</unSettleBalance><requestTime>20190114163952</requestTime></CheckBalanceResponse>";
		xml=xml.substring(xml.indexOf("<?xml"), xml.length());
		System.out.println(xml);
		*//***********************解析String****************************//*
		StringReader read = new StringReader(xml);
		InputSource source = new InputSource(read);
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = (Document) sb.build(source);
			Element root = doc.getRootElement();
			result.put(root.getName(),root.getText());
			parse(root);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(result);
	}*/

	
}
