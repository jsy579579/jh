package com.cardmanager.pro.consumerReplacement;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jayden
 */
@Service
public class ConsumeReplacementServiceImpl implements ConsumeReplacementService {

    @Autowired
    private ConsumeReplacementRepository consumeReplacementRepository;

    @Override
    public ConsumeTaskPOJO findByConsumeTaskId(String failedConsumeTaskId, int taskStatus, int orderStatus) {
        return consumeReplacementRepository.findByConsumeTaskId(failedConsumeTaskId,taskStatus,orderStatus);
    }
}
