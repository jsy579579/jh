package com.jh.good.business;

import com.jh.good.pojo.Address;
import java.util.List;

/**
 * 用户地址接口
 */
public interface AddressBusiness {
    //查询所有地址
    List<Address> findAll();
    //根据用户的id查询地址
    List<Address> findByUserId(Long id);
    //新增用户地址
    void save(Address address);
    //通过地址id获取地址
    Address findById(Long addressId);
    // 删除地址
    void del(Long id);
}
