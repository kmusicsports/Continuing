package com.example.continuing.controller.meeting;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
import com.example.continuing.zoom.ZoomDetails;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class DeleteMeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeetingsRepository meetingsRepository;

    @MockBean
    private ZoomApiIntegration zoomApiIntegration;

    @MockBean
    private JoinService joinService;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private UsersRepository usersRepository;

    @Autowired
    private DeleteMeetingController deleteMeetingController;

    @Nested
    @DisplayName("[createRedirectメソッドのテスト]")
    public class NestedTestCreateRedirect {

        private final int testMeetingId = 1;
        private final String urlTemplate = "/Meeting/delete/" + testMeetingId;
        private final int testUserId = 2;
        private Users testUser;
        private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);

        @BeforeEach
        public void init() {

            testUser = new Users();
            testUser.setId(testUserId);
            testUser.setLanguage("ja");

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        }

        @Test
        @DisplayName("指定したミーティングが見つからない場合")
        public void testMeetingNotFound() throws Exception {

            mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(zoomApiIntegration, never()).getAuthorizationUrl(any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }

        @Nested
        @DisplayName("指定したミーティングが見つかった場合")
        public class NestedTestMeetingFound {

            private Meetings testMeeting;

            @BeforeEach
            public void init() {
                testMeeting = new Meetings();
                testMeeting.setId(testMeetingId);
            }

            @Test
            @DisplayName("指定したミーティングの開催者が自分ではない場合")
            public void meetingHostIsNotUser() throws Exception {

                Users meetingHost = new Users();
                meetingHost.setId(3);

                testMeeting.setHost(meetingHost);

                when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));

                mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                        .andExpect(status().isFound())
                        .andExpect(redirectedUrl("/Meeting/" + testMeetingId))
                        .andExpect(flash().attributeExists("msg"));

                verify(zoomApiIntegration, never()).getAuthorizationUrl(any());

                verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
                Locale capturedLocale = localeCaptor.getValue();
                assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
            }

            @Test
            @DisplayName("指定したミーティングの開催者が自分だった場合")
            public void success() throws Exception {
                testMeeting.setHost(testUser);

                String testAuthUrl = "/testAuthUrl";

                when(meetingsRepository.findById(testMeetingId)).thenReturn(Optional.of(testMeeting));
                when(zoomApiIntegration.getAuthorizationUrl(any())).thenReturn(testAuthUrl);

                mockMvc.perform(get(urlTemplate).sessionAttr("user_id", testUserId))
                        .andExpect(status().isFound())
                        .andExpect(redirectedUrl(testAuthUrl));

                verify(messageSource, never()).getMessage(any(), any(), any());

                String actual = ZoomDetails.getZOOM_STATE();
                String expected = "zoom_delete";

                assertThat(actual).isEqualTo(expected);
            }
        }
    }

    @Test
    @DisplayName("[deleteMeetingメソッドのテスト]")
    public void testDeleteMeeting() throws Exception {
        int idOfMeeting = 1;

        deleteMeetingController.id = idOfMeeting;

        Users meetingHost = new Users();
        meetingHost.setId(2);
        meetingHost.setLanguage("ja");

        Meetings testMeeting = new Meetings();
        testMeeting.setId(idOfMeeting);
        testMeeting.setHost(meetingHost);
        testMeeting.setMeetingId("testMeetingId");

        OAuth2AccessToken oauthToken = new OAuth2AccessToken("testAccessToken");

        Users joinUser = new Users();
        joinUser.setId(3);
        joinUser.setLanguage("en");

        List<Users> joinUserList = new ArrayList<>();
        joinUserList.add(joinUser);

        when(meetingsRepository.findById(idOfMeeting)).thenReturn(Optional.of(testMeeting));
        when(zoomApiIntegration.getAccessToken(any(), any(), any())).thenReturn(oauthToken);
        when(joinService.getJoinUserList(testMeeting)).thenReturn(joinUserList);

        String testCode = "testCode";
        String testState = "testState";

        mockMvc.perform(get("/delete/meeting/redirect")
                        .param("code", testCode)
                        .param("state", testState)
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/mypage"));

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        verify(zoomApiIntegration, times(1)).getAccessToken(any(), codeCaptor.capture(), stateCaptor.capture());
        String capturedCode = codeCaptor.getValue();
        String capturedState = stateCaptor.getValue();
        assertThat(capturedCode).isEqualTo(testCode);
        assertThat(capturedState).isEqualTo(testState);

        verify(zoomApiIntegration, times(1)).deleteMeeting(oauthToken, testMeeting.getMeetingId());

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        ArgumentCaptor<Meetings> meetingCaptor = ArgumentCaptor.forClass(Meetings.class);
        ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
        verify(meetingService, times(1)).sendMail(meetingCaptor.capture(), userCaptor.capture(), any(), localeCaptor.capture());
        Meetings capturedMeeting = meetingCaptor.getValue();
        Users capturedUser = userCaptor.getValue();
        Locale capturedLocale = localeCaptor.getValue();
        assertThat(capturedMeeting).isEqualTo(testMeeting);
        assertThat(capturedUser).isEqualTo(joinUser);
        assertThat(capturedLocale).isEqualTo(new Locale(joinUser.getLanguage()));

        verify(meetingsRepository, times(1)).deleteById(idOfMeeting);

        verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
        capturedLocale = localeCaptor.getValue();
        assertThat(capturedLocale).isEqualTo(new Locale(meetingHost.getLanguage()));
    }

}