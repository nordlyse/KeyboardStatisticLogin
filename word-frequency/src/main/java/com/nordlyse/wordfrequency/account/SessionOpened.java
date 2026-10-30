package com.nordlyse.wordfrequency.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An open visit.")
public record SessionOpened(
        @Schema(description = "Bearer token for later requests. The service stores only a hash of it.")
        String token,
        @Schema(description = "Account id for this visit.", example = "Ada")
        String username) {
}
