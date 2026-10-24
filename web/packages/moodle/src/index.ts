import type { RestConfiguration } from "@mybicocca/data-api-runtime";
import { MoodleClient } from "./client.js";
import { MoodleApi } from "./generated/index.js";

export * from "./client.js";
export * from "./generated/index.js";

/** Creates the Moodle API with its own client, formatting strings in `language` (such as `it`). */
export function createMoodleApi(configuration: Partial<RestConfiguration> = {}, language = "en"): MoodleApi {
  return new MoodleApi(new MoodleClient(configuration, language));
}
