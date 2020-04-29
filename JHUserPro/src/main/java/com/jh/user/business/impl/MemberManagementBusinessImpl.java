package com.jh.user.business.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.jh.user.business.MemberManagementBusiness;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserManagement;
import com.jh.user.redis.RedisUtil;

@Service
public class MemberManagementBusinessImpl implements MemberManagementBusiness {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	RedisUtil redisUtil;

	@Override
	public Map<String, Object> queryAlluser(String brandId, String phone, String userName, String grade,
			String realStatus, String role, String profitDesc, String referralsDesc, Pageable pageAble) {
		Map<String, Object> maps = new HashMap<>();
		// 页显示条数
		int pageNum = pageAble.getPageSize();
		// 当前页码
		int currentPage = pageAble.getPageNumber();
		StringBuffer key = new StringBuffer("" + pageNum + currentPage);
		StringBuffer sql = new StringBuffer(
				"select u.id,u.fullname,u.phone,role.role_name,u.brand_name,u.grade,u.real_name_status,count(relation.level) as person from t_user u left join t_user_relation relation on relation.pre_user_id=u.id and relation.level='1' left join t_user_role r on r.user_id = u.id left join t_role role on role.id = r.role_id where 1=1");

		if (phone != null && !phone.equals("")) {
			sql.append(" and phone='" + phone + "'");
			key.append(phone);
		}
		if (userName != null && !userName.equals("")) {
			sql.append(" and fullname='" + userName + "'");
			key.append(userName);
		}
		
		// 代理商
		if (role != null && !role.equals("")) {
			sql.append(" and r.role_id='" + role + "'");
			key.append(role);
		}
		// 贴牌ID
		if (brandId != null && !brandId.equals("")) {
			sql.append(" and u.brand_id='" + brandId + "'");
			key.append(brandId);
		}

		if (realStatus != null && !realStatus.equals("")) {
			sql.append(" and u.real_name_status='" + realStatus + "'");
			key.append(realStatus);
		}
		if (grade != null && !grade.equals("")) {
			sql.append(" and u.grade='" + grade + "'");
			key.append(grade);
		}
		sql.append(" group by u.id");
		if (referralsDesc != null && !referralsDesc.equals("")) {
			sql.append(" order by person desc");
			key.append(referralsDesc);
		}
		// redis作缓存
		List<UserManagement> result = redisUtil.getUserManagement(key.toString());
		if (result != null && result.size() > 0) {
			maps.put("pageNum", pageNum); // 每页显示条数
			maps.put("currentPage", currentPage); // 当前页
			maps.put("total", result.size()); // 总条数
			if (pageNum != 0) {
				maps.put("totalPages", result.size() / pageAble.getPageSize()); // 总页数
			}
			maps.put("list", result);
			return maps;
		}
		// 总条数
		StringBuffer sqltotal = new StringBuffer("select count(*) number from(");
		sqltotal.append(sql + ")ss");
		System.out.println("========sql:" + sqltotal.toString());
		Map<String, Object> wd = jdbcTemplate.queryForMap(sqltotal.toString());
		Object n1 = wd.get("number");
		int count = Integer.parseInt(n1.toString());
		System.out.println("======总条数：" + count);
		// 分页
		sql.append(" limit " + (currentPage-1) * pageNum + "," + pageNum);

		List<UserManagement> userList = jdbcTemplate.query(sql.toString(), new RowMapper<UserManagement>() {

			@Override
			public UserManagement mapRow(ResultSet rs, int rowNum) throws SQLException {
				UserManagement po = new UserManagement();
				po.setUserId(rs.getString("id"));
				po.setBrandName(rs.getString("brand_name"));
				po.setGrade(rs.getString("grade"));
				po.setUserName(rs.getString("fullname"));
				po.setPhone(rs.getString("phone"));
				po.setRole(rs.getString("role_name"));
				po.setRealnameStatus(rs.getString("real_name_status").equals("1") ? "已审核" : "未审核");
				po.setPerson(rs.getString("person"));
				po.setRole(role);
				return po;
			}
		});
		boolean status =redisUtil.set(key.toString(), userList);
		if (status==false) {
			System.out.println("===============redis缓存失败==================");
		}
		maps.put("pageNum", pageNum); // 每页显示条数
		maps.put("currentPage", currentPage); // 当前页
		maps.put("total", count); // 总条数
		if (pageNum != 0)

		{
			maps.put("totalPages", count / pageAble.getPageSize()); // 总页数
		}
		maps.put("list", userList);
		return maps;
	}

}
