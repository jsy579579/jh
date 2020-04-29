package com.jh.paymentchannel.util.ybhk;

import java.io.IOException;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.alibaba.fastjson.JSONArray;

import org.apache.commons.httpclient.NameValuePair;



public class TransferQueryServlet extends HttpServlet{

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
		String serialNo=format(request.getParameter("serialNo"));
		String requestDateSectionBegin=format(request.getParameter("requestDateSectionBegin"));
		String requestDateSectionEnd=format(request.getParameter("requestDateSectionEnd"));
		String transferStatus=format(request.getParameter("transferStatus"));
		String transferWay=format(request.getParameter("transferWay"));
		String pageNo=format(request.getParameter("pageNo"));

		Map<String,String> map=new LinkedHashMap<String,String>();

		map.put("customerNumber",customerNumber);
		map.put("externalNo",externalNo);
		map.put("mainCustomerNumber",mainCustomerNumber);
		map.put("pageNo",pageNo);
		map.put("requestDateSectionBegin",requestDateSectionBegin);
		map.put("requestDateSectionEnd",requestDateSectionEnd);
		map.put("serialNo",serialNo);
		map.put("transferStatus",transferStatus);
		map.put("transferWay",transferWay);


		String hmac=YeepayService.makeHmac(map,YeepayService.HmacKey());
		map.put("hmac",hmac);
		System.out.print("添加所有参数的map："+map);
		List<NameValuePair> list=new ArrayList<NameValuePair>();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			String key		= entry.getKey();
			//System.out.println("key-----"+key);
			String value	= entry.getValue();
		//	System.out.println("value-----"+value);
			list.add(new NameValuePair(key, format(value)));
		}
		NameValuePair[]  pairs	= new NameValuePair[list.size()];
		for(int i=0;i<pairs.length;i++){
			pairs[i]=list.get(i);
			//System.out.println("NameValuePairs[]:"+pairs[i]);
		}
		String transferQueryURL=YeepayService.getURI(YeepayService.transferQuery_URL);
		System.out.println("请求地址："+transferQueryURL);

		TreeMap<String, Object> responsedateMap=YeepayService.send(pairs,transferQueryURL);

		List<JSONArray> li =new ArrayList<JSONArray>();
		if(responsedateMap.get("transferRequests")!=null){


		li=(List)responsedateMap.get("transferRequests");


		}
		request.setAttribute("li",li);
		//结果处理
		request.setAttribute("responseDataMap", responsedateMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/34transferQueryResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
