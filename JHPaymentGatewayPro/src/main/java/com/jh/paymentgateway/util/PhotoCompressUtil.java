package com.jh.paymentgateway.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import net.coobird.thumbnailator.Thumbnails;

public class PhotoCompressUtil {
	
	public static void compressPhoto(String sourceFilePath,String targetFilePath,float quality) throws Exception{
		Thumbnails.of(sourceFilePath).scale(1f).outputQuality(quality).toFile(targetFilePath);
	}
	
	public static void compressPhoto(InputStream inputStream,File file,float quality) throws Exception{
		Thumbnails.of(inputStream).scale(1f).outputQuality(quality).toFile(file);
	}
	
	public static void compressPhoto(InputStream inputStream,OutputStream outputStream,float quality) throws Exception{
		Thumbnails.of(inputStream).scale(1f).outputQuality(quality).toOutputStream(outputStream);
	}
	
	/*public static void main(String[] args) throws Exception {
		String sourceFilePath = "D:\\back.jpg";
		File file = new File(sourceFilePath);
		OutputStream os = new ByteArrayOutputStream();
		InputStream inputStream = new FileInputStream(file);
		try {
			compressPhoto(inputStream, os, 0.2f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ByteArrayInputStream parse = FileUtils.parse(os);
		
	}*/
	
}

