package cn.jh.facade.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.jh.common.utils.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import cn.jh.facade.util.AlipayAPIClientFactory;
import cn.jh.facade.util.AlipayServiceEnvConstants;
import cn.jh.facade.util.MathRandom;
import cn.jh.facade.util.RC4Util;
import cn.jh.facade.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class PaymentManageService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentManageService.class);

    @Autowired
    Util util;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/alipay/auth/back")
    public String autoAlipayAuthdirct(HttpServletRequest request,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam(value = "auth_code", required = false, defaultValue = "0") String code, Model model) {

        LOG.info("aplipay   phone = " + phone);
        LOG.info("aplipay   auth_code = " + code);

        AlipaySystemOauthTokenRequest oauthTokenRequest = new AlipaySystemOauthTokenRequest();
        oauthTokenRequest.setCode(code);
        oauthTokenRequest.setGrantType(AlipayServiceEnvConstants.GRANT_TYPE);
        AlipayClient alipayClient = AlipayAPIClientFactory.getAlipayClient();
        try {
            AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(oauthTokenRequest);

            // 成功获得authToken
            if (null != oauthTokenResponse && oauthTokenResponse.isSuccess()) {

                String alipayuserid = oauthTokenResponse.getUserId();

                System.out.println("获取用户信息成功：" + alipayuserid);

                model.addAttribute("openid", alipayuserid);
                model.addAttribute("phone", phone);

            } else {
                // 这里仅是简单打印， 请开发者按实际情况自行进行处理
                // System.out.println("authCode换取authToken失败");
                LOG.error("authCode换取authToken失败");
                return "error";
            }

        } catch (AlipayApiException e1) {
            LOG.error("==========请求支付宝认证服务器异常===========" + e1);
            return "error";
        }

        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        JSONObject jsonObject = null;
        JSONObject resultObju = null;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            return "error";
        }
        if (resultObju == null) {
            return "error";
        }

        String brandid = "0";
        if (resultObju.containsKey("brandId")) {
            brandid = resultObju.getString("brandId");
        }
        /***
         * 获取贴牌手机号
         *
         ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
        restTemplate = new RestTemplate();
        JSONObject resultObjb = null;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObjb = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
            return "error";
        }
        if (resultObjb == null) {
            return "error";
        }
        String brandPhone = "";
        if (resultObjb.containsKey("brandPhone")) {
            brandPhone = resultObjb.getString("brandPhone");
        }
        model.addAttribute("brandPhone", brandPhone);

        /*****
         * 获取商铺信息
         ****/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/shops/query/uid";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userid", resultObju.getString("id"));
        restTemplate = new RestTemplate();
        JSONObject resultObjs = null;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObjs = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/shops/query/uid查询用户异常===========" + e);
            return "error";
        }
        if (resultObjs == null) {
            return "error";
        }
        String shopname = "";
        if (resultObjs.containsKey("name")) {
            shopname = resultObjs.getString("name");
        }
        model.addAttribute("shopname", shopname);

        if (brandid.equals("90") || brandid.equals("108")) {
            return "inputalipaymoney_lsf";
        }
        return "inputalipaymoney";

    }

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String index(HttpServletRequest request) {
        return "inputalipaymoney";
    }

    /**
     * 固态二维码充值
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/auth/back")
    public String autoAuthdirct(HttpServletRequest request,
                                @RequestParam(value = "code", required = false, defaultValue = "0") String code,
                                @RequestParam(value = "auth_code", required = false, defaultValue = "0") String authcode,
                                @RequestParam(value = "phone") String phone, Model model) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        JSONObject jsonObject = null;
        JSONObject resultObju = null;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常==========");
            return "error";
        }

        if (resultObju == null) {
            return "error";
        }
        String brandid = "0";
        if (resultObju.containsKey("brandId")) {
            brandid = resultObju.getString("brandId");
        }
        /***
         * 获取贴牌手机号
         *
         ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
        restTemplate = new RestTemplate();
        JSONObject resultObjb = null;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObjb = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========v1.0/user/brand/query/id查询用户异常===========" + e);
            return "error";
        }
        if (resultObjb == null) {
            return "error";
        }

        String brandPhone = "";
        if (resultObjb.containsKey("brandPhone")) {
            brandPhone = resultObjb.getString("brandPhone");
        }
        model.addAttribute("brandPhone", brandPhone);
        /*****
         * 获取商铺信息
         ****/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/shops/query/uid";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userid", resultObju.getString("id"));
        restTemplate = new RestTemplate();
        JSONObject resultObjs = null;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObjs = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/shops/query/uid查询用户商铺异常===========" + e);
            return "error";
        }
        if (resultObjs == null) {
            return "error";
        }

        String shopname = "";
        if (resultObjs.containsKey("name")) {
            shopname = resultObjs.getString("name");
        }
        model.addAttribute("shopname", shopname);

        if (request.getHeader("user-agent").indexOf("MicroMessenger") > 0) {

            String tempurl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx964293b158c6e6af&secret=57206890198734345a8f8f98dfaec682&code="
                    + code + "&grant_type=authorization_code";
            // String tempurl=
            // "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx2ebb475f2cea25e2&secret=169955623c4400a376b1ffb055b17ca0&code="+code+"&grant_type=authorization_code";

            LOG.info("Current URL ............................" + tempurl);

            restTemplate = new RestTemplate();
            String resultStr;
            String openid = "";
            try {
                resultStr = restTemplate.getForObject(tempurl, String.class);
                jsonObject = JSONObject.fromObject(resultStr);
                openid = jsonObject.getString("openid");
            } catch (Exception e) {
                LOG.error("==========请求微信认证服务器异常===========" + e);
                return "error";
            }
            if ("".equals(openid)) {
                return "error";
            }

            model.addAttribute("openid", openid);
            model.addAttribute("phone", phone);

            if (brandid.equals("90") || brandid.equals("108")) {
                return "inputmoney_lsf";
            }
            return "inputmoney";

        } else if (request.getHeader("user-agent").indexOf("Alipay") > 0) {

            LOG.info("aplipay   phone = " + phone);
            LOG.info("aplipay   auth_code = " + authcode);

            AlipaySystemOauthTokenRequest oauthTokenRequest = new AlipaySystemOauthTokenRequest();
            oauthTokenRequest.setCode(authcode);
            oauthTokenRequest.setGrantType(AlipayServiceEnvConstants.GRANT_TYPE);
            AlipayClient alipayClient = AlipayAPIClientFactory.getAlipayClient();
            try {
                AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(oauthTokenRequest);

                // 成功获得authToken
                if (null != oauthTokenResponse && oauthTokenResponse.isSuccess()) {

                    String alipayuserid = oauthTokenResponse.getUserId();

                    System.out.println("获取用户信息成功：" + alipayuserid);

                    model.addAttribute("openid", alipayuserid);
                    model.addAttribute("phone", phone);

                } else {
                    // 这里仅是简单打印， 请开发者按实际情况自行进行处理
                    // System.out.println("authCode换取authToken失败");
                    LOG.error("authCode换取authToken失败");
                    return "error";
                }

            } catch (AlipayApiException e1) {
                LOG.error("==========请求支付宝认证服务器异常===========" + e1);
                return "error";
            }

            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/query/phone";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", phone);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObju = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
                return "error";
            }
            if (resultObju == null) {
                return "error";
            }

            brandid = "0";
            if (resultObju.containsKey("brandId")) {
                brandid = resultObju.getString("brandId");
            }
            /***
             * 获取贴牌手机号
             *
             ***/
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandid;
            restTemplate = new RestTemplate();
            try {
                result = restTemplate.getForObject(url, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObjb = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
                return "error";
            }
            if (resultObjb == null) {
                return "error";
            }
            brandPhone = "";
            if (resultObjb.containsKey("brandPhone")) {
                brandPhone = resultObjb.getString("brandPhone");
            }
            model.addAttribute("brandPhone", brandPhone);

            /*****
             * 获取商铺信息
             ****/
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/shops/query/uid";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userid", resultObju.getString("id"));
            restTemplate = new RestTemplate();
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObjs = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/shops/query/uid查询用户异常===========" + e);
                return "error";
            }
            if (resultObjs == null) {
                return "error";
            }
            shopname = "";
            if (resultObjs.containsKey("name")) {
                shopname = resultObjs.getString("name");
            }
            model.addAttribute("shopname", shopname);

            if (brandid.equals("90") || brandid.equals("108")) {
                return "inputalipaymoney_lsf";
            }
            return "inputalipaymoney";

        } else {
            return "error";
        }

    }

    /**
     * 固态二维码充值
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/fixedcode/topup")
    public String fixedtopup(HttpServletRequest request, @RequestParam(value = "phone") String phone, Model model) {
        LOG.info("user_agent" + request.getHeader("user-agent"));
        // 微信回调的URL
        String tempurl = "http://106.14.214.62/v1.0/facade/auth/back?phone=" + phone;

        // 支付宝回调的URL = "";
        String alipayURL = "http://106.14.214.62/v1.0/facade/auth/back?phone=" + phone;

        /*
         * WexinOpenid weixinOpenid = new WexinOpenid();
         * map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
         * weixinOpenid.setRespType("1");
         * weixinOpenid.setRespResult(redirect_url);
         * map.put(CommonConstants.RESP_MESSAGE, "跳转"); JSONObject
         * jsonObj=JSONObject.fromObject(weixinOpenid);
         * map.put(CommonConstants.RESULT, jsonObj.toString());
         */

        if (request.getHeader("user-agent").indexOf("MicroMessenger") > 0) {

            String redirect_uri = "";

            try {
                redirect_uri = java.net.URLEncoder.encode(tempurl, "utf-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("uri-UTF-8编码异常" + e);
                return "error";
            }
            // 定义跳转微信的URL
            String redirect_url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx964293b158c6e6af&redirect_uri="
                    + redirect_uri + "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
            // String
            // redirect_url="https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx2ebb475f2cea25e2&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=1#wechat_redirect";

            return "redirect:" + redirect_url;

        } else if (request.getHeader("user-agent").indexOf("Alipay") > 0) {

            String redirect_uri = "";

            try {
                redirect_uri = java.net.URLEncoder.encode(alipayURL, "utf-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("uri-UTF-8编码异常" + e);
                return "error";
            }
            // 定义跳转微信的URL
            String redirect_url = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2017031606249487&scope=auth_base&redirect_uri="
                    + redirect_uri;
            // String
            // redirect_url="https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2017070807685815&scope=auth_base&redirect_uri="+redirect_uri;

            return "redirect:" + redirect_url;

        } else if (request.getHeader("user-agent").indexOf("QQ") > 0) {
            /** QQ 扫码 */
            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("user", "error url request!");
            String url = uri.toString() + "/v1.0/user/query/phone";
            /** 根据的用户手机号码查询用户的基本信息 */
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", phone);
            JSONObject jsonObject = null;
            JSONObject resultObju = null;
            String result;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObju = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
                return "error";
            }
            if (resultObju == null) {
                return "error";
            }
            model.addAttribute("phone", phone);
            String brandid = "0";
            if (resultObju.containsKey("brandId")) {
                brandid = resultObju.getString("brandId");
            }
            /*****
             * 获取商铺信息
             ****/
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/shops/query/uid";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userid", resultObju.getString("id"));
            restTemplate = new RestTemplate();
            JSONObject resultObjs = null;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObjs = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/shops/query/uid查询用户异常===========" + e);
                return "error";
            }
            if (resultObjs == null) {
                return "error";
            }
            String shopname = "";
            if (resultObjs.containsKey("name")) {
                shopname = resultObjs.getString("name");
            }

            model.addAttribute("shopname", shopname);
            if (brandid.equals("90") || brandid.equals("109")) {
                return "inputmoney_qq_lsf";
            }
            return "inputmoney_qq";
        } else {
            return "error";
        }

    }

    /**
     * 充值
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/topup")
    public @ResponseBody
    Object topup(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "phone") String phone,
                 @RequestParam(value = "amount") String amount,
                 @RequestParam(value = "channe_tag") String channeltag,
                 @RequestParam(value = "order_desc") String orderdesc,
                 @RequestParam(value = "brand_id", required = false, defaultValue = "-1") String brand_id,
                 @RequestParam(value = "bank_card", required = false) String bankcard,
                 @RequestParam(value = "openid", required = false) String openid,
                 @RequestParam(value = "auth_code", required = false) String authCode,
                 @RequestParam(value = "remark", required = false) String remark) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";

        //String url = "http://risk/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("operation_type", "1");// 0 表示登陆无法进行 1表示无法充值 2 表示无法提现 // 3 无法支付
        JSONObject jsonObject;
        String rescode = "";
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("接口/v1.0/risk/blackwhite/query/phone--RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户黑白名单出现异常,请稍后重试!");
            return map;
        }
        if (rescode != null) {
            if (!CommonConstants.SUCCESS.equalsIgnoreCase(rescode)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
                map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
                return map;
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户黑白名单出现异常,请稍后重试!");
            return map;
        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brand_id);
        restTemplate = new RestTemplate();
        JSONObject resultObju = null;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("接口/v1.0/user/query/phone--RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户信息出现异常,请稍后重试!");
            return map;
        }
        if (resultObju == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户信息出现异常,请稍后重试!");
            return map;
        }
        long userid;
        if (resultObju.containsKey("id")) {
            userid = resultObju.getLong("id");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在，请检查您的账号");
            return map;
        }
        String brandId = resultObju.getString("brandId");
        if (!brandId.equals("3") && !brandId.equals("90")) {

            JSONObject resultChannel = null;
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/channel/query";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channel_tag", channeltag);
            restTemplate = new RestTemplate();
            String paymentStatus = null;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/user/channel/query--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultChannel = jsonObject.getJSONObject("result");
                paymentStatus = resultChannel.getString("paymentStatus");
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/channel/query查询用户异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
                return map;
            }

            //判断如果通道的paymentStatus为0,则直接返回错误信息
            if (paymentStatus == null || paymentStatus.equals("0")) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "当前支付通道已限额,请选择其他支付通道");
                return map;
            }
            // 判断是否在通道开放时间
            String strStartTime = "";
            String strEndTime = "";
            strStartTime = resultChannel.getString("startTime");
            strEndTime = resultChannel.getString("endTime");
            String everyDayMaxLimit = resultChannel.getString("everyDayMaxLimit");
            boolean isOpenTime = compareChannelTime(strStartTime, strEndTime);
            if (!isOpenTime) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,
                        "亲.现在不是通道开放时间,该通道开放时间为:" + strStartTime + "~" + strEndTime + ",请在该时间段进行交易!");
                return map;
            }

            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/payment/querytransaction/sumamount";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userId", String.valueOf(userid));
            requestEntity.add("orderStatus", "1");
            requestEntity.add("channelTag", channeltag);
            restTemplate = new RestTemplate();
            LOG.info("接口/v1.0/transactionclear/payment/querytransaction/sumamount--参数================" + requestEntity.toString());
            long everyDayTransaction;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/transactionclear/payment/querytransaction/sumamount--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                String respCode = jsonObject.getString("resp_code");

                if ("000000".equals(respCode)) {
                    everyDayTransaction = jsonObject.getLong("result");
                } else {
                    everyDayTransaction = 100;
                }

                BigDecimal bigDecimal = new BigDecimal(everyDayMaxLimit);
                BigDecimal bigDecimal1 = new BigDecimal(everyDayTransaction);

                if (bigDecimal1.compareTo(bigDecimal) > 0) {

                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "您在该通道的每日交易总金额已达上限,请选择其他通道继续交易!");
                    return map;
                }

            } catch (Exception e) {
                LOG.error("==========/v1.0/transactionclear/payment/querytransaction/sumamount===========" + e);

                return ResultWrap.init(CommonConstants.FALIED, "查询单日交易出现异常!");
            }

            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/bank/default/userid";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("user_id", resultObju.getString("id"));
            result = restTemplate.postForObject(url, requestEntity, String.class);

            LOG.info("RESULT================" + result);
            JSONObject resultObj;
            try {
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
            } catch (Exception e) {
                LOG.error("查询默认结算卡出错");

                return ResultWrap.init(CommonConstants.FALIED, "查询默认结算卡有误");
            }

            if (resultObj.isNullObject()) {

                return ResultWrap.init(CommonConstants.FALIED, "没有默认结算卡,请先设置默认结算卡!");
            }
            //借记卡银行卡名称
            String bankName = resultObj.getString("bankName");
            //借记卡身份证号码
            String idcard = resultObj.getString("idcard");

            LOG.info("bankcard=====" + bankcard);

            //到账卡银行筛选=======
            restTemplate = new RestTemplate();
            //url = "http://101.132.255.217/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname";
            url = "http://127.0.0.1/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("channelTag", channeltag);
            requestEntity.add("bankName", util.changeBankName(bankName));
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname  RESULT================" + result);
            try {
                jsonObject = JSONObject.fromObject(result);

                if (jsonObject.getString("resp_code").equals("888888")) {

                    String string = jsonObject.getString("result");

                    string = string.substring(1, string.length() - 1).replace("\"", "").replace(",", "、");

                    LOG.info("string======" + string);

                    return ResultWrap.init(CommonConstants.FALIED, "该通道目前支持的到账卡银行: [" + string + "], 请及时更换默认到账卡为以上银行后重新发起交易!");
                }
            } catch (Exception e) {
                LOG.error("查询通道支持默认结算卡出错======", e);

                return ResultWrap.init(CommonConstants.FALIED, "查询通道支持默认结算卡异常,请稍后重试!");
            }

            if (bankcard != null) {

                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/bank/default/cardnoand/type";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("cardno", bankcard);
                requestEntity.add("type", "0");
                requestEntity.add("userId", userid + "");
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/user/bank/default/cardnoand/type--RESULT================" + result);
                try {
                    jsonObject = JSONObject.fromObject(result);
                    resultObj = jsonObject.getJSONObject("result");
                } catch (Exception e) {
                    LOG.error("查询银行卡信息出错");

                    return ResultWrap.init(CommonConstants.FALIED, "查询银行卡信息有误");
                }
                //信用卡银行名称
                String cardName = resultObj.getString("bankName");
                String cardType = resultObj.getString("cardType");
                String idCard = resultObj.getString("idcard");
                String nature = resultObj.getString("nature");

                if (!idCard.equalsIgnoreCase(idcard)) {
                    LOG.error("出款卡和到账卡身份证号码不一致======");

                    return ResultWrap.init(CommonConstants.FALIED, "抱歉,您的充值卡和提现卡身份证号码不一致,无法继续交易!");
                }

                if (cardType.contains("借贷")) {
                    LOG.error("该卡为借贷合一卡======");

                    return ResultWrap.init(CommonConstants.FALIED, "支付通道暂不支持借贷合一卡,请及时更换!");
                }

                if (nature.contains("借记")) {
                    LOG.error("借记卡不支持交易=====");

                    return ResultWrap.init(CommonConstants.FALIED, "抱歉,无法使用借记卡进行交易,请及时更换信用卡!");
                }

                if ("JF_QUICK".equals(channeltag) || "JFX_QUICK1".equals(channeltag) || "JFT_QUICK2".equals(channeltag) || "TYT_QUICK1".equals(channeltag) || "HZN_QUICK".equals(channeltag) || "JFS_QUICK".equals(channeltag)) {
                    if ("准贷记卡".equals(nature)) {
                        LOG.error("部分通道不支持准贷记卡======");

                        return ResultWrap.init(CommonConstants.FALIED, "该支付通道暂不支持准贷记卡,请及时更换!");
                    }
                }

				/*// 查询银联快捷4是否进件成功
				if ("YB_QUICK".equals(channeltag)) {
					// 判断银联11支持该银行否
					if(!cardName.contains("平安")){
						// 金额不超过5000
						if (Double.valueOf(amount)<=Double.valueOf("5000")) {
							restTemplate = new RestTemplate();
							uri = util.getServiceUrl("paymentchannel", "error url request!");
							url = uri.toString() + "/v1.0/paymentchannel/ybfour/querymerchantbyidcard";
							requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("idCard", idCard);
							result = restTemplate.postForObject(url, requestEntity, String.class);
							LOG.info("接口/v1.0/paymentchannel/ybfour/querymerchantbyidcard--RESULT================" + result);
							jsonObject = JSONObject.fromObject(result);
							String responsecode = jsonObject.getString("resp_code");
							if ("999999".equals(responsecode)) {
								channeltag = "YB_QUICKN";
							}
						}
					}
				}
				// 查询银联快捷5是否进件成功
				if("YB_PAY".equals(channeltag)){
					// 判断银联11支持该银行否
					if(!cardName.contains("平安") || !cardName.contains("深圳")
							|| !cardName.contains("农业") || !cardName.contains("邮政")){
						// 金额不超过5000
						if (Double.valueOf(amount)<=Double.valueOf("5000")) {
							restTemplate = new RestTemplate();
							uri = util.getServiceUrl("paymentchannel", "error url request!");
							url = uri.toString() + "/v1.0/paymentchannel/ybfive/querymerchantbyidcard";
							requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("idCard", idCard);
							result = restTemplate.postForObject(url, requestEntity, String.class);
							LOG.info("接口/v1.0/paymentchannel/ybfive/querymerchantbyidcard--RESULT================" + result);
							jsonObject = JSONObject.fromObject(result);
							String responsecode = jsonObject.getString("resp_code");
							if ("999999".equals(responsecode)) {
								channeltag = "YB_QUICKN";
							}
						}
					}
				}*/

                uri = util.getServiceUrl("paymentchannel", "error url request!");
                url = uri.toString() + "/v1.0/paymentchannel/querysupportbank/byname";
                requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("bankName", Util.queryBankNameByBranchName(cardName));
                requestEntity.add("type", "信用卡");
                requestEntity.add("channelTag", channeltag);
                JSONArray jsonArray = null;
                String respCode = null;
                try {
                    result = restTemplate.postForObject(url, requestEntity, String.class);
                    LOG.info("RESULT================" + result);
                    jsonObject = JSONObject.fromObject(result);
                    respCode = jsonObject.getString("resp_code");
                } catch (Exception e) {

                    return ResultWrap.init(CommonConstants.FALIED, "获取通道支持银行出现异常,请稍后重试!");
                }

                if (CommonConstants.SUCCESS.equals(respCode)) {
                    jsonArray = jsonObject.getJSONArray("result");
                } else {

                    return ResultWrap.init(CommonConstants.FALIED, "该通道暂不支持此银行卡,请及时更换银行卡!");
                }

                if (jsonArray != null && jsonArray.size() > 0) {

                    LOG.info("jsonArray======" + jsonArray);

                    JSONObject object = (JSONObject) jsonArray.get(0);

                    String singleMaxLimit = object.getString("singleMaxLimit");
                    String singleMinLimit = object.getString("singleMinLimit");

                    if (Double.valueOf(amount) > Double.valueOf(singleMaxLimit) || Double.valueOf(amount) < Double.valueOf(singleMinLimit)) {

                        return ResultWrap.init(CommonConstants.FALIED, "该通道" + cardName + "单笔交易限额为： " + singleMinLimit + "~" + singleMaxLimit + "元,请重新核对输入金额!");
                    }

                }

                String singleMinLimit = resultChannel.getString("singleMinLimit");
                String singleMaxLimit = resultChannel.getString("singleMaxLimit");

                if (Double.valueOf(amount) < Double.valueOf(singleMinLimit) || Double.valueOf(amount) > Double.valueOf(singleMaxLimit)) {

                    return ResultWrap.init(CommonConstants.FALIED, "该通道交易金额限制为: " + singleMinLimit + "~" + singleMaxLimit + "元,请重新核对输入金额!");
                }

            }

        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "0");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("openid", openid);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("remark", remark);
        requestEntity.add("bank_card", bankcard);
        requestEntity.add("brand_id", brand_id);
        JSONObject resultObj;
        String order;
        long brandid;
        try {
            restTemplate = new RestTemplate();
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("接口/v1.0/transactionclear/payment/add--RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单异常,请稍后重试!");
            return map;
        }

        // 添加订单失败原因提示
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(jsonObject.getString(CommonConstants.RESP_CODE))) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单失败===========失败原因:"
                    + (jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : null));
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, (jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "生成订单失败,请重新下单!"));
            return map;
        }

        resultObj = jsonObject.getJSONObject("result");
        order = resultObj.getString("ordercode");
        brandid = resultObj.getLong("brandid");
        String responsecode = null;
        String responsemessage = "";
        if ("YB_PAY".equalsIgnoreCase(channeltag) || "YB_PAY2".equalsIgnoreCase(channeltag)) {
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/registerAuth/query";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("mobile", phone);
            LOG.info("接口/v1.0/paymentchannel/registerAuth/query--参数================" + requestEntity.toString());
            String customerNumber = null;
            try {
                restTemplate = new RestTemplate();
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/paymentchannel/registerAuth/query--result================" + result.toString());
                jsonObject = JSONObject.fromObject(result);
                if (!"null".equals(jsonObject.getString("result")) && null != jsonObject.getString("result")
                        && !"".equals(jsonObject.getString("result"))) {
                    JSONObject obj = JSONObject.fromObject(jsonObject.getString("result"));
                    customerNumber = obj.getString("customerNumber");

                }
            } catch (Exception e) {
                LOG.error("==========/v1.0/paymentchannel/registerAuth/query查询实名异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,
                        "".equals(responsemessage) ? "亲.网络出错了哦,臣妾已经尽力了,请重试~" : responsemessage);
                return map;
            }
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/topup/request";

            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userid", String.valueOf(userid));
            requestEntity.add("amount", amount);
            requestEntity.add("ordercode", order);
            requestEntity.add("extra", customerNumber);
            requestEntity.add("brandcode", brandid + "");
            requestEntity.add("orderdesc", orderdesc);
            requestEntity.add("channel_tag", channeltag);
            LOG.info("接口/v1.0/paymentchannel/topup/request--参数================" + requestEntity.toString());
            try {
                restTemplate = new RestTemplate();
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/paymentchannel/topup/request--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                responsecode = jsonObject.getString("resp_code");
                responsemessage = jsonObject.getString("resp_message");
            } catch (Exception e) {
                LOG.error("==========/v1.0/paymentchannel/topup/request支付请求异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,
                        "".equals(responsemessage) ? "亲.网络出错了哦,臣妾已经尽力了,请重试~" : responsemessage);
                return map;
            }
        } else if ("RECHARGE_ACCOUNT".equals(channeltag)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            if (jsonObject.containsKey("result"))
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/topup/rechargetoaccount/topage?userid=" + String.valueOf(userid) + "&amount=" + amount + "&ordercode=" + order + "&extra=" + authCode + "&brandcode=" + brandid + "" + "&orderdesc=" + URLEncoder.encode(orderdesc, "UTF-8") + "&channel_tag=" + channeltag);

            return map;

        } else {
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/topup/request";

            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userid", String.valueOf(userid));
            requestEntity.add("amount", amount);
            requestEntity.add("ordercode", order);
            requestEntity.add("extra", orderdesc);
            requestEntity.add("brandcode", brandid + "");
            requestEntity.add("orderdesc", orderdesc);
            requestEntity.add("channel_tag", channeltag);
            LOG.info("接口/v1.0/paymentchannel/topup/request--参数================" + requestEntity.toString());
            try {
                restTemplate = new RestTemplate();
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/paymentchannel/topup/request--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                responsecode = jsonObject.getString("resp_code");
                responsemessage = jsonObject.getString("resp_message");
            } catch (Exception e) {
                LOG.error("==========/v1.0/paymentchannel/topup/request支付请求异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,
                        "".equals(responsemessage) ? "请求支付出现异常,请稍后重试!" : responsemessage);
                return map;
            }
        }

        if (!responsecode.equalsIgnoreCase("000000")) {
            map.put(CommonConstants.RESP_CODE, responsecode);
            map.put(CommonConstants.RESP_MESSAGE, responsemessage);
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            if (jsonObject.containsKey("result"))
                map.put(CommonConstants.RESULT, jsonObject.getString("result"));
        }

        return map;

    }


    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/topup/new")
    public @ResponseBody
    Object topupNew(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "phone") String phone,
                    @RequestParam(value = "userId") String userId,
                    @RequestParam(value = "amount") String amount,
                    @RequestParam(value = "channeTag") String channelTag,
                    @RequestParam(value = "orderDesc") String orderDesc,
                    @RequestParam(value = "brandId", required = false, defaultValue = "-1") String brandId,
                    @RequestParam(value = "bankCard", required = false) String bankcard,
                    @RequestParam(value = "creditBankName", required = false) String creditBankName,
                    @RequestParam(value = "openid", required = false) String openid,
                    @RequestParam(value = "auth_code", required = false) String authCode,
                    @RequestParam(value = "remark", required = false) String remark) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        String url = "http://user/v1.0/user/bank/default/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userId);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject resultObj;
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("查询默认结算卡出错");

            return ResultWrap.init(CommonConstants.FALIED, "查询默认结算卡有误");
        }

        //借记卡银行卡名称
        String bankName = resultObj.getString("bankName");
        String debitBankCard = resultObj.getString("cardNo");

        //到账卡银行筛选=======
        RestTemplate rt = new RestTemplate();
        url = "http://101.132.255.217/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname";
        //url = "http://localhost/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("channelTag", channelTag);
        requestEntity.add("bankName", util.changeBankName(bankName));
        result = rt.postForObject(url, requestEntity, String.class);
        LOG.info("/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname  RESULT================" + result);
        try {
            jsonObject = JSONObject.fromObject(result);

            if (jsonObject.getString("resp_code").equals("888888")) {

                String string = jsonObject.getString("result");

                string = string.substring(1, string.length() - 1).replace("\"", "").replace(",", "、");

                LOG.info("string======" + string);

                return ResultWrap.init(CommonConstants.FALIED, "该通道目前支持的到账卡银行: [" + string + "], 请及时更换默认到账卡为以上银行后重新发起交易!");
            }
        } catch (Exception e) {
            LOG.error("查询通道支持默认结算卡出错======", e);

            return ResultWrap.init(CommonConstants.FALIED, "查询通道支持默认结算卡异常,请稍后重试!");
        }

        /** 调用下单，需要得到用户的订单信息 */
        url = "http://transactionclear/v1.0/transactionclear/payment/addnew";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "0");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channelTag", channelTag);
        requestEntity.add("desc", orderDesc);
        requestEntity.add("remark", remark);
        requestEntity.add("bankCard", bankcard);
        requestEntity.add("brandId", brandId);
        requestEntity.add("creditBankName", creditBankName);
        requestEntity.add("debitBankCard", debitBankCard);
        requestEntity.add("debitBankName", bankName);
        String order;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("接口/v1.0/transactionclear/payment/add--RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单异常,请稍后重试!");

            return map;
        }

        // 添加订单失败原因提示
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(jsonObject.getString(CommonConstants.RESP_CODE))) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单失败===========失败原因:" + (jsonObject.containsKey(CommonConstants.RESP_MESSAGE) ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : null));

            return ResultWrap.init(CommonConstants.FALIED, (jsonObject.containsKey(CommonConstants.RESP_MESSAGE) ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "生成订单失败,请重新下单!"));
        }

        resultObj = jsonObject.getJSONObject("result");
        order = resultObj.getString("ordercode");
        String responseCode = null;
        String responseMessage = "";
        if ("RECHARGE_ACCOUNT".equals(channelTag)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            if (jsonObject.containsKey("result"))
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/topup/rechargetoaccount/topage?userid=" + String.valueOf(userId) + "&amount=" + amount + "&ordercode=" + order + "&extra=" + authCode + "&brandcode=" + brandId + "" + "&orderdesc=" + URLEncoder.encode(orderDesc, "UTF-8") + "&channel_tag=" + channelTag);

            return map;

        } else {
            url = "http://paymentchannel/v1.0/paymentchannel/topup/request";

            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userid", String.valueOf(userId));
            requestEntity.add("amount", amount);
            requestEntity.add("ordercode", order);
            requestEntity.add("extra", orderDesc);
            requestEntity.add("brandcode", brandId);
            requestEntity.add("orderdesc", orderDesc);
            requestEntity.add("channel_tag", channelTag);
            LOG.info("接口/v1.0/paymentchannel/topup/request--参数================" + requestEntity.toString());
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口/v1.0/paymentchannel/topup/request--RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                responseCode = jsonObject.getString("resp_code");
                responseMessage = jsonObject.getString("resp_message");
            } catch (Exception e) {
                LOG.error("==========/v1.0/paymentchannel/topup/request支付请求异常===========" + e);

                return ResultWrap.init(CommonConstants.FALIED, "".equals(responseMessage) ? "请求支付出现异常,请稍后重试!" : responseMessage);
            }
        }

        if (!CommonConstants.SUCCESS.equalsIgnoreCase(responseCode)) {

            return ResultWrap.init(responseCode, responseMessage);
        } else {

            if (jsonObject.containsKey("result")) {

                return ResultWrap.init(CommonConstants.SUCCESS, responseMessage, jsonObject.getString(CommonConstants.RESULT));
            } else {

                return ResultWrap.init(CommonConstants.SUCCESS, responseMessage);
            }

        }

    }


    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/topup/rechargetoaccount/topage")
    public @ResponseBody
    Object reChargeToAccount(HttpServletRequest request, HttpServletResponse response, Model model
    ) throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        String userId = request.getParameter("userid");
        String amount = request.getParameter("amount");
        String ordercode = request.getParameter("ordercode");
        String extra = request.getParameter("extra");
        String brandcode = request.getParameter("brandcode");
        String orderdesc = request.getParameter("orderdesc");
        String channelTag = request.getParameter("channel_tag");

        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("paymentchannel", "error url request!");
        String url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userid", userId);
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", ordercode);
        requestEntity.add("extra", extra);
        requestEntity.add("brandcode", brandcode);
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channelTag);
        LOG.info("接口/v1.0/paymentchannel/topup/request--参数================" + requestEntity.toString());
        JSONObject jsonObject = null;
        String responsecode = null;
        String responsemessage = null;
        try {
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("接口/v1.0/paymentchannel/topup/request--RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            responsecode = jsonObject.getString("resp_code");
            responsemessage = jsonObject.getString("resp_message");
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request支付请求异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,
                    "".equals(responsemessage) ? "亲.网络出错了哦,臣妾已经尽力了,请重试~" : responsemessage);
            return map;
        }

		/*String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if(!CommonConstants.SUCCESS.equals(respCode)){
			if("999990".equals(respCode)){
				map.put(CommonConstants.RESP_CODE, "999990");
				map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
				//map.put(CommonConstants.RESULT, ipAddress+"/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard+"&userId=" + userId);
				return map;
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
				return map;
			}
		}*/

        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    private boolean compareChannelTime(String strStartTime, String strEndTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String strNowTime = "";
        strNowTime = sdf.format(new Date());

        int[] strStartTimes = new int[3];
        int[] strEndTimes = new int[3];
        int[] strNowTimes = new int[3];
        for (int i = 0; i < strStartTime.split(":").length; i++) {
            strStartTimes[i] = Integer.valueOf(strStartTime.split(":")[i]);
            strEndTimes[i] = Integer.valueOf(strEndTime.split(":")[i]);
            strNowTimes[i] = Integer.valueOf(strNowTime.split(":")[i]);
        }
        boolean isTrue = false;

        if (strNowTimes[0] == strStartTimes[0] || strNowTimes[0] == strEndTimes[0]) {
            if (strNowTimes[1] == strStartTimes[1] || strNowTimes[1] == strEndTimes[1]) {
                if (strNowTimes[2] == strStartTimes[2] || strNowTimes[2] == strEndTimes[2]) {
                    // 跳出
                } else {
                    if (strNowTimes[1] == strStartTimes[1] && strNowTimes[2] > strStartTimes[2]) {
                        isTrue = true;
                    } else if (strNowTimes[1] == strEndTimes[1] && strNowTimes[2] < strEndTimes[2]) {
                        isTrue = true;
                    } else {
                        // 跳出
                    }
                }

            } else {
                if (strNowTimes[0] == strStartTimes[0] && strNowTimes[1] > strStartTimes[1]) {
                    isTrue = true;
                } else if (strNowTimes[0] == strEndTimes[0] && strNowTimes[1] < strEndTimes[1]) {
                    isTrue = true;
                } else {

                    // 跳出
                }
            }

        } else {
            if (strNowTimes[0] > strStartTimes[0] && strNowTimes[0] < strEndTimes[0]) {
                isTrue = true;
            } else {
                // 跳出
            }

        }
        return isTrue;
    }

    /**
     * 进行一笔分润提现
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/withdraw/rebate")
    public @ResponseBody
    Object rebateWithdraw(HttpServletRequest request,
                          @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                          @RequestParam(value = "phone") String phone,
                          @RequestParam(value = "amount") String amount, @RequestParam(value = "order_desc") String orderdesc) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            double amountDouble = Double.valueOf(amount);
            amount = String.format("%.2f", amountDouble);
        } catch (NumberFormatException e1) {
            LOG.error("传入amount参数有误" + e1);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.提现的金额有误哦,请重试~");
            return map;
        }
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "2");

        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询黑白名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!CommonConstants.SUCCESS.equalsIgnoreCase(rescode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        restTemplate = new RestTemplate();

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject resultObj;
        long userId;
        long brandid;
        String brandname;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            userId = resultObj.getLong("id");
            brandid = resultObj.getLong("brandId");
        } catch (Exception e) {
            LOG.error("根据手机号查询用户信息失败=============================" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哟,请稍后重试!");
            return map;
        }

        // 获取用户当前帐户信息
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/query/phone";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brand_id", brandid + "");
        LOG.info("参数================" + requestEntity);
        //生产订单？？？？
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("生成订单失败,原因为查询用户信息异常=============================用户手机号:" + phone + ",贴牌号:" + brandid);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哟,请稍后重试!");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哟,请稍后重试!");
            return map;
        }

        JSONObject userAccountJson = jsonObject.getJSONObject(CommonConstants.RESULT);
        String userRebateBalance = userAccountJson.getString("rebateBalance");
        // 判断用户余额是否充足
        if (new Integer(-1).equals((new BigDecimal(userRebateBalance)).compareTo(new BigDecimal(amount)))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "提现失败,原因:帐户分润余额不足,请确认分润余额后再提现.");
            return map;
        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "2");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", "JIEFUBAO");
        requestEntity.add("desc", orderdesc);
        String order;
        String realAmount;
        String userid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            realAmount = resultObj.getString("amount");
            userid = resultObj.getString("userid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        /** 判断用户的真实提现金额和想提现的金额比较 **/

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/rebate/freeze";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid);
        requestEntity.add("realamount", realAmount);
        requestEntity.add("order_code", order);
        // resultObj = jsonObject.getJSONObject("result");
        String withdrawrespcode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            withdrawrespcode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/rebate/freeze冻结余额异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if ("999999".equalsIgnoreCase(withdrawrespcode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
            map.put(CommonConstants.RESP_MESSAGE, "用户的分润余额不充足");
            return map;
        }
        //订单状态更新
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/update";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("order_code", order);
        requestEntity.add("status", "1");
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/transactionclear/payment/update异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "分润提现成功");
        return map;

    }

    /**
     * 提现一笔订单
     */    //   TODO  提现接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/withdraw")
    public @ResponseBody
    Object withdraw(HttpServletRequest request, @RequestParam(value = "phone") String phone,
                    @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                    @RequestParam(value = "amount") String amount,
                    @RequestParam(value = "channe_tag", required = false, defaultValue = "YILIAN1") String channeltag, // 通道标识
                    @RequestParam(value = "order_desc") String orderdesc
    ) {

        Map<String, Object> map = new HashMap<String, Object>();
//		if(!"".equals(phone)){
//			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//			map.put(CommonConstants.RESP_MESSAGE, "抱歉,提现暂时维护中,开放时间请留意公告,感谢配合!");
//			return map;
//		}
        //提现时间为9点-21点
        Date now = new Date();
        Calendar startLimitTime = Calendar.getInstance();
        Calendar endLimitTime = Calendar.getInstance();
        startLimitTime.set(Calendar.HOUR_OF_DAY, 9);
        startLimitTime.set(Calendar.MINUTE, 0);
        startLimitTime.set(Calendar.SECOND, 0);
        endLimitTime.set(Calendar.HOUR_OF_DAY, 21);
        endLimitTime.set(Calendar.MINUTE, 0);
        endLimitTime.set(Calendar.SECOND, 0);
        Date startTime = startLimitTime.getTime();
        Date endTime = endLimitTime.getTime();
        if (now.compareTo(startTime) < 0 || now.compareTo(endTime) > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "抱歉，提现时间为9:00~21:00");
            return map;
        }
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "2");// 0 表示登陆无法进行 1表示无法充值 2 表示无法提现
        // 3 无法支付
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询黑白名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户黑白名单出现异常,请稍后重试!");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
            return map;
        }

        if (Tools.checkAmount(amount) == false) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }
        /**
         * 判断当天提现总额是否超限 /v1.0/user/channel/query
         */
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/channel/query";

        /** 获取通道单日最大金额 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("channel_tag", channeltag);
        JSONObject resultChaObj;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultChaObj = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询黑白名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        LOG.info("result================" + resultChaObj);
        if (resultChaObj.containsKey("result")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }
        String dasd = resultChaObj.getString("everyDayMaxLimit");
        LOG.info("result================" + dasd);
        double everyDayMaxLimit = 0;
        double singleMaxLimit = 0;
        if (resultChaObj.containsKey("singleMaxLimit")) {
            singleMaxLimit = Double.parseDouble(resultChaObj.getString("singleMaxLimit"));
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }
        if (resultChaObj.containsKey("everyDayMaxLimit")) {
            everyDayMaxLimit = Double.parseDouble(resultChaObj.getString("everyDayMaxLimit"));
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }
		/*
		//根据brandId到数据库中获取贴牌信息  2019.4.18
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/brand/query/id";

		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("brand_id", sbrandId);
		restTemplate = new RestTemplate();

		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info(" brand RESULT================" + result);
		jsonObject = JSONObject.fromObject(result);
		JSONObject resultObject;
		resultObject = jsonObject.getJSONObject("result");
		String brandName=resultObject.getString("name");
		*/
        long lbrandId = -1;
        try {
            lbrandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e2) {
            lbrandId = -1;
        }
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", lbrandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户信息出现异常,请稍后重试!");
            return map;
        }
        String userId = "0";// 0表示没有用户，
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        }
        String brandId = "-1";// 给贴牌赋值初始值为空
        if (resultObju.containsKey("brandId")) {
            brandId = resultObju.getString("brandId");
        }

