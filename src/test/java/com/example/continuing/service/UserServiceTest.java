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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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

import com.example.continuing.entity.Users;
import com.example.continuing.form.ContactData;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	
	@Mock
	private UsersRepository usersRepository;
	
	@Mock
	private MailService mailService;
	
	@Mock
	private MessageSource messageSource;
	
	@InjectMocks
	private UserService userService;
	
	private final Locale locale = new Locale("ja");
	private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
	
	@Nested
	@DisplayName("[プロフィール編集用isValid()メソッドのテスト]")
	public class testIsValidProfileData {
		
		private ProfileData testProfileData;
		private Users testOldData;
		private final BindingResult result = new DataBinder(testProfileData).getBindingResult();
		private static final String TEST_OLD_NAME = "oldName";
		
		@BeforeEach
		void init() {
			testProfileData = new ProfileData();
			
			testOldData = new Users();
			testOldData.setName(TEST_OLD_NAME);
		}
		
		@ParameterizedTest
		@CsvSource({"'newPass'", "'AnInvalidPasswordThatTheCharacterLengthExceeded'"})
		@DisplayName("パスワードの長さエラーのみ")
		void newPasswordLengthError(String testNewPassword) {
			
			testProfileData.setName(TEST_OLD_NAME);
			testProfileData.setNewPassword(testNewPassword);
			testProfileData.setNewPasswordAgain(testNewPassword);	
			
			boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
			String getPassword = testProfileData.getNewPassword();
			String getPasswordAgain = testProfileData.getNewPasswordAgain();
			
			assertFalse(isValid);
			assertNull(getPassword);
			assertNull(getPasswordAgain);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("パスワード不一致エラーのみ")
		void unmatchNewPasswordError() {
			
			testProfileData.setName(TEST_OLD_NAME);
			testProfileData.setNewPassword("testPassword");
			testProfileData.setNewPasswordAgain("testPasswordAgain");
			
			boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
			String getPassword = testProfileData.getNewPassword();
			String getPasswordAgain = testProfileData.getNewPasswordAgain();
			
			assertFalse(isValid);
			assertNull(getPassword);
			assertNull(getPasswordAgain);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}		
		
		@Test
		@DisplayName("既に同じ名前が登録されているエラーのみ")
		void alreadyUsedNameError() {
			String testName = "testName";
			
			testProfileData.setName(testName);
			testProfileData.setNewPassword("");
			testProfileData.setNewPasswordAgain("");
			
			Users testNameUser = new Users();
			testNameUser.setName(testName);
			
			when(usersRepository.findByName(testName)).thenReturn(Optional.of(testNameUser));
			
			boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
			String getName = testProfileData.getName();
			
			assertFalse(isValid);
			assertNull(getName);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("名前が全角スペースで構成されているエラーのみ")
		void doubleSpaceNameError() {
			String testName = "　　"; // 全角スペースのみ
			
			testProfileData.setName(testName);
			testProfileData.setNewPassword("");
			testProfileData.setNewPasswordAgain("");
			
			boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
			String getName = testProfileData.getName();
			
			assertFalse(isValid);
			assertNull(getName);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("名前にアプリ名が含まれているエラーのみ")
		void includedContinuingNameError() {
			String testName = "AppNameIsContinuing";
			
			testProfileData.setName(testName);
			testProfileData.setNewPassword("");
			testProfileData.setNewPasswordAgain("");
			
			boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
			String getName = testProfileData.getName();
			
			assertFalse(isValid);
			assertNull(getName);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
	
		@Test
		@DisplayName("エラーなし、名前がnull")
		void noErrorAndNullName() {
			
			testProfileData.setName(null);
			testProfileData.setNewPassword("");
			testProfileData.setNewPasswordAgain("");
			
			boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
			
			assertTrue(isValid);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
		}
	}
	
	@Nested
	@DisplayName("[メールアドレス変更用isValid()メソッドのテスト]")
	public class testIsValidEmailData {
		
		private EmailData testEmailData;
		private final BindingResult result = new DataBinder(testEmailData).getBindingResult();
		private static final String TEST_EMAIL = "test@email";
	
		@BeforeEach
		void init() {
			testEmailData = new EmailData();
		}
 		
		@Test
		@DisplayName("既にメールアドレスが登録されているエラーのみ")
		void alreadyUsedEmailError() {
			testEmailData.setEmail(TEST_EMAIL);
			
			Users testEmailUser = new Users();
			testEmailUser.setEmail(TEST_EMAIL);
			
			when(usersRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testEmailUser));
			
			boolean isValid = userService.isValid(testEmailData, result, locale);
			
			assertFalse(isValid);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@ParameterizedTest
		@CsvSource({"'testEmail@icloud.com'",
			"'testEmail@mac.com'",
			"'testEmail@me.com'",
		})
		@DisplayName("Apple系のメールアドレスエラーのみ")
		void appleBasedEmailError(String testEmail) {
			testEmailData.setEmail(testEmail);
			
			boolean isValid = userService.isValid(testEmailData, result, locale);
			
			assertFalse(isValid);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("エラーなし")
		void noError() {
			testEmailData.setEmail(TEST_EMAIL);
			
			boolean isValid = userService.isValid(testEmailData, result, locale);
			
			assertTrue(isValid);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
		}
	}
	
	@Test
	@DisplayName("[getSearchResultメソッドのテスト]")
	void testGetSearchResult() {
		String testSearchKeyword = "testSearchKeyword";
		
		SearchData testSearchData = new SearchData();
		testSearchData.setKeyword(testSearchKeyword);
		
		Users testUser1 = new Users();
		Users testUser2 = new Users();
		Users testUser3 = new Users();
		testUser1.setId(1);
		testUser2.setId(2);
		testUser3.setId(3);
		
		List<Users> userListName = new ArrayList<Users>();
		userListName.add(testUser3);
		userListName.add(testUser1);
		
		List<Users> userListProfileMessage =  new ArrayList<Users>();
		userListProfileMessage.add(testUser1);
		userListProfileMessage.add(testUser2);
		
		when(usersRepository.findByNameContainingIgnoreCase(testSearchKeyword)).thenReturn(userListName);
		when(usersRepository.findByProfileMessageContainingIgnoreCase(testSearchKeyword)).thenReturn(userListProfileMessage);
		
		List<Users> expected = new ArrayList<Users>();
		expected.add(testUser1);
		expected.add(testUser2);
		expected.add(testUser3);
		
		List<Users> result = userService.getSearchReuslt(testSearchData);
		
		assertThat(result).isEqualTo(expected);
		verify(usersRepository, times(1)).findByNameContainingIgnoreCase(testSearchKeyword);
		verify(usersRepository, times(1)).findByProfileMessageContainingIgnoreCase(testSearchKeyword);
	}
	
	@Test
	@DisplayName("[makeRankingMapメソッドのテスト]")
	void testMakeRankingMap() {
		int testFirstDays = 30;
		int testSecondDays = 15;
		int testThirdDays = 0;
		
		Users testUser1 = new Users();
		Users testUser2 = new Users();
		Users testUser3 = new Users();
		Users testUser4 = new Users();
		testUser1.setContinuousDays(testSecondDays);
		testUser2.setContinuousDays(testFirstDays);
		testUser3.setContinuousDays(testThirdDays);
		testUser4.setContinuousDays(testSecondDays);
		
		List<Users> testUserList = new ArrayList<Users>();
		testUserList.add(testUser1);
		testUserList.add(testUser2);
		testUserList.add(testUser3);
		testUserList.add(testUser4);
		
		Map<Integer, Integer> expected = new TreeMap<>();
		expected.put(testFirstDays, 1);
		expected.put(testSecondDays, 2);
		expected.put(testThirdDays, 3);
		
		Map<Integer, Integer> result = userService.makeRankingMap(testUserList);
		
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	@DisplayName("[sendContactEmailメソッドのテスト]")
	void testSendContactEmail() {
		ContactData testContactData = new ContactData();
		
		userService.sendContactEmail(testContactData);
		
		verify(mailService, times(1)).sendMail(any(), any(), any());
	}
	
	@Test
	@DisplayName("[sendAuthenticationEmailメソッドのテスト]")
	void testSendAuthenticationEmail() {
		String testEmail = "test@email";
		
		String token = userService.sendAuthenticationEmail(testEmail, locale);
		
		assertNotNull(token);
		
		ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
		String captoredEmail = emailCaptor.getValue();
		assertThat(captoredEmail).isEqualTo(testEmail);
		
		verify(messageSource, times(2)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
}
