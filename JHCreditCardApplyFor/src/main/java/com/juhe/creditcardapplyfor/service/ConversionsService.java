package com.juhe.creditcardapplyfor.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.juhe.creditcardapplyfor.bo.CallbackBO;

import com.juhe.creditcardapplyfor.business.impl.ConversionBusinessImpl;
import com.juhe.creditcardapplyfor.entity.ConversionEntity;
import com.juhe.creditcardapplyfor.utils.FileUtils;
import com.juhe.creditcardapplyfor.utils.HttpClientUtils;
import com.juhe.creditcardapplyfor.utils.RsaUtils;
import com.juhe.creditcardapplyfor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huhao
 * @title: ConversionController
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002314:04
 */

@SuppressWarnings("all")
@RestController
@RequestMapping("/v1.0/creditcardapplyfor/conversion")
public class ConversionsService {
    @Value("${baseHost}")
    private String baseHost;


    @Value("${privateKey}")
    private String privateKey;

    private static final Logger LOG= LoggerFactory.getLogger(ConversionsService.class);

    @Value("${conversionCallbackUrl}")
    private String conversionCallbackUrl;

    @Autowired
    private ConversionBusinessImpl conversionBusiness;

    /**
     * 积分通道列表
     * @return
     */
    @RequestMapping(value = "/conversion/list",method = RequestMethod.GET)
    public Object listConversion(){
        String url = baseHost + "/conversion/open/channel/list";
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求头:"+baseMap);
        //httpClient请求
        String conversionList = HttpClientUtils.doGet(url,baseMap);
        LOG.info("积分列表:"+conversionList);
        return conversionList;
    }

    /**
     * 根据id获取通道详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/conversion/info/{id}",method = RequestMethod.GET)
    public Object getChannelInfo(@PathVariable("id") String id){

        String url = baseHost + "/conversion/open/channel/" + id; //{通道id} 通道列表中的id
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求头:"+baseMap);
        //httpClient请求
        String channelDetails = HttpClientUtils.doGet(url,baseMap);
        LOG.info("通道详情:"+channelDetails);
        return channelDetails;
    }

    /**
     * 获取类目信息
     * @return
     */
    @RequestMapping(value = "/conversion/tagsList",method = RequestMethod.GET)
    public Object listTag(@RequestParam(value = "channelId",required = true) String channelId,
                          @RequestParam(value = "userId",required = true) String onlyNo){

        String url = baseHost + "/conversion/open/channel/tags";
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求头:"+baseMap);
        //Body参数
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("channelId",channelId); //通道id  通道列表上的channelId
        paramMap.put("onlyId",onlyNo);       //自己用户id
        //httpClient请求
        String tagsList = HttpClientUtils.doGet(url, baseMap, paramMap);

        LOG.info("类目信息:"+tagsList);

        return tagsList;

    }

    /**
     * 类目详情
     * @param id
     * @return
     */

    @RequestMapping(value = "/conversion/tagsInfo/{id}",method = RequestMethod.GET)
    public Object tagsInfo(@PathVariable("id") String id){

        String url = baseHost + "/conversion/open/channel/tag/"+id; //{类目列表id} 类目列表中的id
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求头:"+baseMap);
        //httpClient请求
        String channelDetails = HttpClientUtils.doGet(url,baseMap);

        LOG.info("类目详情:"+channelDetails);
        return channelDetails;
    }

    /**
     * 通道价格详情
     * @param oemChannelId
     * @return
     */
    @RequestMapping(value = "/conversion/ChannelPrice/{oemChannelId}",method = RequestMethod.GET)
    public Object conversionChannelPriceDetails(@PathVariable("oemChannelId") String oemChannelId){
        String url = baseHost + "/conversion/open/channel/price/"+oemChannelId; //{通道列表id} 通道列表中的id
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求头:"+baseMap);
        //httpClient请求
        String channelPriceDetails = HttpClientUtils.doGet(url,baseMap);

        LOG.info("通道价格详情:"+channelPriceDetails);
        return channelPriceDetails;
    }

