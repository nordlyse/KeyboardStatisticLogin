package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

public record MatchReport(
        boolean ready,
        boolean scored,
        double matchPercent,
        boolean accepted,
        boolean aligned,
        List<PaceCompare> steps) {

    public MatchReport {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
