package com.jh.good.business;


import com.jh.good.pojo.MomentsLike;

public interface MomentsLikeBusiness {

    MomentsLike findByUserIdAndMomentsId(Long userId, Long momentsId);

    void del(Long userId, Long momentsId);

    void add(Long userId, Long momentsId);
}
