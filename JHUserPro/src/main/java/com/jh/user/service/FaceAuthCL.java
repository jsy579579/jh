package com.jh.user.service;

import cn.jh.common.utils.CommonConstants;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.pojo.User;
import com.jh.user.util.IpAddress;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@Configuration
@Component
public class FaceAuthCL {

    private static final Logger LOG = LoggerFactory.getLogger(FaceAuthCL.class);

    private static final String appId="FWrvUdXP";

    private static final String appKey="abrvd8WD";

    private static String downloadPath;

    @Value("${user.realname.downloadpath}")
    public void setdownloadPath(String downloadpath){
        downloadPath=downloadpath;
    }

    //百也特
    //private static final String readPath="http://101.132.160.107:8888/realname/";

    //易百管家
    //private static final String readPath="http://47.102.140.177:8888/realname/";
    private static final String readPath= IpAddress.getIpAddress()+"/realname/";

    //private static final String readPath=downloadPath+"/realname/";

    private static final String writePath="/usr/share/nginx/html/realname/";

    private static String url="https://api.253.com/open/i/witness/witness-check-plus";

    @Autowired
    private UserRealnameAuthService userRealnameAuthService;

    @Autowired
    private UserLoginRegisterBusiness userLoginRegisterBusiness;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.POST,value="/v1.0/user/realname/faceAuthCL")
    @ResponseBody
    public Map FaceAuth(HttpServletRequest request,
                        @RequestParam(value="phone") String phone
    )  {
        // 通过手机号查询到用户
        User user = userLoginRegisterBusiness.queryUserByPhone(phone);
        phone=user.getPhone();
        String brandId=user.getBrandId()+"";
        Map map=new HashMap<String,Object>();
        //上传图片至服务器
//        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//        List<MultipartFile> files=multipartRequest.getFiles("images");
//        System.out.println(files);
//        File file=new File(writePath+phone);
//        if(!file.exists()){
//            file.mkdir();
//        }
//        if (files != null && files.size() > 0) {
//            for (MultipartFile mf : files) {
//                String filename = mf.getOriginalFilename();
//                System.out.println("文件名====="+filename);
//                //String substring = filename.substring(filename.lastIndexOf("."));
//                File dest = new File(writePath + phone+"/"+filename);
//                System.out.println(dest);
//                try {
//                    if(!dest.exists()){
//                        mf.transferTo(dest);
//                        Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
//                    }
//                } catch (Exception e) {
//                    LOG.error("保存实名图片出错啦======");
//                    e.printStackTrace();
//
//                    return ResultWrap.init(CommonConstants.FALIED, "保存实名图片失败!");
//                }
//
//            }
//        }
        // 1.调用身份信息校验api
        final JSONObject jsonObject = invokeShenfen(phone,brandId);

        // 2.处理返回结果
        try {
            if (jsonObject != null) {
                //响应code码。200000：成功，其他失败
                System.out.println("响应内容===="+jsonObject);
                String code = jsonObject.getString("code");
                if ("200000".equals(code) && jsonObject.get("data") != null) {
                    // 调用成功
                    // 解析结果数据，进行业务处理
                    // 校验状态码  000000：成功，其他失败
                    //String value = jsonObject.get("data").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                    JSONObject data=jsonObject.getJSONObject("data");
                    //人证比对分数80以上为认证通过
                    String faceMatchDataValue = data.getJSONObject("faceMatchData").getString("score");
                    LOG.info("调用成功,人证匹配结果value:" + faceMatchDataValue);
                    BigDecimal faceMatchScore=new BigDecimal(faceMatchDataValue);
                    //活体检测的分值，大于87分可判断为活体
                    String livingFaceData = data.getJSONObject("livingFaceData").getString("score");
                    BigDecimal livingFaceScore=new BigDecimal(livingFaceData);
                    //身份证识别状态
                    String authStatusData = data.getJSONObject("idCardAuthData").getString("code");
                    JSONObject ocrIdCardData=data.getJSONObject("ocrIdCardData");
                    LOG.info("ocr识别结果===="+ocrIdCardData);
                    String name=ocrIdCardData.getString("name");
                    String idNum=ocrIdCardData.getString("cardNum").toUpperCase();
                    if(faceMatchScore.compareTo(new BigDecimal("80"))>0 &&
                            livingFaceScore.compareTo(new BigDecimal("87"))>0 && "0".equals(authStatusData)){
                        String url = "http://paymentchannel/v1.0/paymentchannel/realname/add/realname";
                        LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
                        requestEntity.add("userId", user.getId()+"");
                        requestEntity.add("realname", name);
                        requestEntity.add("idCard", idNum);
                        requestEntity.add("result", "1");
                        requestEntity.add("message", "匹配");
                        restTemplate.postForEntity(url, requestEntity, String.class);
                        userRealnameAuthService.updateRealnameStatus(request, user.getBrandId()+"", user.getPhone(), "1");

                        map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
                        map.put(CommonConstants.RESP_MESSAGE,"实名认证成功");
                        return map;
                    }else{
                        map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                        map.put(CommonConstants.RESP_MESSAGE,"识别失败");
                        return map;
                    }
                } else {
                    // 记录错误日志，正式项目中请换成log打印
                    LOG.info("调用失败,code:" + code + ",msg:" + jsonObject.getString("message"));
                    map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                    map.put(CommonConstants.RESP_MESSAGE,"识别失败");
                    return map;
                }
            }else{
                map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
                map.put(CommonConstants.RESP_MESSAGE,"识别失败");
                return map;
            }
        } catch (Exception e) {
            map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE,"识别失败");
            return map;
        }

    }

    private static JSONObject invokeShenfen(String phone,String brandId)  {

//        BASE64Encoder encoder = new BASE64Encoder();
//        byte[] liveImageBytes=liveImage.getBytes("UTF-8");
//        String liveImageBytesBase64=encoder.encode(liveImageBytes);

//        byte[] idCardImageBytes=idCardImage.getBytes("UTF-8");
//        String idCardImageBytesBase64=encoder.encode(idCardImageBytes);
        JSONObject jsonObject=new JSONObject();
        RestTemplate restTemplate=new RestTemplate();
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("appId",appId);
        requestEntity.add("appKey",appKey);
        requestEntity.add("liveImage",readPath+brandId+"/"+phone+"/liveImage.jpg");
        requestEntity.add("idCardImage",readPath+brandId+"/"+phone+"/idCardImage.jpg");
        requestEntity.add("imageType","URL");
        try {
            String data=restTemplate.postForObject(url,requestEntity,String.class);
            return jsonObject.fromObject(data);
        } catch (RestClientException e) {
            return null;
        }
    }

}
