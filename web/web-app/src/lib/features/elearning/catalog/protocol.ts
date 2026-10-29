import type { CatalogLevel, CatalogRoot, CatalogSearchResult } from './model.ts';

/** What the page asks the catalog worker; `id` pairs each answer with its question. */
export type CatalogRequest =
	| { id: number; type: 'root' }
	| { id: number; type: 'level'; key: string }
	| { id: number; type: 'search'; query: string; scope: string | null };

export type CatalogResponse =
	| { id: number; type: 'root'; root: CatalogRoot }
	/** null: the catalog has no such category (an old link). */
	| { id: number; type: 'level'; level: CatalogLevel | null }
	| { id: number; type: 'search'; result: CatalogSearchResult }
	/** The catalog couldn't be read. */
	| { id: number; type: 'error'; message: string };
