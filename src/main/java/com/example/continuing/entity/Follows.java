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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows")
@Data
@NoArgsConstructor
public class Follows {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "follower_id")
	private Integer followerId;
	
	@Column(name = "followee_id")
	private Integer followeeId;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	
	public Follows(Integer follower_id, Integer followee_id) {
		this.followerId = follower_id;
		this.followeeId = followee_id;
		
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        
        this.createdAt = timestamp;
	}
}
