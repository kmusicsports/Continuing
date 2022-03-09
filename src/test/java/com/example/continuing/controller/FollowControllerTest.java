package com.example.continuing.controller;

import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowsRepository followsRepository;

    @MockBean
    private FollowService followService;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private MessageSource messageSource;

    @Autowired
    FollowController followController;

    @Captor
    private ArgumentCaptor<Locale> localeCaptor;

    @Captor
    private ArgumentCaptor<Follows> followsCaptor;

    @Captor
    private ArgumentCaptor<Users> followerCaptor;

    @Captor
    private ArgumentCaptor<Users> followeeCaptor;

    @Captor
    private ArgumentCaptor<List<Follows>> followsListCaptor;

    @Nested
    @DisplayName("[followメソッドのテスト]")
    public class NestedTestFollow {

        private final int followerId = 1;
        private final int followeeId = 2;
        private Users follower;
        private Users followee;
        private Map<String, Object> sessionAttributes;
        private static final String CURRENT_URL = "/";

        @BeforeEach
        public void init() {
            follower = new Users();
            followee = new Users();
            follower.setId(followerId);
            followee.setId(followeeId);
            follower.setLanguage("en");
            followee.setLanguage("ja");

            sessionAttributes = new HashMap<>();
            sessionAttributes.put("user_id", followerId);
            sessionAttributes.put("path", CURRENT_URL);

            when(usersRepository.findById(followerId)).thenReturn(Optional.of(follower));
        }

        @Test
        @DisplayName("正常系")
        public void success() throws Exception {
            when(usersRepository.findById(followeeId)).thenReturn(Optional.of(followee));

            mockMvc.perform(get("/User/follow/" + followeeId).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(CURRENT_URL));

            verify(followsRepository, times(1)).saveAndFlush(followsCaptor.capture());
            Follows capturedFollows = followsCaptor.getValue();
            assertThat(capturedFollows.getFollowerId()).isEqualTo(followerId);
            assertThat(capturedFollows.getFolloweeId()).isEqualTo(followeeId);

            verify(followService, times(1)).sendMail(followeeCaptor.capture(), followerCaptor.capture(), localeCaptor.capture());
            Users capturedFollowee = followeeCaptor.getValue();
            Users capturedFollower = followerCaptor.getValue();
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedFollowee).isEqualTo(followee);
            assertThat(capturedFollower).isEqualTo(follower);
            assertThat(capturedLocale).isEqualTo(new Locale(followee.getLanguage()));

            verify(messageSource, never()).getMessage(any(), any(), any());
        }

        @Test
        @DisplayName("異常系")
        public void fail() throws Exception {

            mockMvc.perform(get("/User/follow/" + followeeId).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(followsRepository, never()).saveAndFlush(any());
            verify(followService, never()).sendMail(any(), any(), any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(follower.getLanguage()));
        }
    }

    @Nested
    @DisplayName("[unfollowメソッドのテスト]")
    public class NestedTestUnfollow {

        private final int followerId = 1;
        private final int followeeId = 2;
        private Users follower;
        private Users followee;
        private Map<String, Object> sessionAttributes;
        private static final String CURRENT_URL = "/";

        @BeforeEach
        public void init() {
            follower = new Users();
            followee = new Users();
            follower.setId(followerId);
            followee.setId(followeeId);
            follower.setLanguage("en");
            followee.setLanguage("ja");

            sessionAttributes = new HashMap<>();
            sessionAttributes.put("user_id", followerId);
            sessionAttributes.put("path", CURRENT_URL);
        }

        @Test
        @DisplayName("正常系")
        public void success() throws Exception {
            List<Follows> testFollowsList = new ArrayList<>();
            testFollowsList.add(new Follows(followerId, followeeId));

            when(usersRepository.findById(followeeId)).thenReturn(Optional.of(followee));
            when(followsRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(testFollowsList);

            mockMvc.perform(get("/User/unfollow/" + followeeId).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(CURRENT_URL));

            verify(followsRepository, times(1)).findByFollowerIdAndFolloweeId(followerId, followeeId);

            verify(followsRepository, times(1)).deleteAll(followsListCaptor.capture());
            List<Follows> capturedFollowsList = followsListCaptor.getValue();
            assertThat(capturedFollowsList).isEqualTo(testFollowsList);

            verify(messageSource, never()).getMessage(any(), any(), any());
        }

        @Test
        @DisplayName("異常系")
        public void fail() throws Exception {
            when(usersRepository.findById(followerId)).thenReturn(Optional.of(follower));

            mockMvc.perform(get("/User/unfollow/" + followeeId).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(followsRepository, never()).findByFollowerIdAndFolloweeId(any(), any());
            verify(followsRepository, never()).deleteAll(any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(follower.getLanguage()));
        }
    }

    @Nested
    @DisplayName("[showUserFollowsメソッドのテスト]")
    public class NestedTestShowUserFollows {

        private final int operationUserId = 1;
        private final int showUserId = 2;
        private final String path = "/User/" + showUserId + "/list/follows";
        private Users users;

        @BeforeEach
        public void init() {
            users = new Users();
        }

        @Test
        @DisplayName("正常系")
        public void success() throws Exception {
            users.setId(showUserId);

            List<Users> followsList = new ArrayList<>();
            List<Users> followersList = new ArrayList<>();
            List<Users> myFollowsList = new ArrayList<>();
            followersList.add(new Users());
            myFollowsList.add(users);

            when(usersRepository.findById(showUserId)).thenReturn(Optional.of(users));
            when(followService.getFollowsList(showUserId)).thenReturn(followsList);
            when(followService.getFollowersList(showUserId)).thenReturn(followersList);
            when(followService.getFollowsList(operationUserId)).thenReturn(myFollowsList);

            mockMvc.perform(get(path).sessionAttr("user_id", operationUserId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("follows"))
                    .andExpect(request().sessionAttribute("path", path))
                    .andExpect(model().attribute("followsList", followsList))
                    .andExpect(model().attribute("followersList", followersList))
                    .andExpect(model().attribute("myFollowsList", myFollowsList))
                    .andExpect(model().attribute("searchData", new SearchData()));

            verify(usersRepository, times(1)).findById(showUserId);
            verify(usersRepository, never()).findById(operationUserId);
            verify(followService, times(1)).getFollowsList(showUserId);
            verify(followService, times(1)).getFollowersList(showUserId);
            verify(followService, times(1)).getFollowsList(operationUserId);
        }

        @Test
        @DisplayName("異常系")
        public void fail() throws Exception {
            
            users.setId(operationUserId);
            users.setLanguage("ja");

            when(usersRepository.findById(operationUserId)).thenReturn(Optional.of(users));

            mockMvc.perform(get(path).sessionAttr("user_id", operationUserId))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(usersRepository, times(1)).findById(showUserId);
            verify(usersRepository, times(1)).findById(operationUserId);
            verify(followService, never()).getFollowsList(showUserId);
            verify(followService, never()).getFollowersList(showUserId);
            verify(followService, never()).getFollowsList(operationUserId);

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(users.getLanguage()));
        }
    }
}