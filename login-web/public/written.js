(function () {
  const storageKey = "keyboardstatisticlogin.lines";

  function read() {
    try {
      const value = JSON.parse(localStorage.getItem(storageKey));
      if (value && typeof value === "object") {
        return value;
      }
    } catch (error) {
      return {};
    }
    return {};
  }

  function remember(language, line) {
    const store = read();
    const lines = Array.isArray(store[language]) ? store[language].slice() : [];
    lines.push(line);
    store[language] = lines;
    localStorage.setItem(storageKey, JSON.stringify(store));
  }

  function words(language) {
    const lines = read()[language];
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
    remember: remember,
    words: words,
    line: line
  };
})();
