package com.example.continuing.controller.meeting;

import com.example.continuing.repository.JoinsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MainMeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeetingsRepository meetingsRepository;

    @MockBean
    private JoinService joinService;

    @MockBean
    private FollowService followService;

    @MockBean
    private JoinsRepository joinsRepository;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private ZoomApiIntegration zoomApiIntegration;

    @Autowired
    private MainMeetingController mainController;

    @Captor
    ArgumentCaptor<Locale> localeCaptor;


}