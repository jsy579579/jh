package com.jh.good.service;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.UUIDGenerator;
import com.jh.good.business.GoodsBusiness;
import com.jh.good.pojo.Goods;
import com.jh.good.util.Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "商品相关接口", description = "提供商品相关的 Rest API")
@Controller
@EnableAutoConfiguration
public class GoodsService {
    private static final Logger LOG = LoggerFactory.getLogger(GoodsService.class);

    @Autowired
    private GoodsBusiness goodsBusiness;

    @Autowired
    Util util;

    /**
     * 查询全部商品
     * @return
     */
    @ApiOperation("查询全部商品接口")
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/good/goods/findAll")
    @ResponseBody
    public Map findAll() {
        Map map = new HashMap();
        List<Goods> list = goodsBusiness.findAll();
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, list);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     *  分页带搜索商品
     */
    @ApiOperation("分页带搜索商品")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/goods/searchGoods")
    @ResponseBody
    public Map searchGoods(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,   //当前页
            @RequestParam(value = "size", defaultValue = "20", required = false) int size,  //每页显示的条数
            @RequestParam(value = "goodsName", required = false) String goodsName,      // 商品名
            @RequestParam(value = "category1Id", required = false) Long category1Id){   // 分类id
        Map map = new HashMap();
        Page<Goods> goodsPage= goodsBusiness.searchGoods(page,size,goodsName,category1Id);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, goodsPage);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 新增商品
     */
    @ApiOperation("新增商品")
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/goods/save")
    @ResponseBody
    public Map searchGoods(@RequestBody Goods goods){
        Map map = new HashMap();
        goodsBusiness.save(goods);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESP_MESSAGE, "新增成功");
        return map;
    }

    @ApiOperation("根据id查询单个商品")
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/good/goods/findById/{id}")
    @ResponseBody
    public Map searchGoods(@PathVariable("id") Long id){
        Map map = new HashMap();
        Goods goods = goodsBusiness.findById(id);
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, goods);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 测试用的
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1.0/good/goods/outlook")
    @ResponseBody
    public Map outlook(){
        Map map = new HashMap();
        URI uri = util.getServiceUrl("paymentchannel", "error url request!");
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, uri.toString());
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }

    /**
     * 商品上下架 0上架 1下架
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1.0/good/goods/isMarketable")
    @ResponseBody
    public Object isMarketable(@RequestParam("goodsId") Long goodsId,
                               @RequestParam("isMarketable") String isMarketable){
        Map map = new HashMap();
        if(("0").equals(isMarketable) || ("1").equals(isMarketable) ){
            goodsBusiness.isMarketable(goodsId,isMarketable);
            map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
            map.put(CommonConstants.RESP_MESSAGE, "成功");
            return map;
        }else {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "参数传入有误!");
            return map;
        }
    }

    /**
     * 商品图片上传
     */
    @RequestMapping("/v1.0/good/goods/upload")
    @ResponseBody
    public Object uploadPic(MultipartFile file) {
        Map map = new HashMap();
        if (file.isEmpty()) {
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "请选择图片");
            return map;
        }
        //获取文件名称
        String originalFilename = file.getOriginalFilename();
        //截取文件扩展名
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        String fileName = UUIDGenerator.getDateTimeOrderCode() + "." + extName;
        String uploadpath = "/usr/share/nginx/html/images/";
        String downloadpath = "http://47.102.98.217:8888/images/";
        try {
            File dest = new File(uploadpath + fileName);
            if (dest.exists()) {
                dest.delete();
            }
            file.transferTo(dest);
            Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
        }catch (Exception e){
            LOG.error("图片上传出现错误!" + e);
            map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            map.put(CommonConstants.RESP_MESSAGE, "图片上传出现错误");
            return map;
        }
        map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
        map.put(CommonConstants.RESULT, downloadpath + fileName);
        map.put(CommonConstants.RESP_MESSAGE, "成功");
        return map;
    }
}
