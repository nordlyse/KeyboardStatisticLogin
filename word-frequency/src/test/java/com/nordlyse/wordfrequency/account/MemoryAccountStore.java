package com.nordlyse.wordfrequency.account;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoryAccountStore implements AccountStore {

    private final Map<String, StoredAccount> accounts = new LinkedHashMap<>();
    private final Map<String, String> sessions = new LinkedHashMap<>();
    private final List<StoredLine> lines = new ArrayList<>();

    @Override
    public Optional<AccountRecord> findByUsername(String username) {
        return accounts.values().stream()
                .filter(account -> account.username().equalsIgnoreCase(username))
                .findFirst()
                .map(account -> new AccountRecord(account.username(), account.passwordHash()));
    }

    @Override
    public List<AccountSummary> listAccounts() {
        return accounts.values().stream()
                .sorted(Comparator.comparing(StoredAccount::username, String.CASE_INSENSITIVE_ORDER))
                .map(account -> new AccountSummary(account.username(), account.username(), account.addedAt()))
                .toList();
    }

    @Override
    public void insertAccount(String username, String passwordHash) {
        accounts.put(username, new StoredAccount(username, passwordHash, Instant.now()));
    }

    @Override
    public void insertSession(String tokenHash, String username) {
        sessions.put(tokenHash, username);
    }

    @Override
    public Optional<String> usernameForToken(String tokenHash) {
        return Optional.ofNullable(sessions.get(tokenHash));
    }

    @Override
    public void deleteSession(String tokenHash) {
        sessions.remove(tokenHash);
    }

    @Override
    public List<SentLine> lines(String username) {
        return lines.stream()
                .filter(line -> line.username().equals(username))
                .map(line -> new SentLine(line.language(), line.text()))
                .toList();
    }

    @Override
    public void insertLine(String username, String language, String text) {
        lines.add(new StoredLine(username, language, text));
    }

    private record StoredAccount(String username, String passwordHash, Instant addedAt) {
    }

    private record StoredLine(String username, String language, String text) {
    }
}
