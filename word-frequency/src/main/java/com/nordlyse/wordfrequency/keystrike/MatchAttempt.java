package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Keys typed for one shown sentence.")
public record MatchAttempt(
        @Schema(description = "Keys in press order, including spaces and corrections.")
        List<KeystrikeRequest> keys,
        @Schema(description = "Shown sentence. When set, the typed keys must spell it or the pace is not scored.",
                example = "River silver harbor.")
        String sentence) {

    public MatchAttempt(List<KeystrikeRequest> keys) {
        this(keys, null);
    }
}
