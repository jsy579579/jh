package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;

import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class WithDrawApiServlet extends HttpServlet{

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
		String externalNo=format(request.getParameter("externalNo"));
		String transferWay=format(request.getParameter("transferWay"));
		String amount=format(request.getParameter("amount"));
		String callBackUrl=format(request.getParameter("callBackUrl"));
		
		String[] before=new String[]{amount,customerNumber,externalNo,mainCustomerNumber,transferWay,callBackUrl};
		String hmac=YeepayService.madeHmac(before,YeepayService.HmacKey());
		String withDrawApiURL=YeepayService.getURI(YeepayService.withDrawApi_URL);
		System.out.println("请求地址："+withDrawApiURL);
		//发送请求
		String[] reqskb=new String[]{mainCustomerNumber,customerNumber,externalNo,transferWay,amount,callBackUrl,hmac};		
	    String withDrawApiQuery[]={"mainCustomerNumber","customerNumber","externalNo","transferWay","amount","callBackUrl","hmac","hmac"};

		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqskb,withDrawApiQuery,withDrawApiURL);
		System.err.println("返回结果："+responseMap);
		//结果处理
		request.setAttribute("responseDataMap", responseMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/33withDrawApiResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
