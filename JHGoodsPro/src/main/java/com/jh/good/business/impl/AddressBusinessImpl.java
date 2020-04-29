package com.jh.good.business.impl;

import com.jh.good.business.AddressBusiness;
import com.jh.good.pojo.Address;
import com.jh.good.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * 用户地址实现
 */
@Service
public class AddressBusinessImpl implements AddressBusiness {

    @Autowired
    private AddressRepository addressRepository;

    //查询所有用户地址
    @Override
    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    //根据用户id查询地址
    @Override
    public List<Address> findByUserId(Long id) {
        Specification spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                //根据企业id查询
                return cb.equal(root.get("userId"),id);
            }
        };
        return addressRepository.findAll(spec);
    }
    // 保存地址
    @Transactional
    @Override
    public void save(Address address) {
        if (address.getIsDefault()) {
            List<Address> list = findByUserId(address.getUserId());
            for (Address address1 : list) {
                address1.setIsDefault(false);
                addressRepository.save(address);
            }
        }
        addressRepository.save(address);
    }

    @Override
    public Address findById(Long addressId) {
        Specification spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                //根据企业id查询
                return cb.equal(root.get("id"),addressId);
            }
        };
        return addressRepository.findOne(spec);
    }

    /**
     * 删除地址
     * @param id
     */
    @Override
    public void del(Long id) {
        addressRepository.deleteById(id);
    }
}
