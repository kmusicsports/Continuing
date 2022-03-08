package com.example.continuing.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
	
	@Nested
	@DisplayName("[isValidメソッドのテスト]")
	public class NestedTestIsValid {
		
		private static final String TEST_EMAIL = "test@email";
		private static final String TEST_TOKEN = "testToken";
		private List<Temporaries> testList;
		
		@BeforeEach
		void init() {
			testList = new ArrayList<>();
		}
		
		@Test
		@DisplayName("エラーなし")
		void noError() {
			
			Temporaries testTemporary = new Temporaries();
			testTemporary.setEmail(TEST_EMAIL);
			testTemporary.setToken(TEST_TOKEN);
			
			testList.add(testTemporary);
			
			when(temporariesRepository.findByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(testList);
			
			boolean isValid = temporaryService.isValid(TEST_EMAIL, TEST_TOKEN);
			
			assertTrue(isValid);
		}
		
		@Test
		@DisplayName("tokenが無効エラーのみ")
		void tokenIsInvalidError() {
			String invalidToken = "invalidToken";
			
			Temporaries testTemporary = new Temporaries();
			testTemporary.setEmail(TEST_EMAIL);
			testTemporary.setToken(TEST_TOKEN);
			
			testList.add(testTemporary);
			
			when(temporariesRepository.findByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(testList);
			
			boolean isValid = temporaryService.isValid(TEST_EMAIL, invalidToken);
			
			assertFalse(isValid);
		}
		
		@Test
		@DisplayName("仮登録なしエラーのみ")
		void temporariesNotExistError() {
			
			when(temporariesRepository.findByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(testList);
			
			boolean isValid = temporaryService.isValid(TEST_EMAIL, TEST_TOKEN);
			
			assertFalse(isValid);
		}
	}

}
