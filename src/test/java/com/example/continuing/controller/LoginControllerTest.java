package com.example.continuing.controller;

import com.example.continuing.form.EmailData;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.TemporariesRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.LoginService;
import com.example.continuing.service.TemporaryService;
import com.example.continuing.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private LoginService loginService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private CsrfTokenRepository csrfTokenRepository;

    @MockBean
    private DeliveriesRepository deliveriesRepository;

    @MockBean
    private TemporariesRepository temporariesRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private TemporaryService temporaryService;

    @Autowired
    private LoginController loginController;

    
}