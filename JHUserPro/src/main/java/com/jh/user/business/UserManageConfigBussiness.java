package com.jh.user.business;


import com.jh.user.pojo.UserManageConfig;

import java.util.List;

public interface UserManageConfigBussiness {

    List<UserManageConfig> findAllStatus();

    UserManageConfig findByBrandId(Long brandId);

    UserManageConfig save(UserManageConfig userManageConfig1);
}
