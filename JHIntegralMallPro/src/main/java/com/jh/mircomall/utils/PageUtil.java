/**
 * 
 */
package com.jh.mircomall.utils;

import com.jh.mircomall.bean.PageForSQLModel;

/**
 * 我需要用到的分页 util
 * @author sunny
 *
 */
public class PageUtil {

	/**
	 * 第一页
	 */
	public static final Integer FIRST_PAGE = 1;
	
	
	/**
	 * 默认分页都是一页20条记录
	 */
	public static final Integer BANNER_MANAGER_DEFAULT_PAGE_SIZE = 4; // 默认每页20条记录
	
	/**
	 * 使用默认20条，获取页面总数
	 * @param recordCount 记录总数
	 * @return
	 */
	public static Integer getPageCount(int recordCount) {
		return PageUtil.getPageCount(recordCount, BANNER_MANAGER_DEFAULT_PAGE_SIZE);
	}
	
	/**
	 * 根据记录总数，每页多少条记录 获取总页数。
	 * @param recordCount 记录总数
	 * @param pageSize 每页多少条记录
	 * @return
	 */
	public static Integer getPageCount(int recordCount, int pageSize) {
		
		if ( pageSize > 0 ) {
			
			int p1 = recordCount/pageSize;
			int m1 = recordCount%pageSize;
			
			if ( m1 != 0 ) {
				return p1+1;
			}
			else {
				return p1;
			}
			
		}
		else {
			return 0;
		}
		
		
	}
	
	
	/**
	 * 最大的查询页数
	 */
	private static final Integer MAX_PAGE_NUMBER = 500;
	
	/**
	 * 对第几页的判断
	 * @param pageNo
	 * @return
	 */
	private static Integer checkPageNo(Integer pageNo) {
		
		if ( pageNo < 1 ) {
			return 1;
		}
		else {
			if ( pageNo > MAX_PAGE_NUMBER ) { // 查询不能超过第50页
				return MAX_PAGE_NUMBER;
			}
			else {
				
			}
			
			return pageNo;
		}
		
	}
	
	/**
	 * 对一页多少条记录的判断
	 * @param pageNo
	 * @return
	 */
	private static Integer checkPageSize(Integer pageSize) {
		
		if ( pageSize < 1 || pageSize > 40 ) {
			return 20;
		}
		else {
			return pageSize;
		}
		
	}
	
	public static PageForSQLModel getPageInfoByPageNoAndSize( Integer pageNo, Integer pageSize) {
		
		int pn = PageUtil.checkPageNo(pageNo);
		int ps = PageUtil.checkPageSize(pageSize);
		
		int offset = (pn-1)*ps;
		
		PageForSQLModel pfsm = new PageForSQLModel(offset, ps, pn, ps);
		
		return pfsm;
		
	}
	
	
	public static PageForSQLModel getPageInfoByPageNoAndSizeNoPageLimit( Integer pageNo, Integer pageSize) {
		
		int pn = pageNo;
		int ps = pageSize;
		
		int offset = (pn-1)*ps;
		
		PageForSQLModel pfsm = new PageForSQLModel(offset, ps, pn, ps);
		
		return pfsm;
		
	}
	
	
}


