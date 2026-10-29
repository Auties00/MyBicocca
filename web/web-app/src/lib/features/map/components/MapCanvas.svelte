<script lang="ts">
	import { onMount, untrack } from 'svelte';
	import type { Attachment } from 'svelte/attachments';
	import type { Map as MapLibreMap } from 'maplibre-gl';
	import { m } from '#lib/paraglide/messages.js';
	import { theme } from '#lib/ui/theme.svelte.ts';
	import type { Basemap } from '../basemap.ts';
	import type { Building } from '../catalog.ts';
	import { resolveColors } from '../palette.ts';
	import type { Insets } from '../view.svelte.ts';
	import { PIN_HEIGHT } from './BuildingPin.svelte';
	import Pins from './Pins.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	export type MapFailure = 'device' | 'basemap';

	interface Props {
		/** The pins, in the order keyboard focus visits them. */
		buildings: readonly Building[];
		selected: Building | undefined;
		hovered: string | undefined;
		dimmed: readonly string[];
		/** What the camera frames while no building is selected; a new set moves it. */
		framed: readonly Building[];
		/** What covers the map's edges (bars, panel, sheet): the camera frames inside. */
		insets: Insets;
		onselect: (code: string) => void;
		onhover: (code: string, inside: boolean) => void;
		/** A tap on the map itself, not on a pin. */
		onbackground: () => void;
		/** The map can't be drawn (no WebGL, no style) or its tiles can't be read; undefined once they can. */
		onfail: (failure: MapFailure | undefined) => void;
	}

	let {
		buildings,
		selected,
		hovered,
		dimmed,
		framed,
		insets,
		onselect,
		onhover,
		onbackground,
		onfail
	}: Props = $props();

	let container = $state<HTMLDivElement>();
	let basemap = $state.raw<Basemap>();
	let ready = $state(false);
	/* No tile came: the paper shows through instead of an empty basemap. */
	let blank = $state(false);
	/* MapLibre's own size, which follows the container's a frame late: framing waits for it. */
	let size = $state('');

	/* How long tiles may fail without a single one drawn before the map is given up. */
	const TILE_GRACE_MS = 1500;
	const TILE_TIMEOUT_MS = 12_000;

	onMount(() => {
		let disposed = false;
		let created: Basemap | undefined;
		let giveUp: ReturnType<typeof setTimeout> | undefined;
		void (async () => {
			try {
				const { createBasemap } = await import('../basemap.ts');
				if (disposed || !container) return;
				created = await createBasemap(container, resolveColors(container), m.map_canvas_label());
				if (disposed) {
					created.map.remove();
					return;
				}
				const { map } = created;
				map.once('load', () => (ready = true));
				/*
				 * A tile that fails (offline) leaves a gap. Only when no tile comes at all
				 * the map says so, over squared paper and the pins; the first tile drawn takes it away.
				 * Glyphs and the sprite fail without a source: labels go missing, nothing more.
				 */
				let drawn = false;
				const fail = (delay: number) => {
					if (drawn || giveUp !== undefined) return;
					giveUp = setTimeout(() => {
						ready = true;
						blank = true;
						onfail('basemap');
					}, delay);
				};
				fail(TILE_TIMEOUT_MS);
				map.on('error', (event) => {
					if ('sourceId' in event && event.sourceId) {
						clearTimeout(giveUp);
						giveUp = undefined;
						fail(TILE_GRACE_MS);
					}
				});
				map.on('data', (event) => {
					if (drawn || event.dataType !== 'source' || !('tile' in event) || !event.tile) return;
					drawn = true;
					blank = false;
					clearTimeout(giveUp);
					onfail(undefined);
				});
				map.on('resize', () => {
					const canvas = map.getCanvas();
					size = `${canvas.clientWidth}x${canvas.clientHeight}`;
				});
				map.on('movestart', (event) => {
					if (event.originalEvent) framing.userMoved = true;
				});
				map.on('click', (event) => {
					const target = event.originalEvent.target;
					if (target instanceof Element && target.closest('.pin')) return;
					onbackground();
				});
				basemap = created;
			} catch {
				onfail('device');
			}
		})();
		return () => {
			disposed = true;
			clearTimeout(giveUp);
			created?.map.remove();
		};
	});

	/* Pins live in MapLibre's canvas container, so a drag or a wheel that starts on one moves the map. */
	function adopt(map: MapLibreMap): Attachment<HTMLElement> {
		return (node) => {
			map.getCanvasContainer().append(node);
		};
	}

	$effect(() => {
		void theme.resolved;
		void theme.palette;
		if (basemap && container) basemap.recolor(resolveColors(container));
	});

	/** Whether the camera has been placed once: the first framing jumps instead of flying. */
	let placed = false;
	const framing = {
		/** The buildings last framed, by code: a different set moves the camera. */
		codes: undefined as string | undefined,
		/** The covered edges when it was last placed. */
		space: '',
		/** The camera shows the framed buildings, as last placed: a change of space re-frames them. */
		onFramed: false,
		/** The user panned or zoomed since the camera was last placed: a change of space leaves it. */
		userMoved: false
	};

	function frameSelected(map: MapLibreMap, building: Building, padding: Insets, animate: boolean) {
		const target = {
			center: [building.longitude, building.latitude] as [number, number],
			zoom: Math.max(map.getZoom(), 17),
			padding,
			/* Centres the pin's head, not its tip. */
			offset: [0, 20] as [number, number]
		};
		if (animate) map.flyTo({ ...target, maxDuration: 1200, essential: true });
		else map.jumpTo(target);
	}

	function frameAll(
		map: MapLibreMap,
		list: readonly Building[],
		padding: Insets,
		animate: boolean
	) {
		/* Room for the pins standing above their points, and for their name tags. */
		const room = {
			top: padding.top + PIN_HEIGHT + 8,
			right: padding.right + 40,
			bottom: padding.bottom + 24,
			left: padding.left + 40
		};
		const canvas = map.getCanvas();
		/* Mid-resize (a phone layout turning into a desktop one) the edges can cover it all. */
		const fits =
			room.left + room.right < canvas.clientWidth && room.top + room.bottom < canvas.clientHeight;
		if (list.length === 0 || !fits) return;
		const longitudes = list.map((entry) => entry.longitude);
		const latitudes = list.map((entry) => entry.latitude);
		const camera = map.cameraForBounds(
			[
				[Math.min(...longitudes), Math.min(...latitudes)],
				[Math.max(...longitudes), Math.max(...latitudes)]
			],
			{ padding: room, absolutePadding: true, maxZoom: 17 }
		);
		if (!camera) return;
		if (animate) map.flyTo({ ...camera, maxDuration: 1200, essential: true });
		else map.jumpTo(camera);
	}

	$effect(() => {
		const map = basemap?.map;
		const building = selected;
		const padding = { ...insets };
		const list = framed;
		const codes = list.map((entry) => entry.code).join(',');
		const space = [padding.top, padding.right, padding.bottom, padding.left, size].join(',');
		if (!map) return;
		untrack(() => {
			const animate = placed && ready && !reducedMotion.current;
			if (!placed && building) framing.codes = codes;
			placed = true;
			const moved = space !== framing.space;
			framing.space = space;
			if (building) {
				frameSelected(map, building, padding, animate);
				framing.onFramed = false;
			} else if (codes !== framing.codes || (moved && framing.onFramed && !framing.userMoved)) {
				framing.codes = codes;
				frameAll(map, list, padding, animate);
				framing.onFramed = true;
			} else return;
			framing.userMoved = false;
		});
	});

	export function zoomIn() {
		framing.userMoved = true;
		basemap?.map.zoomIn();
	}

	export function zoomOut() {
		framing.userMoved = true;
		basemap?.map.zoomOut();
	}

	/** Frames the selection again, or the buildings shown when there is none. */
	export function recenter() {
		const map = basemap?.map;
		if (!map) return;
		const animate = !reducedMotion.current;
		if (selected) frameSelected(map, selected, { ...insets }, animate);
		else frameAll(map, framed, { ...insets }, animate);
		framing.onFramed = !selected;
		framing.userMoved = false;
	}
