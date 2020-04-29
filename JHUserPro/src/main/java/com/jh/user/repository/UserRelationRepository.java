package com.jh.user.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserRealtion;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRealtion, Long>,JpaSpecificationExecutor<UserRealtion>{

	List<UserRealtion> findByFirstUserIdOrderByLevel(Long userId);

	UserRealtion findByFirstUserIdAndPreUserId(long userId, long preUserId);

	List<UserRealtion> findByPreUserIdOrderByLevel(long preUserId);

	List<UserRealtion> findByPreUserIdAndLevelInOrderByLevel(long userId, List<Integer> level);

	@Query("select ur.firstUserId from UserRealtion ur where ur.preUserPhone=:preUserPhone and ur.createTime>=:startTime and ur.createTime<=:endTime")
	List<Long> findUserAgentChangeByTimeAndPhone(@Param("preUserPhone") String preUserPhone, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
	
	@Query("select ur.firstUserId from UserRealtion ur where ur.preUserPhone=:preUserPhone and ur.level=:level and ur.createTime>=:startTime and ur.createTime<=:endTime")
	List<Long> findUserAgentChangeByTimeAndPhoneAndLevel(@Param("preUserPhone") String preUserPhone, @Param("level") String level, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
	
	@Modifying
	@Query("update UserRealtion ur set ur.preUserPhone=:preUserPhone where ur.preUserId=:preUserId")
	void updatePreUserPhoneByPreUserId(@Param("preUserPhone") String preUserPhone, @Param("preUserId") long preUserId);

	@Modifying
	@Query("update UserRealtion ur set ur.firstUserPhone=:firstUserPhone where ur.firstUserId=:firstUserId")
	void updateFirstUserPhoneByFirstUserId(@Param("firstUserPhone") String firstUserPhone, @Param("firstUserId") long firstUserId);
	
	@Query("select ur.firstUserId from UserRealtion ur where ur.preUserId=:preUserId")
	Long[] getFirstUserIdByPreUserId(@Param("preUserId") long preUserId);
	

	@Query("select count(*) from UserRealtion ur where ur.preUserId=:preUserId and ur.firstUserGrade>=:firstUserGrade and ur.realNameStatus =1")
	Integer findByPreUserIdAndGrade(@Param("preUserId") long preUserId, @Param("firstUserGrade")int firstUserGrade);

	@Query("select ur from UserRealtion ur where ur.firstUserId=:firstUserId and ur.level<=:level and ur.preUserGrade>=:preUserGrad")
    List<UserRealtion> findByFirstUserIdAndPreUserGrade(@Param("firstUserId") long firstUserId,@Param("level") int level,@Param("preUserGrad") int preUserGrad);

	@Query("select ur from UserRealtion ur where ur.preUserId=:preUserId and ur.firstUserGrade>=:firstUserIdGarde and ur.level=:level")
	List<UserRealtion> findByPreUserIdAndfirstUserGrade(@Param("preUserId") long preUserId, @Param("firstUserIdGarde") int firstUserIdGarde,@Param("level")int level);

	@Query("select ur from UserRealtion ur where ur.firstUserId=:firstUserId and ur.level=:level")
	UserRealtion findByFirstUserIdAndLevel(@Param("firstUserId")long firstUserId,@Param("level") int level);

	@Query("select ur1.preUserId from UserRealtion ur1 where ur1.preUserId in (select ur.preUserId from UserRealtion ur group by ur.preUserId having count(ur.preUserId)>=:firstUser) and ur1.firstUserGrade>=:grade and ur1.level=1 group by ur1.preUserId having count(ur1.preUserId)>:countt")
    Long[] findByCount(@Param("countt") Long countt,@Param("firstUser") Long firstUser,@Param("grade") int grade);


	@Query("select ur1.preUserId from UserRealtion ur1 where ur1.preUserId in (select ur.preUserId from UserRealtion ur group by ur.preUserId having count(ur.preUserId)>=:firstUser) and ur1.firstUserGrade in :grade and ur1.level=1 group by ur1.preUserId having count(ur1.preUserId)>:countt")
	Long[] findByCounts(@Param("countt") Long countt,@Param("firstUser") Long firstUser,@Param("grade") int[] grade);

    @Query(value = "select url.first_user_id from  t_user_relation url where url.pre_user_id = :userId and url.level = :level and date_format(url.create_time, '%Y-%m-%d') = :todayTime",nativeQuery = true)
    Long[] findByPreUserIdAndLevelAndCreateTime(@Param("userId")long userId, @Param("level")int level, @Param("todayTime")String todayTime);


    @Query(value = "select url.first_user_id from  t_user_relation url where url.pre_user_id = :userId and url.level = :level ",nativeQuery = true)
    Long[] findByPreUserIdAndLevel(@Param("userId")long userId, @Param("level")int level);


    @Query(value = "select url.first_user_id from  t_user_relation url where url.pre_user_id = :userId and date_format(url.create_time, '%Y-%m-%d') = :todayTime",nativeQuery = true)
    Long[] findByPreUserIdAndCreateTime(@Param("userId")long userId, @Param("todayTime")String todayTime);

    @Query(value = "select url.firstUserId from  UserRealtion url where url.preUserId = :userId")
    Long[] findByPreUserId(@Param("userId")long userId);
}
