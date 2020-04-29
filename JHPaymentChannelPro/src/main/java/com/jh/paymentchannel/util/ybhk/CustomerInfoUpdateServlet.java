package com.jh.paymentchannel.util.ybhk;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.TreeMap;



public class CustomerInfoUpdateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        TreeMap<String, Object> responseMap=null;
        String mainCustomerNumber = YeepayService.mainCustomerNumber();
        String customerNumber = format(request.getParameter("customerNumber"));
        String modifyType = format(request.getParameter("modifyType"));
        String bankCardNumber = format(request.getParameter("bankCardNumber"));
        String bankName = URLEncoder.encode(format(request.getParameter("bankName")),"GBK");
        String riskReserveDay = format(request.getParameter("riskReserveDay"));
        String manualSettle = format(request.getParameter("manualSettle"));
        String splitter = format(request.getParameter("splitter"));
        String splitterProfitFee= format(request.getParameter("splitterProfitFee"));
        String bindMobile = format(request.getParameter("bindMobile"));
        String mailStr = format(request.getParameter("mailStr"));
        String areaCode = format(request.getParameter("areaCode"));

       //根据修改类型判断参与验签的参数
        String[] before2 = new String[]{mainCustomerNumber, customerNumber, bankCardNumber, bankName};
        String[] before3 = new String[]{mainCustomerNumber, customerNumber,riskReserveDay, manualSettle};
        String[] before4 = new String[]{mainCustomerNumber, customerNumber, splitter,splitterProfitFee};
        String[] before6 = new String[]{mainCustomerNumber, customerNumber, bindMobile,mailStr,areaCode};


        String hmac2 = YeepayService.madeHmac(before2, YeepayService.HmacKey());

        System.out.println("修改类型为2生成的hmac:"+hmac2);
        String hmac3 = YeepayService.madeHmac(before3, YeepayService.HmacKey());
        System.out.println("修改类型为3生成的hmac:"+hmac3);
        String hmac4 = YeepayService.madeHmac(before4, YeepayService.HmacKey());
        System.out.println("修改类型为4生成的hmac:"+hmac4);
        String hmac6 = YeepayService.madeHmac(before6, YeepayService.HmacKey());
        System.out.println("修改类型为6生成的hmac:"+hmac6);
        String customerInforUpdateURL = YeepayService.getURI(YeepayService.customerInforUpdate_URL);
        System.out.println("请求地址：" + customerInforUpdateURL);
        String customerInforUpdate[] = {"mainCustomerNumber", "customerNumber","modifyType","bankCardNumber","bankName","riskReserveDay","manualSettle","splitter","splitterProfitFee","bindMobile","mailStr","areaCode","hmac"};

        //发送请求，根据修改类型区分
        if(modifyType.equals("2")){
            String[] reqskb2 = new String[]{mainCustomerNumber, customerNumber,modifyType,bankCardNumber,bankName,riskReserveDay,manualSettle,splitter,splitterProfitFee,bindMobile,mailStr,areaCode,hmac2};
            responseMap = YeepayService.sendToSkb(reqskb2, customerInforUpdate, customerInforUpdateURL);

        }else if(modifyType.equals("3")) {
            String[] reqskb3 = new String[]{mainCustomerNumber, customerNumber, modifyType, bankCardNumber, bankName, riskReserveDay, manualSettle, splitter, splitterProfitFee, bindMobile, mailStr, areaCode, hmac3};
            responseMap = YeepayService.sendToSkb(reqskb3, customerInforUpdate, customerInforUpdateURL);

        }else if(modifyType.equals("4")) {
            String[] reqskb4 = new String[]{mainCustomerNumber, customerNumber, modifyType, bankCardNumber, bankName, riskReserveDay, manualSettle, splitter, splitterProfitFee, bindMobile, mailStr, areaCode, hmac4};
            responseMap = YeepayService.sendToSkb(reqskb4, customerInforUpdate, customerInforUpdateURL);

        }else {
            String[] reqskb6 = new String[]{mainCustomerNumber, customerNumber, modifyType, bankCardNumber, bankName, riskReserveDay, manualSettle, splitter, splitterProfitFee, bindMobile, mailStr, areaCode, hmac6};
            responseMap = YeepayService.sendToSkb(reqskb6, customerInforUpdate, customerInforUpdateURL);

        }


        System.err.println("返回结果：" + responseMap);

        //结果处理
        request.setAttribute("responseDataMap", responseMap);
        RequestDispatcher view = request.getRequestDispatcher("jsp/15customerInforUpdateResponse.jsp");
        view.forward(request, response);
    }

    public String format(String text) {
        return text == null ? "" : text.trim();
    }

}
