package com.example.continuing.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.continuing.entity.Joins;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.JoinsRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JoinService {

	private final JoinsRepository joinsRepository;
	private final UsersRepository usersRepository;
	
	public List<Meetings> getJoinMeetingList(Integer userId) {
		List<Meetings> joinMeetingList = new ArrayList<>();
		List<Joins> joinList = joinsRepository.findByUserId(userId);
		if(!joinList.isEmpty()) {
			for(Joins join : joinList) {
				Meetings meeting = join.getMeeting();
				joinMeetingList.add(meeting);
			}
		}
		
		return joinMeetingList;
	}
	
	public List<Users> getJoinUserList(Meetings meeting) {
		List<Users> joinUserList = new ArrayList<>();
		List<Joins> joinList = joinsRepository.findByMeeting(meeting);
		if(!joinList.isEmpty()) {
			for(Joins join : joinList) {
				Users user = usersRepository.findById(join.getUserId()).get();
				joinUserList.add(user);
			}
		}
	
		return joinUserList;
	}

}
