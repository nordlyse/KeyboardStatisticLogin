package com.nordlyse.wordfrequency.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One line an account sent.")
public record SentLine(
        @Schema(description = "Language code: en, tr, de, fr, es, or sv.", example = "en")
        String language,
        @Schema(description = "Line text, from 1 to 1000 characters.", example = "silver notebook")
        String text) {
}
