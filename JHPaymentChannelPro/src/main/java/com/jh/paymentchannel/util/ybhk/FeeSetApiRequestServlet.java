package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;

import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class FeeSetApiRequestServlet extends HttpServlet{

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
		String rate=format(request.getParameter("rate"));
		String productType=format(request.getParameter("productType"));
		
		String[] before=new String[]{customerNumber,mainCustomerNumber,productType,rate};
		String hmac=YeepayService.madeHmac(before,YeepayService.HmacKey());
		String feeSetApiURL=YeepayService.getURI(YeepayService.feeSetApi_URL);
		System.out.println("请求地址："+feeSetApiURL);
		//发送请求
		String[] reqstrskb=new String[]{mainCustomerNumber,customerNumber,productType,rate,hmac};
		String reqskb[]={"mainCustomerNumber","customerNumber","productType","rate","hmac"};
		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqstrskb,reqskb,feeSetApiURL);
		System.err.println("返回结果："+responseMap);
		//结果处理
		request.setAttribute("responseDataMap", responseMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/13feeSetApiResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
