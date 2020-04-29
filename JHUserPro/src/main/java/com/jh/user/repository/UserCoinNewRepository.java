package com.jh.user.repository;

import com.jh.user.pojo.UserCoinHistoryNew;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserCoinNewRepository extends JpaRepository<UserCoinHistoryNew,Long>, JpaSpecificationExecutor<UserCoinHistoryNew> {

    @Query("select u from UserCoinHistoryNew u where u.userId in (:longs)")
    List<UserCoinHistoryNew> findByUserIdList(@Param("longs") Long[] longs, Pageable pageable);
    @Query("select u from UserCoinHistoryNew u where u.userId=:id")
    List<UserCoinHistoryNew> findByUserId(@Param("id")long id, Pageable pageable);
    @Query("select u from UserCoinHistoryNew u where u.ordercode=:orderCode")
    List<UserCoinHistoryNew> findByOrderCode(@Param("orderCode") String orderCode, Pageable pageable);
    @Query("select u from UserCoinHistoryNew u where u.userId in (:longs) and u.createTime>=:strdate and u.createTime<=:enddate")
    List<UserCoinHistoryNew> findByUserIdListAndTime(@Param("longs") Long[] longs, @Param("strdate") Date strdate, @Param("enddate") Date enddate, Pageable pageable);
    @Query("select u from UserCoinHistoryNew u where u.userId=:id and u.createTime>=:strdate and u.createTime<=:enddate ")
    List<UserCoinHistoryNew> findByUserIdAndTime(@Param("id") long id,@Param("strdate") Date strdate, @Param("enddate") Date enddate, Pageable pageable);
    @Query("select u from UserCoinHistoryNew u where u.ordercode=:orderCode and u.createTime>=:strdate and u.createTime<=:enddate ")
    List<UserCoinHistoryNew> findByOrderCodeAndTime(@Param("orderCode") String orderCode, @Param("strdate") Date strdate,@Param("enddate")  Date enddate, Pageable pageable);
}
