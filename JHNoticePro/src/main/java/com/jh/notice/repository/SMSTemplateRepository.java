package com.jh.notice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.SMSTemplate;

@Repository
public interface SMSTemplateRepository extends  PagingAndSortingRepository<SMSTemplate, String>{

	@Query("select SMSTemplate from  SMSTemplate SMSTemplate where SMSTemplate.templateType=:template_type ")
	SMSTemplate findSMSTemplate(@Param("template_type") String template_type);

}
