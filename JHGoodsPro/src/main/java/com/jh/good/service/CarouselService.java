package com.jh.good.service;

import cn.jh.common.utils.CommonConstants;
import com.jh.good.business.CarouselBusiness;
import com.jh.good.pojo.Carousel;
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
 * 轮播图
 */
@Controller
@EnableAutoConfiguration
public class CarouselService {

    @Autowired
    CarouselBusiness carouselBusiness;

    /**
     * 获取需要展示的轮播图
     * @return
     */
    @RequestMapping(method = RequestMethod.GET,value = "/v1.0/good/carousel/findNeed")
    @ResponseBody
    public Object findNeed(){
        Map map = new HashMap();
        List<Carousel> list = carouselBusiness.findNeed();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

}
