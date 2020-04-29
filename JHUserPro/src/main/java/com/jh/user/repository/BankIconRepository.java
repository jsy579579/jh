package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BankIcon;

@Repository
public interface BankIconRepository extends JpaRepository<BankIcon, String>, JpaSpecificationExecutor<BankIcon>{
	
	@Query(" select bi from BankIcon bi")
	public List<BankIcon> getBankIcon();

}
