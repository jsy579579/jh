package com.jh.paymentgateway.util.ap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;

/**
 * 
 * @author kingmanager
 * @title JsonUtil.java
 * @package com.iboxpay.common.util
 * @description 提供用于Json数据转换的静态方法
 * @update 2011-10-25 下午06:19:11
 * @version V1.0
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String HTTP_JSON_CONTENT_TYPE = "application/json";

    public static final String UTF8 = "UTF-8";

    static {
        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //设置为中国上海时区
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //空值不序列化
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //去掉默认的时间戳格式
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
       
    }

    /**
     * @Title: toJson
     * @Description: 将对象转换为Json数据
     * @param @param obj
     * @param @return 设定文件
     * @return String 返回类型
     * @throws
     */
    public static String toJson(Object obj) {
        StringWriter sw = new StringWriter();

        try {
            JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
            mapper.writeValue(gen, obj);
            gen.close();
        } catch (JsonGenerationException e) {
            logger.error("", e);

        } catch (JsonMappingException e) {
            logger.error("", e);

        } catch (IOException e) {
            logger.error("", e);

        }
        String json = sw.toString();

        return json;
    }

    /**
     * 将JSON字符串 转换为对象
     * 
     * @author weiyuanhua
     * @date 2010-11-18 下午02:52:13
     * @param jsonStr
     *            JSON字符串
     * @param beanClass
     *            泛型对象
     * @param field
     *            对象中需要忽略的属性
     * @return
     */
    public static <T> T jsonToObject(String jsonStr, Class<T> beanClass) throws JsonParseException,
            JsonMappingException, IOException {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return mapper.readValue(jsonStr, beanClass);
    }

    public static <T> T jsonToObject(String jsonStr, String encoding, Class<T> beanClass)
            throws JsonParseException, JsonMappingException, IOException {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        return mapper.readValue(jsonStr.getBytes(encoding), beanClass);
    }

    public static Object jsonToCollection(String jsonStr, Class<?> collectionClass,
            Class<?>... elementClasses) throws JsonParseException, JsonMappingException,
            IOException {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JavaType javaType = getParametrizedType(collectionClass, elementClasses);
        return mapper.readValue(jsonStr, javaType);
    }

    public static Object jsonToCollection(String jsonStr, String encoding,
            Class<?> collectionClass, Class<?>... elementClasses) throws JsonParseException,
            JsonMappingException, IOException {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JavaType javaType = getParametrizedType(collectionClass, elementClasses);
        return mapper.readValue(jsonStr.getBytes(encoding), javaType);
    }

    /**
     * 获取集合的JavaType
     * @param parametrized
     * @param parameterClasses
     * @return
     */
    public static JavaType getParametrizedType(Class<?> parametrized, Class<?>... parameterClasses) {
        return mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    public static <T> T jsonToObject(String jsonStr, TypeReference<T> type)
            throws JsonParseException, JsonMappingException, IOException {

        return mapper.readValue(jsonStr.getBytes("UTF-8"), type);
    }

    public static <T> T jsonToObject(String jsonStr, JavaType type, Class<T> t)
            throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(jsonStr.getBytes("UTF-8"), type);
    }

    public static <T> T jsonToObject(String jsonStr, Class<T> respClass,
            Class<?>... parameterClasses) throws JsonParseException, JsonMappingException,
            IOException {
        JavaType parametrizedType = JsonUtil.getParametrizedType(respClass, parameterClasses);
        return mapper.readValue(jsonStr.getBytes("UTF-8"), parametrizedType);
    }

    public static <T> T jsonToObject(String jsonStr, String encoding, TypeReference<T> type)
            throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(jsonStr.getBytes(encoding), type);
    }

    public static Map<String, Object> parseJSON2Map(String jsonStr) throws JsonParseException,
            JsonMappingException, IOException {
        if (StringUtils.isBlank(jsonStr)) {
            return Collections.emptyMap();
        }
        return mapper.readValue(jsonStr, Map.class);
    }

    /**
     * 请使用toJsonV2
     * @param bean
     * @param ignoreVar
     * @return
     */
    @Deprecated
    public static String toJson(Object bean, String... ignoreVar) {

        BeanInfo beanInfo = null;
        StringBuilder sBuilder = null;
        try {
            sBuilder = new StringBuilder();
            beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor propertyDescriptors[] = beanInfo.getPropertyDescriptors();
            sBuilder.append("{");
            for (PropertyDescriptor property : propertyDescriptors) {
                String propertyName = property.getName();
                if (!propertyName.equals("class") && !isContains(propertyName, ignoreVar)) {
                    Method readMethod = property.getReadMethod();
                    String result = (String) readMethod.invoke(bean, new Object[0]);
                    if (result == null) {
                        result = "";
                    }
                    sBuilder.append("\"" + propertyName + "\":\"" + result + "\",");
                    logger.debug("\"" + propertyName + "\":\"" + result + "\"");
                }
            }
            String temp = sBuilder.toString();
            if (temp.length() > 0) {
                String result = temp.substring(0, temp.lastIndexOf(","));
                return result + "}";
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.debug("exception" + e.getMessage());
            return null;
        }
    }

    /**
     * 把对象转换成json数据
     *
     * @param bean
     * @param ignoreVar
     * @return
     */
    public static String toJsonV2(Object bean, String... ignoreVar) {

        if(null == bean){
            return null;
        }
        String jsonStr = JsonUtil.toJson(bean);
        JsonNode rootNode = null;
        StringWriter sw = new StringWriter();
        try {
            rootNode = mapper.readTree(jsonStr);

            if(null != ignoreVar && ignoreVar.length > 0){
                for (String var : ignoreVar) {
                    ((ObjectNode) rootNode).remove(var);
                }
            }
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(sw);
            mapper.writeValue(jsonGenerator,rootNode);
        } catch (Exception e) {
            logger.error("转换异常");
            return null;
        }
        return sw.toString();
    }

    /**
     * 判断被忽略的字段是否等于当前字段
     * 
     * @param propertyName
     * @param ignoreVar
     * @return
     */
    private static boolean isContains(String propertyName, String[] ignoreVar) {
        if (ignoreVar != null && ignoreVar.length > 0) {
            for (String str : ignoreVar) {
                if (propertyName.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Json响应
     * @param callback
     * @param response
     * @param data
     * @throws IOException
     */
    public static void jsonResponse(HttpServletResponse response, Object data) {
        response.setContentType(HTTP_JSON_CONTENT_TYPE + ";charset="
                + UTF8);
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(toJson(data));
        } catch (Exception e) {
            logger.error("jsonp响应写入失败！ 数据：" + data, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.error("输出流关闭异常！", e);
                }
                writer = null;
            }
        }
    }

}
