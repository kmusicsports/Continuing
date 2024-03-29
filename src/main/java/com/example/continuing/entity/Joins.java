package com.example.continuing.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.continuing.common.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "joins")
@Data
@NoArgsConstructor
public class Joins {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "user_id")
	private Integer userId;
	
	@ManyToOne
	@JoinColumn(name = "meeting_id")
	private Meetings meeting;

	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	public Joins(Integer userId, Meetings meeting) {
		
		this.userId = userId;
		this.meeting = meeting;
        this.createdAt = Utils.timestampNow();
	}
}
