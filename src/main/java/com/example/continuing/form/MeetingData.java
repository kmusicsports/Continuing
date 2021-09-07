package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import org.json.simple.JSONObject;

import com.example.continuing.common.Utils;
import com.example.continuing.dto.MeetingDto;
import com.example.continuing.entity.Meetings;

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
		meetingDto.setStartTime(date.replace("/", "-") + "T" + startTime + ":00");
		
		int duration = Utils.string2Int(endTime) - Utils.string2Int(startTime);
		meetingDto.setDuration(duration);
		
		meetingDto.setPassword(password);
		meetingDto.setAgenda(agenda);
		
		return meetingDto;
	}
	
	public Meetings toEntity(JSONObject jsonObject, Integer uesrId) {
		Meetings meeting = new Meetings();
		meeting.setHostId(uesrId);
		meeting.setNumberPeople(numberPeople);
		meeting.setMeetingId(jsonObject.get("id").toString());
		meeting.setUuid(jsonObject.get("uuid").toString());
		meeting.setTopic(topicName);
		meeting.setDate(Utils.str2date(date.replace("/", "-")));
		meeting.setStartTime(startTime);
		meeting.setEndTime(endTime);
		meeting.setPassword(password);
		meeting.setAgenda(agenda);
		meeting.setStartUrl(jsonObject.get("start_url").toString());
		meeting.setJoinUrl(jsonObject.get("join_url").toString());
		
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        meeting.setCreatedAt(timestamp);
        meeting.setUpdatedAt(timestamp);
        
        return meeting;
	}
}
