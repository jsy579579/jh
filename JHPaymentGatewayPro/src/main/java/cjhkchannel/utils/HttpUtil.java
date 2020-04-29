package cjhkchannel.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

	public static Response sendPost(String url, String jsonStr){
		OkHttpClient client = new OkHttpClient();
		client.newBuilder().
				connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS).
				writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS);
		RequestBody body = RequestBody.create(JSON, jsonStr);
		Request request = new Request.Builder().url(url).post(body).build();
		Response response = null;
		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
}
