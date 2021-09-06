package com.example.continuing.dto;

import lombok.Data;

@Data
public class MeetingDto {
	
	private String topic;
	private int type;
	private String startTime;
	private int duration;
	private String password;
	private String agenda;
	private int numberPeople; 
	
	public MeetingDto() {
		type = 2;
	}
}
