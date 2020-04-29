package com.jh.user.service;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.user.business.NewsBusiness;
import com.jh.user.pojo.News;
import com.jh.user.pojo.NewsClassifiCation;
import com.jh.user.util.IpAddress;
import com.jh.user.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
@EnableAutoConfiguration
public class NewsService {

	private static final Logger LOG = LoggerFactory.getLogger(NewsService.class);

	@Autowired
	Util util;

	@Autowired
	private NewsBusiness newsBusiness;

	//private String IPAddress = "http://106.14.28.146:8888";
	
	//private String IPAddress = "http://101.132.160.107:8888";
	private String IPAddress = IpAddress.getIpAddress();
	
	//聚金宝后台
	//private String IPAddress = "http://106.15.206.59:8888";

	private String writePath = "/usr/share/nginx/html/news/brandid";

	private String readPath = IPAddress + "/news/brandid";
	
	/*private String writePath = "I:\\brandId";

	private String readPath = "I:\\brandId";*/

	// 添加新闻的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/addnews")
	public @ResponseBody Object addNews(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "classifiCation", required = false) String classifiCation,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "remark", required = false) String remark,
			@RequestParam(value = "previewNumber", required = false, defaultValue = "0") String previewNumber,
			@RequestParam(value = "publisher", required = false) String publisher,
			@RequestParam(value = "onOrOff", required = false, defaultValue = "1") String onOrOff,
			@RequestParam(value = "spare1", required = false) String spare1,
			@RequestParam(value = "spare2", required = false) String spare2,
			@RequestParam(value = "spare3", required = false) String spare3) throws Exception {

		News news = new News();
		news.setBrandId(brandId);
		news.setClassifiCation(URLDecoder.decode(classifiCation, "UTF-8"));
		news.setTitle(URLDecoder.decode(title, "UTF-8"));
		news.setContent(URLDecoder.decode(content, "UTF-8"));

		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int nextInt = random.nextInt(10);
			sb.append(nextInt);
		}

		news.setLowSourceId(sb.toString());
		news.setOnOff(onOrOff);
		news.setRemark(URLDecoder.decode(remark, "UTF-8"));

		if ("0".equals(previewNumber)) {

			StringBuffer sb1 = new StringBuffer();
			for (int i = 0; i < 4; i++) {
				int nextInt = random.nextInt(10);
				if (i == 0 && nextInt == 0) {
					continue;
				}
				sb1.append(nextInt);
			}

			news.setPreviewNumber(sb1.toString());
		} else {

			news.setPreviewNumber(previewNumber);
		}

		news.setPublisher(URLDecoder.decode(publisher, "UTF-8"));
		news.setSpare1(spare1);
		news.setSpare2(spare2);
		news.setSpare3(spare3);

		List<String> filepaths = null;
		try {
			MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;

			List<MultipartFile> files = multiPartRequest.getFiles("image");

			File file = new File(writePath + "/" + brandId + "/" + sb.toString());

			if (file.mkdirs()) {

			} else {
				File[] listFiles = file.listFiles();
				for (File f : listFiles) {
					f.delete();
				}
			}
			int i = 1;

			if (files != null && files.size() > 0) {
				for (MultipartFile mf : files) {
					String filename = mf.getOriginalFilename();
					String substring = filename.substring(filename.lastIndexOf("."));
					i++;
					File dest = new File(writePath + "/" + brandId + "/" + sb.toString() + "/"
							+ System.currentTimeMillis() + i + substring);

					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
						// return ResultWrap.init(CommonConstants.SUCCESS, "成功");
					} catch (Exception e) {
						LOG.error("保存预览图出错啦======");
						e.printStackTrace();

						return ResultWrap.init(CommonConstants.FALIED, "保存预览图失败!");
					}

				}
			}

			filepaths = new ArrayList<String>();
			
			String path = null;
			if (file != null) {
				String[] list = file.list();
				if (list != null) {
					for (int j = 0; j < list.length; j++) {
						/*String string = list[j];
						filepaths.add(readPath + "/" + brandId + "/" + sb.toString() + "/" + string);*/
						String string = list[0];
						path = readPath + "/" + brandId + "/" + sb.toString() + "/" + string;
					}
				} else {
					//filepaths.add("");
					path = "";
				}

				//news.setLowSource(filepaths.toString());
				news.setLowSource(path);
			}
		} catch (Exception e1) {
			// e1.printStackTrace();
			LOG.info("没有上传预览图======");
			news.setLowSource("");
		}

		try {
			newsBusiness.createNews(news);
		} catch (Exception e) {
			LOG.error("保存数据出错啦======");
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "保存新闻出错,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "成功");
	}

	// 分页查看整个贴牌所有新闻的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/getnewsby/brandidandpage")
	public @ResponseBody Object getNewsByBrandIdAndPage(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "type", defaultValue = "0", required = false) String type,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "500", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

		Page<News> newsByBrandIdAndPage = null;
		try {
			newsByBrandIdAndPage = newsBusiness.getNewsByBrandIdAndPage(brandId, pageable);
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
		}

		if (newsByBrandIdAndPage != null) {

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", newsByBrandIdAndPage);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据", "");
		}

	}
	/**
	 *分页查看整个贴牌各个分类新闻的接口
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/getnewsby/brandidandclassification/andpage")
	public @ResponseBody Object getNewsByBrandIdAndClassifiCationAndPage(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "classifiCation", required = false, defaultValue = "-1") String classifiCation,
			@RequestParam(value = "title", required = false, defaultValue = "-1") String title,
			@RequestParam(value = "type", defaultValue = "0", required = false) String type,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));

		if(!"-1".equals(classifiCation) && !"-1".equals(title)) {
			
			Page<News> newsByBrandIdAndPage = null;
			try {
				newsByBrandIdAndPage = newsBusiness.getNewsByBrandIdAndClassifiCationAndTitleAndPage(brandId, classifiCation, title, pageable);
			} catch (Exception e) {
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
			}

			if (newsByBrandIdAndPage != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", newsByBrandIdAndPage);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", newsByBrandIdAndPage);
			}
			
		}else if(!"-1".equals(title)) {
			
			Page<News> newsByBrandIdAndPage = null;
			try {
				newsByBrandIdAndPage = newsBusiness.getNewsByBrandIdAndTitleAndPage(brandId, title, pageable);
			} catch (Exception e) {
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
			}

			if (newsByBrandIdAndPage != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", newsByBrandIdAndPage);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", newsByBrandIdAndPage);
			}
			
		}else if (!"-1".equals(classifiCation)) {

			Page<News> newsByBrandIdAndClassifiCationAndPage = null;
			try {
				newsByBrandIdAndClassifiCationAndPage = newsBusiness.getNewsByBrandIdAndClassifiCationAndPage(brandId,
						classifiCation, pageable);
			} catch (Exception e) {
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
			}

			if (newsByBrandIdAndClassifiCationAndPage != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", newsByBrandIdAndClassifiCationAndPage);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", newsByBrandIdAndClassifiCationAndPage);
			}

		} else {

			Page<News> newsByBrandIdAndPage = null;
			try {
				newsByBrandIdAndPage = newsBusiness.getNewsByBrandIdAndPage(brandId, pageable);
			} catch (Exception e) {
				e.printStackTrace();

				return ResultWrap.init(CommonConstants.FALIED, "查询失败,请稍后重试!");
			}

			if (newsByBrandIdAndPage != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", newsByBrandIdAndPage);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据", newsByBrandIdAndPage);
			}
			
		}
		
		
		

	}

	// 查看单个新闻的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/getnewsby/brandidandid")
	public @ResponseBody Object getNewsByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "id") long id) {

		News news = null;
		try {
			news = newsBusiness.getNewsByBrandIdAndId(brandId, id);

		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", news);
	}

	// 更改新闻的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/updatenewsby/brandidandid")
	public @ResponseBody Object updateNewsByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "id") long id,
			@RequestParam(value = "classifiCation", required = false, defaultValue = "-1") String classifiCation,
			@RequestParam(value = "title", required = false, defaultValue = "-1") String title,
			@RequestParam(value = "content", required = false, defaultValue = "-1") String content,
			@RequestParam(value = "remark", required = false, defaultValue = "-1") String remark,
			@RequestParam(value = "previewNumber", required = false, defaultValue = "0") String previewNumber,
			@RequestParam(value = "publisher", required = false, defaultValue = "-1") String publisher,
			@RequestParam(value = "onOrOff", required = false, defaultValue = "-1") String onOrOff,
			@RequestParam(value = "spare1", required = false, defaultValue = "-1") String spare1,
			@RequestParam(value = "spare2", required = false, defaultValue = "-1") String spare2,
			@RequestParam(value = "spare3", required = false, defaultValue = "-1") String spare3) throws Exception {

		News news = newsBusiness.getNewsByBrandIdAndId(brandId, id);

		if(news == null) {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据,无法修改!");
		}
		
		if (!"-1".equals(classifiCation)) {
			news.setClassifiCation(URLDecoder.decode(classifiCation, "UTF-8"));
		}
		if (!"-1".equals(title)) {
			news.setTitle(URLDecoder.decode(title, "UTF-8"));
		}
		if (!"-1".equals(content)) {
			news.setContent(URLDecoder.decode(content, "UTF-8"));
		}
		if (!"-1".equals(remark)) {
			news.setRemark(URLDecoder.decode(remark, "UTF-8"));
		}
		if (!"0".equals(previewNumber)) {
			news.setPreviewNumber(URLDecoder.decode(previewNumber, "UTF-8"));
		}
		if (!"-1".equals(publisher)) {
			news.setPublisher(URLDecoder.decode(publisher, "UTF-8"));
		}
		if (!"-1".equals(onOrOff)) {
			news.setOnOff(onOrOff);
		}
		if (!"-1".equals(spare1)) {
			news.setSpare1(spare1);
		}
		if (!"-1".equals(spare2)) {
			news.setSpare2(spare2);
		}
		if (!"-1".equals(spare3)) {
			news.setSpare3(spare3);
		}

		List<String> filepaths = null;
		try {
			List<MultipartFile> files = null;
			try {
				MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest) request;
				
				files = multiPartRequest.getFiles("image");
			} catch (Exception e1) {
				e1.printStackTrace();
				news.setLowSource(news.getLowSource());
			}

			File file = new File(writePath + "/" + brandId + "/" + news.getLowSourceId());

			if (file.mkdirs()) {

			} else if(files != null && files.get(0).getSize()!=0) {
				File[] listFiles = file.listFiles();
				for (File f : listFiles) {
					f.delete();
				}
			}
			int i = 1;

			if (files != null && files.size() > 0) {
				for (MultipartFile mf : files) {
					String filename = mf.getOriginalFilename();
					String substring = filename.substring(filename.lastIndexOf("."));
					i++;
					File dest = new File(writePath + "/" + brandId + "/" + news.getLowSourceId() + "/"
							+ System.currentTimeMillis() + i + substring);

					try {
						mf.transferTo(dest);
						Runtime.getRuntime().exec("chmod 777 " + dest.getAbsolutePath());
						// return ResultWrap.init(CommonConstants.SUCCESS, "成功");
					} catch (Exception e) {
						LOG.error("保存预览图出错啦======");
						e.printStackTrace();

						return ResultWrap.init(CommonConstants.FALIED, "保存预览图失败!");
					}

				}
				
				filepaths = new ArrayList<String>();

				String path = null;
				if (file != null) {
					String[] list = file.list();
					if (list != null) {
						for (int j = 0; j < list.length; j++) {
							/*String string = list[j];
							filepaths.add(readPath + "/" + brandId + "/" + news.getLowSourceId() + "/" + string);*/
							String string = list[0];
							path = readPath + "/" + brandId + "/" + news.getLowSourceId() + "/" + string;
						}
					} else {
						//filepaths.add("");
						path = "";
					}

					//news.setLowSource(filepaths.toString());
					news.setLowSource(path);
				}
				
			}

			
		} catch (Exception e1) {
			//e1.printStackTrace();
			LOG.info("没有上传预览图======");
			
		}

		try {
			newsBusiness.createNews(news);
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "修改新闻信息失败,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
	}

	// 删除新闻的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/deletenewsby/brandidandid")
	public @ResponseBody Object deleteNewsByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "id") String Id) {

		long[] l = null;
		if (Id.contains(",")) {

			String[] split = Id.split(",");
			l = new long[split.length];

			for (int i = 0; i < split.length; i++) {
				l[i] = Long.parseLong(split[i]);
			}

		} else {

			String[] split = { Id };
			l = new long[split.length];

			for (int i = 0; i < split.length; i++) {
				l[i] = Long.parseLong(split[i]);
			}

		}

		List<News> newsByBrandIdAndId = newsBusiness.getNewsByBrandIdAndId(brandId, l);

		if (newsByBrandIdAndId != null && newsByBrandIdAndId.size() > 0) {
			for (News n : newsByBrandIdAndId) {

				try {

					File file = new File(writePath + "/" + brandId + "/" + n.getLowSourceId());

					if (file.isFile() || file.list().length == 0) {
						file.delete();
					} else {
						File[] listFiles = file.listFiles();
						for (int i = 0; i < listFiles.length; i++) {
							listFiles[i].delete();
						}

						if (file.exists()) {
							file.delete();
						}
					}

					newsBusiness.deleteNews(n);

				} catch (Exception e) {
					e.printStackTrace();

					continue;
				}
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}

	}

	// 控制开关的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/news/updatenews/onoroffby/brandidandid")
	public @ResponseBody Object updateNewsOnOrOffByBrandIdAndId(HttpServletRequest request,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "id") long Id, @RequestParam(value = "onOrOff") String onOrOff) {

		try {
			News newsByBrandIdAndId = newsBusiness.getNewsByBrandIdAndId(brandId, Id);

			newsByBrandIdAndId.setOnOff(onOrOff);

			newsBusiness.createNews(newsByBrandIdAndId);
		} catch (Exception e) {
			LOG.error("", e.getMessage());

			return ResultWrap.init(CommonConstants.FALIED, "修改失败");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "修改开关成功");
	}

	// 添加新闻分类的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/newsclassification/addclassification")
	public @ResponseBody Object addClassifiCation(
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "classifiCation") String classifiCation) {

		NewsClassifiCation ncc = newsBusiness.getNewsClassifiCationByBrandIdAndClassifiCation(brandId, classifiCation.trim());
		
		if(ncc == null) {
			
			NewsClassifiCation nc = new NewsClassifiCation();

			nc.setBrandId(brandId);
			nc.setClassifiCation(classifiCation.trim());

			try {
				newsBusiness.createNewsClassifiCation(nc);
			} catch (Exception e) {
				LOG.info("添加新闻分类出错======", e.getMessage());
				return ResultWrap.init(CommonConstants.FALIED, "添加失败!");
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "添加成功!");
		
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "该新闻分类已存在,请勿重复添加!");
		}
		
		
	}

	// 查询新闻分类的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/newsclassification/getclassification/bybrandid")
	public @ResponseBody Object getClassifiCationByBrandId(
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId) {

		List<NewsClassifiCation> nc = null;
		try {
			nc = newsBusiness.getNewsClassifiCationByBrandId(brandId);
		} catch (Exception e) {
			LOG.info("查询新闻分类出错======", e.getMessage());
		}

		if (nc != null && nc.size() > 0) {

			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", nc);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!", "");
		}

	}

	
	//修改新闻分类的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/newsclassification/updateclassification")
	public @ResponseBody Object updateClassifiCation(@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId,
			@RequestParam(value = "oldClassification") String oldClassification,
			@RequestParam(value = "newClassification") String newClassification
			) {
		
		NewsClassifiCation ncc = newsBusiness.getNewsClassifiCationByBrandIdAndClassifiCation(brandId, oldClassification.trim());
		
		ncc.setClassifiCation(newClassification.trim());
		newsBusiness.createNewsClassifiCation(ncc);
		
		List<News> list = newsBusiness.getNewsByBrandIdAndClassifiCationAndPage(brandId, oldClassification.trim());
		
		if(list != null && list.size()>0) {
			
			for(News n : list) {
				
				try {
					n.setClassifiCation(newClassification.trim());
					newsBusiness.createNews(n);
				} catch (Exception e) {
					continue;
				}
				
			}
			
			return ResultWrap.init(CommonConstants.SUCCESS, "修改成功!");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}
		
	}
	
	
	
	//删除新闻分类的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/newsclassification/deleteclassification")
	public @ResponseBody Object deleteClassifiCation(@RequestParam(value = "id") String Id,
			@RequestParam(value = "brandId", required = false, defaultValue = "2") String brandId) {
		
		long[] l = null;
		if (Id.contains(",")) {

			String[] split = Id.split(",");
			l = new long[split.length];

			for (int i = 0; i < split.length; i++) {
				l[i] = Long.parseLong(split[i]);
			}

		} else {

			String[] split = { Id };
			l = new long[split.length];

			for (int i = 0; i < split.length; i++) {
				l[i] = Long.parseLong(split[i]);
			}

		}
		
		List<NewsClassifiCation> list = newsBusiness.getNewsClassifiCationByBrandIdAndId(brandId, l);
		
		if(list != null && list.size()>0) {
			
			for(NewsClassifiCation nc : list) {
				
				try {
					newsBusiness.deleteNewsClassifiCation(nc);
				} catch (Exception e) {
					LOG.info("删除新闻分类有误======", e.getMessage());
					continue;
				}
				
			}
			
			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功!");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}
		
	}
	
	
	
	// 计算请求次数的方法
	// public

}
