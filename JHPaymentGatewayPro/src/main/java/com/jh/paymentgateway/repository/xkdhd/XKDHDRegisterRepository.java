package com.jh.paymentgateway.repository.xkdhd;

import com.jh.paymentgateway.pojo.xkdhd.XKDHDRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface XKDHDRegisterRepository extends JpaRepository<XKDHDRegister, String>, JpaSpecificationExecutor<XKDHDRegister> {

    @Query(nativeQuery=true, value = "select * from t_xkdhd_register where id_Card =?1 order by create_Time desc limit 1")
    XKDHDRegister getXKDHDRegisterByIdCard(String idCard);
}
