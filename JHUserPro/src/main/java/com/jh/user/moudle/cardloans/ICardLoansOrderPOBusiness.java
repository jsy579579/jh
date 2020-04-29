package com.jh.user.moudle.cardloans;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ICardLoansOrderPOBusiness {

	CardLoansOrderPO createOne(LinkConfigPO linkConfigPO, String userId, String phone, String realname, String idcard,
			String loanAmount);

	Page<CardLoansOrderPO> findList(String brandId, String phone, String name, String idcard, String orderType,
			String classify, String orderStatus,String orderCode, Pageable pageable);
	
	Page<CardLoansOrderPO> findList(String userId, String orderStatus, String orderType, String classify,String brandId,Pageable pageable);

	CardLoansOrderPO findById(Long cardLoansOrderId);

	CardLoansOrderPO setOrderStatus(CardLoansOrderPO cardLoansOrderPO, String orderStatus, String rebate);

	List<CardLoansRatioPO> findRatiosByBrandId(String brandId);

	CardLoansRatioPO createCardLoansRatioPO(String brandId, String preGrade, String ratio);

	CardLoansRatioPO findRatiosById(Long id);

	void deleteCardLoansRatioPO(CardLoansRatioPO cardLoansRatioPO);

	CardLoansRatioPO updateCardLoansRatioPO(CardLoansRatioPO cardLoansRatioPO, BigDecimal bigRatio);

	CardLoansRatioPO findRatiosByBrandIdAndPreGrade(String brandId, String preGrade);

	Page<CardLoansRebateHistoryPO> findCardLoansRebateHistoryPOByReceiveUserId(String userId, Pageable pageable);

	CardLoansOrderPO putFeedbackPicture(CardLoansOrderPO cardLoansOrderPO, MultipartFile file1, MultipartFile file2,MultipartFile file3);

	CardLoansOrderPO putFeedbackPicture(CardLoansOrderPO cardLoansOrderPO, String data1, String data2, String data3);

}
