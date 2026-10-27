package com.nordlyse.wordfrequency.keystrike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.nordlyse.wordfrequency.account.AccountService;
import com.nordlyse.wordfrequency.account.AccountStore;
import com.nordlyse.wordfrequency.account.AccountWebConfiguration;
import com.nordlyse.wordfrequency.account.MemoryAccountStore;
import com.nordlyse.wordfrequency.account.PasswordHasher;

@WebMvcTest(controllers = {
        com.nordlyse.wordfrequency.account.AccountController.class,
        com.nordlyse.wordfrequency.account.SessionController.class,
        KeystrikeController.class
})
@Import({
        AccountService.class,
        AccountWebConfiguration.class,
        KeystrikeAnalysis.class,
        KeystrikeConfiguration.class,
        KeystrikeAccountGateTest.MemoryStoreConfiguration.class
})
@TestPropertySource(properties = {"keystrike.max-order=10", "keystrike.profile=local"})
class KeystrikeAccountGateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemoryNgramTransitionStore ngrams;

    @Test
    void recordsKeysOnTheSignedInProfile() throws Exception {
        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"a","pressedAt":"2026-10-27T20:00:00Z"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Ada","password":"correct horse"}
                                """))
                .andExpect(status().isCreated());

        MvcResult opened = mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Ada","password":"correct horse"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(opened.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(post("/keystrikes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"a","pressedAt":"2026-10-27T20:00:00Z"}
                                """))
                .andExpect(status().isOk());

        ActiveProfile.use("Ada");
        try {
            assertThat(ngrams.current(1, "a")).isPresent();
        } finally {
            ActiveProfile.clear();
        }
        assertThat(ngrams.current(1, "a")).isEmpty();
    }

    @TestConfiguration
    static class MemoryStoreConfiguration {

        @Bean
        AccountStore accountStore() {
            return new MemoryAccountStore();
        }

        @Bean
        PasswordHasher passwordHasher() {
            return new PasswordHasher(1_000);
        }

        @Bean
        MemoryNgramTransitionStore ngramTransitionStore() {
            return new MemoryNgramTransitionStore();
        }
    }
}
