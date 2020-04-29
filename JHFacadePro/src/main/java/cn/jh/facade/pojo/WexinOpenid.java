package cn.jh.facade.pojo;

import java.io.Serializable;

public class WexinOpenid implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**0表示无须跳转*/
	private  String respType;

	private  String respResult;

	public String getRespType() {
		return respType;
	}

	public void setRespType(String respType) {
		this.respType = respType;
	}

	public String getRespResult() {
		return respResult;
	}

	public void setRespResult(String respResult) {
		this.respResult = respResult;
	}
	
	
	
	
	
	
}
