package com.example.continuing.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.example.continuing.common.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
public class Deliveries {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "user_id")
	private Integer userId;
	
	@Column(name = "followed")
	private Integer followed;
	
	@Column(name = "meeting_created")
	private Integer meetingCreated;
	
	@Column(name = "meeting_deleted")
	private Integer meetingDeleted;
	
	@Column(name = "meeting_joined")
	private Integer meetingJoined;
	
	@Column(name = "meeting_left")
	private Integer meetingLeft;
	
	@Column(name = "today_meetings")
	private Integer todayMeetings;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	@Column(name = "updated_at")
	private Timestamp updatedAt;
	
	public Deliveries(Integer userId) {
		this.userId = userId;
		this.followed = 1;
		this.meetingCreated = 1;
		this.meetingDeleted = 1;
		this.meetingJoined = 1;
		this.meetingLeft = 1;
		this.todayMeetings = 1;

		Timestamp timestamp = Utils.timestampNow();
		this.createdAt = timestamp;
		this.updatedAt = timestamp;
	}
}
