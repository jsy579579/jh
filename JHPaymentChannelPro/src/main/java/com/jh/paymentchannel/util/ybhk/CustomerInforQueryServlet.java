package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;



public class CustomerInforQueryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        String mainCustomerNumber = YeepayService.mainCustomerNumber();
        String mobilePhone = format(request.getParameter("mobilePhone"));
        String customerNumber = format(request.getParameter("customerNumber"));
        String customerType = format(request.getParameter("customerType"));

        String[] before = new String[]{mainCustomerNumber, mobilePhone, customerNumber, customerType};
        String hmac = YeepayService.madeHmac(before, YeepayService.HmacKey());
        String customerInforQueryURL = YeepayService.getURI(YeepayService.customerInforQuery_URL);
        System.out.println("请求地址：" + customerInforQueryURL);
        //发送请求
        String[] reqskb = new String[]{mainCustomerNumber, mobilePhone, customerNumber, customerType, hmac};
        String customerInforQuery[] = {"mainCustomerNumber", "mobilePhone", "customerNumber", "customerType", "hmac"};

        TreeMap<String, Object> responseMap = YeepayService.sendToSkb(reqskb, customerInforQuery, customerInforQueryURL);
        System.err.println("返回结果：" + responseMap);
        //得到的结果进行格式化处理
//        if (responseMap.get("retList") != null) {
//            System.out.println("得到的retList：" + responseMap.get("retList"));
//
//            String jsonString = JSONObject.toJSONString(responseMap.get("retList"));
//            List<CustomDomain> list = JSONObject.parseArray(jsonString, CustomDomain.class);
//            System.out.print(list.size());
//
//            if (list != null && list.size() > 0) {
//
//
//                request.setAttribute("customDomain", list.get(0));
//            }else{
//                request.setAttribute("customDomain",new CustomDomain());
//            }
//
//        }

        List<JSONArray> li =new ArrayList<JSONArray>();
        if(responseMap.get("retList")!=null){


            li=(List)responseMap.get("retList");


        }
        request.setAttribute("li",li);

        //结果处理
        request.setAttribute("responseDataMap", responseMap);
        RequestDispatcher view = request.getRequestDispatcher("jsp/12customerInforQueryResponse.jsp");
        view.forward(request, response);
    }

    public String format(String text) {
        return text == null ? "" : text.trim();
    }

}
