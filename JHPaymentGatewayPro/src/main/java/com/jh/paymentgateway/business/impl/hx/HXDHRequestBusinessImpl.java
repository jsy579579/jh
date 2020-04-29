package com.jh.paymentgateway.business.impl.hx;

import com.jh.paymentgateway.business.hx.HXDHXRequestBusiness;
import com.jh.paymentgateway.pojo.hx.HXDHAddress;
import com.jh.paymentgateway.repository.hx.HXDHAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class HXDHRequestBusinessImpl implements HXDHXRequestBusiness {

    @Autowired
    private EntityManager em;

    @Autowired
    private HXDHAddressRepository hxdhAddressRepository;

    @Override
    public List<HXDHAddress> getHXDHAddressby0() {
        List<HXDHAddress> hxdhAddresses = hxdhAddressRepository.getHXDHAddresses();
        return hxdhAddresses;
    }

    @Override
    public List<HXDHAddress> getHXDHAddressbyProvince(String province) {
        List<HXDHAddress> hxdhAddresses = hxdhAddressRepository.getHXDHAddressesByProvince(province);
        return hxdhAddresses;
    }

    @Override
    public List<HXDHAddress> getHXDHAddressbyCity(String city) {
        return hxdhAddressRepository.getHXDHAddressbyCity(city);
    }

}
