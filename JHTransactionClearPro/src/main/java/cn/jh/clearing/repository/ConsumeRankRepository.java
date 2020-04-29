package cn.jh.clearing.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.ConsumeRank;
import cn.jh.clearing.pojo.PaymentOrder;
@Repository
public interface ConsumeRankRepository extends JpaRepository<PaymentOrder,String>,JpaSpecificationExecutor<PaymentOrder>{
	
	
	@Query("select paymentOrder.phone , paymentOrder.userid, sum(paymentOrder.amount) from PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.status in (1,4) and paymentOrder.type in (0,1) and paymentOrder.createTime between :strTime and :endTime group by paymentOrder.userid order by sum(paymentOrder.amount) desc")
	List<PaymentOrder> QueryConsumeRank(@Param("brandid") long brandid,@Param("strTime") String strTime,@Param("endTime") String endTime);
	
	@Query("select phone, userid, sum(amount) as rebate from ConsumeRank  where brandid=:brandid and status in (1,4) and type in (0,1) and createTime like %:strTime% group by userid order by sum(amount) desc ")
	List<Object[]> QueryConsumeRank2(@Param("brandid") long brandid,@Param("strTime") String strTime);
	
	@Query("select sum(amount) ,count(*) ,channelname from PaymentOrder  where brandid=:brandid and status in (1,4) and phone in (:phone) and type in (:type) and createTime>=:strTime and createTime<=:endTime group by channelid")
	List<Object[]> QueryConsumeTeambychannelId(@Param("brandid") long brandid ,@Param("phone") String[] phone,@Param("type") String[] type,@Param("strTime") Date strTime,@Param("endTime") Date endTime);
	
	@Query("select sum(amount) as teamSumAmount ,count(*) as teamNum  from PaymentOrder  where brandid=:brandid and status in (1,4) and phone in (:phone) and type in (:type) and createTime>=:strTime and createTime<=:endTime")
	Object[] QueryConsumeTeam(@Param("brandid") long brandid ,@Param("phone") String[] phone,@Param("type") String[] type,@Param("strTime") Date strTime,@Param("endTime") Date endTime);

}
