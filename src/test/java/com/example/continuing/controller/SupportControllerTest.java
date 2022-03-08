package com.example.continuing.controller;

import com.example.continuing.form.ContactData;
import com.example.continuing.form.SearchData;
import com.example.continuing.service.UserService;
import org.junit.Before;
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

import java.util.Locale;

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
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageSource messageSource;

    @Autowired
    private SupportController supportController;

    @Captor
    private ArgumentCaptor<Locale> localeCaptor;

    @Test
    @DisplayName("[showHelpメソッドのテスト]")
    public void testShowHelp() throws Exception {
        String path = "/help";

        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name("help"))
                .andExpect(request().sessionAttribute("path", path))
                .andExpect(model().attribute("searchData", new SearchData()));
    }

    @Test
    @DisplayName("[showContactFormメソッドのテスト]")
    public void testShowContactForm() throws Exception {
        String path = "/contactForm";

        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name("contactForm"))
                .andExpect(request().sessionAttribute("path", path))
                .andExpect(model().attribute("searchData", new SearchData()))
                .andExpect(model().attribute("contactData", new ContactData()));
    }

    @Nested
    @DisplayName("[contactメソッドのテスト]")
    public class NestedTestContact {

        private ContactData contactData = new ContactData();

        @Test
        @DisplayName("バリデーションエラーなし")
        public void resultHasNoErrors() throws Exception {

            contactData.setEmail("test@email");
            contactData.setContents("testContents");

            mockMvc.perform(post("/contact").flashAttr("contactData", contactData))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/contactForm"))
                    .andExpect(redirectedUrl("/contactForm"))
                    .andExpect(model().hasNoErrors())
                    .andExpect(flash().attributeExists("msg"));

            verify(userService, times(1)).sendContactEmail(contactData);
            verify(messageSource, times(1)).getMessage(any(), any(), any());
        }

        @Test
        @DisplayName("バリデーションエラーあり")
        public void resultHasErrors() throws Exception {

            contactData.setEmail("");
            contactData.setContents("");

            mockMvc.perform(post("/contact").flashAttr("contactData", contactData))
                    .andExpect(status().isOk())
                    .andExpect(view().name("contactForm"))
                    .andExpect(request().sessionAttribute("path", "/contactForm"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attribute("searchData", new SearchData()))
                    .andExpect(model().attributeExists("msg"));

            verify(messageSource, atLeastOnce()).getMessage(any(), any(), any());
        }
    }
}