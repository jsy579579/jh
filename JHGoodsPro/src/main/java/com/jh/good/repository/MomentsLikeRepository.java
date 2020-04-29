package com.jh.good.repository;


import com.jh.good.pojo.MomentsLike;
import com.jh.good.pojo.MomentsMessageImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MomentsLikeRepository extends JpaRepository<MomentsLike,Long>, JpaSpecificationExecutor<MomentsLike> {

    @Query(value = "delete from MomentsLike mlike where mlike.userId = ?1 and mlike.momentsId = ?2")
    @Modifying
    void del(Long userId, Long momentsId);
}
