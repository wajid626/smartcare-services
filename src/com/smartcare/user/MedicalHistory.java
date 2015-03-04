package com.smartcare.user;


public class MedicalHistory {
	

	private String description;
	private String dateString;
	
	public MedicalHistory(String description, String dateString) {
		this.description = description;
		this.dateString = dateString;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDateString() {
		return dateString;
	}
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
	
	/*
	 * @Override
	public String toString() {
		return "MedicalHistory [description=" + description + ", dateString="
				+ dateString + "]";
	}
	 */
}
