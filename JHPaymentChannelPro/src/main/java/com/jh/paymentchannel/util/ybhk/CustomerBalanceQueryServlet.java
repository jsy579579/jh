package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;

import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class CustomerBalanceQueryServlet extends HttpServlet{

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
		String balanceType=format(request.getParameter("balanceType"));
		
		String[] before=new String[]{mainCustomerNumber,customerNumber,balanceType};
		String hmac=YeepayService.madeHmac(before,YeepayService.HmacKey());
		String customerBalanceQueryURL=YeepayService.getURI(YeepayService.customerBalanceQuery_URL);
		System.out.println("请求地址："+customerBalanceQueryURL);
		//发送请求
		String[] reqstrskb=new String[]{mainCustomerNumber,customerNumber,balanceType,hmac};		
		String customerBalanceQuery[]={"mainCustomerNumber","customerNumber","balanceType","hmac"};		
		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqstrskb,customerBalanceQuery,customerBalanceQueryURL);
		System.err.println("返回结果："+responseMap);
		//结果处理
		request.setAttribute("responseDataMap", responseMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/35customerBalaceQueryResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
