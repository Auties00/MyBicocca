<script lang="ts">
	import IconBooks from '~icons/ph/books-duotone';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { romeWallClock } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Card from '#lib/ui/Card.svelte';
	import { statusChangeLabel } from '../format.ts';
	import { libraryHref } from '../links.ts';
	import { toLiveStatus, type Library } from '../model.ts';
	import { refreshWhileVisible } from '../live.svelte.ts';
	import { liveDataQuery } from '../queries.ts';
	import OccupancyRing from './OccupancyRing.svelte';

	interface Props {
		library: Library;
		/** Images above the fold load at once. */
		eager?: boolean;
	}

	let { library, eager = false }: Props = $props();

	/* The listing tells only open or closed; the live data adds when that changes. */
	const live = query(() => session.current && liveDataQuery(session.current, library.id));
	refreshWhileVisible(() => live.refresh());
	const status = $derived(live.data ? toLiveStatus(live.data) : undefined);
	const open = $derived(status?.open ?? library.open);
	const occupancy = $derived(status ? status.occupancy : library.occupancy);
	const change = $derived(
		status ? statusChangeLabel(status, romeWallClock(Date.now()).date) : null
	);

	let broken = $state(false);
	const openLabel = $derived(open === null ? null : open ? m.library_open() : m.library_closed());
</script>

<Card padding="none" href={libraryHref(library.id)}>
	<div class="photo">
		{#if library.picture && !broken}
			<img
				src={library.picture}
				alt=""
				width="640"
				height="320"
				loading={eager ? 'eager' : 'lazy'}
				decoding="async"
				onerror={() => (broken = true)}
			/>
		{:else}
			<span class="placeholder"><IconBooks aria-hidden="true" /></span>
		{/if}
	</div>
	<div class="body">
		<div class="text">
			<h3 class="name">{library.name}</h3>
			{#if library.place}<p class="place">{library.place}</p>{/if}
			{#if openLabel}
				<p class="status">
					<span class={['dot', { open }]} aria-hidden="true"></span>
					<span class={['state', { open }]}>{openLabel}</span>
					{#if change}<span class="change">· {change}</span>{/if}
				</p>
			{/if}
		</div>
		{#if open && occupancy !== null}
			<OccupancyRing percent={occupancy} />
		{/if}
	</div>
</Card>

<style>
	.photo {
		position: relative;
		aspect-ratio: 2 / 1;
		overflow: hidden;
		border-radius: inherit;
		border-end-start-radius: 0;
		border-end-end-radius: 0;
		background: var(--surface-sunken);
	}

	img,
	.placeholder {
		display: block;
		inline-size: 100%;
		block-size: 100%;
		object-fit: cover;
	}

	.placeholder {
		display: grid;
		place-items: center;
		background:
			radial-gradient(circle at 30% 20%, var(--container), transparent 60%), var(--surface-sunken);
		color: var(--accent-text);
	}

	.placeholder :global(svg) {
		inline-size: 40px;
		block-size: 40px;
	}

	.body {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		padding: 14px 16px 16px;
	}

	.text {
		display: grid;
		gap: 2px;
		flex: 1;
		min-inline-size: 0;
	}

	.name {
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.place {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.status {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 2px 6px;
		margin-block-start: 6px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.dot {
		inline-size: 8px;
		block-size: 8px;
		border-radius: 50%;
		background: var(--danger);
	}

	.dot.open {
		background: var(--success);
	}

	.state {
		color: var(--danger);
		font-weight: 600;
	}

	.state.open {
		color: var(--success);
	}

	.change {
		font-variant-numeric: tabular-nums;
	}
</style>
