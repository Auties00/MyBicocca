# MyBicocca for the web

The future web app. For now it holds the TypeScript SDKs of the data APIs, generated from the
specs in [`../common`](../common):

- `packages/runtime` (`@mybicocca/data-api-runtime`) — the hand-written runtime every SDK uses
  (dates, lenient decoding, requests over `fetch`)
- `packages/affluences` (`@mybicocca/affluences`), `packages/easystaff`
  (`@mybicocca/easystaff`), `packages/easystaff-planning` (`@mybicocca/easystaff-planning`),
  `packages/esse3` (`@mybicocca/esse3`), `packages/moodle` (`@mybicocca/moodle`) and
  `packages/moodle-web` (`@mybicocca/moodle-web`) and `packages/kaltura` (`@mybicocca/kaltura`) — generated sources in `src/generated`,
  clients included; Portale Planning adds a few hand-written helpers

```ts
import { createEsse3Api } from "@mybicocca/esse3";

const esse3 = createEsse3Api({ headers: { Authorization: "Basic …" } });
const careers = await esse3.careers.getCareers({ userId: "…" });
```

Type-check with `npm run typecheck`. Regenerate the SDKs with the generator in
[`../common/codegen`](../common/codegen).
