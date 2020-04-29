package cn.jh.common.exception;

import javax.servlet.http.HttpServletRequest;

public class UnauthorizedException extends RuntimeException {
	
	public UnauthorizedException(String msg) {
		super(msg);
	}

	public static UnauthorizedException createUnauthorizedExceptionForUserPreloginNeed(HttpServletRequest request) {		
		return new UnauthorizedException("User Login is needed for the API (" + (request==null?"":request.getRequestURI()) + ")");
	}
}
