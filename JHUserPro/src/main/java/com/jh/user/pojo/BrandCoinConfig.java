package com.jh.user.pojo;


import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "t_brand_coin_config")
public class BrandCoinConfig {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="brand_id")
    private long brandId;

    @Column(name="grade")
    private int grade;

    /**现金换算成积分的比率*/
    @Column(name="ratio")
    private BigDecimal ratio=BigDecimal.ZERO.setScale(3);

    @Column(name="status")
    private int status;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(long brandId) {
        this.brandId = brandId;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BrandCoinConfig{" +
                "id=" + id +
                ", brandId=" + brandId +
                ", grade=" + grade +
                ", ratio=" + ratio +
                ", status=" + status +
                '}';
    }
}
