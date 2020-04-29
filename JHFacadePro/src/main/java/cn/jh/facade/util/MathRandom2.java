package cn.jh.facade.util;





/** 
 * 
 * JAVA 返回随机数，并根据概率、比率 
 * @author zsh 
 * 
 */  
public class MathRandom2
{  
 /** 
     * 0出现的概率为%50 
     */  
 public static double rate0 = 0.50;  
 /** 
     * 1出现的概率为%20 
     */  
 public static double rate1 = 0.20;  
 /** 
     * 2出现的概率为%15 
     */  
 public static double rate2 = 0.15;  
 /** 
     * 3出现的概率为%10 
     */  
 public static double rate3 = 0.10;  
 /** 
     * 4出现的概率为%4 
     */  
 public static double rate4 = 0.04;  
 /** 
     * 5出现的概率为%1 
     */  
 public static double rate5 = 0.01;  
  
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
   return randomNumber(300);  
  }  
  else if (randomNumber >= rate0 / 100 && randomNumber <= rate0 + rate1)  
  {  
	  return randomNumber(500);  
  }  
  else if (randomNumber >= rate0 + rate1  
    && randomNumber <= rate0 + rate1 + rate2)  
  {  
   return randomNumber(600);  
  }  
  else if (randomNumber >= rate0 + rate1 + rate2  
    && randomNumber <= rate0 + rate1 + rate2 + rate3)  
  {  
   return randomNumber(700);  
  }  
  else if (randomNumber >= rate0 + rate1 + rate2 + rate3  
    && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4)  
  {  
   return randomNumber(800);  
  }  
  else if (randomNumber >= rate0 + rate1 + rate2 + rate3 + rate4  
    && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4  
      + rate5)  
  {  
   return  randomNumber(1000);
  }  
  return 1;  
 }  
  
 
 public static int randomNumber(int max) {
 	if(max==0)
     max=20;
     int random=(int)(Math.random()*max+1);
     if(random<50){
     	random=(int)(Math.random()*max+1);
     }
     return random;
 }
 /** 
  * 测试主程序 
     * @param agrs 
     */  
// public static void main(String[] agrs)  
// {  
//  int i = 0;  
//  MathRandom2 a = new MathRandom2();  
//  for (i = 0; i <= 1000; i++)//打印100个测试概率的准确性  
//  {  
//   System.out.println(a.PercentageRandom());  
//  }  
// }  
}  

   
