import type { RestConfiguration } from "@mybicocca/data-api-runtime";
import { EasyStaffPlanningClient, EasyStaffPlanningPortal } from "./client.js";
import { EasyStaffPlanningApi, type EasyStaffPlanningParameters } from "./generated/index.js";

export * from "./client.js";
export * from "./generated/index.js";

/** Creates the Portale Planning API of a portal (the information desks by default) with its own client. */
export function createEasyStaffPlanningApi(
  portal: EasyStaffPlanningParameters = EasyStaffPlanningPortal.InformationDesks,
  configuration: Partial<RestConfiguration> = {},
): EasyStaffPlanningApi {
  return new EasyStaffPlanningApi(new EasyStaffPlanningClient(configuration), portal);
}
