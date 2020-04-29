package com.jh.user.moudle.cardloans;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ILinkConfigPOBusiness {

	Page<LinkConfigPO> findList(String brandId, String linkType, String linkClassify, String onOff, Pageable pageable);

	LinkConfigPO createNewOne (LinkConfigPO linkConfigPO,MultipartFile file,MultipartFile file2);

	LinkConfigPO findById(Long id);

	void delete(LinkConfigPO linkConfigPO);

	LinkConfigPO updateLinkConfigOnOff(LinkConfigPO linkConfigPO, String onOff);

	LinkConfigPO updateLinkConfig(LinkConfigPO linkConfigPO,MultipartFile file,MultipartFile file2);

}
