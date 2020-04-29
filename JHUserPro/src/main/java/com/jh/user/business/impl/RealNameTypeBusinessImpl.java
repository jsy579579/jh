package com.jh.user.business.impl;

import com.jh.user.business.RealNameTypeBusiness;
import com.jh.user.pojo.RealNameType;
import com.jh.user.repository.RealNameTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RealNameTypeBusinessImpl implements RealNameTypeBusiness {

    @Autowired
    private RealNameTypeRepository realNameTypeRepository;
    @Override
    public RealNameType queryRealNameType() {
        return realNameTypeRepository.queryRealNameType();
    }
}
