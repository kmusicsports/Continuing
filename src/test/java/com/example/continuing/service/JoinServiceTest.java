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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
	
	@Nested
	@DisplayName("[getJoinMeetingListメソッドのテスト]")
	public class testGetJoinMeetingList {
		
		private int testUserId = 1;
		private List<Joins> joinList;
		private List<Meetings> expected;
		
		@BeforeEach
		void init() {
			joinList = new ArrayList<>();
			expected = new ArrayList<>();
		}
		
		@Test
		@DisplayName("joinList.isEmpty()==true")
		void getEmptyJoinMeetingList() {
			when(joinsRepository.findByUserId(testUserId)).thenReturn(joinList);	
			
			List<Meetings> result = JoinService.getJoinMeetingList(testUserId);
			
			assertThat(result).isEqualTo(expected);
		}
		
		@Test
		@DisplayName("joinList.isEmpty()==false")
		void getNotEmptyJoinMeetingList() {
			
			Meetings testMeeting10 = new Meetings();
			Meetings testMeeting11 = new Meetings();
			testMeeting10.setId(10);
			testMeeting10.setId(11);
			Joins join10 = new Joins(testUserId, testMeeting10);
			Joins join11 = new Joins(testUserId, testMeeting11);
			
			joinList.add(join10);
			joinList.add(join11);
			
			when(joinsRepository.findByUserId(testUserId)).thenReturn(joinList);
			
			List<Meetings> result = JoinService.getJoinMeetingList(testUserId);

			expected.add(testMeeting10);
			expected.add(testMeeting11);
			
			assertThat(result).isEqualTo(expected);
		}
	}
	
	@Nested
	@DisplayName("[getJoinUserListメソッドのテスト]")
	public class testGetJoinUserList {
		
		private int testMeetingId = 10;
		private Meetings testMeeting;
		private List<Joins> joinList;
		private List<Users> expected;
		
		@BeforeEach
		void init() {
			testMeeting = new Meetings();
			joinList = new ArrayList<>();
			expected = new ArrayList<>();
		}
		@Test
		@DisplayName("joinList.isEmpty()==true")
		void getEmptyJoinUserList() {
			testMeeting.setId(testMeetingId);
			
			when(joinsRepository.findByMeeting(testMeeting)).thenReturn(joinList);	
			
			List<Users> result = JoinService.getJoinUserList(testMeeting);
			
			assertThat(result).isEqualTo(expected);
			verify(joinsRepository, times(1)).findByMeeting(any());
			verify(usersRepository, never()).findById(any());
		}
		
		@Test
		@DisplayName("joinList.isEmpty()==false")
		void getNotEmptyJoinUserList() {
			testMeeting.setId(testMeetingId);
			
			int testUserId1 = 1;
			int testUserId2 = 2;
			Users testUser1 = new Users();
			Users testUser2 = new Users();
			testUser1.setId(testUserId1);
			testUser2.setId(testUserId2);
			
			Joins join1 = new Joins(testUserId1, testMeeting);
			Joins join2 = new Joins(testUserId2, testMeeting);
			
			joinList.add(join1);
			joinList.add(join2);
			
			when(joinsRepository.findByMeeting(testMeeting)).thenReturn(joinList);
			when(usersRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
			when(usersRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
			
			List<Users> result = JoinService.getJoinUserList(testMeeting);
			
			expected.add(testUser1);
			expected.add(testUser2);
			
			assertThat(result).isEqualTo(expected);
			verify(usersRepository, times(2)).findById(any());
			verify(usersRepository, times(1)).findById(testUserId1);
			verify(usersRepository, times(1)).findById(testUserId2);
		}		
	}
	
}
