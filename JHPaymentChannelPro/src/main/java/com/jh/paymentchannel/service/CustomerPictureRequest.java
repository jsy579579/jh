package com.jh.paymentchannel.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.YBQuickRegister;
import com.jh.paymentchannel.util.ybhk.Digest;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.PhotoCompressUtil;

@Controller
public class CustomerPictureRequest {

	String baseRequestUrl = "https://skb.yeepay.com/skb-app"; // 基础请求路径

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	/**
	 * 跳转
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/quick/jump/top/xinli")
	public @ResponseBody Object file() throws IOException {
		System.out.println("===============" + ip.toString());
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT,
				ip + "/v1.0/paymentchannel/quick/jump/fill?ip=" + ip + "&key="
						+ "2k2D38072CZD9U6r08yQ7VclZ4b0Z7f0f84sPe4f98U1oJ5J184L26R7bY77" + "&mainCustomerNumber="
						+ "10025093920");
		return maps;
	}

	/**
	 * 跳转到
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/quick/jump/fill")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		System.out.println("又进来了");
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ip = request.getParameter("ip");
		String key = request.getParameter("key");
		String mainCustomerNumber = request.getParameter("mainCustomerNumber");

		model.addAttribute("ips", ip);
		model.addAttribute("key", key);
		model.addAttribute("mainCustomerNumber", mainCustomerNumber);
		System.out.println("==============" + ip.toString());
		return "upload";
	}

	/**
	 *
	 * @param request
	 * 
	 *            *****正面地址******
	 * @param positiveURL
	 * 
	 *            *****反面地址******
	 * @param reverseURL
	 * 
	 *            *****正面图片文件****
	 * @param positiveFile
	 * 
	 *            *****反面图片文件****
	 * @param reverseFile
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/yb/update")
	public @ResponseBody Object findByBankName(HttpServletRequest request,
			@RequestParam(value = "positiveURL") String positiveURL, // 正面
			@RequestParam(value = "positiveFile") MultipartFile positiveFile,
			@RequestParam(value = "key") String hmacKey,
			@RequestParam(value = "mainCustomerNumber") String mainCustomerNumber,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> maps = new HashMap<>();
		System.out.println("进来上传-----------------------------------");
		YBQuickRegister yb = topupPayChannelBusiness.getYBQuickRegisterByIdCard(idCard);
		String result;
		File file = null;
		String zipPath = "/" + idCard + ".jpg";
		PostMethod postMethod = new PostMethod(baseRequestUrl + "/customerPictureUpdate.action");
		HttpClient client = new HttpClient();
		try {
			// 图片转类型，压缩
			InputStream ins = positiveFile.getInputStream();
			file = new File(zipPath);
			inputStreamToFile(ins, file);
			PhotoCompressUtil.compressPhoto(new FileInputStream(file), file, 0.2f);

			String MainCustomerNumber = mainCustomerNumber; // 代理商编码
			String key = hmacKey; // 商户秘钥
			String CustomerNumber = yb.getCustomerNum();
			StringBuffer signature = new StringBuffer();
			signature.append(MainCustomerNumber == null ? "" : MainCustomerNumber)
					.append(CustomerNumber == null ? "" : CustomerNumber);

			System.out.println("source===" + signature.toString());
			String hmac = Digest.hmacSign(signature.toString(), key);
			System.out.println("hmac====" + hmac);

			Part[] parts = new CustomerPicturePartsBuilder().setMainCustomerNumber(MainCustomerNumber)
					.setCustomerNumber(CustomerNumber)

					/**
					 * 单张照片最大512k 需要传什么照片,就传入哪张,最少一张照片
					 */

					// 身份证正面照
					.setIdCardPhoto(file)
					// 身份证背面照
					.setIdCardBackPhoto(file)
					// 银行卡正面照
					.setBankCardPhoto(file)
					// 手持身份证,银行卡和本人合照
					.setPersonPhoto(file)

					.setHmac(hmac).generateParams();

			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
			System.out.println(postMethod.toString());

