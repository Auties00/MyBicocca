<script lang="ts">
	import type { Map as MapLibreMap } from 'maplibre-gl';
	import type { Building } from '../catalog.ts';
	import { spreadHeads } from '../pinShape.ts';
	import type { Insets } from '../view.svelte.ts';
	import BuildingPin, { PIN_HEIGHT, pinHead } from './BuildingPin.svelte';

	interface Props {
		map: MapLibreMap;
		/** In the order keyboard focus visits them. */
		buildings: readonly Building[];
		selected: string | undefined;
		hovered: string | undefined;
		dimmed: readonly string[];
		/** What covers the map's edges: a pin under them takes no keyboard focus. */
		insets: Insets;
		onselect: (code: string) => void;
		onhover: (code: string, inside: boolean) => void;
	}

	let { map, buildings, selected, hovered, dimmed, insets, onselect, onhover }: Props = $props();

	/** Where each pin's tip is on screen, refreshed as the camera moves. */
	let points = $state.raw<Record<string, { x: number; y: number }>>({});
	let size = $state.raw({ width: 0, height: 0 });

	$effect(() => {
		const list = buildings;
		const update = () => {
			const container = map.getContainer();
			size = { width: container.clientWidth, height: container.clientHeight };
			points = Object.fromEntries(
				list.map((building) => [
					building.code,
					map.project([building.longitude, building.latitude])
				])
			);
		};
		update();
		map.on('move', update);
		map.on('resize', update);
		return () => {
			map.off('move', update);
			map.off('resize', update);
		};
	});

	/* Southern pins stack over northern ones, whose tips sit higher on screen. */
	const stack = $derived(
		new Map(
			buildings
				.toSorted((a, b) => b.latitude - a.latitude)
				.map((building, index) => [building.code, index + 1])
		)
	);

	/* Pins standing so close that their heads overlap spread apart, as the camera moves. */
	const offsets = $derived.by(() => {
		const placed = buildings.filter((building) => points[building.code]);
		const spread = spreadHeads(
			placed.map((building) => {
				const point = points[building.code];
				return pinHead(point.x, point.y, building.code === selected);
			})
		);
		return new Map(placed.map((building, index) => [building.code, spread[index]]));
	});

	function inView(point: { x: number; y: number }): boolean {
		return (
			point.x >= insets.left &&
			point.x <= size.width - insets.right &&
			point.y - PIN_HEIGHT >= insets.top &&
			point.y <= size.height - insets.bottom
		);
	}
</script>

{#each buildings as building (building.code)}
	{@const point = points[building.code]}
	{#if point}
		<BuildingPin
			{building}
			x={point.x}
			y={point.y}
			offset={offsets.get(building.code)}
			layer={stack.get(building.code) ?? 0}
			focusable={inView(point)}
			selected={selected === building.code}
			highlighted={hovered === building.code}
			dimmed={dimmed.includes(building.code)}
			onselect={() => onselect(building.code)}
			onhover={(inside) => onhover(building.code, inside)}
		/>
	{/if}
{/each}
