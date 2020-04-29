package com.jh.user.moudle.cardloans;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public  interface  IBaseRepository<T,ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

}
