package com.example.continuing.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.continuing.entity.Temporaries;
import com.example.continuing.repository.TemporariesRepository;

@ExtendWith(MockitoExtension.class)
class TemporaryServiceTest {

	@Mock
	private TemporariesRepository temporariesRepository;
	
	@InjectMocks
	private TemporaryService temporaryService;
	
	@Test
	@DisplayName("[isValidメソッドのテスト]temporariesList.size() != 0 && token is valid")
	void testIsValid() {
		String testEmail = "test.email";
		String testToken = "testtoken";
		
		Temporaries testTemporary = new Temporaries();
		testTemporary.setEmail(testEmail);
		testTemporary.setToken(testToken);
		
		List<Temporaries> testList = new ArrayList<>();
		testList.add(testTemporary);
		
		when(temporariesRepository.findByEmailOrderByCreatedAtDesc(testEmail)).thenReturn(testList);
		
		boolean isValid = temporaryService.isValid(testEmail, testToken);
		
		assertTrue(isValid);
	}

	@Test
	@DisplayName("[isValidメソッドのテスト]temporariesList.size() != 0 && token is invalid")
	void testTokenIsInvalid() {
		String testEmail = "test.email";
		String testToken = "testtoken";
		String invalidToken = "invalidtoken";
		
		Temporaries testTemporary = new Temporaries();
		testTemporary.setEmail(testEmail);
		testTemporary.setToken(testToken);
		
		List<Temporaries> testList = new ArrayList<>();
		testList.add(testTemporary);
		
		when(temporariesRepository.findByEmailOrderByCreatedAtDesc(testEmail)).thenReturn(testList);
		
		boolean isValid = temporaryService.isValid(testEmail, invalidToken);
		
		assertFalse(isValid);
	}
	
	@Test
	@DisplayName("[isValidメソッドのテスト]temporariesList.size() == 0")
	void testTemporariesNotExist() {
		String testEmail = "test.email";
		String testToken = "testtoken";
		
		List<Temporaries> testList = new ArrayList<>();
		
		when(temporariesRepository.findByEmailOrderByCreatedAtDesc(testEmail)).thenReturn(testList);
		
		boolean isValid = temporaryService.isValid(testEmail, testToken);
		
		assertFalse(isValid);
	}
}
