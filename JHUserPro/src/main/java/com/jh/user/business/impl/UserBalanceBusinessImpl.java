package com.jh.user.business.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Transient;

import com.jh.user.pojo.*;
import com.jh.user.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserBalanceBusiness;
import com.jh.user.business.UserRebateHistoryBusiness;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;

@Service
public class UserBalanceBusinessImpl implements UserBalanceBusiness {
    private static final Logger log = LoggerFactory.getLogger(UserBalanceBusinessImpl.class);


    @Autowired
    private UserAccountRepository accountRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserRebateHistoryBusiness userRebateHistoryBusiness;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserAccountFreezeHistoryRepository userAccountFreezeHistoryRepository;

    @Autowired
    private UserRebateFreezeHistoryRepository userRebateFreezeHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 查询用户的账户信息
     */
    public UserAccount queryUserAccountByUserid(long userid) {
        return accountRepository.findUserAccountByUserid(userid);
    }

    /**
     * 转让余额
     */
    @Transactional
    public int userTransferAndReceiveBalance(Long assignorUserAccount,Long recipientUserAccount,BigDecimal amount) {
        int i=0;
        //转让人
        i= accountRepository.userTransferTheBalance(assignorUserAccount, amount.add(new BigDecimal(1)));
        //接收人
         i=i+accountRepository.userReceiveTheBalance(recipientUserAccount, amount);
         return i;
    }

    /**
     * 转让积分
     */
    @Transactional
    public int userTransferAndReceiveCredit(Long assignorUserAccount,Long recipientUserAccount,BigDecimal coinNew) {
        int i=0;
        //转让人
        i= accountRepository.userTransferTheCredit(assignorUserAccount, coinNew.add(new BigDecimal(1)));
        //接收人
        i=i+accountRepository.userReceiveTheCredit(recipientUserAccount, coinNew);
        return i;
    }

    /**
     * 锁定用户的账户信息
     */
    @Transactional
    public UserAccount lockUserAccount(long userid) {
        return accountRepository.findUserAccountByUseridLock(userid);
    }

    @Override
    public Page<UserBalanceHistory> queryUserBalHistoryByUserid(long userid,
                                                                Pageable pageAble) {
        return userBalanceRepository.findBalHistoryByUserid(userid, pageAble);
    }

    @Transactional
    @Override
    public UserAccount saveUserAccount(UserAccount userAccount) {
        UserAccount result = accountRepository.saveAndFlush(userAccount);
        return result;
    }


    @Transactional
    @Override
    public UserBalanceHistory saveUserBalanceHistory(
            UserBalanceHistory balHistory) {
        UserBalanceHistory result = userBalanceRepository.saveAndFlush(balHistory);
        return result;
    }


