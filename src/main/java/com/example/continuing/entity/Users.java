package com.example.continuing.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"meetingList", "recordList"})
@NoArgsConstructor
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
	private String profileImage;
	
	@Column(name = "profile_message")
	private String profileMessage;
	
	@Column(name = "continuous_days")
	private Integer continuousDays;
	
	@Column(name = "language")
	private String language;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	@Column(name = "updated_at")
	private Timestamp updatedAt;
	
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<Meetings> meetingList = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<Records> recordList = new ArrayList<>();
    
    public Users (Temporaries temporary, Locale locale) {
    	this.name = temporary.getName();
    	this.email = temporary.getEmail();
    	this.password = temporary.getPassword();
    	this.continuousDays = 0;
    	this.language = locale.getLanguage();
    	
    	Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        this.createdAt = timestamp;
        this.updatedAt = timestamp;
    }
    
}
