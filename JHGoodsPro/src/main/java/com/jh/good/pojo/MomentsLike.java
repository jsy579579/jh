package com.jh.good.pojo;

import javax.persistence.*;

@Entity
@Table(name = "tb_moments_like")
public class MomentsLike {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "moment_id")
    private Long momentsId;

    public MomentsLike() {
    }

    public MomentsLike(Long userId, Long momentsId) {
        this.userId = userId;
        this.momentsId = momentsId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMomentsId() {
        return momentsId;
    }

    public void setMomentsId(Long momentsId) {
        this.momentsId = momentsId;
    }
}
