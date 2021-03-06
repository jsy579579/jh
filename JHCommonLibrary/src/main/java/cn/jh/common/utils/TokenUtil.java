package cn.jh.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Token类
 */
public class TokenUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(TokenUtil.class);

	/**
	 * 颁发Token
	 * @param userId
	 * @param brandid
	 * @param phone
	 * @return
	 */
	public  static String  createToken(long userId, long brandid, String phone){
		String userToken = Jwts.builder()
				.setSubject("JuHei")
				.setExpiration(new Date(new Date().getTime() + 1000*60*60*24*600))
				.claim("userId", userId)
				.claim("brandId",brandid)
				.claim("phone",phone)
		.signWith(SignatureAlgorithm.HS512, CommonConstants.SECRETKEY).compact();
		
		return userToken;
	}

	/**
	 * 获取用户id
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public  static long getUserId(String token) throws Exception{
		Claims claims = Jwts.parser().setSigningKey(CommonConstants.SECRETKEY).parseClaimsJws(token).getBody();
		Map map  = new HashMap();
		long userId = (Integer)claims.get("userId");
	    return userId;
	}

	/**
	 *  获取用户电话
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public  static String getUserPhone(String token) throws Exception{
		
		Claims claims = Jwts.parser().setSigningKey(CommonConstants.SECRETKEY).parseClaimsJws(token).getBody();
		Map map  = new HashMap();
		String phone = (String)claims.get("phone");
	    return phone;
	}

	/**
	 * 获取临牌
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public  static long getBrandid(String token) throws Exception{
		
		Claims claims = Jwts.parser().setSigningKey(CommonConstants.SECRETKEY).parseClaimsJws(token).getBody();
		Map map  = new HashMap();
		long userId = (Integer)claims.get("brandId");
	    return userId;
	}

	/**
	 * 验证Token
	 * @param token
	 * @return
	 */
	public static Object  validToken(String token) throws Exception{
		Map map = new HashMap();
		long userId;
		try {
			userId = TokenUtil.getUserId(token);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_TOKEN);
			map.put(CommonConstants.RESP_MESSAGE, "token无效");
			return map;		
		}
		return userId;
	
	}
	
	
	
}