			int status = client.executeMethod(postMethod);
			if (status == HttpStatus.SC_OK) {
				System.out.println(postMethod.getResponseBodyAsString());
				result = postMethod.getResponseBodyAsString();

				JSONObject resp = JSON.parseObject(result);
				String code = resp.getString("code");
				if ("0000".equals(code)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "上传成功");
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, result);
					return maps;
				}

			} /*
				 * else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status
				 * == HttpStatus.SC_MOVED_TEMPORARILY) { // 从头中取出转向的地址 Header
				 * locationHeader = postMethod .getResponseHeader("location");
				 * String location = null; if (locationHeader != null) {
				 * location = locationHeader.getValue(); System.out.println(
				 * "The page was redirected to:" + location); } else {
				 * System.err.println("Location field value is null."); } } else
				 * { System.out.println("fail======" + status); }
				 */
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放连接
			file.delete();
			postMethod.releaseConnection();

		}
		maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		maps.put(CommonConstants.RESP_MESSAGE, "请求上传失败");
		return maps;
	}

	class CustomerPicturePartsBuilder {

		private List<Part> parts = new ArrayList<Part>(26);

		public Part[] generateParams() {
			return parts.toArray(new Part[parts.size()]);
		}

		/**
		 * @param mainCustomerNumber
		 *            the mainCustomerNumber to set
		 */
		public CustomerPicturePartsBuilder setMainCustomerNumber(String MainCustomerNumber) {
			this.parts.add(new StringPart("MainCustomerNumber", MainCustomerNumber == null ? "" : MainCustomerNumber,
					"UTF-8"));
			return this;
		}

		/**
		 * @param customerNumber
		 *            the mainCustomerNumber to set
		 */
		public CustomerPicturePartsBuilder setCustomerNumber(String CustomerNumber) {
			this.parts.add(new StringPart("CustomerNumber", CustomerNumber == null ? "" : CustomerNumber, "UTF-8"));
			return this;
		}

		/**
		 * @param hmac
		 *            the hmac to set
		 */
		public CustomerPicturePartsBuilder setHmac(String hmac) {
			this.parts.add(new StringPart("hmac", hmac == null ? "" : hmac, "UTF-8"));
			return this;
		}

		// [end] jun.lin 2015-03-30 这里是普通入参

		// [start] jun.lin 2015-03-20 这里是文件入参

		private void configFilePart(File f, FilePart fp) {
			String fileName = f.getName();
			fp.setContentType("image/" + fileName.substring(fileName.lastIndexOf('.') + 1));
			fp.setCharSet("UTF-8");

			System.out.println(fp.getContentType());
		}

		private void configPdfFilePart(File f, FilePart fp) {
			String fileName = f.getName();
			fp.setContentType("application/" + fileName.substring(fileName.lastIndexOf('.') + 1));
			fp.setCharSet("UTF-8");

			System.out.println(fp.getContentType());
		}

		/**
		 * 身份证正面照
		 * 
		 * @param idCardPhoto
		 *            the idCardPhoto to set
		 * @throws FileNotFoundException
		 */
		public CustomerPicturePartsBuilder setIdCardPhoto(File idCardPhoto) throws FileNotFoundException {
			FilePart fp = new FilePart("idCardPhoto", idCardPhoto);
			configFilePart(idCardPhoto, fp);
			this.parts.add(fp);

			return this;
		}

		/**
		 * 身份证背面照
		 * 
		 * @param idCardBackPhoto
		 * @return
		 * @throws FileNotFoundException
		 */
		public CustomerPicturePartsBuilder setIdCardBackPhoto(File idCardBackPhoto) throws FileNotFoundException {
			FilePart fp = new FilePart("idCardBackPhoto", idCardBackPhoto);
			configFilePart(idCardBackPhoto, fp);
			this.parts.add(fp);

			return this;
		}

		/**
		 * 银行卡正面照
		 * 
		 * @param bankCardPhoto
		 *            the bankCardPhoto to set
		 * @throws FileNotFoundException
		 */
		public CustomerPicturePartsBuilder setBankCardPhoto(File bankCardPhoto) throws FileNotFoundException {
			FilePart fp = new FilePart("bankCardPhoto", bankCardPhoto);
			configFilePart(bankCardPhoto, fp);
			this.parts.add(fp);
			return this;
		}

		/**
		 * 身份证,银行卡和本人合照
		 * 
		 * @param personPhoto
		 *            the personPhoto to set
		 * @throws FileNotFoundException
		 */
		public CustomerPicturePartsBuilder setPersonPhoto(File personPhoto) throws FileNotFoundException {
			FilePart fp = new FilePart("personPhoto", personPhoto);
			configFilePart(personPhoto, fp);
			this.parts.add(fp);
			return this;
		}

		/**
		 * 营业执照
		 * 
		 * @param businessLicensePhoto
		 *            the businessLicensePhoto to set
		 * @throws FileNotFoundException
		 */
		public CustomerPicturePartsBuilder setBusinessLicensePhoto(File businessLicensePhoto)
				throws FileNotFoundException {
			FilePart fp = new FilePart("businessLicensePhoto", businessLicensePhoto);
			configFilePart(businessLicensePhoto, fp);
			this.parts.add(fp);
			return this;
		}

		/**
		 * 电子协议
		 * 
		 * @param electronicAgreement
		 * @return
		 * @throws FileNotFoundException
		 */
		public CustomerPicturePartsBuilder setElectronicAgreement(File electronicAgreement)
				throws FileNotFoundException {
			FilePart fp = new FilePart("electronicAgreement", electronicAgreement);
			configPdfFilePart(electronicAgreement, fp);
			this.parts.add(fp);
			return this;
		}
	}

	public void inputStreamToFile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
