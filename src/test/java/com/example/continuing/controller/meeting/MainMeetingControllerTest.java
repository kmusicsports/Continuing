package com.example.continuing.controller.meeting;

import com.example.continuing.entity.Joins;
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
import com.example.continuing.zoom.ZoomDetails;
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

import java.util.*;

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

    @Captor
    ArgumentCaptor<Joins> joinCaptor;

    @Captor
    ArgumentCaptor<Meetings> meetingCaptor;

    @Captor
    ArgumentCaptor<Users> userCaptor;

    @Nested
    @DisplayName("[showMeetingDetailメソッドのテスト]")
    public class NestedTestShowMeetingDetail {

        private final int testMeetingId = 1;
        private final int testUserId = 2;
        private final String urlTemplate = "/Meeting/" + testMeetingId;
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

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
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

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("meetingDetail"))
                    .andExpect(request().sessionAttribute("path", urlTemplate))
                    .andExpect(model().attribute("meeting", testMeeting))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("joinUserList", joinUserList))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("searchData", new SearchData()));
        }
    }

    @Nested
    @DisplayName("[joinMeetingメソッドのテスト]")
    public class NestedTestJoinMeeting {

        private final int testMeetingId = 1;
        private final int testUserId = 2;
        private final String urlTemplate = "/Meeting/join/" + testMeetingId;
        private Users testUser;

        @BeforeEach
        public void init() {

            testUser = new Users();
            testUser.setId(testUserId);
            testUser.setLanguage("ja");
        }

        @Test
        @DisplayName("指定したミーティングが見つからない場合")
        public void meetingNotFound() throws Exception {
            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(joinsRepository, never()).saveAllAndFlush(any());
            verify(meetingService, never()).sendMail(any(), any(), any(), any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }

        @Test
        @DisplayName("指定したミーティングが見つかった場合")
        public void meetingFound() throws Exception {
            String path = "/Meeting/" + testMeetingId;

            Users meetingHost = new Users();
            meetingHost.setId(3);
            meetingHost.setLanguage("en");

            Meetings testMeeting = new Meetings();
            testMeeting.setId(testMeetingId);
            testMeeting.setHost(meetingHost);

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));

            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("user_id", testUserId);
            sessionAttributes.put("path", path);

            mockMvc.perform(get(urlTemplate).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(path));

            verify(messageSource, never()).getMessage(any(), any(), any());

            verify(joinsRepository, times(1)).saveAndFlush(joinCaptor.capture());
            Joins capturedJoin = joinCaptor.getValue();
            assertThat(capturedJoin.getUserId()).isEqualTo(testUserId);
            assertThat(capturedJoin.getMeeting()).isEqualTo(testMeeting);

            verify(meetingService, times(1)).sendMail(meetingCaptor.capture(), userCaptor.capture(), any(), localeCaptor.capture());

            Meetings capturedMeeting = meetingCaptor.getValue();
            assertThat(capturedMeeting).isEqualTo(testMeeting);

            Users capturedUser = userCaptor.getValue();
            assertThat(capturedUser).isEqualTo(testUser);

            Locale capturedLocale = localeCaptor.getValue();
            String expectedLang = testMeeting.getHost().getLanguage();
            assertThat(capturedLocale).isEqualTo(new Locale(expectedLang));
        }
    }

    @Nested
    @DisplayName("[leaveMeetingメソッドのテスト]")
    public class NestedTestLeaveMeeting {

        private final int testMeetingId = 1;
        private final int testUserId = 2;
        private final String urlTemplate = "/Meeting/leave/" + testMeetingId;
        private Users testUser;

        @BeforeEach
        public void init() {

            testUser = new Users();
            testUser.setId(testUserId);
            testUser.setLanguage("ja");
        }

        @Test
        @DisplayName("指定したミーティングが見つからない場合")
        public void meetingNotFound() throws Exception {
            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(joinsRepository, never()).findById(any());
            verify(joinsRepository, never()).deleteAll(any());
            verify(meetingService, never()).sendMail(any(), any(), any(), any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }

        @Test
        @DisplayName("指定したミーティングが見つかった場合")
        public void meetingFound() throws Exception {
            String path = "/Meeting/" + testMeetingId;

            Users meetingHost = new Users();
            meetingHost.setId(3);
            meetingHost.setLanguage("en");

            Meetings testMeeting = new Meetings();
            testMeeting.setId(testMeetingId);
            testMeeting.setHost(meetingHost);

            List<Joins> joinList = new ArrayList<>();
            joinList.add(new Joins(testUserId, testMeeting));

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));
            when(joinsRepository.findByUserIdAndMeeting(testUserId, testMeeting)).thenReturn(joinList);

            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("user_id", testUserId);
            sessionAttributes.put("path", path);

            mockMvc.perform(get(urlTemplate).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(path));

            verify(messageSource, never()).getMessage(any(), any(), any());

            verify(joinsRepository, times(1)).deleteAll(joinList);

            verify(meetingService, times(1)).sendMail(meetingCaptor.capture(), userCaptor.capture(), any(), localeCaptor.capture());

            Meetings capturedMeeting = meetingCaptor.getValue();
            assertThat(capturedMeeting).isEqualTo(testMeeting);

            Users capturedUser = userCaptor.getValue();
            assertThat(capturedUser).isEqualTo(testUser);

            Locale capturedLocale = localeCaptor.getValue();
            String expectedLang = testMeeting.getHost().getLanguage();
            assertThat(capturedLocale).isEqualTo(new Locale(expectedLang));
        }
    }

    @Test
    @DisplayName("[cancelメソッドのテスト]")
    public void testCancel() throws Exception {
        String path = "/home";

        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("user_id", 1);
        sessionAttributes.put("path", path);

        mockMvc.perform(get("/Meeting/cancel").sessionAttrs(sessionAttributes))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(path));
    }

    @Nested
    @DisplayName("[joinCheckメソッドのテスト]")
    public class NestedTestJoinCheck {

        private final int testMeetingId = 1;
        private final int testUserId = 2;
        private final String urlTemplate = "/Meeting/check/" + testMeetingId;
        private Users testUser;
        private Meetings testMeeting;
        private Locale expectedLocale;

        @BeforeEach
        public void init() {

            testUser = new Users();
            testUser.setId(testUserId);
            testUser.setLanguage("ja");

            testMeeting = new Meetings();
            testMeeting.setId(testMeetingId);

            expectedLocale = new Locale(testUser.getLanguage());
        }

        @Test
        @DisplayName("指定したミーティングが見つからない場合")
        public void meetingNotFound() throws Exception {
            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(meetingService, never()).joinCheck(any(), any(), any(), any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(expectedLocale);
        }

        @Test
        @DisplayName("MeetingServiceによるチェックでエラーが有る場合")
        public void meetingServiceWarning() throws Exception {
            ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));
            when(meetingService.joinCheck(any(), any(), any(), any())).thenReturn("Warning!");

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/Meeting/" + testMeetingId))
                    .andExpect(flash().attributeExists("msg"));

            verify(meetingService, times(1)).joinCheck(meetingCaptor.capture(), userIdCaptor.capture(), localeCaptor.capture(), any());

            Meetings capturedMeeting = meetingCaptor.getValue();
            assertThat(capturedMeeting).isEqualTo(testMeeting);

            Integer capturedUserId = userIdCaptor.getValue();
            assertThat(capturedUserId).isEqualTo(testUserId);

            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(expectedLocale);

            verify(messageSource, never()).getMessage(any(), any(), any());
        }

        @Test
        @DisplayName("指定したミーティングの開催者が自身の場合")
        public void meetingHostIsUser() throws Exception {

            testMeeting.setHost(testUser);
            testMeeting.setStartUrl("/testStartUrl");

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(testMeeting.getStartUrl()));

            verify(messageSource, never()).getMessage(any(), any(), any());
        }

        @Test
        @DisplayName("指定したミーティングの開催者が自身ではない場合")
        public void meetingHostIsNotUser() throws Exception {

            Users meetingHost = new Users();
            meetingHost.setId(3);

            testMeeting.setHost(meetingHost);
            testMeeting.setJoinUrl("/testJoinUrl");

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(testMeeting.getJoinUrl()));

            verify(messageSource, never()).getMessage(any(), any(), any());
        }
    }

    @Test
    @DisplayName("[showTodayMyMeetingsメソッドのテスト]")
    public void testShowTodayMyMeetings() throws Exception {
        int testUserId1 = 1;

        Users testUser1 = new Users();
        testUser1.setId(testUserId1);

        Users testUser2 = new Users();
        testUser2.setId(2);

        Meetings testMeeting1 = new Meetings();
        testMeeting1.setId(1);
        testMeeting1.setHost(testUser1);

        Meetings testMeeting2 = new Meetings();
        testMeeting2.setId(2);
        testMeeting2.setHost(testUser2);

        List<Meetings> todayMeetingList = new ArrayList<>();
        todayMeetingList.add(testMeeting1);

        List<Meetings> myJoinMeetingList = new ArrayList<>();
        myJoinMeetingList.add(testMeeting2);

        when(usersRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
        when(meetingService.getTodayMeetingList(testUser1)).thenReturn(todayMeetingList);
        when(joinService.getJoinMeetingList(testUserId1)).thenReturn(myJoinMeetingList);

        mockMvc.perform(get("/Meeting/list/mine/today").sessionAttr("user_id", testUserId1))
                .andExpect(status().isOk())
                .andExpect(view().name("todayMyMeetings"))
                .andExpect(model().attribute("meetingList", todayMeetingList))
                .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                .andExpect(model().attribute("searchData", new SearchData()));
    }

    @Test
    @DisplayName("[zoomIntegrationsメソッドのテスト]")
    public void testZoomIntegrations() throws Exception {

        mockMvc.perform(get("/integrations/zoom"))
                .andExpect(status().isOk())
                .andExpect(view().name("zoomIntegrations"))
                .andExpect(model().attribute("searchData", new SearchData()));
    }

    @Test
    @DisplayName("[redirectメソッドのテスト]")
    public void testRedirect() throws Exception {
        String testAuthUrl = "/testAuthUrl";

        when(zoomApiIntegration.getAuthorizationUrl(any())).thenReturn(testAuthUrl);

        mockMvc.perform(get("/zoom"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(testAuthUrl));

        String actual = ZoomDetails.getZOOM_STATE();
        String expected = "zoom";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("[callbackメソッドのテスト]")
    public void testCallback() throws Exception {
        Locale locale = new Locale("ja");

        mockMvc.perform(get("/redirect")
                        .param("code", "testCode")
                        .param("state", "testState")
                        .locale(locale)
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attributeExists("msg"));

        verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
        Locale capturedLocale = localeCaptor.getValue();
        assertThat(capturedLocale).isEqualTo(locale);
    }

}