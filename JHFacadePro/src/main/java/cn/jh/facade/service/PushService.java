package cn.jh.facade.service;



    import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.ExceptionUtil;
import cn.jh.facade.util.MD5Utils;
import net.sf.json.JSONObject;

    /**
     * PushDemoController
     * 推送服务消息接收示例
     * 依赖SPRING 3.0或以上版本
     * @auther ChengZi
     * @data 16/9/10
     */
    @Controller
    @EnableAutoConfiguration
    public class PushService {
    	private static final Logger LOG = LoggerFactory.getLogger(PaymentManageService.class);
        private static final int mode = 1 ; //服务商
        private static final String clientId="8f8ceb4165214d9b20"; //服务商的秘钥证书
        private static final String clientSecret="106d08c6795e76d0097e25f1f0b76aa9";//服务商的秘钥证书

        @RequestMapping(value = "/v1.0/facade/youzanyun/ordercoder", method = RequestMethod.POST)
        @ResponseBody
        public Object test(HttpServletRequest request,
        
        		@RequestParam(value="msg",required=false) String msg,
    				
        		@RequestParam(value="sendCount",required=false,defaultValue="0") int sendCount,
    				
        		@RequestParam(value="mode",required=false,defaultValue="0") int mode, //  默认0 : appid  1 :client
    				
        		@RequestParam(value="app_id",required=false) String app_id,
    				
        		@RequestParam(value="client_id",required=false) String client_id,
    				
        		@RequestParam(value="version",required=false) Long version,
    				
        		@RequestParam(value="type",required=false) String type,
    				
        		@RequestParam(value="id",required=false) String id,
    				
        		@RequestParam(value="sign",required=false) String sign,
    				
        		@RequestParam(value="kdt_id",required=false ,defaultValue="-1") Integer kdt_id,
    				
        		@RequestParam(value="test",required=false,defaultValue="false") boolean test ,
    				
        		@RequestParam(value="status",required=false) String status,
    				
        		@RequestParam(value="kdt_name",required=false) String kdt_name)  {

            JSONObject res = new JSONObject();
            res.put("code", 0);
            res.put("msg", "success");

            /**
             *  判断是否为心跳检查消息
             *  1.是则直接返回
             */
            if (test) {
                return res;
            }

            /**
             * 解析消息推送的模式  这步判断可以省略
             * 0-商家自由消息推送 1-服务商消息推送
             * 以服务商 举例
             * 判断是否为服务商类型的消息
             * 否则直接返回
             */
            if (mode!=0 ){
                return res;
            }
            LOG.info("youzanyun====");
            MsgPushEntity entity=new MsgPushEntity();
            entity.setApp_id(app_id);
            entity.setClient_id(client_id);
            entity.setId(id);
            entity.setKdt_id(kdt_id);
            entity.setKdt_name(kdt_name);
            entity.setMode(mode);
            entity.setMsg(msg);
            entity.setSendCount(sendCount);
            entity.setSign(sign);
            entity.setStatus(status);
            entity.setTest(test);
            entity.setType(type);
            entity.setVersion(version);
            
            /**
             * 判断消息是否合法
             * 解析sign
             * MD5 工具类开发者可以自行引入
             */
            String sign1= MD5Utils.MD5(clientId+entity.getMsg()+clientSecret);
            if (!sign1.equals(entity.getSign())){
                return res;
            }
            
            /**
             * 对于msg 先进行URI解码
             */
            String msg1="";
            try {
                 msg1= URLDecoder.decode(entity.getMsg(), "utf-8");
                 LOG.info("youzanyun===="+msg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();LOG.error("",e);
            }

            /**
             *  ..........
             *  接下来是一些业务处理
             *  判断当前消息的类型 比如交易
             *
             */

            if ("TRADE_ORDER_STATE".equals(entity.getType())) {
            	JSONObject jsonObject =  JSONObject.fromObject(msg1);
        		if(jsonObject.containsKey("status")&&jsonObject.getString("status").equals("TRADE_SUCCESS")){
        			
        			
        		}
        		

            }


            /**
             * 返回结果
             */
            return res;
        }





    /**
     * 消息接收类
     */
    public class MsgPushEntity {


        private String msg;

        private int sendCount;

        private int mode; //  默认0 : appid  1 :client

        private String app_id;

        private String client_id;

        private Long version;

        private String type;

        private String id;

        private String sign;

        private Integer kdt_id;

        private boolean test = false;

        private String status;

        private String kdt_name;

        public boolean isTest() {
            return test;
        }

        public void setTest(boolean test) {
            this.test = test;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getVersion() {
            return version;
        }

        public void setVersion(Long version) {
            this.version = version;
        }


        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getSendCount() {
            return sendCount;
        }

        public void setSendCount(int sendCount) {
            this.sendCount = sendCount;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public String getClient_id() {
            return client_id;
        }

        public void setClient_id(String client_id) {
            this.client_id = client_id;
        }

        public Integer getKdt_id() {
            return kdt_id;
        }

        public void setKdt_id(Integer kdt_id) {
            this.kdt_id = kdt_id;
        }

        public String getKdt_name() {
            return kdt_name;
        }

        public void setKdt_name(String kdt_name) {
            this.kdt_name = kdt_name;
        }
    }
}
