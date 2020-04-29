package cjhkchannel.utils;

import java.io.IOException;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 获取支付令牌
 * 
 * @author tinn
 *
 */
public class GetSpToken {

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public BaseResMessage<TokenRes> token(String key, String spCode) throws IOException {
		String url = Constants.getServerUrl() + "/v2/base/getQkSpToken";

		// 2、构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("spCode", spCode);

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("spCode", spCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		String tokenJsonReq = jsonObj.toJSONString();
		System.out.println("jsonReq: " + tokenJsonReq);

		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, tokenJsonReq);
		Request request = new Request.Builder().url(url).post(body).build();
		Response response = client.newCall(request).execute();

		String tokenJsonRsp = response.body().string();
		System.out.println("jsonRsp: " + tokenJsonRsp);

		BaseResMessage<TokenRes> res = null;
		if (response.isSuccessful()) {
			res = JSONObject.parseObject(tokenJsonRsp, new TypeReference<BaseResMessage<TokenRes>>() {
			});
		} else {
			System.out.println("响应码: " + response.code());
			throw new IOException("Unexpected code " + response.message());
		}
		return res;
	}
}
