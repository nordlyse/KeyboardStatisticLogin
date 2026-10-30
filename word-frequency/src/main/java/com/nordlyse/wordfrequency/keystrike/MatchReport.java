package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How closely one sentence matches the stored pace.")
public record MatchReport(
        @Schema(description = "True when this account already has stored typing.")
        boolean ready,
        @Schema(description = "True when the pace was compared. False when the sentence does not match the keys, or fewer than two keys remain.")
        boolean scored,
        @Schema(description = "Share of compared steps that match the stored mean, from 0 to 100.")
        double matchPercent,
        @Schema(description = "True only when the percent is strictly above 95.")
        boolean accepted,
        @Schema(description = "True when the typed keys spell the shown sentence. Omitted sentence counts as aligned.")
        boolean aligned,
        @Schema(description = "Length-2 gaps that were compared, in sentence order. Empty when nothing stored matches, or the sentence was not aligned.")
        List<PaceCompare> steps) {

    public MatchReport {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
