package com.jh.paymentgateway.util;



import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
	String value();
}
