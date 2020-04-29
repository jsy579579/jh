package com.jh.paymentgateway.util.ap.exception;

/**
* 类名称：CusRuntimeException
* 类描述：
* 创建人：dengzhixin
* 创建时间：2017年5月11日 下午3:01:37
* 版本：1.0
* 
*/

public class CusRuntimeException extends RuntimeException {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */

	private static final long serialVersionUID = -9213061167470150235L;

	private String resultCode;
	private String errorCode;
	private String errorDesc;

	/**
     * 
     * @param resultCode
     */
    public CusRuntimeException(String resultCode) {
        super();
        this.resultCode = resultCode;
    }

	/**
     * @param resultCode
     * @param e
     */
    public CusRuntimeException(String resultCode, Exception e) {
        super(resultCode, e);
        this.resultCode = resultCode;

    }

	/**
     * 
     * @param resultCode
     */
    public CusRuntimeException(String resultCode, String errorDesc) {
        this(resultCode);
        this.errorDesc = errorDesc;
    }

	/**
     * 
     * @param resultCode
     */
    public CusRuntimeException(String resultCode, String errorCode, String errorDesc) {
        this(resultCode);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

	/**
     * 
     * @param resultCode
     */
    public CusRuntimeException(String resultCode, String errorDesc, Exception e) {
        this(resultCode, e);
        this.errorDesc = errorDesc;
    }

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

}
