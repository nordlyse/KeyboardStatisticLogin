package com.nordlyse.wordfrequency.account;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PgAccountStore implements AccountStore {

    private final JdbcClient jdbc;

    @Override
    public Optional<AccountRecord> findByUsername(String username) {
        return jdbc.sql("""
                SELECT username, password_hash
                FROM account
                WHERE lower(username) = lower(:username)
                """)
                .param("username", username)
                .query((rs, rowNumber) -> new AccountRecord(rs.getString("username"), rs.getString("password_hash")))
                .optional();
    }

    @Override
    public List<AccountSummary> listAccounts() {
        return jdbc.sql("""
                SELECT username, added_at
                FROM account
                ORDER BY lower(username), username
                """)
                .query((rs, rowNumber) -> new AccountSummary(
                        rs.getString("username"),
                        rs.getString("username"),
                        rs.getObject("added_at", OffsetDateTime.class).toInstant()))
                .list();
    }

    @Override
    public void insertAccount(String username, String passwordHash) {
        jdbc.sql("""
                INSERT INTO account (username, password_hash)
                VALUES (:username, :passwordHash)
                """)
                .param("username", username)
                .param("passwordHash", passwordHash)
                .update();
    }

    @Override
    public void removeAccount(String username) {
        jdbc.sql("""
                DELETE FROM account_session
                WHERE username = :username
                """)
                .param("username", username)
                .update();
        jdbc.sql("""
                DELETE FROM sent_line
                WHERE username = :username
                """)
                .param("username", username)
                .update();
        jdbc.sql("""
                DELETE FROM account
                WHERE username = :username
                """)
                .param("username", username)
                .update();
    }

    @Override
    public void insertSession(String tokenHash, String username) {
        jdbc.sql("""
                INSERT INTO account_session (token_hash, username)
                VALUES (:tokenHash, :username)
                """)
                .param("tokenHash", tokenHash)
                .param("username", username)
                .update();
    }

    @Override
    public Optional<String> usernameForToken(String tokenHash) {
        return jdbc.sql("""
                SELECT username
                FROM account_session
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .query(String.class)
                .optional();
    }

    @Override
    public void deleteSession(String tokenHash) {
        jdbc.sql("""
                DELETE FROM account_session
                WHERE token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .update();
    }

    @Override
    public List<SentLine> lines(String username) {
        return jdbc.sql("""
                SELECT language, line_text
                FROM sent_line
                WHERE username = :username
                ORDER BY line_id
                """)
                .param("username", username)
                .query((rs, rowNumber) -> new SentLine(rs.getString("language"), rs.getString("line_text")))
                .list();
    }

    @Override
    public void insertLine(String username, String language, String text) {
        jdbc.sql("""
                INSERT INTO sent_line (username, language, line_text)
                VALUES (:username, :language, :text)
                """)
                .param("username", username)
                .param("language", language)
                .param("text", text)
                .update();
    }
}
