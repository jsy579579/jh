package com.jh.paymentgateway.util.h5;

public class Extra {

	
	//费率处理方法
		public static String extratrans(String  extraFee1) {
			String extraFee = "";
			if(extraFee1.equals("0.5")){
				 extraFee = extraFee1;
				System.out.println(extraFee);
			}else if (extraFee1.contains("0")&!extraFee1.equals("0.5")){
					 int idx = extraFee1.lastIndexOf(".");
					   extraFee = extraFee1.substring(0,idx);
						System.out.println(extraFee);
				}else{
					   extraFee = extraFee1;
						System.out.println(extraFee);
				}
			return extraFee;
		
		}

}
