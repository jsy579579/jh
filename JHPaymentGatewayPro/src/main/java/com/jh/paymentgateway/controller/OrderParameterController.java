package com.jh.paymentgateway.controller;

import com.jh.paymentgateway.business.OrderParameterBusiness;
import com.jh.paymentgateway.business.impl.OrderParameterBusinessImpl;
import com.jh.paymentgateway.pojo.OrderParameter;
import com.jh.paymentgateway.repository.OrderParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @title: OrderParameterController
 * @projectName: DDSH
 * @description: TODO
 * @author: huhao
 * @date: 2019/11/7 21:51
 */
@Controller
public class OrderParameterController {

    @Autowired
    private OrderParameterBusinessImpl orderParameterBusiness;

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/orderparameter/get")
    @ResponseBody
    public OrderParameter getOrderParameter(@RequestParam("orderCode") String orderCode){
        return orderParameterBusiness.findByOrderCode(orderCode);
    }
}
