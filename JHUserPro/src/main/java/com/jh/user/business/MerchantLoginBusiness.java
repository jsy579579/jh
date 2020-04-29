package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.Merchant;

public interface MerchantLoginBusiness{
	  /**判断是否登陆*/
		public Merchant isLoginMerchant(String preMchId,String password);
		
		/**商户注册*/
		public Merchant saveMerchant(Merchant merchant);
		
		/**查询表中所有preMchId*/
		public Merchant findAllByPreMchId(String preMchId);
		
		/**查询商户信息*/
		public Merchant findMchIdByPreMchid(String preMchId);
		
		/**查询所有商户*/
		public List<Merchant> findAllMerchant();
		
		
		/**根据商户号查询商户*/
		public Merchant findMerchantByMchId(String preMchId);
}


