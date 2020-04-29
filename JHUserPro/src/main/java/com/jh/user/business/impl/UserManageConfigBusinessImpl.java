package com.jh.user.business.impl;

import com.jh.user.business.UserManageConfigBussiness;
import com.jh.user.pojo.UserManageConfig;
import com.jh.user.repository.UserManageConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class UserManageConfigBusinessImpl implements UserManageConfigBussiness {

    @Autowired
    private  UserManageConfigRepository userManageConfigRepository;

    @Autowired
    private EntityManager em;

    @Override
    public List<UserManageConfig> findAllStatus() {
        int status=1;
        List<UserManageConfig> userManageConfigList=userManageConfigRepository.findAllStatus(status);
        return userManageConfigList;
    }

    @Override
    public UserManageConfig findByBrandId(Long brandId) {
        return userManageConfigRepository.findByBrandId(brandId);
    }

    @Transactional
    @Override
    public UserManageConfig save(UserManageConfig userManageConfig1) {
        UserManageConfig u=userManageConfigRepository.save(userManageConfig1);
        em.flush();;
        return u;
    }
}
