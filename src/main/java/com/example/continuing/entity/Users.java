package com.example.continuing.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "profile_image")
	private String profile_image;
	
	@Column(name = "profile_message")
	private String profile_message;
	
	@Column(name = "continuous_days")
	private Integer continuous_days;
	
	@Column(name = "created_at")
	private Timestamp created_at;
	
	@Column(name = "updated_at")
	private Timestamp updated_at;
}
