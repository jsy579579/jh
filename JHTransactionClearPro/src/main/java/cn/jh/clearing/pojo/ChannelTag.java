package cn.jh.clearing.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name="t_channel_tag")
public class ChannelTag implements Serializable{

	private static final long serialVersionUID = 13L;

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="channel_tag")
	private String channelTag;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	@Override
	public String toString() {
		return "ChannelTag [id=" + id + ", channelTag=" + channelTag + "]";
	}
	
	
	
}
