package cn.jh.common.utils;

/**
 * 定义移动端请求的所有可能类型
 */
public class StringUtil {
	private final static String[] agent = { "Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser" }; // 定义移动端请求的所有可能类型

	/**
	 * 判断User-Agent 是不是来自于手机
	 * @param ua
	 * @return
	 */
	public static boolean checkAgentIsMobile(String ua) {
		boolean flag = false;
		if (!ua.contains("Windows NT") || (ua.contains("Windows NT") && ua.contains("compatible; MSIE 9.0;"))) {
			// 排除 苹果桌面系统
			if (!ua.contains("Windows NT") && !ua.contains("Macintosh")) {
				for (String item : agent) {
					if (ua.contains(item)) {
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}
	
	public static boolean isNullString(String str) {
		return str==null||"".equals(str.trim())||"null".equalsIgnoreCase(str.trim());
	}
	
	public static boolean isNotNullString(String str) {
		return !(str==null||"".equals(str.trim())||"null".equalsIgnoreCase(str.trim()));
	}
}
