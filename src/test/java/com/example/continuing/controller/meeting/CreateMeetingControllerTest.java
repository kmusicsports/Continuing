package com.example.continuing.controller.meeting;

import com.example.continuing.common.Utils;
import com.example.continuing.dto.MeetingDto;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
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


    @Nested
    @DisplayName("[createMeetingメソッドのテスト]")
    public class NestedTestCreateMeeting {

        private final int testUserId = 1;
        private Users testUser;
        private MeetingData testMeetingData;
        private static final String URL_TEMPLATE = "/create/meeting/redirect";
        private static final String TEST_CODE = "testCode";
        private static final String TEST_STATE = "testState";
        private final OAuth2AccessToken oauthToken = new OAuth2AccessToken("testAccessToken");
        private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);

        @BeforeEach
        public void init() {
            testUser = new Users();
            testUser.setId(testUserId);
            testUser.setLanguage("ja");

            testMeetingData = new MeetingData();
            testMeetingData.setTopic(1);
            testMeetingData.setNumberPeople(1);
            testMeetingData.setDate("2022-05-01");
            testMeetingData.setStartTime("09:00");
            testMeetingData.setEndTime("18:00");
            testMeetingData.setPassword("testPassword");
            testMeetingData.setAgenda("testPassword");

            createMeetingController.meetingData = testMeetingData;

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        }

        @Test
        @DisplayName("例外が発生")
        public void fail() throws Exception {
            String apiResult = "";

            when(zoomApiIntegration.getAccessToken(any(), any(), any())).thenReturn(oauthToken);
            when(zoomApiIntegration.createMeeting(any(), any())).thenReturn(apiResult);

            String path = "/home";
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("user_id", testUserId);
            sessionAttributes.put("path", path);

            mockMvc.perform(get(URL_TEMPLATE)
                            .sessionAttrs(sessionAttributes)
                            .param("code", TEST_CODE)
                            .param("state", TEST_STATE)
                    )
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(path))
                    .andExpect(flash().attributeExists("msg"));

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
            verify(zoomApiIntegration, times(1)).getAccessToken(any(), codeCaptor.capture(), stateCaptor.capture());
            String capturedCode = codeCaptor.getValue();
            String capturedState = stateCaptor.getValue();
            assertThat(capturedCode).isEqualTo(TEST_CODE);
            assertThat(capturedState).isEqualTo(TEST_STATE);

            MeetingDto meetingDto = testMeetingData.toDto(messageSource, new Locale(testUser.getLanguage()));
            verify(zoomApiIntegration, times(1)).createMeeting(oauthToken, meetingDto);

            verify(meetingsRepository, never()).saveAndFlush(any());
            verify(followService, never()).getFollowersList(any());
            verify(meetingService, never()).sendMail(any(), any(), any(), any());

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }

        @Test
        @DisplayName("例外が発生しない")
        public void success() throws Exception {

            String apiResult = "{\n" +
                    "  \"assistant_id\": \"kFFvsJc-Q1OSxaJQLva_A\",\n" +
                    "  \"host_email\": \"jchill@example.com\",\n" +
                    "  \"id\": 92674392836,\n" +
                    "  \"uuid\": 926743928361,\n" +
                    "  \"registration_url\": \"https://example.com/meeting/register/7ksAkRCoEpt1Jm0wa-E6lICLur9e7Lde5oW6\",\n" +
                    "  \"agenda\": \"My Meeting\",\n" +
                    "  \"created_at\": \"2022-03-25T07:29:29Z\",\n" +
                    "  \"duration\": 60,\n" +
                    "  \"h323_password\": \"123456\",\n" +
                    "  \"join_url\": \"https://example.com/j/11111\",\n" +
                    "  \"occurrences\": [\n" +
                    "    {\n" +
                    "      \"duration\": 60,\n" +
                    "      \"occurrence_id\": \"1648194360000\",\n" +
                    "      \"start_time\": \"2022-03-25T07:46:00Z\",\n" +
                    "      \"status\": \"available\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"password\": \"123456\",\n" +
                    "  \"pmi\": 97891943927,\n" +
                    "  \"pre_schedule\": false,\n" +
                    "  \"recurrence\": {\n" +
                    "    \"end_date_time\": \"2022-04-02T15:59:00Z\",\n" +
                    "    \"end_times\": 7,\n" +
                    "    \"monthly_day\": 1,\n" +
                    "    \"monthly_week\": 1,\n" +
                    "    \"monthly_week_day\": 1,\n" +
                    "    \"repeat_interval\": 1,\n" +
                    "    \"type\": 1,\n" +
                    "    \"weekly_days\": \"1\"\n" +
                    "  },\n" +
                    "  \"settings\": {\n" +
                    "    \"allow_multiple_devices\": true,\n" +
                    "    \"alternative_hosts\": \"jchill@example.com;thill@example.com\",\n" +
                    "    \"alternative_hosts_email_notification\": true,\n" +
                    "    \"alternative_host_update_polls\": true,\n" +
                    "    \"approval_type\": 0,\n" +
                    "    \"approved_or_denied_countries_or_regions\": {\n" +
                    "      \"approved_list\": [\n" +
                    "        \"CX\"\n" +
                    "      ],\n" +
                    "      \"denied_list\": [\n" +
                    "        \"CA\"\n" +
                    "      ],\n" +
                    "      \"enable\": true,\n" +
                    "      \"method\": \"approve\"\n" +
                    "    },\n" +
                    "    \"audio\": \"telephony\",\n" +
                    "    \"authentication_domains\": \"example.com\",\n" +
                    "    \"authentication_exception\": [\n" +
                    "      {\n" +
                    "        \"email\": \"jchill@example.com\",\n" +
                    "        \"name\": \"Jill Chill\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"authentication_name\": \"Sign in to Zoom\",\n" +
                    "    \"authentication_option\": \"signIn_D8cJuqWVQ623CI4Q8yQK0Q\",\n" +
                    "    \"auto_recording\": \"cloud\",\n" +
                    "    \"breakout_room\": {\n" +
                    "      \"enable\": true,\n" +
                    "      \"rooms\": [\n" +
                    "        {\n" +
                    "          \"name\": \"room1\",\n" +
                    "          \"participants\": [\n" +
                    "            \"jchill@example.com\"\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"calendar_type\": 1,\n" +
                    "    \"close_registration\": false,\n" +
                    "    \"cn_meeting\": false,\n" +
                    "    \"contact_email\": \"jchill@example.com\",\n" +
                    "    \"contact_name\": \"Jill Chill\",\n" +
                    "    \"custom_keys\": [\n" +
                    "      {\n" +
                    "        \"key\": \"key1\",\n" +
                    "        \"value\": \"value1\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"email_notification\": true,\n" +
                    "    \"encryption_type\": \"enhanced_encryption\",\n" +
                    "    \"enforce_login\": true,\n" +
                    "    \"enforce_login_domains\": \"example.com\",\n" +
                    "    \"focus_mode\": true,\n" +
                    "    \"global_dial_in_countries\": [\n" +
                    "      \"US\"\n" +
                    "    ],\n" +
                    "    \"global_dial_in_numbers\": [\n" +
                    "      {\n" +
                    "        \"city\": \"New York\",\n" +
                    "        \"country\": \"US\",\n" +
                    "        \"country_name\": \"US\",\n" +
                    "        \"number\": \"+1 1000200200\",\n" +
                    "        \"type\": \"toll\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"host_video\": true,\n" +
                    "    \"in_meeting\": false,\n" +
                    "    \"jbh_time\": 0,\n" +
                    "    \"join_before_host\": true,\n" +
                    "    \"language_interpretation\": {\n" +
                    "      \"enable\": true,\n" +
                    "      \"interpreters\": [\n" +
                    "        {\n" +
                    "          \"email\": \"interpreter@example.com\",\n" +
                    "          \"languages\": \"US,FR\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"meeting_authentication\": true,\n" +
                    "    \"mute_upon_entry\": false,\n" +
                    "    \"participant_video\": false,\n" +
                    "    \"private_meeting\": false,\n" +
                    "    \"registrants_confirmation_email\": true,\n" +
                    "    \"registrants_email_notification\": true,\n" +
                    "    \"registration_type\": 1,\n" +
                    "    \"show_share_button\": true,\n" +
                    "    \"use_pmi\": false,\n" +
                    "    \"waiting_room\": false,\n" +
                    "    \"watermark\": false\n" +
                    "  },\n" +
                    "  \"start_time\": \"2022-03-25T07:29:29Z\",\n" +
                    "  \"start_url\": \"https://example.com/s/11111\",\n" +
                    "  \"timezone\": \"America/Los_Angeles\",\n" +
                    "  \"topic\": \"My Meeting\",\n" +
                    "  \"tracking_fields\": [\n" +
                    "    {\n" +
                    "      \"field\": \"field1\",\n" +
                    "      \"value\": \"value1\",\n" +
                    "      \"visible\": true\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"type\": 2\n" +
                    "}";

            Users follower = new Users();
            follower.setId(3);
            follower.setLanguage("en");

            List<Users> followersList = new ArrayList<>();
            followersList.add(follower);

            when(zoomApiIntegration.getAccessToken(any(), any(), any())).thenReturn(oauthToken);
            when(zoomApiIntegration.createMeeting(any(), any())).thenReturn(apiResult);
            when(followService.getFollowersList(testUserId)).thenReturn(followersList);

            mockMvc.perform(get(URL_TEMPLATE)
                            .sessionAttr("user_id", testUserId)
                            .param("code", TEST_CODE)
                            .param("state", TEST_STATE)
                    )
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("/Meeting/*"))
                    .andExpect(flash().attributeExists("msg"));

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
            verify(zoomApiIntegration, times(1)).getAccessToken(any(), codeCaptor.capture(), stateCaptor.capture());
            String capturedCode = codeCaptor.getValue();
            String capturedState = stateCaptor.getValue();
            assertThat(capturedCode).isEqualTo(TEST_CODE);
            assertThat(capturedState).isEqualTo(TEST_STATE);

            MeetingDto meetingDto = testMeetingData.toDto(messageSource, new Locale(testUser.getLanguage()));
            verify(zoomApiIntegration, times(1)).createMeeting(oauthToken, meetingDto);

            ArgumentCaptor<Meetings> meetingCaptor = ArgumentCaptor.forClass(Meetings.class);
            verify(meetingsRepository, times(1)).saveAndFlush(meetingCaptor.capture());
            Meetings capturedMeeting = meetingCaptor.getValue();
            assertThat(capturedMeeting.getHost()).isEqualTo(testUser);
            assertThat(capturedMeeting.getTopic()).isEqualTo(testMeetingData.getTopic());
            assertThat(capturedMeeting.getNumberPeople()).isEqualTo(testMeetingData.getNumberPeople());
            assertThat(capturedMeeting.getDate()).isEqualTo(Utils.strToDate(testMeetingData.getDate()));
            assertThat(capturedMeeting.getStartTime()).isEqualTo(Utils.strToTime(testMeetingData.getStartTime()));
            assertThat(capturedMeeting.getEndTime()).isEqualTo(Utils.strToTime(testMeetingData.getEndTime()));
            assertThat(capturedMeeting.getPassword()).isEqualTo(testMeetingData.getPassword());
            assertThat(capturedMeeting.getAgenda()).isEqualTo(testMeetingData.getAgenda());

            ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
            verify(meetingService, times(1)).sendMail(any(), userCaptor.capture(), any(), localeCaptor.capture());
            Users capturedUser = userCaptor.getValue();
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedUser).isEqualTo(follower);
            assertThat(capturedLocale).isEqualTo(new Locale(follower.getLanguage()));

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
            capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }
    }
}