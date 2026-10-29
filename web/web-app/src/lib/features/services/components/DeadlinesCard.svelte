<script lang="ts">
	import IconBell from '~icons/ph/bell-simple';
	import IconBellRinging from '~icons/ph/bell-simple-ringing';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconCloudWarning from '~icons/ph/cloud-warning';
	import { now } from '#lib/data/clock.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import type { DeadlinesQuery } from '../deadlines.svelte.ts';
	import { staleLine, streamList, summaryParts } from '../labels.ts';

	interface Props {
		deadlines: DeadlinesQuery;
		/** banner: the hub's pinned strip; nav: the slimmer card atop the services list. */
		variant?: 'banner' | 'nav';
		/** The deadlines page is open. */
		current?: boolean;
	}

	let { deadlines, variant = 'banner', current = false }: Props = $props();

	/** Nothing to show, or an empty list that can't vouch for the sources it lacks. */
	const failed = $derived(
		deadlines.status === 'error' || (deadlines.data?.length === 0 && !deadlines.complete)
	);
	const parts = $derived(
		summaryParts({
			status: deadlines.status,
			complete: deadlines.complete,
			deadlines: deadlines.data
		})
	);
	/** What the figures can't vouch for: a source that never loaded, or a refresh that failed. */
	const caveat = $derived.by(() => {
		if (failed || deadlines.data === undefined) return undefined;
		if (deadlines.missing.length > 0)
			return {
				label: m.services_card_partial(),
				hint: m.services_deadlines_partial({ sources: streamList(deadlines.missing) })
			};
		if (deadlines.stale && deadlines.updatedAt !== undefined)
			return {
				label: m.services_card_stale(),
				hint: staleLine(deadlines.updatedAt, now.current)
			};
		return undefined;
	});
	const urgent = $derived((deadlines.summary?.urgent ?? 0) > 0);
	const Glyph = $derived(failed ? IconCloudWarning : urgent ? IconBellRinging : IconBell);
</script>

<a
	class={['card', variant, { current, urgent }]}
	href={shellHref('services/deadlines')}
	aria-current={current ? 'page' : undefined}
>
	<span class="tile" aria-hidden="true"><Glyph /></span>
	<span class="text">
		<!-- The caveat sits beside the title, which it never outgrows, so the card keeps its height. -->
		<span class="head">
			<span class="title">{m.shell_service_deadlines()}</span>
			{#if caveat}
				<span class="caveat" {@attach tooltip(caveat.hint)}>
					<IconCloudWarning aria-hidden="true" />
					<span class={{ 'sr-only': variant === 'nav' }}>{caveat.label}</span>
				</span>
			{/if}
		</span>
		<span class="line" aria-live="polite">
			{#each parts as part, index (index)}
				{#if index > 0}<span class="separator" aria-hidden="true">&nbsp;·</span>{/if}
				<span class="part">{part}</span>
			{/each}
		</span>
	</span>
	{#if variant === 'banner'}
		<span class="go" aria-hidden="true"><IconArrowRight /></span>
	{/if}
</a>

<style>
	.card {
		position: relative;
		display: flex;
		align-items: center;
		gap: 14px;
		padding: 16px 16px 16px 14px;
		border-radius: var(--r-xl);
		background: var(--container);
		color: var(--on-container);
		box-shadow: var(--inner-light);
		text-decoration: none;
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.card:hover {
			background: var(--container-hover);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.card:active {
		transform: scale(0.985);
		transition-duration: var(--dur-instant);
	}

	.tile {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 44px;
		block-size: 44px;
		border-radius: 15px;
		background: color-mix(in oklch, currentColor 11%, transparent);
	}

	.tile :global(svg) {
		inline-size: 24px;
		block-size: 24px;
	}

	.urgent .tile {
		background: var(--accent);
		color: var(--on-accent);
	}

	.text {
		display: grid;
		flex: 1;
		gap: 1px;
		min-inline-size: 0;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-title-s);
		font-size: 1.1875rem;
		letter-spacing: -0.015em;
	}

	.caveat {
		display: inline-flex;
		align-items: center;
		gap: 4px;
		flex-shrink: 0;
		min-block-size: 20px;
		padding: 0 7px 0 5px;
		border-radius: var(--r-xs);
		background: var(--warning-container);
		color: var(--on-warning-container);
		font: 600 0.75rem / 1 var(--font-ui);
		white-space: nowrap;
	}

	.caveat :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.line {
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		opacity: 0.86;
		text-wrap: pretty;
	}

	.go {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 36px;
		block-size: 36px;
		border-radius: var(--r-pill);
		background: color-mix(in oklch, currentColor 11%, transparent);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.go :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (hover: hover) {
		.card:hover .go {
			transform: translateX(2px);
		}
	}

	/* On the narrowest phones the arrow gives its room to the summary, which then fits one line. */
	@container page (width < 340px) {
		.go {
			display: none;
		}
	}

	.nav {
		gap: 12px;
		padding: 12px;
		border-radius: var(--r-lg);
	}

	.nav .tile {
		inline-size: 40px;
		block-size: 40px;
		border-radius: 13px;
	}

	.nav .tile :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.nav .title {
		font-size: 1rem;
	}

	.nav .caveat {
		padding-inline: 4px;
	}

	/*
	 * Narrow beside the list, each figure takes a line of its own ("2 urgenti", then "Prossima il
	 * 29 ott"), and the card always keeps two, so the list below doesn't move when "Loading…"
	 * turns into the summary.
	 */
	.nav .line {
		min-block-size: 2lh;
		font: var(--type-meta);
	}

	.nav .part {
		display: block;
		text-wrap: balance;
	}

	.nav .separator {
		display: none;
	}

	.current {
		box-shadow:
			var(--inner-light),
			inset 0 0 0 1.5px color-mix(in oklch, var(--accent) 55%, transparent);
	}
</style>
