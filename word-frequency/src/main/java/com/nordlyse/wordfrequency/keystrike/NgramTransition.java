package com.nordlyse.wordfrequency.keystrike;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One stored character sequence that ends on the latest key.")
public record NgramTransition(
        @Schema(description = "Sequence length, from 1 through 10.")
        int orderSize,
        @Schema(description = "Characters in the sequence, already folded to lower case.", example = "ca")
        String sequence,
        @Schema(description = "How many times this sequence has been seen.")
        long seenCount,
        @Schema(description = "How many of those times were corrections.")
        long errorCount,
        @Schema(description = "Error count divided by seen count.")
        double errorFrequency,
        @Schema(description = "Running mean of each gap inside the sequence, in milliseconds.")
        double[] stepMillis) {

    public NgramTransition {
        stepMillis = stepMillis == null ? new double[0] : stepMillis.clone();
    }
}
