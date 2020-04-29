package com.jh.good.repository;

import com.jh.good.pojo.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GoodsRepository extends JpaRepository<Goods,String>, JpaSpecificationExecutor<Goods> {

}
