package com.example.continuing.controller;

import com.example.continuing.form.SearchData;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.service.UserService;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

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

    @Autowired
    private MainController mainController;

    @Nested
    @DisplayName("[showHome(Model model)メソッドのテスト]")
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