package com.jh.good.business.impl;

import com.jh.good.business.GoodsBusiness;
import com.jh.good.pojo.Goods;
import com.jh.good.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品实现
 */
@Service
public class GoodsBusinessImpl implements GoodsBusiness {

    @Autowired
    private GoodsRepository goodsRepository;

    // 查询全部
    @Override
    public List<Goods> findAll() {
        return goodsRepository.findAll();
    }

    //分页带搜索商品
    @Override
    public Page<Goods> searchGoods(int page, int size, String goodsName,Long category1Id) {
        Pageable pageable = new PageRequest(page,size);
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                    if (goodsName!= null && !"".equals(goodsName)) {
                        predicates.add(cb.like(root.get("goodsName"), "%"+goodsName+"%"));
                    }
                    if (category1Id!= null && !"".equals(category1Id)) {
                        predicates.add(cb.equal(root.get("category1Id"), category1Id));
                    }
                    predicates.add(cb.equal(root.get("isMarketable"), "0"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        Page<Goods> all = goodsRepository.findAll(specification,pageable);
        return all;
    }

    //新增 or 修改 商品
    public void save(Goods goods){
        goodsRepository.save(goods);
    }

    //根据id查询单个商品
    public Goods findById(Long id) {

        Specification<Goods> spect = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder cb) {
                //根据id查询
                return cb.equal(root.get("id"),id);
            }
        };
        Goods goods = goodsRepository.findOne(spect);
        return goods;
    }
    // 商品上下架
    @Override
    public void isMarketable(Long goodsId, String isMarketable) {
        Goods goods = findById(goodsId);
        goods.setIsMarketable(isMarketable);
        save(goods);
    }


}
