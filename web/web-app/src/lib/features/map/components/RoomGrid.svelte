<script lang="ts">
	import IconUsers from '~icons/ph/users-three';
	import { m } from '#lib/paraglide/messages.js';
	import { roomHref } from '../links.ts';
	import type { MapRoom } from '../rooms.ts';
	import { roomPlaceId } from '../search.ts';
	import type { RoomStatus } from '../status.ts';
	import { getMapView } from '../view.svelte.ts';
	import StatusLine from './StatusLine.svelte';

	interface Props {
		rooms: readonly MapRoom[];
		status: (code: string) => RoomStatus | undefined;
		label: string;
	}

	let { rooms, status, label }: Props = $props();

	const view = getMapView();
</script>

<ul class="grid" aria-label={label}>
	{#each rooms as room (room.code)}
		{@const now = status(room.code)}
		<li class="cell">
			<a
				class="inner"
				href={roomHref(room.building, room.code, view.filter)}
				data-place={roomPlaceId(room)}
			>
				<span class="layout">
					<span class="name">{room.name}</span>
					{#if room.capacity !== null}
						<span class="seats" title={m.map_seat_count({ count: room.capacity })}>
							<IconUsers aria-hidden="true" />
							<span class="sr-only">{m.map_seat_count({ count: room.capacity })}</span>
							<span aria-hidden="true">{room.capacity}</span>
						</span>
					{/if}
					<!-- When the bookings failed, the banner above the grid says so once. -->
					{#if now?.kind !== 'unknown'}
						<span class="status"><StatusLine status={now} /></span>
					{/if}
				</span>
			</a>
		</li>
	{/each}
</ul>

<style>
	.grid {
		--outer: var(--r-lg);
		--inner: var(--r-2xs);
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	@container level (width < 300px) {
		.grid {
			grid-template-columns: minmax(0, 1fr);
		}
	}

	/* A grid, so the link fills a cell its row stretches: the whole tile is the target. */
	.cell {
		display: grid;
		border-radius: var(--inner);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.cell:first-child {
		border-start-start-radius: var(--outer);
	}

	.cell:nth-child(2) {
		border-start-end-radius: var(--outer);
	}

	.cell:last-child {
		border-end-end-radius: var(--outer);
	}

	.cell:nth-last-child(2):nth-child(odd),
	.cell:last-child:nth-child(odd) {
		border-end-start-radius: var(--outer);
	}

	.cell:last-child:nth-child(odd) {
		grid-column: 1 / -1;
	}

	.cell:only-child {
		border-radius: var(--outer);
	}

	@container level (width < 300px) {
		.cell {
			border-radius: var(--inner);
		}

		.cell:first-child {
			border-start-start-radius: var(--outer);
			border-start-end-radius: var(--outer);
		}

		.cell:last-child {
			border-end-start-radius: var(--outer);
			border-end-end-radius: var(--outer);
		}
	}

	.inner {
		display: grid;
		min-block-size: 68px;
		padding: 12px 14px;
		border-radius: inherit;
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition: background-color var(--dur-fast) var(--ease-out);
		container: cell / inline-size;
	}

	/*
	 * The name, with the seats beside it, over the status. The seats keep the width of three
	 * digits, so whether a name wraps depends on the name alone.
	 */
	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		grid-template-areas:
			'name seats'
			'status status';
		align-items: baseline;
		align-content: space-between;
		gap: 6px;
	}

	.layout :global(.detail.sketch) {
		display: none;
	}

	/*
	 * A narrow cell gives the name the whole line and stacks the status: the word, with the seats
	 * at the end of its line, then until when across the cell, so neither is ever cut.
	 */
	@container cell (width < 160px) {
		.layout {
			grid-template-areas:
				'name name'
				'status status';
			row-gap: 4px;
		}

		.layout .seats {
			grid-area: 2 / 2;
		}

		.layout :global(.detail) {
			flex-basis: 100%;
			padding-inline-start: 15px;
		}

		.layout :global(.detail.sketch) {
			display: flex;
		}
	}

	.inner:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		.inner:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.inner:active {
		background: var(--state-press);
		transition-duration: 0s;
	}

	.status {
		grid-area: status;
		display: flex;
		min-inline-size: 0;
	}

	/* A long name takes a second line rather than losing its end ("con Podio"). */
	.name {
		grid-area: name;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		font: var(--type-ui-strong);
		overflow-wrap: anywhere;
	}

	.seats {
		grid-area: seats;
		display: inline-flex;
		align-items: baseline;
		justify-content: flex-end;
		gap: 4px;
		min-inline-size: calc(18px + 3ch);
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.seats :global(svg) {
		inline-size: 14px;
		block-size: 14px;
		align-self: center;
	}
</style>
