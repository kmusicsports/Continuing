package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.continuing.entity.Joins;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.JoinsRepository;
import com.example.continuing.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class JoinServiceTest {

	@Mock
	private JoinsRepository joinsRepository;
	
	@Mock
	private UsersRepository usersRepository;
	
	@InjectMocks
	private JoinService JoinService;
	
	@Test
	@DisplayName("[getJoinMeetingListメソッドのテスト]joinList.isEmpty()==true")
	void testGetEmptyJoinMeetingList() {
		int testUserId = 1;
		
		List<Joins> emptyJoinsList = new ArrayList<>();
		when(joinsRepository.findByUserId(testUserId)).thenReturn(emptyJoinsList);	
		
		List<Meetings> result = JoinService.getJoinMeetingList(testUserId);
		List<Meetings> expected = new ArrayList<>();
		
		assertThat(result).isEqualTo(expected);
		verify(joinsRepository, times(1)).findByUserId(any());
	}

	@Test
	@DisplayName("[getJoinMeetingListメソッドのテスト]joinList.isEmpty()==false")
	void testGetNotEmptyJoinMeetingList() {
		int testUserId = 1;
		Meetings testMeeting10 = new Meetings();
		Meetings testMeeting11 = new Meetings();
		testMeeting10.setId(10);
		testMeeting10.setId(11);
		Joins join10 = new Joins(testUserId, testMeeting10);
		Joins join11 = new Joins(testUserId, testMeeting11);
	
		List<Joins> twoJoinsList = new ArrayList<>();
		twoJoinsList.add(join10);
		twoJoinsList.add(join11);
		
		when(joinsRepository.findByUserId(testUserId)).thenReturn(twoJoinsList);
		
		List<Meetings> result = JoinService.getJoinMeetingList(testUserId);
		
		List<Meetings> expected = new ArrayList<>();
		expected.add(testMeeting10);
		expected.add(testMeeting11);
		
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	@DisplayName("[getJoinUserListメソッドのテスト]joinList.isEmpty()==true")
	void testGetEmptyJoinUserList() {
		int testMeetingId = 1;
		Meetings testMeeting = new Meetings();
		testMeeting.setId(testMeetingId);
		
		List<Joins> emptyJoinsList = new ArrayList<>();
		when(joinsRepository.findByMeeting(testMeeting)).thenReturn(emptyJoinsList);	
		
		List<Users> result = JoinService.getJoinUserList(testMeeting);
		List<Users> expected = new ArrayList<>();
		
		assertThat(result).isEqualTo(expected);
		verify(joinsRepository, times(1)).findByMeeting(any());
		verify(usersRepository, never()).findById(any());
	}

	@Test
	@DisplayName("[getJoinUserListメソッドのテスト]joinList.isEmpty()==false")
	void testGetNotEmptyJoinUserList() {
		int testUserId1 = 1;
		int testUserId2 = 2;
		Users testUser1 = new Users();
		Users testUser2 = new Users();
		testUser1.setId(testUserId1);
		testUser2.setId(testUserId2);
		
		Meetings testMeeting = new Meetings();
		testMeeting.setId(10);
		Joins join1 = new Joins(testUserId1, testMeeting);
		Joins join2 = new Joins(testUserId2, testMeeting);
	
		List<Joins> twoJoinsList = new ArrayList<>();
		twoJoinsList.add(join1);
		twoJoinsList.add(join2);
		
		when(joinsRepository.findByMeeting(testMeeting)).thenReturn(twoJoinsList);
		when(usersRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
		when(usersRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
		
		List<Users> result = JoinService.getJoinUserList(testMeeting);
		List<Users> expected = new ArrayList<>();
		expected.add(testUser1);
		expected.add(testUser2);
		
		assertThat(result).isEqualTo(expected);
		verify(usersRepository, times(2)).findById(any());
		verify(usersRepository, times(1)).findById(testUserId1);
		verify(usersRepository, times(1)).findById(testUserId2);
	}
	
}
