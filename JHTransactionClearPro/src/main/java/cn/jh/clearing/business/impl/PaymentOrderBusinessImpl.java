package cn.jh.clearing.business.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.pojo.BrandProfit;
import cn.jh.clearing.pojo.ChannelBankRate;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.pojo.PaymentOrderNumber;
import cn.jh.clearing.pojo.UserRealtion;
import cn.jh.clearing.pojo.PaymentOrder_;
import cn.jh.clearing.repository.BrandProfitRepository;
import cn.jh.clearing.repository.ChannelBankRateRepository;
import cn.jh.clearing.repository.PaymentOrderRepository;
import cn.jh.clearing.util.DataEncrypt;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Service
public class PaymentOrderBusinessImpl implements PaymentOrderBusiness {
	private static final Logger LOG = LoggerFactory.getLogger(PaymentOrderBusinessImpl.class);
	@Autowired
	private PaymentOrderRepository paymentOrderRepository;

	@Autowired
	private ChannelBankRateRepository channelBankRateRepository;
	
	@Autowired
	private BrandProfitRepository brandProfitRepository;
	
	@Autowired
	Util util;

	@Autowired
	private EntityManager em;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Page<PaymentOrder> queryPaymentOrderByUserid(long userid, String type, Date startTime, Date endTime,
			Pageable pageAble) {
		Page<PaymentOrder> paymentOrders = null;

		if (startTime != null) {
			if (endTime != null) {
				paymentOrders = paymentOrderRepository.findPaymentOrderByUserid(userid, type.split(","), startTime,
						endTime, pageAble);
			} else {
				paymentOrders = paymentOrderRepository.findPaymentOrderByUseridStart(userid, type.split(","), startTime,
						pageAble);
			}
		} else if (endTime != null) {
			paymentOrders = paymentOrderRepository.findPaymentOrderByUseridEnd(userid, type.split(","), endTime,
					pageAble);
		} else {
			paymentOrders = paymentOrderRepository.findPaymentOrderByUserid(userid, type.split(","), pageAble);
		}
		if (type.equalsIgnoreCase(CommonConstants.ORDER_TYPE_WITHDRAW)) {
			if (paymentOrders != null) {
				List<PaymentOrder> PaymentOrderList = paymentOrders.getContent();
				// List<PaymentOrder> PaymentOrderNewList= new
				// ArrayList<PaymentOrder>();
				for (PaymentOrder paymentOrder : PaymentOrderList) {
					/** 根据的渠道标识或去渠道的相关信息 */

					URI uri = util.getServiceUrl("user", "error url request!");
					String url = uri.toString() + "/v1.0/user/brandrate/query";
					/** 根据的渠道标识或去渠道的相关信息 */
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("brand_id", paymentOrder.getBrandid() + "");
					requestEntity.add("channel_id", paymentOrder.getChannelid() + "");
					RestTemplate restTemplate = new RestTemplate();
					String resultObjx = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("resultObjx================" + resultObjx);
					JSONObject jsonObject = JSONObject.fromObject(resultObjx);
					LOG.info("jsonObject================" + jsonObject);
					JSONObject resultObj = jsonObject.getJSONObject("result");
					if (resultObj != null && !resultObj.isNullObject()) {
						String withdrawFee = resultObj.getString("withdrawFee");
						if (withdrawFee != null && !withdrawFee.equals("") && !withdrawFee.equals("null")) {
							paymentOrder.setRealAmount(paymentOrder.getRealAmount());
						} else {
							paymentOrder.setRealAmount(paymentOrder.getRealAmount());
						}
					}
					// PaymentOrderNewList.add(paymentOrder);
				}
			}
		}
		return paymentOrders;

	}

	@Transactional
	@Override
	public PaymentOrder mergePaymentOrder(PaymentOrder order) {
		PaymentOrder result = paymentOrderRepository.saveAndFlush(order);
		return result;
	}

	@Override
	public PaymentOrder queryPaymentOrderBycode(String ordercode) {
		return paymentOrderRepository.findPaymentOrderByCode(ordercode);
	}

