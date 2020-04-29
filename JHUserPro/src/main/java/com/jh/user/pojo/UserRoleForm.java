package com.jh.user.pojo;


public class UserRoleForm{


	/**用户ID*/
	private long userId;
	
	/**手机号**/
	private String phone;
	
	/**昵称/简称**/
	private String nickName;
	
	/**角色ID**/
	private long roleId;
	
	/**角色别名**/
	private String rolecode;
	
	/**角色名称**/
	private String rolename;
	
	//会员数
	private int size;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getRolecode() {
		return rolecode;
	}

	public void setRolecode(String rolecode) {
		this.rolecode = rolecode;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	
}
