package com.jh.user.business;

import com.jh.user.pojo.AreaNew;
import com.jh.user.pojo.VIPGiftOrder;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserGiftBusiness {
    /** 判断用户是否为第一次领取
     * @return*/
    Map<String, String> checkUserPermissions(Long userId);

    /**
     * 添加订单
     * @param vipGiftOrder
     * @return
     */
    Map<String, Object> addGiftOrder(VIPGiftOrder vipGiftOrder);

    /**
     * 获取订单信息
     * @param userId
     * @return
     */
    Map<String, Object> getOrderInfoByUserId(Long  userId);

    List<VIPGiftOrder> listOrderInfoPageable(Pageable pageable);

    /**
     * 根据手机号和订单号获取订单信息
     * @param phone
     * @param orderCode
     * @param pageable
     * @return
     */
    List<VIPGiftOrder> listOrderInfoPageableByPhoneAndOrderCode(String phone, String orderCode, Pageable pageable);

    /**
     * 根据手机号获取订单信息
     * @param phone
     * @param pageable
     * @return
     */
    List<VIPGiftOrder> listOrderInfoPageableByPhone(String phone, Pageable pageable);

    /**
     * 根据订单号获取订单信息
     * @param orderCode
     * @param pageable
     * @return
     */
    List<VIPGiftOrder> listOrderInfoPageableByOrderCode(String orderCode, Pageable pageable);

    /**
     * 获取城市信息列表
     * @param id
     * @return
     */
    List<AreaNew> listAreaInfo(int id);

    List<VIPGiftOrder> listOrderInfoPageableByPhoneAndUserName(String phone, String name, Pageable pageable);

    List<VIPGiftOrder> listOrderInfoPageableByUserName(String name, Pageable pageable);
}
