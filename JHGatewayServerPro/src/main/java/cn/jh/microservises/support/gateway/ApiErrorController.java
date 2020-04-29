package cn.jh.microservises.support.gateway;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApiErrorController implements ErrorController {
	
	private static Logger log = LoggerFactory.getLogger(ApiErrorController.class);
 
    @Value("${error.path:/error}")
    private String errorPath;
     
    @Override
    public String getErrorPath() {
        return errorPath;
    }
 
    @RequestMapping(value = "${error.path:/error}", produces = "application/json")
    public @ResponseBody ResponseEntity error(HttpServletRequest request) {
 
        final int status = getErrorStatus(request);
        final String errorMessage = getErrorMessage(request);
       
        return ResponseEntity.status(status).body(errorMessage);
    }
 
    private int getErrorStatus(HttpServletRequest request) {
        Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
        
        log.info(String.format(">>>>ApiErrorController.getErrorStatus - %s", statusCode));

        return statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
 
    private String getErrorMessage(HttpServletRequest request) {
        final Throwable exc = (Throwable) request.getAttribute("javax.servlet.error.exception");
        
        log.info(String.format(">>>>ApiErrorController.getErrorMessage - %s", exc != null ? exc.getMessage() : ""));

        return exc != null ? exc.getMessage() : "Unexpected error occurred";
    }
}