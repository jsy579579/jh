package com.cardmanager.pro.consumerReplacement;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ConsumeTaskService {
    @Autowired
    private ConsumeReplacementRepository consumeReplacementRepository;

    public List<ConsumeTaskPOJO> findConsumeTaskByRepaymentTaskId(String repaymentTaskId) {
        List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeReplacementRepository.findConsumeTaskByRepaymentTaskId(repaymentTaskId);
        return consumeTaskPOJOs;
    }

    public void saveConsumeTaskByconsumeTaskPOJO(ConsumeTaskPOJO consumeTaskNew) {
        consumeReplacementRepository.save(consumeTaskNew);
    }

    public void saveConsumeTaskByconsumeTask(ConsumeTaskPOJO consumeTaskPOJO) {
        consumeReplacementRepository.save(consumeTaskPOJO);
    }

    public ConsumeTaskPOJO findConsumeTaskByconsumeTaskId2(String consumeId2) {
        ConsumeTaskPOJO consumeTaskPOJOs = consumeReplacementRepository.findConsumeTaskByconsumeTaskId2(consumeId2);
        return consumeTaskPOJOs;
    }
}
