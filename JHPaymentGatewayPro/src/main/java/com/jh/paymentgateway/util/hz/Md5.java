package com.jh.paymentgateway.util.hz;

import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.controller.HZpageRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
//import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

public class Md5 {
	
	private static final Logger LOG = LoggerFactory.getLogger(Md5.class);
	
    /**
     * md5 加密工具类
     * @author wangwch
     *
     */
    public static class Md5Utils {

        /**
         * 生成有效签名
         *
         * @param orgin
         * @return
         */
        public static String signature(String orgin) {
            String result = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
            } catch (Exception e) {
                throw new java.lang.RuntimeException("sign error !");
            }
            return result;
        }

        /**
         * 二行制转字符串
         *
         * @param b
         * @return
         */
        private static String byte2hex(byte[] b) {
            StringBuffer hs = new StringBuffer();
            String stmp = "";
            for (int n = 0; n < b.length; n++) {
                stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
                if (stmp.length() == 1)
                    hs.append("0").append(stmp);
                else
                    hs.append(stmp);
            }
            return hs.toString().toLowerCase();
        }
    }
    public static class UnionFastPay {

        private String merchantId;//	String	是	商户号测试商户号: 99928989230000密钥: abcd654321

        private String merOrderId;//	String	是	商户订单号

        private String totalFee;//	Int	是	订单总金额，单位为分

        private String body;//	Stirng	是	商品名称

        private String accType;//	String 	是	交易卡类型DEBIT-储蓄卡 CREDIT-信用卡

        private String accName;//	String	是	交易卡持卡人姓名

        private String accNo;//	String	是	交易卡号

        private String cvn2; //cvn2

        private String expire;//有效期MMYY，例例如 0120

        private String mobile;//交易卡号手机


        private String bankCode;//	String	是	交易行号4.2

        private String idType;//	String	是	固定值 01 身份证

        private String idNo;// 证件号
        private String expireTime;//	Int	否	默认15分钟 最小15分钟 注，針對部分通道有效
        private String frontNotifyUrl;//	String	是	页面界面支付完成之后的商户跳转地址

        private String backNotifyUrl;//	String	是	支付结果异步回调地址

        private String settRate;//	String(16)	是	默认为渠道费率。如果需要高签，则必传。“0.003” 代表费率千分之3 最大支持五位小数

        private String settAffix;//	String(16)	是	固定单笔附加手续费固定值单位：分 交易手续费=totalFee*settleRate+setteAffix

        private String settBankCode;//	String	是	见4.1中的行号

        private String settAccNo;//	String	是	卡号

        private String settAccProvince;//	String	否	如：福建省

        private String settAccCity;//	String	否	如：厦门市

        private String address;//商戶地址


        private String settMobile;//	String	是	银行预留手机号

        private String transChannel;//交易渠道sl0 商旅 zb0 珠宝 bh0 百货 yc0 烟草 df0 代付

        private String timeStamp;//	String	是	时间戳例子:20161123164601时间误差不能超过12小时

        private String sign;//	String	是	签名签名规则,参数名ASCII码从小到大排序md5(key=value&key=value&key=密钥)

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getSettRate() {
            return settRate;
        }

        public void setSettRate(String settRate) {
            this.settRate = settRate;
        }

        public String getSettAffix() {
            return settAffix;
        }

        public void setSettAffix(String settAffix) {
            this.settAffix = settAffix;
        }

        public String getCvn2() {
            return cvn2;
        }

        public void setCvn2(String cvn2) {
            this.cvn2 = cvn2;
        }

        public String getExpire() {
            return expire;
        }

        public void setExpire(String expire) {
            this.expire = expire;
        }







        public String getIdNo() {
            return idNo;
        }

        public void setIdNo(String idNo) {
            this.idNo = idNo;
        }


        public String getTransChannel() {
            return transChannel;
        }

        public void setTransChannel(String transChannel) {
            this.transChannel = transChannel;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getMerOrderId() {
            return merOrderId;
        }

        public void setMerOrderId(String merOrderId) {
            this.merOrderId = merOrderId;
        }

        public String getTotalFee() {
            return totalFee;
        }

        public void setTotalFee(String totalFee) {
            this.totalFee = totalFee;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getAccType() {
            return accType;
        }

        public void setAccType(String accType) {
            this.accType = accType;
        }

        public String getAccName() {
            return accName;
        }

        public void setAccName(String accName) {
            this.accName = accName;
        }

        public String getAccNo() {
            return accNo;
        }

        public void setAccNo(String accNo) {
            this.accNo = accNo;
        }

        public String getBankCode() {
            return bankCode;
        }

        public void setBankCode(String bankCode) {
            this.bankCode = bankCode;
        }

        public String getIdType() {
            return idType;
        }

        public void setIdType(String idType) {
            this.idType = idType;
        }

        public String getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(String expireTime) {
            this.expireTime = expireTime;
        }

        public String getFrontNotifyUrl() {
            return frontNotifyUrl;
        }

        public void setFrontNotifyUrl(String frontNotifyUrl) {
            this.frontNotifyUrl = frontNotifyUrl;
        }

        public String getBackNotifyUrl() {
            return backNotifyUrl;
        }

        public void setBackNotifyUrl(String backNotifyUrl) {
            this.backNotifyUrl = backNotifyUrl;
        }



        public String getSettBankCode() {
            return settBankCode;
        }

        public void setSettBankCode(String settBankCode) {
            this.settBankCode = settBankCode;
        }

        public String getSettAccNo() {
            return settAccNo;
        }

        public void setSettAccNo(String settAccNo) {
            this.settAccNo = settAccNo;
        }

        public String getSettAccProvince() {
            return settAccProvince;
        }

        public void setSettAccProvince(String settAccProvince) {
            this.settAccProvince = settAccProvince;
        }

        public String getSettAccCity() {
            return settAccCity;
        }

        public void setSettAccCity(String settAccCity) {
            this.settAccCity = settAccCity;
        }

        public String getSettMobile() {
            return settMobile;
        }

        public void setSettMobile(String settMobile) {
            this.settMobile = settMobile;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }


    }
    /**
     * http帮助类
     * @author Administrator
     *
     */
    public static class HttpUtil {
        //超时时间毫秒
        private static final Integer CONNECTION_TIME_OUT = 20000;

        private static final String CHARSET = "UTF-8";

        /**
         * get通用请求方法
         * @param url
         * @return
         */
        public static String executeGet(String url){

            String result = "";

            if(url.startsWith("https")){
                result = httpsGet(url);
            }else{
                result = httpGet(url);
            }

            return result;
        }

        /**
         * post通用请求方法
         * @param url
         * @param param
         * @return
         */
        public static String executePost(String url,Map<String,String> param){
            String result = "";

            if(url.startsWith("https")){
                result = httpsPost(url,param);
            }else{
                result = httpPost(url,param);
            }

            return result;
        }




        /**
         * get请求
         * @param url 请求地址
         * @return
         */
        public static String httpGet(String url){
            HttpClient client = new DefaultHttpClient();

            HttpGet post = new HttpGet(url);
            String result = "";
            try {
                HttpResponse res = client.execute(post);
                result = EntityUtils.toString(res.getEntity(),"utf-8");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }
            return result;
        }

        /**
         * https get请求
         * @param url
         * @return
         */
        public static String httpsGet(String url){
            HttpClient client = new DefaultHttpClient();

            String result = "";
            X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            };

            SSLContext ctx;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{xtm}, null);

                //创建SSLSocketFactory
                SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
                client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
                HttpGet get = new HttpGet(url);
                HttpResponse httpResponse = client.execute(get);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        /**
         * https post请求
         * @param url 请求地址
         * @param param 参数
         * @return
         */
        public static String httpsPost(String url,Map<String,String> param){
            HttpClient client = new DefaultHttpClient();

            String result = "";
            X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            };

            SSLContext ctx;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{xtm}, null);

                //创建SSLSocketFactory
                SSLSocketFactory socketFactory = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
                HttpPost post = new HttpPost(url);

                //创建参数队列
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                if(param!=null&&param.size()!=0){
                    for (Map.Entry<String, String> set : param.entrySet()) {
                        String key = set.getKey();
                        String value = set.getValue()==null?"":set.getValue();
                        formparams.add(new BasicNameValuePair(key, value));
                    }
                }

                post.setEntity(new UrlEncodedFormEntity(formparams, HTTP.UTF_8));

                //result = EntityUtils.toString(post.getEntity());
                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        /**
         * https post请求
         * @param url 请求地址
         * @param param 参数
         * @return
         */
        public static String httpsPostV2(String url,Map<String,String> param){

            HttpClient client = getSecuredHttpClient(new DefaultHttpClient());
            String result = "";
            try {

                //创建参数队列
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                if(param!=null&&param.size()!=0){
                    for (Map.Entry<String, String> set : param.entrySet()) {
                        String key = set.getKey();
                        String value = set.getValue()==null?"":set.getValue();
                        formparams.add(new BasicNameValuePair(key, value));
                    }
                }
                HttpPost post = new HttpPost(url);
                post.setEntity(new UrlEncodedFormEntity(formparams, HTTP.UTF_8));

                //result = EntityUtils.toString(post.getEntity());
                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        private static DefaultHttpClient getSecuredHttpClient(HttpClient httpClient) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {

                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException
                    {
                        // TODO Auto-generated method stub

                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException
                    {
                        // TODO Auto-generated method stub

                    }

                    public X509Certificate[] getAcceptedIssuers()
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }

                };
                ctx.init(null, new TrustManager[] { tm }, null);
                SSLSocketFactory ssf = new SSLSocketFactory(ctx);
                ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                ClientConnectionManager ccm = httpClient.getConnectionManager();
                SchemeRegistry sr = ccm.getSchemeRegistry();
                //设置要使用的端口，默认是443
                sr.register(new Scheme("https", ssf, 443));
                return new DefaultHttpClient(ccm, httpClient.getParams());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        /**
         * https post请求
         * @param url 请求地址
         * @param param 参数
         * @return
         */
        public static String httpPost(String url,Map<String,String> param){

            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIME_OUT);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, CONNECTION_TIME_OUT);
            String result = "";
            try {

                HttpPost post = new HttpPost(url);

                // 创建参数队列
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                if(param!=null&&param.size()!=0){
                    for (Map.Entry<String, String> set : param.entrySet()) {
                        String key = set.getKey();
                        String value = set.getValue()==null?"":set.getValue();
                        formparams.add(new BasicNameValuePair(key, value));
                    }
                }

                post.setEntity(new UrlEncodedFormEntity(formparams, HTTP.UTF_8));

                //result = EntityUtils.toString(post.getEntity());

                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }


        /**
         * https post请求
         * @param url 请求地址
         * @return
         */
        public static String httpsPost(String url){

            HttpClient client = new DefaultHttpClient();

            String result = "";
            X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            };

            SSLContext ctx;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{xtm}, null);

                //创建SSLSocketFactory
                SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
                client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
                HttpPost post = new HttpPost(url);

                //result = EntityUtils.toString(post.getEntity());
                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }



        /**
         * http post请求
         * @param url 请求地址
         * @return
         */
        public static String httpPost(String url){

            HttpClient client = new DefaultHttpClient();

            String result = "";

            try {
                HttpPost post = new HttpPost(url);
                //result = EntityUtils.toString(post.getEntity());
                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }



        /**
         * json方式的https请求
         * @param url
         * @param content
         * @return
         */
        public static String httpsJsonPost(String url,String content){

            HttpClient client = new DefaultHttpClient();

            String result = "";
            X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            };

            SSLContext ctx;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{xtm}, null);

                //创建SSLSocketFactory
                SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
                client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
                HttpPost post = new HttpPost(url);
                StringEntity s = new StringEntity(content, "UTF-8");   // 中文乱码在此解决
                s.setContentType("application/json");
                post.setEntity(s);  //设置内容

                HttpResponse httpResponse = client.execute(post);

                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");

            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        /**
         * json方式的https请求
         * @param url
         * @param content 请求内容
         * @param head 请求头信息
         * @return
         */
        public static String httpsJsonPost(String url,String content,Map<String,String> head){

            HttpClient client = new DefaultHttpClient();

            String result = "";
            X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            };

            SSLContext ctx;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{xtm}, null);

                //创建SSLSocketFactory
                SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
                client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
                HttpPost post = new HttpPost(url);
                StringEntity s = new StringEntity(content, "UTF-8");   // 中文乱码在此解决
                s.setContentType("application/json");
                post.setEntity(s);  //设置内容

                //设置head
                if(head!=null&&head.size()!=0){
                    Object[] attr = head.keySet().toArray();
                    for(Object obj:attr){
                        String key = String.valueOf(obj);
                        String value = head.get(String.valueOf(obj));
                        post.addHeader(key,value);
                    }
                }

                HttpResponse httpResponse = client.execute(post);

                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");

            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        /**
         * json方式的http请求
         * @param url
         * @param content 请求内容
         * @return
         */
        public static String httpJsonPost(String url,String content){

            HttpClient client = new DefaultHttpClient();

            String result = "";

            try {
                HttpPost post = new HttpPost(url);
                StringEntity s = new StringEntity(content, "UTF-8");   // 中文乱码在此解决
                s.setContentType("application/json");
                post.setEntity(s);  //设置内容
                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        /**
         * json方式的http请求
         * @param url
         * @param content 请求内容
         * @return
         */
        public static String httpJsonPost(String url,String content,String contentType){

            HttpClient client = new DefaultHttpClient();

            String result = "";

            try {
                HttpPost post = new HttpPost(url);
                StringEntity s = new StringEntity(content, "UTF-8");   // 中文乱码在此解决
                s.setContentType(contentType);
                post.setEntity(s);  //设置内容
                HttpResponse httpResponse = client.execute(post);
                result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(client!=null){
                    client.getConnectionManager().shutdown();
                }
            }

            return result;
        }

        /**
         * json提交
         * @return
         */
        public static String executePostJSON(String url,String content){
            String result = "";

            if(url.startsWith("https")){
                result = httpsJsonPost(url,content);
            }else{
                result = httpJsonPost(url,content);
            }

            return result;
        }

        /**
         * 上传图片
         * @param url
         * @param map
         * @param files
         * @return
         * @throws Exception
         */
        public static String httpPostFile(String url,Map<String, String> map, LinkedHashMap<String,File> files) throws Exception{
            try {
                if(StringUtils.isEmpty(url) || null == map || map.isEmpty()){
                    return null;
                }
                //创建POST请求
                HttpPost post = new HttpPost(url);

                MultipartEntity entity = new MultipartEntity();
                //请求参数
                for(String key : map.keySet()){
                    entity.addPart(key, new StringBody(map.get(key), Charset.forName("UTF-8")));
                }

                for (String key : files.keySet()){
                    entity.addPart(key,new FileBody(files.get(key)));
                }


                post.setEntity(entity);

                //发送请求
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);
                if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                    throw new Exception("请求失败！");
                }

                HttpEntity resEntity = response.getEntity();
                return null == resEntity ? "" : EntityUtils.toString(resEntity,CHARSET);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception(e.getMessage());
            }
        }
    }

