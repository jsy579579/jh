package com.jh.good.business;


import com.jh.good.pojo.MomentsMessage;
import org.springframework.data.domain.Page;

public interface MomentsMessageBusiness {

    Page<MomentsMessage> searchGoods(int page, int size);

   void publishNews(MomentsMessage momentsMessage);

    void giveTheThumbsUp(Long userId, Long momentsId);
}
