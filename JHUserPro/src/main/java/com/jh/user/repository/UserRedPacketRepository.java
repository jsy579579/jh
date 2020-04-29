package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserRedPacket;

@Repository
public interface UserRedPacketRepository  extends JpaRepository<UserRedPacket,String>,JpaSpecificationExecutor<UserRedPacket>{

	
	
	@Query(" from  UserRedPacket")
	List<UserRedPacket>  findUserShops();
	
	@Query(" from  UserRedPacket userRedPacket where userRedPacket.id=:id ")
	UserRedPacket findUserShopsById(@Param("id") long id);
	
	@Query(" from  UserRedPacket userRedPacket where userRedPacket.brandId=:brandId ")
	List<UserRedPacket> findUserShopsBybrandId(@Param("brandId") long brandId);
	
	
}
