package com.cardmanager.pro.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.CreditCardManagerConfig;
@Repository
public interface CreditCardManagerConfigRepository extends JpaRepository<CreditCardManagerConfig, Long>,JpaSpecificationExecutor<CreditCardManagerConfig>{

	CreditCardManagerConfig findByVersion(String version);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select creditCardManagerConfig from CreditCardManagerConfig creditCardManagerConfig where creditCardManagerConfig.version =:version")
	CreditCardManagerConfig findByVersionLock(@Param("version")String version);

	List<CreditCardManagerConfig> findByCreateOnOff(int createOnOff);

    @Query("select c from CreditCardManagerConfig c where c.version=:version ")
    CreditCardManagerConfig findCardManangerByVersion(@Param("version")String version);
}
