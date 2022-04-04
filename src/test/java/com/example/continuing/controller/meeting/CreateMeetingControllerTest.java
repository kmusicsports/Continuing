package com.example.continuing.controller.meeting;

import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
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
class CreateMeetingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private MeetingsRepository meetingsRepository;

    @MockBean
    private ZoomApiIntegration zoomApiIntegration;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private FollowService followService;

    @MockBean
    private MessageSource messageSource;

    @Autowired
    CreateMeetingController createMeetingController;

    @Test
    @DisplayName("[createMeetingFormメソッドのテスト]")
    public void testCreateMeetingForm() throws Exception {

        mockMvc.perform(get("/Meeting/showCreateForm").sessionAttr("user_id", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("meetingForm"))
                .andExpect(request().sessionAttribute("mode", "create"))
                .andExpect(model().attribute("meetingData", new MeetingData()))
                .andExpect(model().attribute("searchData", new SearchData()));
    }


    
}