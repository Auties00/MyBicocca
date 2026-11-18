import { DecodeError, asJsonObject, decodeString, sendRequest, type RestConfiguration, type RestRequest } from "@mybicocca/data-api-runtime";

/** A call Moodle rejected. Match on `code`, such as `invalidtoken`. */
export class MoodleError extends Error {
  override readonly name = "MoodleError";

  constructor(
    readonly code: string | undefined,
    message: string,
  ) {
    super(message);
  }
}

/**
 * Sends the generated Moodle web service calls and decodes their results and errors.
 *
 * Moodle answers errors with HTTP 200 and an `{"exception" | "error", "errorcode", "message"}`
 * body, which becomes a {@link MoodleError}. Calls also ask Moodle to filter text and to rewrite
 * file URLs so that they work with the token, as the Moodle mobile app does.
 */
export class MoodleClient {
  readonly configuration: RestConfiguration;

  /** @param language The langpack Moodle formats strings in, such as `it`. */
  constructor(
    configuration: Partial<RestConfiguration> = {},
    readonly language = "en",
  ) {
    this.configuration = { baseUrl: "https://elearning.unimib.it", ...configuration };
  }

  async json<T>(request: RestRequest, decode: (json: unknown) => T | undefined): Promise<T> {
    const value = decode(await this.execute(request));
    if (value === undefined) throw new DecodeError(`Unexpected result of ${wsFunction(request)}`);
    return value;
  }

  async jsonList<T>(request: RestRequest, decodeItem: (json: unknown) => T | undefined): Promise<T[]> {
    const json = await this.execute(request);
    if (!Array.isArray(json)) throw new DecodeError(`Expected a list as the result of ${wsFunction(request)}`);
    return json.map((item, index) => {
      const value = decodeItem(item);
      if (value === undefined) throw new DecodeError(`Unexpected item ${index} in the result of ${wsFunction(request)}`);
      return value;
    });
  }

  async send(request: RestRequest): Promise<void> {
    await this.execute(request);
  }

  private async execute(request: RestRequest): Promise<unknown> {
    const response = await sendRequest(this.configuration, {
      ...request,
      query: [
        ...(request.query ?? []),
        ["moodlewssettingfilter", "true"],
        ["moodlewssettingfileurl", "true"],
        ["moodlewssettinglang", this.language],
      ],
    });
    if (!response.ok) throw new MoodleError(undefined, `Request failed with status ${response.status}`);
    const text = await response.text();
    const json: unknown = text.trim() === "" ? null : JSON.parse(text);
    const error = errorOf(json);
    if (error !== undefined) throw error;
    return json;
  }
}

/** The error a result reports, if it is one. */
function errorOf(json: unknown): MoodleError | undefined {
  const object = asJsonObject(json);
  if (object === undefined) return undefined;
  const error = object["error"];
  if (!("exception" in object) && !("errorcode" in object) && error !== true && typeof error !== "string") return undefined;
  const message = decodeString(object["message"]) ?? (typeof error === "string" ? error : undefined) ?? "unknown error";
  return new MoodleError(decodeString(object["errorcode"]), message);
}

function wsFunction(request: RestRequest): string {
  return String(request.query?.find(([name]) => name === "wsfunction")?.[1] ?? request.path);
}
