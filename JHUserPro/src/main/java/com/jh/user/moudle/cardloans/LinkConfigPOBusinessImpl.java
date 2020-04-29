package com.jh.user.moudle.cardloans;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jh.user.util.AliOSSUtil;

import cn.jh.common.utils.StringUtil;

@Service
public class LinkConfigPOBusinessImpl implements ILinkConfigPOBusiness {

	@Autowired
	private ILinkConfigPORepository iLinkConfigPORepository;
	
	@Autowired
	private AliOSSUtil aliOSSUtil;
	
	@Override
	public Page<LinkConfigPO> findList(String brandId, String linkType, String linkClassify, String onOff,Pageable pageable) {
		return iLinkConfigPORepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (StringUtil.isNotNullString(brandId)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(LinkConfigPO_.brandId), brandId)));
			}
            
            if (StringUtil.isNotNullString(linkClassify)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(LinkConfigPO_.linkClassify), linkClassify)));
			}
            
            if (StringUtil.isNotNullString(linkType)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(LinkConfigPO_.linkType), linkType)));
			}
            
            if (StringUtil.isNotNullString(onOff)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(LinkConfigPO_.onOff), onOff)));
			}
            return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageable);
	}
	
	private String getLinkPicturePrefixUrl(String brandId) {
		return AliOSSUtil.LINK_PICTURES+"-"+brandId+"-"+System.currentTimeMillis();
	}

	@Override
	public LinkConfigPO createNewOne(LinkConfigPO linkConfigPO,MultipartFile file,MultipartFile file2) {
		String linkPicture = null;
		String linkPicture2 = null;
		String ossObjectName = null;
		String ossObjectName2 = null;
		if (file != null && !file.isEmpty()) {
			linkPicture = this.uploadFileToOSS(file, linkConfigPO.getBrandId());
			ossObjectName = linkPicture.substring(linkPicture.lastIndexOf("/")+1, linkPicture.length());
		}
		if (file2 != null && !file2.isEmpty()) {
			linkPicture2 = this.uploadFileToOSS(file2, linkConfigPO.getBrandId());
			ossObjectName2 = linkPicture.substring(linkPicture.lastIndexOf("/")+1, linkPicture.length());
		}
		try {
			linkConfigPO.setLinkPicture(linkPicture);
			linkConfigPO.setDetailPicture(linkPicture2);
			return this.save(linkConfigPO);
		} catch (Exception e) {
			e.printStackTrace();
			if (ossObjectName != null) {
				aliOSSUtil.deleteFileFromOss(ossObjectName);
			}
			if (ossObjectName2 != null) {
				aliOSSUtil.deleteFileFromOss(ossObjectName2);
			}
			return null;
		}
	}
	
	@Override
	public LinkConfigPO findById(Long id) {
		return iLinkConfigPORepository.findOne(id);
	}

	@Override
	public void delete(LinkConfigPO linkConfigPO) {
		this.deleteFileOnOSS(linkConfigPO.getLinkPicture());
		this.deleteFileOnOSS(linkConfigPO.getDetailPicture());
		this.deleteOne(linkConfigPO);
	}
	
	private String uploadFileToOSS(MultipartFile file,String brandId) {
		String fileName = file.getOriginalFilename();
		fileName = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		String ossObjectName = this.getLinkPicturePrefixUrl(brandId)+fileName;
		try {
			aliOSSUtil.uploadStreamToOss(ossObjectName, file.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return aliOSSUtil.getFileUrl(ossObjectName);
	}
	
	private void deleteFileOnOSS(String fileUrl) {
		if (StringUtil.isNotNullString(fileUrl)) {
			String suffix = fileUrl.substring(fileUrl.lastIndexOf("/")+1, fileUrl.length());
			if (suffix != null && StringUtil.isNotNullString(suffix)) {
				aliOSSUtil.deleteFileFromOss(suffix);
			}
		}
	}
	
	@Transactional
	private void deleteOne(LinkConfigPO linkConfigPO) {
		iLinkConfigPORepository.delete(linkConfigPO);
	}
	
	@Transactional
	private LinkConfigPO save(LinkConfigPO linkConfigPO) {
		return iLinkConfigPORepository.saveAndFlush(linkConfigPO);
	}

	@Override
	public LinkConfigPO updateLinkConfigOnOff(LinkConfigPO linkConfigPO, String onOff) {
		linkConfigPO.setOnOff(onOff);
		return this.save(linkConfigPO);
	}

	@Override
	public LinkConfigPO updateLinkConfig(LinkConfigPO linkConfigPO,MultipartFile file,MultipartFile file2){
		if (file != null && !file.isEmpty()) {
			this.deleteFileOnOSS(linkConfigPO.getLinkPicture());
			String fileUrl = this.uploadFileToOSS(file, linkConfigPO.getBrandId());
			linkConfigPO.setLinkPicture(fileUrl);
		}
		if (file2 != null && !file2.isEmpty()) {
			this.deleteFileOnOSS(linkConfigPO.getDetailPicture());
			String fileUrl = this.uploadFileToOSS(file2, linkConfigPO.getBrandId());
			linkConfigPO.setDetailPicture(fileUrl);
		}
		return this.save(linkConfigPO);
	}
}
