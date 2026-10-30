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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = {AccountController.class, SessionController.class, SentLineController.class})
@Import({AccountService.class, AccountWebConfiguration.class, AccountControllerTest.MemoryStoreConfiguration.class})
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
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct horse\"}"))
                .andExpect(status().isCreated());
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
    }
}
