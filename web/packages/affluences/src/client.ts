import { DecodeError, asJsonObject, decodeString, sendRequest, unwrapEnvelope, type RestConfiguration, type RestRequest } from "@mybicocca/data-api-runtime";

/** A request Affluences rejected. Match on `code` (or `status`), never on the localised message. */
export class AffluencesError extends Error {
  /** Polling an email validation the user hasn't completed yet. */
  static readonly notValidatedYet = "does_not_exist";

  override readonly name = "AffluencesError";

  constructor(
    readonly status: number,
    readonly code: string | undefined,
    message: string,
  ) {
    super(message);
  }
}

/**
 * Sends the generated Affluences operations' requests and decodes responses and errors. Every
 * operation names the server it targets, so the configuration's base URL is only a fallback.
 */
export class AffluencesClient {
  readonly configuration: RestConfiguration;

  constructor(configuration: Partial<RestConfiguration> = {}) {
    this.configuration = {
      baseUrl: "https://api.affluences.com/app/v4",
      ...configuration,
      headers: { "Accept-Language": "it", ...configuration.headers },
    };
  }

  async json<T>(request: RestRequest, decode: (json: unknown) => T | undefined): Promise<T> {
    const value = decode(await this.body(request));
    if (value === undefined) throw new DecodeError(`Unexpected response to ${request.method} ${request.path}`);
    return value;
  }

  async jsonList<T>(request: RestRequest, decodeItem: (json: unknown) => T | undefined): Promise<T[]> {
    const json = await this.body(request);
    if (!Array.isArray(json)) throw new DecodeError(`Expected a list in response to ${request.method} ${request.path}`);
    return json.map((item, index) => {
      const value = decodeItem(item);
      if (value === undefined) throw new DecodeError(`Unexpected item ${index} in response to ${request.method} ${request.path}`);
      return value;
    });
  }

  async bytes(request: RestRequest): Promise<Uint8Array> {
    return new Uint8Array(await (await this.execute(request)).arrayBuffer());
  }

  async send(request: RestRequest): Promise<void> {
    await this.execute(request);
  }

  private async body(request: RestRequest): Promise<unknown> {
    return unwrapEnvelope(await (await this.execute(request)).json(), request.envelope ?? []);
  }

  private async execute(request: RestRequest): Promise<Response> {
    const response = await sendRequest(this.configuration, request);
    if (!response.ok) throw await errorOf(response);
    return response;
  }
}

/**
 * Reads either error envelope: `{"error": {"code", "message"}}` from the app API, or
 * `{"error": "…", "errorMessage" | "error_message": "…"}` from the reservation API.
 */
async function errorOf(response: Response): Promise<AffluencesError> {
  const envelope = asJsonObject(await response.json().catch(() => undefined));
  const error = envelope?.["error"];
  const details = asJsonObject(error);
  const code = details !== undefined ? decodeString(details["code"]) : decodeString(error);
  const message = details !== undefined
    ? decodeString(details["message"])
    : decodeString(envelope?.["errorMessage"]) ?? decodeString(envelope?.["error_message"]);
  return new AffluencesError(response.status, code, message ?? code ?? `Request failed with status ${response.status}`);
}
