package com.jh.user.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
@Controller
public class IpAddress {

    private static final Logger LOG = LoggerFactory.getLogger(IpAddress.class);

    //百也特
    //public static String ipAddress ="http://101.132.160.107:8888";

    //易百管家
    //public static String ipAddress ="http://47.102.140.177:8888";

    //测试服
    //public static String ipAddress ="http://139.196.125.48:8888";

    //多多生活
//    public static String ipAddress ="http://101.132.74.149:8888";

    //太品商贸
    public static String ipAddress ="http://47.102.98.217:8888";


    public static String port=":8888";

    public static String getIpAddress(){
//        String ip=getHostAddress()+port;
        LOG.info("当前服务器ip=========="+ipAddress);
        return ipAddress;
    }

//    /**
//     * 获取本地服务器ip
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.GET,value="/v1.0/user/get/address")
//    public static String getHostAddress() {
//        try {
//            // 获取IP地址
//            String ip = InetAddress.getLocalHost().getHostAddress();
//
//            System.out.println("IP地址：" + ip);
//            return ip;
//        } catch (Exception e) {
//            System.out.println("异常：" + e);
//            e.printStackTrace();
//        }
//        return null;
//    }
}
