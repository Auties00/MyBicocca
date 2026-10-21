import { scrapeResponse, sendRequest, type RestConfiguration, type RestRequest, type ScrapeNode } from "@mybicocca/data-api-runtime";

/** A page Moodle answered with an error status. */
export class MoodleWebError extends Error {
  override readonly name = "MoodleWebError";

  constructor(readonly status: number) {
    super(`Moodle page request failed with status ${status}`);
  }
}

/**
 * Fetches the Moodle pages the generated modules read and reads them with the scrapers.
 *
 * The catalog pages are read as an anonymous visitor; the media pages need the user's
 * `MoodleSession` cookie, which a browser sends for its own Moodle session.
 */
export class MoodleWebClient {
  readonly configuration: RestConfiguration;

  constructor(configuration: Partial<RestConfiguration> = {}) {
    this.configuration = { baseUrl: "https://elearning.unimib.it", ...configuration };
  }

  async html<T>(request: RestRequest, scrape: (document: ScrapeNode) => T | undefined): Promise<T> {
    const response = await sendRequest(this.configuration, request);
    if (!response.ok) throw new MoodleWebError(response.status);
    return scrapeResponse(response, scrape);
  }
}
