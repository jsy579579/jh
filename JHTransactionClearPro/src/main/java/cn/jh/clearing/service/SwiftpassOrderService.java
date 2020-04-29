package cn.jh.clearing.service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.clearing.business.SwiftpassOrderBusiness;
import cn.jh.clearing.pojo.SwiftpassOrder;
import cn.jh.clearing.util.Util;
import cn.jh.clearing.util.XmlUtils;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class SwiftpassOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(SwiftpassOrderService.class);
	@Autowired
	SwiftpassOrderBusiness swiftpassOrderBusiness;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	Util util;

	/** 多条件查询swiftpassorder */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/swiftpassorder/query/all")
	public @ResponseBody Object swiftpassOrderqueryById(HttpServletRequest request,
			@RequestParam(value = "tirdOrderCode", required = false) String tirdOrderCode, /** 第三方订单号 */
			@RequestParam(value = "thridMchId", required = false) String thridMchId, /** 第三方商户号 */
			@RequestParam(value = "preMchId", required = false) String preMchId, /** 商户号 */
			@RequestParam(value = "mchId", required = false) String mchId, /** 子商户号 */
			@RequestParam(value = "orderCode", required = false) String orderCode, /** 平台订单号 */
			@RequestParam(value = "mchOrderCode", required = false) String mchOrderCode, /** 商户订单号 */
			@RequestParam(value = "page", defaultValue = "0", required = false) int page, 
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {

		Pageable pageAble = new PageRequest(page, size, new Sort(direction, sortProperty));
		Map map = new HashMap();
		// 根据多个条件查询订单信息
		Map querySwiftpassOrderAll = swiftpassOrderBusiness.findSwiftpassOrderAll(tirdOrderCode, thridMchId,
				preMchId, mchId, orderCode, mchOrderCode, pageAble);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, querySwiftpassOrderAll);
		map.put(CommonConstants.RESP_MESSAGE, "成功");

		return map;

	}
	
}
