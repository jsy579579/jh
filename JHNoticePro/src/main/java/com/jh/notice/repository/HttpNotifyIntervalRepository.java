package com.jh.notice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.HttpNotifyInterval;

@Repository
public interface HttpNotifyIntervalRepository extends  PagingAndSortingRepository<HttpNotifyInterval, String>{

	
	@Query("select httpNotifyInterval from  HttpNotifyInterval httpNotifyInterval  where httpNotifyInterval.curIndex =:curIndex")
	HttpNotifyInterval findHttpNotifyInterval(@Param("curIndex") int curIndex);
	
}
