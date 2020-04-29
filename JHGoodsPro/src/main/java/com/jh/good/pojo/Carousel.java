package com.jh.good.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 轮播图
 */
@Entity
@Table(name = "tb_carousel")
public class Carousel {

    @Id
    @Column(name = "id")
    private Long id ;   //id

    @Column(name = "img_pic")
    private String imgPic;  //轮播图

    @Column(name = "status")
    private String status;  //状态

    @Column(name = "notes")
    private String notes;   //备注

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgPic() {
        return imgPic;
    }

    public void setImgPic(String imgPic) {
        this.imgPic = imgPic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
