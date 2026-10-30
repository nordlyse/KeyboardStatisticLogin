package com.nordlyse.wordfrequency.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Username and password for registration or sign-in.")
public record AccountRequest(
        @Schema(description = "1 to 64 letters, digits, dots, underscores, or hyphens.", example = "Ada")
        String username,
        @Schema(description = "8 to 128 characters. Stored only as a hash.", example = "correct horse")
        String password) {
}
