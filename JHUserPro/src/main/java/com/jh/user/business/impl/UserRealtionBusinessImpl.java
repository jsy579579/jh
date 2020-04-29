package com.jh.user.business.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRealtionBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserRealtion;
import com.jh.user.repository.BrandRepository;
import com.jh.user.repository.UserRelationRepository;
import com.jh.user.repository.UserRepository;

import cn.jh.common.utils.CommonConstants;

@Service
public class UserRealtionBusinessImpl implements UserRealtionBusiness {

	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	private UserRelationRepository userRealtionRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BrandRepository brandRepository;
	
	@Autowired
	private EntityManager em;
	
	@Transactional
	@Override
	public void addRealtion(){
		int size = 20;
		long count = userRepository.count();
		for(int i = 0; i < count/20 + 1; i++){
			Pageable pageable = new PageRequest(i, size);
			Page<User> userPages = userRepository.findAll(pageable);
			List<User> users = userPages.getContent();
			for(int j = 0; j < users.size();j++){
				User user = users.get(j);
				this.updateRealtion(user);
			}
		}
	}
	
	@Transactional
	@Override
	public void updateRealtion(User user){
		long userId = user.getId();
		String phone = user.getPhone();
		String grade = user.getGrade();
		String realNameStatus = user.getRealnameStatus();
		long preUserId = user.getPreUserId();
		User nowUser = user;
		String preUserPhone = user.getPreUserPhone();
		int level = 1;
		while(true){
			if(preUserId == 0){
				break;
			}
			User preUser = userRepository.findUserById(preUserId);
			if(preUser == null){
				System.out.println("无该用户数据======prePhone:"+preUserPhone+" ;preUserId:" + preUserId);
				Brand brand = brandRepository.findBrandByBrand(nowUser.getBrandId());
				User brandUser = userRepository.findUserById(brand.getManageid());
				if(brandUser != null){
					nowUser.setPreUserId(brand.getManageid());
					nowUser.setPreUserPhone(brandUser.getPhone());
					userRepository.save(nowUser);
					preUser = brandUser;
				}else{
					break;
				}
			}
			nowUser = preUser;
			String preGrade = preUser.getGrade();
			UserRealtion userRealtion = new UserRealtion();
			userRealtion.setFirstUserId(userId);
			userRealtion.setFirstUserPhone(phone);
			userRealtion.setFirstUserGrade(Integer.valueOf(grade));
			userRealtion.setRealNameStatus(Integer.valueOf(realNameStatus));
			
			preUserId = preUser.getId();
			preUserPhone = preUser.getPhone();
			
			userRealtion.setPreUserId(preUserId);
			userRealtion.setPreUserPhone(preUserPhone);
			userRealtion.setPreUserGrade(Integer.valueOf(preGrade));
			userRealtion.setLevel(level);
			try {
				userRealtionRepository.save(userRealtion);
			} catch (Exception e) {
				e.printStackTrace();
				UserRealtion userRealtion2 = userRealtionRepository.findByFirstUserIdAndPreUserId(userId,preUserId);
				if(userRealtion2 != null && userRealtion2.getLevel().intValue() != userRealtion.getLevel().intValue()){
					Brand brand = brandRepository.findBrandByBrand(nowUser.getBrandId());
					User brandUser = userRepository.findUserById(brand.getManageid());
					nowUser.setPreUserId(brandUser.getId());
					nowUser.setPreUserPhone(brandUser.getPhone());
					userRepository.save(nowUser);
					preUserId = nowUser.getPreUserId();
					continue;
				}
			}
			level += 1;
			preUserId = preUser.getPreUserId();
		}
	}

	@Override
	public List<UserRealtion> findByFirstUserId(Long userId) {
		List<UserRealtion> userRealtions= userRealtionRepository.findByFirstUserIdOrderByLevel(userId);
		em.clear();
		return userRealtions;
	}

	
	@Override
	public void addOneUserRealtion(User user) {
		this.updateRealtion(user);
	}

	@Override
	public List<UserRealtion> findByPreUserId(long preUserId) {
		return userRealtionRepository.findByPreUserIdOrderByLevel(preUserId);
	}

	@Transactional
	@Override
	public void deleteUserRealtions(List<UserRealtion> preUserRealtions) {
		userRealtionRepository.delete(preUserRealtions);
		em.flush();
	}

	@Override
	public List<UserRealtion> findByPreUserIdAndLevelInOrderByLevel(long userId, List<Integer> level) {
		return userRealtionRepository.findByPreUserIdAndLevelInOrderByLevel(userId,level);
	}

	@Transactional
	@Override
	public void deleteAndRebuildUserRealtion(User oriUser, List<Long> sonUserIds) {
		userLoginRegisterBusiness.saveUser(oriUser);
		this.deleteAndRebuildUserRealtion(oriUser.getId());
		for(Long userId:sonUserIds){
			this.deleteAndRebuildUserRealtion(userId);
		}
	}
	
	
	/**
	 * 先删除传入userId和所有上级的关系,再重新建立关系
	 * @param userId
	 */
	private void deleteAndRebuildUserRealtion(long userId){
		List<UserRealtion> preUserRealtions = this.findByFirstUserId(userId);
		if(preUserRealtions != null && preUserRealtions.size() > 0){
			this.deleteUserRealtions(preUserRealtions);
		}
		User user = userLoginRegisterBusiness.queryUserById(userId);
		if(user !=null){
			this.addOneUserRealtion(user);
		}
	}

