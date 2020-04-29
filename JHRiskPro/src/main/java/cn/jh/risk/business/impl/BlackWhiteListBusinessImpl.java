package cn.jh.risk.business.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.jh.risk.business.BlackWhiteListBusiness;
import cn.jh.risk.pojo.BlackWhiteList;
import cn.jh.risk.repository.BlackWhiteListRepository;

@Service
public class BlackWhiteListBusinessImpl implements BlackWhiteListBusiness{

	
	@Autowired
    private BlackWhiteListRepository blackWhiteListRepository;
	
	@Override
	public BlackWhiteList merge(BlackWhiteList blackWhiteList) {
		return blackWhiteListRepository.save(blackWhiteList);
	}

	@Override
	public BlackWhiteList findBlackWhiteByPhone(String phone, String operationType) {
		return blackWhiteListRepository.findBlackWhiteList(phone, operationType);
	}
	
	@Override
	public BlackWhiteList findBlackWhiteByUserId(long userId , String operationType){
		
		return blackWhiteListRepository.findBlackWhiteByUserId(userId, operationType);
	}

	@Override
	public Page<BlackWhiteList> findBlackWhiteList(String phone, String brandid,
			Date startTime, Date endTime, Pageable pageAble) {
		
		if(phone != null && !phone.equalsIgnoreCase("")){
			
			if(brandid != null && !brandid.equalsIgnoreCase("")){
				
				
					if(startTime != null){
						
						if(endTime != null){
							
							return blackWhiteListRepository.findBlackWhiteList(phone, Long.parseLong(brandid), startTime, endTime, pageAble);
							
						}else{
							
							
							return blackWhiteListRepository.findBlackWhiteList(phone, Long.parseLong(brandid), startTime, pageAble);
						}
						
					}else{
											
							return blackWhiteListRepository.findBlackWhiteList(phone, Long.parseLong(brandid), pageAble);					
					}
				
			}else{
				
				
				if(startTime != null){
					
					if(endTime != null){
						
						return blackWhiteListRepository.findBlackWhiteList(phone, startTime, endTime, pageAble);
						
					}else{
						
						return blackWhiteListRepository.findBlackWhiteList(phone, startTime, pageAble);
					}
					
				}else{
										
						return blackWhiteListRepository.findBlackWhiteList(phone, pageAble);					
				}
				
			}
			
			
			
		}else{
			
			
			if(brandid != null && !brandid.equalsIgnoreCase("")){
				
				
				if(startTime != null){
					
					if(endTime != null){
						
						return blackWhiteListRepository.findBlackWhiteList(Long.parseLong(brandid), startTime, endTime, pageAble);
						
					}else{
						
						
						return blackWhiteListRepository.findBlackWhiteList(Long.parseLong(brandid), startTime, pageAble);
					}
					
				}else{
					
					
					return blackWhiteListRepository.findBlackWhiteList(Long.parseLong(brandid), pageAble);
					
				}
				
			}else{
				
				
				return blackWhiteListRepository.findAll(pageAble);
				
			}
			
		}
	}

	@Transactional
	@Override
	public void delBlackWhiteAndOperation(String phone, String operationtype) {
		
		if(operationtype != null && !operationtype.equalsIgnoreCase("")){
			
			blackWhiteListRepository.delBlackWhite(phone, operationtype);
			
		}else{
			
			blackWhiteListRepository.delBlackWhite(phone);
			
		}
		
		
	}
	
	
	
	

}
