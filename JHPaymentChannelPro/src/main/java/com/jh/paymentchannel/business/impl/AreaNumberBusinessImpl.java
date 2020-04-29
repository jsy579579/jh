package com.jh.paymentchannel.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.AreaNumberBusiness;
import com.jh.paymentchannel.pojo.AreaNumber;
import com.jh.paymentchannel.repository.AreaNumberRepository;

@Service
public class AreaNumberBusinessImpl implements AreaNumberBusiness {
	
	@Autowired
	private AreaNumberRepository areaNumberRepository;
	
	@Autowired
	private EntityManager em; 
	
	@Override
	public List<AreaNumber> queryAreaNumberByAll(String province, String city, String area) {
		
		List<AreaNumber> an = null;
		
		if(province!=null&&!province.equals("")){
			if(city!=null&&!city.equals("")){
				if(area!=null&&!area.equals("")){
					an = areaNumberRepository.queryAreaNumberByAll1(province, city, area);
				}else{
					an = areaNumberRepository.queryAreaNumberByAll2(province, city);
				}
			}else{
				if(area!=null&&!area.equals("")){
					an = areaNumberRepository.queryAreaNumberByAll3(province, area);
				}else{
					an = areaNumberRepository.queryAreaNumberByAll5(province);
				}
			}
		}else{
			if(city!=null&&!city.equals("")){
				if(area!=null&&!area.equals("")){
					an = areaNumberRepository.queryAreaNumberByAll4(city, area);
				}else{
					an = areaNumberRepository.queryAreaNumberByAll6(city);
				}
			}else{
				if(area!=null&&!area.equals("")){
					an = areaNumberRepository.queryAreaNumberByAll7(area);
				}
			}
		}
		
		return an;
	}

}
