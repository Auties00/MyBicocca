<script lang="ts">
	import { page } from '$app/state';
	import { m } from '#lib/paraglide/messages.js';
	import { primaryDestinations, type DestinationId } from '#lib/ui/destinations.ts';
	import { getPageChrome } from '#lib/ui/pageChrome.svelte.ts';
	import { activeDestination } from './nav.ts';
	import { shownPath } from './pending.svelte.ts';
	import { reselectDestination } from './reselect.ts';
	import { keepCurrentDestination } from './tabMemory.svelte.ts';

	interface Props {
		destinationHref: (id: DestinationId) => string;
	}

	let { destinationHref }: Props = $props();

	const active = $derived(activeDestination(page.url.pathname));
	/* A tap is answered at once: the destination being loaded already shows as current. */
	const shown = $derived(activeDestination(shownPath()));
	const index = $derived(primaryDestinations.findIndex((d) => d.id === shown));
	const reselect = reselectDestination(getPageChrome());
</script>

<!-- data-shell-tabbar: base.css keeps focus and anchor scrolling clear of the bar while it is here. -->
<nav
	class="tabbar"
	aria-label={m.shell_nav_label()}
	style:--index={Math.max(0, index)}
	style:--tab-count={primaryDestinations.length}
	data-shell-tabbar
>
	<span class={['indicator', { hidden: index < 0 }]} aria-hidden="true"></span>
	{#each primaryDestinations as destination (destination.id)}
		{@const current = destination.id === shown}
		<!-- eslint-disable svelte/no-navigation-without-resolve -- a resolved shell path or the URL last shown -->
		<a
			class={['tab', { current }]}
			href={destinationHref(destination.id)}
			data-shell-destination
			onclick={destination.id === active ? reselect : keepCurrentDestination}
			aria-current={destination.id === active ? 'page' : undefined}
		>
			<!-- eslint-enable svelte/no-navigation-without-resolve -->
			<span class="icon" aria-hidden="true">
				<destination.icon class="regular" />
				<destination.iconActive class="filled" />
			</span>
			<span class="label">{destination.label()}</span>
		</a>
	{/each}
</nav>

<style>
	.tabbar {
		position: fixed;
		inset-inline: 0;
		inset-block-end: max(10px, var(--safe-bottom));
		z-index: var(--z-nav);
		display: grid;
		grid-template-columns: repeat(var(--tab-count), minmax(0, 1fr));
		inline-size: max-content;
		min-inline-size: min(calc(64px * var(--tab-count) + 8px), 100% - 20px);
		max-inline-size: calc(100% - 20px);
		block-size: var(--tabbar-h);
		margin-inline: auto;
		padding-inline: 4px;
		border-radius: var(--r-xl);
		background: var(--veil-raised);
		-webkit-backdrop-filter: var(--veil-filter);
		backdrop-filter: var(--veil-filter);
		box-shadow: var(--elev-3), var(--inner-light);
		view-transition-name: shell-tabbar;
	}

	.indicator {
		position: absolute;
		inset-block-start: 8px;
		inset-inline-start: 4px;
		inline-size: calc((100% - 8px) / var(--tab-count));
		block-size: 30px;
		transform: translateX(calc(var(--index) * 100%));
		transition:
			transform var(--dur-spring) var(--spring-standard),
			opacity var(--dur-fast) var(--ease-out);
		pointer-events: none;
	}

	.indicator::before {
		content: '';
		position: absolute;
		inset-block: 0;
		inset-inline: max(0px, (100% - 56px) / 2);
		border-radius: var(--r-pill);
		background: var(--container);
	}

	.hidden {
		opacity: 0;
	}

	.tab {
		position: relative;
		display: grid;
		justify-items: center;
		align-content: start;
		gap: 3px;
		padding-block-start: 8px;
		padding-inline: 6px;
		color: var(--text-muted);
		font: 600 0.6875rem / 1.2 var(--font-ui);
		letter-spacing: 0.01em;
		text-decoration: none;
		touch-action: manipulation;
		-webkit-user-select: none;
		user-select: none;
	}

	.tab:focus-visible {
		outline-offset: -4px;
		border-radius: var(--r-lg);
	}

	.icon {
		display: grid;
		place-items: center;
		block-size: 30px;
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.icon :global(svg) {
		grid-area: 1 / 1;
		inline-size: 22px;
		block-size: 22px;
		transition: opacity 120ms var(--ease-out);
	}

	.icon :global(.filled),
	.current .icon :global(.regular) {
		opacity: 0;
	}

	.current .icon :global(.filled) {
		opacity: 1;
	}

	.tab:active .icon {
		transform: scale(0.88);
		transition-duration: var(--dur-instant);
	}

	.current {
		color: var(--text);
	}

	.current .icon {
		color: var(--on-container);
	}

	.label {
		max-inline-size: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	@media print {
		.tabbar {
			display: none;
		}
	}
</style>
