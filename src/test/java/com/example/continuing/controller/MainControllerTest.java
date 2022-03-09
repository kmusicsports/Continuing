package com.example.continuing.controller;

import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @MockBean
    private JoinService joinService;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private UserService userService;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private MessageSource messageSource;

    @Autowired
    private MainController mainController;



    @Test
    @DisplayName("[showVerifyZoomメソッドのテスト]")
    public void testShowVerifyZoom() throws Exception {

        mockMvc.perform(get("/zoomverify/verifyzoom.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("verifyzoom"))
                .andExpect(model().size(0));
    }
}