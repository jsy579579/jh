package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.HCDE;

import java.util.List;

public interface HCDEBusiness {
    public List<HCDE> getAll();

    public void create(HCDE hcde);

    public void del(HCDE hcde);
}
