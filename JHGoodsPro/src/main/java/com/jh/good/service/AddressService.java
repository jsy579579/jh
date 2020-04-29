package com.jh.good.service;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import cn.jh.common.utils.UUIDGenerator;
import com.jh.good.business.AddressBusiness;
import com.jh.good.pojo.Address;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "用户地址相关接口", description = "用户地址相关的 Rest API")
@Controller
@EnableAutoConfiguration
public class AddressService {

    private static final Logger LOG = LoggerFactory.getLogger(AddressService.class);

    @Autowired
    private AddressBusiness addressBusiness;

    @ApiOperation("获取所有用户的地址")
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/good/address/findAll")
    @ResponseBody
    public Map findAll() {
        Map map = new HashMap();
        List<Address> list = addressBusiness.findAll();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    @ApiOperation("根据token获取用户的所有地址")
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/good/address/findByUserId/{token}")
    @ResponseBody
    public Object findByUserId(@PathVariable("token") String token) {
        Map map = new HashMap();
        Long userId = null;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/query/{token}查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "Token不存在或者被篡改");
            return map;
        }
        List<Address> list = addressBusiness.findByUserId(userId);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "获取成功");
        return map;
    }

    @ApiOperation("通过id获取用户地址")
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/good/address/finbById/{id}")
    @ResponseBody
    public Map finbById(@PathVariable("id") Long id){
        Map map = new HashMap();
        Address address = addressBusiness.findById(id);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT,address);
        map.put(CommonConstants.RESP_MESSAGE, "获取成功");
        return map;
    }

    @ApiOperation("修改用户地址")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/address/update/{token}")
    @ResponseBody
    public Map update(@PathVariable("token") String token
            ,@RequestBody Address address){
        Map map = new HashMap();
        Long userId = null;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/query/{token}查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "Token不存在或者被篡改");
            return map;
        }
        address.setUserId(userId);
        addressBusiness.save(address);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "修改成功");
        return map;
    }

    @ApiOperation("保存用户地址")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/address/save/{token}")
    @ResponseBody
    public Map save(@PathVariable("token") String token, @RequestBody Address address){
        Map map = new HashMap();
        Long userId = null;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            LOG.error("==========/v1.0/user/account/query/{token}查询用户异常===========" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "Token不存在或者被篡改");
            return map;
        }
        address.setUserId(userId);
        if (address.getId() == null) {
            address.setId(Long.valueOf(UUIDGenerator.getDateTimeOrderCode()));
            address.setCreateDate(new Date());
        }
        addressBusiness.save(address);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "保存成功");
        return map;
    }

    @ApiOperation("删除用户地址")
    @RequestMapping(method = RequestMethod.DELETE, value = "/v1.0/good/address/del/{id}")
    @ResponseBody
    public Map del(@PathVariable("id") Long id){
        Map map = new HashMap();
        addressBusiness.del(id);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "删除成功");
        return map;
    }

}
