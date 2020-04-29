package com.jh.mircomall.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jh.mircomall.bean.BusinessOrder;
import com.jh.mircomall.bean.Goods;
import com.jh.mircomall.bean.GoodsParent;

import com.jh.mircomall.service.BusinessManagementService;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
@RequestMapping("/v1.0/integralmall/businessManagement")
public class BusinessManagementController {

	private static final Logger log = LoggerFactory.getLogger(BusinessManagementController.class);

	private static final String[] files = null;

	@Autowired
	private BusinessManagementService businessManagementService;

	@Value("${user.realname.uploadpath}")
	private String realnamePic;

	@Value("${user.realname.downloadpath}")
	private String url;

	/**
	 * 商品添加
	 * 
	 * @param request
	 * @param goodsName
	 * @param goodsContext
	 * @param goodsCoin
	 * @param goodsNum
	 * @param oodsgTypeId
	 * @param businessId
	 * @param goodsPrice
	 * @param files
	 * @param filesd
	 * @return
	 * @throws UnsupportedEncodingException
	 *
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/addgoods")
	public @ResponseBody Object uploadGoods(HttpServletRequest request, @RequestParam("goodsName") String goodsName,
			@RequestParam("goodsContext") String goodsContext, @RequestParam("goodsCoin") Integer goodsCoin,
			@RequestParam("goodsNum") Integer goodsNum, @RequestParam("oodsgTypeId") Integer oodsgTypeId,
			@RequestParam("businessId") Integer businessId, @RequestParam("goodsPrice") BigDecimal goodsPrice,
			@RequestParam("originalPrice") BigDecimal originalPrice, @RequestParam("files") MultipartFile[] files,
			@RequestParam("filesd") MultipartFile[] filesd) throws UnsupportedEncodingException {
		String goods_Name = URLDecoder.decode(goodsName, "UTF-8");
		String goods_Context = URLDecoder.decode(goodsContext, "UTF-8");
		Goods goods = new Goods();
		goods.setBusinessId(businessId);
		goods.setGoodsCoin(goodsCoin);
		goods.setGoodsContext(goods_Context);
		goods.setGoodsName(goods_Name);
		goods.setGoodsNum(goodsNum);
		goods.setOodsgTypeId(oodsgTypeId);
		goods.setGoodsPrice(goodsPrice);
		goods.setOriginalPrice(originalPrice);
		Map map = new HashMap();
		// 开始上传图片
		int i = businessManagementService.GoodsUpload(goods);
		if (i > 0) {
			int goodsid = goods.getId();
			File dest = null;
			File details = null;
			String src = businessId + "";
			File dir = new File(realnamePic + src);
			// 创建目录
			if (dir.mkdirs()) {
				System.out.println("创建目录" + realnamePic + src + ":成功！");

			} else {
				File[] tempfiles = dir.listFiles();
				for (File file : tempfiles) {
				}

				System.out.println("创建目录" + realnamePic + src + ":失败，目标已存在！");
			}

			if (files != null && files.length > 0) {
				for (int j = 0; j < files.length; j++) {
					String fileName = files[j].getOriginalFilename();// 获取文件名加后缀
					String fileF = fileName.substring(fileName.lastIndexOf(".") + 1);// 文件后缀
					log.info("====文件后缀名：" + fileF);
					fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + "." + fileF;// 新的文件名
					dest = new File(realnamePic + src + "/" + fileName);
					try {
						files[j].transferTo(dest);
					} catch (IllegalStateException e) {
						log.info(e.getMessage());
					} catch (IOException e) {
						log.info(e.getMessage());
					}
				}
			}
			/* 上传商品详情长图 */
			if (filesd != null && filesd.length > 0) {
				for (int j = 0; j < filesd.length; j++) {
					String fileName = filesd[j].getOriginalFilename();// 获取文件名加后缀
					String fileF = fileName.substring(fileName.lastIndexOf(".") + 1);// 文件后缀
					log.info("====文件后缀名：" + fileF);
					fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + "." + fileF;// 新的文件名
					details = new File(realnamePic + src + "/" + fileName);
					try {
						filesd[j].transferTo(details);
					} catch (IllegalStateException e) {
						log.info(e.getMessage());
					} catch (IOException e) {
						log.info(e.getMessage());
					}
				}
			}
			goods.setId(goodsid);
			goods.setGoodsDetails(url + src + "/" + details.getName());
			goods.setGoodsLogo(url + src + "/" + dest.getName());
			int p = businessManagementService.modifyGoods(goods);
			if (p > 0) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "商品添加成功");
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "商品添加失败");
			}
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "添加商品失败");
		}
		return map;
	}

	/**
	 * 商家删除商品
	 * 
	 * @author lirui
	 * @param request
	 * @param id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/deletegoods")
	public @ResponseBody Object deleteGoods(HttpServletRequest request, @RequestParam("id") int arr[]) {
		Map maps = new HashMap();
		for (int x = 0; x < arr.length; x++) {
			int id = arr[x];
			List<Goods> goodlist = businessManagementService.getGoodsById(id);
			deleteFile(goodlist.get(0).getGoodsLogo(), goodlist.get(0).getBusinessId());
			deleteFile(goodlist.get(0).getGoodsDetails(), goodlist.get(0).getBusinessId());
			businessManagementService.removeGoods(id);
		}
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT, arr.length);
		return maps;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/modify")
	public @ResponseBody Object modify(@RequestParam int id, @RequestParam String name) {
		Map maps = new HashMap();
		Goods goods = new Goods();
		goods.setId(id);
		goods.setGoodsName(name);
		int i = businessManagementService.modifyGoods(goods);
		if (i < 0) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
		}
		return maps;
	}

	/**
	 * 修改商品信息
	 * 
	 * @param request
	 * @param id
	 * @param goodsName
	 * @param goodsLogo
	 * @param goodsDetails
	 * @param goodsContext
	 * @param goodsPrice
	 * @param goodsCoin
	 * @param goodsNum
	 * @param oodsgTypeId
	 * @param businessId
	 * @param files
	 * @param filesd
	 * @return
	 * @throws UnsupportedEncodingException
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/modifygoods")
	public @ResponseBody Object updateGoods(HttpServletRequest request, @RequestParam("id") Integer id,
			@RequestParam(value = "goodsName", required = false) String goodsName,
			@RequestParam(value = "goodsContext", required = false) String goodsContext,
			@RequestParam(value = "goodsPrice", required = false) BigDecimal goodsPrice,
			@RequestParam(value = "goodsCoin", required = false) Integer goodsCoin,
			@RequestParam(value = "goodsNum", required = false) Integer goodsNum,
			@RequestParam(value = "oodsgTypeId", required = false) Integer oodsgTypeId,
			@RequestParam("businessId") Integer businessId,
			@RequestParam(value = "originalPrice", required = false) BigDecimal originalPrice) throws Exception {
		String goods_Name = null;
		String goods_Context = null;
		if (goodsName != null) {
			goods_Name = URLDecoder.decode(goodsName, "UTF-8");
		}
		if (goodsContext != null) {
			goods_Context = URLDecoder.decode(goodsContext, "UTF-8");
		}
		Map maps = new HashMap();
		Goods goods = new Goods();
		goods.setBusinessId(businessId);
		goods.setGoodsCoin(goodsCoin);
		goods.setGoodsContext(goods_Context);
		goods.setGoodsName(goods_Name);
		goods.setId(id);
		goods.setGoodsNum(goodsNum);
		goods.setGoodsPrice(goodsPrice);
		goods.setOodsgTypeId(oodsgTypeId);
		goods.setOriginalPrice(originalPrice);
		/* 上传logo图片 */
		File dest = null;
		File details = null;
		String src = businessId + "";
		File dir = new File(realnamePic + src);
		// 创建目录
		if (dir.mkdirs()) {
			System.out.println("创建目录" + realnamePic + src + ":成功！");

		} else {
			File[] tempfiles = dir.listFiles();
			for (File file : tempfiles) {
			}

			System.out.println("创建目录" + realnamePic + src + ":失败，目标已存在！");
		}
		List<Goods> glist = businessManagementService.getGoodsById(id);
		try {
			MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> files = multiPartRequest.getFiles("files");
			if (files != null && files.size() > 0) {
				String fileF = null;
				for (int j = 0; j < files.size(); j++) {
					String fileName = files.get(j).getOriginalFilename();// 获取文件名加后缀
					log.info("====文件名：" + fileName);
					fileF = fileName.substring(fileName.lastIndexOf(".") + 1);// 文件后缀
					log.info("====文件后缀名：" + fileF);
					fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + "." + fileF;// 新的文件名
					dest = new File(realnamePic + src + "/" + fileName);
					log.info("====文件ming1：" + dest.getName());
					try {
						if (!"".equals(fileF)) {
							deleteFile(glist.get(0).getGoodsLogo().toString(), businessId);
							files.get(j).transferTo(dest);
						} else {
							System.out.println("上传预览图======1为空");
						}
					} catch (IllegalStateException e) {
						log.info(e.getMessage());
					} catch (IOException e) {
						log.info(e.getMessage());
					}
				}
				if (!"".equals(fileF)) {
					goods.setGoodsLogo(url + src + "/" + dest.getName());
				}
			}
		} catch (Exception e) {
			System.out.println("没有上传预览图======1");
		}
		try {
			MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> filesd = multiPartRequest.getFiles("filesd");
			/* 上传商品详情长图 */
			if (filesd != null && filesd.size() > 0) {
				String fileF = null;
				for (int j = 0; j < filesd.size(); j++) {
					String fileName = filesd.get(j).getOriginalFilename();// 获取文件名加后缀
					log.info("====文件名：" + fileName);
					fileF = fileName.substring(fileName.lastIndexOf(".") + 1);// 文件后缀
					log.info("====文件后缀名：" + fileF);
					fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + "." + fileF;// 新的文件名
					details = new File(realnamePic + src + "/" + fileName);
					log.info("====文件ming2：" + details.getName());
					try {
						if (!"".equals(fileF)) {
							deleteFile(glist.get(0).getGoodsDetails().toString(), businessId);
							filesd.get(j).transferTo(details);
						} else {
							System.out.println("上传预览图======2为空");
						}
					} catch (IllegalStateException e) {
						log.info(e.getMessage());
					} catch (IOException e) {
						log.info(e.getMessage());
					}
				}
				if (!"".equals(fileF)) {
					goods.setGoodsDetails(url + src + "/" + details.getName());
				}
			}
		} catch (Exception e) {
			System.out.println("没有上传预览图======2");
		}

		int i = businessManagementService.modifyGoods(goods);
		if (i <= 0) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
		}
		return maps;
	}

	public boolean deleteFile(String path, int businessId) {
		String brandId = businessId + "/";
		int q = path.lastIndexOf("/");
		String fileName = path.substring(q + 1);
		System.out.println(fileName);
		String deletefilePath = realnamePic + brandId + fileName;
		System.out.println(deletefilePath);
		File file = new File(deletefilePath);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				System.out.println("删除单个文件" + fileName + "成功！");
				return true;
			} else {
				System.out.println("删除单个文件" + fileName + "失败！");
				return false;
			}
		} else {
			System.out.println("删除单个文件失败：" + fileName + "不存在！");
			return false;
		}
	}
}
