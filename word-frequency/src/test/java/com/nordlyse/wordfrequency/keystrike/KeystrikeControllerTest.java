package com.nordlyse.wordfrequency.keystrike;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(controllers = KeystrikeController.class)
@Import({
        KeystrikeAnalysis.class,
        KeystrikeConfiguration.class,
        KeystrikeWebConfiguration.class,
        KeystrikeControllerTest.MemoryStoreConfiguration.class
})
@TestPropertySource(properties = {"keystrike.max-order=10", "keystrike.profile=local"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class KeystrikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postsAKeyAndACorrection() throws Exception {
        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"a","pressedAt":"2026-10-23T19:00:00Z"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderSize").value(1))
                .andExpect(jsonPath("$[0].sequence").value("a"))
                .andExpect(jsonPath("$[0].seenCount").value(1))
                .andExpect(jsonPath("$[0].errorCount").value(0))
                .andExpect(jsonPath("$[0].errorFrequency").value(0.0));

        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"b","pressedAt":"2026-10-23T19:00:01Z"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.orderSize == 2)].sequence").value("ab"))
                .andExpect(jsonPath("$[?(@.orderSize == 2)].stepMillis[0]").value(1000.0));

        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"Backspace"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sequence == 'b')].errorFrequency").value(1.0))
                .andExpect(jsonPath("$[?(@.sequence == 'ab')].errorCount").value(1))
                .andExpect(jsonPath("$[?(@.sequence == 'a')].errorCount").doesNotExist());
    }

    @Test
    void postsAnErroneousCharacterOutsideTheFollowingChain() throws Exception {
        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"j","pressedAt":"2026-10-23T19:00:00Z"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"x","pressedAt":"2026-10-23T19:00:01Z","correction":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sequence == 'jx')].errorFrequency").value(1.0));

        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"k","pressedAt":"2026-10-23T19:00:02Z"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.orderSize == 2)].sequence").value("jk"));
    }

    @Test
    void allowsTheSignInPageToPostAKey() throws Exception {
        mockMvc.perform(options("/keystrikes")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    @Test
    void rejectsASymbolThatIsNotOneCharacter() throws Exception {
        mockMvc.perform(post("/keystrikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symbol":"ab","pressedAt":"2026-10-23T19:00:00Z"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class MemoryStoreConfiguration {

        @Bean
        NgramTransitionStore ngramTransitionStore() {
            return new MemoryNgramTransitionStore();
        }
    }
}
