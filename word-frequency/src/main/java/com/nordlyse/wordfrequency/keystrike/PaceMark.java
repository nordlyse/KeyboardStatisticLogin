package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One stored letter gap or word pause.")
public record PaceMark(
        @Schema(description = "First letter of the gap, folded to lower case.", example = "t")
        String from,
        @Schema(description = "Last letter of the gap, folded to lower case.", example = "n")
        String to,
        @Schema(description = "How many times this gap has been seen.")
        long seenCount,
        @Schema(description = "Mean duration in milliseconds. A word pause is the sum of the two steps across the space.")
        double meanMillis,
        @Schema(description = "Error count divided by seen count.")
        double errorFrequency,
        @Schema(description = "Word pairs that use this crossing, up to six. Empty for a letter-to-letter mark.")
        List<WordCrossing> words) {

    public PaceMark {
        words = words == null ? List.of() : List.copyOf(words);
    }
}
