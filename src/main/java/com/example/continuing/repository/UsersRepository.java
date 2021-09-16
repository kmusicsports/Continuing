package com.example.continuing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.continuing.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

	Optional<Users> findByName(String name);
	Optional<Users> findByEmail(String email);
	List<Users> findByNameContainingIgnoreCase(String name);
	List<Users> findByProfileMessageContainingIgnoreCase(String profileMessage);
	List<Users> findAllByOrderByContinuousDaysDesc();
	List<Users> findTop3ByOrderByContinuousDaysDesc();
}
