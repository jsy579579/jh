package com.jh.good.pojo;

import javax.persistence.*;

@Entity
@Table(name = "tb_moments_message_img")
public class MomentsMessageImg {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moments_message_id")
    private Long momentsMessageId;

    @Column(name = "img_url")
    private String imgUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMomentsMessageId() {
        return momentsMessageId;
    }

    public void setMomentsMessageId(Long momentsMessageId) {
        this.momentsMessageId = momentsMessageId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
