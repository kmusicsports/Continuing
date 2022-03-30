package com.example.continuing.controller;

import com.example.continuing.entity.Deliveries;
import com.example.continuing.entity.Temporaries;
import com.example.continuing.entity.Users;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.RegisterData;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.ArrayList;
import java.util.List;
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

    @Captor
    ArgumentCaptor<Temporaries> temporaryCaptor;

    @Captor
    ArgumentCaptor<Users> userCaptor;

    @Captor
    ArgumentCaptor<Deliveries> deliveryCaptor;

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

    @Test
    @DisplayName("[logoutメソッドのテスト]")
    public void testLogout() throws Exception {
        int testUserId = 1;

        Users testUser = new Users();
        testUser.setId(testUserId);
        testUser.setLanguage("ja");

        when(usersRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/User/logout").sessionAttr("user_id", testUserId))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/showLogin"))
                .andExpect(request().sessionAttributeDoesNotExist("user_id"))
                .andExpect(flash().attributeExists("msg"));

        verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
        Locale capturedLocale = localeCaptor.getValue();
        assertThat(capturedLocale).isEqualTo(new Locale(testUser.getLanguage()));
    }

    @Test
    @DisplayName("[showRegisterメソッドのテスト]")
    public void testShowRegister() throws Exception {

        mockMvc.perform(get("/showRegister"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("registerData", new RegisterData()))
                .andExpect(model().attribute("searchData", new SearchData()));
    }

    @Nested
    @DisplayName("[temporarilyRegisterメソッドのテスト]")
    public class NestedTestTemporarilyRegister {

        private RegisterData registerData;
        private final Locale locale = new Locale("ja");

        @BeforeEach
        public void init() {
            registerData = new RegisterData();
        }

        @Test
        @DisplayName("result.hasErrors() == true")
        public void resultHasErrors() throws Exception {

            registerData.setEmail("");
            registerData.setChecked(true);

            mockMvc.perform(post("/regist")
                            .flashAttr("registerData", registerData)
                            .locale(locale)
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attribute("registerData", registerData))
                    .andExpect(model().attributeHasErrors("registerData"))
                    .andExpect(model().attribute("searchData", new SearchData()))
                    .andExpect(model().attributeExists("msg"));

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }

        @Test
        @DisplayName("result.hasErrors() == false && isValid == false")
        public void isInvalid() throws Exception {

            registerData.setEmail("test@email");
            registerData.setPassword("password");
            registerData.setPasswordAgain("password");
            registerData.setName("testName");
            registerData.setChecked(true);

            when(loginService.isValid(any(), any(), any())).thenReturn(false);

            mockMvc.perform(post("/regist")
                            .flashAttr("registerData", registerData)
                            .locale(locale)
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attribute("registerData", registerData))
                    .andExpect(model().attributeHasNoErrors("registerData"))
                    .andExpect(model().attribute("searchData", new SearchData()))
                    .andExpect(model().attributeExists("msg"));

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }

        @Test
        @DisplayName("result.hasErrors() == false && isValid == true")
        public void isValid() throws Exception {

            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            String testToken = "testToken";

            registerData.setEmail("test@email");
            registerData.setPassword("password");
            registerData.setPasswordAgain("password");
            registerData.setName("testName");
            registerData.setChecked(true);

            when(loginService.isValid(any(), any(), any())).thenReturn(true);
            when(loginService.sendMail(any(), any(), any())).thenReturn(testToken);

            mockMvc.perform(post("/regist")
                            .flashAttr("registerData", registerData)
                            .locale(locale)
                    )
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/showLogin"))
                    .andExpect(flash().attributeExists("msg"));

            verify(loginService, times(1)).sendMail(emailCaptor.capture(), any(), any());
            String capturedEmail = emailCaptor.getValue();
            assertThat(capturedEmail).isEqualTo(registerData.getEmail());

            verify(temporariesRepository, times(1)).saveAndFlush(temporaryCaptor.capture());
            Temporaries capturedTemporary = temporaryCaptor.getValue();
            assertThat(capturedTemporary.getEmail()).isEqualTo(registerData.getEmail());
            assertThat(capturedTemporary.getName()).isEqualTo(registerData.getName());
            assertThat(capturedTemporary.getToken()).isEqualTo(testToken);

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }
    }

    @Nested
    @DisplayName("[fullRegisterメソッドのテスト]")
    public class NestedTestFullRegister {

        private static final String TEST_EMAIL = "test@email";
        private static final String TEST_TOKEN = "testToken";
        private final Locale locale = new Locale("ja");

        @Test
        @DisplayName("isValid == false")
        public void isInvalid() throws Exception {

            when(temporaryService.isValid(TEST_EMAIL, TEST_TOKEN)).thenReturn(false);

            mockMvc.perform(get("/register/email/" + TEST_EMAIL + "/token/" + TEST_TOKEN).locale(locale))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/showRegister"))
                    .andExpect(flash().attributeExists("msg"));

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }

        @Test
        @DisplayName("isValid == true")
        public void isValid() throws Exception {
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

            Temporaries testTemporary = new Temporaries();
            testTemporary.setName("testName");
            testTemporary.setEmail(TEST_EMAIL);
            testTemporary.setPassword("testPass");

            List<Temporaries> temporariesList = new ArrayList<>();
            temporariesList.add(testTemporary);

            when(temporaryService.isValid(TEST_EMAIL, TEST_TOKEN)).thenReturn(true);
            when(temporariesRepository.findByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(temporariesList);

            mockMvc.perform(get("/register/email/" + TEST_EMAIL + "/token/" + TEST_TOKEN).locale(locale))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/showLogin"))
                    .andExpect(flash().attributeExists("msg"));

            verify(usersRepository, times(1)).saveAndFlush(userCaptor.capture());
            Users capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getName()).isEqualTo(testTemporary.getName());
            assertThat(capturedUser.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(capturedUser.getPassword()).isEqualTo(testTemporary.getPassword());
            assertThat(capturedUser.getContinuousDays()).isEqualTo(0);
            assertThat(capturedUser.getEmail()).isEqualTo(TEST_EMAIL);

            assertThat(capturedUser.getLanguage()).isEqualTo(locale.getLanguage());

            verify(temporariesRepository, times(1)).deleteAll(temporariesList);

            verify(deliveriesRepository, times(1)).saveAndFlush(deliveryCaptor.capture());
            Deliveries capturedDelivery = deliveryCaptor.getValue();
            assertThat(capturedDelivery.getFollowed()).isEqualTo(1);
            assertThat(capturedDelivery.getMeetingCreated()).isEqualTo(1);
            assertThat(capturedDelivery.getMeetingDeleted()).isEqualTo(1);
            assertThat(capturedDelivery.getMeetingJoined()).isEqualTo(1);
            assertThat(capturedDelivery.getMeetingLeft()).isEqualTo(1);
            assertThat(capturedDelivery.getTodayMeetings()).isEqualTo(1);

            verify(loginService, times(1)).sendMail(emailCaptor.capture(), any(), any());
            String capturedEmail = emailCaptor.getValue();
            assertThat(capturedEmail).isEqualTo(TEST_EMAIL);

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }
    }

    @ParameterizedTest
    @CsvSource({"'th', 'terms/terms'",
            "'ja', 'terms/terms_ja'",
            "'en', 'terms/terms_en'",
    })
    @DisplayName("[showTermsメソッドのテスト]")
    public void testShowTerms(String language, String viewName) throws Exception {
        Locale locale = new Locale(language);

        mockMvc.perform(get("/terms").locale(locale))
                .andExpect(status().isOk())
                .andExpect(view().name(viewName))
                .andExpect(model().size(0));
    }

    @Nested
    @DisplayName("[sendResetPasswordEmailメソッドのテスト]")
    public class NestedTestSendResetPasswordEmail {

        private EmailData emailData;
        private final Locale locale = new Locale("ja");

        @BeforeEach
        public void init() {
            emailData = new EmailData();
        }

        @Test
        @DisplayName("result.hasErrors() == true")
        public void resultHasErrors() throws Exception {
            emailData.setEmail("");

            mockMvc.perform(post("/login/reset-password")
                            .flashAttr("emailData", emailData)
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
        @DisplayName("result.hasErrors() == false && someUser.isPresent() == false")
        public void emailUserNotFound() throws Exception {
            emailData.setEmail("test@email");

            mockMvc.perform(post("/login/reset-password")
                            .flashAttr("emailData", emailData)
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
        @DisplayName("result.hasErrors() == false && someUser.isPresent() == true")
        public void emailUserFound() throws Exception {

            String testEmail = "test@email";
            String testToken = "testToken";

            Users testUser = new Users();
            testUser.setEmail(testEmail);

            emailData.setEmail(testEmail);

            when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(loginService.sendMail(any(), any(), any())).thenReturn(testToken);

            mockMvc.perform(post("/login/reset-password")
                            .flashAttr("emailData", emailData)
                            .locale(locale)
                    )
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/showLogin"))
                    .andExpect(flash().attributeExists("msg"));

            verify(temporariesRepository, times(1)).saveAndFlush(temporaryCaptor.capture());
            Temporaries capturedTemporary = temporaryCaptor.getValue();
            assertThat(capturedTemporary.getEmail()).isEqualTo(testEmail);
            assertThat(capturedTemporary.getToken()).isEqualTo(testToken);

            verify(messageSource, times(1)).getMessage(any(), any(), localeCaptor.capture());
            Locale capturedLocale = localeCaptor.getValue();
            assertThat(capturedLocale).isEqualTo(locale);
        }
    }

}