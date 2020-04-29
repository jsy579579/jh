package com.jh.user.business.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.jh.user.pojo.UserRealtion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserRelationBusiness;
import com.jh.user.pojo.UserAgentChange;
import com.jh.user.repository.UserAgentChangeRepository;
import com.jh.user.repository.UserRelationRepository;


@Service
public class UserRelationBusinessImpl implements UserRelationBusiness{

	@Autowired
	private UserAgentChangeRepository userAgentChangeRepository;

	@Autowired
	private UserRelationRepository userRelationRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private EntityManager em;



	@Override
	public Page<UserAgentChange> findUserAgentChangeByUserid(long userid, Pageable pageAble) {
		return userAgentChangeRepository.findUserAgentChangeByUserid(userid, pageAble);
	}

	@Override
	public Page<UserAgentChange> findUserAgentChange(Pageable pageAble) {
		return userAgentChangeRepository.findAll(pageAble);
	}

	@Transactional
	@Override
	public UserAgentChange saveUserAgentChange(UserAgentChange userAgentChange) {
		
		UserAgentChange result = userAgentChangeRepository.save(userAgentChange);
		em.flush();
		return result;
	}

	

	@Override
	public Page<UserAgentChange> findUserAgentChange(Date startTime,
			Pageable pageAble) {
		// TODO Auto-generated method stub
		return userAgentChangeRepository.findUserAgentChangeByStartTime(startTime, pageAble);
	}

	@Override
	public Page<UserAgentChange> findUserAgentChange(Date startTime,
			Date endTime, Pageable pageAble) {
		
		return userAgentChangeRepository.findUserAgentChangeByStartEndTime(startTime, endTime, pageAble);
	}

	@Override
	public List<Long> findUserAgentChangeByTimeAndPhone(String startTime, String endTime, String phone) {
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		
		StringBuffer sql = new StringBuffer("select first_user_id from t_user_relation  where pre_user_phone="+phone+" and level!=1");
		
		if(startTime!=null && !"".equals(startTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')>='"+startTime+"'");
		}
		
		if(endTime!=null && !"".equals(endTime)) {
			sql.append(" and date_format(create_time,'%y-%M-%d')<='"+endTime+"'");
		}
		
		List<Long> list = jdbcTemplate.query(sql.toString(), new RowMapper<Long>() {

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong("first_user_id");
			}
			
		});
		return list;
	}

	@Override
	public List<Long> findUserAgentChangeByTimeAndPhoneAndLevel(String startTime, String endTime, String phone, String level) {
		
		Map<String, String> map = new HashMap<String, String>();
		
		StringBuffer sql = new StringBuffer("select first_user_id from t_user_relation  where pre_user_phone="+phone+" and level="+level);
		
		if(startTime!=null && !"".equals(startTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')>='"+startTime+"'");
		}
		
		if(endTime!=null && !"".equals(endTime)) {
			sql.append(" and date_format(create_time,'%Y-%m-%d')<='"+endTime+"'");
		}
		
		List<Long> list = jdbcTemplate.query(sql.toString(), new RowMapper<Long>() {

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong("first_user_id");
			}
			
		});
		
		return list;
	}

	@Override
	public Long[] findByCount(Long countt, Long firstUser,int grade) {
		return userRelationRepository.findByCount(countt,firstUser,grade);
	}

	@Override
	public Long[] findByCounts(Long countt, Long firstUser, int[] grade) {
		return userRelationRepository.findByCounts(countt,firstUser,grade);
	}

    @Override
    public Long[] queryFansByPreUserIdAndLevelAndCreateTime(long userId, int level, String todayTime) {
        return userRelationRepository.findByPreUserIdAndLevelAndCreateTime(userId,level,todayTime);
    }

    @Override
    public Long[] queryFansByPreUserIdAndLevel(long userId, int level) {
        return userRelationRepository.findByPreUserIdAndLevel(userId,level);
    }

    @Override
    public Long[] queryAllByPreUserIdAndCreateTime(long userId, String todayTime) {
        return userRelationRepository.findByPreUserIdAndCreateTime(userId,todayTime);
    }

    @Override
    public Long[] findByPreUserId(long userId) {
        return userRelationRepository.findByPreUserId(userId);
    }

}
