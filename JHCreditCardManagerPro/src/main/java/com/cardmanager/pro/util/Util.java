package com.cardmanager.pro.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Configuration
public class Util {

    //JSON数组排序工具类  打乱sort为1的通道排序顺序
    public JSONArray arraySort(JSONArray resultArray){
        List<JSONObject> list = JSONArray.parseArray(resultArray.toJSONString(), JSONObject.class);
        Collections.sort(list, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                int a = Integer.valueOf(o1.getString("sort"));
                int b = Integer.valueOf(o2.getString("sort"));
                if (a > b) {
                    return 1;
                } else if(a == b) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        JSONArray jsonArray = JSONArray.parseArray(list.toString());
        System.out.println("排序后：" + jsonArray);
        return jsonArray;
    }
}
