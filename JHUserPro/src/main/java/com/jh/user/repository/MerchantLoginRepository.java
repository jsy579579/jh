package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.user.pojo.Merchant;
@Repository
public interface MerchantLoginRepository extends JpaRepository<Merchant, String>,JpaSpecificationExecutor<Merchant>{
	/**根据手机号和密码判断是否登陆*/
	@Query("select merchant from  Merchant merchant where merchant.preMchId=:preMchId and merchant.password=:password")
	Merchant findMerchantByMchIdAndPassword(@Param("preMchId") String preMchId, @Param("password") String password);

    /**查询表中preMchId的数据*/
	@Query("select preMchId from Merchant")
	List<Merchant> findMchIdByAll();
	
	/**查询商户信息*/
	@Query("select password from Merchant merchant where merchant.preMchId=:preMchId")
	Merchant findMchIdByPreMchid(@Param("preMchId") String preMchId);
	
	/**查询所有数据*/
	@Query("select merchant from Merchant merchant")
	List<Merchant> findAllMerchant();
	
	/**根据商户号查询到商户*/
	@Query("select merchant from Merchant merchant where merchant.preMchId=:preMchId")
	Merchant queryMerchantByMchId(@Param("preMchId") String preMchId);
	
	/**根据perMchId查询*/
	@Query("select merchant from Merchant merchant where merchant.preMchId=:preMchId")
	Merchant queryAllByPerMchId(@Param("preMchId") String preMchId);
}
