package com.nordlyse.wordfrequency.keystrike;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Two words that use one letter crossing.")
public record WordCrossing(
        @Schema(description = "Word before the space, with edge punctuation left off.", example = "cat")
        String fromWord,
        @Schema(description = "Word after the space, with edge punctuation left off.", example = "nap")
        String toWord) {
}
