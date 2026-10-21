import {
  DecodeError,
  scriptResponse,
  scrapeResponse,
  sendRequest,
  unwrapEnvelope,
  type RestConfiguration,
  type RestRequest,
  type ScrapeNode,
} from "@mybicocca/data-api-runtime";

/** A request Agenda Web or EasyBadge answered with an error status. */
export class EasyStaffError extends Error {
  override readonly name = "EasyStaffError";

  constructor(readonly status: number) {
    super(`EasyStaff request failed with status ${status}`);
  }
}

/**
 * Sends the generated Agenda Web and EasyBadge requests and reads their responses: JSON, the
 * JSON a script assigns to a variable (as `combo.php` declares the options of its forms), or an
 * HTML page read by one of the scrapers.
 *
 * Responses are read whatever their content type, since Agenda Web answers JSON as `text/html`.
 */
export class EasyStaffClient {
  readonly configuration: RestConfiguration;

  constructor(configuration: Partial<RestConfiguration> = {}) {
    this.configuration = { baseUrl: "https://gestioneorari.didattica.unimib.it", ...configuration };
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

  async script<T>(request: RestRequest, variable: string, decode: (json: unknown) => T | undefined): Promise<T> {
    return scriptResponse(await this.execute(request), variable, decode);
  }

  async html<T>(request: RestRequest, scrape: (document: ScrapeNode) => T | undefined): Promise<T> {
    return scrapeResponse(await this.execute(request), scrape);
  }

  private async body(request: RestRequest): Promise<unknown> {
    const response = await this.execute(request);
    return unwrapEnvelope(JSON.parse(await response.text()), request.envelope ?? []);
  }

  private async execute(request: RestRequest): Promise<Response> {
    const response = await sendRequest(this.configuration, request);
    if (!response.ok) throw new EasyStaffError(response.status);
    return response;
  }
}
