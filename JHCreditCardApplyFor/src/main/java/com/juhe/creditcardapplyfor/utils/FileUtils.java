package com.juhe.creditcardapplyfor.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author huhao
 * @title: FileUtils
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002317:24
 */
public class FileUtils {

    public static File getFile(MultipartFile multfile) throws IOException {
        // 获取文件名
        String fileName = multfile.getOriginalFilename();
        // 获取文件后缀
        String prefix=fileName.substring(fileName.lastIndexOf("."));
        // 用uuid作为文件名，防止生成的临时文件重复

        return File.createTempFile(UUIDGenerator.getUUID(), prefix);
    }
}

