package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.QrCodePicture;

@Repository
public interface QrCodePictureRepository extends JpaRepository<QrCodePicture, Long>, JpaSpecificationExecutor<QrCodePicture> {

	List<QrCodePicture> getQrCodePictureByBrandIdAndStatus(int brandId, int status);

	@Query("select qc from QrCodePicture qc where qc.brandId=:brandId and qc.id in (:id)")
	List<QrCodePicture> getQrCodePictureByBrandIdAndId(@Param("brandId")int brandId, @Param("id")long[] id);

	@Query("select qc.qrcodeUrl from QrCodePicture qc where qc.brandId=:brandId and qc.status=:status")
	List<String> getQrCodeUrlByBrandIdAndStatus(@Param("brandId")int brandId, @Param("status")int status);

}
