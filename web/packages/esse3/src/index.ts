import type { RestConfiguration } from "@mybicocca/data-api-runtime";
import { Esse3Client } from "./client.js";
import { Esse3Api } from "./generated/index.js";

export * from "./client.js";
export * from "./generated/index.js";

/** Creates the Esse3 API with its own client. */
export function createEsse3Api(configuration: Partial<RestConfiguration> = {}): Esse3Api {
  return new Esse3Api(new Esse3Client(configuration));
}
