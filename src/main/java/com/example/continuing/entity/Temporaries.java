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
@Table(name = "temporaries")
@Data
@NoArgsConstructor
public class Temporaries {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "user_id")
    private Integer userId;
	
	@Column(name = "name")
    private String name;
	
	@Column(name = "email")
    private String email;
	
	@Column(name = "password")
    private String password;
	
	@Column(name = "token")
    private String token;
	
	@Column(name = "created_at")
    private Timestamp createdAt;
	
	public Temporaries(Users user, String token) {
		this.userId = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.token = token;
		this.createdAt = Utils.timestampNow();
	}
	
	public Temporaries(String email, Users user, String token) {
		this.userId = user.getId();
		this.name = user.getName();
		this.email = email;
		this.password = user.getPassword();
		this.token = token;
		this.createdAt = Utils.timestampNow();
	}
	
}
