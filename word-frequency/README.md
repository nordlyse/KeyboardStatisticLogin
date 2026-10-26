# word-frequency

`word-frequency` is the long-running Spring Boot service for KeyboardStatisticLogin. It stays up beside the pgvector database and keeps the letter-transition analysis.

## Purpose

Each accepted key updates every sequence length from 1 through 10 that ends on that key. Length 1 is the character itself. Length 2 is the step from the previous character to this one. A longer length holds the chain of steps inside that window, so length 10 holds nine steps across the last ten characters. Older characters fall out of the window as new ones arrive.

A correction, sent as `Backspace` or with `"correction": true`, raises the error count on each of those sequences. The error frequency is the error count divided by the number of times that sequence has been seen. The step times stay as a running mean of the gaps between the keys.

The counts, the error frequency, and the step means are written into a `vector(11)` column in `keystrike_ngram` and updated in place. Slot 0 is the error frequency, slot 1 is a scaled seen-count, and slots 2 through 10 are the step means in seconds. The latest ten keys are held in memory so the next chain can be formed. The stored rows remain in pgvector across a restart.

## Keystrikes

`POST /keystrikes` accepts one key.

```bash
curl -s -X POST http://localhost:8080/keystrikes \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"l","pressedAt":"2026-10-23T19:00:00Z"}'
```

A following key sends the same shape. A correction sends `{"symbol":"Backspace"}` or `{"symbol":"g","correction":true}`. The second form counts that character as an error and leaves it out of the following chain. Letters are folded to lower case. A single space is kept, so the step from one word to the next stays in the profile. The response lists the sequences updated by that key.

## Match

`GET /matches` reports whether any typing has been stored. `POST /matches` compares one shown sentence with that profile and does not write it back. The sign-in page sends a sentence assembled from words the person already typed. The body is `{ "sentence": "River silver harbor.", "keys": [ { "symbol": "R", "pressedAt": "2026-10-26T10:00:00Z" } ] }`. The response includes `ready`, `scored`, `matchPercent`, `accepted`, and `aligned`. `aligned` is true when the typed keys spell `sentence`. The pace is scored only then. `accepted` is true only when that percent is above 95. A letter-to-letter step is compared when that pair is already stored. A pair that was never typed is left out of the average, so a new arrangement of known words can still match. When no stored step matches, the score is 0. Longer windows count only when that exact sequence is already in the profile. A step within 35% of the stored mean, and at least 40 milliseconds, counts as a full match.

## Stack

| Tool | Version | Licence |
| --- | --- | --- |
| word-frequency | 0.1.0 | Apache-2.0 |
| Spring Boot | 4.1.2 | Apache-2.0 |
| Java | 25 | — |
| Lombok | from Spring Boot | MIT |
| PostgreSQL JDBC | from Spring Boot | BSD-2-Clause |
| HikariCP | from the JDBC starter | Apache-2.0 |
| Tomcat | from the web starter | Apache-2.0 |
| Maven, used only to build the image | 3.9.16 | Apache-2.0 |
| Eclipse Temurin | 25 | GPL-2.0 with the Classpath Exception |
| JUnit Jupiter, tests only | from Spring Boot | EPL-2.0 |
| Spring Boot web MVC test | from Spring Boot | Apache-2.0 |
| Testcontainers | 2.0.5, tests only | MIT |

Spring Boot 4.1.2 is the current stable release. It needs Java 17 or newer and accepts Java 25. The image build uses Temurin 25.

## Store

The service reads the pgvector database from Compose.

| | |
| --- | --- |
| Host, from this machine | `jdbc:postgresql://localhost:5432/keyboardstatisticlogin` |
| From Compose | `jdbc:postgresql://pgvector:5432/keyboardstatisticlogin` |
| User | `keyboardstatisticlogin` |
| Password | `keyboardstatisticlogin` |
