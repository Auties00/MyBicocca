<script lang="ts">
	import { goto } from '$app/navigation';
	import IconDoor from '~icons/ph/door';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import SearchField from '#lib/ui/SearchField.svelte';
	import { byCity, categoryCounts, type Building, type BuildingCategory } from '../catalog.ts';
	import { categoryIcons, categoryLabels } from '../labels.ts';
	import { buildingHref, roomHref } from '../links.ts';
	import { edgeFade } from '../edgeFade.ts';
	import { buildingName } from '../names.ts';
	import { buildingPlaceId } from '../search.ts';
	import { getMapView } from '../view.svelte.ts';
	import AliasBadge from './AliasBadge.svelte';
	import GroupHead from './GroupHead.svelte';
	import Level from './Level.svelte';
	import PlaceRow from './PlaceRow.svelte';

	interface Props {
		/** The room list is still on its way: the search covers buildings only. */
		roomsPending: boolean;
		onfilter: (filter: BuildingCategory[]) => void;
		/** Whether this level takes page transitions (not when a detail sits beside it). */
		transition?: boolean;
		/** The building open beside the list, on wide screens. */
		open?: string;
	}

	let { roomsPending, onfilter, transition = true, open }: Props = $props();

	const view = getMapView();
	const ROOM_HITS = 20;

	const counts = categoryCounts();
	const cities = $derived(byCity(view.shown));

	const query = $derived(view.query);
	const hits = $derived(view.hits);
	const buildingHits = $derived(hits.filter((hit) => hit.place.kind === 'building'));
	const roomHits = $derived(hits.flatMap((hit) => (hit.place.kind === 'room' ? [hit.place] : [])));
	/** The query whose rooms are all shown; a new query starts short again. */
	let expandedQuery = $state<string>();
	const shownRooms = $derived(expandedQuery === query ? roomHits : roomHits.slice(0, ROOM_HITS));

	function toggle(category: BuildingCategory, selected: boolean) {
		onfilter(
			[...counts.keys()].filter((entry) =>
				entry === category ? selected : view.filter.includes(entry)
			)
		);
	}

	/** Enter in the search opens the best match. */
	function openFirst() {
		const place = hits[0]?.place;
		if (!place) return;
		void goto(
			place.kind === 'room'
				? roomHref(place.building.code, place.room.code, view.filter)
				: buildingHref(place.building.code, view.filter)
		);
	}

	/* A row lights its building's pin; the pin lights its building's row, not its rooms'. */
	function hover(id: string, code: string, inside: boolean) {
		if (inside) {
			view.hovered = code;
			view.hoveredRow = id;
		} else if (view.hoveredRow === id) {
			view.hovered = undefined;
			view.hoveredRow = undefined;
		}
	}

	function lit(entry: Building): boolean {
		return view.hoveredRow === undefined
			? view.hovered === entry.code
			: view.hoveredRow === buildingPlaceId(entry.code);
	}
</script>

