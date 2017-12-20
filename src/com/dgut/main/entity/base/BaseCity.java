package com.dgut.main.entity.base;

public abstract class BaseCity {
	//城市编号
	private String cid;
	//城市名
	private String name;
	//索引
	private String index;
	//是否是热门城市
	private Boolean isHot;
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public Boolean getIsHot() {
		return isHot;
	}
	public void setIsHot(Boolean isHot) {
		this.isHot = isHot;
	}
	
	
}
