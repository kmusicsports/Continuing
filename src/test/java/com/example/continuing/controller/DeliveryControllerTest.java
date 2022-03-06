package com.example.continuing.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

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


}
