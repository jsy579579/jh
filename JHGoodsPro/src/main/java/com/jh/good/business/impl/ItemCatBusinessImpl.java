package com.jh.good.business.impl;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.jh.good.business.ItemCatBusiness;
import com.jh.good.pojo.ItemCat;
import com.jh.good.repository.ItemCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类接口
 */
@Service
public class ItemCatBusinessImpl implements ItemCatBusiness {

    @Autowired
    private ItemCatRepository itemCatRepository;

    // 获取所有类别
    @Override
    public List<ItemCat> findAll() {
        Specification spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("parentId"), 0));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<ItemCat> list = itemCatRepository.findAll(spec);
        return list;
    }

    //根据ParentId查询分类
    public List<ItemCat> findByParentId(ItemCat itemCat){
        Specification spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("parentId"), itemCat.getId()));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return  itemCatRepository.findAll(spec);
    }

}
