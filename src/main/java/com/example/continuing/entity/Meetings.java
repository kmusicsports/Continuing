package com.example.continuing.entity;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "meetings")
@Data
public class Meetings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "host_id")
	private Integer hostId;
	
	@Column(name = "number_people")
    private Integer numberPeople;
	
	@Column(name = "meeting_id")
	private String meetingId;
	
	@Column(name = "uuid")
    private String uuid;
	
	@Column(name = "topic")
    private String topic;
	
	@Column(name = "date")
    private Date date;
	
	@Column(name = "start_time")
    private String startTime;
	
	@Column(name = "end_time")
    private String endTime;
	
	@Column(name = "duration")
    private Long duration;
    
	@Column(name = "password")
	private String password;
	
	@Column(name = "agenda")
    private String agenda;

	@Column(name = "start_url")
    private String startUrl;
	
	@Column(name = "join_url")
    private String joinUrl;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	@Column(name = "updated_at")
	private Timestamp updatedAt; 
	
}
