<script lang="ts">
	import { untrack } from 'svelte';
	import { afterNavigate, beforeNavigate, goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconCrosshair from '~icons/ph/crosshair-simple';
	import IconMinus from '~icons/ph/minus';
	import IconPlus from '~icons/ph/plus';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import Button from '#lib/ui/Button.svelte';
	import { BuildingData } from '../buildingData.svelte.ts';
	import { buildings, findBuilding, type BuildingCategory } from '../catalog.ts';
	import { buildingHref, mapHref, parseFilter, parseQuery, roomHref } from '../links.ts';
	import { roomsCatalogQuery } from '../queries.ts';
	import { buildingPlaceId } from '../search.ts';
	import { MapState, setMapView, type Insets } from '../view.svelte.ts';
	import BuildingDetail from './BuildingDetail.svelte';
	import Directory from './Directory.svelte';
	import MapCanvas, { type MapFailure } from './MapCanvas.svelte';
	import MapPanel from './MapPanel.svelte';
	import MapSheet, { type Detent } from './MapSheet.svelte';
	import RoomDetail from './RoomDetail.svelte';
	import { compact } from '#lib/ui/media.ts';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';

	interface Props {
		/** The building code in the URL; other names for it redirect there before this renders. */
		building?: string;
		room?: string;
	}

	let { building: buildingParam, room }: Props = $props();

	/** Width of the map area from which the directory stays open beside a building or room. */
	const SPLIT_WIDTH = 1280;

	/* Phones: the sheet's detent; its body scrolls only once raised. */
	let detent = $state<Detent>(untrack(() => (buildingParam ? 'half' : 'peek')));
	const catalog = query(() => {
		const scope = session.current;
		return scope && roomsCatalogQuery(scope);
	});
	const view = setMapView(
		new MapState(
			() => parseFilter(page.url.searchParams),
			() => !compact.current || detent === 'full',
			() => catalog.data,
			untrack(() => parseQuery(page.url.searchParams))
		)
	);

	const building = $derived(buildingParam ? findBuilding(buildingParam) : undefined);
	const data = new BuildingData(
		() => building,
		() => catalog.data
	);
	const level = $derived(room ? 'room' : buildingParam ? 'building' : 'directory');

	/** The pins, in catalogue order: the filtered buildings plus the open one. */
	const pins = $derived(
		building && !view.shown.includes(building)
			? buildings.filter((entry) => entry === building || view.shown.includes(entry))
			: view.shown
	);
	/** While searching, the pins of buildings with no match fade. */
	const dimmed = $derived.by(() => {
		if (!view.query || level !== 'directory') return [];
		const matching = new Set(view.hits.map((hit) => hit.place.building.code));
		return buildings.filter((entry) => !matching.has(entry.code)).map((entry) => entry.code);
	});

	let root = $state<HTMLDivElement>();
	let width = $state(0);
	let height = $state(0);
	let topInset = $state(0);
	let tabZone = $state(0);
	let band = $state(0);
	let panelWidth = $state(0);
	let cornerHeight = $state(0);
	let noteHeight = $state(0);
	let headHeight = $state(0);
	let collapsed = $state(false);
	let failure = $state<MapFailure>();
	let canvas = $state<ReturnType<typeof MapCanvas>>();
	const split = $derived(width >= SPLIT_WIDTH);

	/* Phones: the sheet's heights, and how far a drag holds it. */
	let directoryDetent: Detent = 'peek';
	let drag = $state<number>();
	/* What covers the top of the map: the top bar, or the offline band under it. */
	const top = $derived(Math.max(topInset, band));
	const heights = $derived.by((): Record<Detent, number> => {
		const full = Math.max(0, height - top - 8);
		const peek = Math.min(full, 24 + headHeight + tabZone);
		const half = Math.min(full, Math.max(peek + 96, Math.round(height * 0.55)));
		return { peek, half, full };
	});
	const shown = $derived(drag ?? heights[detent]);

	/** The level a map URL shows: `directory`, or the place id of its building or room. */
	function levelOf(params: { building?: string; room?: string } | null | undefined): string {
		if (params?.building && params.room) return `room:${params.building}/${params.room}`;
		return params?.building ? buildingPlaceId(params.building) : 'directory';
	}

	const depth = (level: string) =>
		level === 'directory' ? 0 : level.startsWith('building:') ? 1 : 2;

	/* Going up a level gives back where the user was on it: its scroll, and the row they opened. */
	beforeNavigate((navigation) => {
		const from = levelOf(navigation.from?.params);
		const to = levelOf(navigation.to?.params);
		const inMap = navigation.to?.route.id?.startsWith('/(app)/map') ?? false;
		view.returning = inMap && depth(to) < depth(from) ? { level: to, from } : undefined;
	});

	/**
	 * Focuses the row of the place just left, bringing it into view: a room's own row or its
	 * building's; for a building, its row or, among search results, the first of its rooms.
	 */
	function refocus(from: string) {
		const room = /^room:([^/]+)[/]/.exec(from);
		const selectors = room
			? [
					`[data-place="${CSS.escape(from)}"]`,
					`[data-place="${CSS.escape(buildingPlaceId(room[1]))}"]`
				]
			: [
					`[data-place="${CSS.escape(from)}"]`,
					`[data-place^="${CSS.escape(from.replace(/^building:/, 'room:'))}/"]`
				];
		for (const selector of selectors) {
			const row = root?.querySelector<HTMLElement>(selector);
			if (!row) continue;
			row.focus({ preventScroll: true });
			const body = row.closest<HTMLElement>('[data-level-body]');
			if (!body) return;
			const box = row.getBoundingClientRect();
			const area = body.getBoundingClientRect();
			/* On phones the floating tab bar covers the sheet's foot. */
			const bottom = Math.min(area.bottom, innerHeight - (compact.current ? tabZone : 0));
			if (box.top < area.top) body.scrollTop -= area.top - box.top + 8;
			else if (box.bottom > bottom) body.scrollTop += box.bottom - bottom + 8;
			return;
		}
	}

	afterNavigate((navigation) => {
		const returning = view.returning;
		if (returning) {
			// Synchronously: the shell then leaves focus where this puts it.
			refocus(returning.from);
			view.returning = undefined;
		}
		const params = navigation.to?.params ?? {};
		const next = params.room ? 'room' : params.building ? 'building' : 'directory';
		const previous = navigation.from?.params?.room
			? 'room'
			: navigation.from?.params?.building
				? 'building'
				: 'directory';
		if (navigation.type === 'enter' || next === previous) {
			if (navigation.type === 'enter') detent = next === 'directory' ? 'peek' : 'half';
			return;
		}
		if (previous === 'directory') directoryDetent = detent;
		if (next === 'directory') detent = directoryDetent;
		else if (next === 'building' && previous === 'directory') detent = 'half';
		else if (detent === 'peek') detent = 'half';
	});

	/** What covers the map, so the selection is framed in the part left uncovered. */
	const insets = $derived.by((): Insets => {
		if (compact.current) {
			/* Raised all the way the sheet leaves no map to frame in: the framing stays as it was. */
			const covered = heights[detent === 'full' ? 'half' : detent];
			/* The recentre button and the attribution stand on the sheet's edge. */
			return {
				top: top + 12 + (failure ? noteHeight : 0),
				right: 12,
				bottom: covered + 12 + cornerHeight,
				left: 12
			};
		}
		return {
			top: Math.max(24, band + 8) + (failure ? noteHeight : 0),
			right: 72,
			bottom: 24,
			left: collapsed ? 24 : panelWidth + 40
		};
	});

	function setFilter(filter: BuildingCategory[]) {
		const href = room
			? roomHref(building?.code ?? '', room, filter)
			: building
				? buildingHref(building.code, filter)
				: mapHref(filter);
		void goto(href, { replace: true, reset: false });
	}

	function select(code: string) {
		if (code === building?.code && level === 'building') {
			if (compact.current && detent === 'peek') detent = 'half';
			return;
		}
		void goto(buildingHref(code, view.filter), { replace: level !== 'directory' });
	}

	function hover(code: string, inside: boolean) {
		view.hoveredRow = undefined;
		if (inside) view.hovered = code;
		else if (view.hovered === code) view.hovered = undefined;
	}

	/* On phones a tap on the map lowers the sheet, then closes the building. */
	function background() {
		if (!compact.current) return;
		if (detent !== 'peek') detent = 'peek';
		else if (level !== 'directory') void goto(mapHref(view.filter));
	}

	function up() {
		if (level === 'room' && building) void goto(buildingHref(building.code, view.filter));
		else if (level === 'building') void goto(mapHref(view.filter));
	}

	registerShortcuts(() => level !== 'directory' && [{ keys: [['esc']], label: m.map_keys_up() }]);

	function keydown(event: KeyboardEvent) {
		if (event.key !== 'Escape' || event.defaultPrevented) return;
		const target = event.target;
		if (target instanceof HTMLElement && target.closest('input, textarea, dialog')) return;
		up();
	}
</script>

<svelte:window onkeydown={keydown} />

{#snippet detailLevel(transition: boolean)}
	{#if buildingParam && !building}
		<div class="missing" data-vt-page={transition ? '' : undefined}>
			<EmptyState title={m.map_building_not_found()} body={m.map_building_not_found_body()}>
				{#snippet action()}
					<Button variant="tonal" href={mapHref(view.filter)}>{m.map_show_all_buildings()}</Button>
				{/snippet}
			</EmptyState>
		</div>
	{:else if building && room}
		{#key `${building.code}/${room}`}
			<RoomDetail {building} code={room} {catalog} {data} />
		{/key}
	{:else if building}
		{#key building.code}
			<BuildingDetail {building} {catalog} {data} />
		{/key}
	{/if}
{/snippet}

{#snippet directory(transition: boolean)}
	<Directory
		roomsPending={!catalog.data && !catalog.error}
		onfilter={setFilter}
		{transition}
		open={transition ? undefined : building?.code}
	/>
{/snippet}

<div
	class={['view', { compact: compact.current, collapsed }]}
	data-map-view
	bind:this={root}
	bind:clientWidth={width}
	bind:clientHeight={height}
	style:--sheet-shown={`${shown}px`}
>
	<span class="probe top" bind:offsetHeight={topInset}></span>
	<span class="probe tab" bind:offsetHeight={tabZone}></span>
	<span class="probe band" bind:offsetHeight={band}></span>

	{#if compact.current}
		<MapSheet
			label={m.map_title()}
			{detent}
			{heights}
			visible={shown}
			ondrag={(value) => (drag = value)}
			ondetent={(value) => (detent = value)}
			onhead={(value) => (headHeight = value)}
		>
			{#if level === 'directory'}
				{@render directory(true)}
			{:else}
				{@render detailLevel(true)}
			{/if}
		</MapSheet>
	{:else}
		<div class="panels">
			<div class="stack" bind:offsetWidth={panelWidth} inert={collapsed}>
				{#if split}
					<MapPanel label={m.map_buildings_title()}>{@render directory(false)}</MapPanel>
					{#if level !== 'directory'}
						<MapPanel label={m.map_title()} detail>{@render detailLevel(true)}</MapPanel>
					{:else}
						<span data-vt-page hidden></span>
					{/if}
				{:else}
					<MapPanel label={m.map_title()}>
						{#if level === 'directory'}
							{@render directory(true)}
						{:else}
							{@render detailLevel(true)}
						{/if}
					</MapPanel>
				{/if}
			</div>
			<button
				type="button"
				class="collapse"
				aria-expanded={!collapsed}
				aria-label={collapsed ? m.map_panel_expand() : m.map_panel_collapse()}
				onclick={() => (collapsed = !collapsed)}
			>
				{#if collapsed}
					<IconCaretRight aria-hidden="true" />
				{:else}
					<IconCaretLeft aria-hidden="true" />
				{/if}
			</button>
		</div>
	{/if}
	<MapCanvas
		bind:this={canvas}
		onfail={(value) => (failure = value)}
		buildings={pins}
		selected={building}
		hovered={view.hovered}
		{dimmed}
		framed={view.framed}
		{insets}
		onselect={select}
		onhover={hover}
		onbackground={background}
	/>

	{#if failure}
		<p class="failed" role="status" bind:offsetHeight={noteHeight}>
			{failure === 'device'
				? m.map_unavailable()
				: navigator.onLine
					? m.map_unavailable_basemap()
					: m.map_unavailable_offline()}
		</p>
	{/if}

	<div
		class={['corner', { hidden: compact.current && detent !== 'peek' }]}
		bind:offsetHeight={cornerHeight}
	>
		{#if !compact.current}
			<div class="zoom">
				<IconButton
					icon={IconPlus}
					label={m.map_zoom_in()}
					variant="tonal"
					tooltipPlacement="left"
					onclick={() => canvas?.zoomIn()}
				/>
				<IconButton
					icon={IconMinus}
					label={m.map_zoom_out()}
					variant="tonal"
					tooltipPlacement="left"
					onclick={() => canvas?.zoomOut()}
				/>
			</div>
		{/if}
		<IconButton
			icon={IconCrosshair}
			label={m.map_recenter()}
			variant="tonal"
			tooltipPlacement="left"
			onclick={() => canvas?.recenter()}
		/>
		<a
			class="attribution"
			href="https://www.openstreetmap.org/copyright"
			target="_blank"
			rel="external noopener noreferrer">{m.map_attribution()}</a
		>
	</div>
</div>

<style>
	/* The page is the whole viewport (`<Page fill="viewport">`): the map runs under the floating bars. */
	.view {
		--panel-w: clamp(360px, 36cqi + 40px, 420px);
		--detail-w: 420px;
		/* Below the offline band when it shows. */
		--top: max(var(--sp-4), calc(var(--shell-band) + var(--sp-2)));
		position: relative;
		flex: 1;
		min-block-size: 0;
		overflow: hidden;
		container: map / inline-size;
	}

	.probe {
		position: absolute;
		inline-size: 0;
		visibility: hidden;
		pointer-events: none;
	}

	.probe.top {
		block-size: var(--shell-inset-top);
	}

	/* The floating tab bar, its gap to the screen's edge and a breath above it. */
	.probe.tab {
		block-size: calc(var(--shell-inset-bottom) + 8px);
	}

	.probe.band {
		block-size: var(--shell-band);
	}

	/* Over the top of the map: the camera frames the pins below it. */
	.failed {
		position: absolute;
		inset: var(--top) var(--sp-4) auto auto;
		z-index: 1;
		max-inline-size: 360px;
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface-raised);
		box-shadow: var(--elev-2);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.compact .failed {
		inset: calc(max(var(--shell-inset-top), var(--shell-band)) + 12px) 12px auto;
	}

	.corner {
		position: absolute;
		inset-block-end: var(--sp-4);
		inset-inline-end: var(--sp-4);
		z-index: 1;
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: var(--sp-2);
		transition:
			transform var(--dur-sheet) var(--ease-sheet),
			opacity var(--dur-fast) var(--ease-out);
	}

	.compact .corner {
		inset-block-end: 0;
		inset-inline-end: var(--sp-3);
		transform: translateY(calc(-1 * var(--sheet-shown) - 12px));
	}

	.corner.hidden {
		opacity: 0;
		pointer-events: none;
	}

	.zoom {
		display: grid;
		gap: 1px;
		overflow: hidden;
		border-radius: var(--r-md);
		background: var(--border);
		box-shadow: var(--elev-2);
	}

	.zoom :global(.btn) {
		border-radius: 0;
	}

	.corner > :global(.btn) {
		box-shadow: var(--elev-2);
	}

	.attribution {
		padding: 2px 8px;
		border-radius: var(--r-pill);
		background: color-mix(in oklch, var(--bg) 80%, transparent);
		color: var(--text-muted);
		font: 500 0.6875rem / 1.4 var(--font-ui);
		text-decoration: none;
	}

	.attribution:hover {
		color: var(--text);
		text-decoration: underline;
	}

	.panels {
		position: absolute;
		inset-block: var(--top) var(--sp-4);
		inset-inline-start: var(--sp-4);
		z-index: 2;
		max-block-size: calc(100% - var(--top) - var(--sp-4));
		pointer-events: none;
		transition: transform var(--dur-slow) var(--ease-out);
	}

	.stack {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-3);
		block-size: 100%;
	}

	.stack > :global(*) {
		max-block-size: 100%;
	}

	.collapsed .panels {
		transform: translateX(calc(-100% - var(--sp-4)));
	}

	.collapse {
		position: absolute;
		inset-block-start: var(--sp-6);
		inset-inline-start: 100%;
		display: grid;
		place-items: center;
		inline-size: 24px;
		block-size: 48px;
		border-radius: 0 var(--r-sm) var(--r-sm) 0;
		background: var(--bg);
		box-shadow: var(--elev-2);
		color: var(--text-muted);
		pointer-events: auto;
	}

	@media (hover: hover) {
		.collapse:hover {
			color: var(--text);
		}
	}

	.collapse :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.missing {
		padding: var(--sp-4);
	}
</style>
