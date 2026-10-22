# word-frequency

`word-frequency` is the long-running Spring Boot service for KeyboardStatisticLogin. It stays up beside the pgvector database and is the place that will keep the letter frequencies.

## Purpose

While a person types, each word is a sequence of characters. This service will follow the step from one character to the next inside that word and keep how often that step happens. For the word `login`, the steps are `l→o`, `o→g`, `g→i`, and `i→n`. Each step will record which character follows which, and how often that passage has been seen.

Those frequencies will be written into the pgvector database and updated as further typing arrives. The same record is what a later sign-in can compare when a word is typed again.

This module states that purpose and holds the Spring Boot dependencies the recording will use.

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

Spring Boot 4.1.2 is the current stable release. It needs Java 17 or newer and accepts Java 25. The image build uses Temurin 25.

## Store

The service reads the pgvector database from Compose.

| | |
| --- | --- |
| Host, from this machine | `jdbc:postgresql://localhost:5432/keyboardstatisticlogin` |
| From Compose | `jdbc:postgresql://pgvector:5432/keyboardstatisticlogin` |
| User | `keyboardstatisticlogin` |
| Password | `keyboardstatisticlogin` |
