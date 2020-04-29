package com.jh.good.pojo;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name ="tb_item_cat" )
public class ItemCat {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name")
    private String name;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "logo_pic")
    private String  logoPic;

    @Transient
    private List<ItemCat> list;

    public List<ItemCat> getList() {
        return list;
    }

    public void setList(List<ItemCat> list) {
        this.list = list;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getLogoPic() {
        return logoPic;
    }

    public void setLogoPic(String logoPic) {
        this.logoPic = logoPic;
    }
}
