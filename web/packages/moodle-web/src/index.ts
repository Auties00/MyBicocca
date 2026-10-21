import type { RestConfiguration } from "@mybicocca/data-api-runtime";
import { MoodleWebClient } from "./client.js";
import { MoodleWebApi } from "./generated/index.js";

export * from "./client.js";
export * from "./generated/index.js";

/** Creates the Moodle web pages API with its own client. */
export function createMoodleWebApi(configuration: Partial<RestConfiguration> = {}): MoodleWebApi {
  return new MoodleWebApi(new MoodleWebClient(configuration));
}
