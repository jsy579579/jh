package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.AreaNumber;

@Repository
public interface AreaNumberRepository extends JpaRepository<AreaNumber, String>, JpaSpecificationExecutor<AreaNumber>{
	
	@Query(" select an.areano from AreaNumber an where an.province like %:province% and an.city like %:city% and an.area like %:area%")
	List<AreaNumber> queryAreaNumberByAll1(@Param("province") String province, @Param("city") String city, @Param("area") String area);
	
	@Query(" select an.areano from AreaNumber an where an.province like %:province% and an.city like %:city%")
	List<AreaNumber> queryAreaNumberByAll2(@Param("province") String province, @Param("city") String city);
	
	@Query(" select an.areano from AreaNumber an where an.province like %:province% and an.area like %:area%")
	List<AreaNumber> queryAreaNumberByAll3(@Param("province") String province, @Param("area") String area);
	
	@Query(" select an.areano from AreaNumber an where an.city like %:city% and an.area like %:area%")
	List<AreaNumber> queryAreaNumberByAll4(@Param("city") String city, @Param("area") String area);
	
	@Query(" select an.areano from AreaNumber an where an.province like %:province%")
	List<AreaNumber> queryAreaNumberByAll5(@Param("province") String province);
	
	@Query(" select an.areano from AreaNumber an where an.city like %:city%")
	List<AreaNumber> queryAreaNumberByAll6(@Param("city") String city);
	
	@Query(" select an.areano from AreaNumber an where an.area like %:area%")
	List<AreaNumber> queryAreaNumberByAll7(@Param("area") String area);
	
}
