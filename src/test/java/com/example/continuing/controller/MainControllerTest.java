package com.example.continuing.controller;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Nested
    @DisplayName("[showHome(Model)メソッドのテスト]")
    public class NestedTestShowHomeModel {

        @Test
        @DisplayName("userId == null")
        public void userIdIsNull() throws Exception {

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("welcome"))
                    .andExpect(model().attribute("searchData", new SearchData()));
        }

        @Test
        @DisplayName("userId != null")
        public void userIdIsNotNull() throws Exception {

            mockMvc.perform(get("/").sessionAttr("user_id", 1))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"));
        }
    }

    @Nested
    @DisplayName("[showHome(ModelAndView, Pageable)メソッドのテスト]")
    public class NestedTestShowHomeMAV {

        private static final String PATH = "/home";
        private List<Users> userList;
        private List<Users> userRanking;
        private Map<Integer, Integer> rankingMap;
        private final int testUserId = 1;
        private List<Users> myFollowsList;
        private List<Meetings> myJoinMeetingList;
        private SearchData searchData;

        @BeforeEach
        public void init() {

            userList = new ArrayList<>();
            userRanking = new ArrayList<>();
            rankingMap = new TreeMap<>();
            myFollowsList = new ArrayList<>();
            myJoinMeetingList = new ArrayList<>();
            searchData = new SearchData();
            Users testUser1 = new Users();
            Users testUser2 = new Users();
            Users testUser3 = new Users();
            Meetings testMeeting = new Meetings();

            int continuousDays = 2;
            testUser1.setId(1);
            testUser2.setId(2);
            testUser3.setId(3);
            testUser2.setContinuousDays(continuousDays);
            testMeeting.setId(1);
            userList.add(testUser1);
            userRanking.add(testUser2);
            rankingMap.put(continuousDays, 1);
            myFollowsList.add(testUser3);
            myJoinMeetingList.add(testMeeting);

            when(usersRepository.findTop3By()).thenReturn(userRanking);
            when(userService.makeRankingMap(userRanking)).thenReturn(rankingMap);
            when(followService.getFollowsList(testUserId)).thenReturn(myFollowsList);
            when(joinService.getJoinMeetingList(testUserId)).thenReturn(myJoinMeetingList);
        }

        @Test
        @DisplayName("searchData == null && prevPageable == null")
        public void nullAndNull() throws Exception {
            when(userService.getSearchResult(searchData)).thenReturn(userList);

            mockMvc.perform(get(PATH).sessionAttr("user_id", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(request().sessionAttribute("path", PATH))
                    .andExpect(model().attributeExists("meetingPage"))
                    .andExpect(model().attributeExists("meetingList"))
                    .andExpect(model().attribute("userList", userList))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("searchData", searchData))
                    .andExpect(model().attribute("userRanking", userRanking))
                    .andExpect(model().attribute("rankingMap", rankingMap));

            verify(userService, times(1)).getSearchResult(searchData);
            verify(usersRepository, times(1)).findTop3By();
            verify(userService, times(1)).makeRankingMap(userRanking);
            verify(followService, times(1)).getFollowsList(testUserId);
            verify(joinService, times(1)).getJoinMeetingList(testUserId);
        }

        @Test
        @DisplayName("searchData != null && prevPageable == null")
        public void notNullAndNull() throws Exception {
            searchData.setKeyword("keyword");

            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("user_id", testUserId);
            sessionAttributes.put("searchData", searchData);

            when(userService.getSearchResult(searchData)).thenReturn(userList);

            mockMvc.perform(get(PATH).sessionAttrs(sessionAttributes))
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(request().sessionAttribute("path", PATH))
                    .andExpect(model().attributeExists("meetingPage"))
                    .andExpect(model().attributeExists("meetingList"))
                    .andExpect(model().attribute("userList", userList))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("searchData", searchData))
                    .andExpect(model().attribute("userRanking", userRanking))
                    .andExpect(model().attribute("rankingMap", rankingMap));

            verify(userService, times(1)).getSearchResult(searchData);
            verify(usersRepository, times(1)).findTop3By();
            verify(userService, times(1)).makeRankingMap(userRanking);
            verify(followService, times(1)).getFollowsList(testUserId);
            verify(joinService, times(1)).getJoinMeetingList(testUserId);
        }
    }

    @Nested
    @DisplayName("[searchメソッドのテスト]")
    public class NestedTestSearch {

        private List<Users> userList;
        private List<Users> userRanking;
        private Map<Integer, Integer> rankingMap;
        private final int testUserId = 1;
        private List<Users> myFollowsList;
        private List<Meetings> myJoinMeetingList;
        private SearchData searchData;
        private Users testUser1;
        private Users testUser2;
        private Users testUser3;
        private Meetings testMeeting;
        private final Locale localeOfRequest = new Locale("ja");
        private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);

        @BeforeEach
        public void init() {
            userList = new ArrayList<>();
            userRanking = new ArrayList<>();
            rankingMap = new TreeMap<>();
            myFollowsList = new ArrayList<>();
            myJoinMeetingList = new ArrayList<>();
            searchData = new SearchData();
            testUser1 = new Users();
            testUser2 = new Users();
            testUser3 = new Users();
            testMeeting = new Meetings();

            testUser1.setId(1);
            testUser2.setId(2);
            testUser3.setId(3);
            testMeeting.setId(1);
            userRanking.add(testUser2);

            int continuousDays = 2;
            testUser2.setContinuousDays(continuousDays);
            rankingMap.put(continuousDays, 1);

            when(usersRepository.findTop3By()).thenReturn(userRanking);
            when(userService.makeRankingMap(userRanking)).thenReturn(rankingMap);
        }

        @Test
        @DisplayName("userId == null && isValid == false")
        public void isInvalid() throws Exception {
            when(meetingService.isValid(any(), any(), any())).thenReturn(false);

            mockMvc.perform(post("/search")
                            .flashAttr("searchData", searchData)
                            .locale(localeOfRequest)
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(request().sessionAttribute("path", "/home"))
                    .andExpect(model().attributeDoesNotExist("meetingPage"))
                    .andExpect(model().attributeDoesNotExist("meetingList"))
                    .andExpect(model().attributeDoesNotExist("userList"))
                    .andExpect(model().attributeExists("msgMeeting"))
                    .andExpect(model().attributeDoesNotExist("msgAccount"))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("searchData", searchData))
                    .andExpect(model().attribute("userRanking", userRanking))
                    .andExpect(model().attribute("rankingMap", rankingMap));

            verify(userService, never()).getSearchResult(searchData);
            verify(usersRepository, times(1)).findTop3By();
            verify(userService, times(1)).makeRankingMap(userRanking);
            verify(followService, times(1)).getFollowsList(null);
            verify(joinService, times(1)).getJoinMeetingList(null);

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(localeOfRequest);
        }

        @Test
        @DisplayName("userId == null && isValid == true && meetingPage.getContent().size() == 0 && userList.size() != 0")
        public void userListIsNotEmpty() throws Exception {
            userList.add(testUser1);

            when(meetingService.isValid(any(), any(), any())).thenReturn(true);
            when(userService.getSearchResult(searchData)).thenReturn(userList);

            mockMvc.perform(post("/search")
                            .flashAttr("searchData", searchData)
                            .locale(localeOfRequest)
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(request().sessionAttribute("path", "/home"))
                    .andExpect(request().sessionAttribute("searchData", searchData))
                    .andExpect(model().attributeExists("meetingPage"))
                    .andExpect(model().attributeExists("meetingList"))
                    .andExpect(model().attribute("userList", userList))
                    .andExpect(model().attributeExists("msgMeeting"))
                    .andExpect(model().attributeDoesNotExist("msgAccount"))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("searchData", searchData))
                    .andExpect(model().attribute("userRanking", userRanking))
                    .andExpect(model().attribute("rankingMap", rankingMap));

            verify(userService, times(1)).getSearchResult(searchData);
            verify(usersRepository, times(1)).findTop3By();
            verify(userService, times(1)).makeRankingMap(userRanking);
            verify(followService, times(1)).getFollowsList(null);
            verify(joinService, times(1)).getJoinMeetingList(null);

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(localeOfRequest);
        }

        @Test
        @DisplayName("userId != null && isValid == true && meetingPage.getContent().size() == 0 && userList.size() == 0") // meetingPage.getContent().size() != 0
        public void userListIsEmpty() throws Exception {

            testUser1.setLanguage("en");
            myFollowsList.add(testUser3);
            myJoinMeetingList.add(testMeeting);

            when(followService.getFollowsList(testUserId)).thenReturn(myFollowsList);
            when(joinService.getJoinMeetingList(testUserId)).thenReturn(myJoinMeetingList);
            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser1));
            when(meetingService.isValid(any(), any(), any())).thenReturn(true);
            when(userService.getSearchResult(searchData)).thenReturn(userList);

            mockMvc.perform(post("/search")
                            .flashAttr("searchData", searchData)
                            .locale(localeOfRequest)
                            .sessionAttr("user_id", testUserId)
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("home"))
                    .andExpect(request().sessionAttribute("path", "/home"))
                    .andExpect(request().sessionAttribute("searchData", searchData))
                    .andExpect(model().attributeExists("meetingPage"))
                    .andExpect(model().attributeExists("meetingList"))
                    .andExpect(model().attribute("userList", userList))
                    .andExpect(model().attributeExists("msgMeeting"))
                    .andExpect(model().attributeExists("msgAccount"))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("searchData", searchData))
                    .andExpect(model().attribute("userRanking", userRanking))
                    .andExpect(model().attribute("rankingMap", rankingMap));

            verify(userService, times(1)).getSearchResult(searchData);
            verify(usersRepository, times(1)).findTop3By();
            verify(userService, times(1)).makeRankingMap(userRanking);
            verify(followService, times(1)).getFollowsList(testUserId);
            verify(joinService, times(1)).getJoinMeetingList(testUserId);

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), any());
//            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
//            Locale capturedLocale = localeCaptor.getValue();
//            assertThat(capturedLocale).isEqualTo(new Locale(testUser1.getLanguage()));
        }
    }

    @ParameterizedTest
    @CsvSource({"'th', 'privacy/privacyPolicy'",
            "'ja', 'privacy/privacyPolicy_ja'",
            "'en', 'privacy/privacyPolicy_en'",
    })
    @DisplayName("[showPrivacyPolicyメソッドのテスト]")
    public void testShowPrivacyPolicy(String language, String viewName) throws Exception {
        Locale locale = new Locale(language);

        mockMvc.perform(get("/privacy").locale(locale))
                .andExpect(status().isOk())
                .andExpect(view().name(viewName))
                .andExpect(model().size(0));
    }

    @Test
    @DisplayName("[showVerifyZoomメソッドのテスト]")
    public void testShowVerifyZoom() throws Exception {

        mockMvc.perform(get("/zoomverify/verifyzoom.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("verifyzoom"))
                .andExpect(model().size(0));
    }
}