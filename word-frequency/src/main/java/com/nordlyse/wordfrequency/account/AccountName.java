package com.nordlyse.wordfrequency.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The stored username.")
public record AccountName(
        @Schema(description = "Account id, as it was registered.", example = "Ada")
        String username) {
}
