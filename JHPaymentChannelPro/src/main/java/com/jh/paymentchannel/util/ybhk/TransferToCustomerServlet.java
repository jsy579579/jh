package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;

import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class TransferToCustomerServlet extends HttpServlet{

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
		String transAmount=format(request.getParameter("transAmount"));
		String remark=format(request.getParameter("remark"));

			String[] before=new String[]{mainCustomerNumber,customerNumber,requestId,transAmount,remark};
		String hmac=YeepayService.madeHmac(before,YeepayService.HmacKey());
		String transferToCustomerURL=YeepayService.getURI(YeepayService.transferToCustomer_URL);
		System.out.println("请求地址："+transferToCustomerURL);
		//发送请求
		String[] reqskb=new String[]{mainCustomerNumber,customerNumber,requestId,transAmount,remark,hmac};		
	    String transferToCustomer[]={"mainCustomerNumber","customerNumber","requestId","transAmount","remark","hmac"};

		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqskb,transferToCustomer,transferToCustomerURL);
		System.err.println("返回结果："+responseMap);
		//结果处理
		request.setAttribute("responseDataMap", responseMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/41transferTocustomerResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
