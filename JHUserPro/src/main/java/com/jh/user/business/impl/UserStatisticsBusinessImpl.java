package com.jh.user.business.impl;

import java.util.Date;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jh.user.business.UserStatisticsBusiness;
import com.jh.user.pojo.User;
import com.jh.user.repository.UserRepository;

@Service
public class UserStatisticsBusinessImpl  implements UserStatisticsBusiness {

	@Autowired
	private UserRepository  userRepository; 
	
	@Autowired
	private EntityManager em;
	
	
	@Override
	public Page<User> findPageUser(String phone, String brandid, Date startTime,
			Date endTime, Pageable pageAble) {
		
		
		if(phone != null && !phone.equalsIgnoreCase("")){
			
			
			if(brandid != null && !brandid.equalsIgnoreCase("")){
				
				
				
				if(startTime != null){
					
					if(endTime != null){
						
						return userRepository.findAllPageUser(phone, Long.parseLong(brandid), startTime, endTime, pageAble);
						
					}else{
						
						
						return userRepository.findAllPageUser(phone, Long.parseLong(brandid), startTime, pageAble);
					}
					
				}else{
					
					
					return userRepository.findAllPageUser(phone, Long.parseLong(brandid), pageAble);
					
				}
				
				
				
			}else{
				
				
				if(startTime != null){
					
					if(endTime != null){
						
						return userRepository.findAllPageUser(phone, startTime, endTime,  pageAble);
						
					}else{
						
						
						return userRepository.findAllPageUser(phone, startTime, pageAble);
					}
					
				}else{
					
					
					return userRepository.findAllPageUser(phone, pageAble);
					
				}
				
			}
			
			
		}else{

			
			if(brandid != null && !brandid.equalsIgnoreCase("")){
				
				
				
				if(startTime != null){
					
					if(endTime != null){
						
						return userRepository.findAllPageUser(Long.parseLong(brandid), startTime, endTime, pageAble);
						
					}else{
						
						
						return userRepository.findAllPageUser(Long.parseLong(brandid), startTime, pageAble);
					}
					
				}else{
					
					
					return userRepository.findAllPageUser(Long.parseLong(brandid), pageAble);
					
				}

			}else{
				return userRepository.findAll(pageAble);
			}
			
			
			
		}
	}

}
