package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import com.example.continuing.entity.Users;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.RegisterData;
import com.example.continuing.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
	
	@Mock
	private UsersRepository usersRepository;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Mock
	private MailService mailService;
	
	@Mock
	private MessageSource messageSource;
	
	@InjectMocks
	private LoginService loginService;

	@Test
	@DisplayName("[ログイン用isValid()メソッドのテスト]someUser.isPresent()==false")
	void testEmailIsInvalidLoginData() {
		LoginData testLoginData = new LoginData();
		testLoginData.setEmail("test@email");
		
		when(usersRepository.findByEmail(testLoginData.getEmail())).thenReturn(Optional.empty());

		boolean isValid = loginService.isValid(testLoginData, null);
		
		assertFalse(isValid);
	}
	
	@Test
	@DisplayName("[ログイン用isValid()メソッドのテスト]someUser.isPresent()==true && passwordEncoder.matches()==false")
	void testPasswordIsInvalidLoginData() {
		String testEmail = "test@email";
		String testPassword = "testpassword";
		String invalidPassword = "invalidpassword";
		
		LoginData testLoginData = new LoginData();
		testLoginData.setEmail(testEmail);
		testLoginData.setPassword(invalidPassword);
		
		Users testUser = new Users();
		testUser.setEmail(testEmail);
		testUser.setPassword(testPassword);
		
		when(usersRepository.findByEmail(testLoginData.getEmail())).thenReturn(Optional.of(testUser));

		boolean isValid = loginService.isValid(testLoginData, null);
		
		assertFalse(isValid);
	}
	
	@Test
	@DisplayName("[ログイン用isValid()メソッドのテスト]someUser.isPresent()==true && passwordEncoder.matches()==true")
	void testIsValidLoginData() {
		String testEmail = "test@email";
		String testPassword = "testpassword";
		
		LoginData testLoginData = new LoginData();
		testLoginData.setEmail(testEmail);
		testLoginData.setPassword(testPassword);
		
		Users testUser = new Users();
		testUser.setEmail(testEmail);
		testUser.setPassword(passwordEncoder.encode(testPassword));
		
		when(usersRepository.findByEmail(testLoginData.getEmail())).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(testPassword, passwordEncoder.encode(testPassword))).thenReturn(true);

		boolean isValid = loginService.isValid(testLoginData, null);
		
		assertTrue(isValid);
	}

	@Test
	@DisplayName("[新規登録用isValid()メソッドのテスト]パスワード不一致エラーのみ")
	void testIsInvalidUnmathPasswordRegisterData() {
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName("testName");
		testRegisterData.setEmail("test@email");
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPasswordAgain");
		testRegisterData.setChecked(true);
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		String getPassword = testRegisterData.getPassword();
		String getPasswordAgain = testRegisterData.getPasswordAgain();
		
		assertFalse(isValid);
		assertNull(getPassword);
		assertNull(getPasswordAgain);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
	@Test
	@DisplayName("[新規登録用isValid()メソッドのテスト]既に同じ名前が登録されているエラーのみ")
	void testIsInvalidAlreadyUsedNameRegisterData() {
		String testName = "testName";
		
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName(testName);
		testRegisterData.setEmail("test@email");
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPassword");
		testRegisterData.setChecked(true);
		
		Users testNameUser = new Users();
		testNameUser.setName(testName);
		
		when(usersRepository.findByName(testName)).thenReturn(Optional.of(testNameUser));
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		String getName = testRegisterData.getName();
		
		assertFalse(isValid);
		assertNull(getName);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}

	@Test
	@DisplayName("[新規登録用isValid()メソッドのテスト]名前が全角スペースだけで構成されているエラーのみ")
	void testIsInvalidDoubleSpaceNameRegisterData() {
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName("　　　　　"); // 全角スペースのみ
		testRegisterData.setEmail("test@email");
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPassword");
		testRegisterData.setChecked(true);
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		
		assertFalse(isValid);

		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
	@Test
	@DisplayName("[新規登録用isValid()メソッドのテスト]名前にアプリ名が入っているエラーのみ")
	void testIsInvalidIncludeAppNameRegisterData() {
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName("Continuing");
		testRegisterData.setEmail("test@email");
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPassword");
		testRegisterData.setChecked(true);
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		
		assertFalse(isValid);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
	@Test
	@DisplayName("[新規登録用isValid()メソッドのテスト]既にメールアドレスが登録されているエラーのみ")
	void testIsInvalidAlreadyUsedEmailRegisterData() {
		String testEmail = "test@email";
		
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName("testName");
		testRegisterData.setEmail(testEmail);
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPassword");
		testRegisterData.setChecked(true);
		
		Users testEmailUser = new Users();
		testEmailUser.setEmail(testEmail);
		when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testEmailUser));
		
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		String getEmail = testRegisterData.getEmail();
		
		assertFalse(isValid);
		assertNull(getEmail);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
	@ParameterizedTest
	@CsvSource({"'testEmail@icloud.com'",
	"'testEmail@mac.com'",
	"'testEmail@me.com'",
	})
	@DisplayName("[新規登録用isValid()メソッドのテスト]Apple系のメールアドレスエラーのみ")
	void testIsInvalidAppleBasedEmailRegisterData(String testEmail) {
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName("testName");
		testRegisterData.setEmail(testEmail);
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPassword");
		testRegisterData.setChecked(true);
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		String getEmail = testRegisterData.getEmail();
		
		assertFalse(isValid);
		assertNull(getEmail);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
	@Test
	@DisplayName("[新規登録用isValid()メソッドのテスト]エラーなし、名前がnull")
	void testIsValidNullNameRegisterData() {
		RegisterData testRegisterData = new RegisterData();
		testRegisterData.setName(null);
		testRegisterData.setEmail("test@email");
		testRegisterData.setPassword("testPassword");
		testRegisterData.setPasswordAgain("testPassword");
		testRegisterData.setChecked(true);
		
		BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = loginService.isValid(testRegisterData, result, locale);
		
		assertTrue(isValid);
	}
	
	@ParameterizedTest
	@CsvSource({"'welcome', 6",
	", 2",
	})
	@DisplayName("[sendMail()メソッドのテスト]tokenなし")
	void testSendMailTokenNull(String type, int count) {
		String testEmail = "test@email";
		Locale locale = new Locale("ja");
		
		String token = loginService.sendMail(testEmail, type, locale);
		
		assertNull(token);

		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(count)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
		
		ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
		String captoredEmail = emailCaptor.getValue();
		assertThat(captoredEmail).isEqualTo(testEmail);
	}
	
	@ParameterizedTest
	@CsvSource({"'reset-password', 2",
	"'registration', 3",
	})
	@DisplayName("[sendMail()メソッドのテスト]tokenあり")
	void testSendMailTokenNotNull(String type, int count) {
		String testEmail = "test@email";
		Locale locale = new Locale("ja");
		
		String token = loginService.sendMail(testEmail, type, locale);
		
		assertNotNull(token);

		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(count)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
		
		ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
		String captoredEmail = emailCaptor.getValue();
		assertThat(captoredEmail).isEqualTo(testEmail);
	}

}
