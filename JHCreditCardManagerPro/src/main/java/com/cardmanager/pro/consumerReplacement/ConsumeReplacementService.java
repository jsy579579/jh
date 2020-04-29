package com.cardmanager.pro.consumerReplacement;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;

/**
 * @author jayden
 */
public interface ConsumeReplacementService {
    ConsumeTaskPOJO findByConsumeTaskId(String failedConsumeTaskId, int taskStatus, int orderStatus);
}