    /**
     * 保存订单记录  POST请求 body带参和上传图片
     * 上传图片类型QR_CODE
     */
    @RequestMapping(value = "/conversion/save",method = RequestMethod.POST)
    public Object  conversionSave(
            @RequestParam(value = "oemChannelId",required = true) String oemChannelId,
            @RequestParam(value = "channelTagId",required = true)String channelTagId,
            @RequestParam(value = "content",required = false)String content,
            @RequestParam(value = "type",required = true)String type,
            @RequestParam(value = "multipartFile",required = false) MultipartFile multipartFile){
        String url = baseHost + "/conversion/open/channel/save";
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求头:" + baseMap);
        //Body参数
        Map<String,String> paramMap = new HashMap<>();

        String clientNo = Utils.getConversionClientId();


        paramMap.put("oemChannelId",oemChannelId); //通道id  通道列表上的id
        paramMap.put("clientNo",clientNo); //客户端id  唯一请求标识
        paramMap.put("callbackUrl",conversionCallbackUrl); //回调路径 测试
        paramMap.put("channelTagId",channelTagId); //类目id 类目列表上的id
        paramMap.put("type",type);  //通道类型  通道列表上的通道类型

            try {

                String result;
                if("EXCHANGE_CODE".equals(type)||"VOUCHER_AND_IMG".equals(type)){
                    paramMap.put("content",content);
                }
                if ("QR_CODE".equals(type)||"VOUCHER_AND_IMG".equals(type)){
                    File file = FileUtils.getFile(multipartFile);
                    result = HttpClientUtils.doPost(url,baseMap,paramMap,file);
                }else{
                    result = HttpClientUtils.doPost(url,baseMap,paramMap);
                }

                LOG.info("返回支付信息:"+result);


                JSONObject jsonObject = JSON.parseObject(result);
                LOG.info("响应状态码:"+jsonObject.getString("status"));
                if ("200".equals(jsonObject.getString("status"))){

                    ConversionEntity conversionEntity = new ConversionEntity();
                    conversionEntity.setClientNo(clientNo);
                    conversionEntity.setOemChannelId(oemChannelId);
                    conversionEntity.setChannelTagId(channelTagId);
                    conversionEntity.setCreateTime(Utils.getDate());
                    conversionEntity.setStatus("待成交");
                    ConversionEntity conversion = conversionBusiness.saveOrder(conversionEntity);
                    LOG.info("未完成订单:"+conversion);
                }

                return result;
            } catch (IOException e) {
                LOG.error("文件转换失败!");
                e.printStackTrace();
            }
        //paramMap.put("content","12334873543546633"); //兑换码 当通道类型为EXCHANGE_CODE才填写该参数  必填：否


        //File file = new File("D:\\360MoveData\\Users\\Administrator\\Desktop\\123.jpg"); //只有通道类型为QR_CODE时才上传图片 必填：否
        //httpClient请求

        return "订单失败";

    }

    /**
     * 回调入口
     * @param callbackBO
     * @return
     */
    @RequestMapping(value = "/conversion/getconversion",method = RequestMethod.POST)
    public Object getConversion(@RequestBody CallbackBO callbackBO){
        LOG.info("请求参数:" + callbackBO);
        String callbackType = callbackBO.getCallbackType();
        LOG.info("类型:" + callbackType);

        String sign = callbackBO.getSign();

        String timestamp = callbackBO.getTimestamp();

        String result = RsaUtils.decrypt(privateKey, sign);


        if("CALLBACK_SUCCESS".equals(callbackType) && timestamp.equals(result)){

            String clientNo = callbackBO.getClientNo();
            ConversionEntity conversionEntity = conversionBusiness.getOrderByClientNo(clientNo);

            String transNo = callbackBO.getTransNo();
            Integer price = callbackBO.getUserPrice();

            BigDecimal userPrice = new BigDecimal(price);

            conversionEntity.setTransNo(transNo);
            conversionEntity.setUserPrice(userPrice);
            conversionEntity.setCreateTime(Utils.getDate());

            conversionEntity.setStatus("已成交");
            ConversionEntity cardOrder = conversionBusiness.updateOrder(conversionEntity);



            LOG.info("订单添加成功:" + cardOrder);
            return "{\"result\": {\"message\": \"成功\",\"callbackType\": \"CALLBACK_SUCCESS\"}}";
        }else {
            return "{\"result\": {\"message\": \"失败\",\"callbackType\": \"CALLBACK_ERROR\"}}";
        }
    }

