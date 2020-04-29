package com.cardmanager.pro.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardAccountHistoryBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardAccountHistory;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.repository.CreditCardAccountRepository;
import com.cardmanager.pro.util.CardConstss;

@Service
public class CreditCardAccountBusinessImpl implements CreditCardAccountBusiness {

    @Autowired
    private CreditCardAccountRepository creditCardAccountRepository;

    @Autowired
    private CreditCardAccountHistoryBusiness creditCardAccountHistoryBusiness;

    @Autowired
    private RepaymentBillBusiness repaymentBillBusiness;

    @Autowired
    private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

    @Autowired
    private EntityManager em;

    //	不缓存
    @Override
    public CreditCardAccount findByUserIdAndCreditCardNumberAndVersion(String userId, String creditCardNumber, String version) {
        CreditCardAccount creditCardAccount = creditCardAccountRepository.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
        return creditCardAccount;
    }

    //	不缓存
    @Override
    @Transactional
    public CreditCardAccount findByUserIdAndCreditCardNumberAndVersionLock(String userId, String creditCardNumber, String version) {
        CreditCardAccount creditCardAccount = creditCardAccountRepository.findByCreditCardNumberAndUserIdAndVersion(creditCardNumber, userId, version);
        return creditCardAccount;
    }

    @Transactional
    @Override
    public CreditCardAccount save(CreditCardAccount creditCardAccount) {
        creditCardAccount = creditCardAccountRepository.saveAndFlush(creditCardAccount);
        return creditCardAccount;
    }

    //	不缓存
    @Override
    @Transactional
    /**
     * addOrSub: 0:增加  1:减少blance增加freezeBlance 3:增加blance减少freezeBlance 4:减少freezeBlace 5:退还手续费
     */
    public CreditCardAccount updateCreditCardAccountAndVersion(String userId, String creditCardNumber, String taskId, int addOrSub, BigDecimal amount, String description, String version, String billNo) {
        CreditCardAccount creditCardAccount = this.findByUserIdAndCreditCardNumberAndVersionLock(userId, creditCardNumber, version);
        CreditCardAccountHistory creditCardAccountHistory = creditCardAccountHistoryBusiness.findByTaskIdAndAddOrSub(taskId, addOrSub);
        if (creditCardAccountHistory == null) {
            RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, billNo);
            if (repaymentBill != null) {
                if (4 == addOrSub) {
                    RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(taskId);
                    BigDecimal usedCharge = repaymentBill.getUsedCharge().add(repaymentTaskPOJO.getTotalServiceCharge());
                    BigDecimal repaymentedAmount = repaymentBill.getRepaymentedAmount().add(amount);
                    int repaymentedSuccessCount = repaymentBill.getRepaymentedSuccessCount() + 1;
                    repaymentBill.setRepaymentedAmount(repaymentedAmount);
                    repaymentBill.setRepaymentedSuccessCount(repaymentedSuccessCount);
                    repaymentBill.setUsedCharge(usedCharge);
                    if (repaymentBill.getTaskAmount().compareTo(repaymentBill.getRepaymentedAmount()) <= 0) {
                        repaymentBill.setTaskStatus(3);
                    }
                } else if (0 == addOrSub) {
                    repaymentBill.setConsumedAmount(repaymentBill.getConsumedAmount().add(amount));
                }
                repaymentBillBusiness.save(repaymentBill);
            }
        }

