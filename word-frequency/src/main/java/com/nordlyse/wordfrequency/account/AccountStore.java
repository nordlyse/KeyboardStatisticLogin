package com.nordlyse.wordfrequency.account;

import java.util.List;
import java.util.Optional;

public interface AccountStore {

    Optional<AccountRecord> findByUsername(String username);

    List<AccountSummary> listAccounts();

    void insertAccount(String username, String passwordHash);

    void removeAccount(String username);

    void insertSession(String tokenHash, String username);

    Optional<String> usernameForToken(String tokenHash);

    void deleteSession(String tokenHash);

    List<SentLine> lines(String username);

    void insertLine(String username, String language, String text);

    record AccountRecord(String username, String passwordHash) {
    }
}
