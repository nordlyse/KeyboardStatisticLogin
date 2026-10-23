package com.nordlyse.wordfrequency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.nordlyse.wordfrequency.keystrike.KeystrikeRequest;
import com.nordlyse.wordfrequency.keystrike.KeystrikeAnalysis;
import com.nordlyse.wordfrequency.keystrike.PgVectorDatabase;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestPropertySource(properties = "keystrike.profile=application-test")
class WordFrequencyApplicationTest {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PgVectorDatabase.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", PgVectorDatabase.POSTGRES::getUsername);
        registry.add("spring.datasource.password", PgVectorDatabase.POSTGRES::getPassword);
    }

    @Autowired
    private KeystrikeAnalysis analysis;

    @Test
    void startsAndRecordsAKeystrike() {
        var rows = analysis.addKeystrike(new KeystrikeRequest("q", Instant.EPOCH, false));

        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.orderSize()).isEqualTo(1);
            assertThat(row.sequence()).isEqualTo("q");
            assertThat(row.seenCount()).isEqualTo(1);
        });
    }
}
