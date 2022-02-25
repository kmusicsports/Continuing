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

import org.junit.jupiter.api.DisplayName;
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

//Mockitoの初期化、終了処理(コメント部分)を自動的に行なってくれる
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

	@Test
	@DisplayName("[getFollowsListメソッドのテスト]followList.isEmpty()==true")
	void testGetEmptyFollowsList() {
		int testUserId = 1;
		List<Follows> emptyFollowsList = new ArrayList<>();
		
		when(followsRepository.findByFollowerId(testUserId)).thenReturn(emptyFollowsList);
		
		List<Users> result = followService.getFollowsList(testUserId);
		List<Users> expected = new ArrayList<>();
		
		assertThat(result).isEqualTo(expected);
		verify(followsRepository, times(1)).findByFollowerId(any());		
		verify(usersRepository, never()).findById(any());
	}
	
	@Test
	@DisplayName("[getFollowsListメソッドのテスト]followList.isEmpty()==false")
	void testGetNotEmptyFollowsList() {
		int testUserId1 = 1;
		int testUserId2 = 2;
		int testUserId3 = 3;
		Users testUser2 = new Users();
		testUser2.setId(testUserId2);
		Users testUser3 = new Users();
		testUser3.setId(testUserId3);
		
		List<Follows> twoFollowsList = new ArrayList<>();
		Follows follow1to2 = new Follows(testUserId1, testUserId2);
		Follows follow1to3 = new Follows(testUserId1, testUserId3);
		twoFollowsList.add(follow1to2);
		twoFollowsList.add(follow1to3);
		
		when(followsRepository.findByFollowerId(testUserId1)).thenReturn(twoFollowsList);
		when(usersRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
		when(usersRepository.findById(testUserId3)).thenReturn(Optional.of(testUser3));
		
		List<Users> result = followService.getFollowsList(testUserId1);
		List<Users> expected = new ArrayList<>();
		expected.add(testUser2);
		expected.add(testUser3);
		
		assertThat(result).isEqualTo(expected);
		verify(usersRepository, times(2)).findById((any()));
		verify(usersRepository, times(1)).findById(testUserId2);
		verify(usersRepository, times(1)).findById(testUserId3);
	}
	
	@Test
	@DisplayName("[getFollowersListメソッドのテスト]followList.isEmpty()==true")
	void testGetEmptyFollowersList() {
		int testUserId = 1;
		List<Follows> emptyFollowsList = new ArrayList<>();
		
		when(followsRepository.findByFolloweeId(testUserId)).thenReturn(emptyFollowsList);
		
		List<Users> result = followService.getFollowersList(testUserId);
		List<Users> expected = new ArrayList<>();
		
		assertThat(result).isEqualTo(expected);
		verify(followsRepository, times(1)).findByFolloweeId(any());
		verify(usersRepository, never()).findById(any());
	}

	@Test
	@DisplayName("[getFollowersListメソッドのテスト]followList.isEmpty()==false")
	void testGetNotEmptyFollowersList() {
		int testUserId1 = 1;
		int testUserId2 = 2;
		int testUserId3 = 3;
		Users testUser2 = new Users();
		testUser2.setId(testUserId2);
		Users testUser3 = new Users();
		testUser3.setId(testUserId3);
		
		List<Follows> twoFollowsList = new ArrayList<>();
		Follows follow2to1 = new Follows(testUserId2, testUserId1);
		Follows follow3to1 = new Follows(testUserId3, testUserId1);
		twoFollowsList.add(follow2to1);
		twoFollowsList.add(follow3to1);
		
		when(followsRepository.findByFolloweeId(testUserId1)).thenReturn(twoFollowsList);
		when(usersRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
		when(usersRepository.findById(testUserId3)).thenReturn(Optional.of(testUser3));
		
		List<Users> result = followService.getFollowersList(testUserId1);
		List<Users> expected = new ArrayList<>();
		expected.add(testUser2);
		expected.add(testUser3);
		
		assertThat(result).isEqualTo(expected);
		verify(usersRepository, times(2)).findById((any()));
		verify(usersRepository, times(1)).findById(testUserId2);
		verify(usersRepository, times(1)).findById(testUserId3);
	}

	@Test
	@DisplayName("[sendMailメソッドのテスト]deliveries.getFollowed() == 1")
	void testSendMail() {
		int testUserId1 = 1;
		String testUser1Email = "testUser1@email";
		Users testUser1 = new Users();
		testUser1.setId(testUserId1);
		testUser1.setEmail(testUser1Email);

		Deliveries deliveries = new Deliveries(testUserId1);
		
		when(deliveriesRepository.findByUserId(testUser1.getId())).thenReturn(Optional.of(deliveries));
		
		Users testUser2 = new Users();
		Locale locale = new Locale("ja");
		followService.sendMail(testUser1, testUser2, locale);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(2)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
		
		ArgumentCaptor<String> followeeEmailCaptor = ArgumentCaptor.forClass(String.class);
		verify(mailService, times(1)).sendMail(followeeEmailCaptor.capture(), any(), any());		
		String captoredUserEmail = followeeEmailCaptor.getValue();
		assertThat(captoredUserEmail).isEqualTo(testUser1Email);
		
	}

	@Test
	@DisplayName("[sendMailメソッドのテスト]deliveries.getFollowed() != 1")
	void testNotSendMail() {
		int testUserId1 = 1;
		String testUser1Email = "testUser1@email";
		Users testUser1 = new Users();
		testUser1.setId(testUserId1);
		testUser1.setEmail(testUser1Email);

		Deliveries deliveries = new Deliveries(testUserId1);
		deliveries.setFollowed(0);
		
		when(deliveriesRepository.findByUserId(testUser1.getId())).thenReturn(Optional.of(deliveries));
		
		Users testUser2 = new Users();
		Locale locale = new Locale("ja");
		followService.sendMail(testUser1, testUser2, locale);
		
		verify(messageSource, never()).getMessage(any(), any(), any());
		verify(mailService, never()).sendMail(any(), any(), any());		
		
	}

}
