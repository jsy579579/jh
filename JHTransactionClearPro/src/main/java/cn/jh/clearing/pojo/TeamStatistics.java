package cn.jh.clearing.pojo;

import java.math.BigDecimal;

public class TeamStatistics {

	
	private long teamNum;
	
	private BigDecimal teamSumAmount;

	public long getTeamNum() {
		return teamNum;
	}

	public void setTeamNum(long teamNum) {
		this.teamNum = teamNum;
	}

	public BigDecimal getTeamSumAmount() {
		return teamSumAmount;
	}

	public void setTeamSumAmount(BigDecimal teamSumAmount) {
		this.teamSumAmount = teamSumAmount;
	}
	
}
