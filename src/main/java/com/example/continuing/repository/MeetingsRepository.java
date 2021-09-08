package com.example.continuing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;

public interface MeetingsRepository extends JpaRepository<Meetings, Integer> {

	List<Meetings> findByHost(Users host);
}
