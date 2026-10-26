(function () {
  function bind(form, options) {
    const allowSpace = Boolean(options && options.allowSpace);
    const word = form.querySelector("[data-word]");
    const panel = form.closest(".panel");
    const status = form.querySelector("[data-status]");
    const endpoint = document.body.dataset.keystrikeUrl;
    let chain = Promise.resolve();
    let problem = "";

    function setStatus(message) {
      status.textContent = message;
    }

    function sendKey(symbol) {
      return fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          symbol: symbol,
          pressedAt: new Date().toISOString()
        })
      }).then(function (response) {
        if (!response.ok) {
          throw new Error("That key could not be recorded.");
        }
      }).catch(function (error) {
        if (error instanceof TypeError) {
          throw new Error("The word-frequency service is out of reach.");
        }
        throw error;
      });
    }

    panel.addEventListener("pointermove", function (event) {
      const bounds = panel.getBoundingClientRect();
      panel.style.setProperty("--spot-x", (event.clientX - bounds.left) + "px");
      panel.style.setProperty("--spot-y", (event.clientY - bounds.top) + "px");
    });

    word.addEventListener("paste", function (event) {
      event.preventDefault();
      setStatus("Type each letter so the pace can be read.");
    });

    word.addEventListener("drop", function (event) {
      event.preventDefault();
    });

    word.addEventListener("keydown", function (event) {
      if (event.isComposing || event.repeat || event.metaKey || event.ctrlKey || event.altKey) {
        return;
      }
      if (event.key === " " && !allowSpace) {
        event.preventDefault();
        setStatus("Add one word at a time.");
        return;
      }
      let symbol = "";
      if (event.key === "Backspace" || event.key === "Delete") {
        symbol = event.key;
      } else if (Array.from(event.key).length === 1) {
        symbol = event.key;
      } else {
        return;
      }
      chain = chain.then(function () {
        return sendKey(symbol);
      }).then(function () {
        problem = "";
        setStatus("Listening to the pace.");
      }).catch(function (error) {
        problem = error.message;
        setStatus(problem);
      });
    });

    return {
      word: word,
      setStatus: setStatus,
      whenSettled: function () {
        return chain;
      },
      problem: function () {
        return problem;
      }
    };
  }

  window.KeystrikeField = { bind: bind };
})();
