package com.example.continuing.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.example.continuing.entity.Deliveries;
import com.example.continuing.form.DeliveryData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.UsersRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private DeliveriesRepository deliveriesRepository;
	
	@MockBean
	private UsersRepository usersRepository;
	
	@MockBean
	private MessageSource messageSource;
	
	@Autowired
	private DeliveryController deliveryController;

	@Test
	@DisplayName("[showSettingFormメソッドのテスト]")
	void testShowSettingForm() throws Exception {
		int testUserId = 1;
		
		Deliveries testDeliveries = new Deliveries(testUserId);
		when(deliveriesRepository.findByUserId(testUserId)).thenReturn(Optional.of(testDeliveries));
		
		String path = "/User/setting/emailDelivery";
		
		mockMvc.perform(get(path).sessionAttr("user_id", testUserId))
				.andExpect(status().isOk())
				.andExpect(view().name("deliverySetting"))
	            .andExpect(request().sessionAttribute("path", path))
	            .andExpect(model().attribute("searchData", new SearchData()))
	            .andExpect(model().attribute("deliveryData", new DeliveryData(testDeliveries)))
	            .andReturn();
		
		verify(deliveriesRepository, times(1)).findByUserId(testUserId);
	}
	
}
