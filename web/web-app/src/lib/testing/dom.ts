import { createRequire } from 'node:module';
import type { DOMParser as LinkedomParser } from 'linkedom';

/*
 * Vitest setup: tests run in Node, which has no DOMParser, so linkedom stands in for the browser's.
 * It is required on the first parse, so test files that read no markup never load it.
 */

const require = createRequire(import.meta.url);

class DocumentParser {
	parseFromString(markup: string, type: DOMParserSupportedType): Document {
		const { DOMParser } = require('linkedom') as { DOMParser: typeof LinkedomParser };
		// Unlike browsers, linkedom builds no <html> around a bare <body>.
		const document = /<html[\s>]/i.test(markup) ? markup : `<!doctype html><html>${markup}</html>`;
		return new DOMParser().parseFromString(document, type as 'text/html') as unknown as Document;
	}
}

globalThis.DOMParser ??= DocumentParser as unknown as typeof globalThis.DOMParser;
