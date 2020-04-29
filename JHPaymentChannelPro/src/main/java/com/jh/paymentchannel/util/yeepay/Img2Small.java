package com.jh.paymentchannel.util.yeepay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Img2Small {
	/*** 

     * 功能 :按照正方形缩放图片，精度较高

     * 处理150X150或者960X960

     * @param srcImgPath 原图片路径 

     * @param distImgPath  转换大小后图片路径 

     * @param width   转换后图片宽度 

     * @param height  转换后图片高度 

     */  

    public static void resizeImage(String srcImgPath, String distImgPath,int width, int height) throws IOException {  

    	String subfix = "jpg";

        subfix = srcImgPath.substring(srcImgPath.lastIndexOf(".")+1,srcImgPath.length());

         

        File srcFile = new File(srcImgPath);  

        Image srcImg = ImageIO.read(srcFile);  
        float w = srcImg.getWidth(null);
        float h = srcImg.getHeight(null);
        int wid = (int)Math.ceil(Double.valueOf(String.valueOf(width*(w/h))));
        srcImg.flush();
        BufferedImage buffImg = null; 

        if(subfix.equals("png")){

            buffImg = new BufferedImage(wid, height, BufferedImage.TYPE_INT_ARGB);

        }else{

            buffImg = new BufferedImage(wid, height, BufferedImage.TYPE_INT_RGB);

        }
        buffImg.flush();

        Graphics2D graphics = buffImg.createGraphics();

        graphics.setBackground(Color.WHITE);

        graphics.setColor(Color.WHITE);

        graphics.fillRect(0, 0, wid, height);

        graphics.drawImage(srcImg.getScaledInstance(wid, height, Image.SCALE_SMOOTH), 0, 0, null);  

        ImageIO.write(buffImg, subfix, new File(distImgPath));  
        
        File file = new File(distImgPath);
        int size = (int)Math.ceil(Double.valueOf(String.format("%.1f",file.length()/1024.0)));
        
        if(size>512) {
        	width -= 400;
        	height -=400;
        	resizeImage( srcImgPath,  distImgPath, width,  height);
        }
    }  

}