//    @Test
    public void testSubOrder() {
//        String url = "http://localhost:8090/HzGateway/kjv3/FastPay/subOrder.do";
        String url = "http://www.allforbenefit.com:8083/HzGateway/kjv3/FastPay/subOrder.do";
        UnionFastPay unionFastPay = new UnionFastPay();
        unionFastPay.setAccName("段力");
        unionFastPay.setAccNo("6259588730212343");
        unionFastPay.setAccType("CREDIT");
        unionFastPay.setAddress("上海");
        unionFastPay.setBackNotifyUrl("http://www.baidu.com");
        unionFastPay.setBankCode("531290000011");
        unionFastPay.setBody("珠宝首饰");
        unionFastPay.setCvn2("750");
        unionFastPay.setExpire("0925");
        unionFastPay.setExpireTime("15");
        unionFastPay.setIdNo("431081191212123776");
        unionFastPay.setIdType("01");
        unionFastPay.setFrontNotifyUrl("http://www.baidu.com");
        unionFastPay.setMerchantId("99928989230000");
        unionFastPay.setMerOrderId("TE" + System.currentTimeMillis());
        unionFastPay.setMobile("18817222007");

        unionFastPay.setSettAccCity("上海市");
        unionFastPay.setSettAccNo("6226091212345643");
        unionFastPay.setSettAccProvince("上海市");
        unionFastPay.setSettAffix("100");
        unionFastPay.setSettMobile("18817221234");
        unionFastPay.setSettBankCode("308584000013");
        unionFastPay.setSettRate(0.0040 + "");
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        unionFastPay.setTimeStamp(simpleDateFormat.format(new Date()));
        unionFastPay.setTotalFee("18000");
        unionFastPay.setTransChannel("sl0");

        Map<String, Object> param = JSONObject.parseObject(JSONObject.toJSONString(unionFastPay), Map.class);
        LOG.info("LD:" + param.toString());
        Map<String, String> fs = parseMap(param);

        fs.put("sign", createPaySign(fs));


        String res = HttpUtil.executePost(url, fs);
        System.out.println("res:" + res);
        com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(res);
        System.out.println(js.getString("data"));
    }

    private Map<String, String> parseMap(Map<String, Object> map) {
        Map<String, String> fsMap = new TreeMap<>();
        for (Iterator<Map.Entry<String, Object>> its = map.entrySet().iterator(); its.hasNext();
                ) {
            Map.Entry<String, Object> entry = its.next();
            if (entry.getValue() != null && !StringUtils.isEmpty(entry.getValue().toString())) {
                fsMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return fsMap;
    }

    public static String createPaySign(Map<String, String> param) {

        StringBuffer signStr = new StringBuffer();
        int size = param.entrySet().size();
        int i = 1;
        for (Iterator<Map.Entry<String, String>> its = param.entrySet().iterator(); its.hasNext();
                ) {
            Map.Entry<String, String> entry = its.next();
            if (entry.getValue() != null && !StringUtils.isEmpty(entry.getValue().toString())) {
                signStr.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("&");
            }
        }

        signStr.append("key=").append("Aa123333");
        System.out.println(signStr);
        String md5Sign = Md5Utils.signature(signStr.toString()).toUpperCase();

        return md5Sign;
    }






}