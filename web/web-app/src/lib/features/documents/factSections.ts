import type { Fact } from '#lib/ui/Facts.svelte';
import type { Icon } from '#lib/ui/types.ts';

/** One topic of a detail page: a heading with its icon over a card of facts. */
export interface FactSection {
	id: string;
	title: string;
	icon: Icon;
	facts: Fact[];
	/** Free text shown as a paragraph rather than as facts. */
	note?: string;
}

/** Facts for each present value, in the order added. */
export class FactList {
	readonly items: Fact[] = [];

	add(label: string, value: string | null | undefined, code = false): this {
		if (value !== null && value !== undefined && value !== '') {
			this.items.push(code ? { label, value, code } : { label, value });
		}
		return this;
	}
}
