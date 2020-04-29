package com.jh.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UpGradeDetail;

import java.util.List;

@Repository
public interface UpGradeDetailRepository  extends JpaRepository<UpGradeDetail,String>,JpaSpecificationExecutor<UpGradeDetail>{


    @Query(value = "select * from  t_upgrade_detail  where  t_upgrade_detail.modify_user_id = :userId and date_format(t_upgrade_detail.create_time, '%Y-%m-%d') = :createTime",nativeQuery = true)
    List<UpGradeDetail> findByUserIdAndCreateTime(@Param("userId") long userId,@Param("createTime") String createTime);

    @Query(value = "select count(*) from  t_upgrade_detail  where  t_upgrade_detail.modify_user_id in (:userIds) and date_format(t_upgrade_detail.create_time, '%Y-%m-%d') = :todayTime",nativeQuery = true)
    int findByUserIdsAndCreateTime(@Param("userIds")Long[] userids,@Param("todayTime") String todayTime);

    @Query(value = "select count(*) from  t_upgrade_detail  where  t_upgrade_detail.modify_user_id in (:userIds) ",nativeQuery = true)
    int findByUserIds(@Param("userIds")Long[] userids);
}
