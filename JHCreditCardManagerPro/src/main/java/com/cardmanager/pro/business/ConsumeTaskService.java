package com.cardmanager.pro.business;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;

import java.util.List;

public interface ConsumeTaskService {

    List<ConsumeTaskPOJO> findAbnormalConsumeTaskByMessage(String message, int orderStatus, int taskStatus, String executeDate);

    List<ConsumeTaskPOJO> findConsumeTaskByRepaymentTaskId(String repaymentTaskId);

    ConsumeTaskPOJO saveConsumeTaskByconsumeTaskPOJO(ConsumeTaskPOJO consumeTaskPOJO);

    ConsumeTaskPOJO saveConsumeTaskByconsumeTask(ConsumeTaskPOJO consumeTaskPOJO);

    List<ConsumeTaskPOJO> findAbnormalConsumeTaskByMessageUserId(String message, int orderStatus, int taskStatus, String executeDate, String userId, String cardNo);

    ConsumeTaskPOJO findConsumeTaskByconsumeTaskId2(String consumeTaskId2);

    List<ConsumeTaskPOJO> findByives();

    List<ConsumeTaskPOJO> findByVersion(String version,String  executeDate);

    List<String> findFailOrderByVersionAndReturnMessage(String version, int taskStatus, int orderStatus, String returnMessage, String executeDate);
    List<String> findFailOrderByVersionAndReturnMessage(String version, int taskStatus, int orderStatus, String executeDate);
    List<ConsumeTaskPOJO> findOrderByCreditCardNo(List<String> creditCardNos,int taskStatus,int orderStatus,String executeDate,String version);
}
