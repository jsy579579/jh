package cn.jh.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import cn.jh.common.utils.ExceptionUtil;

@ControllerAdvice
public class ApiExceptionHandler {

	private static Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(InvalidRequestException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleInvalidRequestError(InvalidRequestException ex) {
		log.info(String.format("====ApiExceptionHandler.handleInvalidRequestError - %s", ex.getMessage()));
		return "BAD_REQUEST: " + ex.getMessage();
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public String handleUnauthorizedExceptionError(UnauthorizedException ex) {
		log.info(String.format("====ApiExceptionHandler.handleUnauthorizedExceptionError - %s", ex.getMessage()));
		return "UNAUTHORIZED: " + ex.getMessage();
	}

	@ExceptionHandler(RecordNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ResponseBody
	public String handleRecordNotFoundExceptionError(UnauthorizedException ex) {
		log.info(String.format("====ApiExceptionHandler.handleRecordNotFoundExceptionError - %s", ex.getMessage()));
		return "NOT_FOUND: " + ex.getMessage();
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String handleUnexpectedServerError(RuntimeException ex) {
		ex.printStackTrace();
		log.info(String.format("====ApiExceptionHandler.handleUnexpectedServerError - %s", ex.toString()));
		log.info(String.format("====ApiExceptionHandler.handleUnexpectedServerError:Message - %s", ex.getMessage()));
		log.error("",ex);
		// return "INTERNAL_SERVER_ERROR: " + ex.getMessage();
		return "{'resp_message':'交易繁忙,请稍后重试!','resp_code':'999999','original_exception':'"+ ex.toString()+"'}";
	}
}
