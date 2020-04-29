package com.jh.paymentgateway.pojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import net.sf.json.JSONObject;

public class RegisterPartsBuilderss {

	private List<Part> parts = new ArrayList<Part>(31);

	public Part[] generateParams() {
		return parts.toArray(new Part[parts.size()]);
	}

	public RegisterPartsBuilderss setMch_id(String mch_id) {
		this.parts.add(new StringPart("mch_id", mch_id == null ? "" : mch_id, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setApp_id(String app_id) {
		this.parts.add(new StringPart("app_id", app_id == null ? "" : app_id, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setRate(String rate) {
		this.parts.add(new StringPart("rate", rate == null ? "" : rate, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setMch_no(String mch_no) {
		this.parts.add(new StringPart("mch_no", mch_no == null ? "" : mch_no, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setSign(String sign) {
		this.parts.add(new StringPart("sign", sign == null ? "" : sign, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setCardNo(String bank_card_no) {
		this.parts.add(new StringPart("bank_card_no", bank_card_no == null ? "" : bank_card_no, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setRealname(String realname) {
		this.parts.add(new StringPart("realname", realname == null ? "" : realname, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setBankName(String bank_name) {
		this.parts.add(new StringPart("bank_name", bank_name == null ? "" : bank_name, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setPhone(String bank_card_mobile) {
		this.parts.add(new StringPart("bank_card_mobile", bank_card_mobile == null ? "" : bank_card_mobile, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setidCard(String id_card) {
		this.parts.add(new StringPart("id_card", id_card == null ? "" : id_card, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setAreaCode(String area_code) {
		this.parts.add(new StringPart("area_code", area_code == null ? "" : area_code, "UTF-8"));
		return this;
	}

	private void configFilePart(File f, FilePart fp) {
		String fileName = f.getName();
		fp.setContentType("image/" + fileName.substring(fileName.lastIndexOf('.') + 1));
		fp.setCharSet("UTF-8");

		System.out.println(fp.getContentType());
	}

	public RegisterPartsBuilderss setIdCardFront(File idcard_pic_front) throws FileNotFoundException {
		FilePart fp = new FilePart("idcard_pic_front", idcard_pic_front);
		configFilePart(idcard_pic_front, fp);
		this.parts.add(fp);

		return this;
	}

	public RegisterPartsBuilderss setIdCardBack(File idcard_pic_back) throws FileNotFoundException {
		FilePart fp = new FilePart("idcard_pic_back", idcard_pic_back);
		configFilePart(idcard_pic_back, fp);
		this.parts.add(fp);

		return this;
	}

	public RegisterPartsBuilderss setCardNoFront(File bankcard_pic_front) throws FileNotFoundException {
		FilePart fp = new FilePart("bankcard_pic_front", bankcard_pic_front);
		configFilePart(bankcard_pic_front, fp);
		this.parts.add(fp);

		return this;
	}

	public RegisterPartsBuilderss setCardNoAndIdCard(File idcard_pic_middle) throws FileNotFoundException {
		FilePart fp = new FilePart("idcard_pic_middle", idcard_pic_middle);
		configFilePart(idcard_pic_middle, fp);
		this.parts.add(fp);

		return this;
	}

	// 设置费率
	public RegisterPartsBuilderss setSubM(String sub_mch_no) {
		this.parts.add(new StringPart("sub_mch_no", sub_mch_no == null ? "" : sub_mch_no, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setSinglePrice(String single_price) {
		this.parts.add(new StringPart("single_price", single_price == null ? "" : single_price, "UTF-8"));
		return this;
	}

	// 支付
	public RegisterPartsBuilderss setAmount(String amount) {
		this.parts.add(new StringPart("amount", amount == null ? "" : amount, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setPaybankCard(String pay_bank_no) {
		this.parts.add(new StringPart("pay_bank_no", pay_bank_no == null ? "" : pay_bank_no, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setMcc(String mcc) {
		this.parts.add(new StringPart("mcc", mcc == null ? "" : mcc, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setWithdrawCardno(String withdraw_card_no) {
		this.parts.add(new StringPart("withdraw_card_no", withdraw_card_no == null ? "" : withdraw_card_no, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setSubMchRate(String sub_mch_rate) {
		this.parts.add(new StringPart("sub_mch_rate", sub_mch_rate == null ? "" : sub_mch_rate, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setNotifyUrl(String notify_url) {
		this.parts.add(new StringPart("notify_url", notify_url == null ? "" : notify_url, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setType(String type) {
		this.parts.add(new StringPart("type", type == null ? "" : type, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setBase64Data(String base64_data) {
		this.parts.add(new StringPart("base64_data", base64_data == null ? "" : base64_data, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setBankChannelNo(String bank_code) {
		this.parts.add(new StringPart("bank_code", bank_code == null ? "" : bank_code, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setBankLines(String bank_lines) {
		this.parts.add(new StringPart("bank_lines", bank_lines == null ? "" : bank_lines, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setExpire(String expire_date) {
		this.parts.add(new StringPart("expire_date", expire_date == null ? "" : expire_date, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setSecurityCode(String cvn2) {
		this.parts.add(new StringPart("cvn2", cvn2 == null ? "" : cvn2, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setSmsCode(String code) {
		this.parts.add(new StringPart("code", code == null ? "" : code, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setQuery_type(String query_type) {
		this.parts.add(new StringPart("query_type", query_type == null ? "" : query_type, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setAddress(String address) {
		this.parts.add(new StringPart("address", address == null ? "" : address, "UTF-8"));
		return this;
	}
	
}
