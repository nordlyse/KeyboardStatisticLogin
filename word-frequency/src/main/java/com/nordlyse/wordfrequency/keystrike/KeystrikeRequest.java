package com.nordlyse.wordfrequency.keystrike;

import java.time.Instant;

public record KeystrikeRequest(String symbol, Instant pressedAt, Boolean correction) {
}
