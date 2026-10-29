<script lang="ts">
	import IconMapPin from '~icons/ph/map-pin';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import type { BuildingData } from '../buildingData.svelte.ts';
	import type { Building } from '../catalog.ts';
	import { floorLabel } from '../labels.ts';
	import { directionsHref, mapHref } from '../links.ts';
	import { buildingName, buildingTitle } from '../names.ts';
	import { groupByFloor, type RoomsCatalog } from '../rooms.ts';
	import { buildingPlaceId } from '../search.ts';
	import { getMapView } from '../view.svelte.ts';
	import AliasBadge from './AliasBadge.svelte';
	import GroupHead from './GroupHead.svelte';
	import Level from './Level.svelte';
	import LevelHeader from './LevelHeader.svelte';
	import RoomGrid from './RoomGrid.svelte';
	import RoomsSkeleton from './RoomsSkeleton.svelte';

	interface Props {
		building: Building;
		catalog: Query<RoomsCatalog>;
		data: BuildingData;
	}

	let { building, catalog, data }: Props = $props();

	const view = getMapView();
	let freeOnly = $state(false);

	const rooms = $derived(data.rooms);
	const free = $derived(
		rooms?.filter((room) => data.status(room.code)?.kind === 'free').length ?? 0
	);
	const shown = $derived(
		freeOnly ? rooms?.filter((room) => data.status(room.code)?.kind === 'free') : rooms
	);
	const floors = $derived(groupByFloor(shown ?? []));
	const unknownFloors = $derived(floors.length === 1 && floors[0].floor === null);
	/* The rooms wait for their floors, rather than regrouping under the reader's eyes. */
	const floorsKnown = $derived(
		data.sites?.length === 0 ||
			data.showcase.data !== undefined ||
			data.showcase.error !== undefined
	);
	const loading = $derived((rooms === undefined || !floorsKnown) && !catalog.error);
	const gate = new LoadingGate(() => loading);
	/* Once the list is in, the skeleton sketches as many rooms, in floors of up to eight. */
	const sketch = $derived.by(() => {
		if (rooms === undefined) return undefined;
		const counts: number[] = [];
		for (let left = rooms.length; left > 0; left -= 8) counts.push(Math.min(8, left));
		return counts;
	});
</script>

<Level key={buildingPlaceId(building.code)}>
	{#snippet head()}
		<LevelHeader
			title={buildingName(building)}
			subtitle={`${building.address}, ${building.city}`}
			back={{ href: mapHref(view.filter), label: m.map_back_to_buildings() }}
		>
			{#snippet leading()}<AliasBadge {building} size="lg" />{/snippet}
			{#snippet actions()}
				<Button
					variant="solid"
					size="sm"
					icon={IconMapPin}
					href={directionsHref(building)}
					label={m.map_directions_hint({ building: buildingTitle(building) })}
				>
					{m.map_directions()}
				</Button>
			{/snippet}
		</LevelHeader>
	{/snippet}

	{#if catalog.error && !catalog.data}
		<ErrorState
			message={`${m.map_rooms_load_failed()} ${errorMessage(catalog.error)}`}
			onretry={catalog.refresh}
			retrying={catalog.refreshing}
		/>
	{:else if rooms === undefined || loading || gate.visible}
		<RoomsSkeleton floors={sketch} />
	{:else if rooms.length === 0}
		<EmptyState size="panel" title={m.map_rooms_empty()} body={m.map_rooms_empty_body()} />
	{:else}
		{#if catalog.error}
			<div class="notice">
				<ErrorState
					variant="stale"
					message={errorMessage(catalog.error)}
					updatedAt={catalog.updatedAt === undefined ? undefined : new Date(catalog.updatedAt)}
					onretry={catalog.refresh}
					retrying={catalog.refreshing}
				/>
			</div>
		{/if}
		<div class="summary">
			<p class="counts">
				<span>{m.map_room_count({ count: rooms.length })}</span>
				{#if data.schedule}
					<span class="free">{m.map_free_now_count({ count: free })}</span>
				{/if}
			</p>
			<FilterChip label={m.map_free_only()} bind:selected={freeOnly} disabled={!data.schedule} />
		</div>
		{#if data.occupation.error && !data.occupation.data}
			<div class="notice">
				<Banner tone="warning" message={m.map_occupation_failed()}>
					{#snippet actions()}
						<Button
							variant="ghost"
							size="sm"
							loading={data.occupation.refreshing}
							onclick={data.occupation.refresh}
						>
							{m.common_retry()}
						</Button>
					{/snippet}
				</Banner>
			</div>
		{/if}
		{#if freeOnly && floors.length === 0}
			<EmptyState size="panel" title={m.map_no_free_rooms()}>
				{#snippet action()}
					<Button variant="tonal" onclick={() => (freeOnly = false)}>
						{m.map_show_all_rooms()}
					</Button>
				{/snippet}
			</EmptyState>
		{/if}
		{#each floors as group (group.floor)}
			{@const label = unknownFloors ? m.map_floor_all() : floorLabel(group.floor)}
			<section class="floor">
				<GroupHead title={label} meta={m.map_room_count({ count: group.rooms.length })} />
				<RoomGrid rooms={group.rooms} status={(code) => data.status(code)} {label} />
			</section>
		{/each}
	{/if}
</Level>

<style>
	.summary {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3);
		padding: var(--sp-1) var(--sp-2) 0;
	}

	.counts {
		display: flex;
		flex-wrap: wrap;
		gap: 0 var(--sp-2);
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.counts span + span::before {
		content: '·';
		margin-inline-end: var(--sp-2);
		color: var(--text-subtle);
	}

	.free {
		color: var(--success);
		font-weight: 600;
	}

	.notice {
		padding-block: var(--sp-2) 0;
	}
</style>
