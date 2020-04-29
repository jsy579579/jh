package com.jh.paymentgateway.repository.kft;

import com.jh.paymentgateway.pojo.kft.KFTRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author zhangchaofeng
 * @date 2019/4/22
 * @description 快付通进件
 */
@Repository
public interface KFTRegisterRepository extends JpaRepository<KFTRegister,Long> {

}
