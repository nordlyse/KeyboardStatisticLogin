package com.nordlyse.wordfrequency.keystrike;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One accepted or corrected key.")
public record KeystrikeRequest(
        @Schema(description = "One character, or Backspace, or Delete. Letters are folded to lower case.", example = "a")
        String symbol,
        @Schema(description = "When the key was pressed. Omitted means the service clock.")
        Instant pressedAt,
        @Schema(description = "True counts this character as an error and leaves it out of the following chain.")
        Boolean correction) {
}
