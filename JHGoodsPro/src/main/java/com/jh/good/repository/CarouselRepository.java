package com.jh.good.repository;


import com.jh.good.pojo.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarouselRepository extends JpaRepository<Carousel,Long>, JpaSpecificationExecutor<Carousel> {

}
