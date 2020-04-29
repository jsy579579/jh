package com.juhe.creditcardapplyfor.bo;



import java.io.Serializable;
import java.util.List;

/**
 * @author huhao
 * @title: CardBO
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/19 001915:21
 */

public class CardBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> bankIds;
    private List<Long> topicIds;
    private List<Long> tagIds;
    private Integer current;

    public List<Long> getBankIds() {
        return bankIds;
    }

    public void setBankIds(List<Long> bankIds) {
        this.bankIds = bankIds;
    }

    public List<Long> getTopicIds() {
        return topicIds;
    }

    public void setTopicIds(List<Long> topicIds) {
        this.topicIds = topicIds;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    @Override
    public String toString() {
        return "CardBO{" +
                "bankIds=" + bankIds +
                ", topicIds=" + topicIds +
                ", tagIds=" + tagIds +
                ", current=" + current +
                '}';
    }
}
