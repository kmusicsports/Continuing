package com.example.continuing.controller;

import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        public void failed() throws Exception {

            mockMvc.perform(get("/User/follow/" + followeeId).sessionAttrs(sessionAttributes))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/home"))
                    .andExpect(flash().attributeExists("msg"));

            verify(followsRepository, never()).saveAndFlush(any());
            verify(followsRepository, never()).saveAndFlush(any());

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(new Locale(follower.getLanguage()));
        }
    }
}