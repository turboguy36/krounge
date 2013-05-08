package kr.co.ktech.cse.model;

public class GroupInfo {
	private int group_id;
	private String group_name;
	private int group_total_number = 0;
	public GroupInfo(){
		group_id = 0;
		group_name = "";
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	public int getGroup_total_number() {
		return group_total_number;
	}
	public void setGroup_total_number(int group_total_number) {
		this.group_total_number = group_total_number;
	}
}
