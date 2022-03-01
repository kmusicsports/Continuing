package com.example.continuing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import com.example.continuing.entity.Users;
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

	@ParameterizedTest
	@CsvSource({"'newPass'", "'AnInvalidPasswordThatTheCharacterLengthExceeded'"})
	@DisplayName("[プロフィール編集用isValid()メソッドのテスト]パスワードの長さエラーのみ")
	void testIsInvalidLengthNewPasswordProfileData(String testNewPassword) {
		String testName = "testName";
		
		Users testOldData = new Users();
		testOldData.setName(testName);
		
		ProfileData testProfileData = new ProfileData();
		testProfileData.setName(testName);
		testProfileData.setNewPassword(testNewPassword);
		testProfileData.setNewPasswordAgain(testNewPassword);
		
		BindingResult result = new DataBinder(testProfileData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
		String getPassword = testProfileData.getNewPassword();
		String getPasswordAgain = testProfileData.getNewPasswordAgain();
		
		assertFalse(isValid);
		assertNull(getPassword);
		assertNull(getPasswordAgain);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
	@Test
	@DisplayName("[プロフィール編集用isValid()メソッドのテスト]パスワード不一致エラーのみ")
	void testIsInvalidUnmatchNewPasswordProfileData() {
		String testName = "testName";
		
		Users testOldData = new Users();
		testOldData.setName(testName);
		
		ProfileData testProfileData = new ProfileData();
		testProfileData.setName(testName);
		testProfileData.setNewPassword("testPassword");
		testProfileData.setNewPasswordAgain("testPasswordAgain");
		
		BindingResult result = new DataBinder(testProfileData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
		String getPassword = testProfileData.getNewPassword();
		String getPasswordAgain = testProfileData.getNewPasswordAgain();
		
		assertFalse(isValid);
		assertNull(getPassword);
		assertNull(getPasswordAgain);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}

	@Test
	@DisplayName("[プロフィール編集用isValid()メソッドのテスト]既に同じ名前が登録されているエラーのみ")
	void testIsInvalidAlreadyUsedNameProfileData() {
		String testName = "testName";
		
		ProfileData testProfileData = new ProfileData();
		testProfileData.setName(testName);
		testProfileData.setNewPassword("");
		testProfileData.setNewPasswordAgain("");
		
		Users testNameUser = new Users();
		testNameUser.setName(testName);
		
		when(usersRepository.findByName(testName)).thenReturn(Optional.of(testNameUser));
		
		Users testOldData = new Users();
		testOldData.setName("oldName");
		BindingResult result = new DataBinder(testProfileData).getBindingResult();
		Locale locale = new Locale("ja");
		
		boolean isValid = userService.isValid(testProfileData, testOldData, result, locale);
		String getName = testProfileData.getName();
		
		assertFalse(isValid);
		assertNull(getName);
		
		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
		Locale captoredLocale = localeCaptor.getValue();
		assertThat(captoredLocale).isEqualTo(locale);
	}
	
}
