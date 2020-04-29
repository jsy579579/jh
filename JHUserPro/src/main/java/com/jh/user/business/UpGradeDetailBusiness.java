package com.jh.user.business;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.UpGradeDetail;

public interface UpGradeDetailBusiness {
	
	public void createUpGradeDetail(UpGradeDetail upGradeDetail);
	
	public Page<UpGradeDetail> getAllUpGradeDetail(Pageable pageable);
	
	public Page<UpGradeDetail> getAllUpGradeDetailByBrandIdAndMore(int brandId, String phone, String modifyPhone, int modifyGrade, int modifyType, Date startTime, Date endTime, Pageable pageAble);

	public List<UpGradeDetail> queryUpGradeDetailByUseridAndCreateTime(Long userId,String createTime);


    int queryUpGradeDetailByUseridsAndCreateTime(Long[] userids, String todayTime);

    int queryUpGradeDetailByUserids(Long[] userids);
}
