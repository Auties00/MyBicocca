import { buildCatalogIndex, catalogLevel, catalogRoot, type CatalogIndex } from './index.ts';
import type { CatalogRequest, CatalogResponse } from './protocol.ts';
import { searchCatalog } from './search.ts';
import { loadCatalog } from './source.ts';

/*
 * Holds the bundled catalog (22,000 courses, 3.6 MB) off the main thread: it decodes and folds
 * it once, then answers the page's small questions: the root tiles, one level, one search.
 */

/** The worker's global scope, typed without the webworker lib, which clashes with the DOM's. */
const worker = self as unknown as {
	addEventListener(type: 'message', listener: (event: MessageEvent<CatalogRequest>) => void): void;
	postMessage(message: CatalogResponse): void;
};

let loading: Promise<CatalogIndex> | undefined;

function catalog(): Promise<CatalogIndex> {
	loading ??= loadCatalog()
		.then(buildCatalogIndex)
		.catch((error: unknown) => {
			loading = undefined;
			throw error;
		});
	return loading;
}

async function answer(request: CatalogRequest): Promise<CatalogResponse> {
	const index = await catalog();
	switch (request.type) {
		case 'root':
			return { id: request.id, type: 'root', root: catalogRoot(index) };
		case 'level':
			return { id: request.id, type: 'level', level: catalogLevel(index, request.key) ?? null };
		case 'search':
			return {
				id: request.id,
				type: 'search',
				result: searchCatalog(index, request.query, request.scope)
			};
	}
}

worker.addEventListener('message', (event) => {
	const request = event.data;
	answer(request).then(
		(response) => worker.postMessage(response),
		(error: unknown) =>
			worker.postMessage({
				id: request.id,
				type: 'error',
				message: error instanceof Error ? error.message : String(error)
			})
	);
});
