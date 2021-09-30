package com.example.continuing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Temporaries;

public interface TemporariesRepository extends JpaRepository<Temporaries, Integer> {

	List<Temporaries> findByEmailOrderByCreatedAtDesc(String email);
	
}
