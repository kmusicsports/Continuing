package com.example.continuing.form;

import com.example.continuing.common.Utils;
import com.example.continuing.dto.MeetingDto;

import lombok.Data;

@Data
public class MeetingData {

    private String topicName;
    private int numberPeople;
    private String date;
    private String startTime;
    private String endTime;
	private String password;
	private String passwordAgain;
	private String agenda;
	
	public MeetingDto toDto() {
		MeetingDto meetingDto = new MeetingDto();
		meetingDto.setTopic(topicName);
		meetingDto.setNumberPeople(numberPeople);
		meetingDto.setStartTime(date.replace("/", "-") + "T" + startTime + ":00");
		
		int duration = Utils.stringToInt(endTime) - Utils.stringToInt(startTime);
		meetingDto.setDuration(duration);
		
		meetingDto.setPassword(password);
		meetingDto.setAgenda(agenda);
		
		return meetingDto;
	}
}
