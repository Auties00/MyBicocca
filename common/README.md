# Common

Platform-neutral sources shared by the Android, iOS and (future) web apps.

- [`specs/`](specs) — the specifications of the data APIs, one directory per API with its
  `codegen.json` and any overlays (such as Esse3's glossary, enum names and required-field
  overrides)
- [`codegen/`](codegen) — the generator that turns the specs into the Kotlin, Swift and web
  (TypeScript) SDKs