//		if (brandId.equals("3")) {
//			Date date = new Date();
//			if (date.getHours() > 17 || date.getHours() < 9) {
//				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
//				map.put(CommonConstants.RESP_MESSAGE, "提现时间为9:00~18:00");
//				return map;
//			}
//		} else if (!brandId.equals("90")) {
//			Date date = new Date();
//			if (!brandId.equals("0") && date.getHours() < 9) {
//				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
//				map.put(CommonConstants.RESP_MESSAGE, "提现时间为9:00~24:00");
//				return map;
//			}
//
//		}

        if (!userId.equals("0")) {
            /**
             * 判断当天提现总额是否超限 /v1.0/transactionclear/payment/query/userid
             */
            /** 调用下单，需要得到用户的订单信息 */
            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/payment/query/sum/userid";
            /** 获取通道单日最大金额 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("user_id", userId);
            requestEntity.add("type", "2");
            requestEntity.add("status", "1");
            String resultsum;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
                JSONObject resultsumObju = jsonObject.getJSONObject("result");
                resultsum = resultsumObju.getString("withdrawSum");
            } catch (Exception e) {
                LOG.error("==========/v1.0/transactionclear/payment/query/sum/userid获取通道单日最大金额异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "查询单日提现金额出现异常,请稍后重试!");
                return map;
            }
            double sumwithdraw = Double.parseDouble(amount);
            if (resultsum != null && !resultsum.equals("")) {
                sumwithdraw += Double.parseDouble(resultsum);
            }
            if (sumwithdraw > everyDayMaxLimit) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
                map.put(CommonConstants.RESP_MESSAGE, "单日提款金额已超限");
                return map;
            }
            if (Double.parseDouble(amount) > singleMaxLimit) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
                map.put(CommonConstants.RESP_MESSAGE, "单笔提款金额已超限");
                return map;

            }

        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
            return map;
        }
        /****
         * 先余额对比
         *
         */
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/query/phone";
        /** 根据的用户手机号码查询用户的余额 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        restTemplate = new RestTemplate();
        JSONObject userAccounmt;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            userAccounmt = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/query/phone根据的用户手机号码查询用户的余额异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户账号信息异常,请稍后重试!");
            return map;
        }
        String balance = "0";
        if (userAccounmt.containsKey("balance")) {
            balance = userAccounmt.getString("balance");
        }
        if (Double.parseDouble(amount) > Double.parseDouble(balance)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "您的余额不足");
            return map;
        }

        channeltag = "YBGJDF";

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "2");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        JSONObject resultObj;
        String order;
        long brandid;
        long userid;
        String realAmount;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
            //brandid = lbrandId;
            userid = resultObj.getLong("userid");
            realAmount = resultObj.getString("realAmount");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add生成订单出现异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单出现异常,请稍后重试!");
            return map;
        }
        /** 判断用户的真实提现金额和想提现的金额比较 **/
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/withdraw/freeze";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid + "");
        requestEntity.add("realamount", amount);
        requestEntity.add("order_code", order);
        // resultObj = jsonObject.getJSONObject("result");
        String withdrawrespcode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            withdrawrespcode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/withdraw/freeze判断用户的真实提现金额和想提现的金额比较异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "冻结提现金额出现异常,请稍后重试!");
            return map;
        }
        if ("999999".equalsIgnoreCase(withdrawrespcode)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
            map.put(CommonConstants.RESP_MESSAGE, "用户的余额不充足");
            return map;

        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/bank/default/userid";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid + "");
        String realnameBank = "";
        String idcardBank = "";
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            realnameBank = resultObj.getString("userName");
            idcardBank = resultObj.getString("idcard");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/bank/default/userid获取用户默认结算卡异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户默认提现卡出现异常,请稍后重试!");
            return map;
        }
        {
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userid", userId + "");
            String resultAuth = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            JSONObject jsonObjectAuth = JSONObject.fromObject(resultAuth);
            JSONObject realnameAuth = jsonObjectAuth.getJSONObject("realname");
            String realnaemAuth = realnameAuth.getString("realname");
            String idCardAuth = realnameAuth.getString("idcard");
            if (!realnaemAuth.equals(realnameBank) || !idCardAuth.equals(idcardBank)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "你在进行违规操作，请慎行！");
                return map;
            }
        }

        if (resultObj == null || resultObj.isNullObject()) {

            /** 提现失败的解冻 */
            unFreezeAccount(order, userid, amount);
            /** 用户没有绑定默认结算卡 */
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NO_DEFAULT_CARD);
            map.put(CommonConstants.RESP_MESSAGE, "您在本系统暂未绑定默认结算卡(储蓄卡),请先绑定默认结算卡!");
            return map;
        }

        String cardno = resultObj.getString("cardNo");
        String username = resultObj.getString("userName");
        String bankname = resultObj.getString("bankName");
        String priOrPub = resultObj.getString("priOrPub");
        String phoneno = resultObj.getString("phone");
        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/pay/request";
        LOG.info("开始调用支付接口================" + url);

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("cardno", cardno);
        requestEntity.add("username", username);
        requestEntity.add("bankname", bankname);
        requestEntity.add("phone", phoneno);
        requestEntity.add("channel_type", "2");
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("pri_or_pub", priOrPub);
        LOG.info("支付接口参数================" + requestEntity.toString());
        String respcode;
        String resmsg;
        String thirdordercode = null;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObj = jsonObject.getJSONObject("result");
            //respcode = resultObj.getString("reqcode");
            respcode = resultObj.getString("resp_code");
            //resmsg = resultObj.getString("resmsg");
            resmsg = resultObj.getString("resp_message");
            //thirdordercode = resultObj.getString("thirdordercode");
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/pay/request请求支付通道异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该系统暂时不能提现，请联系客服!");
            return map;
        }
        /***
         * 如果是E秒付
         */
        if (respcode.length() == 4 && respcode.equals("0000")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "提现成功");
            return map;
        } else if (respcode.length() == 4 && !respcode.equals("0000")) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "提现失败");
            return map;
        }

        /***
         * 如果是联动
         */
        if (respcode.length() == 5 && respcode.equals("00000")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "下单成功");
            return map;
        } else if (respcode.length() == 5 && !respcode.equals("00000")) {
            /** 提现失败的解冻 */
            unFreezeAccount(order, userid, amount);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, resmsg);
            return map;
        }

        /** 如果是中茂通道的, 那么无需走后面的流程 */
        if (respcode.equalsIgnoreCase("000000")) {

            /** 更新订单, 将第三方的订单号加到订单里面去 */
            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/payment/update";

            LOG.info("transactionclear=============" + order);
            LOG.info("transactionclear=============" + thirdordercode);
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("order_code", order);
            requestEntity.add("third_code", thirdordercode);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (RestClientException e1) {
                LOG.error("==========/v1.0/transactionclear/payment/update异常===========" + e1);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
                return map;
            }
            /** 直接调用 **/
            uri = util.getServiceUrl("paymentchannel", "error url request!");
            url = uri.toString() + "/v1.0/paymentchannel/pay/query";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("ordercode", thirdordercode);
            requestEntity.add("brandcode", brandid + "");
            requestEntity.add("channel_type", "2");
            requestEntity.add("channel_tag", channeltag);
            LOG.info("接口(/v1.0/paymentchannel/pay/query)参数================" + requestEntity.toString());
            String reqcode;
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口(/v1.0/paymentchannel/pay/query)-result================" + result);
                jsonObject = JSONObject.fromObject(result);
                resultObj = jsonObject.getJSONObject("result");
                respcode = resultObj.getString("rescode");
                reqcode = resultObj.getString("reqcode");
            } catch (Exception e) {
                LOG.error("==========/v1.0/paymentchannel/pay/query查询用户订单异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
                return map;
            }
            map.put(CommonConstants.RESULT, thirdordercode);
            String status = "0";
            if (reqcode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
                if (respcode != null && respcode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
                    /** 回调商家的交易处理页面 */
                    status = "1";
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESP_MESSAGE, "提现成功");
                } else if (respcode != null && respcode.equalsIgnoreCase(CommonConstants.WAIT_CHECK)) {
                    status = "3";
                    map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
                    map.put(CommonConstants.RESP_MESSAGE, "等待处理");
                } else {
                    status = "2";
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "提现失败");
                }
            } else {
                status = "2";
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "请求失败");
            }
            uri = util.getServiceUrl("transactionclear", "error url request!");
            url = uri.toString() + "/v1.0/transactionclear/payment/update";
            /** 根据的用户手机号码查询用户的基本信息 */
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("order_code", order);
            requestEntity.add("third_code", thirdordercode);
            requestEntity.add("status", status);
            LOG.info("接口(/v1.0/transactionclear/payment/update)参数================" + requestEntity.toString());
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("接口(/v1.0/transactionclear/payment/update)-result================" + result);
            } catch (RestClientException e) {
                LOG.error("==========/v1.0/transactionclear/payment/update异常===========" + e);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
                return map;
            }
            return map;
        } else {
            /** 提现失败的解冻 */
            unFreezeAccount(order, userid, amount);

            if (respcode.equalsIgnoreCase("000000")) {

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "下单成功");
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_REQ_FAILD);
                map.put(CommonConstants.RESP_MESSAGE, "下单失败");
                return map;
            }
        }

    }

    public void unFreezeAccount(String order, long userid, String amount) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/account/freeze";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("order_code", order);
        requestEntity.add("user_id", userid + "");
        requestEntity.add("amount", amount);
        requestEntity.add("add_or_sub", "1");
        try {
            String result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/user/account/freeze冻结余额异常===========" + e);
        }
    }

    /**
     * 购买一个产品订单
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchase")
    public @ResponseBody
    Object purchase(HttpServletRequest request,
                    @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                    @RequestParam(value = "phone") String phone,
                    @RequestParam(value = "amount") String amount,
                    @RequestParam(value = "channe_tag", required = false, defaultValue = "YILIAN") String channeltag,
                    @RequestParam(value = "order_desc") String orderdesc,
                    @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                    @RequestParam(value = "bank_card", required = false, defaultValue = "") String bankcard,
                    @RequestParam(value = "product_id") String prodid,
                    //该字段判断是否是差额会员升级的方式
                    @RequestParam(value = "difference", required = false, defaultValue = "-1") String difference,
                    Model model) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<String, Object>();

		/*if(!"".equals(phone)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		    map.put(CommonConstants.RESP_MESSAGE, "抱歉,购买产品通道暂时维护中,开放时间请留意公告,感谢配合!");
			return map;
		}*/

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户黑名单异常,请稍后重试!");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户信息异常,请稍后重试!");
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            amount = jSONObject.getString("money");
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                return map;
            }

        } else {
            LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级产品查询异常");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该产品暂时无法购买");
            return map;

        }
        //如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            //判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();

                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");

                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }

                }

            }
        }
        if ("YILIAN".equals(channeltag) || channeltag.contains("QUICK")) {
			/*restTemplate = new RestTemplate();
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/bank/default/cardno";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("cardno", bankcard);
			requestEntity.add("type", "0");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
			JSONObject resultObj1;
			try {
				jsonObject = JSONObject.fromObject(result);
				resultObj1 = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				LOG.error("查询银行卡信息出错");
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", "查询银行卡信息有误");
				return map;
			}

			String nature = resultObj1.getString("nature");
			String bankName = resultObj1.getString("bankName");*/

            channeltag = "YLDZ_QUICK";

//			return ResultWrap.init(CommonConstants.FALIED, "银联充值购买产品暂时维护中,请选择支付宝或微信支付方式购买会员产品,感谢配合!");
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询贴牌商信息出现异常,请稍后重试!");
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        requestEntity.add("bank_card", bankcard);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单出现异常,请稍后重试!");
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付异常,请稍后重试!");
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard + "&userId=" + userId);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                return map;
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        //map.put(CommonConstants.RESP_MESSAGE, "亲.支付成功!");
        map.put(CommonConstants.RESULT, jsonObject.getString(CommonConstants.RESULT));
        return map;

    }


    //微信APP支付购买产品的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchase/wxpay")
    public @ResponseBody
    Object purchaseWXPay(HttpServletRequest request,
                         @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                         @RequestParam(value = "phone") String phone,
                         @RequestParam(value = "amount") String amount,
                         @RequestParam(value = "channe_tag", required = false, defaultValue = "YILIAN") String channeltag,
                         @RequestParam(value = "order_desc") String orderdesc,
                         @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                         @RequestParam(value = "product_id") String prodid,
                         //该字段判断是否是差额会员升级的方式
                         @RequestParam(value = "difference", required = false, defaultValue = "-1") String difference,
                         Model model) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<String, Object>();

		/*if(!"".equals(phone)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		    map.put(CommonConstants.RESP_MESSAGE, "抱歉,购买产品通道暂时维护中,开放时间请留意公告,感谢配合!");
			return map;
		}*/

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            amount = jSONObject.getString("money");
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                return map;
            }

        } else {
            LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级产品查询异常");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该产品暂时无法购买");
            return map;

        }
        //如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            //判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();

                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");

                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }

                }

            }
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {

            return ResultWrap.init(CommonConstants.FALIED, jsonObject.getString(CommonConstants.RESP_MESSAGE));
        } else {

            JSONObject fromObject = JSONObject.fromObject(jsonObject.getString(CommonConstants.RESULT));

            return ResultWrap.init(CommonConstants.SUCCESS, jsonObject.getString(CommonConstants.RESP_MESSAGE), fromObject);
        }

    }


    /**
     * 支付宝、微信购买产品
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchase/aliandwx")
    public @ResponseBody
    Object aliPayAndWXPurchase(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                               @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "amount") String amount,
                               @RequestParam(value = "channe_tag", required = false, defaultValue = "ALI") String channeltag,
                               @RequestParam(value = "order_desc") String orderdesc,
                               @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                               @RequestParam(value = "product_id") String prodid,
                               //该字段判断是否是差额会员升级的方式
                               @RequestParam(value = "difference", required = false, defaultValue = "-1") String difference,
                               Model model) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        if ("ALI".equalsIgnoreCase(channeltag)) {

            channeltag = "SPALI_PAY";
        } else if ("WX".equalsIgnoreCase(channeltag)) {

            channeltag = "SPWX_PAY";
            /*
             */

        } else if ("ALIAPP".equals(channeltag)) {
            channeltag = "SPALI_PAY_APP";
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该支付暂不可用,程序员在努力加班处理中!");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("该支付暂不可用,程序员在努力加班处理中!", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("该支付暂不可用,程序员在努力加班处理中!", "UTF-8"));
            return map;
        }
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("用户在黑名单中!", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("用户在黑名单中!", "UTF-8"));
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String userId = "0";
//		if(!"1".equals(resultObju.getString("realnameStatus"))) {
//			LOG.info("暂未实名======");
//			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//			map.put(CommonConstants.RESP_MESSAGE, "暂未实名,无法购买会员!");
//			map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("暂未实名,无法购买会员!", "UTF-8"));
//			//response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("暂未实名,无法购买会员!", "UTF-8"));
//			return map;
//		}

        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("无法重复购买此权限!", "UTF-8"));
                //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("无法重复购买此权限!", "UTF-8"));
                return map;
            }

        }

        // 如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            // 判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();

                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");

                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }

                }

            }
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepageurchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("您已成为主宰,不可升级!", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("您已成为主宰,不可升级!", "UTF-8"));
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                //map.put(CommonConstants.RESULT, ipAddress+"/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard+"&userId=" + userId);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
                //response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
                return map;
            }
        }
        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		/*map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		//map.put(CommonConstants.RESP_MESSAGE, "亲.支付成功!");
		map.put(CommonConstants.RESULT, jsonObject.getString(CommonConstants.RESULT));*/
        return null;
    }

    /**
     * 支付宝、微信购买产品
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchase/ali")
    public @ResponseBody
    Object aliPay(HttpServletRequest request, HttpServletResponse response,
                  @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                  @RequestParam(value = "phone") String phone,
                  @RequestParam(value = "amount") String amount,
                  @RequestParam(value = "channe_tag", required = false, defaultValue = "ALI") String channeltag,
                  @RequestParam(value = "order_desc") String orderdesc,
                  @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                  @RequestParam(value = "product_id") String prodid,
                  //该字段判断是否是差额会员升级的方式
                  @RequestParam(value = "difference", required = false, defaultValue = "-1") String difference,
                  Model model) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        if ("ALI".equalsIgnoreCase(channeltag)) {
            channeltag = "SPALI_PAY";
        } else if ("WX".equalsIgnoreCase(channeltag)) {
            channeltag = "SPWX_PAY";

        } else if ("ALIAPP".equals(channeltag)) {
            channeltag = "SPALI_PAY_APP";
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该支付暂不可用,程序员在努力加班处理中!");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("该支付暂不可用,程序员在努力加班处理中!", "UTF-8"));
            return map;
        }
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("用户在黑名单中!", "UTF-8"));
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("无法重复购买此权限!", "UTF-8"));
                return map;
            }

        }
        // 如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            // 判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();
                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");
                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepageurchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("您已成为主宰,不可升级!", "UTF-8"));
            return map;
        }
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
                return map;
            }
        }
        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //跳转页面
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchase/tofailepage")
    public String returnToFailePage(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException {
        // 设置编码
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        String resp_message = request.getParameter("resp_message");
        model.addAttribute("resp_message", resp_message);
        return "tofailepage";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchase/to/set/bankcard/info")
    public String toSetBinkInfo(HttpServletRequest request,
                                @RequestParam(value = "bankCard") String bankCard,
                                @RequestParam(value = "userId") String userId,
                                Model model) {
        model.addAttribute("creditCardNumber", bankCard);
        model.addAttribute("userId", userId);
        model.addAttribute("requestUrl", ipAddress + "/v1.0/user/bank/set/bankinfo");
        return "setbankcard";
    }


    /**
     * 是否购买一个产品订单
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchaseft")
    public String purchaseFT(HttpServletRequest request,
                             @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                             @RequestParam(value = "phone") String phone,
                             @RequestParam(value = "amount") String amount,
                             @RequestParam(value = "channe_tag", required = false, defaultValue = "YILIAN") String channeltag,
                             @RequestParam(value = "order_desc") String orderdesc,
                             @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                             @RequestParam(value = "bank_card") String bankcard,
                             @RequestParam(value = "product_id") String prodid
            , Model model) {
        model.addAttribute("brandId", sbrandId);
        model.addAttribute("phone", phone);
        model.addAttribute("amount", amount);
        model.addAttribute("channe_tag", channeltag);
        model.addAttribute("order_desc", orderdesc);
        model.addAttribute("purcase_type", purcasetype);
        model.addAttribute("bank_card", bankcard);
        model.addAttribute("product_id", prodid);
        model.addAttribute("post_url", ipAddress + "/v1.0/facade/purchase");
        return "purchaseft";
    }
    /*** 商城对接接口 **/
    /**
     * 跳转登录页面 callbackConfig JuHeAPIdBeccancyService
     **/
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/added/shop/login")
    public String shopCallLogin(HttpServletRequest request, @RequestParam(value = "manage_code") String userId,
                                @RequestParam(value = "amount") String amount, @RequestParam(value = "order_desc") String orderdesc,
                                @RequestParam(value = "notify_url", defaultValue = "", required = false) String notifyUrl,
                                @RequestParam(value = "remark") String remark, @RequestParam(value = "out_order_code") String outorderCode,
                                Model model) {
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userId);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/userid查询用户黑名单异常===========" + e);
            return "shopLogin_err";
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            model.addAttribute(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            model.addAttribute(CommonConstants.RESP_MESSAGE, "用户风控中……");
            return "shopLogin_err";
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            return "shopLogin_err";
        }
        if (resultbrand.containsKey("id")) {

        } else {
            model.addAttribute(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            model.addAttribute(CommonConstants.RESP_MESSAGE, "商户号不存在");
            return "shopLogin_err";
        }
        /**
         * @RequestParam(value = "manage_code") String userId,
         * @RequestParam(value = "amount") String amount,
         * @RequestParam(value = "order_desc") String orderdesc,
         * @RequestParam(value = "bank_card") String bankcard,
         * @RequestParam(value = "notify_url", defaultValue = "", required =
         *                     false) String notifyUrl,
         * @RequestParam(value = "remark") String remark,
         * @RequestParam(value = "out_order_code") String outorderCode
         **/
        model.addAttribute("manage_code", userId);
        model.addAttribute("brand_name", resultbrand.getString("name"));
        model.addAttribute("amount", amount);
        model.addAttribute("order_desc", orderdesc);
        model.addAttribute("notify_url", notifyUrl);
        model.addAttribute("remark", remark);
        model.addAttribute("out_order_code", outorderCode);
        return "shopLogin";
    }

    /**
     * 跳转银行卡选择页面 callbackConfig JuHeAPIdBeccancyService
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/added/shop/login/go")
    public String shopCallLogingo(HttpServletRequest request, @RequestParam(value = "manage_code") String userId,
                                  @RequestParam(value = "amount") String amount, @RequestParam(value = "order_desc") String orderdesc,
                                  @RequestParam(value = "notify_url", defaultValue = "", required = false) String notifyUrl,
                                  @RequestParam(value = "remark") String remark, @RequestParam(value = "phone") String phone,
                                  @RequestParam(value = "password") String password,
                                  @RequestParam(value = "out_order_code") String outorderCode, Model model) {
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/userid查询用户黑名单异常===========" + e);
            return "shopLogin_err";
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            model.addAttribute(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            model.addAttribute(CommonConstants.RESP_MESSAGE, "用户风控中……");
            return "shopLogin_err";
        }

        /** 验证登录名密码 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            return "shopLogin_err";
        }
        if (resultbrand.containsKey("id")) {

        } else {
            model.addAttribute(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            model.addAttribute(CommonConstants.RESP_MESSAGE, "商户号不存在");
            return "shopLogin_err";
        }

        /** 登陆验证 **/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/login";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brand_id", resultbrand.getString("id"));
        requestEntity.add("password", password);
        restTemplate = new RestTemplate();
        JSONObject location;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            location = null;
            if (jsonObject.getString("resp_code").equals("000000")) {
                location = jsonObject.getJSONObject("result");
            } else {
                model.addAttribute(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                model.addAttribute(CommonConstants.RESP_MESSAGE, jsonObject.getString("resp_message"));
                return "shopLogin_err";
            }
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/user/login用户登陆异常===========" + e);
            return "shopLogin_err";
        }

        model.addAttribute("manage_code", userId);
        model.addAttribute("brand_name", resultbrand.getString("name"));
        model.addAttribute("amount", amount);
        model.addAttribute("order_desc", orderdesc);
        model.addAttribute("notify_url", notifyUrl);
        model.addAttribute("remark", remark);
        model.addAttribute("out_order_code", outorderCode);
        model.addAttribute("user", location);

        return "shopGopay";
    }

    /**
     * 进行支付 callbackConfig JuHeAPIdBeccancyService
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/added/shop/go/pay/{token}")
    public @ResponseBody
    Object shopCallLogingopay(HttpServletRequest request, @PathVariable("token") String token,
                              @RequestParam(value = "manage_code") String userId, @RequestParam(value = "amount") String amount,
                              @RequestParam(value = "order_desc") String orderdesc, @RequestParam(value = "bank_card") String bankcard,
                              @RequestParam(value = "notify_url") String notifyUrl, @RequestParam(value = "remark") String remark,
                              @RequestParam(value = "phone") String phone, @RequestParam(value = "paypass") String paypass,
                              @RequestParam(value = "out_order_code") String outorderCode, Model model) {
        Map<String, Object> map = new HashMap<String, Object>();

        long loginUser;
        try {
            loginUser = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");

        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户风控中……");
            return map;
        }

        /** 验证登录名密码 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (resultbrand.containsKey("id")) {

        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESP_MESSAGE, "商户号不存在");
            return map;
        }

        /** 登陆验证 **/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/paypass/auth/" + token;
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("paypass", paypass);
        restTemplate = new RestTemplate();
        String location;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            location = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/paypass/auth/{token}验证支付密码异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!location.endsWith(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("resp_message"));
            return map;
        }

        model.addAttribute("manage_code", userId);
        model.addAttribute("amount", amount);
        model.addAttribute("order_desc", orderdesc);
        model.addAttribute("notify_url", notifyUrl);
        model.addAttribute("remark", remark);
        model.addAttribute("out_order_code", outorderCode);
        model.addAttribute("user", location);

        /** 登陆验证 **/
        uri = util.getServiceUrl("facade", "error url request!");
        url = uri.toString() + "/v1.0/facade/purchase/shop";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("manage_code", userId);
        requestEntity.add("amount", amount);
        requestEntity.add("order_desc", orderdesc);
        requestEntity.add("notify_url", notifyUrl);
        requestEntity.add("bank_card", bankcard);
        requestEntity.add("remark", remark);
        requestEntity.add("out_order_code", outorderCode);
        requestEntity.add("phone", phone);
        restTemplate = new RestTemplate();

        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            if (jsonObject.containsKey("result")) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("resp_message"));
                map.put(CommonConstants.RESULT, jsonObject.getString("result"));
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("resp_message"));
                return map;
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/facade/purchase/shop接口异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

    }

    /**
     * 商城对接接口
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchase/shop")
    public @ResponseBody
    Object purchaseShopping(HttpServletRequest request,
                            @RequestParam(value = "manage_code") String userId,
                            @RequestParam(value = "amount") String amount,
                            @RequestParam(value = "phone") String phone,
                            @RequestParam(value = "order_desc") String orderdesc,
                            @RequestParam(value = "bank_card") String bankcard,
                            @RequestParam(value = "notify_url", defaultValue = "", required = false) String notifyUrl,
                            @RequestParam(value = "remark") String remark,
                            @RequestParam(value = "out_order_code") String outorderCode) {

        String channeltag = "YILIAN";
        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userId);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");

        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (resultbrand.containsKey("id")) {

        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESP_MESSAGE, "商户号不存在");
            return map;
        }
        /*
         * String ip = request.getHeader("x-forwarded-for"); if(ip == null ||
         * ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { ip =
         * request.getHeader("Proxy-Client-IP"); } if(ip == null || ip.length()
         * == 0 || "unknown".equalsIgnoreCase(ip)) { ip =
         * request.getHeader("WL-Proxy-Client-IP"); } if(ip == null ||
         * ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { ip =
         * request.getRemoteAddr(); }
         * if(!ip.equals(resultbrand.getString("ip"))){
         * map.put(CommonConstants.RESP_CODE,CommonConstants.
         * ERROR_USER_NOT_EXISTED); map.put(CommonConstants.RESP_MESSAGE,
         * "路由不匹配"); return map; }
         */
        if (bankcard == null || bankcard.equals("")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "银行卡信息不能为空");
            return map;
        }

        /** 判断银行卡是否为借记卡 **/
        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/bankcard/location";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("cardid", bankcard);
        restTemplate = new RestTemplate();
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject location = jsonObject.getJSONObject("result");

            if (!location.getString("errorCode").equalsIgnoreCase("0")) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_CARD_ERROR);
                map.put(CommonConstants.RESP_MESSAGE, location.getString("reason"));
                return map;
            }
            String nature = null;
            if (location.containsKey("nature")) {
                nature = location.getString("nature");
            } else {
            }
            if (nature.indexOf("贷记卡") != -1) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_CARD_ERROR);
                map.put(CommonConstants.RESP_MESSAGE, "仅支持借记卡支付");
                return map;
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/bankcard/location判断银行卡是否为借记卡异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        /** 获取用户信息 **/
        /*
         * uri = util.getServiceUrl("user", "error url request!"); url =
         * uri.toString() + "/v1.0/user/query/id";
         *//** 根据的用户手机号码查询用户的基本信息 *//*
         * requestEntity = new
         * LinkedMultiValueMap<String, String>();
         * requestEntity.add("id", userId);
         * restTemplate=new RestTemplate(); result =
         * restTemplate.postForObject(url,
         * requestEntity, String.class); LOG.info(
         * "RESULT================purchaseShopping"+
         * result); jsonObject =
         * JSONObject.fromObject(result); JSONObject
         * resultObju =
         * jsonObject.getJSONObject("result");
         * String phone =null;
         * if(resultObju.containsKey("phone")){
         * phone=resultObju.getString("phone"); }
         */

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("desc_code", "shopping");
        requestEntity.add("notify_url", notifyUrl);
        requestEntity.add("bank_card", bankcard);
        requestEntity.add("out_order_code", outorderCode);
        requestEntity.add("remark", remark);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add生成订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        String redirecturl;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            redirecturl = jsonObject.getString("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付通道异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        map.put(CommonConstants.RESULT, redirecturl);
        return map;

    }

    /**
     * 违章查询
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/wzdjQuerywz")
    public @ResponseBody
    Object wzdjQuerywz(HttpServletRequest request, @RequestParam(value = "phone") String phone,
                       @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                       @RequestParam(value = "amount") String amount,
                       @RequestParam(value = "channe_tag", required = false, defaultValue = "JIEFUBAO") String channeltag,
                       // 车牌号
                       @RequestParam(value = "carNo") String carNo,
                       // 车架号(根据城市列表的规则决定长度)
                       @RequestParam(value = "frameNo") String frameNo,
                       // 发动机号(根据城市列表的规则决定长度)
                       @RequestParam(value = "enginNo") String enginNo,
                       // 车类型(默认02:小型车,暂时只支持小型车)
                       @RequestParam(value = "carType") String carType,
                       // 省份id(当不指定的时候默认根据车前缀)
                       @RequestParam(value = "provinceid") String provinceid,
                       // 城市id(当不指定的时候默认根据车前缀)
                       @RequestParam(value = "cityid") String cityid, @RequestParam(value = "order_desc") String orderdesc) {

        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "2");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
            return map;
        }
        if (Tools.checkAmount(amount) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        String userId;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
            userId = resultObju.getString("id");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone根据的用户手机号码查询用户的基本信息异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String brandidb = "0";
        if (resultObju.containsKey("brandId") && resultObju.getString("brandId") != null) {
            brandidb = resultObju.getString("brandId");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "违章代缴升级中敬请期待！！！");
            return map;
        }

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandidb;
        JSONObject brand;
        String juheOpenid;
        try {
            result = restTemplate.getForObject(url, String.class);
            jsonObject = JSONObject.fromObject(result);
            LOG.info("RESULT================wzdjQuerywz" + result);
            brand = jsonObject.getJSONObject("result");
            juheOpenid = brand.getString("juheOpenid");
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/user/brand/query/id异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        LOG.info("RESULT=======juheOpenid=========wzdjQuerywz" + juheOpenid);
        if (!brand.containsKey("juheOpenid")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("userid", userId + "");
        requestEntity.add("carNo", carNo);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        String order;
        long brandid;
        String realAmount;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
            realAmount = resultObj.getString("realAmount");
            userId = resultObj.getString("userid");
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/add生成订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        /** 判断用户的真实提现金额和想提现的金额比较 **/
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/withdraw/freeze";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userId);
        requestEntity.add("realamount", realAmount);
        requestEntity.add("order_code", order);
        // resultObj = jsonObject.getJSONObject("result");
        String withdrawrespcode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            withdrawrespcode = jsonObject.getString("resp_code");
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/user/account/withdraw/freeze冻结余额异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (withdrawrespcode.equalsIgnoreCase("999999")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
            map.put(CommonConstants.RESP_MESSAGE, "用户的余额不充足");
            return map;
        }

        /**
         * 违章查询 querywz
         */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/added/wzdj/querywz";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandid + "");
        requestEntity.add("carNo", carNo);
        requestEntity.add("frameNo", frameNo);
        requestEntity.add("enginNo", enginNo);
        requestEntity.add("carType", carType);
        requestEntity.add("provinceid", provinceid);
        requestEntity.add("cityid", cityid);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObjb = jsonObject.getJSONObject("result");
            if (jsonObject.containsKey("result")) {
                map.put(CommonConstants.RESP_CODE, resultObjb.getString("error_code"));
                map.put(CommonConstants.RESP_MESSAGE, resultObjb.getString("reason"));
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, "查询失败");
                return map;
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/added/wzdj/querywz异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
    }

    /**
     * 充值话费
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchasebill")
    public @ResponseBody
    Object purchaseBill(HttpServletRequest request,
                        @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                        @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "phonebill") String phonebill, @RequestParam(value = "amount") String amount,
                        @RequestParam(value = "realamount") String realamount,
                        @RequestParam(value = "channe_tag", required = false, defaultValue = "JIEFUBAO") String channeltag,
                        @RequestParam(value = "order_desc") String orderdesc) {

        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "2");
        String result;
        JSONObject jsonObject;
        String rescode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
            return map;
        }
        if (Tools.checkAmount(amount) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }
        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        String userId;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
            userId = resultObju.getString("id");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String brandidb = "0";
        if (resultObju.containsKey("brandId") && resultObju.getString("brandId") != null) {
            brandidb = resultObju.getString("brandId");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandidb;
        String juheOpenid;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject brand = jsonObject.getJSONObject("result");
            juheOpenid = brand.getString("juheOpenid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        LOG.info("RESULT=======juheOpenid=========purchaseBill" + juheOpenid);
        if (juheOpenid == null || juheOpenid.equals("") || juheOpenid.equals("null")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }
        /****
         * 先余额对比
         *
         */
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/query/phone";
        /** 根据的用户手机号码查询用户的余额 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        restTemplate = new RestTemplate();
        JSONObject userAccounmt;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            userAccounmt = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/query/phone根据的用户手机号码查询用户的余额异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String balance = "0";
        if (userAccounmt.containsKey("balance")) {
            balance = userAccounmt.getString("balance");
        } else {
            balance = "0";
        }
        if (Double.parseDouble(realamount) > Double.parseDouble(balance)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "您的余额不足");
            return map;
        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("userid", userId + "");
        requestEntity.add("phonebill", phonebill);
        requestEntity.add("amount", amount);
        requestEntity.add("realamount", realamount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc_code", "MobilePayment");
        requestEntity.add("desc", orderdesc);
        String order;
        long brandid;
        long userid;
        String realAmount;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
            userid = resultObj.getLong("userid");
            realAmount = resultObj.getString("realAmount");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/add生成订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        /** 判断用户的真实提现金额和想提现的金额比较 **/
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/withdraw/freeze";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid + "");
        requestEntity.add("realamount", realAmount);
        requestEntity.add("order_code", order);
        // resultObj = jsonObject.getJSONObject("result");
        String withdrawrespcode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            withdrawrespcode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/withdraw/freeze判断用户的真实提现金额和想提现的金额比较异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (withdrawrespcode.equalsIgnoreCase("999999")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
            map.put(CommonConstants.RESP_MESSAGE, "用户的余额不充足");
            return map;
        }

        /**
         * 手机充值 /v1.0/user/added/phone/onlineorder
         */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/added/phone/onlineorder";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandid + "");
        requestEntity.add("phone", phonebill);
        requestEntity.add("cardnum", amount);
        requestEntity.add("ordercode", order);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObjb = jsonObject.getJSONObject("result");
            if (jsonObject.containsKey("result")) {
                map.put(CommonConstants.RESP_CODE, resultObjb.getString("error_code").equals("0")
                        ? CommonConstants.SUCCESS : CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, resultObjb.getString("reason"));
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "下单失败");
                return map;
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/added/phone/onlineorder调用手机充值接口异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
    }

    /**
     * 违章下单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/wzdjsubmitOrder")
    public @ResponseBody
    Object wzdjSubmitOrder(HttpServletRequest request,
                           @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                           @RequestParam(value = "phone") String phone,
                           @RequestParam(value = "amount") String amount,
                           @RequestParam(value = "channe_tag", required = false, defaultValue = "JIEFUBAO") String channeltag,
                           // 违章记录,多个用英文逗号分隔,如recordIds=12345,87342
                           @RequestParam(value = "recordIds") String recordIds,
                           // 车牌号
                           @RequestParam(value = "carNo") String carNo,
                           // 联系人(如果是测试订单,请写"测试")
                           @RequestParam(value = "contactName") String contactName,
                           // 联系人电话
                           @RequestParam(value = "tel") String tel, @RequestParam(value = "order_desc") String orderdesc) {

        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "2");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
            return map;
        }
        if (Tools.checkAmount(amount) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        String userId;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
            userId = resultObju.getString("id");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String brandidb = "0";
        if (resultObju.containsKey("brandId") && resultObju.getString("brandId") != null) {
            brandidb = resultObju.getString("brandId");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandidb;
        JSONObject brand;
        String juheOpenid;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            brand = jsonObject.getJSONObject("result");
            juheOpenid = brand.getString("juheOpenid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        LOG.info("RESULT=======juheOpenid=========wzdjQuerywz" + juheOpenid);
        if (!brand.containsKey("juheOpenid")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("userid", userId + "");
        requestEntity.add("carNo", carNo);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
            String realAmount = resultObj.getString("realAmount");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/add生成订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        /**
         * 违章下单 querywz
         */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/added/wzdj/submitOrder";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandid + "");
        requestEntity.add("carNo", carNo);
        requestEntity.add("contactName", contactName);
        requestEntity.add("tel", tel);
        requestEntity.add("userOrderId", order);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObjb = jsonObject.getJSONObject("result");

            /** 判断用户的真实提现金额和想提现的金额比较 **//*
             * restTemplate=new RestTemplate(); uri
             * = util.getServiceUrl("user",
             * "error url request!"); url =
             * uri.toString() +
             * "/v1.0/user/account/withdraw/freeze";
             * requestEntity = new
             * LinkedMultiValueMap<String,
             * String>(); requestEntity.add("phone",
             * phone);
             * requestEntity.add("realamount",
             * realAmount);
             * requestEntity.add("order_code",
             * order); result =
             * restTemplate.postForObject(url,
             * requestEntity, String.class);
             * LOG.info("RESULT================"+
             * result) ; jsonObject =
             * JSONObject.fromObject(result);
             * //resultObj =
             * jsonObject.getJSONObject("result");
             * String withdrawrespcode =
             * jsonObject.getString("resp_code");
             * if(withdrawrespcode.equalsIgnoreCase(
             * "999999")){
             * map.put(CommonConstants.RESP_CODE,
             * CommonConstants.
             * ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
             * map.put(CommonConstants.RESP_MESSAGE,
             * "用户的余额不充足"); return map; }
             */

            if (jsonObject.containsKey("result")) {
                map.put(CommonConstants.RESP_CODE, resultObjb.getString("error_code"));
                map.put(CommonConstants.RESULT, resultObjb.getString("result"));
                map.put("", resultObjb.getString("result"));
                map.put(CommonConstants.RESP_MESSAGE, resultObjb.getString("reason"));
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, resultObjb.getString("resp_message"));
                return map;
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/added/wzdj/submitOrder调用违章下单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
    }

    /**
     * 违章查询
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/wzdjPayOrder")
    public @ResponseBody
    Object wzdjPayOrder(HttpServletRequest request,
                        @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                        @RequestParam(value = "phone") String phone,
                        @RequestParam(value = "amount") String amount,
                        @RequestParam(value = "channe_tag", required = false, defaultValue = "JIEFUBAO") String channeltag,
                        // 违章记录,多个用英文逗号分隔,如recordIds=12345,87342
                        @RequestParam(value = "recordIds") String recordIds,
                        // 车牌号
                        @RequestParam(value = "carNo") String carNo,
                        // 联系人(如果是测试订单,请写"测试")
                        @RequestParam(value = "contactName") String contactName,
                        // 联系人电话
                        @RequestParam(value = "tel") String tel, @RequestParam(value = "order_desc") String orderdesc) {

        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "2");
        String result;
        JSONObject jsonObject;
        String rescode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
            return map;
        }
        if (Tools.checkAmount(amount) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        String userId;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
            userId = resultObju.getString("id");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String brandidb = "0";
        if (resultObju.containsKey("brandId") && resultObju.getString("brandId") != null) {
            brandidb = resultObju.getString("brandId");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandidb;
        LOG.info("RESULT================wzdjQuerywz" + result);
        JSONObject brand;
        String juheOpenid;
        try {
            result = restTemplate.getForObject(url, String.class);
            jsonObject = JSONObject.fromObject(result);
            brand = jsonObject.getJSONObject("result");
            juheOpenid = brand.getString("juheOpenid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        LOG.info("RESULT=======juheOpenid=========wzdjQuerywz" + juheOpenid);
        if (!brand.containsKey("juheOpenid")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "手机充值升级中敬请期待！！！");
            return map;
        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("userid", userId + "");
        requestEntity.add("carNo", carNo);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
            String realAmount = resultObj.getString("realAmount");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/add生成订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        /**
         * 违章查询 querywz
         */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/added/wzdj/submitOrder";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandid + "");
        requestEntity.add("carNo", carNo);
        requestEntity.add("contactName", contactName);
        requestEntity.add("tel", tel);
        requestEntity.add("userOrderId", order);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObjb = jsonObject.getJSONObject("result");

            /** 判断用户的真实提现金额和想提现的金额比较 **//*
             * restTemplate=new RestTemplate(); uri
             * = util.getServiceUrl("user",
             * "error url request!"); url =
             * uri.toString() +
             * "/v1.0/user/account/withdraw/freeze";
             * requestEntity = new
             * LinkedMultiValueMap<String,
             * String>(); requestEntity.add("phone",
             * phone);
             * requestEntity.add("realamount",
             * realAmount);
             * requestEntity.add("order_code",
             * order); result =
             * restTemplate.postForObject(url,
             * requestEntity, String.class);
             * LOG.info("RESULT================"+
             * result) ; jsonObject =
             * JSONObject.fromObject(result);
             * //resultObj =
             * jsonObject.getJSONObject("result");
             * String withdrawrespcode =
             * jsonObject.getString("resp_code");
             * if(withdrawrespcode.equalsIgnoreCase(
             * "999999")){
             * map.put(CommonConstants.RESP_CODE,
             * CommonConstants.
             * ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
             * map.put(CommonConstants.RESP_MESSAGE,
             * "用户的余额不充足"); return map; }
             */

            if (jsonObject.containsKey("result")) {
                map.put(CommonConstants.RESP_CODE, resultObjb.getString("error_code"));
                map.put(CommonConstants.RESULT, resultObjb.getString("result"));
                map.put(CommonConstants.RESP_MESSAGE, resultObjb.getString("reason"));
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESP_MESSAGE, resultObjb.getString("resp_message"));
                return map;
            }
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/user/added/wzdj/submitOrder异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
    }

    /*
     * public static void main(String[] args){
     *
     *
     * AlipaySystemOauthTokenRequest oauthTokenRequest = new
     * AlipaySystemOauthTokenRequest();
     * oauthTokenRequest.setCode("4b55710185a54b858cfe81be3991RC92");
     * oauthTokenRequest.setGrantType(AlipayServiceEnvConstants.GRANT_TYPE);
     * AlipayClient alipayClient = AlipayAPIClientFactory.getAlipayClient(); try
     * { AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient
     * .execute(oauthTokenRequest);
     *
     *
     * //成功获得authToken if (null != oauthTokenResponse &&
     * oauthTokenResponse.isSuccess()) {
     *
     * String alipayuserid = oauthTokenResponse.getUserId();
     *
     * System.out.println("获取用户信息成功：" + alipayuserid); //4. 利用authToken获取用户信息
     * AlipayUserUserinfoShareRequest userinfoShareRequest = new
     * AlipayUserUserinfoShareRequest(); AlipayUserUserinfoShareResponse
     * userinfoShareResponse = alipayClient.execute( userinfoShareRequest,
     * oauthTokenResponse());
     *
     * //成功获得用户信息 if (null != userinfoShareResponse &&
     * userinfoShareResponse.isSuccess()) { //这里仅是简单打印， 请开发者按实际情况自行进行处理
     * System.out.println("获取用户信息成功：" + userinfoShareResponse.getBody());
     *
     * } else { //这里仅是简单打印， 请开发者按实际情况自行进行处理 System.out.println("获取用户信息失败");
     *
     * }
     *
     *
     *
     *
     * } else { //这里仅是简单打印， 请开发者按实际情况自行进行处理
     * System.out.println("authCode换取authToken失败"); }
     *
     * } catch (AlipayApiException e1) { // TODO Auto-generated catch block
     * e1.printStackTrace(); }
     *
     *
     * }
     */

    /**
     * 保险下单返佣 callbackConfig JuHeAPIdBeccancyService 保费 premium=226 保额
     * sumInsured=6000000
     * <p>
     * bizContent=
     * a5f1a1a50f335d7c54017b8a437e3ebede47322c0e3ea69a1b89c83dbce76f419c6fe682c0ce7eddc35f001e0189d794847815f443c265b44bff5e672b895285dee49e9d8da0bdd613bde593fafbd5e545e80f6ab3f050877d8ef2e95c317aacdfef93f84ae919a2a49c796503fa1deb41ddcd818d716275b44f72b49fa392ef9ca2ff059359d3c592b96008e9bd82fccd6c29d20bc00f61fce4707afa7ef2b67bc003103f6da528d6a60824953bd87ca7c2dd03b93a6d
     * 保单号 policyNo=887742001400945001 产品名称 productComment=尊享e生·医疗险2017 尊享版
     **/
    String key = "open20160501";

    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/added/zhongan/callback")
    public String zhonganCallback(HttpServletRequest request, Model model) {
        /**
         *
         * **/

        Map<String, String> text = getParamNames(request);
        // 保费
        String premium = text.get("premium");
        // 保额
        String sumInsured = text.get("sumInsured");

        // 单号
        String policyNo = text.get("policyNo");
        // 产品名称
        String productComment = text.get("productComment");

        // 附加值
        String bizContent = text.get("bizContent");
        LOG.info("解码前：bizContent");
        bizContent = RC4Util.decryRC4(bizContent, key);
        LOG.info("解码后：bizContent");
        // 身份证号
        String policyHolderCertiNo;
        // 手机号
        String policyHolderPhone;
        // 姓名
        String policyHolderUserName;
        long userId;
        try {
            JSONObject bizContentObject = JSONObject.fromObject(bizContent);
            // 附加字段
            JSONObject extraInfo = bizContentObject.containsKey("extraInfo")
                    ? bizContentObject.getJSONObject("extraInfo") : null;
            policyHolderCertiNo = bizContentObject.containsKey("policyHolderCertiNo")
                    ? bizContentObject.getString("policyHolderCertiNo") : "";
            policyHolderPhone = bizContentObject.containsKey("policyHolderPhone")
                    ? bizContentObject.getString("policyHolderPhone") : "";
            policyHolderUserName = bizContentObject.containsKey("policyHolderUserName")
                    ? bizContentObject.getString("policyHolderUserName") : "";

            userId = 0;
            if (extraInfo != null) {
                userId = Long.parseLong(extraInfo.getString("userid"));
            } else {
                model.addAttribute("RespMsg", "未在我们平台下单");
                return "zhonganpay_err";
            }
        } catch (NumberFormatException e) {
            LOG.error("==========/v1.0/facade/added/zhongan/callback查询用户黑名单异常===========" + e);
            return "zhonganpay_err";
        }
        RestTemplate restTemplate = new RestTemplate();
        /** 调用下单，需要得到用户的订单信息 */
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/payment/query/thirdcode";

        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("third_code", policyNo);

        JSONObject jsonObject;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            if (jsonObject.containsKey("result")) {
                model.addAttribute("RespMsg", "下单成功");
                return "zhonganpay_err";
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/query/thirdcode调用下单，需要得到用户的订单信息异常===========" + e);
            return "zhonganpay_err";
        }

        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/id";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("id", userId + "");
        restTemplate = new RestTemplate();
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================wzdjQuerywz" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/id查询用户异常===========" + e);
            return "zhonganpay_err";
        }
        restTemplate = new RestTemplate();
        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("userid", userId + "");
        requestEntity.add("amount", premium);
        requestEntity.add("channel_tag", "JIEFUBAO");
        requestEntity.add("desc", "保险购买");
        requestEntity.add("remark",
                "受保人:" + policyHolderUserName + "-联系电话:" + policyHolderPhone + "-身份证件" + policyHolderCertiNo);
        String order;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseBill" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            long brandid = resultObj.getLong("brandid");
            String realAmount = resultObj.getString("realAmount");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/add生成订单异常===========" + e);
            return "zhonganpay_err";
        }

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("status", "1");
        requestEntity.add("order_code", order);
        requestEntity.add("third_code", policyNo);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT======sta==========" + result);
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/update更新订单异常===========" + e);
            return "zhonganpay_err";
        }
        model.addAttribute("RespMsg", "保险购买成功");
        return "zhonganpay_err";
    }

    private Map getParamNames(HttpServletRequest request) {
        Map map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }

        return map;
    }

    /*
     * //单发模板消息，需要先领取对应的消息模板，参数要根据要使用的模板样式来拼接，具体说明可参见开放平台接口文档 public static void
     * main(String[] args) { AlipayClient alipayClient =
     * AlipayAPIClientFactory.getAlipayClient();
     * AlipayOpenPublicMessageSingleSendRequest request = new
     * AlipayOpenPublicMessageSingleSendRequest(); Map<String, Object> bizMap =
     * new HashMap<String, Object>(); bizMap.put("to_user_id",
     * "2088402402508924"); Map<String, Object> templateMap = new
     * HashMap<String, Object>(); templateMap.put("template_id",
     * "2cadf4d7b89648fd87a30c8ee3b4b505"); Map<String, Object> contextMap = new
     * HashMap<String, Object>(); contextMap.put("head_color", "#85be53");
     * contextMap.put("url", "http://m.baidu.com");
     * contextMap.put("action_name", "查看详情"); Map<String, Object> first = new
     * HashMap<String, Object>(); first.put("value", "亲爱的李亮同学你好，现在是李亮在给你发消息");
     * Map<String, Object> keyword1 = new HashMap<String, Object>();
     * keyword1.put("value", "12345"); Map<String, Object> keyword2 = new
     * HashMap<String, Object>(); keyword2.put("value", "测试交易"); Map<String,
     * Object> keyword3 = new HashMap<String, Object>(); keyword3.put("value",
     * "2016-08-02 15:16:00"); Map<String, Object> keyword4 = new
     * HashMap<String, Object>(); keyword4.put("value", "宝剑锋从磨砺出，梅花香自苦寒来！");
     * Map<String, Object> remark = new HashMap<String, Object>();
     * remark.put("value", "哈哈哈哈"); contextMap.put("first", first);
     * contextMap.put("keyword1", keyword1); contextMap.put("keyword2",
     * keyword2); contextMap.put("keyword3", keyword3);
     * contextMap.put("keyword4", keyword4); contextMap.put("remark", remark);
     * templateMap.put("context", contextMap); bizMap.put("template",
     * templateMap);
     * request.setBizContent(JSONObject.fromObject(bizMap).toString());
     * System.out.println(request.getBizContent());
     * AlipayOpenPublicMessageSingleSendResponse response = null; try { response
     * = alipayClient.execute(request); System.out.println(response.getBody());
     * } catch (Exception e) { // TODO: handle exception } }
     */

    /****
     * 随机红包 0.1~2红包 transactionclear
     *
     ***/
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/red/payment/{token}")
    public @ResponseBody
    Object RedPayment(HttpServletRequest request, @PathVariable("token") String token) {
        Map<String, Object> map = new HashMap<String, Object>();
        long loginUser;
        long brandId;
        try {
            loginUser = TokenUtil.getUserId(token);
            brandId = TokenUtil.getBrandid(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        String orderdesc = "鼓励金";

        String channeltag = "JIEFUBAO";

        /*** 获取用户信息 ***/
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/query/id";
        /** 根据的用户手机号码查询用户的基本信息 */
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("id", loginUser + "");
        String phone = null;
        int encourageNum = 0;
        JSONObject jsonObject;
        JSONObject resultObju;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
            if (resultObju.containsKey("phone")) {
                phone = resultObju.getString("phone");
            }
            if (resultObju.containsKey("encourageNum")) {
                encourageNum = resultObju.getInt("encourageNum");
            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/id查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!resultObju.getString("realnameStatus").equals("1")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请先进行实名认证！！");
            return map;

        }
        if (encourageNum == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "抽奖次数已用尽！！");
            return map;
        }

        int amount = 1;
        int type = 0;
        url = uri.toString() + "/v1.0/user/redPacket/get/rednum";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("brand_id", brandId + "");
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================redPacket" + result);
        jsonObject = JSONObject.fromObject(result);
        if (jsonObject.get("resp_code").equals(CommonConstants.FALIED)) {
            try {
                if (resultObju.getInt("brandId") == 513 || resultObju.getInt("brandId") == 100132) {
                    amount = 500;
                } else {
                    amount = MathRandom.PercentageRandom();
                }
            } catch (Exception e) {
                amount = 1;
            }

        } else {
            JSONObject resultObred = jsonObject.getJSONObject("result");
            if (resultObred.containsKey("ishan")) {
                amount = resultObred.getInt("ishan");
                type = resultObred.getInt("type");
            }

        }
        BigDecimal amountBD = new BigDecimal(amount + "").setScale(2, BigDecimal.ROUND_DOWN);
        if (type == 0) {
            amountBD = new BigDecimal(amount + "").divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_DOWN);
        }

        /***
         * 判断贴牌余额
         *
         ****/
        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + resultObju.getString("brandId");
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/query/userId";
        // **根据的用户手机号码查询用户的基本信息*/
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", resultbrand.getString("manageid"));
        restTemplate = new RestTemplate();
        BigDecimal balance = new BigDecimal("0");
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchaseShopping" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject UserAccount = jsonObject.getJSONObject("result");
            if (UserAccount.containsKey("balance")) {
                balance = new BigDecimal(UserAccount.getString("balance"));
            }
            if (balance.compareTo(amountBD) == -1) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "平台帐户余额不足！");
                return map;

            }
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/query/userId查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        /***
         * 检查今天是否已抽取
         *
         *
         * restTemplate = new RestTemplate(); uri =
         * util.getServiceUrl("transactionclear", "error url request!"); url =
         * uri.toString() +
         * "/v1.0/transactionclear/payment/desccode/query/userid"; requestEntity
         * = new LinkedMultiValueMap<String, String>();
         * requestEntity.add("user_id", loginUser + ""); try { result =
         * restTemplate.postForObject(url, requestEntity, String.class);
         * LOG.info("RESULT================" + result); jsonObject =
         * JSONObject.fromObject(result); if (jsonObject.containsKey("result"))
         * { map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
         * map.put(CommonConstants.RESP_MESSAGE, "您今天的次数已用完!!"); return map; } }
         * catch (Exception e) { LOG.error(
         * "==========/v1.0/transactionclear/payment/desccode/query/userid查询订单异常==========="
         * + e); map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
         * map.put(CommonConstants.RESP_MESSAGE,"亲.网络出错了哦,臣妾已经尽力了,请重试~"); return
         * map; }
         */

        /***** 生成红包订单 ********/
        /** 首先看在不在黑名单里面，/v1.0/user/update/encourageNum如果在不能登录 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("risk", "error url request!");
        url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "1");
        String rescode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        /** 调用下单，需要得到用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();

        requestEntity.add("userid", loginUser + "");
        requestEntity.add("amount", amountBD.toString());
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        if (type == 0) {
            requestEntity.add("type", "0");
            requestEntity.add("desc_code", "RedPayment");
        } else if (type == 1) {
            requestEntity.add("desc_code", "RedPayment" + type);
            requestEntity.add("type", "13");
        }

        String order;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            long brandid = resultObj.getLong("brandid");

            String realAmount = resultObj.getString("realAmount");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/add生成订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        /***** 订单确认 *****/
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/type1/update";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("status", "1");
        requestEntity.add("order_code", order);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e1) {
            LOG.error("==========/v1.0/transactionclear/payment/type1/update更新订单异常===========" + e1);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        /** 减去抽奖次数 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/update/encourageNum";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", loginUser + "");
        String resp_code;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
            resp_code = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/update/encourageNum减去抽奖次数异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!resp_code.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString("resp_message"));
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, amountBD);
        map.put(CommonConstants.RESP_MESSAGE, "发放成功");
        return map;
    }

    /**
     * 同一贴牌下帐户余额互转接口
     *
     * @param request
     * @param sourceUserId      转账用户userId
     * @param destinationUserId 到账用户userId
     * @param amount            转账金额
     * @param channelTag        调用channelTag,默认JIEFUBAO即可,可不传
     * @param orderDesc         转账描述
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/amount2amount")
    public @ResponseBody
    Object amountToAmount(HttpServletRequest request,
                          @RequestParam("sourceUserId") String sourceUserId,
                          @RequestParam("destinationUserId") String destinationUserId, @RequestParam("amount") String amount,
                          @RequestParam(value = "isVerify", required = false, defaultValue = "true") String isVerify,
                          @RequestParam(value = "payPass", required = false) String payPass,
                          @RequestParam(value = "channelTag", required = false, defaultValue = "JIEFUBAO") String channelTag,
                          @RequestParam(value = "orderDesc", required = false, defaultValue = "") String orderDesc) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (Tools.checkAmount(amount) == false) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
            return map;
        }
        URI uri;
        String url;
        String result;
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestEntity;
        JSONObject jsonObject;

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + sourceUserId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================/v1.0/user/brand/query/managerid:" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        resultbrand = jsonObject.getJSONObject("result");
        if (!resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.您不是贴牌商,无法进行转账!");
            return map;
        }

        if (!"notVerify".equals(isVerify)) {
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/paypass/auth/userid";
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("userId", sourceUserId);
            requestEntity.add("paypass", payPass);
            LOG.info("参数================/v1.0/user/paypass/auth/userid:" + requestEntity);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                LOG.info("RESULT================" + result);
                jsonObject = JSONObject.fromObject(result);
            } catch (Exception e) {
                LOG.error("验证用户支付密码异常=============================用户userId:" + sourceUserId);
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哟,请稍后重试!");
                return map;
            }

            if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                        ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哟,请稍后重试!");
                return map;
            }
        }

        // 获取转账用户信息
        Map<String, Object> userInfoMap = getUserInfo(sourceUserId);
        if (!CommonConstants.SUCCESS.equals((String) userInfoMap.get(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, userInfoMap.get(CommonConstants.RESP_MESSAGE));
            return map;
        }
        JSONObject userInfo = (JSONObject) userInfoMap.get(CommonConstants.RESULT);
        String sourceBranId = userInfo.getString("brandId");
        String sourcePhone = userInfo.getString("phone");
        // 获取到账用户信息
        userInfoMap = getUserInfo(destinationUserId);
        if (!CommonConstants.SUCCESS.equals((String) userInfoMap.get(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, userInfoMap.get(CommonConstants.RESP_MESSAGE));
            return map;
        }
        userInfo = (JSONObject) userInfoMap.get(CommonConstants.RESULT);
        String destinationBranId = userInfo.getString("brandId");
        String destinationPhone = userInfo.getString("phone");
        if (sourceBranId == null || destinationBranId == null || !sourceBranId.equals(destinationBranId)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "转账失败,帐号不在同一个贴牌上");
            return map;
        }

        // 获取转账用户帐户信息
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/query/phone";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", sourcePhone);
        requestEntity.add("brand_id", sourceBranId + "");
        LOG.info("参数================/v1.0/user/account/query/phone:" + requestEntity);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("生成订单失败,原因为查询用户信息异常=============================用户手机号:" + sourcePhone + ",贴牌号:" + sourceBranId);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哟,请稍后重试!");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哟,请稍后重试!");
            return map;
        }

        JSONObject userAccountJson = jsonObject.getJSONObject(CommonConstants.RESULT);
        String userBalance = userAccountJson.getString("balance");
        // 判断用户余额是否充足
        if (new Integer(-1).equals((new BigDecimal(userBalance)).compareTo(new BigDecimal(amount)))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "转账失败,原因:帐户余额不足,请确认余额后再转账.");
            return map;
        }

        /** 调用下单，需要得到转账用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "4"); // 4为余额内互转
        requestEntity.add("phone", sourcePhone);
        requestEntity.add("amount", "-" + amount);
        requestEntity.add("channel_tag", "JIEFUBAO");
        requestEntity.add("desc", orderDesc);
        String order;
        String realAmount;
        String userid;
        String sourceOrderCode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/transactionclear/payment/add:" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        JSONObject resultObj = jsonObject.getJSONObject("result");
        sourceOrderCode = resultObj.getString("ordercode");
        realAmount = resultObj.getString("amount");
        userid = resultObj.getString("userid");

        // 将用户转账的钱冻结到冻结帐户中
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/freeze";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", sourceUserId);
        requestEntity.add("amount", amount);
        requestEntity.add("add_or_sub", "0");
        requestEntity.add("order_code", sourceOrderCode);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/account/freeze" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/freeze添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        /** 调用下单，需要得到到账用户的订单信息 */
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "4"); // 4为余额内互转
        requestEntity.add("phone", destinationPhone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", "JIEFUBAO");
        requestEntity.add("desc", orderDesc);
        String destinationOrderCode;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/transactionclear/payment/add" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        resultObj = jsonObject.getJSONObject("result");
        destinationOrderCode = resultObj.getString("ordercode");
        realAmount = resultObj.getString("amount");
        userid = resultObj.getString("userid");

        // 将用户转账的钱解冻到余额中
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/freeze";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", sourceUserId);
        requestEntity.add("amount", realAmount);
        requestEntity.add("add_or_sub", "1");
        requestEntity.add("order_code", sourceOrderCode);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/account/freeze" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/freeze异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        // 转账用户从余额中扣钱
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/update";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", sourceUserId);
        requestEntity.add("amount", amount);
        requestEntity.add("addorsub", "1");
        requestEntity.add("order_code", sourceOrderCode);

        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT=======扣钱=========/v1.0/user/account/update" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (RestClientException e) {
            LOG.error("==========/v1.0/user/account/update异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        // 到账用户余额中加钱
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/account/update";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", destinationUserId);
        requestEntity.add("amount", amount);
        requestEntity.add("addorsub", "0");
        requestEntity.add("order_code", destinationOrderCode);

        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT=======加钱=========/v1.0/user/account/update" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/update异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "转账成功");
        return map;
    }

    // 调用获取用户信息接口
    private Map<String, Object> getUserInfo(String userId) {
        Map<String, Object> map = new HashMap<String, Object>();
        /** 调用下单，需要得到用户的订单信息 */

        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("user", "error url request!");
        String url = uri.toString() + "/v1.0/user/find/by/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userId", userId);
        JSONObject resultChaObj;
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================/v1.0/user/find/by/userid:" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/find/by/userid查询用户信息异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        // LOG.info("result================" + jsonObject);
        if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, jsonObject.containsKey(CommonConstants.RESP_MESSAGE)
                    ? jsonObject.getString(CommonConstants.RESP_MESSAGE) : "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        map.put(CommonConstants.RESULT, jsonObject.getJSONObject("result"));
        return map;

    }


    //用于差额购买产品的接口

    /**
     * 购买一个产品订单
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchase/difference")
    public @ResponseBody
    Object purchaseDifference(HttpServletRequest request,
                              @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                              @RequestParam(value = "phone") String phone,
                              @RequestParam(value = "amount") String amount,
                              @RequestParam(value = "channe_tag", required = false, defaultValue = "YILIAN") String channeltag,
                              @RequestParam(value = "order_desc") String orderdesc,
                              @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                              @RequestParam(value = "bank_card") String bankcard,
                              @RequestParam(value = "product_id") String prodid,
                              Model model) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<String, Object>();
        /***安全校验**/


		/*if(!"".equals(phone)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		    map.put(CommonConstants.RESP_MESSAGE, "抱歉,购买产品通道暂时维护中,开放时间请留意公告,感谢配合!");
			return map;
		}*/

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        int grade1;
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            grade1 = jSONObject.getInt("grade");
            if (grade >= grade1) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                return map;
            }

        }

        if (grade > 0) {
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
            result = restTemplate.getForObject(url, String.class);
            jSONObject = JSONObject.fromObject(result);
            JSONArray jsonArray = jSONObject.getJSONArray("result");
            resp_code = jsonObject.getString("resp_code");
            if (resp_code.equals(CommonConstants.SUCCESS)) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int grade2 = object.getInt("grade");
                    String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                    if ("1".equals(subtract)) {
                        for (int j = 0; j < jsonArray.size(); j++) {
                            object = (JSONObject) jsonArray.get(j);
                            int grade3 = object.getInt("grade");
                            String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();

                            if ("0".equals(subtract1)) {
                                String money = object.getString("money");

                                amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                            }
                        }

                    } else {
                        continue;
                    }
                }

            }

        }

        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/bank/default/cardno";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("cardno", bankcard);
        requestEntity.add("type", "0");
        result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
        JSONObject resultObj1;
        try {
            jsonObject = JSONObject.fromObject(result);
            resultObj1 = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("查询银行卡信息出错");
            map.put("resp_code", "failed");
            map.put("channel_type", "jf");
            map.put("resp_message", "查询银行卡信息有误");
            return map;
        }

        String nature = resultObj1.getString("nature");
        String bankName = resultObj1.getString("bankName");

        channeltag = "YLDZ_QUICK";

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        requestEntity.add("bank_card", bankcard);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard + "&userId=" + userId);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                return map;
            }
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        //map.put(CommonConstants.RESP_MESSAGE, "亲.支付成功!");
        map.put(CommonConstants.RESULT, jsonObject.getString(CommonConstants.RESULT));
        return map;

    }

    /**
     * 支付宝、微信扫码购买产品
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchase/aliandwx/difference")
    public @ResponseBody
    Object aliPayAndWXPurchaseDifference(HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                                         @RequestParam(value = "phone") String phone,
                                         @RequestParam(value = "amount") String amount,
                                         @RequestParam(value = "channe_tag", required = false, defaultValue = "ALI") String channeltag,
                                         @RequestParam(value = "order_desc") String orderdesc,
                                         @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                                         @RequestParam(value = "product_id") String prodid,
                                         Model model) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        if ("ALI".equalsIgnoreCase(channeltag)) {
            channeltag = "SPALI_PAY";
        } else {
            //channeltag = "SPWX_PAY";
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "微信支付暂不可用,程序员在努力加班处理中!");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("微信支付暂不可用,程序员在努力加班处理中!", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("微信支付暂不可用,程序员在努力加班处理中!", "UTF-8"));
            return map;
        }

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("用户在黑名单中!", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("用户在黑名单中!", "UTF-8"));
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("无法重复购买此权限!", "UTF-8"));
                response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("无法重复购买此权限!", "UTF-8"));
                return map;
            }

        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("您已成为主宰,不可升级!", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("您已成为主宰,不可升级!", "UTF-8"));
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                //map.put(CommonConstants.RESULT, ipAddress+"/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard+"&userId=" + userId);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
                response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
                return map;
            }
        }

        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }


		/*map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		//map.put(CommonConstants.RESP_MESSAGE, "亲.支付成功!");
		map.put(CommonConstants.RESULT, jsonObject.getString(CommonConstants.RESULT));*/

        return null;

    }

    /**
     * 合利宝
     *
     * @param request
     * @param sbrandId
     * @param phone
     * @param amount
     * @param channeltag
     * @param orderdesc
     * @param purcasetype
     * @param bankcard
     * @param prodid
     * @param difference
     * @param model
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchase/hlb")
    public @ResponseBody
    Object purchaseHLB(HttpServletRequest request,
                       @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                       @RequestParam(value = "phone") String phone,
                       @RequestParam(value = "amount") String amount,
                       @RequestParam(value = "channe_tag", required = false, defaultValue = "HLB") String channeltag,
                       @RequestParam(value = "order_desc") String orderdesc,
                       @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                       @RequestParam(value = "bank_card", required = false, defaultValue = "") String bankcard,
                       @RequestParam(value = "product_id") String prodid,
                       @RequestParam(value = "orderCode",required = false ) String orderCode,
                       // 该字段判断是否是差额会员升级的方式
                       @RequestParam(value = "difference", required = false, defaultValue = "-1") String difference,
                       Model model) throws UnsupportedEncodingException {
        if(orderCode == null){
            orderCode = UUIDGenerator.getDateTimeOrderCode();
        }

        Map<String, Object> map = new HashMap<String, Object>();

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户黑名单异常,请稍后重试!");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户信息异常,请稍后重试!");
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            amount = jSONObject.getString("money");
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                return map;
            }

        } else {
            LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级产品查询异常");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该产品暂时无法购买");
            return map;

        }
        //如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            //判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();

                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");

                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }

                }

            }
        }
        if ("HLB".equals(channeltag)) {
            channeltag = "HLB_QUICK";
        }else if("HLB_YL".equals(channeltag)){
            channeltag = "HLB_YL_QUICK";
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询贴牌商信息出现异常,请稍后重试!");
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        requestEntity.add("bank_card", bankcard);
        requestEntity.add("orderCode", orderCode);
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单出现异常,请稍后重试!");
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", orderCode);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付异常,请稍后重试!");
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard + "&userId=" + userId);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                return map;
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "生成支付码成功");
        map.put(CommonConstants.RESULT, jsonObject.getString(CommonConstants.RESULT));
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/facade/purchases")
    public @ResponseBody
    Object purchases(HttpServletRequest request,
                     @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                     @RequestParam(value = "phone") String phone,
                     @RequestParam(value = "amount") String amount,
                     @RequestParam(value = "channe_tag", required = false, defaultValue = "YILIAN") String channeltag,
                     @RequestParam(value = "order_desc") String orderdesc,
                     @RequestParam(value = "purcase_type", required = false, defaultValue = "0") String purcasetype,
                     @RequestParam(value = "bank_card", required = false, defaultValue = "") String bankcard,
                     @RequestParam(value = "product_id") String prodid,
                     //该字段判断是否是差额会员升级的方式
                     @RequestParam(value = "difference", required = false, defaultValue = "-1") String difference,
                     Model model) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<String, Object>();


		/*if(!"".equals(phone)){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		    map.put(CommonConstants.RESP_MESSAGE, "抱歉,购买产品通道暂时维护中,开放时间请留意公告,感谢配合!");
			return map;
		}*/

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户黑名单异常,请稍后重试!");
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId + "");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户信息异常,请稍后重试!");
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code = jsonObject.getString("resp_code");
        if (resp_code.equals(CommonConstants.SUCCESS)) {
            amount = jSONObject.getString("money");
            if (grade >= jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                return map;
            }

        } else {
            LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级产品查询异常");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该产品暂时无法购买");
            return map;

        }
        //如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            //判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();

                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");

                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }

                }

            }
        }
        if ("YILIAN".equals(channeltag) || channeltag.contains("QUICK")) {
            channeltag = "YLDZ_QUICK";
        }

        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询贴牌商信息出现异常,请稍后重试!");
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            return map;
        }

        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        requestEntity.add("bank_card", bankcard);
        String order;
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            order = resultObj.getString("ordercode");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "生成订单出现异常,请稍后重试!");
            return map;
        }

        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";

        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", order);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请求支付异常,请稍后重试!");
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if (!CommonConstants.SUCCESS.equals(respCode)) {
            if ("999990".equals(respCode)) {
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" + bankcard + "&userId=" + userId);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                return map;
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        //map.put(CommonConstants.RESP_MESSAGE, "亲.支付成功!");
        map.put(CommonConstants.RESULT, jsonObject.getString(CommonConstants.RESULT));
        return map;

    }

    /** 支付宝购买产品 通过商城购买 增加了了订单号
     * @throws Exception */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/facade/purchase/aliPay")
    public @ResponseBody Object aliPay(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam(value="brandId",required=false,defaultValue="-1")String sbrandId,
                                       @RequestParam(value = "phone") String phone,
                                       @RequestParam(value = "amount") String amount,
                                       @RequestParam(value = "channe_tag", required = false, defaultValue = "ALI") String channeltag,
                                       @RequestParam(value = "order_desc") String orderdesc,
                                       @RequestParam(value = "purcase_type",required=false,defaultValue="0") String purcasetype,
                                       @RequestParam(value = "product_id") String prodid,
                                       //该字段判断是否是差额会员升级的方式
                                       @RequestParam(value = "difference",required=false,defaultValue="-1") String difference,
                                       @RequestParam(value = "orderCode") String orderCode,
                                       Model model) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        if("ALI".equalsIgnoreCase(channeltag)) {
            channeltag = "SPALI_PAY";
        }else if("WX".equalsIgnoreCase(channeltag)) {
            channeltag = "SPWX_PAY";
        }else if("ALIAPP".equals(channeltag)){
            channeltag = "SPALI_PAY_APP";
        }else{
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该支付暂不可用,程序员在努力加班处理中!");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("该支付暂不可用,程序员在努力加班处理中!", "UTF-8"));
            return map;
        }
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "3");
        JSONObject jsonObject;
        String rescode;
        String result;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            rescode = jsonObject.getString("resp_code");
        } catch (Exception e) {
            LOG.error("==========/v1.0/risk/blackwhite/query/phone查询用户黑名单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("用户在黑名单中!", "UTF-8"));
            return map;
        }
        long brandId = -1;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e1) {
            brandId = -1;
        }
        /** 判断贴牌商无法购买产品 ***/
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/query/phone";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        requestEntity.add("brandId", brandId+"");
        restTemplate = new RestTemplate();
        JSONObject resultObju;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultObju = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String userId = "0";
        int grade = Integer.parseInt(resultObju.getString("grade"));
        if (resultObju.containsKey("id")) {
            userId = resultObju.getString("id");
        } else {
            userId = "0";
        }
        /***查询产品等级 {id}**/
        /** 如果是支付订单，返佣，并且设置用户的刷卡费率 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + prodid;
        result = restTemplate.getForObject(url, String.class);
        jsonObject = JSONObject.fromObject(result);
        JSONObject jSONObject = jsonObject.getJSONObject("result");
        String resp_code=jsonObject.getString("resp_code");
        if(resp_code.equals(CommonConstants.SUCCESS)) {
            if(grade>=jSONObject.getInt("grade")) {
                LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("无法重复购买此权限!", "UTF-8"));
                return map;
            }
        }
        // 如果 difference不等于-1 代表是差额会员升级的方式
        if (!"-1".equals(difference)) {
            // 判断当前等级大于普通会员等级才可以差额购买会员
            if (grade > 0) {
                restTemplate = new RestTemplate();
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/thirdlevel/prod/brand/" + brandId;
                result = restTemplate.getForObject(url, String.class);
                jSONObject = JSONObject.fromObject(result);
                JSONArray jsonArray = jSONObject.getJSONArray("result");
                resp_code = jsonObject.getString("resp_code");
                if (resp_code.equals(CommonConstants.SUCCESS)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        int grade2 = object.getInt("grade");
                        String subtract = new BigDecimal(grade2).subtract(new BigDecimal(grade)).toString();
                        if ("1".equals(subtract)) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                object = (JSONObject) jsonArray.get(j);
                                int grade3 = object.getInt("grade");
                                String subtract1 = new BigDecimal(grade3).subtract(new BigDecimal(grade)).toString();
                                if ("0".equals(subtract1)) {
                                    String money = object.getString("money");
                                    amount = new BigDecimal(amount).subtract(new BigDecimal(money)).toString();
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        /** 通过用户ID判定是否为贴牌 */
        restTemplate = new RestTemplate();
        uri = util.getServiceUrl("user", "error url request!");
        url = uri.toString() + "/v1.0/user/brand/query/managerid?manager_id=" + userId;
        JSONObject resultbrand;
        try {
            result = restTemplate.getForObject(url, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            resultbrand = jsonObject.getJSONObject("result");
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/brand/query/managerid查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepageurchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        if (resultbrand.containsKey("id")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "您以成为主宰，不可升级");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("您已成为主宰,不可升级!", "UTF-8"));
            return map;
        }
        uri = util.getServiceUrl("transactionclear", "error url request!");
        url = uri.toString() + "/v1.0/transactionclear/payment/add";
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("type", "1");
        requestEntity.add("phone", phone);
        requestEntity.add("amount", amount);
        requestEntity.add("channel_tag", channeltag);
        requestEntity.add("desc", orderdesc);
        requestEntity.add("product_id", prodid);
        requestEntity.add("orderCode", orderCode);
        long brandid;
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
            JSONObject resultObj = jsonObject.getJSONObject("result");
            brandid = resultObj.getLong("brandid");
        } catch (Exception e) {
            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        uri = util.getServiceUrl("paymentchannel", "error url request!");
        url = uri.toString() + "/v1.0/paymentchannel/topup/request";
        /** 根据的用户手机号码查询用户的基本信息 */
        requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("amount", amount);
        requestEntity.add("ordercode", orderCode);
        requestEntity.add("brandcode", brandid + "");
        requestEntity.add("orderdesc", orderdesc);
        requestEntity.add("channel_tag", channeltag);
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("RESULT================purchase" + result);
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
            map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
            return map;
        }
        String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
        if(!CommonConstants.SUCCESS.equals(respCode)){
            if("999990".equals(respCode)){
                map.put(CommonConstants.RESP_CODE, "999990");
                map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
                return map;
            }else{
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
                map.put(CommonConstants.RESULT,ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
                return map;
            }
        }
        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
