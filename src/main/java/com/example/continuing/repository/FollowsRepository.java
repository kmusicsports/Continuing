package com.example.continuing.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.continuing.entity.Follows;

@Repository
public interface FollowsRepository extends JpaRepository<Follows, Integer> {

	List<Follows> findByFollowerId(Integer followerId);
	List<Follows> findByFolloweeId(Integer followeeId);
	List<Follows> findByFollowerIdAndFolloweeId(Integer followerId, Integer followeeId);
	
	@Transactional
	void deleteByFollowerId(Integer followerId);
	
	@Transactional
	void deleteByFolloweeId(Integer followeeId);
}
