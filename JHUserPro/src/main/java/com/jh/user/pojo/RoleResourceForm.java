package com.jh.user.pojo;




public class RoleResourceForm {


	

	/** 角色编号*/
	private long roleid;
	
	/**角色别称*/
	private String rolecode;
	
	/**角色名称*/
	private String rolename;
	
	/**权限资源编号*/
	private long resourceid;
	
	/**权限资源别称*/
	private String resourceNo;
	
	/**权限资源名称*/
	private String resourceName;
	
	/**权限资源地址*/
	private String url;
	

	public long getRoleid() {
		return roleid;
	}

	public void setRoleid(long roleid) {
		this.roleid = roleid;
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

	public long getResourceid() {
		return resourceid;
	}

	public void setResourceid(long resourceid) {
		this.resourceid = resourceid;
	}

	public String getResourceNo() {
		return resourceNo;
	}

	public void setResourceNo(String resourceNo) {
		this.resourceNo = resourceNo;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	

	
	

	
	
	
}
