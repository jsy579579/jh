package com.jh.user.pojo;


import javax.persistence.*;

@Entity
@Table(name = "t_older_config")
public class UserOlderConfig {


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="grade")
    private String grade;

    @Column(name="count")
    private int count;

    @Column(name="first_user")
    private int firstUser;

    @Column(name="brand_id")
    private Long brandId;
    @Column(name = "status")
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(int firstUser) {
        this.firstUser = firstUser;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
}
