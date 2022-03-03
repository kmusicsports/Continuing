package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import com.example.continuing.comparator.MeetingsComparator;
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
	private MeetingsComparator meetingsComparator;
	
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
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private final String TODAY = sdf.format(new Date());
	private static final String TEST_START_TIME = "00:00";
	
	@Nested
	@DisplayName("[ミーティングフォーム用isValid()メソッドのテスト]")
	public class testIsValidMeetingData {
		
		private MeetingData testMeetingData;
		private final BindingResult result = new DataBinder(testMeetingData).getBindingResult();
		private static final String TEST_PASS = "testpassword";
	
		@BeforeEach
		void init() {
			testMeetingData = new MeetingData();
		}
	
		@Test
		@DisplayName("パスワード不一致エラーのみ")
		void unmatchPasswordError() {
			
			testMeetingData.setNumberPeople(1);
			testMeetingData.setDate(TODAY);
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
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@ParameterizedTest
		@CsvSource({"'00:10'", "'01:00'"})
		@DisplayName("複数人でのミーティングにおいて、ミーティング時間が15分以上40分以下でないエラーのみ")
		void multiplePeoplemeetingTimeError(String testEndTime) {
			
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
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@ParameterizedTest
		@CsvSource({"'00:10'", "'36:00'"})
		@DisplayName("1対1でのミーティングにおいて、ミーティング時間が15分以上1800分(30時間)以下でないエラーのみ")
		void oneOnOnemeetingTimeError(String testEndTime) {
			
			testMeetingData.setNumberPeople(1);
			testMeetingData.setDate(TODAY);
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
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
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
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("ミーティングの日付がDate型に変換できないエラーのみ")
		void invalidFormatDateError() {
			
			testMeetingData.setNumberPeople(2);
			testMeetingData.setDate((new Date()).toString());
			testMeetingData.setStartTime(TEST_START_TIME);
			testMeetingData.setEndTime("00:20");
			testMeetingData.setPassword(TEST_PASS);
			testMeetingData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = meetingService.isValid(testMeetingData, false, result, locale);
			String getDate = testMeetingData.getDate();
			
			assertFalse(isValid);
			assertNull(getDate);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
	}
	
	@Nested
	@DisplayName("[検索条件用isValid()メソッドのテスト]")
	public class testIsValidSearchData {
		
		private SearchData testSearchData;
		private final BindingResult result = new DataBinder(testSearchData).getBindingResult();
	
		@BeforeEach
		void init() {
			testSearchData = new SearchData();		
		}
		
		@Test
		@DisplayName("日付がDate型に変換できないエラーのみ")
		void invalidFormatDateError() {
			
			testSearchData.setDate((new Date()).toString());
			testSearchData.setStartTime(TEST_START_TIME);
			testSearchData.setEndTime("00:20");
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			String getDate = testSearchData.getDate();
			
			assertFalse(isValid);
			assertNull(getDate);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("開始時刻の形式エラーのみ")
		void invalidFormatStartTimeError() {
			
			testSearchData.setDate(TODAY);
			testSearchData.setStartTime((new Date()).toString());
			testSearchData.setEndTime("00:20");
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			String getStartTime = testSearchData.getStartTime();
			
			assertFalse(isValid);
			assertNull(getStartTime);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("終了時刻の形式エラーのみ")
		void invalidFormatEndTimeError() {
			
			testSearchData.setDate(TODAY);
			testSearchData.setStartTime(TEST_START_TIME);
			testSearchData.setEndTime((new Date()).toString());
			
			boolean isValid = meetingService.isValid(testSearchData, result, locale);
			String getEndTime = testSearchData.getEndTime();
			
			assertFalse(isValid);
			assertNull(getEndTime);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
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
}
