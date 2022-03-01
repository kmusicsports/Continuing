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

	private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
	
	private static final String TEST_EMAIL = "test@email";
	private static final String TEST_PASS = "testpassword";
	
	@Nested
	@DisplayName("[ログイン用isValid()メソッドのテスト]")
	public class testIsValidLoginData {
		
		private LoginData testLoginData;
		private Users testUser;

		@BeforeEach
		void init() {
			testLoginData = new LoginData();
			testLoginData.setEmail(TEST_EMAIL);
			
			testUser = new Users();
			testUser.setEmail(TEST_EMAIL);
		}
		
		@Test
		@DisplayName("メールアドレスが間違っているエラーのみ")
		void emailError() {
			
			boolean isValid = loginService.isValid(testLoginData, null);
			
			assertFalse(isValid);
		}
		
		@Test
		@DisplayName("パスワードが間違っているエラーのみ")
		void passwordError() {
			
			String invalidPassword = "invalidpassword";

			testLoginData.setPassword(invalidPassword);
			
			testUser.setPassword(TEST_PASS);
			
			when(usersRepository.findByEmail(testLoginData.getEmail())).thenReturn(Optional.of(testUser));
			
			boolean isValid = loginService.isValid(testLoginData, null);
			
			assertFalse(isValid);
		}
		
		@Test
		@DisplayName("エラーなし")
		void noError() {
			
			testLoginData.setPassword(TEST_PASS);
			
			testUser.setPassword(passwordEncoder.encode(TEST_PASS));
			
			when(usersRepository.findByEmail(testLoginData.getEmail())).thenReturn(Optional.of(testUser));
			when(passwordEncoder.matches(TEST_PASS, passwordEncoder.encode(TEST_PASS))).thenReturn(true);
			
			boolean isValid = loginService.isValid(testLoginData, null);
			
			assertTrue(isValid);
		}
	}

	@Nested
	@DisplayName("[新規登録用isValid()メソッドのテスト]")
	public class testIsValidRegisterData {
		
		private RegisterData testRegisterData;
		private final Locale locale = new Locale("ja");
		private final BindingResult result = new DataBinder(testRegisterData).getBindingResult();
		
		@BeforeEach
		void init() {
			testRegisterData = new RegisterData();
			testRegisterData.setPassword(TEST_PASS);
			testRegisterData.setChecked(true);
		}
		
		@Test
		@DisplayName("パスワード不一致エラーのみ")
		void unmatchPasswordError() {
			
			testRegisterData.setName("testName");
			testRegisterData.setEmail(TEST_EMAIL);
			testRegisterData.setPasswordAgain("testUnmatchPassword");
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			String getPassword = testRegisterData.getPassword();
			String getPasswordAgain = testRegisterData.getPasswordAgain();
			
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
			
			testRegisterData.setName(testName);
			testRegisterData.setEmail(TEST_EMAIL);
			testRegisterData.setPasswordAgain(TEST_PASS);
			
			Users testNameUser = new Users();
			testNameUser.setName(testName);
			
			when(usersRepository.findByName(testName)).thenReturn(Optional.of(testNameUser));
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			String getName = testRegisterData.getName();
			
			assertFalse(isValid);
			assertNull(getName);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}

		@Test
		@DisplayName("名前が全角スペースだけで構成されているエラーのみ")
		void doubleSpaceNameError() {
			
			testRegisterData.setName("　　　　　"); // 全角スペースのみ
			testRegisterData.setEmail(TEST_EMAIL);
			testRegisterData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			
			assertFalse(isValid);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("名前にアプリ名が入っているエラーのみ")
		void appNameError() {
			
			testRegisterData.setName("Continuing");
			testRegisterData.setEmail(TEST_EMAIL);
			testRegisterData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			
			assertFalse(isValid);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
				
		@Test
		@DisplayName("既にメールアドレスが登録されているエラーのみ")
		void alreadyUsedEmailError() {
			testRegisterData.setName("testName");
			testRegisterData.setEmail(TEST_EMAIL);
			testRegisterData.setPasswordAgain(TEST_PASS);
			
			Users testEmailUser = new Users();
			testEmailUser.setEmail(TEST_EMAIL);
			
			when(usersRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testEmailUser));
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			String getEmail = testRegisterData.getEmail();
			
			assertFalse(isValid);
			assertNull(getEmail);
			
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
			
			testRegisterData.setName("testName");
			testRegisterData.setEmail(testEmail);
			testRegisterData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			String getEmail = testRegisterData.getEmail();
			
			assertFalse(isValid);
			assertNull(getEmail);
			
			verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
		}
		
		@Test
		@DisplayName("エラーなし、名前がnull")
		void noErrorAndNullName() {
			
			testRegisterData.setName(null);
			testRegisterData.setEmail(TEST_EMAIL);
			testRegisterData.setPasswordAgain(TEST_PASS);
			
			boolean isValid = loginService.isValid(testRegisterData, result, locale);
			
			assertTrue(isValid);
			
			verify(messageSource, never()).getMessage(any(), any(), any());
		}
	}
	
	@Nested
	@DisplayName("[sendMail()メソッドのテスト]")
	public class testSendMail {
		
		private final Locale locale = new Locale("ja");
		private final ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		
		@ParameterizedTest
		@CsvSource({"'welcome', 6",
			", 2",
		})
		@DisplayName("tokenなし")
		void tokenNull(String type, int count) {
			String token = loginService.sendMail(TEST_EMAIL, type, locale);
			
			assertNull(token);
			
			verify(messageSource, times(count)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
			
			verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
			String captoredEmail = emailCaptor.getValue();
			assertThat(captoredEmail).isEqualTo(TEST_EMAIL);
		}
		
		@ParameterizedTest
		@CsvSource({"'reset-password', 2",
			"'registration', 3",
		})
		@DisplayName("tokenあり")
		void tokenNotNull(String type, int count) {
			String token = loginService.sendMail(TEST_EMAIL, type, locale);
			
			assertNotNull(token);
			
			verify(messageSource, times(count)).getMessage(any(), any(), localeCaptor.capture());
			Locale captoredLocale = localeCaptor.getValue();
			assertThat(captoredLocale).isEqualTo(locale);
			
			verify(mailService, times(1)).sendMail(emailCaptor.capture(), any(), any());
			String captoredEmail = emailCaptor.getValue();
			assertThat(captoredEmail).isEqualTo(TEST_EMAIL);
		}
	}
	

}
