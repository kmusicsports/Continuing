package com.example.continuing.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.continuing.entity.Deliveries;

public interface DeliveriesRepository extends JpaRepository<Deliveries, Integer> {

	Optional<Deliveries> findByUserId(Integer userId);
	
	@Transactional
	void deleteByUserId(Integer userId);
}
