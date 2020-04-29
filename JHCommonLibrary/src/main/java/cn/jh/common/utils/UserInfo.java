package cn.jh.common.utils;

public class UserInfo {

	private String id;
    private String name;
    
    public UserInfo() {

    }

	public UserInfo (String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "UserID-" + this.id + ", UserName-" + this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}