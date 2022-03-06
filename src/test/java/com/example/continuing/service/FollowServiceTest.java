package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import com.example.continuing.entity.Deliveries;
import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {
	
	@Mock
	private FollowsRepository followsRepository;
	
	@Mock
	private UsersRepository usersRepository;
	
	@Mock
	private MailService mailService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private DeliveriesRepository deliveriesRepository;
	
	@InjectMocks
	private FollowService followService;

	@Nested
	@DisplayName("[getFollowsListメソッドのテスト]")
	public class testGetFollowsList {
		
		private int testUserId1 = 1;
		List<Follows> followList;
		List<Users> expected;
		
		@BeforeEach
		void init() {
			followList = new ArrayList<>();
			expected = new ArrayList<>();
		}
		
		@Test
		@DisplayName("followList.isEmpty()==true")
		void getEmptyFollowsList() {
			when(followsRepository.findByFollowerId(testUserId1)).thenReturn(followList);
			
			List<Users> result = followService.getFollowsList(testUserId1);
			
			assertThat(result).isEqualTo(expected);
			verify(followsRepository, times(1)).findByFollowerId(any());		
			verify(usersRepository, never()).findById(any());
		}
		
		@Test
		@DisplayName("followList.isEmpty()==false")
		void getNotEmptyFollowsList() {
			
			int testUserId2 = 2;
			int testUserId3 = 3;
			Users testUser2 = new Users();
			Users testUser3 = new Users();
			testUser2.setId(testUserId2);
			testUser3.setId(testUserId3);
			
			Follows follow1to2 = new Follows(testUserId1, testUserId2);
			Follows follow1to3 = new Follows(testUserId1, testUserId3);
			followList.add(follow1to2);
			followList.add(follow1to3);
			
			when(followsRepository.findByFollowerId(testUserId1)).thenReturn(followList);
			when(usersRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
			when(usersRepository.findById(testUserId3)).thenReturn(Optional.of(testUser3));
			
			List<Users> result = followService.getFollowsList(testUserId1);
			
			expected.add(testUser2);
			expected.add(testUser3);
			
			assertThat(result).isEqualTo(expected);
			verify(usersRepository, times(2)).findById((any()));
			verify(usersRepository, times(1)).findById(testUserId2);
			verify(usersRepository, times(1)).findById(testUserId3);
		}
	}
	
	@Nested
	@DisplayName("[getFollowersListメソッドのテスト]")
	public class testGetFollowersList {
		
		private int testUserId1 = 1;
		List<Follows> followList;
		List<Users> expected;
		
		@BeforeEach
		void init() {
			followList = new ArrayList<>();
			expected = new ArrayList<>();
		}
		
		@Test
		@DisplayName("followList.isEmpty()==true")
		void getEmptyFollowersList() {
			when(followsRepository.findByFolloweeId(testUserId1)).thenReturn(followList);
			
			List<Users> result = followService.getFollowersList(testUserId1);
			
			assertThat(result).isEqualTo(expected);
			verify(followsRepository, times(1)).findByFolloweeId(any());
			verify(usersRepository, never()).findById(any());
		}
		
		@Test
		@DisplayName("followList.isEmpty()==false")
		void getNotEmptyFollowersList() {
			
			int testUserId2 = 2;
			int testUserId3 = 3;
			Users testUser2 = new Users();
			Users testUser3 = new Users();
			testUser2.setId(testUserId2);
			testUser3.setId(testUserId3);
			
			Follows follow2to1 = new Follows(testUserId2, testUserId1);
			Follows follow3to1 = new Follows(testUserId3, testUserId1);
			followList.add(follow2to1);
			followList.add(follow3to1);
			
			when(followsRepository.findByFolloweeId(testUserId1)).thenReturn(followList);
			when(usersRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
			when(usersRepository.findById(testUserId3)).thenReturn(Optional.of(testUser3));
			
			List<Users> result = followService.getFollowersList(testUserId1);
			
			expected.add(testUser2);
			expected.add(testUser3);
			
			assertThat(result).isEqualTo(expected);
			verify(usersRepository, times(2)).findById((any()));
			verify(usersRepository, times(1)).findById(testUserId2);
			verify(usersRepository, times(1)).findById(testUserId3);
		}
	}

	@Nested
	@DisplayName("[sendMailメソッドのテスト]")
	public class testSendMail {
		
		private Users testUser1 = new Users();
		private Users testUser2 = new Users();
		private Locale locale = new Locale("ja");
		private Deliveries deliveries;
		
		@BeforeEach
		void init() {
			testUser1.setId(1);
			testUser1.setEmail("testUser1@email");
			
			deliveries = new Deliveries(testUser1.getId());
		}
		
		@Test
		@DisplayName("メールを送る")
		void sendMail() {
			when(deliveriesRepository.findByUserId(testUser1.getId())).thenReturn(Optional.of(deliveries));
			
			followService.sendMail(testUser1, testUser2, locale);
			
			ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
			verify(messageSource, times(2)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
			
			ArgumentCaptor<String> followeeEmailCaptor = ArgumentCaptor.forClass(String.class);
			verify(mailService, times(1)).sendMail(followeeEmailCaptor.capture(), any(), any());		
			String captoredUserEmail = followeeEmailCaptor.getValue();
			assertThat(captoredUserEmail).isEqualTo(testUser1.getEmail());
		}
		
		@Test
		@DisplayName("配信の許可がされておらず、メールを送らない")
		void notSendMail() {
			deliveries.setFollowed(0);
			
			when(deliveriesRepository.findByUserId(testUser1.getId())).thenReturn(Optional.of(deliveries));
			
			Locale locale = new Locale("ja");
			followService.sendMail(testUser1, testUser2, locale);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
			verify(mailService, never()).sendMail(any(), any(), any());		
		}
	}

}
