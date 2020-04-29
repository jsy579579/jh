package com.jh.paymentgateway.util.ytjf;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jh.paymentgateway.business.impl.YTJFTopupPage;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;


public class HttpClient {

	private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);
    /**
     * 目标地址
     */
    private URL url;

    /**
     * 通信连接超时时间
     */
    private int connectionTimeout;

    /**
     * 通信读超时时间
     */
    private int readTimeOut;

    /**
     * 通信结果
     */
    private String result;

    /**
     * 获取通信结果
     *
     * @return
     */
    public String getResult() {
        return result;
    }

    /**
     * 设置通信结果
     *
     * @param result
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * 构造函数
     *
     * @param url               目标地址
     * @param connectionTimeout HTTP连接超时时间
     * @param readTimeOut       HTTP读写超时时间
     */
    public HttpClient(String url, int connectionTimeout, int readTimeOut) {
        try {
            this.url = new URL(url);
            this.connectionTimeout = connectionTimeout;
            this.readTimeOut = readTimeOut;
        } catch (MalformedURLException e) {
        }
    }

    /**
     * 发送信息到服务端
     *
     * @param data
     * @param encoding
     * @return
     * @throws Exception
     */
    public String  send(Map<String, String> data, String encoding) throws Exception {
        try {
            HttpURLConnection httpURLConnection = createConnection(encoding);
            if (null == httpURLConnection) {
                throw new Exception("创建联接失败");
            }
            String sendData = this.getRequestParamString(data, encoding);//转换key=value；
            this.requestServer(httpURLConnection, sendData,
                    encoding);
            LOG.info("请求报文:[" + data + "]");
            this.result = this.response(httpURLConnection, encoding);
            LOG.info("返回报文:[" + result + "]");
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 将url参数转换成map
     *
     * @param param aa=11&bb=22&cc=33
     * @return
     */
    public static Map<String, String> getUrlParams(String param) {
        Map<String, String> map = new HashMap<String, String>(0);
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }
    /**
     * HTTP Post发送消息
     *
     * @param connection
     * @param message
     * @throws IOException
     */
    private void requestServer(final URLConnection connection, String message, String encoder)
            throws Exception {
        PrintStream out = null;
        try {
            connection.connect();
            out = new PrintStream(connection.getOutputStream(), false, encoder);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    /**
     * 显示Response消息
     *
     * @param connection
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    private String response(final HttpURLConnection connection, String encoding)
            throws Exception {
        InputStream in = null;
        StringBuilder sb = new StringBuilder(1024);
        BufferedReader br = null;
        try {
            if (200 == connection.getResponseCode()) {
                in = connection.getInputStream();
                sb.append(new String(read(in), encoding));
            } else {
                in = connection.getErrorStream();
                sb.append(new String(read(in), encoding));
            }
            return sb.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != br) {
                br.close();
            }
            if (null != in) {
                in.close();
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
    }

    public static byte[] read(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int length = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((length = in.read(buf, 0, buf.length)) > 0) {
            bout.write(buf, 0, length);
        }
        bout.flush();
        return bout.toByteArray();
    }

    /**
     * 创建连接
     *
     * @return
     * @throws ProtocolException
     */
    private HttpURLConnection createConnection(String encoding) throws ProtocolException {
        HttpURLConnection httpURLConnection = null;
        try {
            if ("https".equalsIgnoreCase(url.getProtocol())) {
                String urlStr = url.toString();
                url = new URL(null, urlStr, new sun.net.www.protocol.https.Handler());//https请求用sun.net.www.protocol.https.Handler
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } else {
                httpURLConnection = (HttpURLConnection) url.openConnection();
            }
        } catch (IOException e) {
            return null;
        }
        httpURLConnection.setConnectTimeout(this.connectionTimeout);// 连接超时时间
        httpURLConnection.setReadTimeout(this.readTimeOut);// 读取结果超时时间
        httpURLConnection.setDoInput(true); // 可读
        httpURLConnection.setDoOutput(true); // 可写
        httpURLConnection.setUseCaches(false);// 取消缓存
        httpURLConnection.setRequestProperty("Content-type",
                "application/x-www-form-urlencoded;charset=" + encoding);
        httpURLConnection.setRequestMethod("POST");
        if ("https".equalsIgnoreCase(url.getProtocol())) {
            HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
            husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
            husn.setHostnameVerifier(new BaseHttpSSLSocketFactory.TrustAnyHostnameVerifier());//解决由于服务器证书问题导致HTTPS无法访问的情�?
            return husn;
        }
        return httpURLConnection;
    }

    /**
     * 将Map存储的对象，转换为key=value&key=value的字段
     *
     * @param requestParam
     * @param coder
     * @return
     */
    private String getRequestParamString(Map<String, String> requestParam, String coder) {
        if (null == coder || "".equals(coder)) {
            coder = "UTF-8";
        }
        StringBuffer sf = new StringBuffer("");
        String reqstr = "";
        if (null != requestParam && 0 != requestParam.size()) {
            for (Map.Entry<String, String> en : requestParam.entrySet()) {
                try {
                    sf.append(en.getKey()
                            + "="
                            + (null == en.getValue() || "".equals(en.getValue()) ? "" : URLEncoder
                            .encode(en.getValue(), coder)) + "&");
                } catch (UnsupportedEncodingException e) {
                    return "";
                }
            }
            reqstr = sf.substring(0, sf.length() - 1);
        }
        return reqstr;
    }
}
