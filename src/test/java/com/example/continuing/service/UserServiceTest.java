package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;

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
import com.example.continuing.form.EmailData;
import com.example.continuing.form.ProfileData;
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
	}
	
}
