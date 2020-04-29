package com.cardmanager.pro.consumerReplacement;

import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepaymentTaskService {
    @Autowired
    private RepaymentReplacementRepository repaymentReplacementRepository;
    public RepaymentTaskPOJO findRepaymentTaskByRepaymentTaskId(String repaymentTaskId) {
        RepaymentTaskPOJO repaymentTaskPOJO =repaymentReplacementRepository.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
        return repaymentTaskPOJO;
    }

    public void saveRepaymentTaskByRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTaskNew) {
        repaymentReplacementRepository.save(repaymentTaskNew);
    }

    public void saveRepaymentTaskByRepaymentTask(RepaymentTaskPOJO repaymentTask) {
        repaymentReplacementRepository.save(repaymentTask);
    }
}
