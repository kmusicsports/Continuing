package com.example.continuing.controller;

import com.example.continuing.entity.Users;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.TemporariesRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.LoginService;
import com.example.continuing.service.TemporaryService;
import com.example.continuing.service.UserService;
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

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private LoginService loginService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private DeliveriesRepository deliveriesRepository;

    @MockBean
    private TemporariesRepository temporariesRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private TemporaryService temporaryService;

    @Autowired
    private LoginController loginController;

    @Captor
    ArgumentCaptor<Locale> localeCaptor;

    @Test
    @DisplayName("[showLoginメソッドのテスト]")
    public void testShowLogin() throws Exception {

        mockMvc.perform(get("/showLogin"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginData", new LoginData()))
                .andExpect(model().attribute("searchData", new SearchData()))
                .andExpect(model().attribute("emailData", new EmailData()));
    }

    @Nested
    @DisplayName("[loginメソッドのテスト]")
    public class NestedTestLogin {

        private LoginData loginData;
        private final Locale locale = new Locale("ja");

        @BeforeEach
        public void init() {
            loginData = new LoginData();
        }

        @Test
        @DisplayName("result.hasErrors() == true")
        public void resultHasErrors() throws Exception {
            loginData.setEmail("");

            mockMvc.perform(post("/login")
                            .flashAttr("loginData", loginData)
                            .locale(locale)
                    )
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/showLogin"))
                    .andExpect(flash().attributeExists("msg"));

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }

        @Test
        @DisplayName("result.hasErrors() == false && isValid == false")
        public void isInvalid() throws Exception {

            loginData.setEmail("test@email");
            loginData.setPassword("testPassword");

            when(loginService.isValid(any(), any())).thenReturn(false);

            mockMvc.perform(post("/login")
                            .flashAttr("loginData", loginData)
                            .locale(locale)
                    )
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/showLogin"))
                    .andExpect(flash().attributeExists("msg"));

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }

        @Nested
        @DisplayName("result.hasErrors() == false && isValid == true")
        public class NestedTestIsValid {

            private Users testUser;

            @BeforeEach
            public void init() {
                String testEmail = "test@email";

                loginData.setEmail(testEmail);
                loginData.setPassword("testPassword");

                testUser = new Users();
                testUser.setId(1);
                testUser.setEmail(testEmail);
                testUser.setName("testUserName");
                testUser.setLanguage("en");

                when(loginService.isValid(any(), any())).thenReturn(true);
                when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            }

            @Test
            @DisplayName("session.getAttribute('path') == null")
            public void sessionPathIsNull() throws Exception {

                mockMvc.perform(post("/login")
                                .flashAttr("loginData", loginData)
                                .locale(locale)
                        )
                        .andExpect(status().isFound())
                        .andExpect(redirectedUrl("/home"))
                        .andExpect(request().sessionAttribute("user_id", testUser.getId()))
                        .andExpect(request().sessionAttribute("user_name", testUser.getName()))
                        .andExpect(flash().attributeExists("msg"));

                verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
                Locale capturedLocale = localeCaptor.getValue();
                assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
            }

            @Test
            @DisplayName("session.getAttribute('path') != null")
            public void sessionPathIsNotNull() throws Exception {
                String path = "/showLogin";

                mockMvc.perform(post("/login")
                                .flashAttr("loginData", loginData)
                                .sessionAttr("path", path)
                                .locale(locale)
                        )
                        .andExpect(status().isFound())
                        .andExpect(redirectedUrl(path))
                        .andExpect(request().sessionAttribute("user_id", testUser.getId()))
                        .andExpect(request().sessionAttribute("user_name", testUser.getName()))
                        .andExpect(flash().attributeExists("msg"));

                verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
                Locale capturedLocale = localeCaptor.getValue();
                assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
            }
        }
    }

    
}