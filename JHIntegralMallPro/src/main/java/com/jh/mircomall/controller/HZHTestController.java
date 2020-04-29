package com.jh.mircomall.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jh.mircomall.bean.Provinces;
import com.jh.mircomall.service.ProvincesService;

@Controller
@RequestMapping("/v1.0/integralmall/hzh")
public class HZHTestController {
	
	@Autowired
	private ProvincesService provincesService;
	
	@RequestMapping("/test")
	public String test(HttpSession session){
		List<Provinces> listAllProvinces = provincesService.listAllProvinces();	
		session.setAttribute("listAllProvinces", listAllProvinces);
//		session.setAttribute("listAllProvinces", listAllProvinces);
		System.out.println(session.toString());
		return "test";
	}

}
