package com.jh.user.repository;

import com.jh.user.pojo.UserManageHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface UserManageHistoryRepository extends JpaRepository<UserManageHistory,Long>, JpaSpecificationExecutor<UserManageHistory> {


    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId")
    List<UserManageHistory> findBybrandId(@Param("brandId") Long brandId, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.preUserPhone=:preUserPhone")
    List<UserManageHistory> findBybrandIdAndPrePhone(@Param("brandId")Long brandId,@Param("preUserPhone") String preUserPhone, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.firstUserPhone=:firstUserPhone")
    List<UserManageHistory> findBybrandIdAndFirPhone(@Param("brandId")Long brandId, @Param("firstUserPhone") String firstUserPhone, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.firstUserPhone=:firstUserPhone and u.preUserPhone=:preUserPhone")
    List<UserManageHistory> findBybrandIdAndFirPhoneAndPrePhone(@Param("brandId") Long brandId, @Param("firstUserPhone") String firstUserPhone,@Param("preUserPhone") String preUserPhone, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.createTime>=:strdate and u.createTime<=:enddate")
    List<UserManageHistory> findBybrandIdAndTime(@Param("brandId")Long brandId, @Param("strdate")Date strdate,@Param("enddate") Date enddate, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.createTime>=:strdate and u.createTime<=:enddate and u.preUserPhone=:prePhone")
    List<UserManageHistory> findBybrandIdAndPrePhoneAndTime(@Param("brandId")Long brandId,@Param("strdate") Date strdate, @Param("enddate")Date enddate, @Param("prePhone")String prePhone, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.createTime>=:strdate and u.createTime<=:enddate and u.firstUserPhone=:firPhone")
    List<UserManageHistory> findBybrandIdAndFirPhoneAndTime(@Param("brandId")Long brandId,@Param("strdate") Date strdate, @Param("enddate")Date enddate, @Param("firPhone")String firPhone, Pageable pageable);
    @Query("select u from UserManageHistory u,User a where u.firstUserId=a.id and a.brandId=:brandId and u.createTime>=:strdate and u.createTime<=:enddate and u.firstUserPhone=:firPhone and u.preUserPhone=:prePhone")
    List<UserManageHistory> findBybrandIdAndFirPhoneAndPrePhoneAndTime(@Param("brandId")Long brandId,@Param("strdate") Date strdate,@Param("enddate") Date enddate,@Param("firPhone") String firPhone,  @Param("prePhone")String prePhone, Pageable pageable);
}
