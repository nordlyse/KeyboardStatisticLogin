package com.nordlyse.wordfrequency.keystrike;

public record MatchReport(boolean ready, boolean scored, double matchPercent, boolean accepted, boolean aligned) {
}
