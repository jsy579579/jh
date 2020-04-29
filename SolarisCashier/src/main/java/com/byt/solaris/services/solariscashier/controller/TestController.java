package com.byt.solaris.services.solariscashier.controller;

import cn.jh.common.utils.AjaxResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author ysj
 * @Date 2019/12/17 14:54
 */
@RestController
public class TestController {
    @RequestMapping(method = RequestMethod.GET,value = "/v1.0/solarisCashier/test")
    public AjaxResult test(){
        return AjaxResult.success();
    }

}
