package com.jh.user.business.impl;

import com.jh.user.business.UserOlderConfigBusiness;
import com.jh.user.pojo.UserOlderConfig;
import com.jh.user.repository.UserOlderConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserOlderConfigBusinessImpl implements UserOlderConfigBusiness {

    @Autowired
    private UserOlderConfigRepository userOlderConfigRepository;



    @Override
    public List<UserOlderConfig> findAll() {
        return userOlderConfigRepository.findAll();
    }
}
