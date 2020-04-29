package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.Area;
import com.jh.user.pojo.BankAcronym;
import com.jh.user.pojo.BankBranch;
import com.jh.user.pojo.City;
import com.jh.user.pojo.Province;
@Repository
public interface BankBranchRepository extends JpaRepository<BankAcronym,String>,JpaSpecificationExecutor<BankAcronym>{

	@Query(value="select bank_no,bank_name from t_bank_branch where (bank_name like %?1% or bank_name like %?2%) and top_name like %?3%",nativeQuery = true)
	List<BankBranch> querybranchinfo(String province,String city, String topName); 
	
	@Query("select province from Province province")
	List<Province> findAllProvince();
	
	@Query("select city from City city where city.provinceid=:provinceid")
	List<City> findAllCity(@Param("provinceid") String provinceid);
	
	@Query("select area from Area area where area.cityid=:cityid")
	List<Area> findAllArea(@Param("cityid") String cityid);
	
	@Query("select bb.bankNo from BankBranch bb where bb.bankName = :bankName")
	public BankBranch getBankCodeByBankName(@Param("bankName") String bankName);
}
