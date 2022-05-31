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
@Table(name = "records")
@Data
@NoArgsConstructor
public class Records {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private Users user;
	
	@Column(name = "topic")
	private Integer topic;
	
	@Column(name = "days")
	private Integer days;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	@Column(name = "updated_at")
	private Timestamp updatedAt;
	
	
	public Records(Users user, Integer topic) {
		this.user = user;
		this.topic = topic;
		this.days = 0;

        Timestamp timestamp = Utils.timestampNow();
		this.createdAt = timestamp;
		this.updatedAt = timestamp;
	}
	
}
