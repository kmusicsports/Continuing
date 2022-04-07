package com.example.continuing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Temporaries;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporariesRepository extends JpaRepository<Temporaries, Integer> {

	List<Temporaries> findByEmailOrderByCreatedAtDesc(String email);
	
}
