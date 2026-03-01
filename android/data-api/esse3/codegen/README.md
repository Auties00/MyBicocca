# Esse3 Codegen

Kotlin code generation module for the Esse3 OpenAPI specifications.

### Background

When we decided to integrate [Esse3](https://s3w.si.unimib.it/Root.do) into our app, we tried to look for a REST API, but couldn't find one.
As a result, we started developing the original [Esse3 Data Module](./../../esse3-scraper) using web scraping.
Some time later, though, we randomly discovered that [Esse3 exposes a SwaggerUI on a non-indexed page](https://s3w.si.unimib.it/e3rest/api/swagger-service-v1/swagger/apis).
From there, we were able to develop a bash script that extracts the OpenAPI specs that the SwaggerUI shows: these are available in the [openapi](./openapi) directory.

### Why not use the OpenAPI CLI

The OpenAPI CLI supports generating Kotlin code, but it doesn't follow the structure and conventions we have used for the other data modules.
Moreover, it doesn't support a few important OpenAPI spec features for Kotlin that Esse3 uses.

### How does this tool work

The tool parses the OpenAPI YAML specs from the [openapi](./openapi) directory using [Swagger Parser](https://github.com/swagger-api/swagger-parser), extracts operations (endpoints) and definitions (schemas), deduplicates definitions that appear across multiple specs, and generates:

- **DTO data classes** (`*Types.kt`) — `@Serializable` data classes with `@SerialName` annotations for wire-format field names, custom serializers for dates, and sensible defaults for optional fields
- **API classes** (`*Api.kt`) — one per spec, each extending `Esse3AbstractApi` and providing suspend functions for every endpoint, with path/query/body parameters and typed return values
- **Facade class** (`Esse3Api.kt`) — aggregates all sub-APIs behind a single entry point with a shared Ktor `HttpClient` and JSON configuration

The tool also handles OpenAPI-level details like `allOf` composition (flattened into a single data class), permission annotations (`x-enabled-profiles`), and response type resolution (including list responses).

### Name Translation

Esse3 uses a mix of Italian and English names, along with cryptic internal acronyms that CINECA never documented publicly.
For example, `aggcarr` stands for *aggiornamento carriera* (career update), `regsceApi` is *regole di scelta* (choice rules), and `AD` is short for *Attività Didattica* (teaching activity).
We didn't want any of that in our codebase.

The codegen collects every name it encounters — class names, field names, method names, file names — and writes them as keys in [dictionary.json](./dictionary.json).
New entries are added with an empty value; existing translations are preserved.
An LLM is then used to translate these entries, given the appropriate context about Esse3 and the university domain so that even the most obscure acronyms can be deciphered.
At generation time, every name is looked up in the dictionary: if a translation exists, it's used; otherwise, the original name is kept as-is.

This fixes the issue without us having to manually rename thousands of identifiers.
