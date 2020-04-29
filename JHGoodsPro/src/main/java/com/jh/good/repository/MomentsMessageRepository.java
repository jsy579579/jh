package com.jh.good.repository;


import com.jh.good.pojo.Carousel;
import com.jh.good.pojo.MomentsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MomentsMessageRepository extends JpaRepository<MomentsMessage,Long>, JpaSpecificationExecutor<MomentsMessage> {

}
