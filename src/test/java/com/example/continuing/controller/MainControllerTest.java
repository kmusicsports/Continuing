package com.example.continuing.controller;

import com.example.continuing.dao.MeetingsDaoImpl;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
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

    @MockBean
    private MeetingsDaoImpl meetingsDaoImpl;

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
        private final int continuousDays = 2;
        private List<Users> myFollowsList;
        private List<Meetings> myJoinMeetingList;
        private SearchData searchData;
        private Users testUser1;
        private Users testUser2;
        private Users testUser3;
        private Meetings testMeeting;

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