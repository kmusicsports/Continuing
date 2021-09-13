package com.example.continuing.schedulingtasks;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.continuing.entity.Users;
import com.example.continuing.repository.RecordsRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledTasks {

	private final UsersRepository usersRepository;
	private final RecordsRepository recordsRepository;
 	
	@Scheduled(cron = "${cron.every.month}")
	public void resetDays() {
		recordsRepository.deleteAll();
		List<Users> userList = usersRepository.findAll();
		for(Users user : userList) {
			user.setContinuousDays(0);
		}
		usersRepository.saveAllAndFlush(userList);

	}
	
}
