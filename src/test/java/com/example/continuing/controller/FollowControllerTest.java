package com.example.continuing.controller;

import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

    @MockBean
    private FollowsRepository followsRepository;

    @MockBean
    private FollowService followService;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private MessageSource messageSource;

    @Autowired
    FollowController followController;

    @Captor
    private ArgumentCaptor<Locale> localeCaptor;

    
}