package cn.jh.clearing.pojo;

public class PaymentOrderNumber {
		
		/**
		 * 通道标识
		 * */
		 public String  channeltag;
		 
		 /**
		  * 通道编号
		  * **/
		 public long channelId;
		 
		 /**
		  * 通道名称
		  * **/
		 public String channelName;
		 
		 /**
		  * 成功笔数
		  * **/
		 public  long successNumber;
		 
		 /**
		  * 成功金额
		  * **/
		 public  long successMoney;
		 
		 /**
		  * 失败笔数
		  * **/
		 public long failureNumber;
		 
		 /**
		  * 失败笔数
		  * **/
		 public long failureMoney;
		 
		 /**
		  * 总笔数
		  * **/
		 public long allNumber;
		
		 /**
		  * 总笔数
		  * **/
		 public long allMoney;
		 
		public String getChanneltag() {
			return channeltag;
		}
		
		public void setChanneltag(String channeltag) {
			this.channeltag = channeltag;
		}
		
		public long getChannelId() {
			return channelId;
		}
		
		public void setChannelId(long channelId) {
			this.channelId = channelId;
		}
		
		public String getChannelName() {
			return channelName;
		}

		public void setChannelName(String channelName) {
			this.channelName = channelName;
		}

		public long getSuccessNumber() {
			return successNumber;
		}
		
		public void setSuccessNumber(long successNumber) {
			this.successNumber = successNumber;
		}
		
		public long getSuccessMoney() {
			return successMoney;
		}

		public void setSuccessMoney(long successMoney) {
			this.successMoney = successMoney;
		}

		public long getFailureNumber() {
			return failureNumber;
		}
		
		public void setFailureNumber(long failureNumber) {
			this.failureNumber = failureNumber;
		}
		 
		public long getFailureMoney() {
			return failureMoney;
		}

		public void setFailureMoney(long failureMoney) {
			this.failureMoney = failureMoney;
		}

		public long getAllNumber() {
			return allNumber;
		}

		public void setAllNumber(long allNumber) {
			this.allNumber = allNumber;
		}

		public long getAllMoney() {
			return allMoney;
		}

		public void setAllMoney(long allMoney) {
			this.allMoney = allMoney;
		}

		public PaymentOrderNumber  add(String  channeltag,long channelId,long successNumber,long failureNumber,long allNumber,
				long successMoney,long failureMoney,long allMoney) {
			PaymentOrderNumber pon=new PaymentOrderNumber();
			pon.setChannelId(channelId);
			pon.setChanneltag(channeltag);
			pon.setFailureNumber(failureNumber);
			pon.setSuccessNumber(successNumber);
			pon.setAllNumber(allNumber);
			pon.setSuccessMoney(successMoney);
			pon.setFailureMoney(failureMoney);
			pon.setAllMoney(allMoney);
			return pon;
		}
 
	
}