{#snippet buildingRow(entry: Building, description: string)}
	<PlaceRow
		place={buildingPlaceId(entry.code)}
		href={buildingHref(entry.code, view.filter)}
		title={buildingName(entry)}
		{description}
		highlighted={lit(entry)}
		selected={open === entry.code}
		onhover={(inside) => hover(buildingPlaceId(entry.code), entry.code, inside)}
	>
		{#snippet leading()}
			<AliasBadge building={entry} active={lit(entry) || open === entry.code} />
		{/snippet}
	</PlaceRow>
{/snippet}

<Level key="directory" {transition}>
	{#snippet head()}
		<!-- Beside an open building the directory is secondary: the building has the h1. -->
		<svelte:element this={open ? 'h2' : 'h1'} class="sr-only" tabindex="-1">
			{m.map_title()}
		</svelte:element>
		<SearchField label={m.map_search_label()} bind:value={view.search} onsearch={openFirst} />
		{#if counts.size > 1}
			<div class="chips" role="group" aria-label={m.map_filter_label()} {@attach edgeFade}>
				{#each counts as [category, count] (category)}
					<FilterChip
						label={categoryLabels[category]()}
						icon={categoryIcons[category]}
						{count}
						selected={view.filter.includes(category)}
						onchange={(selected) => toggle(category, selected)}
					/>
				{/each}
			</div>
		{/if}
	{/snippet}

	{#if query}
		{#if hits.length === 0}
			<EmptyState
				title={m.map_no_results({ query })}
				body={roomsPending ? m.map_rooms_searching() : m.map_no_results_body()}
			>
				{#snippet action()}
					{#if view.filter.length > 0}
						<Button variant="tonal" onclick={() => onfilter([])}>
							{m.map_filter_show_all()}
						</Button>
					{/if}
				{/snippet}
			</EmptyState>
		{:else}
			{#if buildingHits.length > 0}
				<GroupHead title={m.map_results_buildings()} meta={String(buildingHits.length)} />
				<ListGroup label={m.map_results_buildings()}>
					{#each buildingHits as { place } (place.id)}
						{@render buildingRow(
							place.building,
							`${place.building.address}, ${place.building.city}`
						)}
					{/each}
				</ListGroup>
			{/if}
			{#if roomHits.length > 0}
				<GroupHead title={m.map_results_rooms()} meta={String(roomHits.length)} />
				<ListGroup label={m.map_results_rooms()}>
					{#each shownRooms as place (place.id)}
						<PlaceRow
							place={place.id}
							href={roomHref(place.building.code, place.room.code, view.filter)}
							title={place.title}
							description={place.subtitle}
							meta={place.room.capacity === null
								? undefined
								: m.map_seat_count({ count: place.room.capacity })}
							highlighted={view.hoveredRow === place.id}
							onhover={(inside) => hover(place.id, place.building.code, inside)}
						>
							{#snippet leading()}<IconChip icon={IconDoor} />{/snippet}
						</PlaceRow>
					{/each}
				</ListGroup>
				{#if shownRooms.length < roomHits.length}
					<div class="more">
						<Button variant="ghost" onclick={() => (expandedQuery = query)}>
							{m.map_results_more({ count: roomHits.length })}
						</Button>
					</div>
				{/if}
			{:else if roomsPending}
				<p class="hint">{m.map_rooms_searching()}</p>
			{/if}
		{/if}
	{:else}
		<div class={['title', { beside: open }]}>
			<h2>{m.map_buildings_title()}</h2>
			<span class="count">{m.map_site_count({ count: view.shown.length })}</span>
			{#if view.filter.length > 0}
				<Button variant="ghost" size="sm" onclick={() => onfilter([])}>
					{m.map_filter_clear()}
				</Button>
			{/if}
		</div>
		{#each cities as [city, list] (city)}
			<GroupHead title={city} meta={String(list.length)} level={3} />
			<ListGroup label={city}>
				{#each list as entry (entry.code)}
					{@render buildingRow(entry, entry.address)}
				{/each}
			</ListGroup>
		{/each}
	{/if}
</Level>

<style>
	.chips {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-2);
		margin-block-start: var(--sp-3);
	}

	/* Phones keep the chips on one line, scrolling sideways under the search: an edge with more fades. */
	@media (width < 600px) {
		.chips {
			--fade-start: 0px;
			--fade-end: 0px;
			flex-wrap: nowrap;
			margin-inline: calc(-1 * var(--sp-4));
			padding: 2px var(--sp-4);
			overflow-x: auto;
			scrollbar-width: none;
			mask-image: linear-gradient(
				to right,
				transparent,
				#000 var(--fade-start),
				#000 calc(100% - var(--fade-end)),
				transparent
			);
		}

		.chips:global([data-more-start]) {
			--fade-start: 32px;
		}

		.chips:global([data-more-end]) {
			--fade-end: 32px;
		}
	}

	.title {
		display: flex;
		align-items: baseline;
		gap: var(--sp-3);
		min-block-size: 44px;
		padding: var(--sp-2) var(--sp-2) 0;
	}

	.title h2 {
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
	}

	/* Beside an open building or room, whose title is the view's serif line. */
	.beside h2 {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.count {
		margin-inline-start: auto;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.more {
		display: flex;
		justify-content: center;
		padding-block: var(--sp-2);
	}

	.hint {
		padding: var(--sp-4) var(--sp-2);
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
