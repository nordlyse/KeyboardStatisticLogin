package com.nordlyse.wordfrequency.keystrike;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaceProfileReader {

    private static final int LETTER_LIMIT = 16;
    private static final int WORD_LIMIT = 12;

    private final NgramTransitionStore store;

    public PaceProfile read() {
        return new PaceProfile(letters(), words());
    }

    private List<PaceMark> letters() {
        return store.list(2).stream()
                .filter(row -> letterPair(row.sequence()) && row.stepMillis().length > 0)
                .map(row -> mark(row, row.stepMillis()[0]))
                .sorted(byWeight())
                .limit(LETTER_LIMIT)
                .toList();
    }

    private List<PaceMark> words() {
        return store.list(3).stream()
                .filter(row -> wordGap(row.sequence()) && row.stepMillis().length > 1)
                .map(row -> mark(row, row.stepMillis()[0] + row.stepMillis()[1]))
                .sorted(byWeight())
                .limit(WORD_LIMIT)
                .toList();
    }

    private static PaceMark mark(NgramTransition row, double meanMillis) {
        int[] points = row.sequence().codePoints().toArray();
        String from = new String(Character.toChars(points[0]));
        String to = new String(Character.toChars(points[points.length - 1]));
        return new PaceMark(from, to, row.seenCount(), meanMillis, row.errorFrequency());
    }

    private static Comparator<PaceMark> byWeight() {
        return Comparator.comparingLong(PaceMark::seenCount).reversed()
                .thenComparing(PaceMark::from)
                .thenComparing(PaceMark::to);
    }

    private static boolean letterPair(String sequence) {
        int[] points = sequence.codePoints().toArray();
        return points.length == 2 && Character.isLetter(points[0]) && Character.isLetter(points[1]);
    }

    private static boolean wordGap(String sequence) {
        int[] points = sequence.codePoints().toArray();
        return points.length == 3
                && Character.isLetter(points[0])
                && points[1] == ' '
                && Character.isLetter(points[2]);
    }
}
