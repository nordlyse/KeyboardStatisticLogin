package com.nordlyse.wordfrequency.keystrike;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MatchController.class)
@Import({
        KeystrikeMatcher.class,
        KeystrikeAnalysis.class,
        KeystrikeConfiguration.class,
        KeystrikeWebConfiguration.class,
        MatchControllerTest.MemoryStoreConfiguration.class
})
@TestPropertySource(properties = {"keystrike.max-order=10", "keystrike.profile=local"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeystrikeAnalysis analysis;

    @Test
    void reportsThatNoProfileIsStored() throws Exception {
        mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(false))
                .andExpect(jsonPath("$.scored").value(false));
    }

    @Test
    void scoresASentenceWithoutStoringIt() throws Exception {
        analysis.addKeystrike(new KeystrikeRequest("a", Instant.parse("2026-10-26T10:00:00Z"), false));
        analysis.addKeystrike(new KeystrikeRequest("b", Instant.parse("2026-10-26T10:00:00.100Z"), false));

        mockMvc.perform(post("/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keys":[
                                  {"symbol":"a","pressedAt":"2026-10-26T11:00:00Z"},
                                  {"symbol":"b","pressedAt":"2026-10-26T11:00:00.100Z"}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(true))
                .andExpect(jsonPath("$.scored").value(true))
                .andExpect(jsonPath("$.matchPercent").value(100.0))
                .andExpect(jsonPath("$.accepted").value(true));

        mockMvc.perform(post("/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keys":[
                                  {"symbol":"a","pressedAt":"2026-10-26T11:00:00Z"},
                                  {"symbol":"b","pressedAt":"2026-10-26T11:00:00.400Z"}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(false));
    }

    @Test
    void allowsTheSignInPageToScoreASentence() throws Exception {
        mockMvc.perform(options("/matches")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    @TestConfiguration
    static class MemoryStoreConfiguration {

        @Bean
        NgramTransitionStore ngramTransitionStore() {
            return new MemoryNgramTransitionStore();
        }
    }
}
