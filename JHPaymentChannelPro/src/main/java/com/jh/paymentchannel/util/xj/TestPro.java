package com.jh.paymentchannel.util.xj;

import java.math.BigDecimal;
import java.util.UUID;

public class TestPro {

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		String uuid = UUID.randomUUID().toString(); 
		System.out.print(uuid);
	}*/
	
	public static Integer calculateProfit(String totalFee, double fee, double downFee, Integer d0Fee, Integer downD0Fee) {
        return new BigDecimal(totalFee).multiply(new BigDecimal(String.valueOf(downFee)).subtract(new BigDecimal(String
                .valueOf(fee))).divide(new BigDecimal("1000"))).add(new BigDecimal(downD0Fee)).subtract(new BigDecimal(d0Fee)).intValue();
    }

}
