package com.jh.good.service;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import cn.jh.common.utils.UUIDGenerator;
import com.jh.good.business.AddressBusiness;
import com.jh.good.business.GoodsBusiness;
import com.jh.good.business.OrderBusiness;
import com.jh.good.pojo.Address;
import com.jh.good.pojo.Goods;
import com.jh.good.pojo.Order;
import com.jh.good.pojo.OrderItem;
import com.jh.good.util.GoodConstants;
import com.jh.good.util.Util;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class OrderService {

    @Autowired
    OrderBusiness orderBusiness;

    @Autowired
    GoodsBusiness goodsBusiness;

    @Autowired
    AddressBusiness addressBusiness;

    @Autowired
    Util util;

    @Autowired
    RestTemplate restTemplate;



    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    /**
     * 保存订单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/order/save/{token}")
    @ResponseBody
    public Object save(@PathVariable("token") String token,
                       @RequestParam("goodsId") Long goodsId,
                       @RequestParam("addressId") Long addressId,
                       @RequestParam("number") Long number,
                       @RequestParam("paymentMethod") int paymentMethod,
                       @RequestParam("brandId") String brandId) {
        try {
            LOG.info("订单创建开始");
            String userPhone = null;
            try {
                userPhone = TokenUtil.getUserPhone(token);
            } catch (Exception e) {
                LOG.error("=========={token}传入有误===========" + e);
                return "error";
            }
            Map map = new HashMap();
            // 通过goodsId获取商品详情
            Goods goods = goodsBusiness.findById(goodsId);
            // 通过addressId获取收获地址
            Address address = addressBusiness.findById(addressId);
            if (address == null) {
                LOG.info("地址传入有误！");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "地址传入有误！");
                return map;
            }
            // 获取用户微服务
            URI uri = null;
            String url = null;
            String result = null;
            JSONObject jsonObject = null;
            JSONObject data = null;
            RestTemplate restTemplate = null;
            Long coin = null;
            Long userId = null;
            try {
                uri = util.getServiceUrl("user", "error url request!");
                url = uri.toString() + "/v1.0/user/account/query/" + token;
                System.out.println(url);
                // 通过用户token获取到用户的信息
                restTemplate = new RestTemplate();
                result = restTemplate.getForObject(url, String.class);
                jsonObject = JSONObject.fromObject(result);
                System.out.println(result);
                data = jsonObject.getJSONObject("result");
                coin = data.getLong("coin");    // 获取用户的积分
                userId = data.getLong("userId"); // 获取用户的id
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/account/query/{token}查询用户异常===========" + e);
                return "error";
            }

            // 当用户积分不够时，不可以下单
            if (coin < goods.getIntegral() * number) {
                LOG.info("商品订单创建失败，用户积分不够！");
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "抱歉！您的积分小于商品积分暂时不能购买!");
                return map;
            }
            //构建订单对象
            Order order = new Order();
            // 设置订单号
            order.setId(Long.valueOf(UUIDGenerator.getDateTimeOrderCode()));
            // 设置订单状态   1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭'
            order.setStatus(GoodConstants.ORDER_UNPAID);
            // 设置实付金额
            order.setPayment(goods.getPrice().multiply(new BigDecimal(number)));
            // 设置实付积分
            order.setTotalIntegral(goods.getIntegral() * number.intValue());
            // 设置邮费 默认包邮
            order.setPostFee(new BigDecimal(0));
            // 设置创建时间
            order.setCreateTime(new Date());
            // 设置用户id
            order.setUserId(userId);
            // 设置收货人地址 省 市 区 详细
            order.setReceiverAreaName(address.getProvinceAndCity() + " " + address.getAddress());
            // 设置收货人手机
            order.setReceiverMobile(address.getMobile());
            // 设置收货人
            order.setReceiver(address.getContact());
            //设置订单详情
            List<OrderItem> list = new ArrayList<>();
            OrderItem orderItem = new OrderItem();
            //设置订单号
            orderItem.setOrderId(order.getId());
            // 设置商品分类id
            orderItem.setItemId(goods.getCategory1Id());
            // 设置商品id
            orderItem.setGoodsId(goods.getId());
            // 设置商品标题
            orderItem.setTitle(goods.getGoodsName());
            // 设置商品单价
            orderItem.setPrice(goods.getPrice());
            // 设置商品积分
            orderItem.setIntegral(goods.getIntegral());
            // 设置商品数量
            orderItem.setNum(number.intValue());
            // 设置商品总价
            orderItem.setTotalPrice(goods.getPrice().multiply(new BigDecimal(number)));
            // 设置商品总积分
            orderItem.setTotalIntegral(goods.getIntegral() * number.intValue());
            // 设置品图片地址
            orderItem.setPicPath(goods.getSmallPic());
            list.add(orderItem);
            order.setOrderItemList(list);
            String notice = "高级会员";
            int product_id = 2617;
            if (order.getPayment().compareTo(new BigDecimal(398)) >= 0) {
                product_id = 2618;
                notice = "超级会员";
            }

            /** 判断贴牌商无法购买产品 ***/
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/query/phone";
            /** 根据的用户手机号码查询用户的基本信息 */
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("phone", userPhone);
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
            int grade = Integer.parseInt(resultObju.getString("grade"));
            restTemplate = new RestTemplate();
            uri = util.getServiceUrl("user", "error url request!");
            url = uri.toString() + "/v1.0/user/thirdlevel/prod/query/" + product_id;
            result = restTemplate.getForObject(url, String.class);
            jsonObject = JSONObject.fromObject(result);
            JSONObject jSONObject = jsonObject.getJSONObject("result");
            String resp_code = jsonObject.getString("resp_code");
            if (resp_code.equals(CommonConstants.SUCCESS)) {
                if (grade >= jSONObject.getInt("grade")) {
                    LOG.info("/v1.0/user/thirdlevel/prod/query/品牌等级重复购买");
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "无法重复购买此权益");
                    return map;
                }
            }

            // 扣除用户积分
            url = uri.toString() + "/v1.0/user/coin/update/userid";
            System.out.println(url);
            requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("user_id", order.getUserId().toString()); // 用户id
            requestEntity.add("coin", order.getTotalIntegral().toString()); // 需要支付的积分
            requestEntity.add("addorsub", "1");     // 代表扣分
            requestEntity.add("order_code", order.getId().toString());  // 订单id

            // 发送请求 修改用户积分
            try {
                restTemplate = new RestTemplate();
                result = restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                LOG.error("==========/v1.0/user/coin/update/useridnew修改用户积分异常===========" + e);
                return "error";
            }

            // 选择支付方法
            switch (paymentMethod) {
                // 余额付款
                case 1:
                    LOG.info("进入到余额付款");
                    // 获取到用户的余额
                    BigDecimal balance = new BigDecimal(data.getDouble("balance"));
                    if (balance.compareTo(order.getPayment()) >= 0) {
                        // 用户余额足够 执行扣除余额操作
                        url = uri.toString() + "/v1.0/user/account/update";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("user_id", order.getUserId().toString()); // 用户id
                        requestEntity.add("amount", order.getPayment().toString()); // 需要付款的金额
                        requestEntity.add("addorsub", "1");     // 代表扣钱
                        requestEntity.add("order_code", order.getId().toString());  // 订单id
                        // 发送请求 修改用户余额
                        try {
                            restTemplate = new RestTemplate();
                            result = restTemplate.postForObject(url, requestEntity, String.class);
                        } catch (RestClientException e) {
                            LOG.error("==========/v1.0/user/account/update修改用户余额异常===========" + e);
                            return "error";
                        }
                        //
                        jsonObject = JSONObject.fromObject(result);
                        // 修改订单状态
                        order.setStatus(GoodConstants.ORDER_PAID);
                        // 设置付款时间
                        order.setPaymentTime(new Date());
                        // 设置订单更新时间
                        order.setUpdateTime(new Date());
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "余额支付成功！");
                        // 完成返佣
                        URI uri2 = util.getServiceUrl("transactionclear", "error url request!");
                        url = uri2.toString() + "/v1.0/transactionclear/payment/add";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("type", "1");
                        requestEntity.add("phone", userPhone);
                        requestEntity.add("amount", order.getPayment().toString());
                        requestEntity.add("orderCode", order.getId().toString());
                        requestEntity.add("channel_tag", "SPALI_PAY_APP");
                        requestEntity.add("desc", "购买商品：" + orderItem.getTitle());
                        requestEntity.add("brand_id", brandId);
                        requestEntity.add("product_id", product_id + "");
                        // 发送请求
                        try {
                            restTemplate = new RestTemplate();
                            result = restTemplate.postForObject(url, requestEntity, String.class);
                        } catch (RestClientException e) {
                            LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "/v1.0/transactionclear/payment/add添加订单异常！");
                            return map;
                        }
                        // 修改订单状态，返佣
                        url = uri2.toString() + "/v1.0/transactionclear/payment/update";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("order_code", order.getId().toString());
                        requestEntity.add("status", 1 + "");
                        // 发送请求
                        try {
                            restTemplate = new RestTemplate();
                            result = restTemplate.postForObject(url, requestEntity, String.class);
                        } catch (RestClientException e) {
                            LOG.error("==========/v1.0/transactionclear/payment/update修改订单异常===========" + e);
                            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                            map.put(CommonConstants.RESP_MESSAGE, "==========/v1.0/transactionclear/payment/update修改订单异常===========");
                            return map;
                        }
                        LOG.info("余额支付成功");
                    } else {
                        // 用户余额不足
                        LOG.info("支付失败，用户余额不足");
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "支付失败，用户余额不足！");
                    }
                    break;
                case 2:
                    LOG.info("进入支付宝付款");
                    // 支付宝付款
                    URI uri1 = util.getServiceUrl("facade", "error url request!");
                    url = uri1.toString() + "/v1.0/facade/purchase/hlb";
                    System.out.println(url);
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("brandId", brandId); // 用户id
                    requestEntity.add("phone", userPhone); // 需要付款的金额
                    requestEntity.add("amount", order.getPayment().toString()); //订单信息
                    requestEntity.add("channe_tag", "HLB_QUICK");  // 代表支付包付款
                    requestEntity.add("order_desc", "购买商品:" + orderItem.getTitle());
                    requestEntity.add("product_id", product_id + ""); // 需要付款的金额
                    requestEntity.add("orderCode", order.getId().toString());
                    // 发送请求 调用接口
                    try {
                        restTemplate = new RestTemplate();
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESULT, jsonObject.getString("result"));
                        map.put(CommonConstants.RESP_MESSAGE, "获取支付成功！等待用户付款！");
                        System.out.println(result);
                    } catch (Exception e) {
                        LOG.error("==========/v1.0/paymentgateway/topup/hlb/createOrder获取支付宝支付异常===========" + e);
                        return "error";
                    }
                    break;
                case 3:
                    LOG.info("进入到会员升级");
                    // 支付宝付款
                    URI uri2 = util.getServiceUrl("facade", "error url request!");
                    url = uri2.toString() + "/v1.0/facade/purchase/aliPay/?amount=" + order.getPayment() + "&brand_id=" + brandId + "&channe_tag=ALIAPP&order_desc=购买商品:" + orderItem.getTitle() + "&phone=" + userPhone + "&product_id=" + product_id + "&orderCode=" + order.getId();
                    // 发送请求 调用支付宝接口
                    try {
                        restTemplate = new RestTemplate();
                        result = restTemplate.getForObject(url, String.class);
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESULT, JSONObject.fromObject(result).getString("orderInfo"));
                        map.put(CommonConstants.RESP_MESSAGE, "获取支付宝支付成功！");
                        System.out.println(result);
                    } catch (Exception e) {
                        LOG.error("==========/v1.0/facade/purchase/aliPay获取支付宝支付异常===========" + e);
                        return "error";
                    }
                    break;
                case 4:
                    LOG.info("进入银联扫码");
                    // 支付宝付款
                    URI uri3 = util.getServiceUrl("facade", "error url request!");
                    url = uri3.toString() + "/v1.0/facade/purchase/hlb";
                    System.out.println(url);
                    requestEntity = new LinkedMultiValueMap<String, String>();
                    requestEntity.add("brandId", brandId); // 用户id
                    requestEntity.add("phone", userPhone); // 需要付款的金额
                    requestEntity.add("amount", order.getPayment().toString()); //订单信息
                    requestEntity.add("channe_tag", "HLB_YL_QUICK");  // 代表支付包付款
                    requestEntity.add("order_desc", "购买商品:" + orderItem.getTitle());
                    requestEntity.add("product_id", product_id + ""); // 需要付款的金额
                    requestEntity.add("orderCode", order.getId().toString());
                    // 发送请求 调用接口
                    try {
                        restTemplate = new RestTemplate();
                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        jsonObject = JSONObject.fromObject(result);
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESULT, jsonObject.getString("result"));
                        map.put(CommonConstants.RESP_MESSAGE, "获取支付成功！等待用户付款！");
                        System.out.println(result);
                    } catch (Exception e) {
                        LOG.error("==========/v1.0/paymentgateway/topup/hlb/createOrder获取支付宝支付异常===========" + e);
                        return "error";
                    }
                    break;
            }
            // 保存订单
            orderBusiness.save(order);
            return map;
        } catch (Exception e) {
            LOG.error("==========保存订单异常===========" + e);
            Map map = new HashMap();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "保存订单异常");
            return map;
        }
    }

    /**
     * 根据订单的id修改订单的状态
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/order/update")
    @ResponseBody
    public Map update(@RequestParam(value = "order_code") String ordercode,
                      @RequestParam(value = "status") String status) {
        Map map = new HashMap();
        orderBusiness.update(ordercode, status);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "修改订单状态成功");
        return map;
    }

    /**
     * 根据token查询用户的订单
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/order/findByToken/{token}")
    @ResponseBody
    public Object findByToken(@PathVariable("token") String token,
                              @RequestParam(value = "status", required = false, defaultValue = "0") String status) {
        Map map = new HashMap();
        // 获取用户的id
        Long userId = null;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            e.printStackTrace();
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "失败，用户的Token不存在!");
            return map;
        }
        List<Order> list = orderBusiness.findByToken(userId, status);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 分页带搜索订单
     */
    @ApiOperation("分页带搜索商品")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/order/searchGoods")
    @ResponseBody
    public Map searchGoods(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,   //当前页
            @RequestParam(value = "size", defaultValue = "20", required = false) int size,  //每页显示的条数
            @RequestBody(required = false) Order order) {
        Map map = new HashMap();
        Page<Order> orderPage = orderBusiness.searchGoods(page, size, order);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, orderPage);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 订单继续付款
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/order/continuePayment/{token}")
    @ResponseBody
    public Object continuePayment(@PathVariable("token") String token,
                                  @RequestParam("order_code") String orderCode,
                                  @RequestParam("paymentMethod") int paymentMethod,
                                  @RequestParam("brandId") String brandId) {
        Map map = new HashMap();
        String userPhone = null;
        try {
            userPhone = TokenUtil.getUserPhone(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "用户token传入有误");
            return map;
        }
        Order order = orderBusiness.fingById(orderCode);
        if (order == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "订单号不存在");
            return map;
        }
        if (!order.getStatus().equals("1")) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "订单以付款或者已取消");
            return map;
        }
        // 选择支付方法
        switch (paymentMethod) {
            case 3:
                LOG.info("进入到会员升级");
                // 支付宝付款
                int product_id = 2617;
                if (order.getPayment().compareTo(new BigDecimal(398)) >= 0) {
                    product_id = 2618;
                }
                URI uri2 = util.getServiceUrl("facade", "error url request!");
                String url = uri2.toString() + "/v1.0/facade/purchase/aliPay/?amount=" + order.getPayment() + "&brand_id=" + brandId + "&channe_tag=ALIAPP&order_desc=购买商品:" + order.getOrderItemList().get(0).getTitle() + "&phone=" + userPhone + "&product_id=" + product_id + "&orderCode=" + order.getId();
                // 发送请求 调用支付宝接口
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    String result = restTemplate.getForObject(url, String.class);
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESULT, JSONObject.fromObject(result).getString("orderInfo"));
                    map.put(CommonConstants.RESP_MESSAGE, "获取支付宝支付成功！");
                    System.out.println(result);
                } catch (RestClientException e) {
                    LOG.error("==========/v1.0/paymentgateway/topup/hlb/createOrder获取支付宝支付异常===========" + e);
                    return "error";
                }
                break;
        }
        return map;
    }

    // 及时雨保存订单
    @RequestMapping(method = RequestMethod.POST,value = "/v1.0/good/order/jsySave")
    @ResponseBody
    public Object jsySave(@RequestBody Order order,
                          @RequestParam(name = "brand_id") String brand_id){
        Map map = new HashMap();
        LOG.info("进入到会员升级=====");
        order.setId(Long.valueOf(UUIDGenerator.getDateTimeOrderCode()));
        OrderItem orderItem = order.getOrderItemList().get(0);
        LOG.info("订单号=====" + order.getId());
        String url = "http://user/v1.0/user/thirdlevel/findByPrice?price=" + order.getPayment();
        String result = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(result);
        LOG.info("查询产品获取到的结果：" + result);
        JSONObject result1 = jsonObject.getJSONObject("result");
        // 根据价格获取产品product_id
        String product_id = result1.getString("id");
        // 支付宝付款
        url = "http://facade/v1.0/facade/purchase/aliPay/?amount=" + order.getPayment() + "&brand_id=" +
                brand_id + "&channe_tag=ALIAPP&order_desc=购买商品:" + orderItem.getTitle() + "&phone=" +
                order.getReceiverMobile() + "&product_id=" + product_id + "&orderCode=" + order.getId();
        // 发送请求 调用支付宝接口
        try {
            result = restTemplate.getForObject(url, String.class);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, JSONObject.fromObject(result).getString("orderInfo"));
            map.put(CommonConstants.RESP_MESSAGE, "获取支付宝支付成功！");
            orderBusiness.save(order);
            return map;
        } catch (Exception e) {
            LOG.error("==========/v1.0/facade/purchase/aliPay获取支付宝支付异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "获取支付宝支付失败！");
            return map;
        }

    }
}
