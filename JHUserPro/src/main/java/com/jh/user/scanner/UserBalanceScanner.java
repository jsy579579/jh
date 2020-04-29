package com.jh.user.scanner;

import com.jh.user.business.AutoRebateWithdrawConfigBusiness;
import com.jh.user.business.UserBalanceBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.config.RestTemplateConfig;
import com.jh.user.pojo.AutoRebateWithdrawConfig;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/*
@author jayden
 */
@Configuration
@EnableScheduling
public class UserBalanceScanner {

    private static final Logger LOG = LoggerFactory.getLogger(UserBalanceScanner.class);

    @Autowired
    private UserBalanceBusiness userBalanceBusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private AutoRebateWithdrawConfigBusiness autoRebateWithdrawConfigBusiness;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${schedule-task.on-off}")
    private String scheduleOnOff;

    /**
     * 分润自动提现到余额
     */
    @Scheduled(cron = "0 0/30 * * * ? ")
    private void autoScanUserAccountBalanceNotZero(){
        if("true".equals(scheduleOnOff)){
            LOG.info("分润自动提现到余额开始了=========");
            this.autoRebateWithdraw();
        }
    }

    private void autoRebateWithdraw() {
        String url="http://facade/v1.0/facade/withdraw/rebate";
        List<AutoRebateWithdrawConfig> autoRebateWithdrawConfig=autoRebateWithdrawConfigBusiness.queryConfig(1);
        LOG.info("当前进行分润自动提现的贴牌========"+autoRebateWithdrawConfig);
        if(autoRebateWithdrawConfig.size()>0){
            List brands=new ArrayList<>();
            for(AutoRebateWithdrawConfig autoRebateWithdrawConfig1:autoRebateWithdrawConfig){
                brands.add(autoRebateWithdrawConfig1.getBrandId());
            }
            List<UserAccount> userAccounts=userBalanceBusiness.queryUserAccountByRebateBalanceThan0();
            LOG.info("自动分润提现数量============"+userAccounts.size());
            if(userAccounts.size()>0){
                for(UserAccount account:userAccounts){
                    String rebateAmount=account.getRebateBalance().toString();
                    User user=userLoginRegisterBusiness.queryUserById(account.getUserId());
                    if(user==null){
                        continue;
                    }
                    String brandId=Long.toString(user.getBrandId());
                    if(brands.contains(brandId)){
                        String phone=user.getPhone();
                        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("brandId", brandId);
                        requestEntity.add("phone", phone);
                        requestEntity.add("amount", rebateAmount);
                        requestEntity.add("order_desc", "分润提现");
                        try {
                            restTemplate.postForObject(url,requestEntity,String.class);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
            }
        }
    }
}