	@Override
	public UserRealtion findByFirstUserIdAndPreUserId(Long firstUserId, Long preUserId) {
		return userRealtionRepository.findByFirstUserIdAndPreUserId(firstUserId,preUserId);
	}

	
	/**
	 * 将该user的直属下级转移到该user的上级下,再对该user的上级关系进行删除
	 */
	@Transactional
	@Override
	public void updateUserToBrandUser(User user) {
		long preUserId = user.getPreUserId();
		String preUserPhone = user.getPreUserPhone();
		List<Integer> level = new ArrayList<>();
		level.add(1);
//		查出该用户的所有直属下级
		List<UserRealtion> level1SonUserRelation = this.findByPreUserIdAndLevelInOrderByLevel(user.getId(),level);
		if(level1SonUserRelation != null && level1SonUserRelation.size() > 0){
			for(int i = 0;i < level1SonUserRelation.size();i++){
				UserRealtion userRealtion = level1SonUserRelation.get(i);
				User userSon = userLoginRegisterBusiness.queryUserById(userRealtion.getFirstUserId());
				if(userSon != null){
					userSon.setPreUserId(preUserId);
					userSon.setPreUserPhone(preUserPhone);
					List<UserRealtion> userRelation = this.findByPreUserId(user.getId());
					List<Long> sonUserIds = new ArrayList<>();
					if(userRelation != null && userRelation.size() > 0){
						for(int j = 0; j < userRelation.size();j++){
							UserRealtion userRealtion2 = userRelation.get(j);
							sonUserIds.add(userRealtion2.getFirstUserId());
						}
					}
					this.deleteAndRebuildUserRealtion(userSon, sonUserIds);
				}
			}
		}
		user.setPreUserId(0);
		user.setPreUserPhone(null);
		List<UserRealtion> preUserRelation = this.findByFirstUserId(user.getId());
		if(preUserRelation != null && preUserRelation.size() > 0){
			this.deleteUserRealtions(preUserRelation);
		}
		userLoginRegisterBusiness.saveUser(user);
	}

	@Transactional
	@Override
	public void updateUserToBrandUser2(User user) {
		user.setPreUserId(0);
		user.setPreUserPhone(null);
		List<UserRealtion> preUserRelation = this.findByFirstUserId(user.getId());
		if(preUserRelation != null && preUserRelation.size() > 0){
			this.deleteUserRealtions(preUserRelation);
		}
		userLoginRegisterBusiness.saveUser(user);
		List<UserRealtion> allSonUserRelations = this.findByPreUserId(user.getId());
		for(UserRealtion userRealtion:allSonUserRelations) {
			List<UserRealtion> sonPreUserRelation = this.findByFirstUserId(userRealtion.getFirstUserId());
			for(UserRealtion userRealtion2:sonPreUserRelation) {
				if(userRealtion2.getLevel().intValue() > userRealtion.getLevel().intValue()) {
					ArrayList<UserRealtion> userRealtionList = new ArrayList<UserRealtion>();
					userRealtionList.add(userRealtion2);
					this.deleteUserRealtions(userRealtionList);
				}
			}
		}
	}

	@Transactional
	@Override
	public void updatePreUserPhoneByPreUserId(String preUserPhone, long preUserId) {
		userRealtionRepository.updatePreUserPhoneByPreUserId(preUserPhone, preUserId);
		em.flush();
}

	@Transactional
	@Override
	public void updateFirstUserPhoneByFirstUserId(String firstUserPhone, long firstUserId) {
		userRealtionRepository.updateFirstUserPhoneByFirstUserId(firstUserPhone, firstUserId);
		em.flush();
	}
	
	@Override
	public Long[] getFirstUserIdByPreUserId(long preUserId) {
		em.clear();
		Long[] result = userRealtionRepository.getFirstUserIdByPreUserId(preUserId);
		return result;
	}

	@Override
	public List<UserRealtion> findByFirstUserIdAndPreUserGrade(long firstUserId,int level,int preUserGrad) {
		return userRealtionRepository.findByFirstUserIdAndPreUserGrade(firstUserId,level,preUserGrad);
	}

	@Override
	public List<UserRealtion> findByPreUserIdAndfirstUserGrade(long preUserId, int firstUserIdGarde,int level) {
		return userRealtionRepository.findByPreUserIdAndfirstUserGrade(preUserId,firstUserIdGarde,level);
	}

	@Override
	public Integer findByPreUserIdAndGradeAndLeve(long preUserId,int grade){
		return userRealtionRepository.findByPreUserIdAndGrade(preUserId, grade);
	}
	@Override
	public UserRealtion findByFirstUserIdAndLevel(long firstUserId, int level) {
		return userRealtionRepository.findByFirstUserIdAndLevel(firstUserId,level);
	}

	@Override
	public UserRealtion queryUserRelationByFirstUserIdAndPreUserId(Long firstUserId, Long preUserId) {
		return userRealtionRepository.findByFirstUserIdAndPreUserId(firstUserId,preUserId);
	}
}
