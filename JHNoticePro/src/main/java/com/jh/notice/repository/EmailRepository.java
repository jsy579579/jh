package com.jh.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import com.jh.notice.pojo.EmailRecord;

@Repository
public interface EmailRepository extends  PagingAndSortingRepository<EmailRecord, String>{


	/*@Query("select emailRecord from EmailRecord emailRecord")
	Page<EmailRecord> findEmailRecord(Pageable page);*/
	
}
