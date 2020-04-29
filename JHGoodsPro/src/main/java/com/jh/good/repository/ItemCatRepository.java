package com.jh.good.repository;

import com.jh.good.pojo.ItemCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ItemCatRepository extends JpaRepository<ItemCat,String>, JpaSpecificationExecutor<ItemCat> {

}
