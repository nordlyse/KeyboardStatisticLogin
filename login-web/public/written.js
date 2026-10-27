(function () {
  let cache = {};

  function load(lines) {
    cache = {};
    lines.forEach(function (line) {
      const language = line.language;
      const stored = cache[language] || [];
      stored.push(line.text);
      cache[language] = stored;
    });
  }

  function remember(language, line) {
    return window.AccountSession.authFetch(window.AccountSession.base() + "/lines", {
      method: "POST",
      body: JSON.stringify({ language: language, text: line })
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("That line could not be stored.");
      }
      const stored = cache[language] ? cache[language].slice() : [];
      stored.push(line);
      cache[language] = stored;
    });
  }

  function words(language) {
    const lines = cache[language];
    const found = [];
    if (!Array.isArray(lines)) {
      return found;
    }
    lines.forEach(function (line) {
      String(line).split(/\s+/).forEach(function (token) {
        const word = token.replace(/^[^\p{L}\p{N}]+|[^\p{L}\p{N}]+$/gu, "");
        if (Array.from(word).length >= 2) {
          found.push(word);
        }
      });
    });
    return found;
  }

  function lines(language) {
    const stored = cache[language];
    return Array.isArray(stored) ? stored.slice() : [];
  }

  function fold(text, language) {
    return String(text).trim().replace(/\s+/g, " ").toLocaleLowerCase(language);
  }

  function leadWord(word, language) {
    const chars = Array.from(word);
    chars[0] = chars[0].toLocaleUpperCase(language);
    return chars.join("");
  }

  function assemble(pool, language, count) {
    const picked = [];
    for (let index = 0; index < count; index += 1) {
      picked.push(pool[Math.floor(Math.random() * pool.length)]);
    }
    picked[0] = leadWord(picked[0], language);
    return picked.join(" ") + ".";
  }

  function unseen(language, prepared) {
    const sent = {};
    lines(language).forEach(function (text) {
      sent[fold(text, language)] = true;
    });
    return prepared.filter(function (line) {
      return !sent[fold(line, language)];
    });
  }

  function line(language, previous) {
    const pool = words(language);
    if (pool.length === 0) {
      return "";
    }
    const sent = {};
    lines(language).forEach(function (text) {
      sent[fold(text, language)] = true;
    });
    const prior = previous ? fold(previous, language) : "";
    let built = "";
    for (let attempt = 0; attempt < 12; attempt += 1) {
      const count = 6 + ((Math.floor(Math.random() * 5) + attempt) % 5);
      built = assemble(pool, language, count);
      const folded = fold(built, language);
      if (folded !== prior && !sent[folded]) {
        return built;
      }
    }
    return built;
  }

  window.WrittenLines = {
    load: load,
    remember: remember,
    words: words,
    lines: lines,
    unseen: unseen,
    line: line
  };
})();
