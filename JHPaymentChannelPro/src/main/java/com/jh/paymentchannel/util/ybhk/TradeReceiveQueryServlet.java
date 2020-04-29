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


public class TradeReceiveQueryServlet extends HttpServlet{

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
		String createTimeBegin=format(request.getParameter("createTimeBegin"));
		String createTimeEnd=format(request.getParameter("createTimeEnd"));
		String payTimeBegin=format(request.getParameter("payTimeBegin"));
		String payTimeEnd=format(request.getParameter("payTimeEnd"));
		String lastUpdateTimeBegin=format(request.getParameter("lastUpdateTimeBegin"));
		String lastUpdateTimeEnd=format(request.getParameter("lastUpdateTimeEnd"));
		String status=format(request.getParameter("status"));
		String busiType=format(request.getParameter("busiType"));
		String pageNo=format(request.getParameter("pageNo"));


		Map<String,String>  map=new LinkedHashMap<String,String>();
		map.put("mainCustomerNumber",mainCustomerNumber);
		map.put("customerNumber",customerNumber);
		map.put("requestId",requestId);
		map.put("createTimeBegin",createTimeBegin);
		map.put("createTimeEnd",createTimeEnd);
		map.put("payTimeBegin",payTimeBegin);
		map.put("payTimeEnd",payTimeEnd);
		map.put("lastUpdateTimeBegin",lastUpdateTimeBegin);
		map.put("lastUpdateTimeEnd",lastUpdateTimeEnd);
		map.put("status",status);
		map.put("busiType",busiType);
		map.put("pageNo",pageNo);
		String hmac=YeepayService.makeHmac(map,YeepayService.HmacKey());
		map.put("hmac",hmac);
		System.out.print("添加所有参数的map："+map);
			List< NameValuePair > list=new ArrayList<NameValuePair>();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			String key		= entry.getKey();

			String value	= entry.getValue();

			list.add(new NameValuePair(key, format(value)));
		}
		NameValuePair[]  pairs	= new NameValuePair[list.size()];
		for(int i=0;i<pairs.length;i++){
			pairs[i]=list.get(i);

		}
		String tradeReviceQueryURL=YeepayService.getURI(YeepayService.tradeReviceQuery_URL);
		System.out.println("请求地址："+tradeReviceQueryURL);

		TreeMap<String, Object> responsedateMap=YeepayService.send(pairs,tradeReviceQueryURL);
		System.err.println("返回结果："+responsedateMap);
		List<JSONArray> li =new ArrayList<JSONArray>();
		if(responsedateMap.get("tradeReceives")!=null){


			li=(List)responsedateMap.get("tradeReceives");


		}
		request.setAttribute("li",li);
		//结果处理
		request.setAttribute("responseDataMap", responsedateMap);
		RequestDispatcher view	= request.getRequestDispatcher("jsp/32tradeReceiveQueryResponse.jsp");
		view.forward(request, response);
	}

public  String format(String text){
	return text==null?"":text.trim();
}

}
