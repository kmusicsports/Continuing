package com.example.continuing.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FollowService {
	
	private final FollowsRepository followsRepository;
	private final UsersRepository usersRepository;
	
	// フォロー中のユーザーアカウントのリストを返す
	public List<Users> getFollowsList(Integer followerId) {
		List<Users> followsList = new ArrayList<>();
		List<Follows> followList = followsRepository.findByFollowerId(followerId);
		if(!followList.isEmpty()) {
			for(Follows follow : followList) {
				Users user = usersRepository.findById(follow.getFolloweeId()).get();
				followsList.add(user);
			}			
		}
		
		return followsList;
	}
	
	// フォロー中のユーザーアカウントのリストを返す
	public List<Users> getFollowersList(Integer followeeId) {
		List<Users> followersList = new ArrayList<>();
		List<Follows> followList = followsRepository.findByFolloweeId(followeeId);
		if(!followList.isEmpty()) {
			for(Follows follow : followList) {
				Users user = usersRepository.findById(follow.getFollowerId()).get();
				followersList.add(user);
			}			
		}
		
		return followersList;
	}
	
}
