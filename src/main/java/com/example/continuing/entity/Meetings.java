package com.example.continuing.entity;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.json.simple.JSONObject;

import com.example.continuing.common.Utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
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
	
	@Column(name = "start_time")
    private String startTime;
	
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
	
	public Meetings(JSONObject jsonObject, Integer hostId, Integer numberPeople) {
		
		this.hostId = hostId;
		this.numberPeople = numberPeople;
		this.meetingId = jsonObject.get("id").toString();
		this.uuid = jsonObject.get("uuid").toString();
		this.topic = jsonObject.get("topic").toString();
		
		String dateString = jsonObject.get("start_time").toString().replace("T", " ").replace("Z", "");
		this.startTime = Utils.changeTimeZone(dateString);
		
		this.duration = (Long) jsonObject.get("duration");
		this.password = jsonObject.get("password").toString();
		this.agenda = jsonObject.get("agenda").toString();
		this.startUrl = jsonObject.get("start_url").toString();
		this.joinUrl = jsonObject.get("join_url").toString();
		
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        this.createdAt = timestamp;
        this.updatedAt = timestamp;
        
	}
}
