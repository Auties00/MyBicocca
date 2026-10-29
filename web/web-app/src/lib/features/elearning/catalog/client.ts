import type { CatalogLevel, CatalogRoot, CatalogSearchResult } from './model.ts';
import type { CatalogRequest, CatalogResponse } from './protocol.ts';

type Question =
	| { type: 'root' }
	| { type: 'level'; key: string }
	| { type: 'search'; query: string; scope: string | null };

type Answer<Q extends Question['type']> = Extract<CatalogResponse, { type: Q }>;

/** The worker is let go this long after the last page using it leaves. */
const IDLE_MS = 60_000;

/**
 * The page's side of the catalog worker. The worker starts with the first page that uses it and
 * stays while pages use it, plus a minute, so going back and forth costs one decode.
 */
class CatalogClient {
	#worker: Worker | undefined;
	#next = 1;
	readonly #pending = new Map<
		number,
		{ resolve: (response: CatalogResponse) => void; reject: (error: Error) => void }
	>();
	#users = 0;
	#idle: ReturnType<typeof setTimeout> | undefined;

	/** Keeps the worker alive until the returned function is called. */
	use(): () => void {
		this.#users++;
		clearTimeout(this.#idle);
		let released = false;
		return () => {
			if (released) return;
			released = true;
			this.#users--;
			if (this.#users === 0) this.#idle = setTimeout(() => this.#stop(), IDLE_MS);
		};
	}

	root(): Promise<CatalogRoot> {
		return this.#ask({ type: 'root' }).then((answer) => answer.root);
	}

	level(key: string): Promise<CatalogLevel | null> {
		return this.#ask({ type: 'level', key }).then((answer) => answer.level);
	}

	search(query: string, scope: string | null): Promise<CatalogSearchResult> {
		return this.#ask({ type: 'search', query, scope }).then((answer) => answer.result);
	}

	#ask<Q extends Question>(question: Q): Promise<Answer<Q['type']>> {
		const worker = this.#start();
		const id = this.#next++;
		return new Promise((resolve, reject) => {
			this.#pending.set(id, {
				resolve: (response) => {
					if (response.type === 'error') reject(new Error(response.message));
					else resolve(response as Answer<Q['type']>);
				},
				reject
			});
			worker.postMessage({ ...question, id } as CatalogRequest);
		});
	}

	#start(): Worker {
		if (this.#worker) return this.#worker;
		const worker = new Worker(new URL('./catalog.worker.ts', import.meta.url), {
			type: 'module',
			name: 'catalog'
		});
		worker.addEventListener('message', (event: MessageEvent<CatalogResponse>) => {
			const pending = this.#pending.get(event.data.id);
			this.#pending.delete(event.data.id);
			pending?.resolve(event.data);
		});
		worker.addEventListener('error', (event) => {
			event.preventDefault();
			this.#fail(new Error(event.message || 'The catalog worker failed to start'));
		});
		this.#worker = worker;
		return worker;
	}

	#fail(error: Error): void {
		for (const pending of this.#pending.values()) pending.reject(error);
		this.#pending.clear();
		this.#worker?.terminate();
		this.#worker = undefined;
	}

	#stop(): void {
		if (this.#users > 0 || this.#pending.size > 0) return;
		this.#worker?.terminate();
		this.#worker = undefined;
	}
}

export const catalogClient = new CatalogClient();
