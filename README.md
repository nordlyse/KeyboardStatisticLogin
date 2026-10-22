# KeyboardStatisticLogin

KeyboardStatisticLogin watches how a person types and uses that record to sign them in. It follows the words they type, keeps the pace and the corrections inside each word, and stores that pattern in pgvector. A later sign-in is the same word typed again. The application compares the new typing with the stored pattern. A password is not part of the check.

## Aim

The application stays on while the person types. It does not wait for a separate enrolment form. Each word adds another observation, and the stored pattern is updated from that observation.

Three things are kept current:

- How often each word is typed.
- The step from one character to the next inside that word: which character follows which, how the step is made, and how long it takes.
- Corrections inside the word, such as a deleted character or a character typed again.

When a word is offered at sign-in, the application reads the stored pace, the frequency of that word, and the corrections. A close match signs the person in.

## What is stored

Every accepted word is broken into the gaps between its characters. For the word `login`, the gaps are `l→o`, `o→g`, `g→i`, and `i→n`. Each gap records:

- the character just typed and the character that follows it
- the time spent on that step
- whether the step includes a correction
- how often this word, and this step, have been seen

Those measures are written as vectors in PostgreSQL with pgvector. A new sample does not sit beside an old one forever. The vector for that word and for each step inside it is updated, so the profile follows the person's current typing.

The same store answers a later question: given this word, typed at this pace, with these corrections, does it belong to this person?

## Sign-in

Sign-in asks for a word, not a password. The person types the word in the usual way. The application measures the same gaps, the same timing, and the same corrections, then compares that attempt with the vectors already held for the person.

A match needs more than the right spelling. The pace between characters, the frequency of the word, and the corrections have to agree with the profile. An attempt that spells the word and still types it unlike the stored profile is refused.

## Store

The profile lives in PostgreSQL. pgvector holds the vectors and supports the similarity check used at sign-in. Observations are inserted as the person types, and existing vectors are updated in place.

The database is the `pgvector` service in `compose.yaml`.

| | |
| --- | --- |
| Image | `pgvector/pgvector:0.8.6-pg17` |
| Host | `localhost:5432` |
| From another Compose service | `postgresql://pgvector:5432/keyboardstatisticlogin` |
| Database | `keyboardstatisticlogin` |
| User | `keyboardstatisticlogin` |
| Password | `keyboardstatisticlogin` |

Data is kept in the `pgvector-data` volume.

## Run

Start the store:

```bash
docker compose up
```

## Licence

This project is Apache-2.0. The full terms are in [LICENSE](LICENSE).

The `pgvector/pgvector:0.8.6-pg17` image and PostgreSQL 17 use the PostgreSQL License. That image is the store for the typing profile. Other tools added to this project must be MIT or Apache-2.0.
