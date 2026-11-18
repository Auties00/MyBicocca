# MyBicocca for the web

The future web app. For now it holds the TypeScript SDKs of the data APIs, generated from the
specs in [`../common`](../common):

- `packages/runtime` (`@mybicocca/data-api-runtime`) — the hand-written runtime every SDK uses
  (dates, lenient decoding, requests over `fetch`)
- `packages/affluences` (`@mybicocca/affluences`), `packages/easystaff-planning`
  (`@mybicocca/easystaff-planning`), `packages/esse3` (`@mybicocca/esse3`) and
  `packages/moodle` (`@mybicocca/moodle`) — generated sources in `src/generated` plus a
  hand-written `client.ts` each

```ts
import { createEsse3Api } from "@mybicocca/esse3";

const esse3 = createEsse3Api({ headers: { Authorization: "Basic …" } });
const careers = await esse3.careers.getCareers({ userId: "…" });
```

Type-check with `npm run typecheck`. Regenerate the SDKs with the generator in
[`../common/codegen`](../common/codegen).
