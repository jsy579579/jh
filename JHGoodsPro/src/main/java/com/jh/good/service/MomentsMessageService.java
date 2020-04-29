package com.jh.good.service;
import	java.nio.file.Path;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.TokenUtil;
import com.jh.good.business.MomentsMessageBusiness;
import com.jh.good.pojo.MomentsMessage;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class MomentsMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MomentsMessageService.class);

    @Autowired
    MomentsMessageBusiness momentsMessageBusiness;

    @ApiOperation("分页查询咨询")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/momentsMessage/searchMomentsMessage")
    @ResponseBody
    public Map searchGoods(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,   //当前页
            @RequestParam(value = "size", defaultValue = "20", required = false) int size //每页显示的条数
            ) {
        Map map = new HashMap();
        Page<MomentsMessage> momentsMessages = momentsMessageBusiness.searchGoods(page, size);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, momentsMessages);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    @ApiOperation("官方发布咨询")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/momentsMessage/publishNews")
    @ResponseBody
    public Object publishNews(@RequestBody MomentsMessage momentsMessage) {
        Map map = new HashMap();
        momentsMessageBusiness.publishNews(momentsMessage);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "发布咨询成功");
        return map;
    }

    @ApiOperation("用户点赞")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/momentsMessage/giveTheThumbsUp/{token}")
    @ResponseBody
    public Object giveTheThumbsUp(@PathVariable("token") String token,
                              @RequestParam("momentsId") String momentsId) {
        Long userId;
        try {
            userId = TokenUtil.getUserId(token);
        } catch (Exception e) {
            LOG.error("=========={token}传入有误===========" + e);
            return "error";
        }
        Map map = new HashMap();
        momentsMessageBusiness.giveTheThumbsUp(userId,Long.valueOf(momentsId));
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "发布咨询成功");
        return map;
    }

}
