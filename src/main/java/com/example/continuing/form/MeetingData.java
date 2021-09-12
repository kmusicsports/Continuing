package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import org.json.simple.JSONObject;

import com.example.continuing.common.Utils;
import com.example.continuing.dto.MeetingDto;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeetingData {

	private int id;
    private String topicName;
    private int numberPeople;
    private String date;
    private String startTime;
    private String endTime;
	private String password;
	private String passwordAgain;
	private String agenda;
	
	public MeetingData(Meetings meeting) {
		id = meeting.getId();
		topicName = meeting.getTopic();
		numberPeople = meeting.getNumberPeople();
		date = meeting.getDate().toString();
		startTime = meeting.getStartTime().toString();
		endTime = meeting.getEndTime().toString();
		password = meeting.getPassword();
		passwordAgain = meeting.getPassword();
		agenda = meeting.getAgenda();
	}
	
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
	
	public Meetings toEntity(JSONObject jsonObject, Users uesr) {
		Meetings meeting = new Meetings();
		meeting.setHost(uesr);
		meeting.setNumberPeople(numberPeople);
		meeting.setMeetingId(jsonObject.get("id").toString());
		meeting.setUuid(jsonObject.get("uuid").toString());
		meeting.setTopic(topicName);
		meeting.setDate(Utils.str2date(date.replace("/", "-")));
		meeting.setStartTime(Utils.str2time(startTime));
		meeting.setEndTime(Utils.str2time(endTime));
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
	
	public Meetings toEntity(Meetings meeting) {
		meeting.setTopic(topicName);
		meeting.setNumberPeople(numberPeople);
		meeting.setDate(Utils.str2date(date.replace("/", "-")));
		meeting.setStartTime(Utils.str2time(startTime));
		meeting.setEndTime(Utils.str2time(endTime));
		meeting.setPassword(password);
		meeting.setAgenda(agenda);
		
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        meeting.setUpdatedAt(timestamp);
        
		return meeting;
	}
	
}
