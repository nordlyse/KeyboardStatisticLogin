# login-web

`login-web` is the sign-in page for KeyboardStatisticLogin. It is a static page inside an Apache HTTP Server image, so the container starts without an install step.

## Purpose

The page will ask the person to type a word. It will send that typing to `word-frequency`, and the service will compare the letter-to-letter pace with the profile in pgvector. A password is not part of the check.

This image serves that purpose. The sign-in form comes in a later change.

## Stack

| Tool | Version | Licence |
| --- | --- | --- |
| Apache HTTP Server | 2.4.69 | Apache-2.0 |
| Image | `httpd:2.4.69-alpine` | Apache-2.0 for httpd |
| Alpine | 3.24, the image base | musl is MIT; BusyBox in the base is GPL-2.0 |

The page is published on [http://localhost:8081](http://localhost:8081). From another Compose service it is `http://login-web:80`.