	@Override
	public Page<PaymentOrder> queryAllPaymentOrder(String userid, String type, String[] status, Date startTime,
			Date endTime, Pageable pageAble) {
		if (userid != null && !userid.equalsIgnoreCase("")) {
			if (type != null) {
				if (status != null && status.length > 0) {
					return paymentOrderRepository.findAllPaymentOrderByts(Long.parseLong(userid), type.split(","),
							status, pageAble);
				} else if (startTime != null) {
					if (endTime != null) {
						return paymentOrderRepository.findPaymentOrderByUserid(Long.parseLong(userid), type.split(","),
								startTime, endTime, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderByUseridStart(Long.parseLong(userid),
								type.split(","), startTime, pageAble);
					}

				} else if (endTime != null) {
					return paymentOrderRepository.findPaymentOrderByUseridEnd(Long.parseLong(userid), type.split(","),
							endTime, pageAble);
				} else {
					return paymentOrderRepository.findPaymentOrderByUserid(Long.parseLong(userid), type.split(","),
							pageAble);
				}
			} else if (startTime != null) {
				if (endTime != null) {
					return paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), startTime, endTime,
							pageAble);
				} else {
					return paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), startTime, pageAble);
				}
			} else if (endTime != null) {

				return paymentOrderRepository.findAllPaymentOrderByEndTime(Long.parseLong(userid), endTime, pageAble);
			} else {
				return paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), pageAble);
			}
		} else {
			if (type != null) {
				if (status != null && status.length > 0) {
					return paymentOrderRepository.findAllPaymentOrderByts(type.split(","), status, pageAble);
				} else if (startTime != null) {
					if (endTime != null) {
						return paymentOrderRepository.findPaymentOrderByUserid(type.split(","), startTime, endTime,
								pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderByUseridStart(type.split(","), startTime,
								pageAble);
					}

				} else if (endTime != null) {
					return paymentOrderRepository.findPaymentOrderByUseridEnd(type.split(","), endTime, pageAble);
				} else {
					return paymentOrderRepository.findPaymentOrderByUserid(type.split(","), pageAble);
				}

			} else if (startTime != null) {
				if (endTime != null) {
					return paymentOrderRepository.findAllPaymentOrderByNoUserid(startTime, endTime, pageAble);
				} else {
					return paymentOrderRepository.findAllPaymentOrderByNoUserid(startTime, pageAble);
				}
			} else if (endTime != null) {
				return paymentOrderRepository.findAllPaymentOrderByEndTime(endTime, pageAble);
			} else {
				return paymentOrderRepository.findAll(pageAble);
			}

		}
		/*
		 * if(grade==null||grade.equals("")){ }else{ Long[]
		 * uids=this.findUserByGrade(grade,brandid); if(userid != null &&
		 * !userid.equalsIgnoreCase("")){ long useid=Long.parseLong(userid); String
		 * start="n"; for(long uid:uids){ if(uid==useid){ start="y"; } }
		 * if(start.equals("n")){ userid="0"; } if(startTime != null){ if(endTime !=
		 * null){ return
		 * paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), startTime,
		 * endTime, pageAble); }else{ return
		 * paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), startTime,
		 * pageAble); } }else{ return
		 * paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), pageAble);
		 * } }else{ return paymentOrderRepository.findAllPaymentOrder(uids,pageAble); }
		 * 
		 * }
		 */

	}

	@Override
	public Page<PaymentOrder> queryAllPaymentOrderByBrand(String userid, long brandid, String type, String[] status,
			Date startTime, Date endTime, Pageable pageAble) {

		if (userid != null && !userid.equalsIgnoreCase("")) {
			if (type != null) {
				if (status != null && status.length > 0) {
					return paymentOrderRepository.findAllPaymentOrderBytsbrandid(Long.parseLong(userid), type, status,
							brandid, pageAble);
				} else if (startTime != null) {
					if (endTime != null) {
						return paymentOrderRepository.findPaymentOrderByUbrandid(Long.parseLong(userid), type,
								startTime, endTime, brandid, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderByUseridStartbrandid(Long.parseLong(userid), type,
								startTime, brandid, pageAble);
					}

				} else if (endTime != null) {
					return paymentOrderRepository.findPaymentOrderByUseridEndbrandid(Long.parseLong(userid), type,
							endTime, brandid, pageAble);
				} else {
					return paymentOrderRepository.findPaymentOrderByUseridbrandid(Long.parseLong(userid), type, brandid,
							pageAble);
				}
			} else if (startTime != null) {
				if (endTime != null) {
					return paymentOrderRepository.findAllPaymentOrderbrandid(Long.parseLong(userid), startTime, endTime,
							brandid, pageAble);
				} else {
					return paymentOrderRepository.findAllPaymentOrderbrandid(Long.parseLong(userid), startTime, brandid,
							pageAble);
				}
			} else {
				return paymentOrderRepository.findAllPaymentOrderbrandid(Long.parseLong(userid), brandid, pageAble);
			}
		} else {
			if (type != null) {
				if (status != null && status.length > 0) {
					if (startTime != null) {
						if (endTime != null) {
							return paymentOrderRepository.findAllPaymentOrderBytsbrandid(brandid, type, status,
									startTime, endTime, pageAble);
						} else {
							return paymentOrderRepository.findAllPaymentOrderBytsbrandid(brandid, type, status,
									startTime, pageAble);
						}

					} else if (endTime != null) {
						return paymentOrderRepository.findAllPaymentOrderBytsbrandidend(brandid, type, status, endTime,
								pageAble);
					} else {
						return paymentOrderRepository.findAllPaymentOrderBytsbrandid(brandid, type, status, pageAble);
					}

				} else if (startTime != null) {
					if (endTime != null) {
						return paymentOrderRepository.findPaymentOrderBybrandid(brandid, type, startTime, endTime,
								pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderBybrandidStart(brandid, type, startTime,
								pageAble);
					}

				} else if (endTime != null) {
					return paymentOrderRepository.findPaymentOrderBybrandidEnd(brandid, type, endTime, pageAble);
				} else {
					return paymentOrderRepository.findPaymentOrderBybrandid(brandid, type, pageAble);
				}
			} else if (startTime != null) {
				if (endTime != null) {
					return paymentOrderRepository.findAllPaymentOrderbrandid(brandid, startTime, endTime, pageAble);
				} else {
					return paymentOrderRepository.findAllPaymentOrderbrandid(brandid, startTime, pageAble);
				}
			}
			if (endTime != null) {
				return paymentOrderRepository.findAllPaymentOrderBybrandidEndTime(brandid, startTime, pageAble);
			} else {
				return paymentOrderRepository.findAllPaymentOrderbrandid(brandid, pageAble);
			}

		}

		/*
		 * if(grade==null||grade.equals("")){ }else{ Long[]
		 * uids=this.findUserByGrade(grade,brandid); if(userid != null &&
		 * !userid.equalsIgnoreCase("")){ long useid=Long.parseLong(userid); String
		 * start="n"; for(long uid:uids){ if(uid==useid){ start="y"; } }
		 * if(start.equals("n")){ userid="0"; } if(type!=null){ if(status!=null){ return
		 * paymentOrderRepository.findAllPaymentOrderByts(Long.parseLong(userid), type,
		 * status, pageAble); }else if(startTime!=null){ if(endTime!=null){ return
		 * paymentOrderRepository.findPaymentOrderByUserid(Long.parseLong(userid), type,
		 * startTime, endTime, pageAble); }else{ return
		 * paymentOrderRepository.findPaymentOrderByUseridStart(Long.parseLong(userid),
		 * type, startTime, pageAble); }
		 * 
		 * }else if(endTime!=null){ return
		 * paymentOrderRepository.findPaymentOrderByUseridEnd(Long.parseLong(userid),
		 * type, endTime, pageAble); }else{ return
		 * paymentOrderRepository.findPaymentOrderByUserid(Long.parseLong(userid), type,
		 * pageAble); } }else if(startTime != null){ if(endTime != null){ return
		 * paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), startTime,
		 * endTime, pageAble); }else{ return
		 * paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), startTime,
		 * pageAble); } }else{ return
		 * paymentOrderRepository.findAllPaymentOrder(Long.parseLong(userid), pageAble);
		 * } }else{ return paymentOrderRepository.findAllPaymentOrder(uids,pageAble); }
		 * 
		 * }
		 */

	}

	public Long[] findUserByGrade(String grade, long brandid) {
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/grade";
		/** 根据的用户手机号码查询用户的基本信息 */
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("grade", grade);
		requestEntity.add("brand_id", brandid + "");
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		Long[] str2 = new Long[] { 0l };
		JSONObject object = jsonObject.getJSONObject("result");
		String ids = object.getString("uids");
		if (ids.length() > 0) {
			ids = ids.substring(0, ids.length() - 1);
			String[] str1 = ids.split(",");
			str2 = new Long[str1.length];
			for (int j = 0; j < str1.length; j++) {
				str2[j] = Long.valueOf(str1[j]);
			}
		}
		return str2;
	}

	public Long[] findUserByBrandId(long brandid) {
		Long[] str2 = paymentOrderRepository.findAllPaymentOrderbyBrandid(brandid);
		return str2;
	}

	@Override
	public PaymentOrder queryPaymentOrderBycodeAndStatus(String ordercode, String status) {
		return paymentOrderRepository.findPaymentOrderByCodeAndStatus(ordercode, status);
	}

	@Override
	public PaymentOrder queryPaymentOderByOutOrdercode(String outordercode) {
		return paymentOrderRepository.findPaymentOrderByOutCode(outordercode);
	}

	@Transactional
	@Override
	public void updateThirdcodeByOrdercode(String thirdcode, String ordercode) {
		paymentOrderRepository.updatePaymentThirdcodeByOrdercode(thirdcode, ordercode);
	}

	@Override
	public PaymentOrder queryPaymentOrderByThirdcode(String thirdcode) {
		return paymentOrderRepository.findPaymentOrderByThirdCode(thirdcode);
	}

	@Override
	public List<PaymentOrder> queryWeekBrandWithdrawRebate(Date startDate, Date endDate) {

		List<PaymentOrder> paymentOrders = paymentOrderRepository.findBrandWithdrawRebate(startDate, endDate);
		return paymentOrders;

	}

	@Override
	public List<PaymentOrder> queryWeekBrandWithdrawClearRebate(Date startDate, Date endDate) {

		List<PaymentOrder> paymentOrders = paymentOrderRepository.findBrandWithdrawclearRebate(startDate, endDate);
		return paymentOrders;

	}
	
	@Override
	public List<PaymentOrder> queryWeekBrandWithdrawRebate(Date startDate, Date endDate,long brandid) {
		
		List<PaymentOrder> paymentOrders = paymentOrderRepository.findBrandWithdrawRebate(startDate, endDate,brandid);
		return paymentOrders;
		
	}
	
	@Override
	public List<PaymentOrder> queryWeekBrandWithdrawClearRebate(Date startDate, Date endDate,long brandid) {
		
		List<PaymentOrder> paymentOrders = paymentOrderRepository.findBrandWithdrawclearRebate(startDate, endDate,brandid);
		return paymentOrders;
		
	}

	@Override
	public List<PaymentOrder> queryWaitClearingOrders() {

		return paymentOrderRepository.findWaitCleaingOrder();
	}

	@Override
	public BigDecimal findsumPaymentOrder(long userid, String[] type, String[] status, String autoClearing) {

		return paymentOrderRepository.findsumPaymentOrder(userid, type, status, autoClearing);
	}

	@Override
	public List<PaymentOrder> findsumPaymentOrderByDescCode(long userid, String[] type, String[] status,
			String desccode, Date startTimeDate, Date endTimeDate) {

		return paymentOrderRepository.findsumPaymentOrderByDescCode(userid, type, status, desccode, startTimeDate,
				endTimeDate);
	}

	@Override
	public BigDecimal findsumPaymentOrder(long userid, String[] type, String[] status, String autoClearing,
			Date startTimeDate, Date endTimeDate) {
		return paymentOrderRepository.findsumPaymentOrder(userid, type, status, autoClearing, startTimeDate,
				endTimeDate);
	}
	
	@Override
	public BigDecimal findsumPaymentOrderAmount(long userid, String[] type, String[] status, String autoClearing,
			Date startTimeDate, Date endTimeDate) {
		
		return paymentOrderRepository.findsumPaymentOrderAmount(userid, type, status, autoClearing, startTimeDate,
				endTimeDate);
	}

	@Override
	public int findsumPaymentOrderCount(long userid, String[] type, String[] status, String autoClearing,
			Date startTimeDate, Date endTimeDate) {

		return paymentOrderRepository.findsumPaymentOrdercount(userid, type, status, autoClearing, startTimeDate,
				endTimeDate);
	}

	@Override
	public BigDecimal findsumPaymentOrderBrand(long brand, String[] type, String[] status, String[] autoClearing,
			Date startTimeDate, Date endTimeDate) {
		return paymentOrderRepository.findsumPaymentOrderBrand(brand, type, status, autoClearing, startTimeDate,
				endTimeDate);

	}

	@Override
	public int findsumPaymentOrderBrandCount(long brand, String[] type, String[] status, String[] autoClearing,
			Date startTimeDate, Date endTimeDate) {

		return paymentOrderRepository.findsumPaymentOrderBrandcount(brand, type, status, autoClearing, startTimeDate,
				endTimeDate);
	}

	@Override
	public BigDecimal findsumPaymentOrderPlatform(String[] type, String[] status, String[] autoClearing,
			Date startTimeDate, Date endTimeDate) {

		return paymentOrderRepository.findsumPaymentOrderPlatform(type, status, autoClearing, startTimeDate,
				endTimeDate);
	}

	@Override
	public int findsumPaymentOrderPlatformCount(String[] type, String[] status, String autoClearing[],
			Date startTimeDate, Date endTimeDate) {

		return paymentOrderRepository.findsumPaymentOrderPlatformcount(type, status, autoClearing, startTimeDate,
				endTimeDate);
	}

	@Transactional
	@Override
	public void updateAutoClearingByOrdercode(String ordercode, String autoclearing) {
		paymentOrderRepository.updateAutoClearingByOrdercode(autoclearing, ordercode);
	}

	@Override
	public Page<PaymentOrder> queryPaymentOrder(String userid, String brandid, String ordercode, String[] status,
			String thirdOrdercode, String[] channelTag, Pageable pageAble) {

		if (userid != null && !userid.equals("")) {
			if (brandid != null && !brandid.equals("")) {
				if (ordercode != null && !ordercode.equals("")) {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid1(Long.parseLong(brandid),
								Long.parseLong(userid), ordercode, thirdOrdercode, status, channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid4(Long.parseLong(brandid),
								Long.parseLong(userid), ordercode, status, channelTag, pageAble);
					}
				} else {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid3(Long.parseLong(brandid),
								Long.parseLong(userid), thirdOrdercode, status, channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderuserid6(Long.parseLong(brandid),
								Long.parseLong(userid), status, channelTag, pageAble);
					}
				}
			} else {
				if (ordercode != null && !ordercode.equals("")) {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderuserid5(Long.parseLong(userid), ordercode,
								thirdOrdercode, status, channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid9(Long.parseLong(userid), ordercode,
								status, channelTag, pageAble);
					}
				} else {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid10(Long.parseLong(userid), thirdOrdercode,
								status, channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid13(Long.parseLong(userid), status,
								channelTag, pageAble);
					}
				}
			}
		} else {
			if (brandid != null && !brandid.equals("")) {
				if (ordercode != null && !ordercode.equals("")) {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid2(Long.parseLong(brandid), ordercode,
								thirdOrdercode, status, channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid7(Long.parseLong(brandid), ordercode,
								status, channelTag, pageAble);
					}
				} else {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid8(Long.parseLong(brandid), thirdOrdercode,
								status, channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid12(Long.parseLong(brandid), status,
								channelTag, pageAble);
					}
				}
			} else {
				if (ordercode != null && !ordercode.equals("")) {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid11(ordercode, thirdOrdercode, status,
								channelTag, pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid14(ordercode, status, channelTag,
								pageAble);
					}
				} else {
					if (thirdOrdercode != null && !thirdOrdercode.equals("")) {
						return paymentOrderRepository.findPaymentOrderbrandid15(thirdOrdercode, status, channelTag,
								pageAble);
					} else {
						return paymentOrderRepository.findPaymentOrderbrandid16(status, channelTag, pageAble);
					}
				}
			}
		}

	}

	// 根据多个条件查询订单信息
	@Override
	public Map queryPaymentOrderAll(String startTime, String endTime, String phone, String ordercode, String ordertype,
			String orderstatus, long brandid, String bankCard, String userName, String bankName, String debitBankName, Pageable pageAble) {
		Map object = new HashMap();
		// 将sql语句变成字符串进行拼接
		StringBuffer sql = new StringBuffer("from t_payment_order where 1=1");

		StringBuffer sql1 = new StringBuffer(" and order_type='2'");
		StringBuffer sql2 = new StringBuffer(" and order_type='1'");
		StringBuffer sql3 = new StringBuffer(" and order_type='0'");
		StringBuffer sql4 = new StringBuffer(" and order_status='0'");
		StringBuffer sql5 = new StringBuffer(" and order_status='1'");

		// 如果条件不为空往后面拼接
		if (startTime != null && !startTime.equals("")) {
			sql.append(" and create_time>='" + startTime + "'");
		}

		if (endTime != null && !endTime.equals("")) {
			sql.append(" and create_time<='" + endTime + "'");
		}

		if (phone != null && !phone.equals("")) {
			sql.append(" and phone='" + phone + "'");
		}

		if (ordercode != null && !ordercode.equals("")) {
			sql.append(" and order_code='" + ordercode + "'");
		}

		if (ordertype != null && !ordertype.equals("")) {
			sql.append(" and order_type='" + ordertype + "'");
		}

		if (orderstatus != null && !orderstatus.equals("")) {
			sql.append(" and order_status='" + orderstatus + "'");
		}

		if (brandid != -1) {
			sql.append(" and brand_id='" + brandid + "'");
		}
		
		if (bankCard !=null && !bankCard.equals("")) {
			sql.append(" and bank_card='" + bankCard + "'");
		}
		
		if(userName != null && !userName.equals("")) {
			sql.append(" and user_name like'%" + userName + "%'");
		}
		
		if(bankName != null && !bankName.equals("")) {
			sql.append(" and bank_name like'%" + bankName + "%'");
		}
		
		if(debitBankName != null && !debitBankName.equals("")) {
			sql.append(" and debit_bank_name like'%" + debitBankName + "%'");
		}
		
		// 定义一个新的字符串用来拼接之前的字符串
		StringBuffer sqlCount = new StringBuffer("select count(*) as count ").append(sql);

		StringBuffer withdrawingamount = null; // 待提现
		StringBuffer withdrawedamount = null; // 已提现
		StringBuffer rechargingamount = null; // 待充值
		StringBuffer rechargedamount = null; // 已充值
		StringBuffer buyamount = null; // 成功的总购买
		StringBuffer withdrawamount = null; // 总提现
		StringBuffer rechargamount = null; // 总充值
		StringBuffer buys = null; // 总购买
		StringBuffer ingamount = null; // 待完成
		StringBuffer edamount = null; // 已完成

		if ((ordertype == null && orderstatus.equals(""))) {
			withdrawingamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql1)
					.append(sql4);
			rechargingamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql3)
					.append(sql4);
			rechargedamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql3).append(sql5);
			withdrawedamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql1)
					.append(sql5);
			buyamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql2).append(sql5);
			withdrawamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql1);
			rechargamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql3);
			buys = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql2);
		}

		if (ordertype != null || (ordertype != null && !orderstatus.equals(""))) {
			if (ordertype.equals("2") && orderstatus.equals("0")) {
				// 待提现金额
				withdrawingamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql1)
						.append(sql4);
			}

			if (ordertype.equals("2") && orderstatus.equals("1")) {
				// 已提现金额
				withdrawedamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql1)
						.append(sql5);
			}

			if (ordertype.equals("2")) {
				// 提现金额
				withdrawamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql1);
			}
			if (orderstatus.equals("0") && ordertype == null) {
				// 待完成金额
				ingamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql4);
			}

			if (orderstatus.equals("1") && ordertype == null) {
				// 完成金额
				edamount = new StringBuffer("select sum(real_amount) as sum ").append(sql5);
			}

			if (ordertype.equals("1") && orderstatus.equals("1")) {
				// 总购买成功的金额
				buyamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql2).append(sql5);
			}

			if (ordertype.equals("0") && orderstatus.equals("0")) {
				// 待充值金额
				rechargingamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql3)
						.append(sql4);
			}

			if (ordertype.equals("0")) {
				// 充值总金额
				rechargamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql3);
			}

			if (ordertype.equals("0") && orderstatus.equals("1")) {
				// 已充值总金额
				rechargedamount = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql3)
						.append(sql5);
			}

			if (ordertype.equals("1")) {
				// 总购买金额
				buys = new StringBuffer("select sum(real_amount) as sum ").append(sql).append(sql2);
			}
		}

		// 将字符串变成sql语句查询到的结果转成int类型
		int count = Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString());

		Double withdrawing = 0.00; // 待提现
		Double recharging = 0.00; // 待充值
		Double recharged = 0.00; // 已充值
		Double withdrawed = 0.00; // 已提现
		Double buy = 0.00; // 成功总购买
		Double withdraw = 0.00; // 总提现
		Double recharge = 0.00; // 总充值
		Double allbuy = 0.00; // 总购买

		String str = new String();
		String str1 = new String();
		String str2 = new String();
		String str3 = new String();
		String str4 = new String();
		String str5 = new String();
		String str6 = new String();
		String str7 = new String();

		try {
			if ((ordertype == null && orderstatus.equals(""))) {

				// 定义一个map集合存放查询的数据

				Map<String, Object> queryForwithdrawing = jdbcTemplate.queryForMap(withdrawingamount.toString());
				Map<String, Object> queryForrecharging = jdbcTemplate.queryForMap(rechargingamount.toString());
				Map<String, Object> queryForrecharged = jdbcTemplate.queryForMap(rechargedamount.toString());
				Map<String, Object> queryForwithdrawed = jdbcTemplate.queryForMap(withdrawedamount.toString());
				Map<String, Object> queryForbuy = jdbcTemplate.queryForMap(buyamount.toString());
				Map<String, Object> queryForwithdraw = jdbcTemplate.queryForMap(withdrawamount.toString());
				Map<String, Object> queryForrecharge = jdbcTemplate.queryForMap(rechargamount.toString());
				Map<String, Object> queryForbuys = jdbcTemplate.queryForMap(buys.toString());

				if (queryForwithdrawing.get("sum") != null) {
					str = queryForwithdrawing.get("sum").toString();
				} else {
					str = "0.00";
				}

				if (queryForrecharging.get("sum") != null) {
					str1 = queryForrecharging.get("sum").toString();
				} else {
					str1 = "0.00";
				}

				if (queryForrecharged.get("sum") != null) {
					str2 = queryForrecharged.get("sum").toString();
				} else {
					str2 = "0.00";
				}

				if (queryForwithdrawed.get("sum") != null) {
					str3 = queryForwithdrawed.get("sum").toString();
				} else {
					str3 = "0.00";
				}

				if (queryForbuy.get("sum") != null) {
					str4 = queryForbuy.get("sum").toString();
				} else {
					str4 = "0.00";
				}

				if (queryForwithdraw.get("sum") != null) {
					str5 = queryForwithdraw.get("sum").toString();
				} else {
					str5 = "0.00";
				}

				if (queryForrecharge.get("sum") != null) {
					str6 = queryForrecharge.get("sum").toString();
				} else {
					str6 = "0.00";
				}

				if (queryForbuys.get("sum") != null) {
					str7 = queryForbuys.get("sum").toString();
				} else {
					str7 = "0.00";
				}

				withdrawing = Double.valueOf(str);
				recharging = Double.valueOf(str1);
				recharged = Double.valueOf(str2);
				withdrawed = Double.valueOf(str3);
				buy = Double.valueOf(str4);
				withdraw = Double.valueOf(str5);
				recharge = Double.valueOf(str6);
				allbuy = Double.valueOf(str7);
			} else if (ordertype != null && !"".equals(ordertype)) {
				LOG.info("=======orderType=======" + ordertype);
				if ("2".equals(ordertype)) {
					withdraw = Double
							.valueOf(jdbcTemplate.queryForMap(withdrawamount.toString()).get("sum").toString());
				}
				if ("1".equals(ordertype)) {
					allbuy = Double.valueOf(jdbcTemplate.queryForMap(buys.toString()).get("sum").toString());
				}
				if ("0".equals(ordertype)) {
					recharge = Double.valueOf(jdbcTemplate.queryForMap(rechargamount.toString()).get("sum").toString());
				}

				if ("2".equals(ordertype) && "0".equals(orderstatus)) {
					withdrawing = Double
							.valueOf(jdbcTemplate.queryForMap(withdrawingamount.toString()).get("sum").toString());
					recharging = 0.00;
					recharged = 0.00;
					withdrawed = 0.00;
					buy = 0.00;
				}
				if ("0".equals(ordertype) && "0".equals(orderstatus)) {
					withdrawing = 0.00;
					recharging = Double
							.valueOf(jdbcTemplate.queryForMap(rechargingamount.toString()).get("sum").toString());
					recharged = 0.00;
					withdrawed = 0.00;
					buy = 0.00;

				}
				if ("0".equals(ordertype) && "1".equals(orderstatus)) {
					withdrawing = 0.00;
					recharging = 0.00;
					recharged = Double
							.valueOf(jdbcTemplate.queryForMap(rechargedamount.toString()).get("sum").toString());
					withdrawed = 0.00;
					buy = 0.00;

				}
				if ("2".equals(ordertype) && "1".equals(orderstatus)) {
					withdrawing = 0.00;
					recharging = 0.00;
					recharged = 0.00;
					withdrawed = Double
							.valueOf(jdbcTemplate.queryForMap(withdrawedamount.toString()).get("sum").toString());
					buy = 0.00;
				}
				if ("1".equals(ordertype) && "1".equals(orderstatus)) {
					withdrawing = 0.00;
					recharging = 0.00;
					recharged = 0.00;
					withdrawed = 0.00;
					buy = Double.valueOf(jdbcTemplate.queryForMap(buyamount.toString()).get("sum").toString());

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			object.put("message", "该用户没有订单信息");
		}

		int pageNum = pageAble.getPageSize();
		int currentPage = pageAble.getPageNumber();

		// 定义一个新的字符串用来表示分页的sql语句
		StringBuffer sqlList = new StringBuffer("select * ").append(sql)
				.append(" order by create_time desc limit " + (currentPage) * pageNum + "," + pageNum);

		List<PaymentOrder> list = jdbcTemplate.query(sqlList.toString(), new RowMapper<PaymentOrder>() {

			@Override
			public PaymentOrder mapRow(ResultSet rs, int rowNum) throws SQLException {

				PaymentOrder po = new PaymentOrder();
				po.setCreateTime(DateUtil.getYYMMHHMMSSDateFromStr(rs.getString("create_time")));
				po.setUserName(rs.getString("user_name"));
				po.setPhone(rs.getString("phone"));
				po.setOrdercode(rs.getString("order_code"));
				po.setType(rs.getString("order_type"));
				po.setStatus(rs.getString("order_status"));
				po.setBrandid(rs.getLong("brand_id"));
				po.setAmount(rs.getBigDecimal("amount"));
				po.setAutoClearing(rs.getString("auto_clearing"));
				po.setBankcard(rs.getString("bank_card"));
				po.setBrandname(rs.getString("brand_name"));
				po.setBankName(rs.getString("bank_name"));
				po.setCarNo(rs.getString("car_no"));
				po.setChannelid(rs.getLong("channel_id"));
				po.setChannelname(rs.getString("channel_name"));
				po.setChannelTag(rs.getString("channel_tag"));
				po.setChannelType(rs.getString("channel_type"));
				po.setDesc(rs.getString("order_desc"));
				po.setDescCode(rs.getString("order_desc_code"));
				po.setExtraFee(rs.getBigDecimal("extra_fee"));
				po.setOpenid(rs.getString("openid"));
				po.setOutMerOrdercode(rs.getString("out_mer_order_code"));
				po.setOutNotifyUrl(rs.getString("out_notify_url"));
				po.setOutReturnUrl(rs.getString("out_return_url"));
				po.setPhoneBill(rs.getString("phone_bill"));
				po.setRate(rs.getBigDecimal("rate"));
				po.setRealAmount(rs.getBigDecimal("real_amount"));
				po.setRemark(rs.getString("remark"));
				po.setThirdlevelid(rs.getString("third_level_id"));
				po.setThirdOrdercode(rs.getString("third_order_code"));
				po.setUpdateTime(DateUtil.getYYMMHHMMSSDateFromStr(rs.getString("update_time")));
				po.setUserid(rs.getLong("user_id"));
				po.setDebitBankName(rs.getString("debit_bank_name"));
				po.setDebitBankCard(rs.getString("debit_bank_card"));

				return po;
			}

		});

		//对查询数据进行脱敏处理
		//List list1=DataEncrypt.paymentOrderdataEncrypt(list);
		object.put("pageNum", pageNum); // 每页显示条数
		object.put("currentPage", currentPage); // 当前页
		object.put("totalElements", count); // 总条数
		if (pageNum != 0) {
			object.put("totalPages", count / pageAble.getPageSize()); // 总页数
		}
		object.put("content", list);
		object.put("withdrawing", withdrawing); // 待提现
		object.put("recharging", recharging); // 待充值
		object.put("recharged", recharged); // 已充值
		object.put("withdrawed", withdrawed); // 已提现
		object.put("buy", buy); // 成功的总购买
		object.put("withdraw", withdraw); // 总提现
		object.put("recharge", recharge); // 总充值
		object.put("allbuy", allbuy); // 总购买
		return object;

	}

	// 根据手机号和brandid查询订单号
	@Override
	public Map queryOrdercodeByPhone(String phone, long brandid) {

		StringBuffer sql = new StringBuffer("from t_payment_order tpo where 1=1");

		if (phone != null && !phone.equals("")) {
			sql.append(" and phone='" + phone + "'");
		}

		if (brandid != -1) {
			sql.append(" and brand_id='" + brandid + "'");
		}

		StringBuffer sqllist = new StringBuffer("select * ").append(sql);
		List<PaymentOrder> list = jdbcTemplate.query(sqllist.toString(), new RowMapper<PaymentOrder>() {

			@Override
			public PaymentOrder mapRow(ResultSet rs, int rowNum) throws SQLException {

				PaymentOrder po = new PaymentOrder();
				po.setOrdercode(rs.getString("order_code"));

				return po;
			}

		});

		Map object = new HashMap();

		object.put("content", list);
		object.put("size", list.size());

		return object;
	}

	@Override
	public PaymentOrder updatePaymentOrder(String orderCode, String orderNo) {

		PaymentOrder result = null;

		PaymentOrder oldCode = paymentOrderRepository.findByOrdercode(orderCode);
		if (oldCode != null) {
			oldCode.setOrdercode(orderNo);
			result = paymentOrderRepository.save(oldCode);
		} else {
			return null;
		}
		return result;
	}

	// 依据用户phone获取用户该时间段内所有order
	@Override
	public List<PaymentOrder> findOrderByphoneAndbrandid(String phone, long brandid, Date startTimeDate,
			Date endTimeDate, Pageable pageable) {
		return paymentOrderRepository.findOrderByphoneAndbrandid(phone, brandid, startTimeDate, endTimeDate, pageable);
	}

	// 根据设置金额,筛选出符合条件的userId
	@Override
	public List<Long> queryUserIdsByAmount(Long brandId, BigDecimal limitAmount, Long autoRebateConfigId) {
		return paymentOrderRepository.queryUserIdsByAmount(brandId, limitAmount, autoRebateConfigId);
	}

	// 调用此方法获取待结算订单
	@Override
	public List<PaymentOrder> findOrderByUseridAndStatus(long userid, String[] status) {
		return paymentOrderRepository.findOrderByUseridAndStatus(userid, status);
	}

	@Override
	public List<Object[]> findSumByUserIds(long[] userIds, String type, String status, String autoClearing) {
		return paymentOrderRepository.findSumByUserIds(userIds, type, status, autoClearing);
	}

	@Transactional
	@Override
	public void addErrorByOrderCode(String ordercode, String remark) {
		paymentOrderRepository.updateOrderCodeMsg(ordercode, remark);
	}

	@Override
	public List<PaymentOrderNumber> findOrderSuccessRate(long brandId, Date StartTimeDate, Date endTimeDate) {

		String sqlCommonalityList = "select count(po.real_amount) as number ,sum(po.amount) as amount,  po.channel_tag ,po.channel_id   from t_payment_order po where 1=1";

		if (brandId != -1) {
			sqlCommonalityList += " and  po.brand_id=" + brandId;
		}

		if (StartTimeDate != null) {
			sqlCommonalityList += " and  po.create_time >= '" + DateUtil.getDateFromStr(StartTimeDate) + "' ";
		}

		if (endTimeDate != null) {
			sqlCommonalityList += " and  po.create_time <= '" + DateUtil.getDateFromStr(endTimeDate) + "' ";
		}

		String endsql = "  group by po.channel_id ";

		String sqlFailureList = sqlCommonalityList + " and po.order_status  not in (1,2,4) " + endsql;

		String sqlSuccessList = sqlCommonalityList + " and po.order_status   in (1,4) " + endsql;

		String sqlAllList = sqlCommonalityList + " and po.order_status != 2 " + endsql;

		List<PaymentOrderNumber> failureList = jdbcTemplate.query(sqlFailureList.toString(),
				new RowMapper<PaymentOrderNumber>() {
					@Override
					public PaymentOrderNumber mapRow(ResultSet rs, int rowNum) throws SQLException {
						PaymentOrderNumber pon = new PaymentOrderNumber();
						pon.setChannelId(rs.getLong("channel_id"));
						URI uri = util.getServiceUrl("user", "error url request!");
						String url = uri.toString() + "/v1.0/user/channel/find/by/channelid";
						/** 根据的用户手机号码查询用户的基本信息 */
						MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("channelId", rs.getString("channel_id"));
						RestTemplate restTemplate = new RestTemplate();
						String result = restTemplate.postForObject(url, requestEntity, String.class);
						LOG.info("RESULT================" + result);
						JSONObject jsonObject = JSONObject.fromObject(result);
						JSONObject object = jsonObject.getJSONObject("result");
						if (jsonObject.getString("resp_code").equals("000000")) {
							pon.setChannelName(object.getString("name"));
						}
						pon.setChanneltag(rs.getString("channel_tag"));
						pon.setFailureNumber(rs.getLong("number"));
						pon.setFailureMoney(rs.getLong("amount"));
						return pon;
					}
				});

		List<PaymentOrderNumber> successList = jdbcTemplate.query(sqlSuccessList.toString(),
				new RowMapper<PaymentOrderNumber>() {
					@Override
					public PaymentOrderNumber mapRow(ResultSet rs, int rowNum) throws SQLException {
						PaymentOrderNumber pon = new PaymentOrderNumber();
						pon.setChannelId(rs.getLong("channel_id"));
						URI uri = util.getServiceUrl("user", "error url request!");
						String url = uri.toString() + "/v1.0/user/channel/find/by/channelid";
						/** 根据的用户手机号码查询用户的基本信息 */
						MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("channelId", rs.getString("channel_id"));
						RestTemplate restTemplate = new RestTemplate();
						String result = restTemplate.postForObject(url, requestEntity, String.class);
						LOG.info("RESULT================" + result);
						JSONObject jsonObject = JSONObject.fromObject(result);
						JSONObject object = jsonObject.getJSONObject("result");
						if (jsonObject.getString("resp_code").equals("000000")) {
							pon.setChannelName(object.getString("name"));
						}
						pon.setChanneltag(rs.getString("channel_tag"));
						pon.setSuccessNumber(rs.getLong("number"));
						pon.setSuccessMoney(rs.getLong("amount"));
						return pon;
					}
				});
		List<PaymentOrderNumber> allList = jdbcTemplate.query(sqlAllList.toString(),
				new RowMapper<PaymentOrderNumber>() {
					@Override
					public PaymentOrderNumber mapRow(ResultSet rs, int rowNum) throws SQLException {
						PaymentOrderNumber pon = new PaymentOrderNumber();
						pon.setChannelId(rs.getLong("channel_id"));
						URI uri = util.getServiceUrl("user", "error url request!");
						String url = uri.toString() + "/v1.0/user/channel/find/by/channelid";
						/** 根据的用户手机号码查询用户的基本信息 */
						MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("channelId", rs.getString("channel_id"));
						RestTemplate restTemplate = new RestTemplate();
						String result = restTemplate.postForObject(url, requestEntity, String.class);
						LOG.info("RESULT================" + result);
						JSONObject jsonObject = JSONObject.fromObject(result);
						JSONObject object = jsonObject.getJSONObject("result");
						if (jsonObject.getString("resp_code").equals("000000")) {
							pon.setChannelName(object.getString("name"));
						}
						pon.setChanneltag(rs.getString("channel_tag"));
						pon.setAllNumber(rs.getLong("number"));
						pon.setAllMoney(rs.getLong("amount"));
						return pon;
					}
				});

		for (PaymentOrderNumber pon : failureList) {
			for (int i = 0; i < allList.size(); i++) {
				PaymentOrderNumber pon1 = allList.get(i);
				if (pon.getChannelId() == pon1.getChannelId()) {
					pon1.setFailureNumber(pon.getFailureNumber());
					if (pon1.getChannelName() == null || pon1.getChannelName().trim().length() == 0) {
						pon1.setChannelName(pon.getChannelName());
					}
					pon1.setFailureMoney(pon.getFailureMoney());
					allList.set(i, pon1);
				}
			}
		}

		for (PaymentOrderNumber pon : successList) {
			for (int i = 0; i < allList.size(); i++) {
				PaymentOrderNumber pon1 = allList.get(i);
				if (pon.getChannelId() == pon1.getChannelId()) {
					pon1.setSuccessNumber(pon.getSuccessNumber());
					if (pon1.getChannelName() == null || pon1.getChannelName().trim().length() == 0) {
						pon1.setChannelName(pon.getChannelName());
					}
					pon1.setSuccessMoney(pon.getSuccessMoney());
					allList.set(i, pon1);
				}
			}
		}

		return allList;
	}

	@Override
	public List<PaymentOrder> findPaymentOrderByTimeAndPhone(String phone, Date strDate, Date endDate) {
		// TODO Auto-generated method stub
		return paymentOrderRepository.findPaymentOrderByTimeAndPhone(phone, strDate, endDate);
	}

	@Override
	public PaymentOrder findByThirdOrdercode(String order_code) {
		return paymentOrderRepository.findByThirdOrdercode(order_code);
	}

	@Transactional
	@Override
	public void deletePaymentOrderByOrderCode(String ordercode) {
		paymentOrderRepository.deletePaymentOrderByOrderCode(ordercode);
	}

	@Override
	public List<PaymentOrder> findOrderByTimeAndChannelTagAndStatus(String startTimeDate, String endTimeDate,
			String[] channelTag, String orderStatus, String remark) {
		return paymentOrderRepository.findOrderByTimeAndChannelTagAndStatus(startTimeDate, endTimeDate, channelTag,
				orderStatus, remark);
	}

	@Override
	public List<PaymentOrder> findOrderByTimeAndChannelTagAndStatusAndRemark(String startTimeDate, String endTimeDate,
			String[] channelTag, String orderStatus) {
		return paymentOrderRepository.findOrderByTimeAndChannelTagAndStatusAndRemark(startTimeDate, endTimeDate,
				channelTag, orderStatus);
	}

	@Override
	public Object queryPaymentOrderSumAmountByUserId(long[] userId, String startTime, String endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		BigDecimal big = null;
		BigDecimal big1 = null;
		BigDecimal big2 = null;
		
		if(userId != null && userId.length>0) {
			
			StringBuffer userSb = new StringBuffer();
			for (long l : userId) {
				userSb.append(l + ",");
			}
			String str = userSb.substring(0, userSb.length() - 1);
			StringBuffer sql = new StringBuffer(
					"select sum(amount) from t_payment_order where order_status=1 and user_id in(" + str + ")");

			if (startTime != null && !"".equals(startTime)) {
				sql.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
			}

			StringBuffer sql1 = new StringBuffer(
					"select sum(amount) from t_payment_order where order_status=1 and user_id in(" + str + ")");

			if (startTime != null && !"".equals(startTime)) {
				sql1.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql1.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
			}
			
			StringBuffer sql2 = new StringBuffer(
					"select sum(real_amount) from t_payment_order where order_status=1 and user_id in(" + str + ")");

			if (startTime != null && !"".equals(startTime)) {
				sql2.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql2.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
			}
			
			// 充值的金额
			Map<String, Object> rechargeAmount = jdbcTemplate.queryForMap(sql.append(" and order_type=0").toString());
			if (rechargeAmount.get("sum(amount)") != null) {
				big = (BigDecimal) rechargeAmount.get("sum(amount)");
			} else {
				double d = 0.00;
				big = big.valueOf(d);
			}

			// 购买产品的金额
			Map<String, Object> buyAmount = jdbcTemplate.queryForMap(sql1.append(" and order_type=1").toString());
			
			if (buyAmount.get("sum(amount)") != null) {
				big1 = (BigDecimal) buyAmount.get("sum(amount)");
			} else {
				double d = 0.00;
				big1 = big1.valueOf(d);
			}

			// 还款的金额
			Map<String, Object> repaymentAmount = jdbcTemplate.queryForMap(sql2.append(" and order_type=11").toString());
			if (repaymentAmount.get("sum(real_amount)") != null) {
				big2 = (BigDecimal) repaymentAmount.get("sum(real_amount)");
			} else {
				double d = 0.00;
				big2 = big2.valueOf(d);
			}
			
		}else {
			double d = 0.00;
			big = big.valueOf(d);
			big1 = big1.valueOf(d);
			big2 = big2.valueOf(d);
		}
		
		

		map.put("big", big);
		map.put("big1", big1);
		map.put("big2", big2);
		
		return map;
	}

	@Override
	public PaymentOrder findByOutMerOrdercode(String outOrderCode) {
		return paymentOrderRepository.findByOutMerOrdercode(outOrderCode);
	}

	@Override
	public List<String> findYBpayOrder(String startTimeDate, String endTimeDate, String channelTag,
			String[] orderStatus) {
		em.clear();
		return paymentOrderRepository.findYBpayOrder(startTimeDate, endTimeDate, channelTag, orderStatus);
	}

	@Override
	public BigDecimal getEveryDayMaxLimit(String startTimeDate, String endTimeDate, String channelTag,
			String orderStatus, long userId) {
		em.clear();
		BigDecimal everyDayMaxLimit = paymentOrderRepository.getEveryDayMaxLimit(userId, channelTag, orderStatus, startTimeDate, endTimeDate);
		return everyDayMaxLimit;
	}

	@Override
	public BigDecimal getCalculationProfit(String channelTag, String repaymentOrQuick, String rate, String extraFee, String startTime, String endTime) {
		
		Long LongCount = null;
		BigDecimal bigAmount = null;
		BigDecimal bigRealAmount = null;
		
		if("0".equals(repaymentOrQuick)) {
			
			StringBuffer sql = new StringBuffer(" from t_payment_order where order_status =1 and channel_tag='" + channelTag + "'");
			
			if (startTime != null && !"".equals(startTime)) {
				sql.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
			}
			
			StringBuffer count = new StringBuffer("select count(*)").append(sql);
			StringBuffer amount = new StringBuffer("select sum(amount)").append(sql);
			StringBuffer realAmount = new StringBuffer("select sum(real_amount)").append(sql);
			
			Map<String, Object> countMap = jdbcTemplate.queryForMap(count.toString());
			if (countMap.get("count(*)") != null) {
				LongCount = (Long) countMap.get("count(*)");
			} else {
				LongCount = new Long(0);
			}
			
			Map<String, Object> amountMap = jdbcTemplate.queryForMap(amount.toString());
			if (amountMap.get("sum(amount)") != null) {
				bigAmount = (BigDecimal) amountMap.get("sum(amount)");
			} else {
				double d = 0.00;
				bigAmount = bigAmount.valueOf(d);
			}
			
			Map<String, Object> realAmountMap = jdbcTemplate.queryForMap(realAmount.toString());
			if (realAmountMap.get("sum(real_amount)") != null) {
				bigRealAmount = (BigDecimal) realAmountMap.get("sum(real_amount)");
			} else {
				double d = 0.00;
				bigRealAmount = bigRealAmount.valueOf(d);
			}
			
			BigDecimal subtract = bigAmount.subtract(bigRealAmount).subtract(bigAmount.multiply(new BigDecimal(rate))).subtract(new BigDecimal(LongCount).multiply(new BigDecimal(extraFee)));
			
			return subtract;
			
		}else {
			
			BigDecimal bigFastPayRealAmount = null;
			BigDecimal bigFastPayAmount = null;
			Long longCount = null;
			
			StringBuffer sql = new StringBuffer(" from t_payment_order where order_status =1 and channel_tag='" + channelTag + "'");
			
			if (startTime != null && !"".equals(startTime)) {
				sql.append(" and date_format(create_time,'%Y-%m-%d')>='" + startTime + "'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql.append(" and date_format(create_time,'%Y-%m-%d')<='" + endTime + "'");
			}
			
			StringBuffer fastPayRealAmount = new StringBuffer("select sum(real_amount)").append(sql).append(" and order_type=10");
			
			StringBuffer transferPayCount = new StringBuffer("select count(*)").append(sql).append(" and order_type=11");
			StringBuffer fastPayAmount = new StringBuffer("select sum(amount)").append(sql).append(" and order_type=10");
			
			Map<String, Object> countMap = jdbcTemplate.queryForMap(transferPayCount.toString());
			if (countMap.get("count(*)") != null) {
				longCount = (Long) countMap.get("count(*)");
			} else {
				
			}
			
			Map<String, Object> fastPayRealAmountMap = jdbcTemplate.queryForMap(fastPayRealAmount.toString());
			if (fastPayRealAmountMap.get("sum(real_amount)") != null) {
				bigFastPayRealAmount = (BigDecimal) fastPayRealAmountMap.get("sum(real_amount)");
			} else {
				
			}
			
			Map<String, Object> fastPayAmountMap = jdbcTemplate.queryForMap(fastPayAmount.toString());
			if (fastPayAmountMap.get("sum(amount)") != null) {
				bigFastPayAmount = (BigDecimal) fastPayAmountMap.get("sum(amount)");
			} else {
				
			}
			
			BigDecimal subtract = bigFastPayRealAmount.subtract(bigFastPayRealAmount.multiply(new BigDecimal(rate))).subtract(bigFastPayAmount).subtract(new BigDecimal(longCount).multiply(new BigDecimal(extraFee)));
			
			return subtract;
			
		}
		
	}

	
	@Override
	public BigDecimal queryPaymentOrderSumAmountByUserIds(long[] userId, String[] type, Date startTime, Date endTime) {
		em.clear();
		BigDecimal result = paymentOrderRepository.queryPaymentOrderSumAmountByUserIds(userId, type, startTime, endTime);
		return result;
	}

	@Override
	public List<PaymentOrder> getPaymentOrderByUserIdAndChannelTagAndStatus(long userId, String channelTag,
			String orderStatus, String startTime, String endTime) {
		em.clear();
		List<PaymentOrder> result = paymentOrderRepository.getPaymentOrderByUserIdAndChannelTagAndStatus(userId, channelTag, orderStatus, startTime, endTime);
		return result;
	}

	@Override
	public Map getTransactionByChannelTagAndTypeAndBrandIdAndDate(String channelTag, BigDecimal brandMinRate, BigDecimal brandExtraFee, BigDecimal costRate, BigDecimal costExtraFee, String type, int isBankRate, String brandId,
			String startTime, String endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long longCount = null;
		BigDecimal bigAmount = null;
		BigDecimal bigRealAmount = null;
		BigDecimal brandProfit = null;
		BigDecimal costProfit = null;
		
		if("0".equals(type)) {
			
			StringBuffer sql = new StringBuffer(" from t_payment_order where order_status = '1' and order_type = '0' and channel_tag='" + channelTag + "'");
			
			if (brandId != null && !"".equals(brandId)) {
				sql.append(" and brand_id='" + brandId + "'");
			}
			if (startTime != null && !"".equals(startTime)) {
				sql.append(" and create_time>='" + startTime + " 00:00:00'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql.append(" and create_time<='" + endTime + " 23:59:59'");
			}
			
			StringBuffer count = new StringBuffer("select count(*)").append(sql);
			StringBuffer amount = new StringBuffer("select sum(amount)").append(sql);
			StringBuffer realAmount = new StringBuffer("select sum(real_amount)").append(sql);
			
			Map<String, Object> countMap = jdbcTemplate.queryForMap(count.toString());
			if (countMap.get("count(*)") != null) {
				longCount = (Long) countMap.get("count(*)");
			} else {
				longCount = new Long(0);
			}
			
			Map<String, Object> amountMap = jdbcTemplate.queryForMap(amount.toString());
			if (amountMap.get("sum(amount)") != null) {
				bigAmount = (BigDecimal) amountMap.get("sum(amount)");
			} else {
				bigAmount = BigDecimal.ZERO;
			}
			Map<String, Object> realAmountMap = jdbcTemplate.queryForMap(realAmount.toString());
			if (realAmountMap.get("sum(real_amount)") != null) {
				bigRealAmount = (BigDecimal) realAmountMap.get("sum(real_amount)");
			} else {
				bigRealAmount = BigDecimal.ZERO;
			}
			
			brandProfit = bigAmount.subtract(bigRealAmount).subtract(bigAmount.multiply(brandMinRate)).subtract(new BigDecimal(longCount).multiply(brandExtraFee));
			BigDecimal profit1 = null;
			BigDecimal add = BigDecimal.ZERO;
			if(isBankRate == 1) {
				
				StringBuffer sql1 = new StringBuffer(" group by bank_name");
				
				StringBuffer group = new StringBuffer("select sum(amount),count(*),sum(real_amount),bank_name,channel_tag").append(sql).append(sql1);
				
				List<Map<String, Object>> list = jdbcTemplate.queryForList(group.toString());
				
				LOG.info("list======" + list);
				for(int i = 0 ; i < list.size(); i++) {
					Map<String, Object> map2 = list.get(i);
					LOG.info("map2======" + map2);
					
					String bankName = (String) map2.get("bank_name");
					BigDecimal bigAmount1 = (BigDecimal) map2.get("sum(amount)");
					BigDecimal bigRealAmount1 = (BigDecimal) map2.get("sum(real_amount)");
					Long LongCount1 = (Long) map2.get("count(*)");
					
					ChannelBankRate channelBankRateByChannelTagAndBankName = channelBankRateRepository.getChannelBankRateByChannelTagAndBankName(channelTag, Util.queryBankNameByBranchName(bankName));
					
					if(channelBankRateByChannelTagAndBankName != null) {
						BigDecimal costRate2 = channelBankRateByChannelTagAndBankName.getCostRate();
						BigDecimal extraFee = channelBankRateByChannelTagAndBankName.getExtraFee();
						
						profit1 = bigAmount1.subtract(bigRealAmount1).subtract(bigAmount1.multiply(costRate2)).subtract(new BigDecimal(LongCount1).multiply(extraFee));
						
						LOG.info("profit1======" + profit1);
						
					}else {
						
						profit1 = bigAmount1.subtract(bigRealAmount1).subtract(bigAmount1.multiply(costRate)).subtract(new BigDecimal(LongCount1).multiply(costExtraFee));
						
						LOG.info("profit1======" + profit1);
						
					}
					
					add =  add.add(profit1);
					
				}
				
				costProfit = add;
				
			}else {
				
				costProfit = bigAmount.subtract(bigRealAmount).subtract(bigAmount.multiply(costRate)).subtract(new BigDecimal(longCount).multiply(costExtraFee));
			}
			
			LOG.info("costProfit======" + costProfit);
			
			map.put("count", longCount);
			map.put("sumAmount", bigAmount);
			map.put("sumRealAmount", bigRealAmount);
			map.put("brandProfit", brandProfit);
			map.put("costProfit", costProfit.subtract(brandProfit));
			
			return map;
			
		}
		
		if("10".equals(type)) {
			
			BigDecimal bigFastPayRealAmount = null;
			BigDecimal bigFastPayAmount = null;
			
			StringBuffer sql = new StringBuffer(" from t_payment_order where order_status = '1' and channel_tag='" + channelTag + "'");
			
			if (brandId != null && !"".equals(brandId)) {
				sql.append(" and brand_id='" + brandId + "'");
			}
			if (startTime != null && !"".equals(startTime)) {
				sql.append(" and create_time>='" + startTime + " 00:00:00'");
			}
			if (endTime != null && !"".equals(endTime)) {
				sql.append(" and create_time<='" + endTime + " 23:59:59'");
			}
			
			StringBuffer fastPayRealAmount = new StringBuffer("select sum(real_amount)").append(sql).append(" and order_type='10'");
			
			StringBuffer transferPayCount = new StringBuffer("select count(*)").append(sql).append(" and order_type='11'");
			StringBuffer fastPayAmount = new StringBuffer("select sum(amount)").append(sql).append(" and order_type='10'");
			
			Map<String, Object> countMap = jdbcTemplate.queryForMap(transferPayCount.toString());
			if (countMap.get("count(*)") != null) {
				longCount = (Long) countMap.get("count(*)");
			} else {
				longCount = new Long(0);
			}
			
			Map<String, Object> fastPayRealAmountMap = jdbcTemplate.queryForMap(fastPayRealAmount.toString());
			if (fastPayRealAmountMap.get("sum(real_amount)") != null) {
				bigFastPayRealAmount = (BigDecimal) fastPayRealAmountMap.get("sum(real_amount)");
			} else {
				bigFastPayRealAmount = BigDecimal.ZERO;
			}
			
			Map<String, Object> fastPayAmountMap = jdbcTemplate.queryForMap(fastPayAmount.toString());
			if (fastPayAmountMap.get("sum(amount)") != null) {
				bigFastPayAmount = (BigDecimal) fastPayAmountMap.get("sum(amount)");
			} else {
				bigFastPayAmount = BigDecimal.ZERO;
			}
			
			brandProfit = bigFastPayRealAmount.subtract(bigFastPayRealAmount.multiply(brandMinRate)).subtract(bigFastPayAmount).subtract(new BigDecimal(longCount).multiply(brandExtraFee));

			BigDecimal profit1 = null;
			BigDecimal add = BigDecimal.ZERO;
			if(isBankRate == 1) {
				
				StringBuffer sql1 = new StringBuffer(" group by bank_name");
				
				StringBuffer group = new StringBuffer("select sum(amount),count(*),sum(real_amount),bank_name,channel_tag").append(sql).append(" and order_type='10'").append(sql1);
				
				List<Map<String, Object>> list = jdbcTemplate.queryForList(group.toString());
				
				LOG.info("list======" + list);
				
				for(int i = 0 ; i < list.size(); i++) {
					Map<String, Object> map2 = list.get(i);
					LOG.info("map2======" + map2);
					
					String bankName = (String) map2.get("bank_name");
					BigDecimal bigAmount1 = (BigDecimal) map2.get("sum(amount)");
					BigDecimal bigRealAmount1 = (BigDecimal) map2.get("sum(real_amount)");
					//Long LongCount1 = (Long) map2.get("count(*)");
					
					ChannelBankRate channelBankRateByChannelTagAndBankName = channelBankRateRepository.getChannelBankRateByChannelTagAndBankName(channelTag, Util.queryBankNameByBranchName(bankName));
					
					if(channelBankRateByChannelTagAndBankName != null) {
						BigDecimal costRate2 = channelBankRateByChannelTagAndBankName.getCostRate();
						BigDecimal extraFee = channelBankRateByChannelTagAndBankName.getExtraFee();
						
						//profit1 = bigRealAmount1.subtract(bigRealAmount1.multiply(costRate2)).subtract(bigAmount1).subtract(new BigDecimal(longCount).multiply(extraFee));
						profit1 = bigRealAmount1.subtract(bigRealAmount1.multiply(costRate2)).subtract(bigAmount1);
						
						LOG.info("profit1======" + profit1);
						
					}else {
						
						profit1 = bigRealAmount1.subtract(bigRealAmount1.multiply(costRate)).subtract(bigAmount1);
						
						LOG.info("profit1======" + profit1);
						
					}
					
					add =  add.add(profit1);
					
				}
				
				costProfit = add.subtract(new BigDecimal(longCount).multiply(costExtraFee));
				
			}else {
				
				costProfit = bigFastPayRealAmount.subtract(bigFastPayRealAmount.multiply(costRate)).subtract(bigFastPayAmount).subtract(new BigDecimal(longCount).multiply(costExtraFee));

			}
			
			LOG.info("costProfit======" + costProfit);
			
			map.put("count", longCount);
			map.put("sumAmount", bigFastPayAmount);
			map.put("sumRealAmount", bigFastPayRealAmount);
			map.put("brandProfit", brandProfit);
			map.put("costProfit", costProfit.subtract(brandProfit));
			
			return map;
			
		}
		
		
		
		
		
		
		
		return null;
	}

	@Transactional
	@Override
	public void createBrandProfit(BrandProfit brandProfit) {
		brandProfitRepository.saveAndFlush(brandProfit);
		em.clear();
	}

	@Override
	public Map<String, Object> getSumProfitByStartTimeAndEndTimeAndBrandIdAndChannelTag(String startTime, String endTime, long brandId, String channelTag) {
		
		StringBuffer sql = new StringBuffer(" from t_payment_order where order_status = '1' and channel_tag='" + channelTag + "'");
		
		if (brandId != -1) {
			sql.append(" and brand_id='" + brandId + "'");
		}
		if (startTime != null && !"".equals(startTime)) {
			sql.append(" and create_time>='" + startTime + " 00:00:00'");
		}
		if (endTime != null && !"".equals(endTime)) {
			sql.append(" and create_time<='" + endTime + " 23:59:59'");
		}
		
		//StringBuffer sql1 = new StringBuffer(" and order_type not in('11')");
		StringBuffer sql1 = new StringBuffer(" and order_type in('11','0')");
		
		StringBuffer group = new StringBuffer("select sum(phone_bill),sum(car_no),sum(amount),sum(real_amount),count(*)").append(sql);
		
		StringBuffer group1 = new StringBuffer("select sum(amount),sum(real_amount),count(*)").append(sql).append(sql1);
		
		Map<String, Object> map = jdbcTemplate.queryForMap(group.toString());
		
		Map<String, Object> map1 = jdbcTemplate.queryForMap(group1.toString());
		
		map.putAll(map1);
		
		LOG.info("map======" + map);
		
		return map;
	}

	@Override
	public Map<String, Object> getBrandProfitByBrandId(long brandId, Pageable pageAble) {
		Map<String, Object> object = new HashMap<String, Object>();
		
		StringBuffer sql = new StringBuffer(" from t_brand_profit where 1=1 ");
		
		if (brandId != -1) {
			sql.append(" and brand_id='" + brandId + "'");
		}
		
		StringBuffer sqlCount = new StringBuffer("select count(*) as count from t_brand_profit where brand_id=2 and channel_tag='GHTDH_QUICK'");
		
		int count = Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString());
		
		int pageNum = pageAble.getPageSize();
		int currentPage = pageAble.getPageNumber();
		
		StringBuffer sql1 = new StringBuffer(" group by trade_time");
		
		StringBuffer group = new StringBuffer("select trade_time as tradeTime,sum(sum_amount) as sumAmount,sum(sum_real_amount) as sumRealAmount,sum(brand_profit) as brandProfit,sum(cost_profit) as costProfit,sum(number),sum(count_profit)").append(sql).append(sql1)
				.append(" order by trade_time desc limit " + (currentPage) * pageNum + "," + pageNum);
		
		List<Map<String, Object>> list = jdbcTemplate.queryForList(group.toString());
		
		LOG.info("list======" + list);
		
		object.put("pageNum", pageNum); // 每页显示条数
		object.put("currentPage", currentPage); // 当前页
		object.put("totalElements", count); // 总条数
		if (pageNum != 0) {
			object.put("totalPages", count / pageAble.getPageSize()); // 总页数
		}
		object.put("content", list);
		
		return object;
	}

	@Override
	public Map<String, Object> getBrandProfitByTradeTime(String tradeTime, Pageable pageAble) {
		//em.clear();
		//Page<BrandProfit> result = brandProfitRepository.getBrandProfitByTradeTime(tradeTime, pageAble);
		
		Map<String, Object> object = new HashMap<String, Object>();
		
		StringBuffer sql = new StringBuffer(" from t_brand_profit where 1=1 ");
		
		StringBuffer sqlCount = new StringBuffer("select count(*) as count from t_brand_profit where brand_id=2 and trade_time='" + tradeTime + "'");
		
		int count = Integer.parseInt(jdbcTemplate.queryForMap(sqlCount.toString()).get("count").toString());
		
		int pageNum = pageAble.getPageSize();
		int currentPage = pageAble.getPageNumber();
		
		StringBuffer sql1 = new StringBuffer(" group by channel_tag");
		
		StringBuffer group = new StringBuffer("select channel_name as channelName,channel_real_name as channelRealName,channel_tag as channelTag,channel_type as channelType,trade_time as tradeTime,sum(sum_amount) as sumAmount,sum(sum_real_amount) as sumRealAmount,sum(brand_profit) as brandProfit,sum(cost_profit) as costProfit,sum(number) as number").append(sql).append(sql1)
				.append(" order by trade_time desc limit " + (currentPage) * pageNum + "," + pageNum);
		
		List<Map<String, Object>> list = jdbcTemplate.queryForList(group.toString());
		
		LOG.info("list======" + list);
		
		object.put("pageNum", pageNum); // 每页显示条数
		object.put("currentPage", currentPage); // 当前页
		object.put("totalElements", count); // 总条数
		if (pageNum != 0) {
			object.put("totalPages", count / pageAble.getPageSize()); // 总页数
		}
		object.put("content", list);
		
		return object;
	}

	@Override
	public Page<BrandProfit> getBrandProfitByBrandIdAndTradeTime(long brandId, String tradeTime, Pageable pageAble) {
		em.clear();
		Page<BrandProfit> result = brandProfitRepository.getBrandProfitByBrandIdAndTradeTime(brandId, tradeTime, pageAble);
		return result;
	}

	@Override
	public List<PaymentOrder> findOrderByUpdateTimeTimeAndChannelTagAndStatus(String startTimeDate, String endTimeDate,
			String[] channelTag, String orderStatus, String remark) {
		return paymentOrderRepository.findOrderByUpdateTimeTimeAndChannelTagAndStatus(startTimeDate, endTimeDate, channelTag, orderStatus, remark);
	}

	@Override
	public Map<String, Object> findsumPaymentOrderByUserIdAndLevel(long userid, String[] type, String[] status, 
			String autoClearing, String level,Date startTimeDate, Date endTimeDate) {
		
		String start =new SimpleDateFormat("yyyy-MM-dd").format(startTimeDate);
		String end =new SimpleDateFormat("yyyy-MM-dd").format(endTimeDate);
		
		StringBuffer sql = new StringBuffer("select SUM(p.real_amount) ra from `user`.t_user_relation u");
		sql.append(" LEFT JOIN transactionclear.t_payment_order p");
		sql.append(" on u.first_user_id = p.user_id");
		sql.append(" where u.pre_user_id= " + userid);
		if(!level.equals("-1")) {
			sql.append(" and u.level="+ "'" + level + "'");
		}
		
		sql.append(" and p.order_status in " + "(" + "1" + ")");
		sql.append(" and p.order_type in" + "(" + "10" + ")");
		sql.append(" and p.auto_clearing= " + "'"  + autoClearing + "'" );
		sql.append(" and p.create_time>=" + "'" + start + "'");
		sql.append(" and p.create_time < " + "'"+ end + "'");
		
		System.out.println(sql.toString());
		Map<String, Object> map = jdbcTemplate.queryForMap(sql.toString());
		LOG.info("map======" + map);
		
		return map;
	}

	@Override
	public Map<String, Object> findsumPaymentOrderAmountByUserIdAndLevel(long userid, String[] type, String[] status,
			 String autoClearing, String level, Date startTimeDate, Date endTimeDate) {
		
		String start =new SimpleDateFormat("yyyy-MM-dd").format(startTimeDate);
		String end =new SimpleDateFormat("yyyy-MM-dd").format(endTimeDate);
		
		StringBuffer sql = new StringBuffer("select SUM(p.amount) a from `user`.t_user_relation u ");
		sql.append(" LEFT JOIN transactionclear.t_payment_order p");
		sql.append(" on u.first_user_id = p.user_id");
		sql.append(" where u.pre_user_id= " + userid);
		if(!level.equals("-1")) {
			sql.append(" and u.level="+ "'" + level + "'");
		}
		sql.append(" and p.order_status in " + "(" + "1" + ")");
		sql.append(" and p.order_type in" + "(" + "0" + ")");
		sql.append(" and p.auto_clearing= " + "'"  + autoClearing + "'" );
		sql.append(" and p.create_time>=" + "'" + start + "'");
		sql.append(" and p.create_time < " + "'"+ end + "'");
		System.out.println(sql.toString());
		
		Map<String, Object> map = jdbcTemplate.queryForMap(sql.toString());
		
		LOG.info("map======" + map);
		
		return map;
	}
	@Override
	public Page<PaymentOrder> queryPaymentOrderByUserIdsAndMore(String[] userId, String type, String status,
			Date startTime, Date endTime, Pageable pageAble) {
		return paymentOrderRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
           
            In<Long> in = criteriaBuilder.in(root.get(PaymentOrder_.userid));
            for (String string : userId) {
            	in.value(Long.valueOf(string));
			}
            predicatesList.add(criteriaBuilder.and(in));
            
            if (isNotNull(type)) {
                
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(PaymentOrder_.type), type)));
            	
			}
            if (isNotNull(status)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(PaymentOrder_.status), Integer.valueOf(status))));
			}
            
            if (startTime != null) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get(PaymentOrder_.createTime), startTime)));
            }
            
            if (endTime != null) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get(PaymentOrder_.createTime), endTime)));
            }
            
            return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageAble);
	}
	
	private static boolean isNotNull(String str) {
		return !(str == null || "".equals(str) || "null".equalsIgnoreCase(str));
	}

	/*@Override
	public List<String> getPaymentOrderByStartTimeAndEndTime(String[] orderType, String[] channelTag,String startTime, String endTime) {
	
		List<String> result = paymentOrderRepository.getPaymentOrderByStartTimeAndEndTime(orderType, channelTag, startTime, endTime);
		
		return result;
	}*/

	

}
