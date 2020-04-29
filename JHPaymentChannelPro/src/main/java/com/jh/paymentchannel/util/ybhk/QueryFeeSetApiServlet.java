package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;

import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class QueryFeeSetApiServlet extends HttpServlet{

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
		String productType=format(request.getParameter("productType"));
		
		String[] before=new String[]{customerNumber,mainCustomerNumber,productType};
		String hmac=YeepayService.madeHmac(before,YeepayService.HmacKey());
		String queryFeeSetApiURL=YeepayService.getURI(YeepayService.queryFeeSetApi_URL);
		System.out.println("请求地址："+queryFeeSetApiURL);
		//发送请求
		String[] reqskb=new String[]{customerNumber,mainCustomerNumber,productType,hmac};
	    String queryFeeSetApi[]={"customerNumber","mainCustomerNumber","productType","hmac"};

		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqskb,queryFeeSetApi,queryFeeSetApiURL);
		System.err.println("返回结果："+responseMap);
		//结果处理
		request.setAttribute("responseDataMap", responseMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/14queryFeeSetApiResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
