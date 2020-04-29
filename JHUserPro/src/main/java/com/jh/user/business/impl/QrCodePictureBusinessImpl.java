package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.QrCodePictureBusiness;
import com.jh.user.pojo.QrCodePicture;
import com.jh.user.repository.QrCodePictureRepository;

@Service
public class QrCodePictureBusinessImpl implements QrCodePictureBusiness {

	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private QrCodePictureRepository qrCodePictureRepository;

	@Transactional
	@Override
	public void createQrCodePicture(QrCodePicture qrCodePicture) {
		qrCodePictureRepository.saveAndFlush(qrCodePicture);
	}

	@Override
	public List<QrCodePicture> getQrCodePictureByBrandIdAndStatus(int brandId, int status) {
		em.clear();
		List<QrCodePicture> result = qrCodePictureRepository.getQrCodePictureByBrandIdAndStatus(brandId, status);
		return result;
	}

	@Override
	public List<QrCodePicture> getQrCodePictureByBrandIdAndId(int brandId, long[] id) {
		em.clear();
		List<QrCodePicture> result = qrCodePictureRepository.getQrCodePictureByBrandIdAndId(brandId, id);
		return result;
	}

	@Override
	public List<String> getQrCodeUrlByBrandIdAndStatus(int brandId, int status) {
		em.clear();
		List<String> result = qrCodePictureRepository.getQrCodeUrlByBrandIdAndStatus(brandId, status);
		return result;
	}
	
	
	
}
