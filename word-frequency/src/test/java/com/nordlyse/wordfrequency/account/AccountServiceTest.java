package com.nordlyse.wordfrequency.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AccountServiceTest {

    private final MemoryAccountStore store = new MemoryAccountStore();
    private final AccountService accounts = new AccountService(store, new PasswordHasher(1_000));

    @Test
    void keepsLinesForTheAccountThatSentThem() {
        accounts.addAccount("Ada", "correct horse");
        SessionOpened session = accounts.openSession("ada", "correct horse");

        accounts.addAccount("Bea", "another secret");
        accounts.addLine(session.username(), "en", "silver notebook");
        accounts.addLine("Bea", "tr", "gumus anahtar");

        assertThat(accounts.lines("Ada")).containsExactly(new SentLine("en", "silver notebook"));
        assertThat(accounts.lines("Bea")).containsExactly(new SentLine("tr", "gumus anahtar"));
        assertThat(accounts.usernameFor(session.token())).contains("Ada");

        accounts.closeSession(session.token());
        assertThat(accounts.usernameFor(session.token())).isEmpty();
    }

    @Test
    void rejectsARepeatedUsernameAndAWrongPassword() {
        accounts.addAccount("Ada", "correct horse");

        assertThatThrownBy(() -> accounts.addAccount("ada", "another secret"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThatThrownBy(() -> accounts.openSession("Ada", "wrong password"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
