<script lang="ts" module>
	import type { Icon } from './types.ts';

	export interface Fact {
		label: string;
		value: string;
		icon?: Icon;
		/** Codes and identifiers (matricola, IUV): tabular figures, slashed zero, never translated. */
		code?: boolean;
	}
</script>

<script lang="ts" generics="T extends Fact">
	import type { Snippet } from 'svelte';

	interface Props {
		/** Facts; extra fields on them reach the `value` snippet. */
		items: readonly T[];
		/** Accessible name of the list when no heading precedes it. */
		label?: string;
		/** Draws a fact's value, e.g. a code with a copy button; the text is drawn without it. */
		value?: Snippet<[T]>;
	}

	let { items, label, value }: Props = $props();
</script>

<div class="wrap">
	<dl class="facts" aria-label={label}>
		{#each items as fact (fact.label)}
			<div class={['fact', { 'with-icon': !!fact.icon }]}>
				<dt class="term">
					{#if fact.icon}<span class="icon"><fact.icon aria-hidden="true" /></span>{/if}
					<span class="label">{fact.label}</span>
				</dt>
				<dd class={['value', { code: fact.code }]} translate={fact.code ? 'no' : undefined}>
					{#if value}{@render value(fact)}{:else}{fact.value}{/if}
				</dd>
			</div>
		{/each}
	</dl>
</div>

<style>
	.wrap {
		container-type: inline-size;
	}

	/* One card; cells are divided by hairlines drawn to their right and bottom, clipped at the edges. */
	.facts {
		display: grid;
		grid-template-columns: 1fr;
		margin: 0;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		overflow: hidden;
	}

	@container (width >= 440px) {
		.facts {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}
	}

	@container (width >= 760px) {
		.facts {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}
	}

	.fact {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		grid-template-areas:
			'icon label'
			'icon value';
		align-items: center;
		column-gap: 12px;
		padding: 12px 16px;
		box-shadow:
			1px 0 0 var(--hairline),
			0 1px 0 var(--hairline);
	}

	.term {
		display: contents;
	}

	.fact:not(.with-icon) {
		grid-template-columns: minmax(0, 1fr);
		grid-template-areas:
			'label'
			'value';
	}

	.icon {
		grid-area: icon;
		display: grid;
		place-items: center;
		inline-size: 36px;
		block-size: 36px;
		border-radius: 11px;
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.icon :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.label {
		grid-area: label;
		color: var(--text-muted);
		font: var(--type-label);
		letter-spacing: 0.01em;
	}

	.value {
		grid-area: value;
		margin: 0;
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		overflow-wrap: anywhere;
	}

	.code {
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.02em;
	}
</style>
