package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stored pace for one account.")
public record PaceProfile(
        @Schema(description = "Up to 16 letter-to-letter gaps, most seen first.")
        List<PaceMark> letters,
        @Schema(description = "Up to 12 pauses from the last letter of one word to the first letter of the next.")
        List<PaceMark> words) {
}
