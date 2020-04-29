/**
 * 
 */
package com.jh.mircomall.bean;

/**
 * 给 sql 用的分页 model
 * 
 *
 */
public class PageForSQLModel {
		
		private Integer pageNo;
		private Integer pageSize;
		
		private Integer offset;
		private Integer limit;
		
		
		public PageForSQLModel(Integer offset, Integer limit, Integer pageNo, Integer pageSize) {
			this.offset = offset;
			this.limit = limit;
			this.pageNo = pageNo;
			this.pageSize = pageSize;
		}
		

		public Integer getPageCount(Integer recordCount) {
			
			if ( recordCount < 1 ) {
				return 0;
			}
			else {}
			
			
			int p1 = recordCount/pageSize;
			int m1 = recordCount%pageSize;
			
			if ( m1 != 0 ) {
				return p1+1;
			}
			else {
				return p1;
			}
			
		}
		
		
		//======================
		// Getter and Setter
		//======================
		public Integer getOffset() {
			return offset;
		}

		public void setOffset(Integer offset) {
			this.offset = offset;
		}

		public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

		public Integer getPageNo() {
			return pageNo;
		}

		public void setPageNo(Integer pageNo) {
			this.pageNo = pageNo;
		}

		public Integer getPageSize() {
			return pageSize;
		}

		public void setPageSize(Integer pageSize) {
			this.pageSize = pageSize;
		}
		
		
		
}
