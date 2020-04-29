package com.jh.good.service;

import cn.jh.common.utils.CommonConstants;
import com.jh.good.business.ItemCatBusiness;
import com.jh.good.pojo.ItemCat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品类别
 */
@Api(tags = "商品分类接口", description = "提供商品分类相关的 Rest API")
@Controller
@EnableAutoConfiguration
public class ItemCatService {

    @Autowired
    ItemCatBusiness itemCatBusiness;

    /**
     * 获取所有类别
     * @return
     */
    @ApiOperation("获取所有类别")
    @RequestMapping(method = RequestMethod.GET,value = "/v1.0/good/itemCat/findAll")
    @ResponseBody
    public Object findAll(){
        Map map = new HashMap();
        List<ItemCat> list = itemCatBusiness.findAll();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

}
