package com.juhe.creditcardapplyfor.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.juhe.creditcardapplyfor.async.AsyncMethod;
import com.juhe.creditcardapplyfor.bo.OrderResBO;

import com.juhe.creditcardapplyfor.business.AmountDisRatioConfigService;
import com.juhe.creditcardapplyfor.business.impl.CardOrderBusinessImpl;
import com.juhe.creditcardapplyfor.entity.AmountDisRatioConfig;
import com.juhe.creditcardapplyfor.entity.CardOrderEntity;
import com.juhe.creditcardapplyfor.utils.HttpClientUtils;
import com.juhe.creditcardapplyfor.utils.RsaUtils;
import com.juhe.creditcardapplyfor.utils.Utils;
import com.juhe.creditcardapplyfor.utils.enums.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Transactional
@RestController
@RequestMapping("/v1.0/creditcardapplyfor/creditcard")
public class CreditCardService {

    @Value("${baseHost}")
    private String baseHost;

    @Value("${privateKey}")
    private String privateKey;


    @Value("${callbackUrl}")
    private String callbackUrl;  //回调地址


    private final CardOrderBusinessImpl cardOrderServiceImpl;

    @Autowired
    private AmountDisRatioConfigService amountDisRatioConfigService;

    @Autowired
    private AsyncMethod asyncMethod;


    private static final Logger LOG = LoggerFactory.getLogger(CreditCardService.class);

    @Autowired
    public CreditCardService(CardOrderBusinessImpl cardOrderServiceImpl) {
        this.cardOrderServiceImpl = cardOrderServiceImpl;
    }

