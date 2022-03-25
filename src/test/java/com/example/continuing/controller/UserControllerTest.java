package com.example.continuing.controller;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Temporaries;
import com.example.continuing.entity.Users;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.*;
import com.example.continuing.service.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private StorageService storageService;

    @MockBean
    private FollowService followService;

    @MockBean
    private FollowsRepository followsRepository;

    @MockBean
    private MeetingsRepository meetingsRepository;

    @MockBean
    private JoinService joinService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private DeliveriesRepository deliveriesRepository;

    @MockBean
    private TemporariesRepository temporariesRepository;

    @MockBean
    private TemporaryService temporaryService;

    @Autowired
    private UserController userController;

    @Captor
    private ArgumentCaptor<Users> userCaptor;

    @Captor
    private ArgumentCaptor<List<Temporaries>> temporaryListCaptor;

    @Nested
    @DisplayName("[showUserDetailメソッドのテスト]")
    public class NestedTestShowUserDetail {

        private final int pathUserId = 1;
        private final int sessionUserId = 2;
        private Users testUser;
        private final String path = "/User/" + pathUserId;

        @BeforeEach
        public void init() {
            testUser = new Users();
        }

        @Test
        @DisplayName("ユーザーが見つからなかった場合")
        public void testUserNotFound() throws Exception {
            ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);

            testUser.setId(sessionUserId);
            testUser.setLanguage("ja");

            when(usersRepository.findById(sessionUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get(path).sessionAttr("user_id", sessionUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(followService, never()).getFollowsList(any());
            verify(followService, never()).getFollowersList(any());
            verify(meetingService, never()).getUserMeetingList(any());
            verify(followService, never()).getFollowsList(any());
            verify(joinService, never()).getJoinMeetingList(any());
            verify(usersRepository, times(1)).findById(sessionUserId);
            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
        }

        @Test
        @DisplayName("ユーザーが見つかった場合")
        public void testUserFound() throws Exception {
            testUser.setId(pathUserId);

            Users testUser3 = new Users();
            Users testUser4 = new Users();
            Users testUser5 = new Users();
            Meetings testMeeting1 = new Meetings();
            Meetings testMeeting2 = new Meetings();

            testUser3.setId(3);
            testUser4.setId(4);
            testUser5.setId(5);
            testMeeting1.setId(1);
            testMeeting1.setHost(testUser);
            testMeeting2.setId(2);

            List<Users> followsList = new ArrayList<>();
            List<Users> followersList = new ArrayList<>();
            List<Meetings> meetingList = new ArrayList<>();
            List<Users> myFollowsList = new ArrayList<>();
            List<Meetings> myJoinMeetingList = new ArrayList<>();

            followsList.add(testUser3);
            followersList.add(testUser4);
            meetingList.add(testMeeting1);
            myFollowsList.add(testUser5);
            myJoinMeetingList.add(testMeeting2);

            when(usersRepository.findById(pathUserId)).thenReturn(Optional.of(testUser));
            when(followService.getFollowsList(pathUserId)).thenReturn(followsList);
            when(followService.getFollowersList(pathUserId)).thenReturn(followersList);
            when(meetingService.getUserMeetingList(testUser)).thenReturn(meetingList);
            when(followService.getFollowsList(sessionUserId)).thenReturn(myFollowsList);
            when(joinService.getJoinMeetingList(sessionUserId)).thenReturn(myJoinMeetingList);

            mockMvc.perform(get(path).sessionAttr("user_id", sessionUserId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("userDetail"))
                    .andExpect(request().sessionAttribute("path", path))
                    .andExpect(model().attribute("user", testUser))
                    .andExpect(model().attribute("followsList", followsList))
                    .andExpect(model().attribute("followersList", followersList))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("meetingList", meetingList))
                    .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                    .andExpect(model().attribute("searchData", new SearchData()));

            verify(followService, times(1)).getFollowsList(pathUserId);
            verify(followService, times(1)).getFollowersList(pathUserId);
            verify(meetingService, times(1)).getUserMeetingList(testUser);
            verify(followService, times(1)).getFollowsList(sessionUserId);
            verify(joinService, times(1)).getJoinMeetingList(sessionUserId);
            verify(usersRepository, never()).findById(sessionUserId);
        }
    }

    @Test
    @DisplayName("[showMyPageメソッドのテスト]")
    public void testShowMyPage() throws Exception {
        String path = "/User/mypage";

        int testUserId = 1;
        Users testUser = new Users();
        testUser.setId(testUserId);

        Users testUser3 = new Users();
        Users testUser4 = new Users();
        Meetings testMeeting1 = new Meetings();
        Meetings testMeeting2 = new Meetings();

        testUser3.setId(3);
        testUser4.setId(4);
        testMeeting1.setId(1);
        testMeeting1.setHost(testUser);
        testMeeting2.setId(2);

        List<Users> followsList = new ArrayList<>();
        List<Users> followersList = new ArrayList<>();
        List<Meetings> meetingList = new ArrayList<>();
        List<Meetings> myJoinMeetingList = new ArrayList<>();

        followsList.add(testUser3);
        followersList.add(testUser4);
        meetingList.add(testMeeting1);
        myJoinMeetingList.add(testMeeting2);

        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(followService.getFollowsList(testUserId)).thenReturn(followsList);
        when(followService.getFollowersList(testUserId)).thenReturn(followersList);
        when(meetingService.getUserMeetingList(testUser)).thenReturn(meetingList);
        when(joinService.getJoinMeetingList(testUserId)).thenReturn(myJoinMeetingList);

        mockMvc.perform(get(path).sessionAttr("user_id", testUserId))
                .andExpect(status().isOk())
                .andExpect(view().name("userDetail"))
                .andExpect(request().sessionAttribute("path", path))
                .andExpect(model().attribute("user", testUser))
                .andExpect(model().attribute("followsList", followsList))
                .andExpect(model().attribute("followersList", followersList))
                .andExpect(model().attribute("myFollowsList", followsList))
                .andExpect(model().attribute("meetingList", meetingList))
                .andExpect(model().attribute("myJoinMeetingList", myJoinMeetingList))
                .andExpect(model().attribute("searchData", new SearchData()));

        verify(usersRepository, times(1)).findById(testUserId);
        verify(followService, times(1)).getFollowsList(testUserId);
        verify(followService, times(1)).getFollowersList(testUserId);
        verify(meetingService, times(1)).getUserMeetingList(testUser);
        verify(followService, times(1)).getFollowsList(testUserId);
        verify(joinService, times(1)).getJoinMeetingList(testUserId);
    }

    @Test
    @DisplayName("[deleteUserメソッドのテスト]")
    public void testDeleteUser() throws Exception {
        ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);

        int testUserId = 1;
        Users testUser = new Users();
        testUser.setId(testUserId);
        testUser.setLanguage("ja");

        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/User/delete").sessionAttr("user_id", testUserId))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/home"))
                .andExpect(request().sessionAttributeDoesNotExist("user_id"))
                .andExpect(flash().attributeExists("msg"));

        verify(usersRepository, times(1)).findById(testUserId);
        verify(followsRepository, times(1)).deleteByFollowerId(testUserId);
        verify(followsRepository, times(1)).deleteByFolloweeId(testUserId);
        verify(deliveriesRepository, times(1)).deleteByUserId(testUserId);
        verify(meetingsRepository, times(1)).deleteByHost(testUser);
        verify(usersRepository, times(1)).deleteById(testUserId);
        verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
        Locale capturedLocale = localeCaptor.getValue();
        assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
    }

    @Test
    @DisplayName("[updateProfileFormメソッドのテスト]")
    public void testUpdateProfileForm() throws Exception {
        String path = "/User/updateForm";

        int testUserId = 1;
        Users testUser = new Users();
        testUser.setId(testUserId);
        testUser.setEmail("test@email");
        testUser.setLanguage("ja");

        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get(path).sessionAttr("user_id", testUserId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(request().sessionAttribute("path", path))
                .andExpect(request().sessionAttribute("mode", "profile"))
                .andExpect(model().attribute("profileData", new ProfileData(testUser)))
                .andExpect(model().attribute("searchData", new SearchData()))
                .andExpect(model().attribute("emailData", new EmailData(testUser.getEmail())));

        verify(usersRepository, times(1)).findById(testUserId);
    }

    @Nested
    @DisplayName("[deleteProfileImageメソッドのテスト]")
    public class NestedTestDeleteProfileImage {

        private final int testUserId = 1;
        private Users testUser;

        @BeforeEach
        public void init() {
            testUser = new Users();
            testUser.setId(testUserId);
        }

        @Test
        @DisplayName("プロフィール画像が設定されている場合")
        public void testProfileImageIsExists() throws Exception {
            String profileImage = "testImageUrl";

            testUser.setProfileImage(profileImage);

            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/User/profileImage/delete").sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/User/updateForm"));

            verify(usersRepository, times(1)).findById(testUserId);
            verify(storageService, times(1)).deleteFile(profileImage);
            verify(usersRepository, times(1)).saveAndFlush(userCaptor.capture());
            Users capturedUser = userCaptor.getValue();
            testUser.setProfileImage(null);
            assertThat(capturedUser).isEqualTo(testUser);
        }

        @Test
        @DisplayName("プロフィール画像が設定されていない場合")
        public void testProfileImageIsNotExist() throws Exception {
            when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/User/profileImage/delete").sessionAttr("user_id", testUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/User/updateForm"));

            verify(usersRepository, times(1)).findById(testUserId);
            verify(storageService, never()).deleteFile(any());
            verify(usersRepository, times(1)).saveAndFlush(userCaptor.capture());
            Users capturedUser = userCaptor.getValue();
            assertThat(capturedUser).isEqualTo(testUser);
        }
    }

    @Test
    @DisplayName("[showUserRankingメソッドのテスト]")
    public void testShowUserRanking() throws Exception {

        int testUserId = 1;
        String path = "/User/list/ranking";

        List<Users> userList = new ArrayList<>();
        Users testUser2 = new Users();
        testUser2.setId(2);
        testUser2.setContinuousDays(30);
        userList.add(testUser2);

        Map<Integer, Integer> rankingMap = new TreeMap<>();
        rankingMap.put(30, 1);

        List<Users> myFollowsList = new ArrayList<>();
        Users testUser3 = new Users();
        testUser3.setId(3);
        myFollowsList.add(testUser3);

        when(usersRepository.findAll()).thenReturn(userList);
        when(userService.makeRankingMap(userList)).thenReturn(rankingMap);
        when(followService.getFollowsList(testUserId)).thenReturn(myFollowsList);

        mockMvc.perform(get(path).sessionAttr("user_id", testUserId))
                .andExpect(status().isOk())
                .andExpect(view().name("userRanking"))
                .andExpect(request().sessionAttribute("path", path))
                .andExpect(model().attribute("searchData", new SearchData()))
                .andExpect(model().attribute("userList", userList))
                .andExpect(model().attribute("myFollowsList", myFollowsList))
                .andExpect(model().attribute("rankingMap", rankingMap));

        verify(usersRepository, times(1)).findAll();
        verify(userService, times(1)).makeRankingMap(userList);
        verify(followService, times(1)).getFollowsList(testUserId);
    }

    @Test
    @DisplayName("[showSettingメソッドのテスト]")
    public void testShowSetting() throws Exception {
        String path = "/User/setting";

        mockMvc.perform(get(path).sessionAttr("user_id", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("setting"))
                .andExpect(request().sessionAttribute("path", path))
                .andExpect(model().attribute("searchData", new SearchData()));
    }

    @Nested
    @DisplayName("[updateEmailメソッドのテスト]")
    public class NestedTestUpdateEmail {

        private static final String TEST_EMAIL = "test@email";
        private static final String TEST_TOKEN = "test@email";
        private static final String PATH = "/updateEmail/email/" + TEST_EMAIL + "/token/" + TEST_TOKEN;
        private final Locale locale = new Locale("ja");
        private final ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);

        @Test
        @DisplayName("isValid == false")
        public void isInvalid() throws Exception {
            when(temporaryService.isValid(TEST_EMAIL, TEST_TOKEN)).thenReturn(false);

            mockMvc.perform(get(PATH).locale(locale))
                    .andExpect(status().isFound())
                    .andExpect(flash().attributeExists("msg"));

            verify(temporaryService, times(1)).isValid(TEST_EMAIL, TEST_TOKEN);
            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }

        @Test
        @DisplayName("isValid == true")
        public void isValid() throws Exception {

            Users testUser = new Users();
            testUser.setId(1);

            Temporaries temporary = new Temporaries(testUser, TEST_TOKEN);
            List<Temporaries> temporaryList = new ArrayList<>();
            temporaryList.add(temporary);


            when(temporaryService.isValid(TEST_EMAIL, TEST_TOKEN)).thenReturn(true);
            when(temporariesRepository.findByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(temporaryList);
            when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

            mockMvc.perform(get(PATH).locale(locale))
                    .andExpect(status().isFound())
                    .andExpect(flash().attributeExists("msg"));

            verify(temporaryService, times(1)).isValid(TEST_EMAIL, TEST_TOKEN);
            verify(temporariesRepository, times(1)).findByEmailOrderByCreatedAtDesc(TEST_EMAIL);
            verify(usersRepository, times(1)).findById(testUser.getId());

            verify(usersRepository, times(1)).saveAndFlush(userCaptor.capture());
            Users capturedUser = userCaptor.getValue();
            testUser.setEmail(TEST_EMAIL);
            assertThat(capturedUser).isEqualTo(testUser);

            verify(temporariesRepository, times(1)).deleteAll(temporaryListCaptor.capture());
            List<Temporaries> capturedTemporaryList = temporaryListCaptor.getValue();
            assertThat(capturedTemporaryList).isEqualTo(temporaryList);

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }
    }

}