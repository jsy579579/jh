package com.jh.user.service;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.RandomUtils;
import cn.jh.common.utils.TokenUtil;
import com.jh.user.business.impl.UserGiftBusinessImpl;
import com.jh.user.pojo.AreaNew;
import com.jh.user.pojo.VIPGiftOrder;
import com.jh.user.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: UserGiftService
 * @projectName: juhepay
 * @description: TODO
 * @author: huhao
 * @date: 2019/8/30 14:43
 */
@RestController
public class UserGiftService {
    private static final Logger LOG = LoggerFactory.getLogger(UserGiftService.class);

    @Autowired
    private UserGiftBusinessImpl userGiftBusiness;

    /**
     * 判断是否重复领取
     * @param token
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/permissions/{token}")
    public Object checkUserPermissions(@PathVariable("token") String token){
        Map<String, Object> map = new HashMap<>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        Map<String, String> result = userGiftBusiness.checkUserPermissions(userId);
        LOG.info("RESULT======================="+result.get(CommonConstants.RESP_MESSAGE));
        return result;
    }

    /**
     * 添加领取信息
     * @param token
     * @param name
     * @param phone
     * @param giftName
     * @param address
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/order/add/{token}")
    public Object addGiftOrder(@PathVariable("token") String token,
                               @RequestParam("name") String name,
                               @RequestParam("phone") String phone,
                               @RequestParam("gift_name") String giftName,
                               @RequestParam("address") String address){
        Map<String, Object> map = new HashMap<>();
        LOG.info("======================礼品下单=======================");
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        Map<String, String> result = userGiftBusiness.checkUserPermissions(userId);
        if (!CommonConstants.SUCCESS.equals(result.get(CommonConstants.RESP_CODE))){
            return result;
        }
        VIPGiftOrder vipGiftOrder = new VIPGiftOrder();
        vipGiftOrder.setUserId(userId);
        vipGiftOrder.setUserName(name);
        vipGiftOrder.setUserPhone(phone);
        vipGiftOrder.setGiftName(giftName);
        vipGiftOrder.setOrderCode(RandomUtils.generateUpperString(18));
        //vipGiftOrder.setLogisticsCode();
        vipGiftOrder.setStatus("0");
        vipGiftOrder.setAddress(address);
        vipGiftOrder.setCreateTime(new Date());
        Map<String, Object> resultMap = userGiftBusiness.addGiftOrder(vipGiftOrder);
        LOG.info("RESULT==========================" + resultMap);
        return result;
    }

    /**
     * 获取领取信息
     * @param token
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/order/info/{token}")
    public Object getGiftOrder(@PathVariable("token") String token){
        Map<String, Object> map = new HashMap<>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        Map<String, Object> orderInfoMap = userGiftBusiness.getOrderInfoByUserId(userId);
        LOG.info("获取礼品领取信息========================"+orderInfoMap);
        return orderInfoMap;
    }

    /**
     * 修改领取信息
     * @param id
     * @param userId
     * @param userPhone
     * @param userName
     * @param orderCode
     * @param logisticsCode
     * @param giftName
     * @param address
     * @param status
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/order/update")
    public Object updateGiftOrder(@RequestParam("id") Integer id,
                               @RequestParam("user_id") long userId,
                               @RequestParam("user_phone") String userPhone,
                               @RequestParam("user_name") String userName,
                               @RequestParam("order_code") String orderCode,
                               @RequestParam(value = "logistics_code" ,required = false) String logisticsCode,
                               @RequestParam("gift_name") String giftName,
                               @RequestParam("address") String address,
                               @RequestParam("status") String status){
        Map<String, Object> map = new HashMap<>();
        VIPGiftOrder vipGiftOrder = new VIPGiftOrder();
        vipGiftOrder.setId(id);
        vipGiftOrder.setUserId(userId);
        vipGiftOrder.setUserName(userName);
        vipGiftOrder.setUserPhone(userPhone);
        vipGiftOrder.setGiftName(giftName);
        vipGiftOrder.setOrderCode(orderCode);
        vipGiftOrder.setLogisticsCode(logisticsCode);
        vipGiftOrder.setStatus(status);
        vipGiftOrder.setAddress(address);
        vipGiftOrder.setCreateTime(new Date());
        LOG.info("请求参数：===========================" + vipGiftOrder);
        Map<String, Object> result = userGiftBusiness.addGiftOrder(vipGiftOrder);
        LOG.info("RESULT=========================="+ result);
        return result;
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @param direction
     * @param sortProperty
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/order/query/all")
    public Object listOrderInfo(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "20", required = false) int size,
            @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
            @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty){
        Map<String, Object> map = new HashMap<>();
        LOG.info("VIP领取分页查询参数============Phone:===="+ phone + "===============" + "name:" + name);
        List<VIPGiftOrder> list = null;
        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
        if (phone != null && !"".equals(phone) && phone.trim().length() != 0 && name != null && !"".equals(name) && name.trim().length() != 0){
            list = userGiftBusiness.listOrderInfoPageableByPhoneAndUserName(phone,name,pageable);
        }
        if((name == null || "".equals(name) || name.trim().length() == 0 )&& (phone == null || "".equals(phone) || phone.trim().length() == 0)){
            list = userGiftBusiness.listOrderInfoPageable(pageable);
        }
        if (phone != null && !"".equals(phone) && phone.trim().length() != 0){
            list = userGiftBusiness.listOrderInfoPageableByPhone(phone,pageable);
        }
        if (name != null && !"".equals(name) && name.trim().length() != 0){
            list = userGiftBusiness.listOrderInfoPageableByUserName(name,pageable);
        }

        LOG.info("RESULT==========================" + list);
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"查询成功");
        map.put(CommonConstants.RESULT,list);
        return map;
    }

    /**
     * 根据用户id修改订单状态
     * @param userId
     * @param logisticsCode
     * @param status
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/order/update/id")
    public Object updateOrderStatusByUserId(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "logistics_code",required = false) String logisticsCode,
            @RequestParam(value = "status") String status){
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> orderInfoMap = userGiftBusiness.getOrderInfoByUserId(userId);
        VIPGiftOrder vipGiftOrder = (VIPGiftOrder) orderInfoMap.get(CommonConstants.RESULT);
        vipGiftOrder.setStatus(status);
        vipGiftOrder.setLogisticsCode(logisticsCode);
        Map<String, Object> resultMap = userGiftBusiness.addGiftOrder(vipGiftOrder);
        if (CommonConstants.SUCCESS.equals(resultMap.get(CommonConstants.RESP_CODE))){
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"订单修改成功");
            map.put(CommonConstants.RESULT,resultMap.get(CommonConstants.RESULT));
            return map;
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE,"订单修改失败");
        return map;
    }

    /**
     * 三级联动
     * @param id
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/order/area/id")
    public Object listArea(@RequestParam(value = "id")int id){
        LOG.info("三级联动======================" + id);
        Map<String, Object> map = new HashMap<>();
        List<AreaNew> list = null;
        try {
            list = userGiftBusiness.listAreaInfo(id);
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"查询成功");
            map.put(CommonConstants.RESULT,list);
            return map;
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,e);
            return map;
        }
    }
}
