package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.User;

@Repository
public interface BrandRepository extends JpaRepository<Brand,String>,JpaSpecificationExecutor<Brand>{

	@Query("select brand from  Brand brand where brand.id=:id")
	Brand findBrandByid(@Param("id") long id);
	
	@Query("select brand from  Brand brand")
	List<Brand> findAllBrand();
	
	@Query("select brand from  Brand brand where brand.number=:number")
	Brand findBrandNumber(@Param("number") String number);
	
	@Query("select brand from  Brand brand where brand.name like %:name%")
	List<Brand> findBrandByName(@Param("name") String name);
	
	@Query("select brand from  Brand brand where brand.manageid=:manageid")
	Brand findBrandByUserid(@Param("manageid") long manageid);
	
	@Query("select brand from  Brand brand where brand.manageid in (:manageids)")
	List<Brand> findBrandByUserids(@Param("manageids") Long[] manageids);

	@Query("select brand from  Brand brand where brand.manageid =:userId and brand.id =:brandId")
	Brand findByUserIdAndBrandId(@Param("userId")long userId, @Param("brandId")long brandid);
	
	@Modifying
	@Query("update Brand set brandDescription=:branddescription where id=:brandid")
	void updateBrandDescription(@Param("brandid") long brandid, @Param("branddescription") String branddescription);

	Brand findByManageidAndId(long userId, long brandid);
	
	@Query("select brand from Brand brand where brand.id=:brandid")
	Brand findBrandByBrand(@Param("brandid") long brandid);
	

	
}
