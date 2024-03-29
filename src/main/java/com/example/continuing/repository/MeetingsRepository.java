package com.example.continuing.repository;

import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;

@Repository
public interface MeetingsRepository extends JpaRepository<Meetings, Integer> {

	List<Meetings> findByHost(Users host);
	List<Meetings> findByHostAndDate(Users host, Date date);
	List<Meetings> findByHostAndDateGreaterThanEqual(Users host, Date date);
	
	@Transactional
	void deleteByHost(Users host);
}
