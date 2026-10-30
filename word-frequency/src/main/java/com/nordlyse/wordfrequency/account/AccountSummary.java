package com.nordlyse.wordfrequency.account;

import java.time.Instant;

public record AccountSummary(String id, String username, Instant addedAt) {
}
