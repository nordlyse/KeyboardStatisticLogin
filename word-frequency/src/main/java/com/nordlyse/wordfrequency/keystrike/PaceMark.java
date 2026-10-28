package com.nordlyse.wordfrequency.keystrike;

public record PaceMark(String from, String to, long seenCount, double meanMillis, double errorFrequency) {
}
