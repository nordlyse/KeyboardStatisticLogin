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

  window.WrittenLines = {
    remember: remember,
    words: words
  };
})();
