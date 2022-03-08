package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import com.example.continuing.entity.Deliveries;
import com.example.continuing.entity.Joins;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Records;
import com.example.continuing.entity.Users;
import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.RecordsRepository;
import com.example.continuing.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

	@Mock
	private UsersRepository usersRepository;
	
	@Mock
	private RecordsRepository recordsRepository;
	
	@Mock
	private MeetingsRepository meetingsRepository;
	
	@Mock
	private JoinService joinService;
	
	@Mock
	private MailService mailService;
	
	@Mock
	private MessageSource messageSource;
	
	@Mock
	private DeliveriesRepository deliveriesRepository;
	
	@InjectMocks
	private MeetingService meetingService;

	private final Locale locale = new Locale("ja");
	private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private final String strToday = sdf.format(new java.util.Date());
	private final String invalidDate = (new java.util.Date()).toString();
	private static final String TEST_START_TIME = "00:00";
	
	@Nested
	@DisplayName("[ミーティングフォーム用isValid()メソッドのテスト]")
	public class NestedTestIsValidMeetingData {
		
		private MeetingData testMeetingData;
		private BindingResult result;
		private static final String TEST_PASS = "testPassword";
	
		@BeforeEach
		void init() {
			testMeetingData = new MeetingData();
			result = new DataBinder(testMeetingData).getBindingResult();
		}
	
		@Test
		@DisplayName("パスワード不一致エラーのみ")
		void unmatchedPasswordError() {
			
			testMeetingData.setNumberPeople(1);
			testMeetingData.setDate(strToday);
			testMeetingData.setStartTime(TEST_START_TIME);
			testMeetingData.setEndTime("01:00");
			testMeetingData.setPassword(TEST_PASS);
			testMeetingData.setPasswordAgain("testPasswordAgain");
			
			boolean isValid = meetingService.isValid(testMeetingData, true, result, locale);
			String getPassword = testMeetingData.getPassword();
			String getPasswordAgain = testMeetingData.getPasswordAgain();
			
			assertFalse(isValid);
			assertNull(getPassword);
			assertNull(getPasswordAgain);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
	
		@ParameterizedTest
		@CsvSource({"'00:10'", "'01:00'"})
		@DisplayName("複数人でのミーティングにおいて、ミーティング時間が15分以上40分以下でないエラーのみ")
		void multiplePeopleMeetingTimeError(String testEndTime) {
			
			testMeetingData.setNumberPeople(2);
			testMeetingData.setDate("1000/10/10");
			testMeetingData.setStartTime(TEST_START_TIME);
			testMeetingData.setEndTime(testEndTime);
			testMeetingData.setPassword(TEST_PASS);
			testMeetingData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = meetingService.isValid(testMeetingData, false, result, locale);
			String getStartTime = testMeetingData.getStartTime();
			String getEndTime = testMeetingData.getEndTime();
			
			assertFalse(isValid);
			assertNull(getStartTime);
			assertNull(getEndTime);
			
			verify(messageSource, times(2)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@ParameterizedTest
		@CsvSource({"'00:10'", "'36:00'"})
		@DisplayName("1対1でのミーティングにおいて、ミーティング時間が15分以上1800分(30時間)以下でないエラーのみ")
		void oneOnOneMeetingTimeError(String testEndTime) {
			
			testMeetingData.setNumberPeople(1);
			testMeetingData.setDate(strToday);
			testMeetingData.setStartTime(TEST_START_TIME);
			testMeetingData.setEndTime(testEndTime);
			testMeetingData.setPassword(TEST_PASS);
			testMeetingData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = meetingService.isValid(testMeetingData, true, result, locale);
			String getStartTime = testMeetingData.getStartTime();
			String getEndTime = testMeetingData.getEndTime();
			
			assertFalse(isValid);
			assertNull(getStartTime);
			assertNull(getEndTime);
			
			verify(messageSource, times(2)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("ミーティングの日付が作成日以前エラーのみ")
		void meetingDateBeforeTodayError() {
			
			testMeetingData.setNumberPeople(2);
			testMeetingData.setDate("1000/10/10");
			testMeetingData.setStartTime(TEST_START_TIME);
			testMeetingData.setEndTime("00:20");
			testMeetingData.setPassword(TEST_PASS);
			testMeetingData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = meetingService.isValid(testMeetingData, true, result, locale);
			String getDate = testMeetingData.getDate();
			
			assertFalse(isValid);
			assertNull(getDate);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("ミーティングの日付がDate型に変換できないエラーのみ")
		void invalidFormatDateError() {
			
			testMeetingData.setNumberPeople(2);
			testMeetingData.setDate(invalidDate);
			testMeetingData.setStartTime(TEST_START_TIME);
			testMeetingData.setEndTime("00:20");
			testMeetingData.setPassword(TEST_PASS);
			testMeetingData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = meetingService.isValid(testMeetingData, false, result, locale);
			String getDate = testMeetingData.getDate();
			
			assertFalse(isValid);
			assertNull(getDate);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
	}
	
	@Nested
	@DisplayName("[検索条件用isValid()メソッドのテスト]")
	public class NestedTestIsValidSearchData {
		
		private SearchData testSearchData;
		private BindingResult result;
	
		@BeforeEach
		void init() {
			testSearchData = new SearchData();
			result = new DataBinder(testSearchData).getBindingResult();
		}
		
		@Test
		@DisplayName("日付がDate型に変換できないエラーのみ")
		void invalidFormatDateError() {
			
			testSearchData.setDate(invalidDate);
			testSearchData.setStartTime(TEST_START_TIME);
			testSearchData.setEndTime("00:20");
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			String getDate = testSearchData.getDate();
			
			assertFalse(isValid);
			assertNull(getDate);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
	
		@Test
		@DisplayName("開始時刻の形式エラーのみ")
		void invalidFormatStartTimeError() {
			
			testSearchData.setDate(strToday);
			testSearchData.setStartTime(invalidDate);
			testSearchData.setEndTime("00:20");
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			String getStartTime = testSearchData.getStartTime();
			
			assertFalse(isValid);
			assertNull(getStartTime);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("終了時刻の形式エラーのみ")
		void invalidFormatEndTimeError() {
			
			testSearchData.setDate(strToday);
			testSearchData.setStartTime(TEST_START_TIME);
			testSearchData.setEndTime(invalidDate);
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			String getEndTime = testSearchData.getEndTime();
			
			assertFalse(isValid);
			assertNull(getEndTime);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("エラーなし、日時条件なし")
		void noErrorAndNoDateTime() {
			
			testSearchData.setDate("");
			testSearchData.setStartTime("");
			testSearchData.setEndTime("");
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			
			assertTrue(isValid);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
		}
	}
	
	@Nested
	@DisplayName("[ミーティングフォーム用isValid()メソッドのテスト]")
	public class NestedTestJoinCheck {
		
		private Meetings testMeeting;
		private final int testUserId = 1;
		private final int meetingHostId = 2;
		private final java.sql.Date sqlToday = new java.sql.Date(System.currentTimeMillis());
		private final java.sql.Time sqlNow = new java.sql.Time(System.currentTimeMillis());
		private List<Joins> joinList;
		private Users meetingHost;
		private final MockHttpServletRequest testRequest = new MockHttpServletRequest();
		private HttpSession testSession;
	
		@BeforeEach
		void init() {
			meetingHost = new Users();
			testMeeting = new Meetings();
			joinList = new ArrayList<>();
			testSession = testRequest.getSession();
		}
		
		@Test
		@DisplayName("ミーティング開催者が参加者のいないミーティングを開始しようとした場合のエラーのみ")
		void noJoinListError() {
			meetingHost.setId(testUserId);
			
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlToday);
			testMeeting.setJoinList(joinList);
			
			when(messageSource.getMessage(any(), any(), any())).thenReturn("testWarningMessage");
			
			String warningMessage = meetingService.joinCheck(testMeeting, testUserId, locale, testSession);
						
			assertNotNull(warningMessage);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("ミーティングの日付が今日ではないエラーのみ")
		void notTodayError() {
			final java.sql.Date sqlNotToday = new java.sql.Date(System.currentTimeMillis() + 86400000); // 1日加える

			meetingHost.setId(meetingHostId);
			
			Joins testJoin = new Joins();
			testJoin.setMeeting(testMeeting);
			testJoin.setUserId(testUserId);
			joinList.add(testJoin);
			
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlNotToday);
			testMeeting.setJoinList(joinList);
			
			when(messageSource.getMessage(any(), any(), any())).thenReturn("testWarningMessage");
			
			String warningMessage = meetingService.joinCheck(testMeeting, meetingHostId, locale, testSession);
						
			assertNotNull(warningMessage);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@ParameterizedTest
		@CsvSource({"-1800000", "1800000"}) // 30分
		@DisplayName("ミーティング開始時刻から遠い(または遅い)時刻でミーティングを開始しようとしている場合のエラーのみ")
		void invalidTimeError(int plusMillis) {
			final java.sql.Time sqlInvalidTime = new java.sql.Time(System.currentTimeMillis() + plusMillis);
			
			meetingHost.setId(meetingHostId);
			
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlToday);
			testMeeting.setJoinList(joinList);
			testMeeting.setStartTime(sqlInvalidTime);
			
			when(messageSource.getMessage(any(), any(), any())).thenReturn("testWarningMessage");
			
			String warningMessage = meetingService.joinCheck(testMeeting, testUserId, locale, testSession);
						
			assertNotNull(warningMessage);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("エラーなし、同ミーティングへの参加が2回目以降")
		void noErrorAndNotFirstJoinInMeeting() {
			final SimpleDateFormat stf = new SimpleDateFormat("HH:mm");

			meetingHost.setId(meetingHostId);			
			
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlToday);
			testMeeting.setJoinList(joinList);
			testMeeting.setStartTime(sqlNow);
			
			testSession.setAttribute(stf.format(sqlNow), "not null");
			
			Users testUser = new Users();
			testUser.setId(testUserId);
			
			when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
			when(recordsRepository.findByUserAndTopic(testUser, testMeeting.getTopic())).thenReturn(Optional.empty());
			
			String warningMessage = meetingService.joinCheck(testMeeting, testUserId, locale, testSession);
						
			assertNull(warningMessage);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
			verify(recordsRepository, never()).saveAndFlush(any());
			verify(usersRepository, never()).saveAndFlush(any());
		}
		
		@Test
		@DisplayName("エラーなし、ミーティング参加が今日初じゃない")
		void noErrorAndNotTodayFirstJoin() {			
			meetingHost.setId(meetingHostId);
			
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlToday);
			testMeeting.setJoinList(joinList);
			testMeeting.setStartTime(sqlNow);
			
			LocalDate localDate = LocalDate.parse(strToday);			
			testSession.setAttribute(localDate.toString(), "not null");
			
			Users testUser = new Users();
			testUser.setId(testUserId);
			
			when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
			when(recordsRepository.findByUserAndTopic(testUser, testMeeting.getTopic())).thenReturn(Optional.empty());
			
			String warningMessage = meetingService.joinCheck(testMeeting, testUserId, locale, testSession);
						
			assertNull(warningMessage);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
			verify(recordsRepository, times(1)).saveAndFlush(any());
			verify(usersRepository, never()).saveAndFlush(any());
		}
		
		@Test
		@DisplayName("エラーなし、ミーティング参加が今日初")
		void noErrorAndTodayFirstJoin() {			
			meetingHost.setId(meetingHostId);
			
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlToday);
			testMeeting.setJoinList(joinList);
			testMeeting.setStartTime(sqlNow);
			testMeeting.setTopic(1);
			
			Users testUser = new Users();
			testUser.setId(testUserId);
			testUser.setContinuousDays(0);
			
			Records testRecord = new Records(testUser, testMeeting.getTopic());
			
			when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
			when(recordsRepository.findByUserAndTopic(testUser, testMeeting.getTopic())).thenReturn(Optional.of(testRecord));
			
			String warningMessage = meetingService.joinCheck(testMeeting, testUserId, locale, testSession);
						
			assertNull(warningMessage);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
			verify(recordsRepository, times(1)).saveAndFlush(any());
			verify(usersRepository, times(1)).saveAndFlush(any());
		}
	}
	
	@Nested
	@DisplayName("[ミーティング用sendMail()メソッドのテスト]")
	public class NestedTestSendMail {
		
		private Users meetingHost;
		private Users testUser;
		private Meetings testMeeting;
		private Deliveries testDeliveries; 
		private final java.sql.Date sqlToday = new java.sql.Date(System.currentTimeMillis());
		private final java.sql.Time sqlNow = new java.sql.Time(System.currentTimeMillis());
		private final ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
		private final ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
	
		@BeforeEach
		void init() {
			meetingHost = new Users();
			meetingHost.setId(1);
			meetingHost.setName("");
			meetingHost.setEmail("test@meetinghost.email");
			
			testUser = new Users();
			testUser.setId(2);
			testUser.setEmail("test@user.email");
			
			testMeeting = new Meetings();
			testMeeting.setHost(meetingHost);
			testMeeting.setDate(sqlToday);
			testMeeting.setStartTime(sqlNow);
			testMeeting.setEndTime(sqlNow);
		}

		@ParameterizedTest
		@CsvSource({", 2",
			"'create', 3",
			"'delete', 3",
		})
		@DisplayName("userへのメール")
		void sendToUser(String type, int count) {
			testDeliveries = new Deliveries(testUser.getId());
			
			when(deliveriesRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testDeliveries));
			
			meetingService.sendMail(testMeeting, testUser, type, locale);
		
			verify(deliveriesRepository, times(1)).findByUserId(userIdCaptor.capture());
			int capturedUserId = userIdCaptor.getValue();
			assertThat(capturedUserId).isEqualTo(testUser.getId());
			
			verify(messageSource, times(5 + count)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
			
			verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
			String capturedEmail = emailCaptor.getValue();
			assertThat(capturedEmail).isEqualTo(testUser.getEmail());
		}
		
		@ParameterizedTest
		@CsvSource({"'join', 3",
			"'leave', 2",
		})
		@DisplayName("ミーティング開催者へのメール")
		void sendToMeetingHost(String type, int count) {
			testDeliveries = new Deliveries(meetingHost.getId());
			
			when(deliveriesRepository.findByUserId(meetingHost.getId())).thenReturn(Optional.of(testDeliveries));
			
			meetingService.sendMail(testMeeting, testUser, type, locale);
		
			verify(deliveriesRepository, times(1)).findByUserId(userIdCaptor.capture());
			int capturedUserId = userIdCaptor.getValue();
			assertThat(capturedUserId).isEqualTo(meetingHost.getId());
			
			verify(messageSource, times(5 + count)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
			
			verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
			String capturedEmail = emailCaptor.getValue();
			assertThat(capturedEmail).isEqualTo(meetingHost.getEmail());
		}
		
		@ParameterizedTest
		@CsvSource({"'create'",
			"'delete'",
			"'join'",
			"'leave'",
		})
		@DisplayName("配信の許可がされておらず、メールを送らない")
		void notSend(String type) {
			
			testDeliveries = new Deliveries();
			testDeliveries.setMeetingCreated(0);
			testDeliveries.setMeetingDeleted(0);
			testDeliveries.setMeetingJoined(0);
			testDeliveries.setMeetingLeft(0);
			
			when(deliveriesRepository.findByUserId(any())).thenReturn(Optional.of(testDeliveries));
			
			meetingService.sendMail(testMeeting, testUser, type, locale);
		
			verify(deliveriesRepository, times(1)).findByUserId(any());
			
			verify(messageSource, times(5)).getMessage(any(), any(), localeCaptor.capture());
			Locale capturedLocale = localeCaptor.getValue();
			assertThat(capturedLocale).isEqualTo(locale);
			
			verify(mailService, never()).sendMail(any(), any(), any());
		}
	}
	
	@Test
	@DisplayName("[getUserMeetingListメソッドのテスト]")
	void testGetUserMeetingList() {
		final java.sql.Date sqlYesterday = new java.sql.Date(System.currentTimeMillis() - 86400000);
		final java.sql.Date sqlToday = new java.sql.Date(System.currentTimeMillis());
		final java.sql.Date sqlTomorrow = new java.sql.Date(System.currentTimeMillis() + 86400000);
		
		Users testUser = new Users();
		testUser.setId(1);
		
		Meetings testMeeting1 = new Meetings();
		Meetings testMeeting2 = new Meetings();
		Meetings testMeeting3 = new Meetings();
		testMeeting1.setId(1);
		testMeeting2.setId(2);
		testMeeting3.setId(3);
		testMeeting1.setDate(sqlYesterday);
		testMeeting2.setDate(sqlToday);
		testMeeting3.setDate(sqlTomorrow);
		
		List<Meetings> hostMeetingList = new ArrayList<>();
		hostMeetingList.add(testMeeting3);
		
		List<Meetings> joinMeetingList = new ArrayList<>();
		joinMeetingList.add(testMeeting2);
		joinMeetingList.add(testMeeting1);
		
		when(meetingsRepository.findByHostAndDateGreaterThanEqual(any(), any())).thenReturn(hostMeetingList);
		when(joinService.getJoinMeetingList(testUser.getId())).thenReturn(joinMeetingList);
		
		List<Meetings> result = meetingService.getUserMeetingList(testUser);
		
		List<Meetings> expected = new ArrayList<>();
		expected.add(testMeeting2);
		expected.add(testMeeting3);
		
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	@DisplayName("[getTodayMeetingListメソッドのテスト]")
	void testGetTodayMeetingList() {
		final java.sql.Date sqlYesterday = new java.sql.Date(System.currentTimeMillis() - 86400000);
		final java.sql.Date sqlToday = new java.sql.Date(System.currentTimeMillis());
		final java.sql.Date sqlTomorrow = new java.sql.Date(System.currentTimeMillis() + 86400000);
		
		Users testUser = new Users();
		testUser.setId(1);
		
		Meetings testMeeting1 = new Meetings();
		Meetings testMeeting2 = new Meetings();
		Meetings testMeeting3 = new Meetings();
		Meetings testMeeting4 = new Meetings();
		testMeeting1.setId(1);
		testMeeting2.setId(2);
		testMeeting3.setId(3);
		testMeeting4.setId(4);
		testMeeting1.setDate(sqlYesterday);
		testMeeting2.setDate(sqlToday);
		testMeeting3.setDate(sqlTomorrow);
		testMeeting4.setDate(sqlToday);
		
		List<Meetings> todayHostMeetingList = new ArrayList<>();
		todayHostMeetingList.add(testMeeting4);
		
		List<Meetings> joinMeetingList = new ArrayList<>();
		joinMeetingList.add(testMeeting2);
		joinMeetingList.add(testMeeting3);
		joinMeetingList.add(testMeeting1);
		
		when(meetingsRepository.findByHostAndDate(any(), any())).thenReturn(todayHostMeetingList);
		when(joinService.getJoinMeetingList(testUser.getId())).thenReturn(joinMeetingList);
		
		List<Meetings> result = meetingService.getTodayMeetingList(testUser);
		
		List<Meetings> expected = new ArrayList<>();
		expected.add(testMeeting2);
		expected.add(testMeeting4);
		
		assertThat(result).isEqualTo(expected);
	}
}
