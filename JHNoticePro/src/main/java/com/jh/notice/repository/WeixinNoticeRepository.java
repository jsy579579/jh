package com.jh.notice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.WeixinNotice;

@Repository
public interface  WeixinNoticeRepository extends  PagingAndSortingRepository<WeixinNotice, String>{

}
