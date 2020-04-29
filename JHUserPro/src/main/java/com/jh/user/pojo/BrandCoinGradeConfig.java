package com.jh.user.pojo;


import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "t_brand_coin_grade_config")
public class BrandCoinGradeConfig {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Column(name="brand_id")
    private long brandId;


    @Column(name="grade")
    private int grade;


    @Column(name="type")
    private BigDecimal type;


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

    public BigDecimal getType() {
        return type;
    }

    public void setType(BigDecimal type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
