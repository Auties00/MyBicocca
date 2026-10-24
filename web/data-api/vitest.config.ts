import { defineConfig } from "vitest/config";

// The live tests, generated from each API's tests.yaml into test/, call the real services: see
// "Live tests" in common/codegen/README.md. They share one process, so that every file sees the
// same throttle, request budget and values of other calls.
export default defineConfig({
  test: {
    include: ["test/**/*.live.test.ts"],
    setupFiles: ["test/runtime/setup.ts"],
    pool: "forks",
    maxWorkers: 1,
    isolate: false,
    fileParallelism: false,
    testTimeout: 300_000,
    // This config only runs the live tests, so running it means running them.
    env: { LIVE_TESTS: "1" },
  },
});
