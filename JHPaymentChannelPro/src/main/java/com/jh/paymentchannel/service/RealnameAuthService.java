package com.jh.paymentchannel.service;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.RealnameAuthBusiness;
import com.jh.paymentchannel.pojo.RealNameAuth;
import com.jh.paymentchannel.repository.RealnameAuthRepository;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.utils.AuthorizationHandle;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.TokenUtil;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class RealnameAuthService {

	
	private static final Logger LOG = LoggerFactory.getLogger(RealnameAuthService.class);
	
	@Autowired
	private RealnameAuthBusiness  realnameAuthBusiness;
	
	@Autowired
    private RealnameAuthRepository realnameAuthRepository;
	
	 @Autowired
	 Util util;
	
	/**实名验证**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/auth/{token}")
	public @ResponseBody Object realnameAuth(HttpServletRequest request, 
			@RequestParam(value = "realname") String realname,
			@RequestParam(value = "idcard") String idcard,
			@PathVariable("token") String token){
		
		Map<String,Object> map = new HashMap<>();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		String birthDate;
		try {
			birthDate = idcard.substring(6,10);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您的身份证号有误,请重新输入!");
			return map;
		}
		int birthYear = Integer.valueOf(birthDate);
		int nowYear = Integer.valueOf(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy"));
		if(nowYear-birthYear < 18 || nowYear-birthYear > 80){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "很抱歉,仅对年龄18岁~80岁用户进行实名认证!");
			return map;
		}
		
		realname=realname.trim();
		idcard=idcard.trim();
//		RealNameAuth realNameAuth=realnameAuthBusiness.findRealNamesByIdcard(idcard);
		RealNameAuth realNameAuth=realnameAuthBusiness.findRealNamesAuthByUserId(userId);
		

		
		if(realNameAuth!=null&&realNameAuth.getUserId()!=userId){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "你的证件号已在其他账户认证");
			return map;
		}
		
		realNameAuth=realnameAuthBusiness.findRealNamesByall(userId, idcard, realname);
		if(realNameAuth!=null){
			map.put(CommonConstants.RESULT,  realNameAuth);
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "接口返回成功");
			return map;
		}
		realNameAuth=realnameAuthBusiness.findRealNamesAuthByUsserId(userId);
		if(realNameAuth!=null&&realNameAuth.getAuthTime()!=null){
			long starttime=realNameAuth.getAuthTime().getTime();
			long endtime=System.currentTimeMillis();
			starttime=starttime+60*10*1000;
			if(starttime>endtime){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "操作过于频繁，请稍后再试!");
				return map;
			}
		}
		
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT,  realnameAuthBusiness.realNameAuth(realname, idcard.toUpperCase(), userId));
		map.put(CommonConstants.RESP_MESSAGE, "接口返回成功");
		return map;
	
	}
	
	/**实名验证**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/auth/backstage")
	public @ResponseBody Object BackstagerealnameAuth(HttpServletRequest request, 
			@RequestParam(value = "userid") long userId,
			@RequestParam(value = "realname") String realname,
			@RequestParam(value = "idcard") String idcard){
		
		Map<String,Object> map = new HashMap<>();
		if(userId!=0){
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/query/id";
			/**根据的用户userid查询用户的基本信息*/
			MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
			requestEntity.add("id", userId+"");
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================"+result);
			JSONObject jsonObject =  JSONObject.fromObject(result);
			JSONObject resultObj  =  jsonObject.getJSONObject("result");
			if(resultObj==null){
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
				return map;
			}
			if(resultObj.containsKey("id")){
			}else{
				map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
				return map;
			}	
		}else{
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "用户不存在");
			return map;
		}	
		realname=realname.trim();
		idcard=idcard.trim();
		
