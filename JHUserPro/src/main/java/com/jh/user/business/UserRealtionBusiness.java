package com.jh.user.business;

import java.util.List;
import java.util.Map;

import com.jh.user.pojo.User;
import com.jh.user.pojo.UserRealtion;

public interface UserRealtionBusiness {

	public void addRealtion();
	
	public void updateRealtion(User user);

	public List<UserRealtion> findByFirstUserId(Long userId);

	public void addOneUserRealtion(User user);

	public List<UserRealtion> findByPreUserId(long preUserId);

	public void deleteUserRealtions(List<UserRealtion> preUserRealtions);
	
	public Integer findByPreUserIdAndGradeAndLeve(long preUserId,int grade);


	public List<UserRealtion> findByPreUserIdAndLevelInOrderByLevel(long userId, List<Integer> level);

	public void deleteAndRebuildUserRealtion(User oriUser, List<Long> sonUserIds);

	public UserRealtion findByFirstUserIdAndPreUserId(Long firstUserId, Long preUserId);

	public void updateUserToBrandUser(User user);

	public void updateUserToBrandUser2(User user);
	
	public void updatePreUserPhoneByPreUserId(String preUserPhone, long preUserId);
	
	public void updateFirstUserPhoneByFirstUserId(String firstUserPhone, long firstUserId);
	
	public Long[] getFirstUserIdByPreUserId(long preUserId);

	List<UserRealtion> findByFirstUserIdAndPreUserGrade(long firstUserId,int  level,int preUserGrad);

	List<UserRealtion> findByPreUserIdAndfirstUserGrade(long preUserId, int firstUserIdGarde,int level);

	UserRealtion findByFirstUserIdAndLevel(long firstUserId, int level);

    UserRealtion queryUserRelationByFirstUserIdAndPreUserId(Long firstUserId, Long preUserId);
}
