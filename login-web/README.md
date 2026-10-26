# login-web

`login-web` serves the word page and the sign-in page for KeyboardStatisticLogin. Both are static pages inside an Apache HTTP Server image, so the container starts without an install step.

## Purpose

[http://localhost:8081](http://localhost:8081) is the first page. The person chooses a language, and a prepared line in that language is shown. They type the same line, then **Send line**. The next line appears. **Write an extra line** accepts a line of their own, and after the prepared lines they can keep writing. Each letter, space, and correction is posted to `http://localhost:8080/keystrikes` with the time of the key, and the profile in pgvector is updated.

Sign-in is at [http://localhost:8081/sign-in.html](http://localhost:8081/sign-in.html). It posts the same way. A password is not part of the check.

The dark field, the moving colour, and the light that follows the pointer are original. React Bits was not added. Its licence is MIT plus the Commons Clause, which is outside the MIT and Apache-2.0 rule.

## Stack

| Tool | Version | Licence |
| --- | --- | --- |
| Apache HTTP Server | 2.4.69 | Apache-2.0 |
| Image | `httpd:2.4.69-alpine` | Apache-2.0 for httpd |
| Alpine | 3.24, the image base | musl is MIT; BusyBox in the base is GPL-2.0 |

The page is published on [http://localhost:8081](http://localhost:8081). From another Compose service it is `http://login-web:80`.