//		RealNameAuth realNameAuth=realnameAuthBusiness.findRealNamesByIdcard(idcard);
		RealNameAuth realNameAuth=realnameAuthBusiness.findRealNamesAuthByUserId(userId);
		
		if(realNameAuth!=null&&realNameAuth.getUserId()!=userId){
			map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "你的证件号已在其他账户认证");
			return map;
		}
		
		realNameAuth=realnameAuthBusiness.findRealNamesByall(userId, idcard, realname);
		if(realNameAuth!=null){
			map.put(CommonConstants.RESULT,  realNameAuth);
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "接口返回成功");
			return map;
		}
		realNameAuth=realnameAuthBusiness.findRealNamesAuthByUsserId(userId);
		if(realNameAuth!=null&&realNameAuth.getAuthTime()!=null){
			long starttime=realNameAuth.getAuthTime().getTime();
			long endtime=System.currentTimeMillis();
			starttime=starttime+60*10*1000;
			if(starttime>endtime){
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "请求过于频繁，请稍后再试!");
				return map;
			}
		}
		
		map.put(CommonConstants.RESULT,  realnameAuthBusiness.realNameAuth(realname, idcard, userId));
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "接口返回成功");
		return map;
		
	
	}
	
	
	/**根据身份证号查询*/
	@Deprecated
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/idcard")
	public @ResponseBody Object realNameByIdcard(HttpServletRequest request, 
			@RequestParam(value = "idcard") String idcard){
//		return realnameAuthBusiness.findRealNamesByIdcard(idcard);
		return null;
	
	}
	
	
	
	/**根据姓名查询*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/name")
	public @ResponseBody Object realNameByName(HttpServletRequest request, 
			@RequestParam(value = "realname") String realname){
		return realnameAuthBusiness.findRealNamesAuthByName(realname);
	
	}
	
	/**根据后台连接用userId查询*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/userid")
	public @ResponseBody Object realNameByUserId(HttpServletRequest request, 
			@RequestParam(value = "userid") long userid){
		Map map = new HashMap();
		map.put("realname", realnameAuthBusiness.findRealNamesAuthByUsserId(userid));
		return map ;
		
	}
	/**根据userId查询*/
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/realname/{token}")
	public @ResponseBody Object realNameBytoken(HttpServletRequest request, 
			@PathVariable("token") String token){
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;
		}
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, realnameAuthBusiness.findRealNamesAuthByUsserId(userId));
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map ;
	
	}
	
	/**查询所有的记录分页显示*/
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/realname/all")
	public @ResponseBody Object allRealNameAuth(HttpServletRequest request, 
    @RequestParam(value = "page", defaultValue = "0", required = false) int page,
	@RequestParam(value = "size", defaultValue = "20", required = false) int size,
	@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
	@RequestParam(value = "sort", defaultValue = "authTime", required = false) String sortProperty){
	
		Pageable pageable = new PageRequest(page, size, new Sort(direction,sortProperty));
		return realnameAuthBusiness.findAllRealnames(pageable);
		
	
	}
	
	//根据userid注销用户的实名信息
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/delrealname")
	@ResponseBody
	public Object delRealnameByUserid(HttpServletRequest request,
			@RequestParam(value = "userId") long userid) {
		Map map = new HashMap();
		try {
			realnameAuthBusiness.delRealnameByUserid(userid);
		} catch (Exception e) {
			LOG.info("",e);
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/findby/userids")
	public @ResponseBody Object getRealNameByUserIds(HttpServletRequest request,
			@RequestParam("userIds")String[] suserIds
			){
		Map<String,Object>map = new HashMap<>();
		Map<String,Object>realNameMap = new HashMap<>();
		long [] userIds = null;
		if(suserIds.length>0){
			userIds = new long[suserIds.length];
			for(int i = 0;i < suserIds.length;i++){
				userIds[i] = Long.valueOf(suserIds[i]);
			}
		}
		List<RealNameAuth> models = realnameAuthBusiness.findRealNamesAuthByUserIds(userIds);
		if(models!=null&&models.size()!=0){
			for(int i = 0;i < models.size();i++){
				realNameMap.put(models.get(i).getUserId()+"", models.get(i));
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, realNameMap);
		return map;
	}

	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/find/userids/and/date")
	public @ResponseBody Object getRealNameByUserIdsAndTime(HttpServletRequest request,
													 @RequestParam("userIds")String[] suserIds,
													@RequestParam("date")String date
	){
		Map<String,Object>map = new HashMap<>();
		long [] userIds = null;
		if(suserIds.length>0){
			userIds = new long[suserIds.length];
			for(int i = 0;i < suserIds.length;i++){
				String substring = suserIds[i].substring(1, suserIds[i].length() - 1);
				userIds[i] = Long.valueOf(substring);
			}
		}
		List<RealNameAuth> realNameIds = realnameAuthBusiness.findRealNamesAuthByUserIdsAndDate(userIds,date);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, realNameIds.size());
		return map;
	}
	
	//添加实名用户信息
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/add/realname")
	public @ResponseBody Object addRealnameAuth(HttpServletRequest request,
			@RequestParam(value="userId") long userId,
			@RequestParam(value="realname") String realname,
			@RequestParam(value="idCard") String idcard,
			@RequestParam(value="result",defaultValue="3",required=false) String result,
			@RequestParam(value="message") String message
			){
			Map<String,Object> map = new HashMap<>();
			try {
				 RealNameAuth realnameAuth = realnameAuthBusiness.findRealnameAuthById(userId);
				 RealNameAuth realnameAuths = new  RealNameAuth();
				   if(realnameAuth!=null){
					   realnameAuthRepository.delete(realnameAuth);
				   }
					   realnameAuths.setAuthTime(new Date());
					   realnameAuths.setIdcard(idcard);
					   realnameAuths.setMessage(message);
					   realnameAuths.setRealname(realname);
					   realnameAuths.setResult(result);
					   realnameAuths.setUserId(userId);
					   realnameAuthBusiness.addRealnameAuth(realnameAuths);
					   map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					   map.put(CommonConstants.RESP_MESSAGE, "添加成功");
			} catch (Exception e) {
				LOG.error("",e);
			}
			return map;	
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/update/realname")
	public @ResponseBody Object setRealnameResult(HttpServletRequest request,
		   @RequestParam(value="userId") long userId,
		   @RequestParam(value="result") String result){
			Map map = new HashMap();
			RealNameAuth realnameAuth = realnameAuthBusiness.findRealnameAuthById(userId);
			if(realnameAuth !=null){
				 realnameAuthBusiness.updateRealnameMsg(userId,result);	
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE,"修改成功");
			}else{
				 map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				  map.put(CommonConstants.RESP_MESSAGE, "查询失败");
			}
				
		return map;
	}

    /**
     * 根据日期查询实名人数
     * @param request
     * @param suserIds
     * @param date
     * @return
     */
    @RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/find/userids/and/createtime/new")
    public @ResponseBody Object getRealNameByUserIdsAndTimeNew(HttpServletRequest request,
                                                               @RequestParam("userIds")String[] suserIds,
                                                               @RequestParam("date")String date
    ){
        Map<String,Object>map = new HashMap<>();
        System.out.println("==================================================" + suserIds.length);
        List<RealNameAuth> realNameIds = null;
        try {
            Long [] userIds = null;
            if(suserIds.length > 1){
                userIds = new Long[suserIds.length];
                for(int i = 0;i < suserIds.length;i++){
                    if (i==0 || i== suserIds.length - 1){
                        String substring = suserIds[i].substring(1, suserIds[i].length() - 1);
                        userIds[i] = Long.valueOf(substring);
                    }else{
                        userIds[i] = Long.valueOf(suserIds[i]);
                    }
                }
                realNameIds = realnameAuthBusiness.findRealNamesAuthByUserIdsAndDateNew(userIds,"匹配",date);
                map.put(CommonConstants.RESULT, realNameIds.size());
            }else if(suserIds.length == 1){
                for(String str :suserIds){
                    if (str.equals("[]")){
                        map.put(CommonConstants.RESULT, 0);
                    }else {
                        userIds = new Long[suserIds.length];
                        for(int i = 0;i < suserIds.length;i++){
                            if (i==0 || i== suserIds.length - 1){
                                String substring = suserIds[i].substring(1, suserIds[i].length() - 1);
                                userIds[i] = Long.valueOf(substring);
                            }else{
                                userIds[i] = Long.valueOf(suserIds[i]);
                            }
                        }
                        realNameIds = realnameAuthBusiness.findRealNamesAuthByUserIdsAndDateNew(userIds,"匹配",date);
                        map.put(CommonConstants.RESULT, realNameIds.size());
                    }
                }
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "查询异常");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        return map;
    }

    //查询实名人数
    @RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/realname/findRealNameCounts/userids")
    public @ResponseBody Object getRealNameCountsByUserIds(HttpServletRequest request,
                                                     @RequestParam("userIds")String[] suserIds
    ){
        Map<String,Object>map = new HashMap<>();
        Map<String,Object>realNameMap = new HashMap<>();
        int realNameCounts = 0;
        Long [] userIds = null;
        if(suserIds.length > 1){
            userIds = new Long[suserIds.length];
            for(int i = 0;i < suserIds.length;i++){
                if (i==0 || i== suserIds.length - 1){
                    String substring = suserIds[i].substring(1, suserIds[i].length() - 1);
                    userIds[i] = Long.valueOf(substring);
                }else{
                    userIds[i] = Long.valueOf(suserIds[i]);
                }
            }
            realNameCounts = realnameAuthBusiness.findRealNameCountsByUserIds(userIds, "匹配");
        }else if(suserIds.length == 1){
            for(String str :suserIds){
                if (str.equals("[]")){
                    map.put(CommonConstants.RESULT, 0);
                }else {
                    userIds = new Long[suserIds.length];
                    for(int i = 0;i < suserIds.length;i++){
                        if (i==0 || i== suserIds.length - 1){
                            String substring = suserIds[i].substring(1, suserIds[i].length() - 1);
                            userIds[i] = Long.valueOf(substring);
                        }else{
                            userIds[i] = Long.valueOf(suserIds[i]);
                        }
                    }
                    realNameCounts = realnameAuthBusiness.findRealNameCountsByUserIds(userIds, "匹配");

                }
            }
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "查询成功");
        map.put(CommonConstants.RESULT, realNameCounts);
        return map;
    }



	
}
