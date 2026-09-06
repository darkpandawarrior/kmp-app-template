# AI wiring convention

Three HireSignal AI services shipped a working backend that nothing on the client ever called —
same defect, no mechanical guard. `scripts/check-ai-wiring.sh` is that guard, wired into
`.github/workflows/ci.yml`, and it fails the build for the same reason: a backend or an engine
module exists with no reachable consumer.

## The rule

- Every **server AI endpoint** needs an API client method that calls it, and a real (non-fake) DI
  binding that wires that client into the app.
- Every **zero-IO AI engine module** (a classifier, parser, scorer — pure compute, no network or
  disk) needs a client-side consumer: something that actually calls it.

## How the check finds them

The script doesn't parse Ktor routes or Koin modules — a syntax-aware check needs a rule per
routing library and DI framework, and this repo family uses more than one. It greps for four
one-line marker comments instead, the same shape regardless of what's underneath:

| Marker | Placed on | Says |
|---|---|---|
| `// ai-endpoint: <name>` | a server-side AI route/handler | this endpoint exists |
| `// ai-client: <name>` | the API client method that calls it | the client can reach it |
| `// ai-di: <name>` | the DI binding that wires the client/backend in | production wiring exists |
| `// ai-engine: <name>` | a zero-IO AI engine class/object | this engine exists |
| `// ai-consumer: <name>` | the call site that uses the engine | someone actually calls it |

`<name>` is any identifier you pick — it only has to match between the two markers, so a
`grep -rn` of the name shows both ends of the wire.

The script enforces:
1. Every `ai-endpoint: X` has both an `ai-client: X` and an `ai-di: X` somewhere in the repo.
2. Every `ai-engine: X` has an `ai-consumer: X` somewhere in the repo.
3. No `ai-di:` line names a `Fake`/`Mock`/`Stub` type — test doubles belong in a test source set,
   never in the binding CI checks.

A repo with zero markers passes: a fresh fork of this template has nothing to wire yet, so the
check costs nothing until you add an AI surface. Add the markers when you add the capability.

## Ceiling

This trusts the marker — it can't tell a genuine wire-up from a comment pasted with nothing real
behind it. It catches the "forgot entirely" failure, not a client method that calls the wrong
endpoint. Upgrade to a real call-graph check if the convention ever gets gamed.

## Try it

```sh
scripts/check-ai-wiring.sh          # checks this repo, exit 0 if nothing (or everything) is wired
scripts/test-check-ai-wiring.sh     # the check's own test: known-good/known-bad fixtures
```
