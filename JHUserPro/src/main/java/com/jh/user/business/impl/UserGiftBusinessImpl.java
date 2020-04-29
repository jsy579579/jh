package com.jh.user.business.impl;

import cn.jh.common.utils.CommonConstants;
import com.jh.user.business.UserGiftBusiness;
import com.jh.user.pojo.AreaNew;
import com.jh.user.pojo.VIPGiftOrder;
import com.jh.user.repository.AreaRepository;
import com.jh.user.repository.UserGiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: UserGiftBusinessImpl
 * @projectName: juhepay
 * @description: TODO
 * @author: huhao
 * @date: 2019/8/30 15:07
 */
@Service
public class UserGiftBusinessImpl implements UserGiftBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(UserGiftBusinessImpl.class);

    @Autowired
    private UserGiftRepository userGiftRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private EntityManager em;

    @Override
    public Map<String, String> checkUserPermissions(Long userId) {

        Map<String, String> map = new HashMap<>();
        VIPGiftOrder order =  userGiftRepository.findByUserId(userId);
        LOG.info("ORDER=========================="+order);
        if (order == null){
            map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE,"您还没有领取奖励！");
            return map;
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
        map.put(CommonConstants.RESP_MESSAGE,"您已领取过该奖品！");
        return map;
    }

    @Override
    public Map<String, Object> addGiftOrder(VIPGiftOrder vipGiftOrder) {

        Map<String, Object> map = new HashMap<>();
        VIPGiftOrder entity = userGiftRepository.save(vipGiftOrder);
        if (entity == null){
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"添加订单失败！");
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"添加订单成功！");
        map.put(CommonConstants.RESULT,entity);
        return map;
    }

    @Override
    public Map<String, Object> getOrderInfoByUserId(Long userId) {
        Map<String, Object> map = new HashMap<>();
        VIPGiftOrder orderInfo = userGiftRepository.findByUserId(userId);
        if (orderInfo == null){
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"该用户没有礼品领取信息！");
        }
        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE,"该礼品领取信息查询成功");
        map.put(CommonConstants.RESULT,orderInfo);
        return map;
    }

    @Override
    public List<VIPGiftOrder> listOrderInfoPageable(Pageable pageable) {
        em.clear();
        return userGiftRepository.findAllPageable(pageable);
    }

    @Override
    public List<VIPGiftOrder> listOrderInfoPageableByPhoneAndOrderCode(String phone, String orderCode, Pageable pageable) {
        em.clear();
        return userGiftRepository.findOrderInfoPageByPhoneAndOrderCode(phone,orderCode,pageable);
    }

    @Override
    public List<VIPGiftOrder> listOrderInfoPageableByPhone(String phone, Pageable pageable) {
        em.clear();
        return userGiftRepository.findOrderInfoPageByPhone(phone,pageable);
    }

    @Override
    public List<VIPGiftOrder> listOrderInfoPageableByOrderCode(String orderCode, Pageable pageable) {
        em.clear();
        return userGiftRepository.findOrderInfoPageByOrderCode(orderCode,pageable);
    }

    @Override
    public List<AreaNew> listAreaInfo(int id) {
        em.clear();
        return areaRepository.findByAreaParentId(id);
    }

    @Override
    public List<VIPGiftOrder> listOrderInfoPageableByPhoneAndUserName(String phone, String name, Pageable pageable) {
        em.clear();
        return userGiftRepository.findOrderInfoPageByPhoneAndUserName(phone,name,pageable);
    }

    @Override
    public List<VIPGiftOrder> listOrderInfoPageableByUserName(String name, Pageable pageable) {
        em.clear();
        return userGiftRepository.findOrderInfoPageByUserName(name,pageable);
    }
}
