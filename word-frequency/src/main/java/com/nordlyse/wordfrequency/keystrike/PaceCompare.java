package com.nordlyse.wordfrequency.keystrike;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One length-2 gap of the checked sentence beside the stored mean.")
public record PaceCompare(
        @Schema(description = "Character before the gap. A space is a space.", example = "a")
        String from,
        @Schema(description = "Character after the gap.", example = "b")
        String to,
        @Schema(description = "Gap in this attempt, in milliseconds.")
        double attemptMillis,
        @Schema(description = "Stored running mean for this pair, in milliseconds.")
        double meanMillis) {
}
