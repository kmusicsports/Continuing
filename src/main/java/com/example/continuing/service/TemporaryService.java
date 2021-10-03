package com.example.continuing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.continuing.entity.Temporaries;
import com.example.continuing.repository.TemporariesRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TemporaryService {

	private final TemporariesRepository temporariesRepository;
	
	public boolean isValid(String email, String token) {
		List<Temporaries> temporariesList = temporariesRepository.findByEmailOrderByCreatedAtDesc(email);
		if(temporariesList.size() != 0) {
			Temporaries latestTemporaries = temporariesList.get(0);
			return latestTemporaries.getToken().equals(token);
		} else {
			System.out.println("There is no data in the temporaries table.");
			return false;
		}
	}
}
