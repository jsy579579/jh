package cn.jh.common.utils;




import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * MD5加密
 * 
 * @version 1.0
 * @author
 * 
 */
public class Md5Util {
	 public static String byteToHEX(byte ib) {

		    //���Сд��

		    char[] DigitNormal = {
		        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',

		        'a', 'b', 'c', 'd', 'e', 'f'};

		    //����д��

		    /* char[] DigitCap = { '0','1','2','3','4','5','6','7','8','9',
		                    'A','B','C','D','E','F' }; */

		    char[] ob = new char[2];

		    ob[0] = DigitNormal[ (ib >>> 4) & 0X0F];

		    // ob[0] = DigitCap[(ib >>> 4) & 0X0F]; //��д

		    ob[1] = DigitNormal[ib & 0X0F];

		    // ob[1] = DigitCap[ib & 0X0F]; //��д

		    return new String(ob);

		  }
    /**
     * MD5加密
     * @param plainText
     * @return 
     */
    public static String generateMD5String(String plainText){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            
            int i;
            StringBuffer sb=new StringBuffer();
            for(int offset=0;offset<b.length;offset++){
                i=b[offset];
                if(i<0){
                    i+=256;
                }
                if(i<16){
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i));
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            //log.error...
            System.out.println("generate MD5 String error! "+e.getMessage());
            //throw e;
        }
        return null;
    }
    
    public static String getMD5(String source) {
		String s = null;
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字�?
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");

			md.update(source.getBytes());
			byte tmp[] = md.digest(); // MD5 的计算结果是�?�? 128 位的长整数，
			// 用字节表示就�? 16 个字�?
			char str[] = new char[16 * 2]; // 每个字节�? 16 进制表示的话，使用两个字符，
			// �?以表示成 16 进制�?�? 32 个字�?
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第�?个字节开始，�? MD5 的每�?个字�?
				// 转换�? 16 进制字符的转�?
				byte byte0 = tmp[i]; // 取第 i 个字�?
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中�? 4 位的数字转换,
				// >>> 为�?�辑右移，将符号位一起右�?
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中�? 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符�?

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
    
    
    //19pay提供的MD5加密方式-仅限手机充值接口使用--begin
    
	public static String getKeyedDigest(String strSrc, String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes("UTF8"));
            
            String result="";
            byte[] temp;
            temp=md5.digest(key.getBytes("UTF8"));
    		for (int i=0; i<temp.length; i++){
    			result+=Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
    		}
    		
    		return result;
    		
        } catch (NoSuchAlgorithmException e) {
        	
        	e.printStackTrace();
        	
        }catch(Exception e)
        {
          e.printStackTrace();
        }
        return null;
    }
	 /**
	   * 对字符串数组进行排序 
	   * @param keys
	   * @return
	   * */
	  public static Object[] getUrlParam(Object[] keys){
	     
	    for (int i = 0; i < keys.length - 1; i++) {
	      for (int j = 0; j < keys.length - i -1; j++) {
	    	  Object pre = keys[j];
	    	  Object next = keys[j + 1];
	        if(isMoreThan(String.valueOf(pre),String.valueOf(next))){
	        	Object temp = pre;
	          keys[j] = next;
	          keys[j+1] = temp;
	        }
	      }
	    }
	    return keys;
	  }
	 
	  /**
	   * 比较两个字符串的大小，按字母的ASCII码比较
	   * @param pre
	   * @param next
	   * @return
	   * */
	  private static boolean isMoreThan(String pre, String next){
	    if(null == pre || null == next || "".equals(pre) || "".equals(next)){
	      return false;
	    }
	     
	    char[] c_pre = pre.toCharArray();
	    char[] c_next = next.toCharArray();
	     
	    int minSize = Math.min(c_pre.length, c_next.length);
	     
	    for (int i = 0; i < minSize; i++) {
	      if((int)c_pre[i] > (int)c_next[i]){
	        return true;
	      }else if((int)c_pre[i] < (int)c_next[i]){
	        return false;
	      }
	    }
	    if(c_pre.length > c_next.length){
	      return true;
	    }
	     
	    return false;
	  }
	/*map的key按ASCII排序后连接非空字符串*/
	public static String mapKey2String(Map<String,Object> map) {
		StringBuffer param = new StringBuffer();
		 for (Iterator it = map.keySet().iterator(); it.hasNext();)
	     {
	      String key = (String)it.next();
	      Object value = (Object)map.get(key);
	      param.append(String.valueOf(value));
	     }
		return param.toString();
	}
	/*map的value按ASCII排序后连接非空字符串*/
	public static String MapValuetoString(Map<String,Object> map) {
		 StringBuffer param = new StringBuffer();
		 Object[] group = new Object[map.size()];
		 int i=0;
	     for (Iterator it = map.keySet().iterator(); it.hasNext();)
	     {
	      String key = (String)it.next();
	      Object value = (Object)map.get(key);
	      group[i] = value;
	      i++;                                                                                                            
	     }
	     Object[] valuegroup = Md5Util.getUrlParam(group);
		  for(int j=0;j<valuegroup.length;j++) {
			  param.append(String.valueOf(valuegroup[j]));
		  }
		  String str = param.toString();
		return str;
	}
	
	 //19pay提供的MD5加密方式-仅限手机充值接口使用--end

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String mi;
        String s = "123456";
        //第二个参数请填空字符串
		mi=Md5Util.getMD5(s);
		System.out.println("mi:"+mi);
		Map<String, Object> map = new TreeMap<String, Object>(
		         new Comparator<String>() {
		             @Override
					public int compare(String obj1, String obj2) {
		             return obj1.compareTo(obj2);
		           }
		        });
				map.put("bankNo", "dd");		//开户卡号   @
				map.put("bankName", ""); //开户行名称(请参考总行.xlsx)  @
				map.put("payBankCardIdNo", "aa"); //付款信用卡持卡人身份证号  @
				map.put("cardNo", "cc"); //开户人身份证号码  @
				map.put("tel", "bb");  //银行预留手机号     @
				String signMsg = Md5Util.MapValuetoString(map);
				System.out.println("拼接字符串——signMsg"+signMsg);
				System.out.println(map);
			
	}
	
}
