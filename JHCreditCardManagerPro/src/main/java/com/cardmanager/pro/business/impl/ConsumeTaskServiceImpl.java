package com.cardmanager.pro.business.impl;

import com.cardmanager.pro.business.ConsumeTaskService;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.repository.ConsumeTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumeTaskServiceImpl implements ConsumeTaskService {

    @Autowired
    ConsumeTaskRepository consumeTaskRepository;

    @Override
    public List<ConsumeTaskPOJO> findAbnormalConsumeTaskByMessage(String message, int orderStatus, int taskStatus, String executeDate){
        return consumeTaskRepository.findAbnormalConsumeTaskByMessage(message,orderStatus,taskStatus,executeDate);
    }

    @Override
    public List<ConsumeTaskPOJO> findConsumeTaskByRepaymentTaskId(String repaymentTaskId) {
        return consumeTaskRepository.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
    }

    @Override
    @Transactional
    public ConsumeTaskPOJO saveConsumeTaskByconsumeTaskPOJO(ConsumeTaskPOJO consumeTaskPOJO) {
        return consumeTaskRepository.save(consumeTaskPOJO);
    }

    @Override
    @Transactional
    public ConsumeTaskPOJO saveConsumeTaskByconsumeTask(ConsumeTaskPOJO consumeTaskPOJO) {
        return consumeTaskRepository.save(consumeTaskPOJO);
    }

    @Override
    public List<ConsumeTaskPOJO> findAbnormalConsumeTaskByMessageUserId(String message, int orderStatus, int taskStatus, String executeDate, String userId, String cardNo) {
        return consumeTaskRepository.findAbnormalConsumeTaskByMessageUserId(message,orderStatus,taskStatus,executeDate,userId,cardNo);
    }

    @Override
    public ConsumeTaskPOJO findConsumeTaskByconsumeTaskId2(String consumeTaskId2) {
        return consumeTaskRepository.findConsumeTaskByconsumeTaskId2(consumeTaskId2);
    }

    @Override
    public List<ConsumeTaskPOJO> findByives() {
        return consumeTaskRepository.findByives();
    }

    @Override
    public List<ConsumeTaskPOJO> findByVersion(String version,String executeDate) {
        return consumeTaskRepository.findByVersion(version,executeDate);
    }

    @Override
    public List<String> findFailOrderByVersionAndReturnMessage(String version, int taskStatus, int orderStatus, String returnMessage, String executeDate) {
        return consumeTaskRepository.findFailOrderByVersionAndReturnMessage(version,taskStatus,orderStatus,returnMessage,executeDate);
    }
    @Override
    public List<String> findFailOrderByVersionAndReturnMessage(String version, int taskStatus, int orderStatus, String executeDate) {
        return consumeTaskRepository.findFailOrderByVersionAndReturnMessage(version,taskStatus,orderStatus,executeDate);
    }
    @Override
    public List<ConsumeTaskPOJO> findOrderByCreditCardNo(List<String> creditCardNos,int taskStatus,int orderStatus,String executeDate,String version) {
        return consumeTaskRepository.findOrderByCreditCardNo(creditCardNos,executeDate,version,taskStatus,orderStatus);
    }

}
