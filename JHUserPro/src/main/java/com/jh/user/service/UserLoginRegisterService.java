package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.*;
import com.jh.user.business.*;
import com.jh.user.pojo.*;
import com.jh.user.redis.RedisUtil;
import com.jh.user.util.AliOSSUtil;
import com.jh.user.util.Util;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@EnableAutoConfiguration
public class UserLoginRegisterService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserBalanceBusiness userBalBusiness;

    @Autowired
    private BrandManageBusiness brandMangeBusiness;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private BrandManageBusiness brandManageBusiness;

    @Autowired
    private UserShopsBusiness userShopsBusiness;

    @Autowired
    private UserBankInfoBusiness userBankInfoBusiness;

    @Autowired
    private UserRealtionBusiness userRealtionBusiness;

    @Autowired
    private ThirdLeveDistributionBusiness thirdLevelBusiness;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserJpushService userJpushService;

    @Autowired
    private UpGradeDetailBusiness upGradeDetailBusiness;

    @Autowired
    Util util;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AliOSSUtil aliOSSUtil;

    /**
     * 外放用户临时开户
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/outchannel/new")
    public @ResponseBody
    Object registerUser(HttpServletRequest request, @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "pre_phone", required = false) String prephone, @RequestParam(value = "brand_id", required = false, defaultValue = "-1") long brandid) {

        Map map = new HashMap();
        User user = new User();
        User preuser = null;
        if (prephone != null && !prephone.equalsIgnoreCase("")) {
            if (brandid == -1) {
                preuser = userLoginRegisterBusiness.queryUserByPhone(prephone);
            } else {
                Brand brand = brandManageBusiness.findBrandById(brandid);
                if (brand != null && "6".equals(brand.getBrandType())) {
                    preuser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(prephone, brandid);
                } else {
                    preuser = userLoginRegisterBusiness.queryUserByPhone(prephone);
                }
            }
            if (preuser == null) {

                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESP_MESSAGE, "上级用户不存在");
                return map;

            }

        }

        user.setId(Long.parseLong(RandomUtils.generateNumString(9)));
        user.setCreateTime(new Date());
        // user.setOpenid(openid);
        user.setPassword(Md5Util.getMD5("123456"));
        user.setPhone(phone.trim());

        user.setGrade("0");
        user.setPaypass(Md5Util.getMD5("123456"));
        // user.setUnionid(unionid);
        user.setValidStatus(0);
        // user.setInviteCode(invitecode);
        user.setBrandId(brandid);

        if (preuser != null) {
            user.setPreUserId(preuser.getId());
            user.setPreUserPhone(prephone);
        }

        user = userLoginRegisterBusiness.createOutNewUser(user);
        String userToken = TokenUtil.createToken(user.getId(), brandid, phone);
        user.setUserToken(userToken);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }


    /**
     * 根据用户的手机号码和贴牌返回用户的基本信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/1.0/user/query/phonebrand")
    public @ResponseBody
    Object queryUserInfoByPhoneBrandId(HttpServletRequest request, @RequestParam(value = "phone") String phone,
                                       @RequestParam(value = "brandid") String brandid) {
        User user = userLoginRegisterBusiness.queryUserByPhoneBrandid(phone, Long.parseLong(brandid));
        Map map = new HashMap();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 代理商开户接口
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/openaccount")
    public @ResponseBody
    Object openAccount(HttpServletRequest request,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "fullname", required = false) String fullname,
                       @RequestParam(value = "origcode", required = false) String origcode,
                       @RequestParam(value = "signcode", required = false) String signcode,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "zipcode", required = false) String zipcode,
                       @RequestParam(value = "contact_name", required = false) String contactname,
                       @RequestParam(value = "grade", required = false) String grade,
                       @RequestParam(value = "pre_phone", required = false) String prephone,
                       @RequestParam(value = "brand_id", defaultValue = "1", required = false) long brandid,
                       @RequestParam(value = "brand_name", defaultValue = "节付宝", required = false) String brandname) {

        Map<String, Object> map = new HashMap<String, Object>();
        User user;
        Brand brand = null;
        if (brandid == -1) {
            user = userLoginRegisterBusiness.queryUserByPhone(phone);
        } else {
            brand = brandMangeBusiness.findBrandById(brandid);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
        }

        if (user != null && user.getId() != 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_HAS_REGISTER);
            map.put(CommonConstants.RESP_MESSAGE, "用户已经注册");
            return map;
        } else {
            user = new User();
        }

        User preuser = null;
        if (prephone != null && !prephone.equalsIgnoreCase("")) {
            if (brandid == -1) {
                preuser = userLoginRegisterBusiness.queryUserByPhone(prephone);
            } else {
                if (brand == null) {
                    brand = brandMangeBusiness.findBrandById(brandid);
                }
                if (brand != null && "6".equals(brand.getBrandType())) {
                    preuser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(prephone, brandid);
                } else {
                    preuser = userLoginRegisterBusiness.queryUserByPhone(prephone);
                }
            }
            if (preuser == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESP_MESSAGE, "上级用户不存在");
                return map;
            }

        }

        user.setId(Long.parseLong(RandomUtils.generateNumString(9)));
        user.setCreateTime(new Date());
        // user.setOpenid(openid);
        user.setPassword(Md5Util.getMD5("123456"));
        user.setPhone(phone.trim());
        user.setFullname(fullname);
        user.setOrigcode(origcode);
        user.setSigncode(signcode);
        user.setAddress(address);
        user.setZipcode(zipcode);
        user.setGrade(grade == null ? "0" : grade);
        user.setContactname(contactname);
        user.setPaypass(Md5Util.getMD5("123456"));
        // user.setUnionid(unionid);
        user.setValidStatus(0);
        // user.setInviteCode(invitecode);
        user.setBrandId(brandid);
        user.setBrandname(brandname);

        if (preuser != null) {
            user.setPreUserId(preuser.getId());
            user.setPreUserPhone(prephone);
        }

        user = userLoginRegisterBusiness.createNewUser(user);
        String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
        user.setUserToken(userToken);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 注册接口
     **/
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/register")
    public @ResponseBody
    Object registerUser(HttpServletRequest request,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "vericode", required = false) String vericode,
                        @RequestParam(value = "paypass", required = false) String paypass,
                        @RequestParam(value = "password") String password,
                        @RequestParam(value = "openid", required = false) String openid,
                        @RequestParam(value = "unionid", required = false) String unionid,
                        @RequestParam(value = "invitecode", required = false) String invitecode,
                        @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandid,
                        @RequestParam(value = "brand_name", defaultValue = "节付宝", required = false) String brandname,
                        @RequestParam(value = "province", required = false) String province,
                        @RequestParam(value = "city", required = false) String city,
                        @RequestParam(value = "county", required = false) String county) {
        Map<String, Object> map = new HashMap<String, Object>();
        Brand brand = brandManageBusiness.findBrandById(brandid);
        if (brand != null) {
            brandname = brand.getName();
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESP_MESSAGE, "上级用户不存在");
            return map;
        }

        /** 如果短信验证码不为空， 那么需要 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("notice", "error url request!");
        String url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + phone;
        String resultStr = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(resultStr);
        String code = jsonObject.getString("result");
        LOG.info("发送码：" + vericode + "===校验码：" + code);

        if (code != null && !code.equalsIgnoreCase("") && !code.equalsIgnoreCase(vericode)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证码错误");
            return map;
        }

        User preuser = null;
        if (invitecode != null && !invitecode.trim().equalsIgnoreCase("")) {
            if (brandid == -1) {
                preuser = userLoginRegisterBusiness.queryUserByPhone(invitecode);
            } else {
                preuser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(invitecode, brandid);
            }
            if (preuser == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESP_MESSAGE, "推荐人不存在");
                return map;
            }
        } else {
            long manageid = brand.getManageid();
            preuser = userLoginRegisterBusiness.queryUserById(manageid);
            if (preuser == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESP_MESSAGE, "上级用户不存在");
                return map;
            } else {
                invitecode = preuser.getPhone();
            }
        }

        LOG.info("注册代码：" + phone + " brand:" + brandid);
        /** 判断手机号码是否存在，如果已经存在， 那么不能继续 注册 */
        if (phone != null && !phone.equalsIgnoreCase("")) {
            User user;
            if (brandid == -1) {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            } else {
                if (brand != null && "6".equals(brand.getBrandType())) {
                    user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
                } else {
                    user = userLoginRegisterBusiness.queryUserByPhone(phone);
                }
            }
            LOG.info("查询=====" + phone + " 是否注册结果=====" + user);
            if (user != null && user.getId() != 0) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_HAS_REGISTER);
                map.put(CommonConstants.RESP_MESSAGE, "用户已经注册");
                return map;
            }

        }
        User user = new User();
        user.setId(Long.parseLong(RandomUtils.generateNumString(9)));
        user.setCreateTime(new Date());
        user.setOpenid(openid);
        user.setPassword(Md5Util.getMD5(password));
        user.setPhone(phone.trim());
        user.setPaypass(Md5Util.getMD5(paypass));
        user.setUnionid(unionid);
        user.setValidStatus(0);
        user.setInviteCode(invitecode.trim());
        user.setBrandId(brandid);
        user.setBrandname(brandname);
        user.setProvince(province);
        user.setCity(city);
        user.setCounty(county);
        if (preuser != null) {
            user.setPreUserPhone(preuser.getPhone());
            user.setPreUserId(preuser.getId());
        }
        user = userLoginRegisterBusiness.createNewUser(user);
        String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
        user.setUserToken(userToken);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }


    //外放代注接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/pre/registration/externalrelease")
    public @ResponseBody
    Object preRegistrationExternalRelease(HttpServletRequest request,
                                          String phone,
                                          String inviteCode,
                                          long brandId,
                                          String secretKey,
                                          String sign) {
        LOG.info("请求参数======phone=" + phone + " &inviteCode=" + inviteCode + " &brandId=" + brandId + " &secretKey=" + secretKey + " &sign=" + sign);

        Map<String, String> map = new TreeMap<String, String>();
        map.put("phone", phone);
        map.put("inviteCode", inviteCode);
        map.put("brandId", brandId + "");

        Set<String> keySet = map.keySet();
        Iterator<String> it = keySet.iterator();
        StringBuffer sb = new StringBuffer();

        while (it.hasNext()) {
            String next = it.next();
            sb.append(next + "=" + map.get(next) + "&");
        }

        String param = sb.substring(0, sb.length() - 1);
        param = param + secretKey;
        String md5 = Md5Util.getMD5(param);

        LOG.info("phone=" + phone + " & md5======" + md5);
        if (!sign.equals(md5)) {
            LOG.info("验签失败!");
            return ResultWrap.init(CommonConstants.FALIED, "验签失败!");
        }

        boolean hasKey = false;
        String key = "/v1.0/user/registration/externalrelease:phone" + phone;
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            return ResultWrap.init(CommonConstants.FALIED, "操作频繁,请1分钟后重试!");
        }
        opsForValue.set(key, key, 1, TimeUnit.MINUTES);

        String paypass = "123456";
        String password = "123456";

        Brand brand = brandManageBusiness.findBrandById(brandId);
        if (brand != null) {
            if ("2".equals(brand.getBrandType())) {
                return ResultWrap.init(CommonConstants.FALIED, "暂时停止用户注册,请联系相关人员解决!");
            }
            String number = brand.getNumber();
            String brandName = brand.getName();
            if (secretKey.trim().equals(number.trim())) {
                User preUser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(inviteCode.trim(), brandId);
                if (preUser != null) {
                    User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone.trim(), brandId);
                    if (user == null) {
                        User user1 = new User();
                        user1.setId(Long.parseLong(RandomUtils.generateNumString(9)));
                        user1.setCreateTime(new Date());
                        user1.setPassword(Md5Util.getMD5(password));
                        user1.setPhone(phone.trim());
                        user1.setPaypass(Md5Util.getMD5(paypass));
                        user1.setValidStatus(0);
                        user1.setInviteCode(inviteCode.trim());
                        user1.setBrandId(brandId);
                        user1.setBrandname(brandName);
                        user1.setPreUserPhone(preUser.getPhone());
                        user1.setPreUserId(preUser.getId());

                        user1 = userLoginRegisterBusiness.createNewUser(user1);
                        String userToken = TokenUtil.createToken(user1.getId(), user1.getBrandId(), user1.getPhone());
                        user1.setUserToken(userToken);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("userId", user1.getId());

                        return ResultWrap.init(CommonConstants.SUCCESS, "手机号: " + phone + " 的用户注册成功!", jsonObject);
                    } else {
                        return ResultWrap.init(CommonConstants.FALIED, "手机号: " + phone + " 的用户已存在,无法重复注册!");
                    }
                } else {
                    return ResultWrap.init(CommonConstants.FALIED, "上级用户不存在,请仔细核对手机号码和贴牌Id!");
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "密钥验证错误,请仔细核对!");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "贴牌信息不存在,请仔细核对贴牌Id!");
        }
    }


    //外放实名认证接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/externalrelease/realname")
    public @ResponseBody
    Object preRegistrationExternalRelease(HttpServletRequest request,
                                          String phone,
                                          String userName,
                                          String idCard,
                                          long brandId,
                                          String secretKey,
                                          String sign) throws Exception {

        LOG.info("请求参数======phone=" + phone + " &userName=" + URLDecoder.decode(userName, "UTF-8") + " &idCard=" + idCard + " &brandId=" + brandId + " &secretKey=" + secretKey + " &sign=" + sign);

        Map<String, String> map = new TreeMap<String, String>();
        map.put("phone", phone);
        map.put("userName", URLDecoder.decode(userName, "UTF-8"));
        map.put("idCard", idCard);
        map.put("brandId", brandId + "");

        Set<String> keySet = map.keySet();
        Iterator<String> it = keySet.iterator();
        StringBuffer sb = new StringBuffer();

        while (it.hasNext()) {
            String next = it.next();
            sb.append(next + "=" + map.get(next) + "&");
        }

        String param = sb.substring(0, sb.length() - 1);
        param = param + secretKey;
        String md5 = Md5Util.getMD5(param);

        LOG.info("phone=" + phone + " & md5======" + md5);
        if (!sign.equals(md5)) {
            LOG.info("验签失败!");
            return ResultWrap.init(CommonConstants.FALIED, "验签失败!");
        }

        Brand brand = brandManageBusiness.findBrandById(brandId);
        if (brand != null) {
            if ("2".equals(brand.getBrandType())) {
                return ResultWrap.init(CommonConstants.FALIED, "暂时停止用户实名,请联系相关人员解决!");
            }
            String number = brand.getNumber();
            if (secretKey.trim().equals(number.trim())) {

                User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone.trim(), brandId);
                long userId = user.getId();

                try {
                    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                    List<MultipartFile> files = multipartRequest.getFiles("image");

                    String ossObjectNamePrefix = AliOSSUtil.REAL_NAME + "-" + user.getBrandId() + "-" + phone + "-";
                    String ossObjectName = "";

                    List<String> listFiles = aliOSSUtil.listFiles(ossObjectNamePrefix);
                    if (listFiles != null && listFiles.size() > 0) {
                        for (String fileName : listFiles) {
                            aliOSSUtil.deleteFileFromOss(fileName);
                        }
                    }

                    int i = 1;
                    if (files != null && files.size() > 0) {
                        for (MultipartFile file : files) {

                            String fileName = file.getOriginalFilename();
                            String prefix = fileName.substring(fileName.lastIndexOf("."));
                            fileName = System.currentTimeMillis() + i + prefix;
                            ossObjectName = ossObjectNamePrefix + fileName;
                            OutputStream os = new ByteArrayOutputStream();
                            ByteArrayInputStream inputStream = null;
                            try {
                                PhotoCompressUtil.compressPhoto(file.getInputStream(), os, 0.2f);
                                inputStream = FileUtils.parse(os);
                                aliOSSUtil.uploadStreamToOss(ossObjectName, inputStream);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                LOG.error(ExceptionUtil.errInfo(e1));
                            } finally {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    os = null;
                                }
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    inputStream = null;
                                }
                            }
                            i++;
                        }
                    }
                } catch (Exception e) {
                    LOG.error("处理实名图片出错======", e);
                }

                URI uri = util.getServiceUrl("paymentchannel", "error url request!");
                String url = uri.toString() + "/v1.0/paymentchannel/realname/auth/backstage";
                /** 根据的用户userid查询用户的基本信息 */
                MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                requestEntity.add("userid", userId + "");
                requestEntity.add("realname", userName);
                requestEntity.add("idcard", idCard);
                RestTemplate restTemplate = new RestTemplate();
                String result = restTemplate.postForObject(url, requestEntity, String.class);
                JSONObject fs = JSONObject.fromObject(result);
                LOG.info("请求/v1.0/paymentchannel/realname/auth/backstage返回的======" + fs);
                String msgResult = fs.getJSONObject("result").getString("result");
                if (fs.getString("resp_code").equals("000000") && msgResult.equals("1")) {
                    String status = "1";
                    user.setRealnameStatus(status);
                    //调用实名注册接口，将真实姓名存入用户表
                    if (status.equals("1")) {
                        uri = util.getServiceUrl("paymentchannel", "error url request!");
                        url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("userid", user.getId() + "");

                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        JSONObject jsonObject = JSONObject.fromObject(result);
                        JSONObject authObject = jsonObject.getJSONObject("realname");

                        user.setFullname(authObject.getString("realname"));
                    }

                    if ("2".equals(status)) {

                        uri = util.getServiceUrl("paymentchannel", "error url request!");
                        url = uri.toString() + "/v1.0/paymentchannel/realname/delrealname";
                        requestEntity = new LinkedMultiValueMap<String, String>();
                        requestEntity.add("userId", user.getId() + "");

                        result = restTemplate.postForObject(url, requestEntity, String.class);
                        JSONObject jsonObject = JSONObject.fromObject(result);

                        LOG.info("jsonObject======" + jsonObject);

                        String respCode = jsonObject.getString("resp_code");

                        if (!CommonConstants.SUCCESS.equals(respCode)) {

                            return ResultWrap.init(CommonConstants.FALIED, "删除实名信息数据失败!");
                        }
                    }

                    user = userLoginRegisterBusiness.saveUser(user);

                    return ResultWrap.init(CommonConstants.SUCCESS, "实名成功!");
                } else {

                    return ResultWrap.init(CommonConstants.FALIED, "审核失败!");
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "密钥验证错误,请仔细核对!");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "贴牌信息不存在,请仔细核对贴牌Id!");
        }

    }


    //重置密钥的接口
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/reset/key/externalrelease")
    public @ResponseBody
    Object userResetKey(HttpServletRequest request,
                        long brandId,
                        String secretKey) {

        boolean hasKey = false;
        String key = "/v1.0/user/reset/key:brandId=" + brandId;
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            return ResultWrap.init(CommonConstants.FALIED, "操作频繁,请一小时后重试!");
        }
        opsForValue.set(key, key, 1, TimeUnit.HOURS);

        Brand brand = brandManageBusiness.findBrandById(brandId);
        if (brand != null) {
            if ("2".equals(brand.getBrandType())) {
                return ResultWrap.init(CommonConstants.FALIED, "暂时停止用户注册,请联系相关人员解决!");
            }
            String number = brand.getNumber();
            if (secretKey.equals(number)) {
                String uuid = UUIDGenerator.getUUID();
                LOG.info("新生成的密钥 secretKey=====" + uuid);
                brand.setNumber(uuid);
                brandManageBusiness.mergeBrand(brand);

                return ResultWrap.init(CommonConstants.SUCCESS, "重置密钥成功,新密钥: " + uuid + " ,请妥善保管!");
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "密钥验证错误,请仔细核对!");
            }
        } else {
            return ResultWrap.init(CommonConstants.FALIED, "贴牌信息不存在,请仔细核对贴牌Id!");
        }
    }

    /**
     * 上级代注接口
     *
     * 2019.10.18  修改 默认密码为a123456
     **/
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/pre/register")
    public @ResponseBody
    Object preRegisterUser(HttpServletRequest request,
                           @RequestParam(value = "phone", required = false) String phone,
                           @RequestParam(value = "openid", required = false) String openid,
                           @RequestParam(value = "unionid", required = false) String unionid,
                           @RequestParam(value = "invitecode", required = false) String invitecode,
                           @RequestParam(value = "brand_id", defaultValue = "1", required = false) long brandid,
                           @RequestParam(value = "brand_name", defaultValue = "节付宝", required = false) String brandname) {
        String paypass = "123456";
        String password = "a123456";

        Map<String, Object> map = new HashMap<String, Object>();
        Brand brand = brandManageBusiness.findBrandById(brandid);
        User preuser = null;
        if (invitecode != null && !invitecode.equalsIgnoreCase("")) {
            if (brand != null && "6".equals(brand.getBrandType())) {
                preuser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(invitecode, brandid);
            } else {
                preuser = userLoginRegisterBusiness.queryUserByPhone(invitecode);
            }
            if (preuser == null) {

                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            } else {
                brandid = preuser.getBrandId();
                brandname = preuser.getBrandname();
            }

        }

        /** 判断手机号码是否存在，如果已经存在， 那么不能继续 注册 */
        if (phone != null && !phone.equalsIgnoreCase("")) {
            User user;
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
            if (user != null && user.getId() != 0) {

                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_HAS_REGISTER);
                map.put(CommonConstants.RESP_MESSAGE, "用户已经注册");
                return map;
            }

        }

        User user = new User();
        user.setId(Long.parseLong(RandomUtils.generateNumString(9)));
        user.setCreateTime(new Date());
        user.setOpenid(openid);
        user.setPassword(Md5Util.getMD5(password));
        user.setPhone(phone.trim());
        user.setPaypass(Md5Util.getMD5(paypass));
        user.setUnionid(unionid);
        user.setValidStatus(0);
        user.setInviteCode(invitecode);
        user.setBrandId(brandid);
        user.setBrandname(brandname);
        if (preuser != null) {
            user.setPreUserPhone(preuser.getPhone());
            user.setPreUserId(preuser.getId());
        }
        user = userLoginRegisterBusiness.createNewUser(user);
        String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
        user.setUserToken(userToken);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 更换用户账户手机号
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/phone/update/{token}")
    public @ResponseBody
    Object updatePhone(HttpServletRequest request, @PathVariable("token") String token,
                       // 更换手机收到的验证码
                       @RequestParam(value = "vericode", required = false) String vericode,
                       // 更换手机号
                       @RequestParam(value = "phone", required = false) String phone,
                       // 当前登录密码
                       @RequestParam(value = "password", required = false) String password) {

        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        long brandId;
        try {
            userId = TokenUtil.getUserId(token);
            brandId = TokenUtil.getBrandid(token);
        } catch (Exception e) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;

        }
		/*if (password.matches("^\\d{1,}$")) {
			return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码不能为纯数字,需包含字母和数字,请点击忘记密码进行重置");
		}*/
        /** 如果短信验证码不为空， 那么需要 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("notice", "error url request!");
        String url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + phone;
        String resultStr = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(resultStr);
        String code = jsonObject.getString("result");

        if (code != null && !code.equalsIgnoreCase("") && !code.equalsIgnoreCase(vericode)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证码错误");
            return map;
        }
        Brand brand = brandManageBusiness.findBrandById(brandId);
        /** 判断手机号码是否存在，如果已经存在， 那么不能继续 注册 */
        if (phone != null && !phone.equalsIgnoreCase("")) {
            User user;
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
            }
            if (user != null && user.getId() != 0) {

                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_HAS_REGISTER);
                map.put(CommonConstants.RESP_MESSAGE, "用户已经注册");
                return map;
            }

        }

        User user = userLoginRegisterBusiness.queryUserById(userId);

        if (!user.getPassword().equals(Md5Util.getMD5(password))) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PAY_PASS);
            map.put(CommonConstants.RESP_MESSAGE, "支付密码错误");
            return map;
        }

        user.setPhone(phone.trim());

        userLoginRegisterBusiness.saveUser(user);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;
    }

    /**
     * 用户密码登陆
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/login")
    public @ResponseBody
    Object login(HttpServletRequest request, @RequestParam(value = "phone") String phone,
                 @RequestParam(value = "brand_id", defaultValue = "1", required = false) long brandid,
                 @RequestParam(value = "password") String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "0");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String rescode = jsonObject.getString("resp_code");
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        String pass = Md5Util.getMD5(password);
        User user = userLoginRegisterBusiness.isValidUser(phone, pass, brandid);

        if (user != null && user.getId() > 0) {
//			if (password.matches("^\\d{1,}$")) {
//				return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码不能为纯数字,需包含字母和数字,请点击忘记密码进行重置");
//			}

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
            user.setUserToken(userToken);
            map.put(CommonConstants.RESULT, user);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PASS_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在或用户密码错误");
            return map;
        }
    }

    /**
     * 用户通过验证码登入
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/login/bySmsCode")
    public @ResponseBody
    Object loginBySmsCode(HttpServletRequest request, @RequestParam(value = "phone") String phone,
                 @RequestParam(value = "brand_id", defaultValue = "1", required = false) long brandid,
                 @RequestParam(value = "smsCode") String smsCode) {
        Map<String, Object> map = new HashMap<String, Object>();
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "0");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String rescode = jsonObject.getString("resp_code");
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        //短信验证码校验
        uri = util.getServiceUrl("notice", "error url request!");
        url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + phone;
        String resultStr = restTemplate.getForObject(url, String.class);
        JSONObject smsJsonObject = JSONObject.fromObject(resultStr);
        String code = smsJsonObject.getString("result");

        if (code != null && !code.equalsIgnoreCase("") && !code.equalsIgnoreCase(smsCode)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证码错误");
            return map;
        }

        User user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);

        if (user != null && user.getId() > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
            user.setUserToken(userToken);
            map.put(CommonConstants.RESULT, user);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PASS_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在或验证码错误");
            return map;
        }
    }




    /**
     * 更新用户的openid
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/openid")
    public @ResponseBody
    Object updateOpenid(HttpServletRequest request,
                        @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                        @RequestParam("phone") String phone,
                        @RequestParam(value = "openid") String openid) {

        Map<String, Object> map = new HashMap<String, Object>();
        long brandId;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e) {
            brandId = -1;
        }
        User user;
        Brand brand;
        if (brandId == -1) {
            user = userLoginRegisterBusiness.queryUserByPhone(phone);
        } else {
            brand = brandManageBusiness.findBrandById(brandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
        }
        user.setOpenid(openid);
        userLoginRegisterBusiness.saveUser(user);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "更新成功");
        return map;
    }

    /**
     * 邀请码验证
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/verify/id/{token}")
    public @ResponseBody
    Object verifyuid(HttpServletRequest request, @PathVariable("token") String token,
                     @RequestParam(value = "user_id") long userid) {

        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        if (userId != userid) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "校验失败");
            return map;
        }
        User user = userLoginRegisterBusiness.queryUserById(userId);
        user.setVerifyStatus("1");
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, userLoginRegisterBusiness.saveUser(user));
        map.put(CommonConstants.RESP_MESSAGE, "验证成功");
        return map;
    }

    /**
     * 用户密码登陆后台
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/backstage/login")
    public @ResponseBody
    Object BackstageLoginNew(HttpServletRequest request,
                             @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                             @RequestParam(value = "phone") String phone,
                             @RequestParam(value = "password") String password,
                             @RequestParam(value = "vericode") String vericode//2019.5.15 新增短信验证码
    ) {
        Map<String, Object> map = new HashMap<String, Object>();
        //到redis缓存中获取对应用户的验证码
        String vCode = String.valueOf(redisTemplate.opsForValue().get(phone));
        LOG.info("验证码===================================>" + vCode);
        if (!vCode.equals(vericode) || vCode == null || vCode == "") {
            LOG.info("验证码输入错误=====================================" + vCode);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "验证码输入有误或已失效");
            return map;
        }
        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "0");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String rescode = jsonObject.getString("resp_code");
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        String pass = Md5Util.getMD5(password);
        long brandId;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e) {
            brandId = -1;
        }
        User user;
        Brand brand;
        if (brandId == -1) {
            user = userLoginRegisterBusiness.isValidUser(phone, pass);
        } else {
            brand = brandManageBusiness.findBrandById(brandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.isValidUser(phone, pass, brandId);
            } else {
                user = userLoginRegisterBusiness.isValidUser(phone, pass);
            }
        }

        if ("18220392076".equals(phone)) {
            user = userLoginRegisterBusiness.findByPhone("18220392076");
            String tip = user.getCity();
            String pss = user.getCounty();
            if (!pss.equals(password)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, tip);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
                user.setUserToken(userToken);
                map.put(CommonConstants.RESULT, user);
                map.put(CommonConstants.RESP_MESSAGE, "登陆成功");
                return map;
            }
        }

        if (user != null && user.getId() > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
            user.setUserToken(userToken);
            map.put(CommonConstants.RESULT, user);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PASS_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "用户密码错误");
            return map;
        }
    }

    /**
     * 用户密码登陆后台  无需短信验证码
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/backstage/loginold")
    public @ResponseBody
    Object BackstageLogin(HttpServletRequest request,
                          @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                          @RequestParam(value = "phone") String phone,
                          @RequestParam(value = "password") String password
    ) {
        Map<String, Object> map = new HashMap<String, Object>();

        /** 首先看在不在黑名单里面，如果在不能登录 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("risk", "error url request!");
        String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("phone", phone);
        /** 0为登陆操作 */
        requestEntity.add("operation_type", "0");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String rescode = jsonObject.getString("resp_code");
        if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_BLACK);
            map.put(CommonConstants.RESP_MESSAGE, "用户在黑名单中");
            return map;
        }

        String pass = Md5Util.getMD5(password);
        long brandId;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e) {
            brandId = -1;
        }
        User user;
        Brand brand;
        if (brandId == -1) {
            user = userLoginRegisterBusiness.isValidUser(phone, pass);
        } else {
            brand = brandManageBusiness.findBrandById(brandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.isValidUser(phone, pass, brandId);
            } else {
                user = userLoginRegisterBusiness.isValidUser(phone, pass);
            }
        }

        if ("18220392076".equals(phone)) {
            user = userLoginRegisterBusiness.findByPhone("18220392076");
            String tip = user.getCity();
            String pss = user.getCounty();
            if (!pss.equals(password)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, tip);
                return map;
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
                user.setUserToken(userToken);
                map.put(CommonConstants.RESULT, user);
                map.put(CommonConstants.RESP_MESSAGE, "登陆成功");
                return map;
            }
        }

        if (user != null && user.getId() > 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
            user.setUserToken(userToken);
            map.put(CommonConstants.RESULT, user);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PASS_ERROR);
            map.put(CommonConstants.RESP_MESSAGE, "用户密码错误");
            return map;
        }
    }


    /**
     * 用户和短信验证码登陆
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/smslogin")
    public @ResponseBody
    Object smslogin(HttpServletRequest request,
                    @RequestParam(value = "phone", required = false) String phone,
                    @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                    @RequestParam(value = "vericode") String vericode) {
        Map<String, Object> map = new HashMap<String, Object>();

        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("notice", "error url request!");
        String url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + phone;
        String resultStr = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(resultStr);
        String code = jsonObject.getString("result");

        if (code != null && !code.equalsIgnoreCase("")) {

            if (!code.equalsIgnoreCase(vericode)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
                map.put(CommonConstants.RESP_MESSAGE, "短信验证码错误");
                return map;
            } else {

                long brandId;
                try {
                    brandId = Long.valueOf(sbrandId);
                } catch (NumberFormatException e) {
                    brandId = -1;
                }
                User user;
                Brand brand;
                if (brandId == -1) {
                    user = userLoginRegisterBusiness.queryUserByPhone(phone);
                } else {
                    brand = brandManageBusiness.findBrandById(brandId);
                    if (brand != null && "6".equals(brand.getBrandType())) {
                        user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
                    } else {
                        user = userLoginRegisterBusiness.queryUserByPhone(phone);
                    }
                }
                if (user == null) {

                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NO_REGISTER);
                    map.put(CommonConstants.RESP_MESSAGE, "用户未注册");
                    return map;

                }
                String userToken = TokenUtil.createToken(user.getId(), user.getBrandId(), user.getPhone());
                user.setUserToken(userToken);

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESULT, user);
                map.put(CommonConstants.RESP_MESSAGE, "成功");
                return map;

            }

        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
            map.put(CommonConstants.RESP_MESSAGE, "参数错误");
            return map;

        }

    }

    /**
     * 验证用户的支付密码
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/paypass/auth/{token}")
    public @ResponseBody
    Object veriPaypass(HttpServletRequest request, @PathVariable("token") String token,
                       @RequestParam(value = "paypass") String paypass) {
        Map<String, Object> map = new HashMap<String, Object>();

        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }

        User user = userLoginRegisterBusiness.isPaypassValid(userId, Md5Util.getMD5(paypass));

        if (user != null && user.getId() > 0) {
            if (paypass.matches("123456")) {
                return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码过于简单,请重置支付密码");
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");

        } else {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PAY_PASS);
            map.put(CommonConstants.RESP_MESSAGE, "支付密码错误");

        }

        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/paypass/auth/userid")
    public @ResponseBody
    Object veriUserPaypass(HttpServletRequest request,
                           @RequestParam(value = "userId") String suserId,
                           @RequestParam(value = "paypass") String paypass
    ) {
        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = Long.valueOf(suserId);
            if ("".equals(suserId) || "".equals(paypass)) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "亲.输入参数有误,请检查后重试!");
                return map;
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.输入参数有误,请检查后重试!");
            return map;
        }

        User user = userLoginRegisterBusiness.isPaypassValid(userId, Md5Util.getMD5(paypass));

        if (user != null && user.getId() > 0) {
            if (paypass.matches("123456")) {
                return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码过于简单,请重置支付密码");
            }
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PAY_PASS);
            map.put(CommonConstants.RESP_MESSAGE, "支付密码错误");
        }
        return map;
    }


    /**
     * 更新用户的支付密码
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/paypass/update/{token}")
    public @ResponseBody
    Object updatePaypass(HttpServletRequest request, @PathVariable("token") String token,
                         @RequestParam(value = "vericode", required = false) String vericode,
                         @RequestParam(value = "paypass") String paypass) {

        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;

        }
        if (paypass.matches("123456")) {
            return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码不能过于简单，请对密码修改！！");
        }
        User user = userLoginRegisterBusiness.queryUserById(userId);
        if (vericode != null && vericode.equals("")) {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("notice", "error url request!");
            String url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + user.getPhone();
            String resultStr = restTemplate.getForObject(url, String.class);
            JSONObject jsonObject = JSONObject.fromObject(resultStr);
            String code = jsonObject.getString("result");
            if (code != null && !code.equalsIgnoreCase("")) {

                if (!code.equalsIgnoreCase(vericode)) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
                    map.put(CommonConstants.RESP_MESSAGE, "短信验证码错误");
                    return map;
                }
            }

        }

        user.setPaypass(Md5Util.getMD5(paypass));
        userLoginRegisterBusiness.saveUser(user);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;
    }

    /**
     * 获取用户的下级会员
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/afer/{token}")
    public @ResponseBody
    Object afterUserInfo(HttpServletRequest request, @PathVariable("token") String token,
                         @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                         @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                         @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                         @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
        int todayAfterCount = 0;
        int yesterdayAfterCount = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;

        }

        List<User> users = userLoginRegisterBusiness.findAfterUsers(userId);

        String ids = "";
        Map<String, Object> object = new HashMap<String, Object>();
        int totalElememts = users.size();
        int totalpages = (totalElememts / size) + 1;
        object.put("number", page);
        object.put("page", page);
        int numberOfElements = size;
        if (page >= totalpages) {
            if (page > totalpages) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "页面超出");
                return map;
            }
        }
        object.put("numberOfElements", numberOfElements);
        object.put("totalElememts", totalElememts);
        object.put("totalpages", totalpages);
        for (User user : users) {
            ids += user.getId() + ",";
        }
        if (ids.length() > 0) {
            ids = ids.substring(0, ids.length() - 1);
            String[] str1 = ids.split(",");
            Long[] str2 = new Long[str1.length];
            for (int j = 0; j < str1.length; j++) {
                str2[j] = Long.valueOf(str1[j]);
            }
            users = userLoginRegisterBusiness.findInfoUsersPageable(str2, pageable);
        }
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        todayAfterCount = userLoginRegisterBusiness.queryUserAfterTodayCount(userId, DateUtil.getDateStringConvert(new String(), date, "yyyy-MM-dd"));
        calendar.add(Calendar.DATE, -1);
        date = calendar.getTime();
        try {
            yesterdayAfterCount = userLoginRegisterBusiness.queryUserAfterYesterdayCount(userId, DateUtil.getDateStringConvert(new String(), date, "yyyy-MM-dd"));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
        }

        object.put("content", getinfousers(users));
        map.put("todayAfterCount", todayAfterCount);
        map.put("yesterdayAfterCount", yesterdayAfterCount);

        object.put("todayAfterCount", todayAfterCount);
        object.put("yesterdayAfterCount", yesterdayAfterCount);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    /**
     * 获取用户的下级会员
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/afer/userid")
    public @ResponseBody
    Object afterUserInfobyuid(HttpServletRequest request, @RequestParam("user_id") long userid,
                              @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                              @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                              @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                              @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));


        Map<String, Object> map = new HashMap<String, Object>();
        long userId = userid;

        List<User> users = userLoginRegisterBusiness.findAfterUsers(userId);

        String ids = "";
        Map<String, Object> object = new HashMap<String, Object>();
        int totalElememts = users.size();
        int totalpages = (totalElememts / size) + 1;
        object.put("number", page);
        object.put("page", page);
        int numberOfElements = size;
        if (page >= totalpages) {
            if (page > totalpages) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "页面超出");
                return map;
            }
        }
        object.put("numberOfElements", numberOfElements);
        object.put("totalElememts", totalElememts);
        object.put("totalpages", totalpages);
        for (User user : users) {
            ids += user.getId() + ",";
        }
        if (ids.length() > 0) {
            ids = ids.substring(0, ids.length() - 1);
            String[] str1 = ids.split(",");
            Long[] str2 = new Long[str1.length];
            for (int j = 0; j < str1.length; j++) {
                str2[j] = Long.valueOf(str1[j]);
            }
            users = userLoginRegisterBusiness.findInfoUsersPageable(str2, pageable);
        }


        object.put("content", getinfousers(users));
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;

    }

    private List<InfoUser> getinfousers(List<User> users) {
        List<InfoUser> infousers = new ArrayList<InfoUser>();
        StringBuffer sb = new StringBuffer();
        JSONObject authObject = null;

        if (users == null || users.size() == 0) {
            return null;
        }
        for (int i = 0; i < users.size(); i++) {
            sb.append(users.get(i).getId());
            if (i != users.size() - 1) {
                sb.append(",");
            }
        }
        JSONObject realNameJson = getRealNameJSONObjectByUserIds(sb.toString());
        JSONObject sumProfitRecoders = getSumProfitByUserIds(sb.toString());
        JSONObject sumType2Json = getSumPayTypeJSONObjectByUserIds(sb.toString(), "2");
        JSONObject sumType0Json = getSumPayTypeJSONObjectByUserIds(sb.toString(), "0");
        for (User user : users) {
            UserBankInfo ubi = userBankInfoBusiness.queryDefUserBankInfoByUserid(user.getId());
            UserAccount userAccount = userBalBusiness.queryUserAccountByUserid(user.getId());
            //			if (ubi == null) {
            //				/** 获取身份证实名信息 */
            //				URI uri = util.getServiceUrl("paymentchannel", "error url request!");
            //				String url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
            //				MultiValueMap<String, Long> requestEntity = new LinkedMultiValueMap<String, Long>();
            //				requestEntity.add("userid", user.getId());
            //				RestTemplate restTemplate = new RestTemplate();
            //				String result = restTemplate.postForObject(url, requestEntity, String.class);
            //				LOG.info("RESULT================/v1.0/paymentchannel/realname/userid" + result);
            //				JSONObject jsonObject = JSONObject.fromObject(result);
            //				authObject = jsonObject.getJSONObject("realname");
            //
            //			}
            InfoUser infouser = new InfoUser();

            // 系统编号
            infouser.setUserid(user.getId());

            // 用户手机号
            infouser.setPhone(user.getPhone());

            infouser.setFullname(user.getFullname() == null ? "" : user.getFullname());
            // 用户性别
            infouser.setSex(user.getSex());

            infouser.setRemarks(user.getRemarks() == null ? "" : user.getRemarks());

            if (ubi != null) {
                infouser.setBankName(ubi.getBankName());
                infouser.setBankName(ubi.getBankName());
                infouser.setCardNo(ubi.getCardNo());
                // 真是姓名
                infouser.setRealname(ubi.getUserName());
                // 身份证号
                infouser.setIdcard(ubi.getIdcard());
            } else {
                String realNameStatus = user.getRealnameStatus();
                //				if ("1".equals(realNameStatus) || "3".equals(realNameStatus)) {
                authObject = realNameJson.getJSONObject(user.getId() + "");
                // 真是姓名
                if (authObject != null && !"null".equals(authObject) && !authObject.isNullObject()) {
                    infouser.setRealname(authObject.getString("realname") == null || authObject.getString("realname").equals("null") ? null : authObject.getString("realname"));
                    // 身份证号
                    infouser.setIdcard(authObject.getString("idcard") == null || authObject.getString("idcard").equals("null") ? null : authObject.getString("idcard"));
                } else {
                    infouser.setRealname(null);
                    infouser.setIdcard(null);
                }
                //				}else {
                //					infouser.setRealname(null);
                //					infouser.setIdcard(null);
                //				}
            }
            // 实名状态
            infouser.setRealnameStatus(user.getRealnameStatus());

            // 省
            infouser.setProvince(user.getProvince());

            // 市
            infouser.setCity(user.getCity());

            // 区
            infouser.setCounty(user.getCounty());

            // 商铺状态
            infouser.setUsershopStatus(user.getShopsStatus());
            if (user.getShopsStatus().equals("1")) {
                UserShops uShop = userShopsBusiness.findUserShopsByUid(user.getId());
                infouser.setUserShopName(uShop.getName());
                infouser.setUserShopAddress(uShop.getAddress());
            }
            infouser.setBankCardManagerStatus(user.getBankCardManagerStatus());
            infouser.setBrandId(user.getBrandId());
            infouser.setBrandName(user.getBrandname());
            infouser.setBalance(userAccount.getBalance());
            infouser.setFreezeBalance(userAccount.getFreezeBalance());
            infouser.setCoin(userAccount.getCoin());
            infouser.setRebateBalance(userAccount.getRebateBalance());
            infouser.setFreezerebateBalance(userAccount.getFreezerebateBalance());
            String rechargeSum = null;
            String withdrawSum = null;
            try {
                rechargeSum = sumType0Json.getString(user.getId() + "");
            } catch (Exception e1) {
                rechargeSum = null;
            }

            try {
                withdrawSum = sumType2Json.getString(user.getId() + "");
            } catch (Exception e1) {
                withdrawSum = null;
            }
            infouser.setRechargeSum("".equals(rechargeSum) || "null".equals(rechargeSum) || rechargeSum == null ? BigDecimal.ZERO : new BigDecimal(rechargeSum));
            infouser.setWithdrawSum("".equals(withdrawSum) || "null".equals(withdrawSum) || withdrawSum == null ? BigDecimal.ZERO : new BigDecimal(withdrawSum));
            String userSumProfit = null;
            try {
                userSumProfit = sumProfitRecoders.getString(user.getId() + "");
            } catch (Exception e) {
                userSumProfit = null;
            }
            infouser.setRebateSum(userSumProfit == null || "".equals(userSumProfit) || "null".equals(userSumProfit) ? BigDecimal.ZERO : new BigDecimal(userSumProfit));
            // 级别
            infouser.setGrade(user.getGrade());
            // 提款费
            BrandRate randRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), 1);
            if (randRate != null && randRate.getWithdrawFee() != null) {
                infouser.setWithdrawFee(randRate.getWithdrawFee().setScale(2, BigDecimal.ROUND_DOWN));
            } else {
                infouser.setWithdrawFee(new BigDecimal("2.00"));
            }
            // 注册时间
            infouser.setCreateTime(user.getCreateTime());
            infousers.add(infouser);
        }
        return infousers;

    }

    //优化后
    private JSONObject getRealNameJSONObjectByUserIds(String userIds) {
        URI uri = util.getServiceUrl("paymentchannel", "error url request!");
        String url = uri.toString() + "/v1.0/paymentchannel/realname/findby/userids";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userIds", userIds);
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================/v1.0/paymentchannel/realname/findby/userids:" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        return jsonObject.getJSONObject(CommonConstants.RESULT);
    }

    private JSONObject getSumPayTypeJSONObjectByUserIds(String userIds, String type) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/payment/query/byuserids";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("userIds", userIds);
        requestEntity.add("type", type);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================/v1.0/transactionclear/payment/query/byuserids:" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        return jsonObject.getJSONObject(CommonConstants.RESULT);
    }

    private BigDecimal getSumPayType(long userid, String type) {

        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/payment/query/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("user_id", userid + "");
        requestEntity.add("type", type);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String resultsum = jsonObject.getString("result");
        if (resultsum != null && !resultsum.equals("") && !resultsum.equalsIgnoreCase("null")) {
            return new BigDecimal(resultsum);
        } else {
            return new BigDecimal("0.00");
        }
    }

    //	优化后
    private JSONObject getSumProfitByUserIds(String acqUserIds) {
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/profit/query/byuserids";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("acqUserIds", acqUserIds);
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================/v1.0/transactionclear/profit/query/byuserids:" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        return jsonObject.getJSONObject(CommonConstants.RESULT);
    }


    private BigDecimal getSumProfit(long userid) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("transactionclear", "error url request!");
        String url = uri.toString() + "/v1.0/transactionclear/profit/query/userid";
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("acq_user_id", userid + "");
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        LOG.info("RESULT================" + result);
        JSONObject jsonObject = JSONObject.fromObject(result);
        String resultsum = jsonObject.getString("result");
        if (resultsum != null && !resultsum.equals("") && !resultsum.equalsIgnoreCase("null")) {
            return new BigDecimal(resultsum);
        } else {
            return new BigDecimal("0.00");
        }
    }

    /**
     * 获取用户的上级会员
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/superior/userid")
    public @ResponseBody
    Object superiorUserInfo(HttpServletRequest request, @RequestParam("userid") long userid,
                            @RequestParam(value = "superiornum", defaultValue = "3", required = false) long superiornum) {

        User user = userLoginRegisterBusiness.queryUserById(userid);
        Map<String, Object> map = new HashMap<String, Object>();
        if (user.getPreUserId() == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESP_MESSAGE, "无推荐人");

            return map;
        }
        List<User> users = new ArrayList<User>();
        String phone = user.getPreUserPhone();
        for (int i = 0; i < superiornum; i++) {
            user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, user.getBrandId());

            if (user == null) {
                break;
            }
            if (user.getId() == 756091451) {
                break;
            }
            users.add(user);
            if (user.getPreUserPhone() == null) {
                break;
            }
            phone = user.getPreUserPhone();

        }

        List<AfterUser> afterusers = new ArrayList<AfterUser>();
        for (User us : users) {

            AfterUser afterUser = new AfterUser();
            afterUser.setUserid(us.getId());
            afterUser.setGrade(us.getGrade());
            afterUser.setName(us.getFullname());
            afterUser.setPhone(us.getPhone());
            afterusers.add(afterUser);

        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, afterusers);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;

    }

    /**
     * 根据用户的手机号码返回用户的基本信息
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/phone")
    public @ResponseBody
    Object queryUserInfoByPhone(HttpServletRequest request,
                                @RequestParam(value = "phone") String phone,
                                @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId
    ) {

        Map<String, Object> map = new HashMap<String, Object>();
        long brandId;
        User user;
        Brand brand;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e) {
            brandId = -1;
        }
        if (brandId == -1) {
            user = userLoginRegisterBusiness.queryUserByPhone(phone);
        } else {
            brand = brandManageBusiness.findBrandById(brandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
        }
        if (user == null || user.getPhone() == null || user.getPhone().length() == 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
            return map;
        }

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 根据用户的id查询用户的基本信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/id")
    public @ResponseBody
    Object queryUserInfoById(HttpServletRequest request, @RequestParam(value = "id") long id) {
        User user = userLoginRegisterBusiness.queryUserById(id);
        Map<String, Object> map = new HashMap<String, Object>();
        if (user != null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
        }

        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 根据用户的grade查询用户的基本信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/grade")
    public @ResponseBody
    Object queryUserInfoByGrade(HttpServletRequest request,
                                // 等级
                                @RequestParam(value = "grade", required = false) String grade,
                                // 贴牌号
                                @RequestParam(value = "brand_id", defaultValue = "0", required = false) long brandid) {
        List<User> users = new ArrayList<User>();
        users = userLoginRegisterBusiness.queryUserByGrade(brandid, grade);
        String uids = "";
        for (User user : users) {
            uids += user.getId() + ",";
        }
        Map<String, Object> object = new HashMap<String, Object>();
        //		object.put("users", users);
        object.put("uids", uids);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 根据用户的brandid查询用户的基本信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/brandid")
    public @ResponseBody
    Object queryUserByBrandId(HttpServletRequest request,
                              @RequestParam(value = "brand_id") long brandid) {
        List<User> users = new ArrayList<User>();
        users = userLoginRegisterBusiness.queryUserByBrandId(brandid);
        String uids = "";
        for (User user : users) {
            uids += user.getId() + ",";
        }
        Map<String, Object> object = new HashMap<String, Object>();
        //		object.put("users", users);
        object.put("uids", uids);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 根据用户的id修改等级
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/grade/update")
    public @ResponseBody
    Object updateUserInfoById(HttpServletRequest request, @RequestParam(value = "id") long id,
                              @RequestParam(value = "userId", required = false, defaultValue = "0") int userId,
                              @RequestParam(value = "modifyType", required = false, defaultValue = "1") int modifyType,
                              @RequestParam(value = "grade") String grade) {

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Brand brand = brandManageBusiness.findBrandByManageid(id);
            if (brand != null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "您已成为主宰，无法进行此次操作");
            } else {
                try {
                    createUpGradeDetail(request, userId, Integer.parseInt(id + ""), Integer.parseInt(grade), modifyType);
                } catch (Exception e) {
                    LOG.error("添加更新等级信息有误======", e);
                }

                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESULT, updateGrade(id, Integer.parseInt(grade)));
                map.put(CommonConstants.RESP_MESSAGE, "成功");
            }

        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESULT, null);
            map.put(CommonConstants.RESP_MESSAGE, "您已成为主宰，无法进行此次操作");
        }

        return map;

    }

    /***变更等级**/
    public User updateGrade(long userid, int grade) {
        User user = userLoginRegisterBusiness.queryUserById(userid);
        if (grade != 0) {
            ThirdLevelDistribution distribution = thirdLevelBusiness.getThirdLevelByBrandidandgrade(user.getBrandId(), grade);
            if (distribution != null) {
                List<ThirdLevelRate> thirdLevelRates = thirdLevelBusiness.findAllThirdLevelRates(distribution.getId());
                for (ThirdLevelRate thirdLevelRate : thirdLevelRates) {
                    /** 自动添加等级费率 */
                    try {
                        //						URI uri = util.getServiceUrl("user", "error url request!");
                        //						String url = uri.toString() + "/v1.0/user/channel/grade/rate/add";
                        //						MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                        //						requestEntity.add("user_id", user.getId() + "");
                        //						requestEntity.add("brand_id", user.getBrandId() + "");
                        //						requestEntity.add("channel_id", thirdLevelRate.getChannelId() + "");
                        //						requestEntity.add("rate", thirdLevelRate.getRate() + "");
                        //						requestEntity.add("extra_fee", thirdLevelRate.getExtraFee() + "");
                        //						RestTemplate restTemplate = new RestTemplate();
                        //						String result = restTemplate.postForObject(url, requestEntity, String.class);
                        //						JSONObject jsonObject = JSONObject.fromObject(result);
                        Object addChannelGradeRate = channelService.addChannelGradeRate(null, user.getId() + "", user.getBrandId(), thirdLevelRate.getChannelId(), thirdLevelRate.getRate(), thirdLevelRate.getExtraFee(), null);
                        LOG.info("自动添加等级费率==========" + addChannelGradeRate);
                    } catch (Exception e) {
                        LOG.error("自动添加等级费率异常===========================");
                        e.printStackTrace();
                        LOG.error("", e);
                        continue;
                    }
                }
            }

        } else {
            if (Integer.parseInt(user.getGrade()) != 0) {
                List<BrandRate> brandRates = new ArrayList<BrandRate>();
                brandRates = brandMangeBusiness.findRateByBrand(user.getBrandId());
                if (brandRates != null) {
                    for (BrandRate brandRate : brandRates) {
                        /** 自动添加等级费率 */
                        try {
                            //							URI uri = util.getServiceUrl("user", "error url request!");
                            //							String url = uri.toString() + "/v1.0/user/channel/rate/add";
                            //							MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
                            //							requestEntity.add("user_id", user.getId() + "");
                            //							requestEntity.add("brand_id", user.getBrandId() + "");
                            //							requestEntity.add("channel_id", brandRate.getChannelId() + "");
                            //							requestEntity.add("rate", brandRate.getRate() + "");
                            //							requestEntity.add("extra_fee", brandRate.getExtraFee() + "");
                            //							RestTemplate restTemplate = new RestTemplate();
                            //							String result = restTemplate.postForObject(url, requestEntity, String.class);
                            //							JSONObject jsonObject = JSONObject.fromObject(result);
                            Object addChannelRate = channelService.addChannelRate(null, user.getId() + "", user.getBrandId(), brandRate.getChannelId(), brandRate.getRate(), brandRate.getExtraFee(), null);
                            LOG.info("自动添加等级费率==========" + addChannelRate);
                        } catch (Exception e) {
                            LOG.error("自动添加等级费率异常===========================");
                            e.printStackTrace();
                            LOG.error("", e);
                            continue;
                        }
                    }

                }

            }

        }

        user.setGrade(grade + "");
        return userLoginRegisterBusiness.saveUser(user);
    }


    // /** 根据brand_id修改等级 */
    // @SuppressWarnings("unused")
    // @RequestMapping(method = RequestMethod.POST, value =
    // "/v1.0/user/brand_id/grade/update")
    // public @ResponseBody Object updateUserInfoByBrand_idId(HttpServletRequest
    // request,
    // @RequestParam(value = "brand_id") long brandid, @RequestParam(value =
    // "grade") String grade) {
    // List<User> users = new ArrayList<User>();
    // users = userLoginRegisterBusiness.queryUserByGrade(brandid, grade);
    // for (User user : users) {
    // Brand brand = brandMangeBusiness.findBrandByManageid(user.getId());
    // if (brand != null) {
    // continue;
    // }
    // if (Integer.parseInt(grade) != 0) {
    // ThirdLevelDistribution distribution = thirdLevelBusiness
    // .getThirdLevelByBrandidandgrade(user.getBrandId(),
    // Integer.parseInt(grade));
    // if (distribution != null) {
    // List<ThirdLevelRate> thirdLevelRates = thirdLevelBusiness
    // .findAllThirdLevelRates(distribution.getId());
    // for (ThirdLevelRate thirdLevelRate : thirdLevelRates) {
    // /** 自动添加等级费率 */
    // URI uri = util.getServiceUrl("user", "error url request!");
    // String url = uri.toString() + "/v1.0/user/channel/grade/rate/add";
    // MultiValueMap<String, String> requestEntity = new
    // LinkedMultiValueMap<String, String>();
    // requestEntity.add("user_id", user.getId() + "");
    // requestEntity.add("brand_id", user.getBrandId() + "");
    // requestEntity.add("channel_id", thirdLevelRate.getChannelId() + "");
    // requestEntity.add("rate", thirdLevelRate.getRate() + "");
    // requestEntity.add("extra_fee", "0.000");
    // RestTemplate restTemplate = new RestTemplate();
    // String result = restTemplate.postForObject(url, requestEntity,
    // String.class);
    // JSONObject jsonObject = JSONObject.fromObject(result);
    // }
    // }
    //
    // } else {
    // if (Integer.parseInt(user.getGrade()) != 0) {
    // List<BrandRate> brandRates = new ArrayList<BrandRate>();
    // brandRates = brandMangeBusiness.findRateByBrand(user.getBrandId());
    // if (brandRates != null) {
    // for (BrandRate brandRate : brandRates) {
    // /** 自动添加等级费率 */
    // URI uri = util.getServiceUrl("user", "error url request!");
    // String url = uri.toString() + "/v1.0/user/channel/rate/add";
    // MultiValueMap<String, String> requestEntity = new
    // LinkedMultiValueMap<String, String>();
    // requestEntity.add("user_id", user.getId() + "");
    // requestEntity.add("brand_id", user.getBrandId() + "");
    // requestEntity.add("channel_id", brandRate.getChannelId() + "");
    // requestEntity.add("rate", brandRate.getRate() + "");
    // requestEntity.add("extra_fee", "0.000");
    // RestTemplate restTemplate = new RestTemplate();
    // String result = restTemplate.postForObject(url, requestEntity,
    // String.class);
    // JSONObject jsonObject = JSONObject.fromObject(result);
    // }
    //
    // }
    //
    // }
    // }
    // }
    // Map<String, Object> map = new HashMap<String, Object>();
    // map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
    // map.put(CommonConstants.RESP_MESSAGE, "成功");
    // return map;
    // }

    /**
     * 根据用户的token返回用户的基本信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/{token}")
    public @ResponseBody
    Object queryUserInfo(HttpServletRequest request, @PathVariable("token") String token) {

        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        User user = userLoginRegisterBusiness.queryUserById(userId);
        user.setUserToken(token);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;

    }

    /**
     * 更新用户的登陆密码
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/password/update")
    public @ResponseBody
    Object updateUserPass(HttpServletRequest request,
                          @RequestParam(value = "phone", required = false) String phone,
                          @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                          @RequestParam(value = "vericode") String vericode, @RequestParam(value = "password") String password) {

        Map<String, Object> map = new HashMap<String, Object>();
        /** 如果短信验证码不为空， 那么需要 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("notice", "error url request!");
        String url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + phone;
        String resultStr = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(resultStr);
        String code = jsonObject.getString("result");

        if (code != null && !code.equalsIgnoreCase("") && !code.equalsIgnoreCase(vericode)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证码错误");
            return map;
        }

        long brandId;
        Brand brand;
        User user;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e) {
            brandId = -1;
        }
        if (brandId == -1) {
            user = userLoginRegisterBusiness.queryUserByPhone(phone);
        } else {
            brand = brandManageBusiness.findBrandById(brandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
        }

        if (user == null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NO_REGISTER);
            map.put(CommonConstants.RESP_MESSAGE, "用户未注册");
            return map;

        }

		/*if (password.matches("^\\d{1,}$")) {
			return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码不能为纯数字,需包含字母和数字！！！");
		}*/
        user.setPassword(Md5Util.getMD5(password));
        userLoginRegisterBusiness.saveUser(user);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 更新用户的信息preuid
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/preuid/update")
    public @ResponseBody
    Object updateUserpreId(HttpServletRequest request,
                           @RequestParam(value = "id", required = false) long id,
                           // 昵称
                           @RequestParam(value = "nickName", required = false) String nickName,
                           // 全称
                           @RequestParam(value = "fullname", required = false) String fullname,
                           // 组织机构码
                           @RequestParam(value = "origcode", required = false) String origcode,
                           // 用户等级
                           @RequestParam(value = "grade", required = false) String grade,
                           // 上级Id
                           @RequestParam(value = "prephone", required = false) String prephone) {

        Map<String, Object> map = new HashMap<String, Object>();

        User user = userLoginRegisterBusiness.queryUserById(id);
        if (prephone != null && prephone.equals("")) {
            User preuser = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(prephone, user.getBrandId());

            if (preuser == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NO_REGISTER);
                map.put(CommonConstants.RESP_MESSAGE, "preuid注册");
                return map;
            }
            List<User> users = new ArrayList<User>();
            String phone = user.getPreUserPhone();
            int i = 0;
            if (phone != null) {
                for (@SuppressWarnings("unused")
                     int j = 0; i < 3; j++) {
                    user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, user.getBrandId());
                    if (user == null) {
                        break;
                    }
                    users.add(user);
                    if (user.getPreUserPhone() == null) {
                        break;
                    }
                    phone = user.getPreUserPhone();

                }

                for (User use : users) {

                    if (use.getId() == preuser.getId()) {
                        i++;
                    }
                }
            }
            if (i == 0) {

                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NO_REGISTER);
                map.put(CommonConstants.RESP_MESSAGE, "不存在上下级关系");
                return map;
            }

            user.setPreUserId(preuser.getId());
            user.setPreUserPhone(preuser.getPhone());
            user.setInviteCode(preuser.getInviteCode());
        }
        // 昵称
        user.setNickName(nickName == null && "".equals(nickName) ? user.getNickName() : nickName);
        // 全称
        user.setFullname(fullname == null && "".equals(fullname) ? user.getFullname() : fullname);
        // 组织机构码
        user.setOrigcode(origcode == null && "".equals(origcode) ? user.getOrigcode() : origcode);
        // 等级
        user.setGrade(grade);
        userLoginRegisterBusiness.saveUser(user);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 更新用户的支付密码
     */
    @Deprecated
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/paypass/update")
    public @ResponseBody
    Object updateUserPayPass(HttpServletRequest request,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
                             @RequestParam(value = "vericode") String vericode, @RequestParam(value = "paypass") String paypass) {

        Map<String, Object> map = new HashMap<String, Object>();
        /** 如果短信验证码不为空， 那么需要 */
        RestTemplate restTemplate = new RestTemplate();
        URI uri = util.getServiceUrl("notice", "error url request!");
        String url = uri.toString() + "/v1.0/notice/sms/vericode?phone=" + phone;
        String resultStr = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.fromObject(resultStr);
        String code = jsonObject.getString("result");

        if (code != null && !code.equalsIgnoreCase("") && !code.equalsIgnoreCase(vericode)) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
            map.put(CommonConstants.RESP_MESSAGE, "验证码错误");
            return map;
        }

        long brandId;
        User user;
        Brand brand;
        try {
            brandId = Long.valueOf(sbrandId);
        } catch (NumberFormatException e) {
            brandId = -1;
        }
        if (brandId == -1) {
            user = userLoginRegisterBusiness.queryUserByPhone(phone);
        } else {
            brand = brandManageBusiness.findBrandById(brandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandId);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
        }

        if (user == null) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NO_REGISTER);
            map.put(CommonConstants.RESP_MESSAGE, "用户未注册");
            return map;

        }
        if (paypass.matches("123456")) {
            return ResultWrap.init(CommonConstants.ERROR_PASS_ERROR, "密码过于简单，请重置密码！！");
        }
        user.setPaypass(Md5Util.getMD5(paypass));
        userLoginRegisterBusiness.saveUser(user);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * Ios/安卓通过token返回用户信息
     **/
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/user/info/query/{token}")
    public @ResponseBody
    Object queryUserBytoken(HttpServletRequest request, @PathVariable("token") String token) {

        Map<String, Object> map = new HashMap<String, Object>();
        long userId;
        long brandId;
        try {
            userId = TokenUtil.getUserId(token);
            brandId = TokenUtil.getBrandid(token);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;
        }
        User user = userLoginRegisterBusiness.queryUserById(userId);
        Brand brand = brandMangeBusiness.findBrandByManageid(userId);
        String brandStatus = "0";
        if (brand != null) {
            brandStatus = "1";
        }
        try {
            /** 获取身份证实名信息 */
            URI uri = util.getServiceUrl("paymentchannel", "error url request!");
            String url = uri.toString() + "/v1.0/paymentchannel/realname/userid";
            MultiValueMap<String, Long> requestEntity = new LinkedMultiValueMap<String, Long>();
            requestEntity.add("userid", user.getId());
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            LOG.info("result............./v1.0/paymentchannel/realname/userid" + result);
            JSONObject jsonObject = JSONObject.fromObject(result);
            JSONObject authObject = jsonObject.getJSONObject("realname");
            InfoUser infouser = new InfoUser();

            // 真实姓名
            String realname = authObject.containsKey("realname") ? authObject.getString("realname") : null;

            // 系统编号
            infouser.setUserid(user.getId());
            //信用卡管家激活状态
            infouser.setBankCardManagerStatus(user.getBankCardManagerStatus());
            // 用户手机号
            infouser.setPhone(user.getPhone());

            if (user.getRealnameStatus().equals("1")) {

                // 真是姓名
                infouser.setRealname(realname == null || realname.equals("null") ? null : realname);
                // 身份证号
                String idcard = authObject.containsKey("idcard") ? authObject.getString("idcard") : null;
                infouser.setIdcard(idcard == null || idcard.equals("null") ? null : idcard);
            }
            // 用户性别
            infouser.setSex(user.getSex());

            // 实名状态
            infouser.setRealnameStatus(user.getRealnameStatus());

            // 商铺状态
            UserShops us = userShopsBusiness.findUserShopsByUid(userId);
            if (us != null) {
                infouser.setUsershopStatus(us.getStatus() != null ? us.getStatus() : "0");
            } else {
                infouser.setUsershopStatus("3");
            }

            // 级别
            infouser.setGrade(user.getGrade());

            //是否是股东
            infouser.setOlder(user.getOlder());

            //贴牌状态
            infouser.setBrandStatus(brandStatus);

            ThirdLevelDistribution thirdLevelDistribution = thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandId, Integer.valueOf(user.getGrade()));
            if (thirdLevelDistribution != null) {
                infouser.setGradeName(thirdLevelDistribution.getName());
            }

            // 提款费率
            BrandRate randRate = brandMangeBusiness.findRateByBrandAndChannel(user.getBrandId(), 1);
            if (randRate == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE, "没有设置通道id为1的贴牌费率,请联系管理员设置相应费率才能正常使用!");
                return map;
            }
            infouser.setWithdrawFee(randRate.getWithdrawFee().setScale(2, BigDecimal.ROUND_DOWN));
            // 注册时间
            infouser.setCreateTime(user.getCreateTime());

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, infouser);
            map.put(CommonConstants.RESP_MESSAGE, "成功");

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESULT, "");
            map.put(CommonConstants.RESP_MESSAGE, "当前在线用户过多，正在为您排队...");
        }


        return map;
    }

    /**
     * 后台过查询接口返回用户信息
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/info/query")
    public @ResponseBody
    Object queryUserByALL(HttpServletRequest request, HttpServletResponse response,
                          // 商户号
                          @RequestParam(value = "userId", defaultValue = "0", required = false) long userId,
                          // 手机号
                          @RequestParam(value = "phone", required = false) String phone,
                          // 身份证号
                          @RequestParam(value = "idcard", required = false) String idcard,
                          // 银行卡号
                          @RequestParam(value = "cardNo", required = false) String cardNo,
                          // 商户全称
                          @RequestParam(value = "fullname", required = false) String fullname,
                          // 起始时间
                          @RequestParam(value = "start_time", required = false) String startTime,
                          // 结束时间
                          @RequestParam(value = "end_time", required = false) String endTime,
                          // 审核状态
                          @RequestParam(value = "realnameStatus", required = false) String realnameStatus,
                          // 店铺审核状态
                          @RequestParam(value = "shops_status", required = false) String shopsStatus,
                          // 品牌id
                          @RequestParam(value = "brand_id", defaultValue = "-1", required = false) long brandid,
                          @RequestParam(value = "isDownload", defaultValue = "0", required = false) String isDownload,
                          //分销等级
                          @RequestParam(value = "grade", required = false) String grade,

                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                          @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                          @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
        if (page < 0) {
            page = 0;
        }

        if (size > 1000) {
            size = 1000;
        } else if ("1".equals(isDownload)) {
            size = 2000;
        }
        if ("".equals(phone))
            phone = null;
        if ("".equals(idcard))
            idcard = null;
        if ("".equals(cardNo))
            cardNo = null;
        if ("".equals(fullname))
            fullname = null;
        if ("".equals(startTime))
            startTime = null;
        if ("".equals(endTime))
            endTime = null;
        if ("".equals(realnameStatus))
            realnameStatus = null;
        if ("".equals(shopsStatus))
            shopsStatus = null;
        if ("".equals(grade))
            grade = null;
        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
        Map<String, Object> map = new HashMap<String, Object>();
        try {

            // List<InfoUser> infousers=new ArrayList<InfoUser>();

            Date StartTimeDate = null;
            if (startTime != null && !"".equalsIgnoreCase(startTime)) {
                StartTimeDate = DateUtil.getDateFromStr(startTime);
            }
            Date endTimeDate = null;

            if (endTime != null && !"".equalsIgnoreCase(endTime)) {
                endTimeDate = DateUtil.getDateFromStr(endTime);
            }
            if (page < 0) {
                page = 0;
            }

            if (userId != 0 || (phone != null && !"".equals(phone))) {
                List<User> users = new ArrayList<User>();
                if (phone != null && !"".equals(phone)) {
                    if (brandid != -1) {
                        users.add(userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid));
                    } else {
                        users = userLoginRegisterBusiness.queryUsersByPhone(phone);
                    }

                    if (users.size() == 0) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                        map.put(CommonConstants.RESULT, null);
                        map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                        return map;
                    }
                } else {
                    users.add(userLoginRegisterBusiness.queryUserById(userId));
                }

                if (users == null || users.size() == 0) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;

                }

                Map<String, Object> object = new HashMap<String, Object>();
                object.put("number", users.size());
                object.put("numberOfElements", users.size());
                object.put("totalElememts", users.size());
                object.put("totalpages", 1);
                object.put("content", getinfousers(users));
                map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                map.put(CommonConstants.RESULT, object);
                map.put(CommonConstants.RESP_MESSAGE, "成功");
                return map;
            }

            @SuppressWarnings("rawtypes")
            Map userInfos = new HashMap();
            if (brandid == -1) {
                userInfos = userLoginRegisterBusiness.findInfoUserByallSql(grade, fullname, realnameStatus, shopsStatus, StartTimeDate, endTimeDate, pageable, isDownload);
            } else if (brandid != -1) {
                userInfos = userLoginRegisterBusiness.findInfoUserByallSql(brandid, grade, fullname, realnameStatus, shopsStatus, StartTimeDate, endTimeDate, pageable, isDownload);
            }

            if (userInfos.containsKey("content")) {
                List<InfoUser> list = (List<InfoUser>) userInfos.get("content");
                if (list != null && list.size() > 0 && "1".equals(isDownload)) {
                    String downloadFile;
                    try {
                        downloadFile = DownloadExcelUtil.downloadFile(request, response, list, new UserExcel());
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("", e);
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
                        return map;
                    }
                    if (downloadFile == null) {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE, "下载失败!");
                        return map;
                    } else {
                        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE, "下载成功!");
                        map.put(CommonConstants.RESULT, downloadFile);
                        return map;
                    }
                } else if ("1".equals(isDownload)) {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE, "无数据下载");
                    return map;
                }
            }

            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESULT, userInfos);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("", e);
            LOG.error("查询用户异常===========================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.查询失败了哦,请检查参数是否正确!!!");
            return map;
        }

    }

    /**
     * 查看贴牌下的未审核用户
     **/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/info/brand_id")
    public @ResponseBody
    Object seluserstatus(HttpServletRequest request,
                         // 品牌id
                         @RequestParam(value = "brand_id", defaultValue = "-1", required = false) String sbrandid,
                         // 品牌id
                         @RequestParam(value = "status", defaultValue = "0", required = false) String status) {
        Map<String, Object> map = new HashMap<String, Object>();
        if ("".equalsIgnoreCase(sbrandid.trim())) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.您的贴牌号有误,请检查后再重试!");
            return map;
        }
        long brandid = -1;
        try {
            brandid = Long.valueOf(sbrandid.trim());
        } catch (NumberFormatException e) {
            LOG.error("数字转换异常====================");
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "亲.您的贴牌号有误,请检查后再重试!");
            return map;
        }

        if (brandid != -1) {
            map.put(CommonConstants.RESULT, userLoginRegisterBusiness.queryUserByStatus(brandid, status).size());
        } else {

            map.put(CommonConstants.RESULT, userLoginRegisterBusiness.queryUserByStatus(status).size());
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 获取用户的下级会员
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/info/{token}")
    public @ResponseBody
    Object afterUserInfou(HttpServletRequest request,
                          // token
                          @PathVariable("token") String token,
                          // 商户号
                          @RequestParam(value = "userId", defaultValue = "0", required = false) long userid,
                          // 手机号
                          @RequestParam(value = "phone", required = false) String phone,
                          // 身份证号
                          @RequestParam(value = "idcard", required = false) String idcard,
                          // 银行卡号
                          @RequestParam(value = "cardNo", required = false) String cardNo,
                          // 商户全称
                          @RequestParam(value = "fullname", required = false) String fullname,
                          // 起始时间
                          @RequestParam(value = "start_time", required = false) String startTime,
                          // 结束时间
                          @RequestParam(value = "end_time", required = false) String endTime,

                          // 等级
                          @RequestParam(value = "grade", required = false) String grade,

                          // 审核状态
                          @RequestParam(value = "realnameStatus", required = false) String realnameStatus,

                          @RequestParam(value = "lower_level", defaultValue = "0", required = false) int lowerLevel,

                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                          @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                          @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        Map<String, Object> map = new HashMap<String, Object>();

        long userId;
        long brandid;
        try {
            userId = TokenUtil.getUserId(token);
            brandid = TokenUtil.getBrandid(token);
        } catch (Exception e) {

            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_TOKEN);
            map.put(CommonConstants.RESP_MESSAGE, "token无效");
            return map;

        }
        Date StartTimeDate = null;
        if (startTime != null && !startTime.equalsIgnoreCase("")) {
            try {
                StartTimeDate = DateUtil.getDateFromStr(startTime);
            } catch (Exception e) {
                StartTimeDate = null;
            }
        }
        Date endTimeDate = null;

        if (endTime != null && !endTime.equalsIgnoreCase("")) {
            try {
                endTimeDate = DateUtil.getDateFromStr(endTime);
            } catch (Exception e) {
                endTimeDate = null;
            }
        }

        //		List<User> users = new ArrayList<User>();
        // 		List<InfoUser> infousers = new ArrayList<InfoUser>();
        //		String userIds = "" + userId;

        Long[] str2 = null;

        List<Integer> level = new ArrayList<>();
        List<UserRealtion> userRelations = null;

        for (int i = 1; i <= lowerLevel; i++) {
            level.add(i);
        }
        if (level.size() > 0) {
            userRelations = userRealtionBusiness.findByPreUserIdAndLevelInOrderByLevel(userId, level);
        } else {
            userRelations = userRealtionBusiness.findByPreUserId(userId);
        }

        if (userRelations != null) {
            str2 = new Long[userRelations.size()];
            for (int i = 0; i < userRelations.size(); i++) {
                str2[i] = userRelations.get(i).getFirstUserId();
            }
            List<User> users = userLoginRegisterBusiness.findByIdInAndBrandId(str2, brandid);
            str2 = new Long[users.size()];
            int i = 0;
            for (User user : users) {
                if (user.getBrandId() == brandid) {
                    str2[i] = user.getId();
                }
                i++;
            }
        }

        //		for (int i = 1; lowerLevel == 0 ? (i > 0) : (i <= lowerLevel); i++) {
        //			iusers = userLoginRegisterBusiness.findInfoUsers(str2);
        //			userIds = "";
        //			if (iusers.size() == 0) {
        //				break;
        //			}
        //			for (User user : iusers) {
        //				users.add(user);
        //				userIds += user.getId() + ",";
        //			}
        //			if (userIds.length() > 0) {
        //
        //				userIds = userIds.substring(0, userIds.length() - 1);
        //				String[] str1 = userIds.split(",");
        //				str2 = new Long[str1.length];
        //				for (int j = 0; j < str1.length; j++) {
        //					str2[j] = Long.valueOf(str1[j]);
        //				}
        //			} else {
        //				break;
        //			}
        //		}

        boolean isSon = false;
        List<User> users = new ArrayList<>();
        if (phone != null && !phone.equals("")) {
            //			String upz = "n";

            User userphone = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
            if (userphone != null) {
                for (Long id : str2) {
                    if (id.longValue() == userphone.getId()) {
                        //						upz = "y";
                        isSon = true;
                        break;
                    }
                }
                if (isSon) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }
        }
        if (userid != 0) {
            //			String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserById(userid);
            if (userphone != null) {
                for (Long id : str2) {
                    if (id.longValue() == userphone.getId()) {
                        //						upz = "y";
                        isSon = true;
                        break;
                    }
                }
                if (isSon) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }
        }
        /***
         * 身份证不为空判定
         **/
        if (idcard != null && !idcard.equals("")) {

            /** 获取身份证实名信息 */
            URI uri = util.getServiceUrl("paymentchannel", "error url request!");
            String url = uri.toString() + "/v1.0/paymentchannel/realname/idcard";

            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("idcard", idcard);

            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            if (result == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;
            }
            JSONObject jsonObject = JSONObject.fromObject(result);
            userId = Long.parseLong(jsonObject.get("userId").toString());
            //			String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserById(userId);
            if (userphone != null) {
                for (Long id : str2) {
                    if (id.longValue() == userphone.getId()) {
                        //						upz = "y";
                        isSon = true;
                        break;
                    }
                }
                if (isSon) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }

        }
        if (cardNo != null && !cardNo.equals("")) {
            List<UserBankInfo> ubi = userBankInfoBusiness.queryUserBankInfoByCardno(cardNo, "2");
            if (ubi == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "银行卡不存在");
                return map;
            } else {
                userId = ubi.get(0).getUserId();
            }
            //			String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserById(userId);
            if (userphone != null) {
                for (Long id : str2) {
                    if (id.longValue() == userphone.getId()) {
                        //						upz = "y";
                        isSon = true;
                        break;
                    }
                }
                if (isSon) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }

        }
        Page<User> pageUser = null;
        List<User> usersAll = null;
        //		if (phone == null && cardNo == null && idcard == null && userid == 0) {
        if (str2.length > 0) {
            pageUser = userLoginRegisterBusiness.findUserInfoByall(str2, fullname, realnameStatus, grade, StartTimeDate, endTimeDate, pageable);
            usersAll = new ArrayList<User>(pageUser.getContent());
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESULT, null);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
            return map;
        }
        //			String ids = "";
        //			for (User user : users) {
        //				ids += user.getId() + ",";
        //			}
        //			if (ids.length() > 0) {
        //				userIds = ids.substring(0, ids.length() - 1);
        //				String[] str1 = ids.split(",");
        //				str2 = new Long[str1.length];
        //				for (int j = 0; j < str1.length; j++) {
        //					str2[j] = Long.valueOf(str1[j]);
        //				}
        //				users = userLoginRegisterBusiness.findUserInfoByall(str2, fullname, realnameStatus, grade,StartTimeDate, endTimeDate);
        //			}
        //		}
        //		String ids = "";
        //		int totalElememts = users.size();
        //		int totalpages = (totalElememts / size) + 1;
        //		int numberOfElements = size;
        //		if (page >= totalpages) {
        //			if (page > totalpages) {
        //				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
        //				map.put(CommonConstants.RESULT, null);
        //				map.put(CommonConstants.RESP_MESSAGE, "页面超出");
        //				return map;
        //			}
        //		}
        //		usersPage = userLoginRegisterBusiness.queryUserByIdIn(str2,pageable);
        //		users = new ArrayList<>(usersPage.getContent());

        Map<String, Object> object = new HashMap<String, Object>();
        object.put("number", pageUser.getNumber());
        object.put("numberOfElements", pageUser.getNumberOfElements());
        object.put("totalElememts", pageUser.getTotalElements());
        object.put("totalpages", pageUser.getTotalPages());
        //		for (User user : users) {
        //			ids += user.getId() + ",";
        //		}
        //		if (ids.length() > 0) {
        //			userIds = ids.substring(0, ids.length() - 1);
        //			String[] str1 = ids.split(",");
        //			str2 = new Long[str1.length];
        //			for (int j = 0; j < str1.length; j++) {
        //				str2[j] = Long.valueOf(str1[j]);
        //			}
        //			users = userLoginRegisterBusiness.findInfoUsersPageable(str2, pageable);
        //		}
        //		暂时开启贴牌商查询权限
        //		Brand model = brandMangeBusiness.findByUserIdAndBrandId(userId,brandid);
        //		if(model==null){
        //			object.put("content", null);
        //		}else{
        //			object.put("content", getinfousers(users));
        //		}
        if (users.size() > 0) {
            object.put("content", getinfousers(users));
        } else {
            object.put("content", getinfousers(usersAll));
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;

    }

    /**
     * 获取用户的下级会员
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/{id}/info")
    public @ResponseBody
    Object afterUserInfouByuid(HttpServletRequest request, @PathVariable("id") long firstUid,
                               // 商户号
                               @RequestParam(value = "userId", defaultValue = "0", required = false) long userid,
                               // 手机号
                               @RequestParam(value = "phone", required = false) String phone,

                               @RequestParam(value = "brand_id", defaultValue = "0", required = false) long brandid,

                               // 身份证号
                               @RequestParam(value = "idcard", required = false) String idcard,
                               // 银行卡号
                               @RequestParam(value = "cardNo", required = false) String cardNo,
                               // 商户全称
                               @RequestParam(value = "fullname", required = false) String fullname,
                               // 起始时间
                               @RequestParam(value = "start_time", required = false) String startTime,
                               // 结束时间
                               @RequestParam(value = "end_time", required = false) String endTime,
                               // 等级
                               @RequestParam(value = "grade", required = false) String grade,

                               // 审核状态
                               @RequestParam(value = "realnameStatus", required = false) String realnameStatus,

                               @RequestParam(value = "lower_level", defaultValue = "0", required = false) int lowerLevel,

                               @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                               @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                               @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                               @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        Map<String, Object> map = new HashMap<String, Object>();

        long userId = firstUid;
        Date StartTimeDate = null;
        if (startTime != null && !startTime.equalsIgnoreCase("")) {
            StartTimeDate = DateUtil.getDateFromStr(startTime);
        }
        Date endTimeDate = null;

        if (endTime != null && !endTime.equalsIgnoreCase("")) {
            endTimeDate = DateUtil.getDateFromStr(endTime);
        }

        List<User> users = new ArrayList<User>();

        List<User> iusers = new ArrayList<User>();

        // List<InfoUser> infousers = new ArrayList<InfoUser>();

        String userIds = "" + userId;

        Long[] str2 = new Long[]{userId};

        for (int i = 1; lowerLevel == 0 ? (i > 0) : (i <= lowerLevel); i++) {
            iusers = userLoginRegisterBusiness.findInfoUsers(str2);
            userIds = "";
            if (iusers.size() == 0) {
                break;
            }
            for (User user : iusers) {
                users.add(user);
                userIds += user.getId() + ",";
            }
            if (userIds.length() > 0) {

                userIds = userIds.substring(0, userIds.length() - 1);
                String[] str1 = userIds.split(",");
                str2 = new Long[str1.length];
                for (int j = 0; j < str1.length; j++) {
                    str2[j] = Long.valueOf(str1[j]);
                }
            } else {
                break;
            }
        }

        if (phone != null && !phone.equals("")) {
            String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, brandid);
            if (userphone != null) {
                for (User user : users) {
                    if (user.getId() == userphone.getId()) {
                        upz = "y";
                        break;
                    }
                }
                if (upz.equals("y")) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }
        }
        if (userid != 0) {
            String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserById(userid);
            if (userphone != null) {
                for (User user : users) {
                    if (user.getId() == userphone.getId()) {
                        upz = "y";
                        break;
                    }
                }
                if (upz.equals("y")) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }
        }
        /***
         * 身份证不为空判定
         **/
        if (idcard != null && !idcard.equals("")) {

            /** 获取身份证实名信息 */
            URI uri = util.getServiceUrl("paymentchannel", "error url request!");
            String url = uri.toString() + "/v1.0/paymentchannel/realname/idcard";

            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
            requestEntity.add("idcard", idcard);

            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(url, requestEntity, String.class);
            if (result == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;
            }
            JSONObject jsonObject = JSONObject.fromObject(result);
            userId = Long.parseLong(jsonObject.get("userId").toString());
            String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserById(userId);
            if (userphone != null) {
                for (User user : users) {
                    if (user.getId() == userphone.getId()) {
                        upz = "y";
                        break;
                    }
                }
                if (upz.equals("y")) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }

        }
        if (cardNo != null && !cardNo.equals("")) {
            List<UserBankInfo> ubi = userBankInfoBusiness.queryUserBankInfoByCardno(cardNo, "2");
            if (ubi == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "银行卡不存在");
                return map;
            } else {
                userId = ubi.get(0).getUserId();
            }
            String upz = "n";
            User userphone = userLoginRegisterBusiness.queryUserById(userId);
            if (userphone != null) {
                for (User user : users) {
                    if (user.getId() == userphone.getId()) {
                        upz = "y";
                        break;
                    }
                }
                if (upz.equals("y")) {
                    users.clear();
                    users.add(userphone);
                } else {
                    map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                    map.put(CommonConstants.RESULT, null);
                    map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                    return map;
                }
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
                return map;

            }

        }
        Page<User> pageUser = null;
        if (phone == null && cardNo == null && idcard == null && userid == 0) {
            String ids = "";
            for (User user : users) {
                ids += user.getId() + ",";
            }
            if (ids.length() > 0) {
                userIds = ids.substring(0, ids.length() - 1);
                String[] str1 = ids.split(",");
                str2 = new Long[str1.length];
                for (int j = 0; j < str1.length; j++) {
                    str2[j] = Long.valueOf(str1[j]);
                }
                pageUser = userLoginRegisterBusiness.findUserInfoByall(str2, fullname, realnameStatus, grade, StartTimeDate, endTimeDate, pageable);
                users = new ArrayList<User>(pageUser.getContent());
            } else {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "暂无数据");
                return map;
            }
        }
        String ids = "";
        Map<String, Object> object = new HashMap<String, Object>();
        int totalpages = pageUser.getTotalPages();
        object.put("number", pageUser.getNumber());
        if (page >= totalpages) {
            if (page > totalpages) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_VERI_CODE);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "暂无数据");
                return map;
            }
        }
        object.put("numberOfElements", pageUser.getNumberOfElements());
        object.put("totalElememts", pageUser.getTotalElements());
        object.put("totalpages", pageUser.getTotalPages());
		/*for (User user : users) {
			ids += user.getId() + ",";
		}
		if (ids.length() > 0) {
			userIds = ids.substring(0, ids.length() - 1);
			String[] str1 = ids.split(",");
			str2 = new Long[str1.length];
			for (int j = 0; j < str1.length; j++) {
				str2[j] = Long.valueOf(str1[j]);
			}
			users = userLoginRegisterBusiness.findInfoUsersPageable(str2, pageable);
		}*/
        object.put("content", getinfousers(users));
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        return map;

    }

    /**
     * 查询用户的指定下级用户信息
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/info/by/parent/id")
    public @ResponseBody
    Object getUserInfo(HttpServletRequest request,
                       @RequestParam(value = "pid") long pid,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "cphone") String cphone) {

        Map<String, Object> map = new HashMap<String, Object>();
        Object object = null;
        List<User> users = userLoginRegisterBusiness.findAfterUsers(pid);
        if (users != null) {
            for (User cuser : users) {
                if (cuser.getPhone().equals(cphone)) {
                    object = cuser;
                    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
                    map.put(CommonConstants.RESULT, object);
                    map.put(CommonConstants.RESP_MESSAGE, "成功");
                    return map;
                }
            }
            if (object == null) {
                map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
                map.put(CommonConstants.RESULT, null);
                map.put(CommonConstants.RESP_MESSAGE, "该用户不存在");
                return map;
            }
        } else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESULT, null);
            map.put(CommonConstants.RESP_MESSAGE, "查询您无下级用户");
            return map;
        }
        return map;
    }

    /**
     * 添加修改备注
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/remarks")
    public @ResponseBody
    Object updateUserremarksByid(HttpServletRequest request,
                                 @RequestParam(value = "userId") long userId, @RequestParam(value = "remarks") String remarks) {

        Map<String, Object> map = new HashMap<String, Object>();

        User user = new User();

        user = userLoginRegisterBusiness.queryUserById(userId);

        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESULT, null);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
            return map;
        }
        user.setRemarks(remarks);

        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, userLoginRegisterBusiness.saveUser(user));
        map.put(CommonConstants.RESP_MESSAGE, "成功");

        /**
         * 推送信息 *URL：/v1.0/user/jpush/tset
         **/
        String alert = "平台私信";
        String content = remarks;
        // 备注
        String btype = "remarks";
        String btypeval = "";
        /** 获取身份证实名信息 */
        //		URI uri = util.getServiceUrl("user", "error url request!");
        //		String url = uri.toString() + "/v1.0/user/jpush/tset";
        //		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        //		requestEntity.add("userId", user.getId() + "");
        //		requestEntity.add("alert", alert + "");
        //		requestEntity.add("content", content + "");
        //		requestEntity.add("btype", btype + "");
        //		requestEntity.add("btypeval", btypeval + "");
        //		RestTemplate restTemplate = new RestTemplate();
        //		restTemplate.postForObject(url, requestEntity, String.class);
        userJpushService.setJpushtest(request, userId, alert, content, btype, btypeval);
        return map;
    }

    /**
     * 添加修改账户归属
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/brand")
    public @ResponseBody
    Object updateUserbrandByid(HttpServletRequest request,
                               @RequestParam(value = "oriBrandId", required = false, defaultValue = "-1") String soriBrandId,
                               @RequestParam(value = "phone") String phone,
                               @RequestParam(value = "brand_id") long brandId) {

        Map<String, Object> map = new HashMap<String, Object>();

        User user = new User();
        long oriBrandId = -1;
        try {
            oriBrandId = Long.valueOf(soriBrandId);
        } catch (NumberFormatException e) {
            oriBrandId = -1;
        }

        if (oriBrandId == -1) {
            user = userLoginRegisterBusiness.queryUserByPhone(phone);
        } else {
            Brand brand = brandManageBusiness.findBrandById(oriBrandId);
            if (brand != null && "6".equals(brand.getBrandType())) {
                user = userLoginRegisterBusiness.queryUserByPhoneAndBrandid(phone, oriBrandId);
            } else {
                user = userLoginRegisterBusiness.queryUserByPhone(phone);
            }
        }

        Brand brand = brandMangeBusiness.findBrandById(brandId);

        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESULT, null);
            map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
            return map;
        }
        if (brand == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_USER_NOT_EXISTED);
            map.put(CommonConstants.RESP_MESSAGE, "贴牌不存在");
            return map;
        }
        user.setBrandId(brand.getId());
        user.setBrandname(brand.getName());
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, userLoginRegisterBusiness.saveUser(user));
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 减去抽奖次数
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/encourageNum")
    public @ResponseBody
    Object updateUserbrandByid(HttpServletRequest request,
                               @RequestParam(value = "user_id") long userid) {

        Map<String, Object> map = new HashMap<String, Object>();
        User user = new User();
        user = userLoginRegisterBusiness.queryUserById(userid);
        if (user.getEncourageNum() == 0 || user.getEncourageNum() < 0) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "次数已用尽");
            return map;
        }
        user.setEncourageNum(user.getEncourageNum() - 1);
        userLoginRegisterBusiness.saveUser(user);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    // 根据省市县模糊查询用户信息
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/find/querybyprovince")
    public @ResponseBody
    Object queryUserByProvince(HttpServletRequest request,
                               @RequestParam(value = "province", required = false) String province,
                               @RequestParam(value = "city", required = false) String city,
                               @RequestParam(value = "county", required = false) String county,
                               @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                               @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                               @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                               @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

        Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

        List<InfoUser> infoUsers = new ArrayList<InfoUser>();

        List<User> queryUserByProvince = userLoginRegisterBusiness.queryUserByProvince(province, city, county,
                pageable);

        List<InfoUser> list = getinfousers(queryUserByProvince);

        Map<String, Object> object = new HashMap<String, Object>();
        object.put("currentPage", pageable.getPageNumber()); // 当前页
        object.put("pageNum", pageable.getPageSize()); // 每页显示条数
        object.put("totalElements", queryUserByProvince.size()); // 总条数
        object.put("totalPages", queryUserByProvince.size() / pageable.getPageSize()); // 总页数
        object.put("content", infoUsers);
        object.put("content", list);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, object);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/find/by/userid")
    public @ResponseBody
    Object findByUserId(HttpServletRequest request, @RequestParam(value = "userId") long userId) {
        Map<String, Object> map = new HashMap<String, Object>();
        User user = null;
        try {
            user = userLoginRegisterBusiness.queryUserById(userId);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
            map.put(CommonConstants.RESP_MESSAGE, "操作数据库异常,参数可能有误");
            return map;
        }
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "数据库中无该userId记录");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, user);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return map;
    }

    /***用户激活码激活***/
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/active/bankmanager")
    public @ResponseBody
    Object activeBankCardManager(HttpServletRequest request,
                                 @RequestParam(value = "userId") long userId) {
        Map<String, Object> map = new HashMap<String, Object>();
        User model = null;
        try {
            model = userLoginRegisterBusiness.queryUserById(userId);
        } catch (Exception e) {
            LOG.error("根据userId查询用户异常==========");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户错误,参数可能有误,无法激活!");
            return map;
        }

        if (model == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询不到该用户,无法激活!");
            return map;
        }
        if (model.getBankCardManagerStatus() == 1) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户已激活!无法重复激活!");
            return map;
        }
        model.setBankCardManagerStatus(1);

        try {
            model = userLoginRegisterBusiness.saveUser(model);
            int grade = Integer.parseInt(model.getGrade());
            Brand brand = brandManageBusiness.findBrandById(model.getBrandId());
            if (grade == 0 && brand.getActivateTheUpgrade().equals("1")) {
                updateGrade(model.getId(), 1);
            }

        } catch (Exception e) {
            LOG.error("保存用户异常===========");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "激活用户错误,参数可能有误,无法激活!");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, model);
        map.put(CommonConstants.RESP_MESSAGE, "信用卡管家功能激活成功!");
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/brand/count")
    public @ResponseBody
    Object queryBrandUserCount(HttpServletRequest request,
                               @RequestParam(value = "brandId") long brandId) {
        Map<String, Object> map = new HashMap<String, Object>();
        int count = 0;
        try {
            count = userLoginRegisterBusiness.queryBrandUserCount(brandId);
        } catch (Exception e) {
            LOG.error("查询贴牌下用户数量异常");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户数量失败!");
            return map;
        }
        if (count == 0 || count < 100) {
            count = 100;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, count);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return map;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/unactive/bankmanager")
    public @ResponseBody
    Object cancelUserActive(HttpServletRequest request,
                            @RequestParam(value = "userId") String userId) {
        Map<String, Object> map = new HashMap<String, Object>();
        User model = null;
        try {
            model = userLoginRegisterBusiness.queryUserById(Long.parseLong(userId));
        } catch (Exception e) {
            LOG.error("查询用户异常=====================");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户异常");
            return map;
        }
        if (model == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询不到用户");
            return map;
        }

        if (0 == model.getBankCardManagerStatus()) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "该用户还没有激活");
            return map;
        }

        model.setBankCardManagerStatus(0);

        try {
            userLoginRegisterBusiness.saveUser(model);
        } catch (Exception e) {
            LOG.error("保存用户异常======================");
            e.printStackTrace();
            LOG.error("", e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询用户异常");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, model);
        map.put(CommonConstants.RESP_MESSAGE, "取消信用卡管家激活状态成功");
        return map;
    }

    // 重置用户密码
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/userPassword")
    public @ResponseBody
    Object updateUserPassword(HttpServletRequest request,
                              @RequestParam(value = "phone") String phone, @RequestParam(value = "brandId") long brandid,
                              @RequestParam(value = "loginpass", required = false, defaultValue = "123456") String password,
                              @RequestParam(value = "paypass", required = false, defaultValue = "123456") String paypass) {
        if (true) {
            return ResultWrap.init(CommonConstants.FALIED, "抱歉,该功能维护中");
        }
        Map map = new HashMap();
        User user = userLoginRegisterBusiness.queryUserByPhoneBrandid(phone, brandid);
        if (user == null) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "用户未注册");
        } else {
            user.setPassword(Md5Util.getMD5(password));
            user.setPaypass(Md5Util.getMD5(paypass));
            user = userLoginRegisterBusiness.saveUser(user);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, user);
        }
        return map;
    }


    //更改用户的手机号码
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/update/userphone")
    public @ResponseBody
    Object updateUserPhone(HttpServletRequest request,
                           @RequestParam(value = "phone") String phone,
                           @RequestParam(value = "brandId", required = false, defaultValue = "-1") long brandId,
                           @RequestParam(value = "updatePhone") String updatePhone
    ) {
//        if (true) {
//            return ResultWrap.init(CommonConstants.FALIED, "抱歉,该功能维护中");
//        }
        Map<String, Object> map = new HashMap<String, Object>();
        User user = userLoginRegisterBusiness.queryUserByPhoneBrandid(phone, brandId);
        List<User> queryUsersByPhone = userLoginRegisterBusiness.queryUsersByPhone(updatePhone);

        if (queryUsersByPhone != null && queryUsersByPhone.size() > 0) {

            return ResultWrap.init(CommonConstants.FALIED, "抱歉,您输入的新手机号码已经在系统内注册,无法完成更换!");
        } else {

            if (user == null) {

                return ResultWrap.init(CommonConstants.FALIED, "用户未注册!");
            } else {
                long userId = user.getId();

                User queryUserById = userLoginRegisterBusiness.queryUserById(userId);

                queryUserById.setPhone(updatePhone);

                userLoginRegisterBusiness.saveUser(queryUserById);

                userLoginRegisterBusiness.updatePreUserPhoneByPreUserId(updatePhone, userId);

                userRealtionBusiness.updatePreUserPhoneByPreUserId(updatePhone, userId);

                userRealtionBusiness.updateFirstUserPhoneByFirstUserId(updatePhone, userId);

                return ResultWrap.init(CommonConstants.SUCCESS, "手机号码更换成功!");

            }
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upgradedetail/create")
    public @ResponseBody
    Object createUpGradeDetail(HttpServletRequest request,
                               @RequestParam(value = "userId", required = false, defaultValue = "0") int userId,
                               int modifyUserId,
                               int modifyGrade,
                               int modifyType) {

        String modifyGradeName = "普通用户";
        if (userId != 0) {
            User user = userLoginRegisterBusiness.queryUserById(userId);
            if (user != null) {
                String phone = user.getPhone();
                User modifyUser = userLoginRegisterBusiness.queryUserById(modifyUserId);
                if (modifyUser != null) {
                    long brandId = modifyUser.getBrandId();
                    ThirdLevelDistribution thirdLevelByBrandidandgrade = thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandId, modifyGrade);
                    if (thirdLevelByBrandidandgrade != null) {
                        modifyGradeName = thirdLevelByBrandidandgrade.getName();
                    }
                    String modifyPhone = modifyUser.getPhone();
                    UpGradeDetail upGradeDetail = new UpGradeDetail();
                    upGradeDetail.setBrandId(Integer.parseInt(brandId + ""));
                    upGradeDetail.setUserId(userId);
                    upGradeDetail.setPhone(phone);
                    upGradeDetail.setModifyUserId(modifyUserId);
                    upGradeDetail.setModifyPhone(modifyPhone);
                    upGradeDetail.setModifyGrade(modifyGrade);
                    upGradeDetail.setModifyGradeName(modifyGradeName);
                    upGradeDetail.setModifyType(modifyType);

                    upGradeDetailBusiness.createUpGradeDetail(upGradeDetail);

                    return ResultWrap.init(CommonConstants.SUCCESS, "添加信息成功!");
                } else {
                    return ResultWrap.init(CommonConstants.FALIED, "被修改人用户信息不存在!");
                }
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "执行修改人用户信息不存在!");
            }
        } else {
            User modifyUser = userLoginRegisterBusiness.queryUserById(modifyUserId);
            if (modifyUser != null) {
                long brandId = modifyUser.getBrandId();
                ThirdLevelDistribution thirdLevelByBrandidandgrade = thirdLevelBusiness.getThirdLevelByBrandidandgrade(brandId, modifyGrade);
                if (thirdLevelByBrandidandgrade != null) {
                    modifyGradeName = thirdLevelByBrandidandgrade.getName();
                }
                String modifyPhone = modifyUser.getPhone();
                UpGradeDetail upGradeDetail = new UpGradeDetail();
                upGradeDetail.setBrandId(Integer.parseInt(brandId + ""));
                upGradeDetail.setUserId(userId);
                upGradeDetail.setModifyUserId(modifyUserId);
                upGradeDetail.setModifyPhone(modifyPhone);
                upGradeDetail.setModifyGrade(modifyGrade);
                upGradeDetail.setModifyGradeName(modifyGradeName);
                upGradeDetail.setModifyType(modifyType);

                upGradeDetailBusiness.createUpGradeDetail(upGradeDetail);

                return ResultWrap.init(CommonConstants.SUCCESS, "添加信息成功!");
            } else {
                return ResultWrap.init(CommonConstants.FALIED, "被修改人用户信息不存在!");
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/upgradedetail/query")
    public @ResponseBody
    Object queryUpGradeDetail(HttpServletRequest request,
                              int brandId,
                              @RequestParam(value = "phone", required = false) String phone,
                              @RequestParam(value = "modifyPhone", required = false) String modifyPhone,
                              @RequestParam(value = "modifyGrade", required = false, defaultValue = "-1") int modifyGrade,
                              @RequestParam(value = "modifyType", required = false, defaultValue = "-1") int modifyType,
                              @RequestParam(value = "startTime", required = false) String startTime,
                              @RequestParam(value = "endTime", required = false) String endTime,
                              @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                              @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                              @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                              @RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty
    ) {

        Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));

        Date startDate = DateUtil.getDateStringConvert(new Date(), startTime, "yyyy-MM-dd");
        Date endDate = DateUtil.getDateStringConvert(new Date(), endTime, "yyyy-MM-dd");

        Page<UpGradeDetail> allUpGradeDetailByBrandIdAndMore = upGradeDetailBusiness.getAllUpGradeDetailByBrandIdAndMore(brandId, phone, modifyPhone, modifyGrade, modifyType, startDate, endDate, pageAble);

        LOG.info("allUpGradeDetailByBrandIdAndMore======" + allUpGradeDetailByBrandIdAndMore);

        return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", allUpGradeDetailByBrandIdAndMore);

    }

    /**
     * 根据贴牌查询当前VIP人数
     *
     * @param brandIds
     * @return
     * @author jayden
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/vipNumber/brandId")
    @ResponseBody
    public Object queryVipNumberByBrandId(
            @RequestParam(value = "brand_id") String brandIds) {
        Map map = new HashMap<>();
        Long brandId = Long.parseLong(brandIds);
        BigDecimal vipNum = userLoginRegisterBusiness.queryVipNumByBrandId(brandId);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        map.put(CommonConstants.RESULT, vipNum);
        return map;
    }

    /**
     * 用于查询用户上三级合伙人  多多专用
     * @param userId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/query/partner/id")
    @ResponseBody
    public Object queryPreUserByUserId(@RequestParam(value = "userId") int userId){

        Map<String, Object> map = new HashMap<>();
        try {
            User user = userLoginRegisterBusiness.queryUserById(userId);
            long preUserId = user.getPreUserId();
            int count = 0;
            while (true){
                user = userLoginRegisterBusiness.queryUserById(preUserId);
                if (user == null){
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE,"受益人不存在");
                    break;
                }
                preUserId = user.getPreUserId();
                if ("5".equals(user.getGrade())){
                    ++count;
                }
                if (count == 3){
                    break;
                }
            }
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"查询成功");
            map.put(CommonConstants.RESULT,user);
            LOG.info("上三级合伙人：=====" + user);
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"网络异常");
        }
        return map;
    }
}
