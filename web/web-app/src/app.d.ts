// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}

	/**
	 * Whether this is the demo build (`vite --mode demo`): it opens straight into the demo account,
	 * whose every request the fixtures in `#lib/demo` answer. Replaced by a literal at build time,
	 * so other builds drop each `__DEMO__ ? import(...) : ...` and ship none of the demo.
	 */
	const __DEMO__: boolean;
}

export {};
