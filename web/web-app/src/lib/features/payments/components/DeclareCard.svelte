<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconBookOpen from '~icons/ph/book-open-text';
	import IconSeal from '~icons/ph/seal-check';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import { INPS_ISEE, ISEE_GUIDE } from '../format.ts';

	interface Props {
		/** The academic year whose ISEE can still be declared. */
		year: number;
		/** Narrow, beside an open year. */
		compact?: boolean;
	}

	let { year, compact = false }: Props = $props();

	const label = $derived(academicYearLabel(year));
	const headingId = $props.id();
</script>

<section class={['declare', { compact }]} aria-labelledby={headingId}>
	{#if !compact}<IconChip icon={IconSeal} tone="accent" size="lg" />{/if}
	<div class="text">
		<h2 id={headingId}>{m.payments_isee_declare_title({ year: label })}</h2>
		<p>{m.payments_isee_declare_body({ year: label })}</p>
	</div>
	<div class="buttons">
		<Button variant="solid" href={INPS_ISEE} trailingIcon={IconArrowSquareOut}>
			{m.payments_isee_declare()}
		</Button>
		<Button href={ISEE_GUIDE} icon={IconBookOpen}>{m.payments_isee_guide()}</Button>
	</div>
</section>

<style>
	.declare {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-3) var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--container);
		color: var(--on-container);
	}

	.compact {
		grid-template-columns: minmax(0, 1fr);
		padding: var(--sp-4);
	}

	.text {
		display: grid;
		gap: var(--sp-1);
	}

	h2 {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: balance;
	}

	p {
		max-inline-size: 64ch;
		font: var(--type-meta);
		text-wrap: pretty;
		opacity: 0.86;
	}

	.buttons {
		grid-column: 1 / -1;
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
	}

	@container payments (width >= 560px) {
		.declare:not(.compact) .buttons {
			grid-column: 2;
		}
	}
</style>
