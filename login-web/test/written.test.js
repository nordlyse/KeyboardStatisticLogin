const assert = require("node:assert/strict");

function here(value) {
  return JSON.parse(JSON.stringify(value));
}
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

function loadWritten() {
  const source = fs.readFileSync(path.join(__dirname, "../public/written.js"), "utf8");
  const context = { window: {} };
  vm.runInNewContext(source, context);
  return context.window.WrittenLines;
}

test("skips a line the account already sent", () => {
  const written = loadWritten();
  written.load([{ language: "en", text: "She wrote the date on the first blank page." }]);

  const left = written.unseen("en", [
    "SHE WROTE THE DATE ON THE FIRST BLANK PAGE.",
    "A grey cat watches the rain from the sill."
  ]);

  assert.deepEqual(here(left), ["A grey cat watches the rain from the sill."]);
});

test("keeps another language when one language is filtered", () => {
  const written = loadWritten();
  written.load([{ language: "tr", text: "gumus defter" }]);

  const left = written.unseen("en", ["gumus defter", "silver notebook"]);

  assert.deepEqual(here(left), ["gumus defter", "silver notebook"]);
});

test("asks for nothing when no line is stored", () => {
  const written = loadWritten();
  written.load([]);

  assert.equal(written.line("en"), "");
  assert.deepEqual(here(written.words("en")), []);
});

test("builds the next sentence from that language only", () => {
  const written = loadWritten();
  written.load([
    { language: "en", text: "silver notebook" },
    { language: "tr", text: "gumus defter" }
  ]);

  const sentence = written.line("tr");

  assert.match(sentence, /gumus|defter/i);
  assert.doesNotMatch(sentence, /silver|notebook/i);
  assert.notEqual(sentence.toLocaleLowerCase("tr"), "gumus defter");
});

test("asks for a different sentence than the one just shown", () => {
  const written = loadWritten();
  written.load([{ language: "en", text: "silver notebook harbor" }]);

  const first = written.line("en");
  const second = written.line("en", first);

  assert.notEqual(second.toLocaleLowerCase("en"), first.toLocaleLowerCase("en"));
  assert.notEqual(first.toLocaleLowerCase("en"), "silver notebook harbor");
});

test("drops a one-letter token and the punctuation around a word", () => {
  const written = loadWritten();
  written.load([{ language: "en", text: "a silver, notebook." }]);

  assert.deepEqual(here(written.words("en")), ["silver", "notebook"]);
});
