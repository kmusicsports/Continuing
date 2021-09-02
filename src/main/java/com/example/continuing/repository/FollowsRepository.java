package com.example.continuing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Follows;

public interface FollowsRepository extends JpaRepository<Follows, Integer> {

	List<Follows> findByFollowerId(Integer followerId);
	List<Follows> findByFolloweeId(Integer followeeId);
	List<Follows> findByFollowerIdAndFolloweeId(Integer followerId, Integer followeeId);
	
}
