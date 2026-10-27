# login-web

`login-web` serves the word page and the sign-in page for KeyboardStatisticLogin. Both are static pages inside an Apache HTTP Server image, so the container starts without an install step.

## Purpose

[http://localhost:8081/account.html](http://localhost:8081/account.html) asks for a username and a password. **Register** stores a new account. **Sign in** opens a later visit. The password is stored only as a hash. Lines and the typing profile belong to that account, so the next visit continues them.

[http://localhost:8081](http://localhost:8081) is the lines page. The person chooses a language, and a prepared line in that language is shown. Each language has forty lines. A later visit skips lines that account already sent and shows one that is still new, in a mixed order. The person types that line, then **Send line**. The next new line appears. After the prepared lines are sent, the next prompt is a new sentence made from the stored words. **Write an extra line** accepts a line of their own. Each letter, space, and correction is posted to `http://localhost:8080/keystrikes` with the time of the key, and the profile for that account in pgvector is updated. The sent line is stored in the database as well.

The sentence check is at [http://localhost:8081/sign-in.html](http://localhost:8081/sign-in.html). It shows a sentence assembled from words the account already sent in that language. The sentence does not have to mean anything, and the same word can appear again. Each visit, and **Another sentence**, asks for a different sentence from the lines already stored. A prepared line they have not typed is not shown. The person types that sentence, and the page sends it to `POST /matches` in the `sentence` field. The profile is not updated, and a different sentence is not scored. The page shows the match percent and stays there: above 95% the check is verified, and at or below 95% it is refused. **Another sentence** assembles a new line from the same words. When no sent line is stored for the account, or `GET /matches` reports that no profile is stored, the page asks the person to type lines first. The two pages switch from the buttons at the top. **Sign out** closes the visit. A password is not part of the sentence check.

The dark field, the moving colour, and the light that follows the pointer are original. React Bits was not added. Its licence is MIT plus the Commons Clause, which is outside the MIT and Apache-2.0 rule.

## Stack

| Tool | Version | Licence |
| --- | --- | --- |
| Apache HTTP Server | 2.4.69 | Apache-2.0 |
| Image | `httpd:2.4.69-alpine` | Apache-2.0 for httpd |
| Alpine | 3.24, the image base | musl is MIT; BusyBox in the base is GPL-2.0 |

The page is published on [http://localhost:8081](http://localhost:8081). From another Compose service it is `http://login-web:80`.

## Tests

The line selection checks run with Node, which is used only for tests and is MIT:

```bash
node --test test/written.test.js
```
