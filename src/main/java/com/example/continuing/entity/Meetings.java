package com.example.continuing.entity;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "meetings")
@Data
@ToString(exclude = "joinList")
public class Meetings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
    @JoinColumn(name = "host_id")
	private Users host;
	
	@Column(name = "number_people")
    private Integer numberPeople;
	
	@Column(name = "meeting_id")
	private String meetingId;
	
	@Column(name = "uuid")
    private String uuid;
	
	@Column(name = "topic")
    private Integer topic;
	
	@Column(name = "date")
    private Date date;
	
	@Column(name = "start_time")
    private Time startTime;
	
	@Column(name = "end_time")
    private Time endTime;
	
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
	
	@OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    @OrderBy("id asc")
	private List<Joins> joinList;
	
}
