package com.jh.good.repository;


import com.jh.good.pojo.Carousel;
import com.jh.good.pojo.MomentsMessageImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MomentsMessageImgRepository extends JpaRepository<MomentsMessageImg,Long>,
        JpaSpecificationExecutor<MomentsMessageImg> {


}
