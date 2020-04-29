package com.jh.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.sun.xml.internal.bind.v2.model.core.ID;

@NoRepositoryBean
public interface BaseRepository<T>   extends JpaRepository<T, ID> {

	
	boolean support(String modelType);
}
