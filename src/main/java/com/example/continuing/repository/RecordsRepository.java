package com.example.continuing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.continuing.entity.Records;
import com.example.continuing.entity.Users;

@Repository
public interface RecordsRepository extends JpaRepository<Records, Integer> {

	List<Records> findByUserOrderByDays(Users user);
	List<Records> findByTopicOrderByDays(Integer topic);
	Optional<Records> findByUserAndTopic(Users user, Integer topic);
	
}
