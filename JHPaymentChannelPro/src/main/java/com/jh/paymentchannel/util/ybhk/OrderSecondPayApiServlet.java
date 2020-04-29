package com.jh.paymentchannel.util.ybhk;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TreeMap;


public class OrderSecondPayApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        String mainCustomerNumber=YeepayService.mainCustomerNumber();
        String customerNumber=format(request.getParameter("customerNumber"));
        String requestId=format(request.getParameter("requestId"));
        String ip=format(request.getParameter("ip"));
        String amount=format(request.getParameter("amount"));
        String mcc=format(request.getParameter("mcc"));
        String src=format(request.getParameter("src"));
        String callBackUrl=format(request.getParameter("callBackUrl"));
        String cardLastNo=format(request.getParameter("cardLastNo"));
        String repayPlanNo=format(request.getParameter("repayPlanNo"));
        String repayPlanStage=format(request.getParameter("repayPlanStage"));

        String productName=format(request.getParameter("productName"));

        String[] before=new String[]{mainCustomerNumber,customerNumber,requestId,amount,ip,mcc,src,cardLastNo,callBackUrl,productName,repayPlanNo,repayPlanStage};
        String hmac=YeepayService.madeHmac(before,YeepayService.HmacKey());
        String SecondpayApiURL=YeepayService.getURI(YeepayService.secondPayApi_URL);
        System.out.println("请求地址："+SecondpayApiURL);
        //发送请求
        String[] reqskb=new String[]{mainCustomerNumber,customerNumber,requestId,amount,ip,mcc,src,cardLastNo,callBackUrl,productName,repayPlanNo,repayPlanStage,hmac};
        String receiveApi[]={"mainCustomerNumber","customerNumber","requestId","amount","ip","mcc","src","cardLastNo","callBackUrl","productName","repayPlanNo","repayPlanStage","hmac"};

        TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqskb,receiveApi,SecondpayApiURL);
        System.err.println("返回结果："+responseMap);
        //结果处理
        request.setAttribute("responseDataMap", responseMap);
        RequestDispatcher view	= request.getRequestDispatcher("jsp/311orderSecondPayResponse.jsp");
        view.forward(request, response);
    }

    public  String format(String text){
        return text==null?"":text.trim();
    }

}
