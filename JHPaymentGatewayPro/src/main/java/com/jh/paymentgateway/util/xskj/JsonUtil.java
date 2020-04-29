package com.jh.paymentgateway.util.xskj;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
* Json工具类
* JsonUtil.class
* @author lihexiao
* @create 2017-03-02 14:30 www.hnapay.com Inc.All rights reserved.
**/
public class JsonUtil {
    /**
     * 将json字符串转换为map
     * @param json
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static Map<String, Object> jsonToMap(String json) throws JsonParseException, JsonMappingException,
            IOException
    {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        ObjectMapper mapper = new ObjectMapper();

        model = mapper.readValue(json, Map.class);

        return model;
    }
}
