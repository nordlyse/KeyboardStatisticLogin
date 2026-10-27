(function () {
  const storageKey = "keyboardstatisticlogin.session";

  function read() {
    try {
      const value = JSON.parse(localStorage.getItem(storageKey));
      if (value && typeof value.token === "string" && value.token !== "") {
        return value;
      }
    } catch (error) {
      return null;
    }
    return null;
  }

  function token() {
    const value = read();
    return value ? value.token : "";
  }

  function username() {
    const value = read();
    return value && value.username ? value.username : "";
  }

  function keep(nextToken, nextUsername) {
    localStorage.setItem(storageKey, JSON.stringify({
      token: nextToken,
      username: nextUsername
    }));
  }

  function clear() {
    localStorage.removeItem(storageKey);
  }

  function base() {
    if (document.body && document.body.dataset.accountUrl) {
      return document.body.dataset.accountUrl;
    }
    return "http://localhost:8080";
  }

  function headers() {
    const value = { "Content-Type": "application/json" };
    if (token()) {
      value.Authorization = "Bearer " + token();
    }
    return value;
  }

  function onAccountPage() {
    return window.location.pathname.endsWith("/account.html") || window.location.pathname.endsWith("account.html");
  }

  function authFetch(url, options) {
    const request = options ? Object.assign({}, options) : {};
    request.headers = Object.assign({}, headers(), request.headers || {});
    return fetch(url, request).then(function (response) {
      if (response.status === 401) {
        clear();
        if (!onAccountPage()) {
          window.location.replace("account.html");
        }
        throw new Error("Sign in again.");
      }
      return response;
    });
  }

  function guard() {
    if (!token()) {
      if (!onAccountPage()) {
        window.location.replace("account.html");
      }
      return Promise.reject(new Error("Sign in again."));
    }
    return authFetch(base() + "/sessions").then(function (response) {
      if (!response.ok) {
        throw new Error("Sign in again.");
      }
      return response.json();
    });
  }

  function bindSignOut(button) {
    button.addEventListener("click", function () {
      authFetch(base() + "/sessions", { method: "DELETE" }).catch(function () {
        return null;
      }).then(function () {
        clear();
        window.location.replace("account.html");
      });
    });
  }

  window.AccountSession = {
    token: token,
    username: username,
    keep: keep,
    clear: clear,
    base: base,
    headers: headers,
    authFetch: authFetch,
    guard: guard,
    bindSignOut: bindSignOut
  };
})();
