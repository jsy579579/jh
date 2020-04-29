package cn.jh.microservises.support.gateway.filters.pre;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class AuthenticationHeaderFilter extends ZuulFilter {

	private static Logger log = LoggerFactory.getLogger(AuthenticationHeaderFilter.class);
	
	private static final String secretKey = "Yqilai-20160523";
	
	private static final String[][] preAuthenticationIgnoreUris = {
		{"/v1.0/authentication/sendSms", "*"}, 
		{"/v1.0/authentication/register", "*"}, 
		{"/v1.0/authentication/login", "*"},
		{"/v1.0/authentication/restster/wx", "*"},
		{"/v1.0/authentication/wx/user", "*"},
		{"/v1.0/facade/openid", "*"},
		{"/v1.0/facade/gatherdetail", "*"},
		{"/v1.0/facade/detailinfo", "*"},
		{"/v1.0/facade/detailinfohistory", "*"},
		{"/v1.0/facade/edit/gathering", "*"},
		{"/v1.0/facade/quit/success", "*"},
		{"/v1.0/facade/music/productype", "*"},
		{"/v1.0/facade/code", "*"},
		{"/v1.0/facade/token", "*"},
		{"/v1.0/facade/index", "*"},
		{"/v1.0/facade/share/friend", "*"},
		{"/v1.0/relationship/friend", "*"},
		{"/v1.0/relationship/judge", "*"},
		{"/v1.0/facade/test/test1", "*"},
		{"/v1.0/restaurant/baidu/view", "*"},
		{"/v1.0/facade/appshare", "*"},
		{"/v1.0/facade/historydetailinfo", "*"},
		{"/v1.0/facade/schedulephotoinfo", "*"},
		{"/v1.0/facade/join/success", "*"},
		{"/v1.0/facade/join/createsuccess", "*"},
		{"/v1.0/facade/intro", "*"},
		{"/v1.0/facade/gathering", "*"},
		{"/v1.0/facade/invitegathering", "*"},
		{"/v1.0/facade/first", "*"},
		{"/v1.0/facade/session", "*"},
		{"/v1.0/facade/version", "*"},
		{"/v1.0/facade/user/id", "*"},
		{"/v1.0/facade/user/friends", "*"},
		{"/v1.0/facade/user/update", "*"},
		{"/v1.0/facade/user/view", "*"},
		{"/v1.0/facade/hx/detail", "*"},
		{"/v1.0/facade/signature/token", "*"},
		{"/v1.0/facade/recommend", "*"},
		{"/v1.0/restaurant/first", "*"},
		{"/v1.0/restaurant/session", "*"},
		{"/v1.0/restaurant/api/ip2place", "*"},
		{"/v1.0/restaurant/api/maphtml5", "*"},
		{"/v1.0/restaurant/api/baidumap", "*"},
		{"/v1.0/restaurant/api/latlng2address", "*"},
		{"/v1.0/restaurant/api/address2latlng", "*"},
		{"/v1.0/restaurant/api/searchKeyword", "*"},
		{"/v1.0/restaurant/api/searchCater", "*"},
		{"/v1.0/restaurant/api/webim", "*"},
		{"/v1.0/facade/Restaurant/webIM", "*"},
		{"/v1.0/restaurant/api/searchLife", "*"},
		{"/v1.0/restaurant/api/maphtml1", "*"},
		{"/v1.0/restaurant/api/maphtml2", "*"},
		{"/v1.0/restaurant/api/choujiang", "*"},
		{"/v1.0/restaurant/api/maphtml3", "*"},
		{"/v1.0/restaurant/api/maphtml4", "*"},
		{"/v1.0/restaurant/api/maphtml6", "*"},
		{"/v1.0/restaurant/api/maphtml7", "*"},
		{"/v1.0/restaurant/api/maphtml8", "*"},
		{"/v1.0/restaurant/api/maphtml9", "*"},
		{"/v1.0/restaurant/index", "*"},
		{"/v1.0/restaurant/login", "*"},
		{"/v1.0/user/users", "*"},
		{"/v1.0/user/update", "*"},
		{"/v1.0/restaurant/api/placeSuggestion", "*"},
		{"/v1.0/restaurant/api/excel", "*"},
		{"/v1.0/relationship/group/accept/excel","*"},
		{"/v1.0/facade/makeExcel","*"},
		{"/v1.0/facade/userList","*"},
		{"/v1.0/facade/invite/wx","*"},
		{"/v1.0/restaurant/api/easyEditor","*"},
		{"/v1.0/restaurant/api/placedetail","*"},
		{"/v1.0/restaurant/api/startPay","*"},
		{"/v1.0/restaurant/api/paySuccess","*"},
		{"/v1.0/restaurant/api/paySuccessPage","*"},
		{"/v1.0/restaurant/api/shoppingcart","*"},
		{"/v1.0/restaurant/api/moreshoppingcart","*"},
		{"/v1.0/restaurant/api/useWeixinPay","*"},
		{"/v1.0/restaurant/api/useAlipay","*"},
		{"/v1.0/restaurant/api/consumeProduct","*"},
		{"/v1.0/restaurant/api/showProductDetail","*"},
		{"/v1.0/restaurant/api/createOrder","*"},
		{"/v1.0/restaurant/api/createOrders","*"},
		{"/v1.0/restaurant/api/orderbytime", "*"},
		{"/v1.0/user/addLuckyCount", "*"},
		{"/v1.0/restaurant/card","*"},
		{"/android_apk", "*"},
		{"/v1.0/restaurant/product/id", "*"},
		{"/v1.0/user/dailySign", "*"},
		{"/v1.0/user/usePatiCoin", "*"},
		{"/v1.0/restaurant/api/PatiCoinPay", "*"},
		{"/v1.0/restaurant/api/shoppingcart2", "*"},
		{"/v1.0/gathering/picture", "*"},
		{"/v1.0/facade/invite/example", "*"},
		{"/v1.0/facade/invite/example", "*"}
	};
	
//	所有request都需要验证Token，除了上面ignore的
	private static final String[][] preAuthenticationMustUris = {
			{"/", "*"}
	};
	
	//If request method is POST
//	private static final String[][] preAuthenticationMustUris = {
//		{"/user", "POST"}
//	};

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		try {
			request.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String uri = request.getRequestURI().toString().toLowerCase();
		String method = request.getMethod();

		log.info(String.format("====AuthenticationHeaderFilter.shouldFilter - http method: (%s)", method));
		log.info(String.format("====AuthenticationHeaderFilter.shouldFilter - url", uri));
		for (int i=0; i<preAuthenticationIgnoreUris.length; i++) {
			if (uri.startsWith(preAuthenticationIgnoreUris[i][0].toLowerCase()) &&
					(preAuthenticationIgnoreUris[i][1].equals("*") || method.equalsIgnoreCase(preAuthenticationIgnoreUris[i][1])) ) {
				log.info("this will be not use filter");
				return false;
			}
		}
		if(uri.contains("/v1.0/user/rebate/update")||
				uri.contains("/v1.0/user/account/update")||
				uri.contains("/v1.0/user/account/rebate/updatebyrebateamount")) {
			log.info("该url只允许本地服务访问");
			return true;
		}
		
		return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		String uri = request.getRequestURI().toString().toLowerCase();
		String method = request.getMethod();
		
		log.info(String.format("====AuthenticationHeaderFilter.run - %s request to %s", request.getMethod(), uri));

		//不允许外部访问账户余额接口
		if(uri.contains("/v1.0/user/rebate/update")||
				uri.contains("/v1.0/user/account/update")||
				uri.contains("/v1.0/user/account/rebate/updatebyrebateamount")) {
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(HttpServletResponse.SC_OK);
			ctx.setResponseBody("该接口不允许外部访问");
			ctx.set("isSuccess",false);
			return null;
		}

		// 1. clear userInfo from HTTP header to avoid fraud attack
		ctx.addZuulRequestHeader("user-info", "");
		
		// 2. verify the passed user token
		String userToken = request.getHeader("UserToken");
		
//		log.info(String.format("====AuthenticationHeaderFilter.run - UserToken: %s",userToken));
		Claims claims = null;
		String userInfo = null;
//		log.info("Judge userToken");
		if (userToken != null && !userToken.trim().equals("null") && !userToken.trim().equals("")) {
			try {
				log.info("UserToken has value.....");
	            claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(userToken).getBody();
	            log.info("claims is:"+claims);
	            
	            String userId="";
	            try{
	            	userId = (String)claims.get("userId");
	            	log.info("这里是String类型");
	            }catch(Exception ex){
	            	userId = ((Integer)claims.get("userId")).toString();
	            	
	            	log.info("这里是Integer类型");
	            }
	            
	            log.info("userId is:"+userId);
	            String userName = (String)claims.get("userName");
	            log.info("userId is:"+userId);
	            log.info("userName is:"+userName);
	            userInfo = "{\"id\":\"" + userId + "\", \"name\":\"" + userName + "\"}";
	            log.info("userInfo is:"+userInfo);
	        } catch (SignatureException e) {
	        	this.stopZuulRoutingWithError(ctx, HttpStatus.UNAUTHORIZED, "Invalid User Token for the API (" + request.getRequestURI().toString() + ")");
				return null;
	        } catch (ExpiredJwtException e) {
	        	this.stopZuulRoutingWithError(ctx, HttpStatus.UNAUTHORIZED, "Expired User Token for the API (" + request.getRequestURI().toString() + ")");
				return null;
	        }
		}
		
//		log.info(String.format("=====AuthenticationHeaderFilter.run - userInfo: %s", userInfo));
		// 3. set userInfo to HTTP header
		if (userInfo != null) {
			
			String encodeUserInfo=userInfo;
			
			try {
				encodeUserInfo=URLEncoder.encode(userInfo,"UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ctx.addZuulRequestHeader("user-info", encodeUserInfo);
		}
		
//		log.info(String.format("====AuthenticationHeaderFilter.run - userInfo: %s", request.getHeader("user-info")));
//		log.info(String.format("====AuthenticationHeaderFilter.run - userInfo: %s", ctx.getZuulRequestHeaders().get("user-info")));
		
		// 4. stop the filter chain if userInfo is must
		/*if (userInfo == null) {
			for (int i=0; i<preAuthenticationMustUris.length; i++) {
				if (uri.startsWith(preAuthenticationMustUris[i][0].toLowerCase()) && 
						(preAuthenticationMustUris[i][1].equals("*") || method.equalsIgnoreCase(preAuthenticationMustUris[i][1])) ) {
					log.info(String.format("userInfo is missed for %s", uri));
					
					this.stopZuulRoutingWithError(ctx, HttpStatus.UNAUTHORIZED, "User Login is needed for the API (" + request.getRequestURI().toString() + ")");
					
					return null;
				}
			}
		}*/
		
		return null;
	}
	
	private void stopZuulRoutingWithError(RequestContext ctx, HttpStatus status, String responseText) {
		ctx.removeRouteHost();
		ctx.setResponseStatusCode(status.value());
		ctx.setResponseBody(responseText);
		ctx.setSendZuulResponse(false);
	}
}
