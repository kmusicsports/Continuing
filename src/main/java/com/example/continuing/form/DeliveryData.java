package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import com.example.continuing.entity.Deliveries;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeliveryData {

	private Integer followed;
	private Integer meetingCreated;
	private Integer meetingDeleted;
	private Integer meetingJoined;
	private Integer meetingLeft;
	private Integer todayMeetings;

	public DeliveryData(Deliveries deliveries) {
		this.followed = deliveries.getFollowed();
		this.meetingCreated = deliveries.getMeetingCreated();
		this.meetingDeleted = deliveries.getMeetingDeleted();
		this.meetingJoined = deliveries.getMeetingJoined();
		this.meetingLeft = deliveries.getMeetingLeft();
		this.todayMeetings = deliveries.getTodayMeetings();
	}
	
	public Deliveries toEntity(Deliveries deliveries) {
		deliveries.setFollowed(followed);
		deliveries.setMeetingCreated(meetingCreated);
		deliveries.setMeetingDeleted(meetingDeleted);
		deliveries.setMeetingJoined(meetingJoined);
		deliveries.setMeetingLeft(meetingLeft);
		deliveries.setTodayMeetings(todayMeetings);
		
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		
		deliveries.setUpdatedAt(timestamp);
		
		return deliveries;
	}
	
	
}
