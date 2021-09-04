package com.example.continuing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Topics;

public interface TopicsRepository extends JpaRepository<Topics, Integer> {

}
