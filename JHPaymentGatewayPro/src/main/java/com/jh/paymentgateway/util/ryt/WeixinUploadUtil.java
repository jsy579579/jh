package com.jh.paymentgateway.util.ryt;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;



public class WeixinUploadUtil {
	/*
	 * 
	 * 进行图片 声音 视频 等信息的回复时 我们都会需要一个media_id
	 * 
	 * "素材管理"--->"新增临时素材"提供了获取media_id的接口
	 * 
	 */

	private static final String UPLOAD_URL = "http://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN&type=TYPE";

	public static String upload(String filePath, String accessToken, String type) throws IOException {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new IOException("文件不存在");
		}
		String url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);
		URL urlobj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);

		// 设置头信息
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charset", "UTF-8");

		// 设置边界
		String BOUNFARY = "----------" + System.currentTimeMillis();
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNFARY);

		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNFARY);
		sb.append("\r\n");
		sb.append("Content-Disposition:from-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/actet-stream\r\n\r\n");

		byte[] head = sb.toString().getBytes("utf-8");
		// 获得输出流
		OutputStream out = new DataOutputStream(conn.getOutputStream());
		// 输出表头
		out.write(head);

		// 文件正文部分
		// 把文件以流的方式 推入到url
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
		in.close();
		// 结尾部分
		byte[] foot = ("\r\n--" + BOUNFARY + "--\r\n").getBytes("utf-8");
		out.write(foot);
		out.flush();
		out.close();

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		String result = null;
		reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		if (result == null) {
			result = buffer.toString();
		}
		if (result != null) {
			reader.close();
		}
		JSONObject jsonObject = JSONObject.fromObject(result);
		System.out.println(jsonObject);
		String typeName = "media_id";
		if (!"image".equals(type) && !"voice".equals(type) && !"video".equals(type)) {
			typeName = type + "_media_id";
		}
		String mediaid = jsonObject.getString(typeName);
		System.out.println(mediaid);
		return mediaid;
	}
}