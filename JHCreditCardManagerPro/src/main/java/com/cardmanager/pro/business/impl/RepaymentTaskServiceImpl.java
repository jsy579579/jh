package com.cardmanager.pro.business.impl;

import com.cardmanager.pro.business.RepaymentTaskService;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.repository.RepaymentTaskPOJORepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepaymentTaskServiceImpl implements RepaymentTaskService {

    @Autowired
    RepaymentTaskPOJORepository repaymentTaskPOJORepository;

    @Override
    public RepaymentTaskPOJO findRepaymentTaskByRepaymentTaskId(String repaymentTaskId){
        return repaymentTaskPOJORepository.findRepaymentTaskByRepaymentTaskId(repaymentTaskId);
    }

    @Override
    @Transactional
    public RepaymentTaskPOJO saveRepaymentTaskByRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTask) {
        return repaymentTaskPOJORepository.save(repaymentTask);
    }

    @Override
    @Transactional
    public RepaymentTaskPOJO saveRepaymentTaskByRepaymentTask(RepaymentTaskPOJO repaymentTask) {
        return repaymentTaskPOJORepository.save(repaymentTask);
    }

    @Override
    public List<RepaymentTaskPOJO> findRepaymentTaskByDescriptionAndVersionAndExeCuteDate(String description, String[] version, String executeDate, int taskStatus, int orderStatus) {
        return repaymentTaskPOJORepository.findRepaymentTaskByDescriptionAndVersionAndExeCuteDate(description,version,executeDate,taskStatus,orderStatus);
    }
}
