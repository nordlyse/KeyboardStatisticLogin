package com.nordlyse.wordfrequency.keystrike;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class KeyParser {

    private KeyParser() {
    }

    static Parsed parse(KeystrikeRequest request, Clock clock) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A keystrike is required");
        }
        String raw = request.symbol() == null ? "" : request.symbol();
        if (raw.isEmpty() || isCorrectionKey(raw.trim())) {
            return new Parsed("", instant(request, clock), true, false);
        }
        if (raw.codePointCount(0, raw.length()) != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A keystrike symbol is a single character");
        }
        int codePoint = raw.codePointAt(0);
        String symbol = new String(Character.toChars(Character.toLowerCase(codePoint)));
        return new Parsed(symbol, instant(request, clock), false, Boolean.TRUE.equals(request.correction()));
    }

    private static Instant instant(KeystrikeRequest request, Clock clock) {
        return request.pressedAt() == null ? clock.instant() : request.pressedAt();
    }

    private static boolean isCorrectionKey(String raw) {
        String name = raw.toLowerCase(Locale.ROOT);
        return name.equals("backspace") || name.equals("delete");
    }

    record Parsed(String symbol, Instant pressedAt, boolean undo, boolean erroneous) {
    }
}
