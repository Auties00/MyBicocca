/**
 * Honest loading: a skeleton appears only if loading lasts longer than `delay`, and once shown it
 * stays at least `min` so it never flashes. Create it during component init:
 *
 * ```ts
 * const gate = new LoadingGate(() => query.loading && !query.data);
 * // {#if gate.visible}<Skeleton …/>{/if}
 * ```
 */
export class LoadingGate {
	visible = $state(false);

	constructor(loading: () => boolean, { delay = 150, min = 400 } = {}) {
		let shownAt = 0;
		$effect(() => {
			const active = loading();
			if (active && !this.visible) {
				const timer = setTimeout(() => {
					this.visible = true;
					shownAt = performance.now();
				}, delay);
				return () => clearTimeout(timer);
			}
			if (!active && this.visible) {
				const wait = Math.max(0, min - (performance.now() - shownAt));
				const timer = setTimeout(() => (this.visible = false), wait);
				return () => clearTimeout(timer);
			}
		});
	}
}