        if (creditCardAccountHistory == null && !CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version)) {
            if (creditCardAccount != null) {
                if (0 == addOrSub || 5 == addOrSub) {
                    if (0 == addOrSub) {
                        creditCardAccountHistory = creditCardAccountHistoryBusiness.findByTaskIdAndAddOrSub(taskId, 5);
                        if (creditCardAccountHistory != null) {
                            BigDecimal amount2 = creditCardAccountHistory.getAmount();
                            amount = amount.subtract(amount2);
                        }
                    }
                    creditCardAccount.setBlance(creditCardAccount.getBlance().add(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    creditCardAccount.setLastUpdateTime(new Date());
                    creditCardAccount = this.save(creditCardAccount);
                    creditCardAccountHistoryBusiness.createNewHistory(addOrSub, amount, taskId, creditCardAccount.getId(), creditCardAccount.getBlance(), description);
                } else if (1 == addOrSub) {
                    creditCardAccount.setBlance(creditCardAccount.getBlance().subtract(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    creditCardAccount.setLastUpdateTime(new Date());
                    creditCardAccount.setFreezeBlance(creditCardAccount.getFreezeBlance().add(amount));
                    creditCardAccount = this.save(creditCardAccount);
                    creditCardAccountHistoryBusiness.createNewHistory(addOrSub, amount, taskId, creditCardAccount.getId(), creditCardAccount.getBlance(), description);
                } else if (3 == addOrSub) {
                    if (creditCardAccount.getFreezeBlance().compareTo(amount) >= 0) {
                        if (BigDecimal.ZERO.compareTo(amount) == 0) {
                            CreditCardAccountHistory creditCardAccountHistory2 = creditCardAccountHistoryBusiness.findByTaskIdAndAddOrSub(taskId, 1);
                            if (creditCardAccountHistory2 != null) {
                                amount = creditCardAccountHistory2.getAmount();
                            }
                        }
                        creditCardAccount.setBlance(creditCardAccount.getBlance().add(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
                        creditCardAccount.setLastUpdateTime(new Date());
                        creditCardAccount.setFreezeBlance(creditCardAccount.getFreezeBlance().subtract(amount));
                        CreditCardAccountHistory creditCardAccountHistoryAddOrSub1 = creditCardAccountHistoryBusiness.findByTaskIdAndAddOrSub(taskId, 1);
                        if (creditCardAccountHistoryAddOrSub1 != null) {
                            creditCardAccountHistoryAddOrSub1.setAddOrSub(7);
                            creditCardAccountHistoryBusiness.save(creditCardAccountHistoryAddOrSub1);
                        }
                        creditCardAccount = this.save(creditCardAccount);
                        creditCardAccountHistoryBusiness.createNewHistory(addOrSub + 4, amount, taskId, creditCardAccount.getId(), creditCardAccount.getBlance(), description);
                    }
                } else if (4 == addOrSub) {
                    creditCardAccount.setLastUpdateTime(new Date());
                    creditCardAccountHistory = creditCardAccountHistoryBusiness.findByTaskIdAndAddOrSub(taskId, 5);
                    if (creditCardAccountHistory != null) {
                        BigDecimal amount2 = creditCardAccountHistory.getAmount();
                        creditCardAccount.setBlance(creditCardAccount.getBlance().subtract(amount2));
                    }
                    if (BigDecimal.ZERO.compareTo(creditCardAccount.getFreezeBlance()) == 0) {
                        creditCardAccount.setBlance(creditCardAccount.getBlance().subtract(amount));
                    } else {
                        creditCardAccount.setFreezeBlance(creditCardAccount.getFreezeBlance().subtract(amount));
                    }
                    creditCardAccount = this.save(creditCardAccount);
                    creditCardAccountHistoryBusiness.createNewHistory(addOrSub, amount, taskId, creditCardAccount.getId(), creditCardAccount.getBlance(), description);
                }
            } else {
                throw new RuntimeException("更改账户失败!无账户信息!!!=====userId=" + userId + "=====creditCardNumber=" + creditCardNumber);
            }
        }
        return creditCardAccount;
    }

    //	不缓存
    @Override
    public List<CreditCardAccount> findCreditCardAccountByBlanceNotZeroAndVersion(BigDecimal firstAmount, String version, Pageable pageable) {
        Page<CreditCardAccount> pageAccount = creditCardAccountRepository.findCreditCardAccountByBlanceNotZeroAndVersion(firstAmount, version, pageable);
        List<CreditCardAccount> models = new ArrayList<>(pageAccount.getContent());
        return models;
    }

    @Override
    public List<CreditCardAccount> findByFreezeBlanceGreaterThan0AndVersion(String version) {
        List<CreditCardAccount> models = creditCardAccountRepository.findByFreezeBlanceGreaterThan0AndVersion(version);
        return models;
    }

    @Override
    public CreditCardAccount findByCreditCardNumberAndVersion(String creditCardNumber, String version) {
        CreditCardAccount creditCardAccount = creditCardAccountRepository.findByCreditCardNumberAndVersion(creditCardNumber, version);
        return creditCardAccount;
    }

    @Override
    public List<CreditCardAccount> findByFreezeBlanceAndVersion(BigDecimal amount, String version) {
        List<CreditCardAccount> models = creditCardAccountRepository.findByFreezeBlanceAndVersion(amount, version);
        return models;
    }

    @Override
    public List<CreditCardAccount> findCreditCardAccountByBlanceLessTenAndVersion(String version, Pageable pageable) {
        Page<CreditCardAccount> findCreditCardAccountByBlanceLessTen = creditCardAccountRepository.findCreditCardAccountByBlanceLessTenAndVersion(version, pageable);
        List<CreditCardAccount> models = new ArrayList<>(findCreditCardAccountByBlanceLessTen.getContent());
        return models;
    }

    @Override
    public List<CreditCardAccount> findByBlanceAndVersionAndLastUpdateTimeLessThan(BigDecimal blance, String version,
                                                                                   Date time) {
        return creditCardAccountRepository.findByBlanceAndVersionAndLastUpdateTimeLessThan(blance, version, time);
    }

    @Transactional
    @Override
    public CreditCardAccount createNewAccount(String userId, String creditCardNumber, String version, String phone, Integer billDate, Integer repaymentDay, BigDecimal creditBlance, String brandId) {
        CreditCardAccount creditCardAccount = new CreditCardAccount();
        creditCardAccount.setUserId(userId);
        creditCardAccount.setBrandId(brandId);
        creditCardAccount.setCreditCardNumber(creditCardNumber);
        creditCardAccount.setVersion(version);
        creditCardAccount.setPhone(phone);
        creditCardAccount.setBillDate(billDate);
        creditCardAccount.setRepaymentDate(repaymentDay);
        creditCardAccount.setCreditBlance(creditBlance);
        creditCardAccount.setLastUpdateTime(new Date());
        return this.save(creditCardAccount);
    }

    @Transactional
    @Override
    public CreditCardAccount createNewAccountAndFirstConsume(String userId, String creditCardNumber, String version, String phone, Integer billDate, Integer repaymentDate, BigDecimal creditCardBlance, ConsumeTaskPOJO consumeTaskPOJO, String brandId) {
        CreditCardAccount creditCardAccount = this.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
        if (creditCardAccount == null) {
            this.createNewAccount(userId, creditCardNumber, version, phone, billDate, repaymentDate, creditCardBlance, brandId);
        }
        return this.updateCreditCardAccountAndVersion(userId, creditCardNumber, consumeTaskPOJO.getConsumeTaskId(), 0, consumeTaskPOJO.getAmount(), "首笔消费任务", version, consumeTaskPOJO.getCreateTime());
    }

    @Override
    public List<CreditCardAccount> findByCreditCardNumber(String creditCardNumber) {
        return creditCardAccountRepository.findByCreditCardNumber(creditCardNumber);
    }

    @Override
    public List<CreditCardAccount> findByPhone(String phone) {
        return creditCardAccountRepository.findByPhone(phone);
    }

    @Override
    public CreditCardAccount getChangeInfo(String bankCard) {
        return creditCardAccountRepository.findByCreditCardNumber(bankCard).get(0);
    }

    @Override
    public CreditCardAccount getCreditCardAccount(String version, String creditCardNumber) {
        CreditCardAccount result = creditCardAccountRepository.getCreditCardAccount(version, creditCardNumber);
        em.clear();
        return result;
    }

    @Override
    public CreditCardAccount updateCreditCardAccount(CreditCardAccount cardAccount) {
        CreditCardAccount account = creditCardAccountRepository.save(cardAccount);
        return account;
    }

    public List<CreditCardAccount> findAll(CreditCardAccount cardAccount) {
        return creditCardAccountRepository.findAll(new Specification<CreditCardAccount>() {
            @Override
            public Predicate toPredicate(Root<CreditCardAccount> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (cardAccount != null) {
                    if (cardAccount.getVersion() != null && !"".equals(cardAccount.getVersion())) {
                        predicates.add(cb.equal(root.get("version"), cardAccount.getVersion()));
                    }
                    if (null != cardAccount.getCreditCardNumber() && !"".equals(cardAccount.getCreditCardNumber())) {
                        predicates.add(cb.equal(root.get("creditCardNumber"), cardAccount.getCreditCardNumber()));
                    }
                    if (null != cardAccount.getUserId() && !"".equals(cardAccount.getUserId())) {
                        predicates.add(cb.equal(root.get("userId"), cardAccount.getUserId()));
                    }
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

}
