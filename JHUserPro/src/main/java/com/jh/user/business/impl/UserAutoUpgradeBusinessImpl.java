package com.jh.user.business.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserAutoUpgradeBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRealtionBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.BrandAutoUpdateConfig;
import com.jh.user.pojo.BrandAutoUpgrade;
import com.jh.user.pojo.User;
import com.jh.user.repository.BrandAutoUpgradeConfigRepository;
import com.jh.user.repository.BrandAutoUpgradeRepository;
import com.jh.user.repository.BrandRepository;
import com.jh.user.repository.ChannelRateRepository;
import com.jh.user.repository.ChannelRepository;
import com.jh.user.repository.ThirdLevelDistributionRepository;
import com.jh.user.repository.ThirdLevelRateRepository;
import com.jh.user.repository.UserRepository;
import com.jh.user.service.UserLoginRegisterService;

@Service
public class UserAutoUpgradeBusinessImpl implements UserAutoUpgradeBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(UserAutoUpgradeBusinessImpl.class);

	@Autowired
	private BrandAutoUpgradeRepository brandAutoUpgradeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChannelRateRepository channelRateRepository;

	@Autowired
	private ChannelRepository channelRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private BrandAutoUpgradeConfigRepository brandAutoUpgradeConfigRepository;

	@Autowired
	private UserRealtionBusiness userRealtionBusiness;
	
	@Autowired
	private ThirdLevelRateRepository thirdLevelRateRepository;

	@Autowired
	private ThirdLevelDistributionRepository thirdLevelDistriRepository;

	@Autowired
	private BrandManageBusiness brandMangeBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	private UserLoginRegisterService userLoginRegisterService;

	@Autowired
	private EntityManager em;

	@Transactional
	public List<User> getNeedAutoGradeUser() {

		/*** 首先判断通道 */
		List<BrandAutoUpdateConfig> autoUpgrades = brandAutoUpgradeConfigRepository.findBrandAutoUpgradeConfig();

		for (BrandAutoUpdateConfig config : autoUpgrades) {

			List<User> users = new ArrayList<User>();
			Brand brand = brandRepository.findBrandByid(config.getBrandId());
			if(brand.getAutoUpgrade()!=null) {
				if(brand.getAutoUpgrade().trim().equals("1")) {
					users = userRepository.findNewAutoUpgradeUser(config.getGrade() - 1, config.getGrade(),config.getPeopleNum(), config.getBrandId());
				}else  if(brand.getAutoUpgrade().trim().equals("2")) {
					users = userRepository.findNewAutoUpgradeUserNoGrade( config.getGrade(),config.getPeopleNum(), config.getBrandId());
				}else if(brand.getAutoUpgrade().trim().equals("3"))  {
					users = userRepository.findNewAutoUpgradeUser3(config.getPursuantGrade(), config.getGrade(),config.getPeopleNum(), config.getBrandId());
				}else if(brand.getAutoUpgrade().trim().equals("4")){
					//4新模式 直推和间推VIP人数达标即可自动升级（需先升级为会员才可自动升级）
					users= userRepository.findNewAutoUpgradeUser4(config.getGrade(),config.getPeopleNum(),config.getBrandId());
					//LOG.info("自动升级人员信息==="+users);
				}else {
					continue;
				}
			}else {
				continue;
			}
			

			if (users != null && users.size() != 0) {
				LOG.info("可以升级的人数................." + users.size());
				for (User user : users) {
					if (brand.getManageid() == user.getId()) {
						continue;
					}
					/*** 先清除 */
					if(brand.getAutoUpgrade().trim().equals("1")||brand.getAutoUpgrade().trim().equals("2")||brand.getAutoUpgrade().trim().equals("4")) {
						LOG.info("当前升级。。。。。。。。。。。。。。。。。。。。。" + user.getId());
						userLoginRegisterService.updateGrade(user.getId(), new Long(config.getGrade()).intValue());
					}else if(brand.getAutoUpgrade().trim().equals("3")) {
						if(config.getTeamPeopleNum()>0&&config.getTeamPeopleNum()>config.getPeopleNum()) {
							 int teamNum=userRealtionBusiness.findByPreUserIdAndGradeAndLeve(user.getId(), config.getTeamGrade());
							 if(config.getTeamPeopleNum()<=teamNum) {
								 LOG.info("当前升级。。。。。。。。。。。。。。。。。。。。。" + user.getId());
								 userLoginRegisterService.updateGrade(user.getId(), new Long(config.getGrade()).intValue());
							 }
						}else {
							LOG.info("当前升级。。。。。。。。。。。。。。。。。。。。。" + user.getId());
							 userLoginRegisterService.updateGrade(user.getId(), new Long(config.getGrade()).intValue());
						}
					}
					em.flush();
					em.clear();
				}
			} else {
				LOG.info("可以升级的人数................." + 0);
			}

		}
		return null;
	}

	@Override
	public BrandAutoUpgrade findBrandAutoUpgradeBybrandidAndchannelId(long brandid, long channelId) {
		BrandAutoUpgrade brandAutoUpgrade = new BrandAutoUpgrade();
		brandAutoUpgrade = brandAutoUpgradeRepository.findBrandAutoUpgradeBybrandidAndchannelId(brandid, channelId);
		return brandAutoUpgrade;
	}

	@Transactional
	@Override
	public BrandAutoUpgrade saveBrandAutoUpgrade(BrandAutoUpgrade brandAutoUpgrade) {

		return brandAutoUpgradeRepository.save(brandAutoUpgrade);
	}

	@Override
	public List<BrandAutoUpgrade> findBrandAutoUpgradeByBrandid(long brandid) {

		return brandAutoUpgradeRepository.findBrandAutoUpgradeBybrandid(brandid);
	}

	@Transactional
	@Override
	public BrandAutoUpdateConfig saveBrandAutoUpdateConfig(BrandAutoUpdateConfig brandAutoUpdateConfig) {

		BrandAutoUpdateConfig result = brandAutoUpgradeConfigRepository.save(brandAutoUpdateConfig);
		em.flush();
		return result;
	}

	@Override
	public BrandAutoUpdateConfig getBrandAutoByBrandidAndGrade(long brandid, long grade) {

		return brandAutoUpgradeConfigRepository.findBrandAutoUpgradeConfigBybrandidAndgrade(brandid, grade);
	}

	@Override
	public BrandAutoUpdateConfig getBrandAutoByBrandidAndGradeNostutas(long brandid, long grade) {

		return brandAutoUpgradeConfigRepository.findBrandAutoUpgradeConfigBybrandidAndgradeNostatus(brandid, grade);
	}

	@Override
	public List<BrandAutoUpdateConfig> getBrandAutoConfigByBrandidNostutas(long brandid) {

		return brandAutoUpgradeConfigRepository.findBrandAutoUpgradeConfigBybrandidNostutas(brandid);
	}

	@Override
	public List<BrandAutoUpdateConfig> getBrandAutoConfigByBrandid(long brandid) {

		return brandAutoUpgradeConfigRepository.findBrandAutoUpgradeConfigBybrandid(brandid);
	}

}
