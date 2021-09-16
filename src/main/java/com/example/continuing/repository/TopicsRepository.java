package com.example.continuing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.continuing.entity.Topics;

@Repository
public interface TopicsRepository extends JpaRepository<Topics, Integer> {

}
