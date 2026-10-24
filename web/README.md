# MyBicocca for the web

The future web app. For now it holds the TypeScript SDKs of the data APIs, generated from the
specs in [`../common`](../common):

- `packages/runtime` (`@mybicocca/data-api-runtime`) — the hand-written runtime every SDK uses
  (dates, lenient decoding, requests over `fetch`)
- `packages/esse3` (`@mybicocca/esse3`) — generated sources in `src/generated` plus the
  hand-written `client.ts`

```ts
import { createEsse3Api } from "@mybicocca/esse3";

const esse3 = createEsse3Api({ headers: { Authorization: "Basic …" } });
const careers = await esse3.careers.getCareers({ userId: "…" });
```

Type-check with `npm run typecheck`. Regenerate the SDKs with the generator in
[`../common/codegen`](../common/codegen).
