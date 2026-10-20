import { DecodeError, sendRequest, type RestConfiguration, type RestRequest } from "@mybicocca/data-api-runtime";

/** The roles Esse3 operations may require, as declared by their specs. */
export type Esse3PermissionLevel =
  | "ANY"
  | "AUTHENTICATED_USER"
  | "TEACHER"
  | "PROVISIONAL_ENROLLED_STUDENT"
  | "REGISTERED_USER"
  | "ADMIN_OFFICE"
  | "ADMIN_OFFICE_ADMIN"
  | "EXTERNAL_SUBJECT"
  | "STUDENT"
  | "TECHNICAL_USER"
  | "UNKNOWN";

/** The error body Esse3 sends with a failed request. */
export interface Esse3ErrorResponse {
  statusCode?: number;
  retCode?: number;
  retErrMsg?: string;
  errDetails?: { errorType: string; value?: string; rawValue?: string }[];
}

/** A request Esse3 rejected. */
export class Esse3Error extends Error {
  override readonly name = "Esse3Error";

  constructor(
    readonly status: number,
    /** The roles the operation requires, a hint when the status is 401 or 403. */
    readonly expectedRoles: readonly Esse3PermissionLevel[],
    readonly response: Esse3ErrorResponse,
  ) {
    super(
      [
        `Esse3 request failed with status ${status}`,
        expectedRoles.length > 0 ? `Expected roles: ${expectedRoles.join(", ")}` : undefined,
        response.retErrMsg ? `Message: ${response.retErrMsg}` : undefined,
      ]
        .filter((line) => line !== undefined)
        .join("\n"),
    );
  }
}

/** Sends the generated Esse3 operations' requests and decodes responses and errors. */
export class Esse3Client {
  static readonly defaultBaseUrl = "https://s3w.si.unimib.it/e3rest/api";

  readonly configuration: RestConfiguration;

  /** Set an `Authorization` header to call operations that need a logged-in user. */
  constructor(configuration: Partial<RestConfiguration> = {}) {
    this.configuration = { baseUrl: Esse3Client.defaultBaseUrl, ...configuration };
  }

  async json<T>(request: RestRequest, roles: readonly Esse3PermissionLevel[], decode: (json: unknown) => T | undefined): Promise<T> {
    const response = await this.execute(request, roles);
    const value = decode(await response.json());
    if (value === undefined) throw new DecodeError(`Unexpected response to ${request.method} ${request.path}`);
    return value;
  }

  /** Like {@link json} for list responses; Esse3 answers HTTP 404 for an empty list. */
  async jsonList<T>(request: RestRequest, roles: readonly Esse3PermissionLevel[], decodeItem: (json: unknown) => T | undefined): Promise<T[]> {
    const response = await sendRequest(this.configuration, request);
    if (response.status === 404) return [];
    await ensureSuccess(response, roles);
    const json: unknown = await response.json();
    if (!Array.isArray(json)) throw new DecodeError(`Expected a list in response to ${request.method} ${request.path}`);
    return json.map((item, index) => {
      const value = decodeItem(item);
      if (value === undefined) throw new DecodeError(`Unexpected item ${index} in response to ${request.method} ${request.path}`);
      return value;
    });
  }

  async bytes(request: RestRequest, roles: readonly Esse3PermissionLevel[]): Promise<Uint8Array> {
    const response = await this.execute(request, roles);
    return new Uint8Array(await response.arrayBuffer());
  }

  async send(request: RestRequest, roles: readonly Esse3PermissionLevel[]): Promise<void> {
    await this.execute(request, roles);
  }

  private async execute(request: RestRequest, roles: readonly Esse3PermissionLevel[]): Promise<Response> {
    const response = await sendRequest(this.configuration, request);
    await ensureSuccess(response, roles);
    return response;
  }
}

async function ensureSuccess(response: Response, roles: readonly Esse3PermissionLevel[]): Promise<void> {
  if (response.status < 400) return;
  const details = (await response.json().catch(() => undefined)) as Esse3ErrorResponse | undefined;
  throw new Esse3Error(response.status, roles, details ?? { statusCode: response.status, retCode: -1 });
}
