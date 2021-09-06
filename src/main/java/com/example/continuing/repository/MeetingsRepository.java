package com.example.continuing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Meetings;

public interface MeetingsRepository extends JpaRepository<Meetings, Integer> {

}
