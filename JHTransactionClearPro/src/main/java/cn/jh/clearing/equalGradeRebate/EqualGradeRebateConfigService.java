package cn.jh.clearing.equalGradeRebate;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class EqualGradeRebateConfigService {

	@Autowired
	private EqualGradeRebateConfigBusiness equalGradeRebateConfigBusiness;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RequestMapping(value="/v1.0/transactionclear/get/equal/grade/rebate/by/brandid")
	public @ResponseBody Object getEqualRebateConfigByBrandId(
			@RequestParam()String brandId
			) {
		List<EqualGradeRebateConfig> equalGradeRebateConfigs = equalGradeRebateConfigBusiness.findByBrandId(brandId);
		if (equalGradeRebateConfigs != null && equalGradeRebateConfigs.size() > 0) {
			JSONObject brandProduct = this.getBrandProduct(brandId);
			if (!CommonConstants.SUCCESS.equals(brandProduct.getString(CommonConstants.RESP_CODE))) {
				return brandProduct;
			}
			JSONArray brandProductArray = brandProduct.getJSONArray(CommonConstants.RESULT);
			for (EqualGradeRebateConfig equalGradeRebateConfig : equalGradeRebateConfigs) {
				for (Object object : brandProductArray) {
					brandProduct = (JSONObject) object;
					if (equalGradeRebateConfig.getGrade() == brandProduct.getInt("grade")) {
						equalGradeRebateConfig.setProductName(brandProduct.getString("name"));
					}
				}
				if (equalGradeRebateConfig.getProductName() == null) {
					equalGradeRebateConfig.setProductName("等级"+equalGradeRebateConfig.getGrade());
				}
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",equalGradeRebateConfigs);
	}
	
	@RequestMapping(value="/v1.0/transactionclear/set/equal/grade/rebate/by/brandid")
	public @ResponseBody Object setEqualRebateConfigByBrandId(
			@RequestParam()String brandId,
			@RequestParam()String grade,
			String rate,
			String isDelete
			) {
		EqualGradeRebateConfig equalGradeRebateConfig = equalGradeRebateConfigBusiness.findByBrandIdAndGrade(brandId,Integer.valueOf(grade));
		if (isDelete == null || !"1".equals(isDelete)) {
			if (equalGradeRebateConfig == null) {
				equalGradeRebateConfig = new EqualGradeRebateConfig();
			}
			equalGradeRebateConfig.setBrandId(brandId);
			try {
				equalGradeRebateConfig.setRate(new BigDecimal(rate));
				equalGradeRebateConfig.setGrade(Integer.valueOf(grade));
			} catch (NumberFormatException e) {
				return ResultWrap.init(CommonConstants.FALIED, "参数有误,请重新设置!");
			}
			equalGradeRebateConfig = equalGradeRebateConfigBusiness.save(equalGradeRebateConfig);
		}else{
			if (equalGradeRebateConfig == null) {
				return ResultWrap.init(CommonConstants.FALIED, "无数据可删除!");
			}
			equalGradeRebateConfigBusiness.delete(equalGradeRebateConfig);
			return ResultWrap.init(CommonConstants.SUCCESS, "删除成功");
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "设置成功",equalGradeRebateConfig);
	}
	
	private JSONObject getBrandProduct(String brandId) {
		String url = "http://user/v1.0/user/thirdlevel/prod/brand/"+brandId+"?access_type=1";
		return restTemplate.getForObject(url, JSONObject.class);
	}
	
	
}
