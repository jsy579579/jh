package cn.jh.common.tools;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import cn.jh.common.utils.CommonConstants;


public class ResultWrap {

    public static Map<String, String> add(Map<String, String> map, String key, String value) {
        map.put(key, value);
        return map;
    }


    public static Map<String, Object> init(String respCode, String respMesg) {
    	Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);
        return map;
    }


    public static Map<String, Object> init(String respCode, String respMesg, String result) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        return map;
    }


    public static Map<String, Object> init(String respCode, String respMesg, Object result) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        return map;
    }


    public static Map<String, String> info(Logger logger, String respCode, String respMesg) {
        return info(logger, respCode, respMesg, null);
    }


    public static Map<String, String> info(Logger logger, String respCode, String respMesg, String result) {
        Map<String, String> map = new HashMap<String, String>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        logger.info("{}", map);
        return map;
    }


    public static Map<String, Object> info(Logger logger, String respCode, String respMesg, Object result) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        logger.info("{}", map);
        return map;
    }


    public static Map<String, String> warn(Logger logger, String respCode, String respMesg) {
        return warn(logger, respCode, respMesg, null);
    }


    public static Map<String, String> warn(Logger logger, String respCode, String respMesg, String result) {
        Map<String, String> map = new HashMap<String, String>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        logger.warn("{}", map);
        return map;
    }


    public static Map<String, Object> warn(Logger logger, String respCode, String respMesg, Object result) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        logger.warn("{}", map);
        return map;
    }


    public static Map<String, Object> err(Logger logger, String respCode, String respMesg) {
        return err(logger, respCode, respMesg, null);
    }


    public static Map<String, Object> err(Logger logger, String respCode, String respMesg, String result) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        logger.error("{}", map);
        return map;
    }


    public static Map<String, Object> err(Logger logger, String respCode, String respMesg, Object result) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(CommonConstants.RESULT, result);
        map.put(CommonConstants.RESP_CODE, respCode);
        map.put(CommonConstants.RESP_MESSAGE, respMesg);

        logger.error("{}", map);
        return map;
    }
    
}