    /**
     * 订单查询接口
     * @return
     */
    @RequestMapping(value = "/conversion/orderState",method = RequestMethod.GET)
    public Object orderState(@RequestParam(value = "transNo",required = false)String transNo,
                             @RequestParam(value = "clientNO",required = false)String clientNO){
        String url = baseHost + "/conversion/open/channel/order/state";
        Map<String, String> baseMap = Utils.getHead();
        //建立Body参数map
        Map<String,String> bodyMap = new HashMap<>();
        //两个参数至少需要传一个，当两个都有时，以transNo为准。
        if (transNo != null){
            bodyMap.put("transNo",transNo);  //积分保存记录后获取的订单号
        }
        bodyMap.put("clientNo",clientNO);  //客户端id唯一请求标识
        //httpClient请求
        String orderState = HttpClientUtils.doGet(url,baseMap,bodyMap);

        LOG.info("查询的订单结果:" + orderState);

        return orderState;
    }


    /**
     * 类目操作流程接口
     * @param id
     * @return
     */
    @RequestMapping(value = "/conversion/excDetail.html/{id}",method = RequestMethod.GET)
    public Object flow(@PathVariable("id")String id){
        String url ="https://lion.51ley.com/recommend/excDetail.html?id=" + id;
        LOG.info("url："+url+"===============类目id：=========================="+id);
        Map<String, String> baseMap = Utils.getHead();

        //httpClient请求
        String flowHtml = HttpClientUtils.doGet(url,baseMap);

        LOG.info("流程图页面:"+flowHtml);
        return flowHtml;
    }


    /**
     * 短信兑换PHONE获取验证码接口
     * @param oemChannelId 通道id
     * @param clientNO     客户端编号
     * @param channelTagId 类目id
     * @param phone        手机号码
     * @param type         通道类型
     * @return
     */
    @RequestMapping(value = "/conversion/getSmsCode",method = RequestMethod.POST)
    public Object getSmsCode(@RequestParam(value = "oemChannelId",required = true)String oemChannelId,
                             @RequestParam(value = "clientNo",required = true)String clientNO,
                             @RequestParam(value = "channelTagId",required = true)String channelTagId,
                             @RequestParam(value = "phone",required = true)String phone,
                             @RequestParam(value = "type",required = true)String type){
        String url = baseHost + "/conversion/open/channel/getSmsCode";
        Map<String, String> baseMap = Utils.getHead();
        Map<String,String> bodyMap = new HashMap<>();
        bodyMap.put("oemChannelId",oemChannelId);
        bodyMap.put("clientNo",clientNO);
        bodyMap.put("callbackUrl",conversionCallbackUrl);
        bodyMap.put("channelTagId",channelTagId);
        bodyMap.put("phone",phone);
        bodyMap.put("type",type);
        LOG.info("bodyMap================"+ bodyMap);
        //httpClient请求
        String code = HttpClientUtils.doGet(url,baseMap,bodyMap);

        LOG.info("短信兑换PHONE获取验证码:" + code);

        return code;
    }

