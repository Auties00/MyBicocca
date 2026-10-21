import type { RestConfiguration } from "@mybicocca/data-api-runtime";
import { EasyStaffClient } from "./client.js";
import { EasyStaffApi } from "./generated/index.js";

export * from "./client.js";
export * from "./generated/index.js";

/** Creates the Agenda Web and EasyBadge API with its own client. */
export function createEasyStaffApi(configuration: Partial<RestConfiguration> = {}): EasyStaffApi {
  return new EasyStaffApi(new EasyStaffClient(configuration));
}
