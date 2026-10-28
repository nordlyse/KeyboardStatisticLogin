package com.nordlyse.wordfrequency.keystrike;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaceProfileReader {

    private static final int LETTER_LIMIT = 16;
    private static final int WORD_LIMIT = 12;

    private final NgramTransitionStore store;

    public PaceProfile read() {
        return read(List.of());
    }

    public PaceProfile read(List<String> lines) {
        return new PaceProfile(letters(), words(crossings(lines)));
    }

    private List<PaceMark> letters() {
        return store.list(2).stream()
                .filter(row -> letterPair(row.sequence()) && row.stepMillis().length > 0)
                .map(row -> mark(row, row.stepMillis()[0]))
                .sorted(byWeight())
                .limit(LETTER_LIMIT)
                .toList();
    }

    private List<PaceMark> words(Map<String, List<WordCrossing>> crossings) {
        return store.list(3).stream()
                .filter(row -> wordGap(row.sequence()) && row.stepMillis().length > 1)
                .map(row -> mark(row, row.stepMillis()[0] + row.stepMillis()[1], crossings))
                .sorted(byWeight())
                .limit(WORD_LIMIT)
                .toList();
    }

    private static PaceMark mark(NgramTransition row, double meanMillis) {
        return mark(row, meanMillis, Map.of());
    }

    private static PaceMark mark(NgramTransition row, double meanMillis, Map<String, List<WordCrossing>> crossings) {
        int[] points = row.sequence().codePoints().toArray();
        String from = new String(Character.toChars(points[0]));
        String to = new String(Character.toChars(points[points.length - 1]));
        List<WordCrossing> words = crossings.getOrDefault(from + "\n" + to, List.of());
        return new PaceMark(from, to, row.seenCount(), meanMillis, row.errorFrequency(), words);
    }

    private static Map<String, List<WordCrossing>> crossings(List<String> lines) {
        Map<String, LinkedHashSet<WordCrossing>> found = new LinkedHashMap<>();
        if (lines == null) {
            return Map.of();
        }
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            int[] points = line.codePoints().toArray();
            for (int index = 0; index + 2 < points.length; index++) {
                if (!Character.isLetter(points[index]) || points[index + 1] != ' ' || !Character.isLetter(points[index + 2])) {
                    continue;
                }
                String fromWord = wordAt(points, index);
                String toWord = wordAt(points, index + 2);
                if (fromWord == null || toWord == null) {
                    continue;
                }
                String from = letter(points[index]);
                String to = letter(points[index + 2]);
                found.computeIfAbsent(from + "\n" + to, key -> new LinkedHashSet<>())
                        .add(new WordCrossing(fromWord, toWord));
            }
        }
        Map<String, List<WordCrossing>> named = new LinkedHashMap<>();
        found.forEach((key, pairs) -> named.put(key, pairs.stream()
                .sorted(Comparator.comparing(WordCrossing::fromWord).thenComparing(WordCrossing::toWord))
                .limit(6)
                .toList()));
        return named;
    }

    private static String wordAt(int[] points, int index) {
        int start = index;
        while (start > 0 && !Character.isWhitespace(points[start - 1])) {
            start--;
        }
        int end = index + 1;
        while (end < points.length && !Character.isWhitespace(points[end])) {
            end++;
        }
        int from = start;
        int to = end;
        while (from < to && !Character.isLetterOrDigit(points[from])) {
            from++;
        }
        while (to > from && !Character.isLetterOrDigit(points[to - 1])) {
            to--;
        }
        if (from >= to) {
            return null;
        }
        StringBuilder word = new StringBuilder();
        for (int cursor = from; cursor < to; cursor++) {
            word.appendCodePoint(points[cursor]);
        }
        return word.toString();
    }

    private static String letter(int point) {
        return new String(Character.toChars(Character.toLowerCase(point)));
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
