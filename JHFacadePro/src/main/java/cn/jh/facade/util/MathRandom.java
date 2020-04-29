package cn.jh.facade.util;

/**  
 * JAVA 返回随机数，并根据概率、比率  
 * @author zsh  
 *  
 */    
public class MathRandom    
{    
 /**  
     * 0~20出现的概率为%90  
     */    
 public static double rate0 = 0.90;   
 
 public static int num0 = 20;   
 /**  
     * 0~40出现的概率为%9 
     */    
 public static double rate1 = 0.09;   
 
 public static int num1 = 50;   
 /**  
     * 0~70出现的概率为%0.5 
     */    
 public static double rate2 = 0.005; 
 
 public static int num2 = 70; 
 /**  
     * 0~100出现的概率为%0.2  
     */    
 public static double rate3 = 0.002;   
 
 public static int num3 = 100; 
 /**  
     * 0~130出现的概率为%0.2  
     */    
 public static double rate4 = 0.002;  
 
 public static int num4 = 130; 
 /**  
     * 0~200出现的概率为%0.1  
     */    
 public static double rate5 = 0.001;    
   
 public static int num5 = 200; 
 /**  
  * Math.random()产生一个double型的随机数，判断一下  
  * 例如0出现的概率为%50，则介于0到0.50中间的返回0  
     * @return int  
     *  
     */    
 public static int PercentageRandom()    
 {    
  double randomNumber;    
  randomNumber = Math.random();    
  if (randomNumber >= 0 && randomNumber <= rate0)    
  {    
   return Util.randomNumber(num0);    
  }    
  else if (randomNumber >= rate0  && randomNumber <= rate0 + rate1)    
  {    
	   return Util.randomNumber(num1);    
  }    
  else if (randomNumber >= rate0 + rate1    
    && randomNumber <= rate0 + rate1 + rate2)    
  {    
	   return Util.randomNumber(num2);    
  }    
  else if (randomNumber >= rate0 + rate1 + rate2    
    && randomNumber <= rate0 + rate1 + rate2 + rate3)    
  {    
	   return Util.randomNumber(num3);    
  }    
  else if (randomNumber >= rate0 + rate1 + rate2 + rate3    
    && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4)    
  {    
	  return Util.randomNumber(num4);     
  }    
  else if (randomNumber >= rate0 + rate1 + rate2 + rate3 + rate4    
    && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4    
      + rate5)    
  {    
	  return Util.randomNumber(num5);      
  }    
  return 1;    
 }    
    
 /**  
  * 测试主程序  
     * @param agrs  
     */    
 /*public static void main(String[] agrs)    
 {    
  int i = 0;   
  //java.lang.Math.random() 返回一个正符号的double值，大于或等于0.0且小于1.0   
  for (i = 0; i <= 100; i++)//打印100个测试概率的准确性    
  {    
   System.out.println(PercentageRandom());    
  }   
  System.out.println( Math.random());    
 }    */
}    
