package com.example.continuing.controller.meeting;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.JoinsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Nested
    @DisplayName("[showMeetingDetailメソッドのテスト]")
    public class NestedTestShowMeetingDetail {

        private final int testMeetingId = 1;
        private final int testUserId = 2;
        private final String path = "/Meeting/" + testMeetingId;
        private Users testUser;

        @BeforeEach
        public void init() {
            testUser = new Users();
            testUser.setId(testUserId);
        }

        @Test
        @DisplayName("指定したミーティングが見つからない場合")
        public void meetingNotFound() throws Exception {
            testUser.setLanguage("ja");

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get(path).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }

        @Test
        @DisplayName("指定したミーティングが見つかった場合")
        public void meetingFound() throws Exception {

            Meetings testMeeting = new Meetings();
            testMeeting.setId(testMeetingId);
            testMeeting.setHost(testUser);

            Users testUser3 = new Users();
            Users testUser4 = new Users();
            testUser3.setId(3);
            testUser4.setId(4);

            List<Meetings> myJoinMeetingList = new ArrayList<>();
            List<Users> myFollowsList = new ArrayList<>();
            List<Users> joinUserList = new ArrayList<>();

            myJoinMeetingList.add(testMeeting);
            myFollowsList.add(testUser3);
            joinUserList.add(testUser4);

            when(joinService.getJoinMeetingList(testUserId)).thenReturn(myJoinMeetingList);
            when(followService.getFollowsList(testUserId)).thenReturn(myFollowsList);
            when(joinService.getJoinUserList(testMeeting)).thenReturn(joinUserList);

            when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));

            mockMvc.perform(get(path).sessionAttr("user_id", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("meetingDetail"))
                    .andExpect(request().sessionAttribute("path", path))
                    .andExpect(model().attribute("meeting", testMeeting))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("joinUserList", joinUserList))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("searchData", new SearchData()));
        }
    }

}