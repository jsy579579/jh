package com.juhe.creditcardapplyfor.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.juhe.creditcardapplyfor.bo.CallbackBO;
import com.juhe.creditcardapplyfor.business.impl.LoanOrderBusinessImpl;
import com.juhe.creditcardapplyfor.entity.LoanOrderEntity;
import com.juhe.creditcardapplyfor.utils.HttpClientUtils;
import com.juhe.creditcardapplyfor.utils.RsaUtils;
import com.juhe.creditcardapplyfor.utils.Utils;
import com.juhe.creditcardapplyfor.utils.enums.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huhao
 * @title: NetworkLoanController
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 00239:50
 */



@RestController
@RequestMapping("/v1.0/creditcardapplyfor/Loan")
public class NetworkLoanService {

    @Value("${baseHost}")
    private String baseHost;

    @Value("${privateKey}")
    private String privateKey;

    @Value("${loanCallbackUrl}")
    private String loanCallbackUrl;

    private final LoanOrderBusinessImpl loanOrderBusiness;

    private static final Logger LOG = LoggerFactory.getLogger(NetworkLoanService.class);

    @Autowired
    public NetworkLoanService(LoanOrderBusinessImpl loanOrderService) {
        this.loanOrderBusiness = loanOrderService;
    }


    @RequestMapping(value = "/network/order/query",method = RequestMethod.POST)
    public Object queryOrder(@RequestParam(value = "phone",required = false)String phone,
                             @RequestParam(value = "orderCode",required = false)String orderCode,
                             @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                             @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                             @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction order,
                             @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sort){

        Map<String,Object> map = new HashMap<>();
        Pageable pageable = new PageRequest(page,size,new Sort(order,sort));
        List<LoanOrderEntity> list = null;
        if (phone != null && orderCode != null){
            list = loanOrderBusiness.listOrderByPhoneAndOrderCode(phone,orderCode,pageable);
        }else if (phone != null && orderCode == null){
            list = loanOrderBusiness.listOrderByPhone(phone,pageable);
        }else if (phone == null && orderCode != null){
            list = loanOrderBusiness.listOrderByOrderCode(orderCode,pageable);
        }
        map.put(Constants.RESP_CODE.toString(),Constants.SUCCESS.toString());
        map.put(Constants.RESP_MESSAGE.toString(),"查询成功");
        map.put(Constants.RESULT.toString(),list);
        return map;
    }

    /**
     * 产品列表
     * @return
     */
    @RequestMapping(value = "/network/channel",method = RequestMethod.POST)
    public Object listChannel(){

        LOG.info("--------产品列表-------");
        String url = baseHost + "/loan/open/loan/product/price";
        Map<String, String> baseMap = Utils.getHead();

        Map<String,Object> map = new HashMap<>();


        String json  = JSON.toJSONString(map);
        //建立post请求
        return HttpClientUtils.doPost(url,baseMap,json);
    }


    /**
     * 保存记录
     * @param mobile   手机
     * @param oemChannelId  通道id
     * @return
     */
    @RequestMapping(value = "/network/save",method = RequestMethod.POST)
    public Object saveOrder(@RequestParam(value = "mobile",required = true) String mobile, @RequestParam(value = "oemChannelId",required = true) Long oemChannelId) {
        LOG.info("--------保存记录-------");
        String url = baseHost + "/loan/open/loan/product/save";
        Map<String, String> baseMap = Utils.getHead();
        //建立请求数据
        Map<String, Object> map = new HashMap<>();

        String loanClientId = Utils.getLoanClientId();
        LOG.info("请求参数:");
        LOG.info("mobile:" + mobile);
        LOG.info("oemChannelId:" + oemChannelId);
        LOG.info("loanClientId:" + loanClientId);
        //申请人手机号
        map.put("mobile", mobile);
        //通道id   网贷列表的id
        map.put("oemChannelId", oemChannelId);
        //回调地址
        map.put("callbackUrl", loanCallbackUrl);
        //客户端编号，唯一请求标识
        map.put("clientNo", loanClientId);

        //建立post请求  传递json格式参数
        String resultText = HttpClientUtils.doPost(url, baseMap, JSON.toJSONString(map));

        JSONObject jsonObject = JSON.parseObject(resultText);
        LOG.info("响应状态码:" + jsonObject.getString("status"));
        if ("200".equals(jsonObject.getString("status"))) {
            LoanOrderEntity loanOrder = new LoanOrderEntity();
            loanOrder.setMobile(mobile);
            loanOrder.setOemChannelId(oemChannelId);
            loanOrder.setClientNo(loanClientId);
            loanOrder.setCreateTime(Utils.getDate());
            loanOrder.setStatus("待成交");

            LoanOrderEntity loanOrderEntity = loanOrderBusiness.saveOrder(loanOrder);
            LOG.info("生成订单:", loanOrderEntity);

        }
        return resultText;
    }

    /**
     * 贷款
     * @param callbackBO  回调实体
     * @return
     */
    @RequestMapping(value = "/network/submit",method = RequestMethod.POST)
    public Object submitOrder(@RequestBody CallbackBO callbackBO){
        LOG.info("--------贷款-------");
        LOG.info("请求参数:");
        LOG.info("callbackBO:" + callbackBO);
        String callbackType = callbackBO.getCallbackType();
        LOG.info("请求类型:" + callbackType);

        String sign = callbackBO.getSign();

        String timestamp = callbackBO.getTimestamp();

        String result = RsaUtils.decrypt(privateKey, sign);

        if("CALLBACK_SUCCESS".equals(callbackType) && timestamp.equals(result)){

            String clientNo = callbackBO.getClientNo();
            LoanOrderEntity loanOrderEntity = loanOrderBusiness.getOrderByClientNo(clientNo);

            LOG.info("查询到未成交订单:"+loanOrderEntity);
            String transNo = callbackBO.getTransNo();
            Integer userPrice = callbackBO.getUserPrice();
            Integer amount = callbackBO.getAmount();

            loanOrderEntity.setTransNO(transNo);
            loanOrderEntity.setUserPrice(new BigDecimal(userPrice));
            loanOrderEntity.setCreateTime(Utils.getDate());
            loanOrderEntity.setAmount(new BigDecimal(amount));

            loanOrderEntity.setStatus("已成交");
            LoanOrderEntity loanOrder = loanOrderBusiness.updateOrder(loanOrderEntity);

            LOG.info("订单添加成功:" + loanOrder);
            return "{\"result\": {\"message\": \"成功\",\"callbackType\": \"CALLBACK_SUCCESS\"}}";
        }else{
            LOG.info("订单添加失败");
            return "{\"result\": {\"message\": \"失败\",\"callbackType\": \"CALLBACK_ERROR\"}}";
        }

    }
}