</script>

<div class={['canvas', { ready, blank }]} bind:this={container}>
	{#if basemap}
		<div class="pin-layer" {@attach adopt(basemap.map)}>
			<Pins
				map={basemap.map}
				{buildings}
				selected={selected?.code}
				{hovered}
				{dimmed}
				{insets}
				{onselect}
				{onhover}
			/>
		</div>
	{/if}
</div>

<style>
	/* Squared paper under the map, seen while it loads or when it can't be drawn. */
	.canvas {
		--grid-line: color-mix(in oklch, var(--border) 50%, transparent);
		position: absolute;
		inset: 0;
		z-index: 0;
		background:
			linear-gradient(var(--grid-line) 1px, transparent 1px) 0 0 / 40px 40px,
			linear-gradient(90deg, var(--grid-line) 1px, transparent 1px) 0 0 / 40px 40px,
			var(--bg);
	}

	.canvas :global(.maplibregl-canvas),
	.pin-layer {
		opacity: 0;
		transition: opacity var(--dur-slow) var(--ease-out);
	}

	.ready :global(.maplibregl-canvas),
	.ready .pin-layer {
		opacity: 1;
	}

	.blank :global(.maplibregl-canvas) {
		opacity: 0;
	}

	.canvas :global(.maplibregl-canvas:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.pin-layer {
		position: absolute;
		inset: 0;
		overflow: hidden;
		pointer-events: none;
	}
</style>
