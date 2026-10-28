# KeyboardStatisticLogin

KeyboardStatisticLogin watches how a person types and keeps that rhythm for a later check. Each key updates the pace and the error frequency of the character sequences that end on that key. The profile is stored in pgvector, one profile per account.

The first page asks for a username and a password. A first visit registers that account. The password is stored only as a hash. Later visits sign in with the same account, so the lines and the typing profile continue instead of starting again.

After sign-in, the lines page asks for a language and shows a prepared line. Six languages are available, with forty lines each. A later visit skips the lines that account already sent and shows one that is still new, in a mixed order. After those lines are sent, the next prompt is a new sentence made from the stored words. The person types the line and sends it. They can also write an extra line. Each letter is posted, with its time, to `word-frequency` for that account. The same letter sequence, typed again at a different pace, updates a running mean.

The sentence check is a separate page. It shows a sentence assembled from words the account already sent. The sentence does not have to mean anything, and each visit asks for a different sentence. The person types that sentence, and the page compares that pace with the stored profile. The result stays on the page. A match above 95% is accepted. When no lines have been stored, that page asks for writing first. Statistics, just left of Sign out, draws that stored pace as a fingerprint of letter gaps and word pauses. The page switch is at the top of each page. A password is not part of the sentence check.

## Aim

The service stays up while the person types. Every accepted key updates that account's stored profile in place.

For a key, the service keeps every sequence length from 1 through 10 that ends on that key:

- Length 1 is the character itself.
- Length 2 is the step from the previous character to this one, including how long the step took.
- Lengths 3 through 10 hold the chain of steps inside that window. Length 10 holds nine steps across the last ten characters.

A correction raises the error count on each of those sequences. The error frequency is the error count divided by the number of times the sequence has been seen. Older characters fall out of the window as new ones arrive.

## What is stored

Rows for the pace live in `keystrike_ngram`. `profile_name` is the signed-in username, so one account's rows stay apart from another's. The measures for one sequence are a `vector(11)`:

| Slot | Meaning |
| --- | --- |
| 0 | Error frequency |
| 1 | Scaled count of how often the sequence has been seen |
| 2–10 | Mean step time, in seconds, for up to nine gaps |

When the same sequence is typed again, each step time becomes the running mean of every writing of that sequence. Letters are folded to lower case. The latest ten keys for each account are held in memory so the next chain can be formed. The rows themselves stay in pgvector across a restart.

The account, the session, and the sent lines live beside that table. `account` holds the username and the password hash. `account_session` holds a hash of the sign-in token. `sent_line` holds each line the account sent, with its language.

For the letters in `login`, the length-2 steps are `l→o`, `o→g`, `g→i`, and `i→n`. The same keystrokes also update the longer windows that end on each of those letters, up to length 10.

## Services

| Service | Role | From this machine | From another Compose service |
| --- | --- | --- | --- |
| `pgvector` | Profile store | `localhost:5432` | `pgvector:5432` |
| `word-frequency` | Keystrike analysis | [http://localhost:8080](http://localhost:8080) | `http://word-frequency:8080` |
| `login-web` | Account, line, and sentence-check pages | [http://localhost:8081](http://localhost:8081) | `http://login-web:80` |

`word-frequency` is a Spring Boot 4.1.2 service on Java 25, with Lombok. Its notes and the full request shape are in [word-frequency/README.md](word-frequency/README.md). The page is in [login-web/README.md](login-web/README.md).

## Keystrikes

`POST /keystrikes` accepts one key for the signed-in account. A request with no valid token is refused.

```bash
curl -s -X POST http://localhost:8080/keystrikes \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer TOKEN' \
  -d '{"symbol":"l","pressedAt":"2026-10-23T19:00:00Z"}'
```

A following key uses the same shape. A correction sends `{"symbol":"Backspace"}` or `{"symbol":"g","correction":true}`. The second form counts that character as an error and leaves it out of the following chain. The response lists the sequences updated by that key.

## Store

The database is the `pgvector` service in `compose.yaml`.

| | |
| --- | --- |
| Image | `pgvector/pgvector:0.8.6-pg17` |
| Host | `localhost:5432` |
| From another Compose service | `jdbc:postgresql://pgvector:5432/keyboardstatisticlogin` |
| Database | `keyboardstatisticlogin` |
| User | `keyboardstatisticlogin` |
| Password | `keyboardstatisticlogin` |

Data is kept in the `pgvector-data` volume. The service adds the `vector` extension when it starts, then `keystrike_ngram`, `account`, `account_session`, and `sent_line`.

## Run

Start the store, the service, and the page:

```bash
docker compose up --build
```

The `word-frequency` tests cover the analysis, the controller, and the pgvector store. From `word-frequency/`:

```bash
mvn test
```

The store test starts `pgvector/pgvector:0.8.6-pg17` through Testcontainers, so a Docker daemon has to be available.

The line-selection checks run from `login-web/` with Node, which is used only for tests:

```bash
node --test test/written.test.js
```

## Licence

This project is Apache-2.0. The full terms are in [LICENSE](LICENSE).

| Tool | Version | Licence |
| --- | --- | --- |
| KeyboardStatisticLogin | 0.1.0 | Apache-2.0 |
| Spring Boot | 4.1.2 | Apache-2.0 |
| Lombok | from Spring Boot | MIT |
| HikariCP and Tomcat | from Spring Boot | Apache-2.0 |
| PostgreSQL JDBC | from Spring Boot | BSD-2-Clause |
| pgvector image `pgvector/pgvector` | 0.8.6-pg17 | PostgreSQL License |
| PostgreSQL | 17, in that image | PostgreSQL License |
| Maven, used only to build the image | 3.9.16 | Apache-2.0 |
| Eclipse Temurin | 25 | GPL-2.0 with the Classpath Exception |
| Apache HTTP Server | 2.4.69 | Apache-2.0 |
| Alpine | 3.24, base of the httpd image | musl is MIT; BusyBox in the base is GPL-2.0 |
| JUnit Jupiter, tests only | from Spring Boot | EPL-2.0 |
| Spring Boot web MVC test | from Spring Boot | Apache-2.0 |
| Testcontainers | 2.0.5, tests only | MIT |
| Node.js | tests only | MIT |

The PostgreSQL JDBC driver, the pgvector image, and Temurin are recorded here because the typing profile needs that store and that Java runtime. Apache HTTP Server is Apache-2.0. Its Alpine base is recorded because that image is the fast page runtime. Further tools added to this project must be MIT or Apache-2.0.
