package com.nordlyse.wordfrequency.account;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One account, without the password hash.")
public record AccountSummary(
        @Schema(description = "Account id. This is the username.", example = "Ada")
        String id,
        @Schema(description = "Username as it was registered.", example = "Ada")
        String username,
        @Schema(description = "When the account was added.")
        Instant addedAt) {
}
