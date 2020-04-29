package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.QrCodePicture;

public interface QrCodePictureBusiness {
	
	public void createQrCodePicture(QrCodePicture qrCodePicture);
	
	public List<QrCodePicture> getQrCodePictureByBrandIdAndStatus(int brandId, int status);
	
	public List<String> getQrCodeUrlByBrandIdAndStatus(int brandId, int status);
	
	public List<QrCodePicture> getQrCodePictureByBrandIdAndId(int brandId, long[] id);
	
}
