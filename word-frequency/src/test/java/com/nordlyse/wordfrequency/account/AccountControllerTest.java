package com.nordlyse.wordfrequency.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;

import com.nordlyse.wordfrequency.keystrike.KeystrikeAnalysis;
import com.nordlyse.wordfrequency.keystrike.KeystrikeSettings;
import com.nordlyse.wordfrequency.keystrike.MemoryNgramTransitionStore;
import com.nordlyse.wordfrequency.keystrike.NgramTransitionStore;

@WebMvcTest(controllers = {AccountController.class, SessionController.class, SentLineController.class})
@Import({
        AccountService.class,
        AccountRemoval.class,
        AccountWebConfiguration.class,
        KeystrikeAnalysis.class,
        AccountControllerTest.MemoryStoreConfiguration.class
})
@TestPropertySource(properties = {"keystrike.max-order=10", "keystrike.profile=local"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registersThenKeepsLinesForTheNextVisit() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Ada","password":"correct horse"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Ada"));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ada","password":"another secret"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Ada","password":"wrong password"}
                                """))
                .andExpect(status().isUnauthorized());

        MvcResult opened = mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ada","password":"correct horse"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Ada"))
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(opened.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get("/lines"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/lines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"en","text":"silver notebook"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/lines").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].language").value("en"))
                .andExpect(jsonPath("$[0].text").value("silver notebook"));

        mockMvc.perform(delete("/sessions").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/lines").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsEveryAccountWithoutThePassword() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());

        register("Bee");
        MvcResult opened = mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Bee","password":"correct horse"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(opened.getResponse().getContentAsString(), "$.token");
        register("Ada");

        mockMvc.perform(get("/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("Ada"))
                .andExpect(jsonPath("$[0].username").value("Ada"))
                .andExpect(jsonPath("$[0].addedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[1].id").value("Bee"))
                .andExpect(jsonPath("$[1].username").value("Bee"));
    }

    @Test
    void removesTheNamedAccountAndLeavesTheOther() throws Exception {
        register("Ada");
        register("Bee");
        String ada = token("Ada");
        String bee = token("Bee");
        mockMvc.perform(post("/lines")
                        .header("Authorization", "Bearer " + bee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"en","text":"dog run"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/accounts/bee").header("Authorization", "Bearer " + ada))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts").header("Authorization", "Bearer " + ada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("Ada"));
        mockMvc.perform(get("/lines").header("Authorization", "Bearer " + bee))
                .andExpect(status().isUnauthorized());

        register("Bee");
        mockMvc.perform(get("/lines").header("Authorization", "Bearer " + token("Bee")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void refusesToRemoveAnUnknownAccount() throws Exception {
        register("Ada");

        mockMvc.perform(delete("/accounts/missing").header("Authorization", "Bearer " + token("Ada")))
                .andExpect(status().isNotFound());
    }

    @Test
    void refusesToRemoveAUsernameOutsideTheAllowedShape() throws Exception {
        register("Ada");

        mockMvc.perform(delete("/accounts/Ada!").header("Authorization", "Bearer " + token("Ada")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refusesToRemoveAnAccountWithoutAToken() throws Exception {
        mockMvc.perform(delete("/accounts/Ada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsTheAccountPageToRegister() throws Exception {
        mockMvc.perform(options("/accounts")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));

        mockMvc.perform(options("/accounts")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));

        mockMvc.perform(options("/accounts/Ada")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct horse\"}"))
                .andExpect(status().isCreated());
    }

    private String token(String username) throws Exception {
        MvcResult opened = mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct horse\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(opened.getResponse().getContentAsString(), "$.token");
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
        NgramTransitionStore ngramTransitionStore() {
            return new MemoryNgramTransitionStore();
        }

        @Bean
        KeystrikeSettings keystrikeSettings() {
            return new KeystrikeSettings(10, "local");
        }

        @Bean
        java.time.Clock keystrikeClock() {
            return java.time.Clock.systemUTC();
        }
    }
}
