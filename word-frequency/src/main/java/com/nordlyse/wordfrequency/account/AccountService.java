package com.nordlyse.wordfrequency.account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Set<String> LANGUAGES = Set.of("en", "tr", "de", "fr", "es", "sv");
    private static final int MAX_LINE = 1000;

    private final AccountStore store;
    private final PasswordHasher passwords;
    private final SecureRandom random = new SecureRandom();

    public String addAccount(String username, String password) {
        String name = checkedUsername(username);
        checkedPassword(password);
        if (store.findByUsername(name).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered.");
        }
        try {
            store.insertAccount(name, passwords.hash(password));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered.");
        }
        return name;
    }

    public SessionOpened openSession(String username, String password) {
        String name = username == null ? "" : username.trim();
        AccountStore.AccountRecord account = store.findByUsername(name).orElse(null);
        if (account == null || !passwords.matches(password, account.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "That username or password does not match.");
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        store.insertSession(sha256(token), account.username());
        return new SessionOpened(token, account.username());
    }

    public Optional<String> usernameFor(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return store.usernameForToken(sha256(token));
    }

    public void closeSession(String token) {
        if (token != null && !token.isBlank()) {
            store.deleteSession(sha256(token));
        }
    }

    public List<SentLine> lines(String username) {
        return store.lines(username);
    }

    public SentLine addLine(String username, String language, String text) {
        String chosen = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        if (!LANGUAGES.contains(chosen)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a supported language.");
        }
        String line = text == null ? "" : text.trim();
        if (line.isEmpty() || line.codePointCount(0, line.length()) > MAX_LINE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type a line to send it.");
        }
        store.insertLine(username, chosen, line);
        return new SentLine(chosen, line);
    }

    private static String checkedUsername(String username) {
        String name = username == null ? "" : username.trim();
        int length = name.codePointCount(0, name.length());
        boolean allowed = length >= 1 && length <= 64 && name.codePoints().allMatch(AccountService::allowed);
        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use 1 to 64 letters, digits, dots, underscores, or hyphens.");
        }
        return name;
    }

    private static boolean allowed(int codePoint) {
        return Character.isLetterOrDigit(codePoint) || codePoint == '.' || codePoint == '_' || codePoint == '-';
    }

    private static void checkedPassword(String password) {
        String value = password == null ? "" : password;
        int length = value.codePointCount(0, value.length());
        if (length < 8 || length > 128) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use a password of 8 to 128 characters.");
        }
    }

    private static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
