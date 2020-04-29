package com.jh.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.SMSBlackList;

@Repository
public interface SMSBlackListRepository extends JpaRepository<SMSBlackList, Long>,JpaSpecificationExecutor<SMSBlackList> {

	SMSBlackList findByIpAddress(String ipAddress);

}
