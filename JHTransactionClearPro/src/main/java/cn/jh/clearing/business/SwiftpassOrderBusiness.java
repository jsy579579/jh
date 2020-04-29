package cn.jh.clearing.business;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import cn.jh.clearing.pojo.SwiftpassOrder;

public interface SwiftpassOrderBusiness {
    
	/**多条件查询信息*/
	public Map findSwiftpassOrderAll(String tirdOrderCode,String thridMchId,String preMchId,String mchId,String orderCode,String mchOrderCode,Pageable pageAble );

	/**添加历史交易清单*/
	public SwiftpassOrder save(SwiftpassOrder swiftpassOrder);

	/**添加历史交易清单*/
	public List<SwiftpassOrder> saveall(List<SwiftpassOrder> swiftpassOrders);
	
	/**下载威富通订单*/
	public List<SwiftpassOrder> downloadSwiftpass(String billDate,String mchId,String key)throws Exception;
	public int findCountByCreateTime(String createTime) throws Exception;
	/**
	 * 根据订单号查询一条数据
	 * @param orderCode
	 * @return
	 */
	public List<SwiftpassOrder> findByOrderCode(String orderCode);
//	/**
//	 * 删除一条记录
//	 * @param swiftpassOrder
//	 */
//	public void delete(SwiftpassOrder swiftpassOrder);
	/**
	 * 查询指定时间的账单
	 * @param createTime
	 * @return
	 */
	public List<SwiftpassOrder> findByCreateTime(String createTime) throws Exception ;

	public void delete(SwiftpassOrder orderCode);
} 


