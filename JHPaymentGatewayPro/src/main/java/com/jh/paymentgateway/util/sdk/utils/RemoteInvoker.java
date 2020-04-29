package com.jh.paymentgateway.util.sdk.utils;

import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.util.sdk.domain.Response;

import java.util.HashMap;
import java.util.Map;

public class RemoteInvoker {
	public static <T extends Response> T invoke(Object request, String url, Class<T> c) throws Exception {
		final String jsonData = request instanceof String? (String)request: JSONObject.toJSONString(request);
        final String sign = RsaUtils.sign(KeyUtil.getPrimaryKey(), jsonData);//签名

        Map<String , String> header = new HashMap<String , String>();
        header.put("x-efps-sign", sign);
        header.put("x-efps-sign-no", Config.getSignNo());
        T response = HttpUtil.post(url, jsonData ,header , c);

        return response;
	}
}
