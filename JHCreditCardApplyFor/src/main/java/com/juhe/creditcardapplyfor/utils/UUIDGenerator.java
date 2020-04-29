package com.juhe.creditcardapplyfor.utils;

/**
 * @author Administrator
 * @title: UUIDGenerator
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002317:32
 */
import java.util.UUID;

public class UUIDGenerator {

    /**
     * 获取32位UUID小写字符串
     * @return
     */

    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }




}
