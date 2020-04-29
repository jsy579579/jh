package com.cardmanager.pro.business;

import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

import java.util.List;

public interface RepaymentTaskService {

    RepaymentTaskPOJO findRepaymentTaskByRepaymentTaskId(String repaymentTaskId);

    RepaymentTaskPOJO saveRepaymentTaskByRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTask);

    RepaymentTaskPOJO saveRepaymentTaskByRepaymentTask(RepaymentTaskPOJO repaymentTask);

    List<RepaymentTaskPOJO> findRepaymentTaskByDescriptionAndVersionAndExeCuteDate(String description, String[] version, String executeDate, int taskStatus, int orderStatus);
}
