<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'add.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
    <<form action="logisticsInfo/add" method="post">
    	<input id="goodsId" name="goodsId" type="text" />
    	<input id="orderId" name="orderId" type="text" />
    	<input id="logisticsName" name="logisticsName" type="text" />
    	<input id="logisticsNum" name="logisticsNum" type="text" />
    	<input id="userId" name="userId" type="text" />
    	<input id="userAddr" name="userAddr" type="text" />
    	<input id="userProvinceId" name="userProvinceId" type="text" />
    	<input id="userCityId" name="userCityId" type="text" />
    	<input id="userAreasId" name="userAreasId" type="text" />
    	<input id="businessName" name="businessName" type="text" />
    	<input id="businessPhone" name="businessPhone" type="text" />
    	<input id="businessProvinceId" name="businessProvinceId" type="text" />
    	<input id="businessCityId" name="businessCityId" type="text" />
    	<input id="businessAreasId" name="businessAreasId" type="text" />
    	<input type="submit" value="提交">
    </form>
  </body>
</html>
