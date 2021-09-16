package com.example.continuing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.continuing.entity.Joins;
import com.example.continuing.entity.Meetings;

@Repository
public interface JoinsRepository extends JpaRepository<Joins, Integer> {

	List<Joins> findByUserId(Integer uesrId);
	List<Joins> findByMeeting(Meetings meeting);
	List<Joins> findByUserIdAndMeeting(Integer userId, Meetings meeting);
	
}
