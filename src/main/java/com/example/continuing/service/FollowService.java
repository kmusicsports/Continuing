package com.example.continuing.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.continuing.entity.Follows;
import com.example.continuing.repository.FollowsRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FollowService {
	
	private final FollowsRepository followsRepository;

	// フォローしているユーザーアカウントのidリストを返す
	public List<Integer> getFolloweeIdList(Integer followerId) {
		List<Integer> followeeIdList = new ArrayList<>();
		if (followerId != null) {
			List<Follows> followList = followsRepository.findByFollowerId(followerId);
			for(Follows follow : followList) {
				followeeIdList.add(follow.getFolloweeId());
			}
		}
		return followeeIdList;
	}
	
}
