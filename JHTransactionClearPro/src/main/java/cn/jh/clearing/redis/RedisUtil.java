package cn.jh.clearing.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import cn.jh.common.utils.CommonConstants;

@Component
public class RedisUtil {
	
	 @Autowired
	 private RedisTemplate redisTemplate;
	 
		/**
		 * 缓存限制用户连续请求时间
		 * @param cacheName
		 * @param cacheTime
		 * @param keys
		 * @return
		 */
		public Map<String,Object> restClientLimit(String cacheName,long cacheTime,String ...keys){
			Map<String,Object> map = new HashMap<String,Object>();
			boolean hasKey = false;
			String key = cacheName+":";
			for(int i = 0;i < keys.length;i++){
				key = key + keys[i];
			}
			ValueOperations<String, String> operations = redisTemplate.opsForValue();
			hasKey = redisTemplate.hasKey(key);
			if(hasKey){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "操作过于频繁,请"+cacheTime+"秒后重试!");
				return map;
			}
			operations.set(key, key, cacheTime, TimeUnit.SECONDS);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "验证通过");
			return map;
		}
}