    /**
     * addorsub  0 表示冻结   1表示解冻
     */
    @Transactional
    @Override
    public UserAccount freezeUserAccount(Long userId, BigDecimal amount, String addorsub, String ordercode) throws Exception {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userId);
        em.clear();
        userAccount = this.queryUserAccountByUserid(userId);
        /**账户先减去钱， 然后冻结账户增加钱*/
        if (addorsub.equalsIgnoreCase("0")) {

            BigDecimal curBal = userAccount.getBalance().subtract(amount);
            BigDecimal freezeBal = userAccount.getFreezeBalance().add(amount);

            userAccount.setFreezeBalance(freezeBal);
            userAccount.setBalance(curBal);

        } else {

            BigDecimal curBal = userAccount.getBalance().add(amount);
            BigDecimal freezeBal = userAccount.getFreezeBalance().subtract(amount);


            if (freezeBal.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("不能多解冻金额");
            }

            userAccount.setFreezeBalance(freezeBal);
            userAccount.setBalance(curBal);

        }
        this.createUserAccountFreezeHistory(userAccount, amount, addorsub, ordercode);
        userAccount = this.saveUserAccount(userAccount);
        return userAccount;
    }

    private void createUserAccountFreezeHistory(UserAccount userAccount, BigDecimal amount, String addorsub, String ordercode) {
        UserAccountFreezeHistory userAccountHistory = new UserAccountFreezeHistory();
        userAccountHistory.setAddOrSub(addorsub);
        userAccountHistory.setAmount(amount);
        userAccountHistory.setCreateTime(new Date());
        userAccountHistory.setOrderCode(ordercode);
        userAccountHistory.setCurFreezeBal(userAccount.getFreezeBalance());
        userAccountHistory.setUserId(userAccount.getUserId());
        userAccountFreezeHistoryRepository.save(userAccountHistory);

    }

    @Transactional
    @Override
    public UserAccount freezeUserRebateAccount(Long userId, BigDecimal amount, String addorsub, String ordercode) throws Exception {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userId);
        em.clear();
        userAccount = this.queryUserAccountByUserid(userId);
        /**账户先减去钱， 然后冻结账户增加钱*/
        if (addorsub.equalsIgnoreCase("0")) {

            BigDecimal curRebateBal = userAccount.getRebateBalance().subtract(amount);
            BigDecimal freezeRebateBal = userAccount.getFreezerebateBalance().add(amount);

            userAccount.setFreezerebateBalance(freezeRebateBal);
            userAccount.setRebateBalance(curRebateBal);

        } else {

            BigDecimal curBal = userAccount.getRebateBalance().add(amount);
            BigDecimal freezeBal = userAccount.getFreezerebateBalance().subtract(amount);


            if (freezeBal.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("不能多解冻金额");
            }

            userAccount.setFreezerebateBalance(freezeBal);
            userAccount.setRebateBalance(curBal);

        }

        this.createUserRebateAccountFreezeHistory(userAccount, amount, addorsub, ordercode);
        userAccount = this.saveUserAccount(userAccount);
        return userAccount;
    }


    private void createUserRebateAccountFreezeHistory(UserAccount userAccount, BigDecimal amount, String addorsub, String ordercode) {
        UserRebateAccountFreezeHistory userAccountHistory = new UserRebateAccountFreezeHistory();
        userAccountHistory.setAddOrSub(addorsub);
        userAccountHistory.setAmount(amount);
        userAccountHistory.setCreateTime(new Date());
        userAccountHistory.setOrderCode(ordercode);
        userAccountHistory.setCurFreezeBal(userAccount.getFreezerebateBalance());
        userAccountHistory.setUserId(userAccount.getUserId());
        userRebateFreezeHistoryRepository.save(userAccountHistory);
    }


    /**
     * 将用户的分润的钱扣除， 然后将用户的余额增加
     */
    @Transactional
    @Override
    public UserAccount updateUserRebateAccount(Long userId, BigDecimal amount, String ordercode) {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userId);
        em.clear();
        userAccount = this.queryUserAccountByUserid(userId);
        BigDecimal rebatebal = userAccount.getRebateBalance().subtract(amount);
        BigDecimal curBal = userAccount.getBalance().add(amount);

        if (BigDecimal.ZERO.compareTo(rebatebal) > 0) {
            throw new RuntimeException("=====分润提现异常=====" + userAccount);
        }

        userAccount.setRebateBalance(rebatebal);
        userAccount.setBalance(curBal);

        this.createUserBalanceHistory(userAccount, amount, ordercode);
        userRebateHistoryBusiness.createOne(userAccount.getUserId(), "2", amount, userAccount.getRebateBalance(), "0", ordercode);
        userAccount = this.saveUserAccount(userAccount);
        return userAccount;
    }

    private void createUserBalanceHistory(UserAccount userAccount, BigDecimal amount, String ordercode) {
        UserBalanceHistory balHistory = new UserBalanceHistory();
        balHistory.setAddOrSub("0");
        balHistory.setOrderCode(ordercode);
        balHistory.setAmount(amount);
        balHistory.setCreateTime(new Date());
        balHistory.setCurBal(userAccount.getBalance());
        balHistory.setUserId(userAccount.getUserId());
        userBalanceRepository.save(balHistory);
    }


    /***
     * 判定是否存在
     *
     * */
    public UserBalanceHistory findUserBalByUidAndorsubAndOrCode(long userId, String addorsub, String ordercode) {

        return userBalanceRepository.findUserBalByUidAndorsubAndOrCode(userId, ordercode, addorsub);
    }

    @Transactional
    @Override
    public UserAccount updateUserAccount(Long userId, BigDecimal amount, String addorsub, String ordercode) throws Exception {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userId);
        em.clear();
        userAccount = this.queryUserAccountByUserid(userId);
        UserBalanceHistory balHistory = findUserBalByUidAndorsubAndOrCode(userAccount.getUserId(), addorsub, ordercode);
        if (balHistory != null) {
            log.info("重复入账问题出现请检查用户Id=" + userAccount.getUserId() + ";用户订单号=" + ordercode);
            return userAccount;
        }
        BigDecimal curBal = BigDecimal.ZERO;
        if (addorsub.equalsIgnoreCase("0")) {

            curBal = userAccount.getBalance().add(amount);

            userAccount.setBalance(curBal);
        } else {

            if (userAccount.getBalance().compareTo(amount) < 0) {

                if (userAccount.getRebateBalance().compareTo(amount) < 0) {

                    throw new Exception("账户余额和分润余额都不足!");
                } else {

                    curBal = userAccount.getRebateBalance().subtract(amount);

                    userAccount.setRebateBalance(curBal);
                }

            } else {

                curBal = userAccount.getBalance().subtract(amount);

                userAccount.setBalance(curBal);
            }

        }

        if (curBal.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("金额不能为负数 ");
        }

        this.createUserBalanceHistory(userAccount, amount, addorsub, ordercode);
        userAccount = this.saveUserAccount(userAccount);
        return userAccount;
    }

    private void createUserBalanceHistory(UserAccount userAccount, BigDecimal amount, String addorsub, String ordercode) {
        UserBalanceHistory balHistory = new UserBalanceHistory();
        balHistory.setAddOrSub(addorsub);
        balHistory.setOrderCode(ordercode);
        balHistory.setAmount(amount);
        balHistory.setCreateTime(new Date());
        balHistory.setCurBal(userAccount.getBalance());
        balHistory.setUserId(userAccount.getUserId());
        userBalanceRepository.save(balHistory);
    }

    /**
     * 按ID查询用户的日。月。总 收入
     ***/
    @Override
    public Map<String, BigDecimal> findSumUserBalByUserId(long userid) {
        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
        Date dt = new Date();
        SimpleDateFormat matter1 = new SimpleDateFormat("yyyy-MM-dd");
        String dayString = matter1.format(dt) + " 00:00:00";
        Date today = DateUtil.getYYMMHHMMSSDateFromStr(dayString);


        SimpleDateFormat matter2 = new SimpleDateFormat("yyyy-MM");
        String monString = matter2.format(dt) + "-01 00:00:00";
        Date month = DateUtil.getYYMMHHMMSSDateFromStr(monString);


        map.put("daysum", userBalanceRepository.findSumUserBalByUserIdstats0(userid, today).subtract(userBalanceRepository.findSumUserBalByUserIdstats1(userid, today)));
        map.put("monsum", userBalanceRepository.findSumUserBalByUserIdstats0(userid, month).subtract(userBalanceRepository.findSumUserBalByUserIdstats1(userid, month)));
        map.put("allsum", userBalanceRepository.findSumUserBalByUserIdstats0(userid).subtract(userBalanceRepository.findSumUserBalByUserIdstats1(userid)));
        map.put("rebatebalance", accountRepository.findUserAccountByUserid(userid).getBalance());
        return map;
    }


    @Transactional
    @Override
    public UserAccount withdrawFreeAccount(long userid, BigDecimal amount, String ordercode) {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userid);
        em.clear();
        userAccount = this.queryUserAccountByUserid(userid);
        if (userAccount.getBalance().compareTo(amount) < 0) {

            return null;

        } else {

            /**先冻结提现的钱*/
            BigDecimal curBal = userAccount.getBalance().subtract(amount);
            BigDecimal freezeBal = userAccount.getFreezeBalance().add(amount);

            userAccount.setFreezeBalance(freezeBal);
            userAccount.setBalance(curBal);

        }

        userAccount = this.saveUserAccount(userAccount);

        UserAccountFreezeHistory userAccountHistory = new UserAccountFreezeHistory();
        userAccountHistory.setAddOrSub("0");
        userAccountHistory.setAmount(amount);
        userAccountHistory.setCreateTime(new Date());
        userAccountHistory.setOrderCode(ordercode);
        userAccountHistory.setCurFreezeBal(userAccount.getFreezeBalance());
        userAccountHistory.setUserId(userAccount.getUserId());
        userAccountFreezeHistoryRepository.saveAndFlush(userAccountHistory);
        return userAccount;
    }

    @Transactional
    @Override
    public UserAccount rebateFreezeAccount(long userid, BigDecimal amount, String ordercode) {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userid);
        em.clear();
        userAccount = this.queryUserAccountByUserid(userid);
        if (userAccount.getRebateBalance().compareTo(amount) < 0) {

            return null;

        } else {

            /**先冻结提现的钱*/
            BigDecimal curRebateBal = userAccount.getRebateBalance().subtract(amount);
            BigDecimal freezeRebateBal = userAccount.getFreezerebateBalance().add(amount);

            userAccount.setFreezerebateBalance(freezeRebateBal);
            userAccount.setRebateBalance(curRebateBal);

        }

        userAccount = this.saveUserAccount(userAccount);

        UserRebateAccountFreezeHistory userAccountHistory = new UserRebateAccountFreezeHistory();
        userAccountHistory.setAddOrSub("0");
        userAccountHistory.setAmount(amount);
        userAccountHistory.setCreateTime(new Date());
        userAccountHistory.setOrderCode(ordercode);
        userAccountHistory.setCurFreezeBal(userAccount.getFreezeBalance());
        userAccountHistory.setUserId(userAccount.getUserId());
        userRebateFreezeHistoryRepository.saveAndFlush(userAccountHistory);
        return userAccount;
    }

    @Transactional
    @Override
    public UserAccount updateUserRebate(Long userId, BigDecimal rebate, String orderType, String addorsub, String orderCode) {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userId);
        em.clear();
        UserRebateHistory userRebateHistory = userRebateHistoryBusiness.findUserRebateHistory(userId, orderType, rebate.setScale(2, BigDecimal.ROUND_DOWN), addorsub, orderCode);
        if (userRebateHistory != null) {
            throw new RuntimeException("重复发放分润订单=====userId:" + userId + ", rebate:" + rebate + ", orderType:" + orderType + ",addorsub:" + addorsub + ", orderCode:" + orderCode);
        }
        userAccount = this.queryUserAccountByUserid(userAccount.getUserId());
        System.out.println("==================================userAccount:" + userAccount);
        BigDecimal curRebate = BigDecimal.ZERO;
        if (addorsub.equalsIgnoreCase("0")) {
            curRebate = userAccount.getRebateBalance().add(rebate.setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            curRebate = userAccount.getRebateBalance().subtract(rebate);
        }

        userAccount.setRebateBalance(curRebate);
        userRebateHistoryBusiness.createOne(userAccount.getUserId(), addorsub, rebate, curRebate, orderType, orderCode);
        userAccount = this.saveUserAccount(userAccount);
        return userAccount;
    }

    @Transactional
    @Override
    public UserAccount updateUserManageByuserId(long userId, BigDecimal amount, String status) {
        UserAccount userAccount = accountRepository.findUserAccountByUseridLock(userId);
        em.clear();
        userAccount = queryUserAccountByUserid(userAccount.getUserId());
        System.out.println("==================================userAccount:" + userAccount);
        BigDecimal curManage = BigDecimal.ZERO;
        if (status.equals("0")) {
            curManage = userAccount.getManage().add(amount);
        }
        userAccount.setManage(curManage);
        userAccount =saveUserAccount(userAccount);
        return userAccount;

    }

	@Override
	public List<UserAccount> findByManage(BigDecimal bigDecimal) {
		List<UserAccount> userAccounts=accountRepository.findUserAccountBymanage(bigDecimal);
		return userAccounts;
	}

    @Override
    public List<UserAccount> findManageByBrandId(String brandId, Pageable pageable) {
        List<User> users=userRepository.findUserByBrandId(Long.valueOf(brandId));
        Long[] userIds=new Long[users.size()];
        for(int i=0;i<users.size();i++){
        userIds[i]=users.get(i).getId();
        }
        List<UserAccount> userAccounts=accountRepository.findManageByBrandId(userIds,pageable);
        return userAccounts;
    }

    @Override
    public UserAccount findByBrandIdAndPhone(String brandId, String phone) {
        User user=userRepository.findUserByPhoneAndBrandId(phone,Long.valueOf(brandId));
        UserAccount userAccount = accountRepository.findUserAccountByUserid(user.getId());
        return userAccount;
    }

    @Override
    public List<UserAccount> queryUserAccountByRebateBalanceThan0() {
        return accountRepository.queryUserAccountByRebateBalanceThan0();
    }

    @Override
    public List<UserAccount> queryUserAccountByUsers(Long[] userids) {
        return accountRepository. queryUserAccountByUsers(userids);
    }
    @Transactional
    @Override
    public int updataCreditPoints(Long userid,BigDecimal CreditPoints) {
        return accountRepository.updataCreditPoints(userid,CreditPoints);
    }

}
