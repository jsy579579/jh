package com.jh.paymentgateway.util.sdk.pay.domain.cashierPay;
import java.util.ArrayList;
import java.util.List;
public class OrderInfo {
    private String id;
    private String businessType;
    private List<OrderGoods> goodsList = new ArrayList<OrderGoods>(10);

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public List<OrderGoods> getGoodsList() { return goodsList; }
    public void setGoodsList(List<OrderGoods> list) { this.goodsList = list; }
    public void addGood(OrderGoods good) { this.goodsList.add(good); }
}
