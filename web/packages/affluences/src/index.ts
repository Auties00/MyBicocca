import type { RestConfiguration } from "@mybicocca/data-api-runtime";
import { AffluencesClient } from "./client.js";
import { AffluencesApi } from "./generated/index.js";

export * from "./client.js";
export * from "./generated/index.js";

/** Creates the Affluences API with its own client. */
export function createAffluencesApi(configuration: Partial<RestConfiguration> = {}): AffluencesApi {
  return new AffluencesApi(new AffluencesClient(configuration));
}
