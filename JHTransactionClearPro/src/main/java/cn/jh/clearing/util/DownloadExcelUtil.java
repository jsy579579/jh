package cn.jh.clearing.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;

import cn.jh.common.utils.ExcelUtil;
import org.springframework.beans.factory.annotation.Value;

public class DownloadExcelUtil {



	private static String downloadPath ;

	@Value("${download.downloadPath}")
	public  void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	
	public static <U,T> String downloadFile(HttpServletRequest request,HttpServletResponse response,List<U> sourceList,T target) throws Exception{
		List<T> targetList = new ArrayList<T>();
		if(sourceList != null && sourceList.size()>0){
			for(U souce:sourceList){
				T targetObj = (T) target.getClass().newInstance();
				BeanUtils.copyProperties(souce, targetObj);
				targetList.add(targetObj);
			}
			ExcelUtil<T> vExcelUtil = new ExcelUtil<T>();
			String fileName = target.getClass().getName().substring(target.getClass().getName().lastIndexOf('.')+1);
            String filePathName = fileName +"_" + System.currentTimeMillis() + ".xlsx";
			vExcelUtil.download(request, response, targetList,fileName,filePathName);
			return downloadPath + filePathName;
		}
		return null;
	}
}
