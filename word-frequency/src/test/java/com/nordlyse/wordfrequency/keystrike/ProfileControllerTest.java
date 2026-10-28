package com.nordlyse.wordfrequency.keystrike;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

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

import com.nordlyse.wordfrequency.account.AccountController;
import com.nordlyse.wordfrequency.account.AccountService;
import com.nordlyse.wordfrequency.account.AccountStore;
import com.nordlyse.wordfrequency.account.AccountWebConfiguration;
import com.nordlyse.wordfrequency.account.MemoryAccountStore;
import com.nordlyse.wordfrequency.account.PasswordHasher;
import com.nordlyse.wordfrequency.account.SentLineController;
import com.nordlyse.wordfrequency.account.SessionController;

@WebMvcTest(controllers = {
        ProfileController.class,
        AccountController.class,
        SessionController.class,
        SentLineController.class,
        KeystrikeController.class
})
@Import({
        PaceProfileReader.class,
        AccountService.class,
        AccountWebConfiguration.class,
        KeystrikeAnalysis.class,
        KeystrikeConfiguration.class,
        ProfileControllerTest.MemoryStoreConfiguration.class
})
@TestPropertySource(properties = {"keystrike.max-order=10", "keystrike.profile=local"})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void refusesAProfileReadWithoutAToken() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsTheStatisticsPageToReadTheProfile() throws Exception {
        mockMvc.perform(options("/profile")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    @Test
    void returnsOnlyTheSignedInAccountsWordCrossing() throws Exception {
        String ada = open("Ada");
        type(ada, "cat nap");
        mockMvc.perform(post("/lines")
                        .header("Authorization", "Bearer " + ada)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"en","text":"cat nap"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/profile").header("Authorization", "Bearer " + ada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.words[0].from").value("t"))
                .andExpect(jsonPath("$.words[0].to").value("n"))
                .andExpect(jsonPath("$.words[0].seenCount").value(1))
                .andExpect(jsonPath("$.words[0].meanMillis", closeTo(200.0, 0.001)))
                .andExpect(jsonPath("$.words[0].words[0].fromWord").value("cat"))
                .andExpect(jsonPath("$.words[0].words[0].toWord").value("nap"))
                .andExpect(jsonPath("$.letters[?(@.from == 'c' && @.to == 'a')]").exists());

        String bee = open("Bee");
        type(bee, "dog run");
        mockMvc.perform(post("/lines")
                        .header("Authorization", "Bearer " + bee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"en","text":"dog run"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/profile").header("Authorization", "Bearer " + bee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.words.length()").value(1))
                .andExpect(jsonPath("$.words[0].from").value("g"))
                .andExpect(jsonPath("$.words[0].to").value("r"))
                .andExpect(jsonPath("$.words[0].words[0].fromWord").value("dog"))
                .andExpect(jsonPath("$.words[0].words[0].toWord").value("run"));

        mockMvc.perform(get("/profile").header("Authorization", "Bearer " + ada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.words.length()").value(1))
                .andExpect(jsonPath("$.words[0].words[0].fromWord").value("cat"))
                .andExpect(jsonPath("$.words[0].words[0].toWord").value("nap"));
    }

    private String open(String username) throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct horse\"}"))
                .andExpect(status().isCreated());
        MvcResult opened = mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct horse\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(opened.getResponse().getContentAsString(), "$.token");
    }

    private void type(String token, String text) throws Exception {
        Instant start = Instant.parse("2026-10-28T18:00:00Z");
        int index = 0;
        for (int offset = 0; offset < text.length();) {
            int next = text.offsetByCodePoints(offset, 1);
            String symbol = text.substring(offset, next);
            mockMvc.perform(post("/keystrikes")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"symbol\":\"" + symbol + "\",\"pressedAt\":\""
                                    + start.plusMillis(100L * index) + "\",\"correction\":false}"))
                    .andExpect(status().isOk());
            offset = next;
            index++;
        }
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
