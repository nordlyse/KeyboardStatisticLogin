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

  function leadWord(word, language) {
    const chars = Array.from(word);
    chars[0] = chars[0].toLocaleUpperCase(language);
    return chars.join("");
  }

  function line(language) {
    const pool = words(language);
    if (pool.length === 0) {
      return "";
    }
    const count = 6 + Math.floor(Math.random() * 5);
    const picked = [];
    for (let index = 0; index < count; index += 1) {
      picked.push(pool[Math.floor(Math.random() * pool.length)]);
    }
    picked[0] = leadWord(picked[0], language);
    return picked.join(" ") + ".";
  }

  window.WrittenLines = {
    load: load,
    remember: remember,
    words: words,
    line: line
  };
})();
