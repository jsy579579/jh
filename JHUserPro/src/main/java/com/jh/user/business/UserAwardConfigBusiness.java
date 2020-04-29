package com.jh.user.business;

import com.jh.user.pojo.UserAwardConfig;

public interface UserAwardConfigBusiness {

	UserAwardConfig findByBrandId(String brandId);

	UserAwardConfig save(UserAwardConfig userAwardConfig);

}