    /**
     * 短信兑换PHONE 验证图形验证码
     * @param oemChannelId       通道id
     * @param geetestChallenge   图形验证码参数1
     * @param geetestValidate    图形验证码参数2
     * @param geetestSeccode     图形验证码参数3
     * @param phone              手机号码
     * @param type               通道类型
     * @return
     */
    @RequestMapping(value = "/conversion/wemGetsmsByyzm",method = RequestMethod.POST)
    public Object wemGetsmsByyzm(@RequestParam(value = "oemChannelId",required = true)String oemChannelId,
                             @RequestParam(value = "geetestChallenge",required = true)String geetestChallenge,
                             @RequestParam(value = "geetestValidate",required = true)String geetestValidate,
                             @RequestParam(value = "geetestSeccode",required = true)String geetestSeccode,
                             @RequestParam(value = "phone",required = true)String phone,
                             @RequestParam(value = "type",required = true)String type){
        String url = baseHost + "/conversion/open/channel/wemGetsmsByyzm";
        Map<String, String> baseMap = Utils.getHead();
        Map<String,String> bodyMap = new HashMap<>();
        bodyMap.put("oemChannelId",oemChannelId);
        bodyMap.put("geetestChallenge",geetestChallenge);
        bodyMap.put("geetestValidate",geetestValidate);
        bodyMap.put("geetestSeccode",geetestSeccode);
        bodyMap.put("phone",phone);
        bodyMap.put("type",type);
        String code = HttpClientUtils.doGet(url,baseMap,bodyMap);

        LOG.info("验证图形验证码:" + code);

        return code;
    }

    /**
     * 短信兑换PHONE报单接口
     * @param smsCode        手机验证码
     * @param loginKey       登入key
     * @param oemChannelId   通道id
     * @param clientNo       客户端编号
     * @param callbackUrl    回调路径
     * @param channelTagId   类目id
     * @param phone          手机号码
     * @param type           通道类型
     * @return
     */
    @RequestMapping(value = "/conversion/wemSetSms",method = RequestMethod.POST)
    public Object wemSetSms(@RequestParam(value = "smsCode",required = true)String smsCode,
                                 @RequestParam(value = "loginKey",required = true)String loginKey,
                                 @RequestParam(value = "oemChannelId",required = true)String oemChannelId,
                                 @RequestParam(value = "clientNo",required = true)String clientNo,
                                 @RequestParam(value = "callbackUrl",required = true)String callbackUrl,
                                 @RequestParam(value = "channelTagId",required = true)String channelTagId,
                                 @RequestParam(value = "phone",required = true)String phone,
                                 @RequestParam(value = "type",required = true)String type){
        String url = baseHost + "/conversion/open/channel/wemSetSms";
        Map<String, String> baseMap = Utils.getHead();
        Map<String,String> bodyMap = new HashMap<>();
        bodyMap.put("smsCode",smsCode);
        bodyMap.put("loginKey",loginKey);
        bodyMap.put("oemChannelId",oemChannelId);
        bodyMap.put("clientNo",clientNo);
        bodyMap.put("callbackUrl",callbackUrl);
        bodyMap.put("channelTagId",channelTagId);
        bodyMap.put("phone",phone);
        bodyMap.put("type",type);
        String code = HttpClientUtils.doGet(url,baseMap,bodyMap);

        LOG.info("验证图形验证码:" + code);

        return code;
    }


    /**
     * 获取客服链接
     * @param channelId   平台通道id
     * @param onlyNo      下游用户id
     * @return
     */
    @RequestMapping(value = "/conversion/getCustomerServiceUrl",method = RequestMethod.POST)
    public Object getCustomerServiceUrl(@RequestParam(value = "channelId",required = true)String channelId,
                            @RequestParam(value = "onlyNo",required = true)String onlyNo){
        String url = baseHost + "/conversion/open/channel/getCustomerServiceUrl";
        Map<String, String> baseMap = Utils.getHead();
        Map<String,String> bodyMap = new HashMap<>();
        bodyMap.put("channelId",channelId);
        bodyMap.put("onlyNo",onlyNo);

        String serviceUrl = HttpClientUtils.doGet(url,baseMap,bodyMap);

        LOG.info("客户链接:" + serviceUrl);

        return serviceUrl;
    }

}