    /**
     * 查询申卡订单
     * @param phone
     * @param idCard
     * @param page
     * @param size
     * @param order
     * @param sort
     * @return
     */
    @RequestMapping(value = "/creditcardcntroller/order/query", method = RequestMethod.POST)
    public Object queryOrder(
            @RequestParam(value = "phone",required = false)String phone,
            @RequestParam(value = "idCard",required = false)String idCard,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "20", required = false) int size,
            @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction order,
            @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sort){

        Map<String,Object> map = new HashMap<String,Object>();
        Pageable pageable = new PageRequest(page,size,new Sort(order,sort));
        List<CardOrderEntity> list = null;
        if (phone != null && idCard != null){
            list = cardOrderServiceImpl.listOrderByPhoneAndIdCard(phone,idCard,pageable);
        }else if (phone != null && idCard == null){
            list = cardOrderServiceImpl.listOrderByPhone(phone,pageable);
        }else if (phone == null && idCard != null){
            list = cardOrderServiceImpl.listOrderByIdCard(idCard,pageable);
        }
        map.put(Constants.RESP_CODE.toString(),Constants.SUCCESS.toString());
        map.put(Constants.RESP_MESSAGE.toString(),"查询成功");
        map.put(Constants.RESULT.toString(),list);
        return map;
    }


    /**
     * 银行列表
     *
     * @return
     */
    @RequestMapping(value = "/creditcardcntroller/listbank", method = RequestMethod.GET)
    public Object listBank() {
        String url = baseHost + "/credit/open/apply/card/bank";
        LOG.info("--------银行列表-------");
        Map<String, String> baseMap = Utils.getHead();
        //建立GET请求

        return HttpClientUtils.doGet(url, baseMap);
    }

    /**
     * 主题列表
     *
     * @return
     */
    @RequestMapping(value = "/creditcardcntroller/listcardtopic", method = RequestMethod.GET)
    public Object listCardTopic() {
        LOG.info("--------主题列表-------");
        String url = baseHost + "/credit/open/apply/card/topic";
        Map<String, String> baseMap = Utils.getHead();

        //建立GET请求


        return HttpClientUtils.doGet(url, baseMap);
    }

    /**
     * 银行卡标签列表
     */
    @RequestMapping(value = "/creditcardcntroller/listcardtag", method = RequestMethod.GET)
    public Object listCardTag() {
        LOG.info("--------银行卡标签列表-------");
        String url = baseHost + "/credit/open/apply/card/tag";
        Map<String, String> baseMap = Utils.getHead();

        //建立GET请求


        return HttpClientUtils.doGet(url, baseMap);
    }


    /**
     * 银行卡通道列表
     */
    @RequestMapping(value = "/creditcardcntroller/listcardchannel", method = RequestMethod.POST)
    public Object listCardChannel(
            @RequestParam(value = "bankIds", required = false) List<Long> bankIds,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(value = "topicIds", required = false) List<Long> topicIds,
            @RequestParam(value = "current", required = false) Integer current) {

        LOG.info("--------银行卡通道列表-------");
        String url = baseHost + "/credit/open/apply/card/channel";
        Map<String, String> baseMap = Utils.getHead();

        Map<String, Object> map = new HashMap<>();

       /* Integer current = card.getCurrent();

        List<Long> bankIds = card.getBankIds();
        List<Long> tagIds = card.getTagIds();
        List<Long> topicIds = card.getTopicIds();*/


        LOG.info("请求参数:bankIds:" + bankIds + ",tagIds:" + tagIds + ",topicIds:" + topicIds + ",current:" + current);
        map.put("bankIds", bankIds);
        map.put("tagIds", tagIds);
        map.put("topicIds", topicIds);
        map.put("current", current);
        String data = JSON.toJSONString(map);

        //建立post请求

        return HttpClientUtils.doPost(url, baseMap, data);
    }

    /**
     * 卡种通道详情
     */
    @RequestMapping(value = "/creditcardcntroller/getcardchannelbyid/{id}", method = RequestMethod.GET)
    public Object getCardChannelById(@PathVariable("id") String id) {
        LOG.info("--------卡种通道详情-------");
        String url = baseHost + "/credit/open/apply/card/" + id;
        Map<String, String> baseMap = Utils.getHead();
        LOG.info("请求参数:id=" + id);


        //建立GET请求

        return HttpClientUtils.doGet(url, baseMap);
    }

    /**
     * 保存网申
     */
    @RequestMapping(value = "/creditcardcntroller/saveCard", method = RequestMethod.POST)
    public Object saveCard(HttpServletRequest httpServletRequest,
                           @RequestParam(value = "name", required = true) String bname,
                           @RequestParam(value = "mobile", required = true) String bmobile,
                           @RequestParam(value = "idCard", required = true) String bidCard,
                           @RequestParam(value = "stationChannelId", required = false) String bstationChannelId,
                           @RequestParam(value = "stationBankCardChannelId", required = true) String bstationBankCardChannelId) {

        LOG.info("--------保存网申-------");
        String url = baseHost + "/credit/open/apply/card/save";
        Map<String, String> baseMap = Utils.getHead();
        //建立请求参数
        Map<String, Object> map = new HashMap<>();

        /*String access_token = httpServletRequest.getHeader("Access_Token");
        JSONObject userInfoJsonObject = null;
        try {
            userInfoJsonObject = tokenUtils.getUserInfoJsonObject(access_token);
        } catch (Exception e) {
            map.put(Constants.RESP_CODE.toString(),Constants.ERROR.toString());
            map.put(Constants.RESP_MESSAGE.toString(),"token无效");
            return JSON.toJSONString(map);
        }
        String userId = userInfoJsonObject.getString("id");*/
        /*String mobile = bmobile;
        String idCard = bidCard;
        String name = bname;*/
        String clientNo = Utils.getClientId();
        Long stationBankCardChannelId = null;
        Long stationChannelId = null;
        if (bstationBankCardChannelId != null && !"".equals(bstationBankCardChannelId)) {
            stationBankCardChannelId = Long.valueOf(bstationBankCardChannelId);
        }
        if (bstationChannelId != null && !"".equals(bstationChannelId)) {
            stationChannelId = Long.valueOf(bstationChannelId);
        }

        LOG.info("请求参数:");
        //申请人手机号
        map.put("mobile", bmobile);
        LOG.info("mobile:" + bmobile);
        //申请人姓名
        map.put("name", bname);
        LOG.info("name:" + bname);
        //申请人身份证号
        map.put("idCard", bidCard);
        LOG.info("idCard:" + bidCard);
        //站点通道id  网申列表中的的stationChannelId
        map.put("stationChannelId", stationChannelId);
        LOG.info("stationChannelId:" + stationChannelId);
        //回调地址
        map.put("callbackUrl", callbackUrl);
        LOG.info("callbackUrl:" + callbackUrl);
        //客户端编号，唯一请求标识(订单号)
        map.put("clientNo", clientNo);
        LOG.info("clientNo:" + clientNo);
        //卡种通道id
        map.put("stationBankCardChannelId", stationBankCardChannelId);
        LOG.info("stationBankCardChannelId:" + stationBankCardChannelId);


        //建立post请求，传递json格式参数
        String resultText = HttpClientUtils.doPost(url, baseMap, JSON.toJSONString(map));

        JSONObject jsonObject = JSON.parseObject(resultText);
        String status = jsonObject.getString("status");
        LOG.info("响应状态:" + status);


        if ("200".equals(status)) {
            JSONObject result = (JSONObject) jsonObject.get("result");
            LOG.info("响应结果:" + result);
            String tradeNo = result.get("tradeNo").toString();

            CardOrderEntity entity = new CardOrderEntity();
            entity.setClientNo(clientNo);
            entity.setUserName(bname);
            entity.setUserPhone(bmobile);
            entity.setCardId(bidCard);
            entity.setCreateTime(Utils.getDate());
            entity.setTradeNo(tradeNo);
            entity.setStationChannelId(stationChannelId + "");
            entity.setBankCardChannelId(stationBankCardChannelId + "");
            entity.setStatus("待成交");
           // entity.setUseId(userId);
            CardOrderEntity cardOrderEntity = cardOrderServiceImpl.saveOrder(entity);
            LOG.info("待成交订单:" + cardOrderEntity);

        } else {
            Object result = jsonObject.get("result");
            LOG.error("响应结果:" + result);
        }
        LOG.info("返回链接:" + resultText);
        return resultText;
    }

    /**
     * 申卡
     * @return
     */
    @RequestMapping(value = "/creditcardcntroller/applyForCard", method = RequestMethod.POST)
    public Object applyForCard(@RequestBody OrderResBO orderResBO) {

        Map<String, String> map;
        LOG.info("--------申卡-------");
        String callbackType = orderResBO.getCallbackType();
        String sign = orderResBO.getSign();
        String timestamp = orderResBO.getTimestamp();
        String result = RsaUtils.decrypt(privateKey, sign);
        String tradeNo = orderResBO.getTradeNo();
        /*BigDecimal bigDecimal = new BigDecimal("25"); // 每笔抽取25
        BigDecimal userPrice = BigDecimal.valueOf(orderResBO.getUserPrice()).subtract(bigDecimal);  //上游下发的分润金额*/
        String clientNo = orderResBO.getClientNo();

        if ("CALLBACK_SUCCESS".equals(callbackType) && timestamp.equals(result)) {
            CardOrderEntity cardOrderEntity = cardOrderServiceImpl.getOrderByClientNo(clientNo);
            LOG.info("查询到网申订单:" + cardOrderEntity);

            /*LOG.info("==============开始计算用户分润=======================");
            String userPhone = cardOrderEntity.getUserPhone();   //办卡人的手机号
            int count = 0; //用于判断办卡人上级有几个
            Map<String, String> userInfoMap = null;              //办卡人的信息
            Map<String, String> upUserInfoMap = null;            //上级用户的信息
            BigDecimal downProfitAmount = BigDecimal.ZERO;       //上级给下级的分润金额
            BigDecimal upProfitAmount = null;                    //上级实际拿到的金额
            String maxPhone = userPhone;                         //默认办卡人为一级


            AccountEntity accountInfo = null;
            ProfitInfo profitInfo =null;
            JSONObject upUserInfo = null;
            String upUserInfoJson = null;
            UserEntity entity = null;
            *//* ====================找到一级用户==================== *//*
                for (int i = 1; i < Integer.valueOf(Constants.LEVEL_THREE.toString()); i++) {
                    userInfoMap = userService.getUserInfoByPhone(maxPhone);
                    String userInfoJSON = userInfoMap.get(Constants.RESULT.toString());

                    JSONObject userInfo = JSONObject.parseObject(userInfoJSON);
                    String fPhone = userInfo.getString("fPhone");
                    if (fPhone != null) {
                        maxPhone = fPhone;
                        ++ count;
                    }

                }
            if (count != 0) {
                *//* ====================得到一级用户设置的下级分润金额==================== *//*
                upUserInfoMap = userService.getUserInfoByPhone(maxPhone);
                upUserInfoJson = upUserInfoMap.get(Constants.RESULT.toString());
                upUserInfo = JSONObject.parseObject(upUserInfoJson);
                downProfitAmount = new BigDecimal(upUserInfo.getString("downProfitAmount"));

                *//* ====================对一级用户进行分润==================== *//*
                upProfitAmount = accountService.countProfit(userPrice, downProfitAmount);
                // 添加分润明细
                profitInfo = new ProfitInfo();
                profitInfo.setClientNo(cardOrderEntity.getClientNo());
                profitInfo.setUserName(cardOrderEntity.getUserName());
                profitInfo.setUserPhone(cardOrderEntity.getUserPhone());
                profitInfo.setTradeNo(cardOrderEntity.getTradeNo());
                profitInfo.setUpUserName(upUserInfo.getString("userName"));
                profitInfo.setUpUserPhone(upUserInfo.getString("userPhone"));
                profitInfo.setProfit(upProfitAmount);
                profitInfo.setCreateTime(Util.getDate());
                map = accountService.addProfit(profitInfo);
                LOG.info("一级分润明细:" + map.get(Constants.RESP_MESSAGE.toString()));
                entity = JSON.parseObject(upUserInfoJson, UserEntity.class);
                accountInfo = accountService.getAccountInfo(entity.getId());
                accountInfo.setBalance_T0(accountInfo.getBalance_T0().add(upProfitAmount));
                accountInfo.setBalance(accountInfo.getBalance().add(upProfitAmount));
                accountInfo = accountService.addAccount(accountInfo);
                LOG.info("用户余额已更新:" + accountInfo);
                if (count == 2) {
                    userPrice = downProfitAmount;
                    *//* ====================得到二级用户设置的下级分润金额==================== *//*
                    upUserInfoMap = userService.getUserInfoByPhone(userPhone);
                    upUserInfoJson = upUserInfoMap.get(Constants.RESULT.toString());
                    upUserInfo = JSONObject.parseObject(upUserInfoJson);
                    downProfitAmount = new BigDecimal(upUserInfo.getString("downProfitAmount"));
                    *//* ====================对二级用户进行分润==================== *//*
                    upProfitAmount = accountService.countProfit(userPrice, downProfitAmount);
                    // 添加分润明细
                    profitInfo = new ProfitInfo();
                    profitInfo.setClientNo(cardOrderEntity.getClientNo());
                    profitInfo.setUserName(cardOrderEntity.getUserName());
                    profitInfo.setUserPhone(cardOrderEntity.getUserPhone());
                    profitInfo.setTradeNo(cardOrderEntity.getTradeNo());
                    profitInfo.setUpUserName(upUserInfo.getString("userName"));
                    profitInfo.setUpUserPhone(upUserInfo.getString("userPhone"));
                    profitInfo.setProfit(upProfitAmount);
                    profitInfo.setCreateTime(Util.getDate());
                    map = accountService.addProfit(profitInfo);
                    LOG.info("二级分润明细:" + map.get(Constants.RESP_MESSAGE.toString()));
                    entity = JSON.parseObject(upUserInfoJson, UserEntity.class);
                    accountInfo = accountService.getAccountInfo(entity.getId());
                    accountInfo.setBalance_T0(accountInfo.getBalance_T0().add(upProfitAmount));
                    accountInfo.setBalance(accountInfo.getBalance().add(upProfitAmount));
                    accountInfo = accountService.addAccount(accountInfo);
                    LOG.info("用户余额已更新:" + accountInfo);
                }
            }
            *//* ====================对办卡用户发放分润==================== *//*
            profitInfo = new ProfitInfo();
            profitInfo.setClientNo(cardOrderEntity.getClientNo());
            profitInfo.setUserName(cardOrderEntity.getUserName());
            profitInfo.setUserPhone(cardOrderEntity.getUserPhone());
            profitInfo.setTradeNo(cardOrderEntity.getTradeNo());
            if (upUserInfo != null){
                profitInfo.setUpUserName(upUserInfo.getString("userName"));
                profitInfo.setUpUserPhone(upUserInfo.getString("userPhone"));
            }

            profitInfo.setProfit(upProfitAmount);
            profitInfo.setCreateTime(Util.getDate());
            map = accountService.addProfit(profitInfo);*/

            /* ==================== 添加修改订单信息 ==================== */
            cardOrderEntity.setTradeNo(tradeNo);
            /*cardOrderEntity.setRebatePrice(downProfitAmount);
            cardOrderEntity.setUserPrice(userPrice);*/
            cardOrderEntity.setCreateTime(Utils.getDate());
            cardOrderEntity.setStatus("已成交");
            CardOrderEntity cardOrder = cardOrderServiceImpl.updateOrder(cardOrderEntity);
            /*LOG.info("办卡人分润明细:"+map.get(Constants.RESP_MESSAGE.toString()));
            entity = JSON.parseObject(upUserInfoJson, UserEntity.class);

            if(entity != null){
                accountInfo = accountService.getAccountInfo(entity.getId());
                accountInfo.setBalance_T0(accountInfo.getBalance_T0().add(upProfitAmount));
                accountInfo.setBalance(accountInfo.getBalance().add(upProfitAmount));
                accountInfo = accountService.addAccount(accountInfo);
            }else {
                accountInfo = accountService.getAccountInfo(entity.getId());
                accountInfo.setBalance_T0(accountInfo.getBalance_T0().add(upProfitAmount));
                accountInfo.setBalance(accountInfo.getBalance().add(upProfitAmount));
                accountInfo = accountService.addAccount(accountInfo);
            }

            LOG.info("用户余额已更新:"+ accountInfo);*/
            LOG.info("添加的订单信息:" + cardOrder);
            /*if (!Constants.SUCCESS.toString().equals(map.get(Constants.RESP_CODE.toString()))) {
                LOG.info("添加分润失败!");
                return "{\"result\": {\"message\": \"失败\",\"callbackType\": \"CALLBACK_ERROR\"}}";
            }*/
            return "{\"result\": {\"message\": \"成功\",\"callbackType\": \"CALLBACK_SUCCESS\"}}";
        } else {
            LOG.info("订单添加失败!");
            return "{\"result\": {\"message\": \"失败\",\"callbackType\": \"CALLBACK_ERROR\"}}";
        }
    }

    @RequestMapping(value = "/creditcardcntroller/card/apply/info",method = RequestMethod.POST)
    public Object applyForCardInfo(
            @RequestParam(value = "clientNo",required = false)String clientNo,
            @RequestParam(value = "userId",required = false)String userId,
            @RequestParam(value = "userName",required = false)String userName,
            @RequestParam(value = "userPhone",required = false)String userPhone,
            @RequestParam(value = "cardId",required = false)String cardId,
            @RequestParam(value = "page",defaultValue = "0",required = false)int start,
            @RequestParam(value = "size",defaultValue = "20",required = false)int size,
            @RequestParam(value = "order",defaultValue = "DESC",required = false) Sort.Direction order,
            @RequestParam(value = "sort",defaultValue = "createTime",required = false)String sort){

        Map<String,Object> map = new HashMap<>();
        Pageable pageable = new PageRequest(start, size, new Sort(order, sort));
        Page<CardOrderEntity> page = null;
        try {
            if (clientNo == null && userId == null && userName == null && userPhone == null && cardId == null){
                page = cardOrderServiceImpl.listOrder(pageable);
                map.put(Constants.RESULT.toString(),page);
            }
            if (clientNo != null && userId == null && userName == null && userPhone == null && cardId == null){
                page = cardOrderServiceImpl.listOrderByClientNo(clientNo,pageable);
                map.put(Constants.RESULT.toString(),page);
            }
            if (clientNo == null && userId != null && userName == null && userPhone == null && cardId == null){
                page = cardOrderServiceImpl.listOrderByUserId(userId,pageable);
                map.put(Constants.RESULT.toString(),page);
            }
            if (clientNo == null && userId == null && userName != null && userPhone == null && cardId == null){
                page = cardOrderServiceImpl.listOrderByUserName(userName,pageable);
                map.put(Constants.RESULT.toString(),page);
            }
            if (clientNo == null && userId == null && userName == null && userPhone != null && cardId == null){
                page = cardOrderServiceImpl.listOrderByUserPhone(userPhone,pageable);
                map.put(Constants.RESULT.toString(),page);
            }
            if (clientNo == null && userId == null && userName == null && userPhone == null && cardId != null){
                page = cardOrderServiceImpl.listOrderByCardId(cardId,pageable);
                map.put(Constants.RESULT.toString(),page);
            }
            map.put(Constants.RESP_CODE.toString(),Constants.SUCCESS.toString());
            map.put(Constants.RESP_MESSAGE.toString(),"查询成功");
            return map;
        } catch (Exception e) {
            map.put(Constants.RESP_CODE.toString(),Constants.ERROR.toString());
            map.put(Constants.RESP_MESSAGE.toString(),"查询异常======" + e);
            return map;
        }
    }

    /**
     * 申卡成功回调地址
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/creditcardcntroller/applyForCardNotify")
    @ResponseBody
    public Object applyCardNotify(@RequestBody JSONObject jsonObject){
        String clientNo = jsonObject.getString("clientNo");
        String tradeNo = jsonObject.getString("tradeNo");
        String userPrice = jsonObject.getString("userPrice");
        String callbackType = jsonObject.getString("sign");
        String timestamp = jsonObject.getString("timestamp");
        Map map=new HashMap<>();
        LOG.info("信用卡申请异步回调进来啦=======订单号==========="+clientNo+"======分润金额========="+userPrice+"=====callbackType===="+callbackType);
        CardOrderEntity cardOrderEntity=cardOrderServiceImpl.getOrderByClientNo(clientNo);
        String status=cardOrderEntity.getStatus();
        if("1".equals(status)){
            map.put("resp_message","订单已处理");
            map.put("reps_code","success");
            return map;
        }
        cardOrderEntity.setStatus("1");
        cardOrderEntity.setUserPrice(new BigDecimal(userPrice));//通道分润金额
        String brandId=cardOrderEntity.getBrandId();
        AmountDisRatioConfig amountDisRatioConfig = amountDisRatioConfigService.queryConfigByBrandId(brandId);
        if(amountDisRatioConfig==null){
            LOG.info("该贴牌未设置信用卡返佣金额比率==========orderCode==="+clientNo);
            return map;
        }
        BigDecimal ratio = amountDisRatioConfig.getAmountDisRatio();
        BigDecimal rebatePrice=new BigDecimal(userPrice).multiply(ratio);
        cardOrderEntity.setRebatePrice(rebatePrice);//返佣金额
        cardOrderEntity.setUpdateTime(new Date());
        cardOrderServiceImpl.saveOrder(cardOrderEntity);


        //发放三级返佣
        asyncMethod.updatePaymentOrderByOrderCode(cardOrderEntity);

        Map map1=new HashMap<>();
        map1.put("message","");
        map1.put("callbackType","CALLBACK_SUCCESS");
        map.put("result",map1);
        return map;
    }
}