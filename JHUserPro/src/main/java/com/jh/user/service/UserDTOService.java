package com.jh.user.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.user.business.impl.UserDTOBusinessImpl;
import com.jh.user.pojo.UserDTO;

//用于转移用户上传的图片
@Controller
@EnableAutoConfiguration
public class UserDTOService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserDTOService.class);

	
	@Autowired
	UserDTOBusinessImpl uDTOImpl;
	
	@Value("${user.realname.uploadpath}")
	private String uploadpath;

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/user/image/transferdata")
	public @ResponseBody Object transferData(HttpServletRequest request,
			@RequestParam(value = "merchant_no", defaultValue = "/usr/share/nginx/html", required = false) String uploadpath,
			 @RequestParam(value = "date", defaultValue = "/mnt/share/nginx/html", required = false) String targetpath
			) {
		// 1.查User表中所有real_name_status!=3的数据
		List<UserDTO> users = uDTOImpl.findByRealNameStatus();
		Map map = new HashMap();
		long s1 = System.currentTimeMillis();
		
//		遍历所有用户,取得每一个非实名用户的phone和brandId
		for (UserDTO user : users) {
			try {
				// 循环
				String phone = user.getPhone();
				// 4.找到对应手机号的目录
				String oldPath = uploadpath + "/" + phone;
				// String oldPath = "D:/123" + "/" + i;
//				取得用户所在的文件夹对象
				File oldForld = new File(oldPath);
//				取得用户文件夹下的所有文件的文件名
				String[] strs = oldForld.list();
//				用户所在贴牌Id
				String brandId = user.getBrandId();
				// String newPath = "D:/123" + "/" + brandId + "/" + i + "/" ;
//				存贮的新路径
				String newPath = targetpath + "/" + brandId + "/realname/" + phone;
				BufferedOutputStream out = null;
				BufferedInputStream in = null;
				try {
//					遍历所有文件
					for (String s : strs) {
						// System.out.println(s);
//						取得旧目录文件对象
						File oldFile = new File(oldPath, s);
//						判断是否是文件,是则开始复制
						if(oldFile.isFile()){
							in = new BufferedInputStream(new FileInputStream(oldFile));
//							创建新的存放路径
							File newForld = new File(newPath);
							if (!newForld.exists()) {
								newForld.mkdirs();
							}

							File newFile = new File(newPath, s);
							newFile.createNewFile();
							out = new BufferedOutputStream(new FileOutputStream(newFile));

							byte[] data = new byte[1024];
							int n = 0;

							while ((n = in.read(data)) != -1) {
								out.write(data, 0, n);
							}
							out.flush();
						}
						
						// System.out.println(oldFile.delete());
					}
					// oldForld.delete();
					out.close();
					in.close();
				} catch (Exception e) {
					
					continue;
				}
				
			} catch (Exception e) {
				
				continue;
				
			}
			
		}
		LOG.error(map.toString());
		map.put("response", "完成");
		map.put("用时", System.currentTimeMillis() - s1);
		return map;
	}



}
