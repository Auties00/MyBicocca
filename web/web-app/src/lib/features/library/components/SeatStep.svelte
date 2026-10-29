<script lang="ts">
	import IconArmchair from '~icons/ph/armchair-duotone';
	import IconChair from '~icons/ph/armchair';
	import IconLightning from '~icons/ph/lightning-fill';
	import IconSparkle from '~icons/ph/sparkle';
	import type { AffluencesAvailableResource } from '@mybicocca/data-api/affluences';
	import { goto } from '$app/navigation';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import { seatsAt, toSeats, type ZoneColor } from '../model.ts';
	import ZoneSwatch from './ZoneSwatch.svelte';

	interface Props {
		seats: Query<AffluencesAvailableResource[]>;
		start: number;
		color: ZoneColor;
		seatHref: (seat: number) => ResolvedPathname;
		/** Back to the day and time. */
		whenHref: ResolvedPathname;
	}

	let { seats, start, color, seatHref, whenHref }: Props = $props();

	let outletOnly = $state(false);
	const free = $derived(seats.data ? seatsAt(toSeats(seats.data), start) : undefined);
	const outlets = $derived(free?.filter((seat) => seat.outlet).length ?? 0);
	const shown = $derived(free && outletOnly ? free.filter((seat) => seat.outlet) : free);
	/* Libraries tend to describe every seat alike: said once above the grid, else on each seat. */
	const sharedDescription = $derived.by(() => {
		const descriptions = new Set(free?.map((seat) => seat.description));
		const [only] = descriptions;
		return descriptions.size === 1 ? only : undefined;
	});
	const gate = new LoadingGate(() => free === undefined && !seats.error);
</script>

{#if seats.error && !free}
	<ErrorState
		title={m.library_times_loading_failed()}
		message={errorMessage(seats.error)}
		onretry={seats.refresh}
		retrying={seats.refreshing}
	/>
{:else if !free || !shown}
	<div class="seats" aria-busy="true">
		{#each { length: 18 }, index (index)}
			<span class="seat-skeleton">
				{#if gate.visible}<Skeleton variant="block" height="100%" radius="var(--r-md)" />{/if}
			</span>
		{/each}
	</div>
{:else if free.length === 0}
	<EmptyState
		size="page"
		icon={IconArmchair}
		title={m.library_no_free_seats()}
		body={m.library_no_free_seats_body()}
	>
		{#snippet action()}
			<Button href={whenHref}>{m.library_step_when()}</Button>
		{/snippet}
	</EmptyState>
{:else}
	<div class="toolbar">
		<div class="summary">
			<p class="count" role="status">{m.library_seat_count({ count: shown.length })}</p>
			{#if sharedDescription}<p class="description">{sharedDescription}</p>{/if}
		</div>
		<div class="tools">
			{#if outlets > 0 && outlets < free.length}
				<FilterChip
					label={m.library_with_outlet()}
					icon={IconLightning}
					count={outlets}
					bind:selected={outletOnly}
				/>
			{/if}
			<Button size="sm" icon={IconSparkle} onclick={() => shown[0] && goto(seatHref(shown[0].id))}>
				{m.library_auto_select()}
			</Button>
		</div>
	</div>

	<ul class={['seats', { refreshing: seats.refreshing }]}>
		{#each shown as seat (seat.id)}
			{@const description = sharedDescription === undefined ? seat.description : null}
			<li>
				<a
					class="seat"
					href={seatHref(seat.id)}
					title={description ?? undefined}
					aria-label={[
						seat.outlet ? m.library_seat_with_outlet({ seat: seat.short }) : seat.short,
						description
					]
						.filter(Boolean)
						.join(', ')}
				>
					<ZoneSwatch {color}><IconChair /></ZoneSwatch>
					<span class="text">
						<span class="name" translate="no">{seat.short}</span>
						{#if description}<span class="detail">{description}</span>{/if}
					</span>
					{#if seat.outlet}<IconLightning class="outlet" aria-hidden="true" />{/if}
				</a>
			</li>
		{/each}
	</ul>
{/if}

<style>
	.toolbar {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3);
	}

	.summary {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.count {
		font: var(--type-ui-strong);
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.tools {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-2);
	}

	.seats {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(116px, 1fr));
		gap: var(--sp-2);
		margin: 0;
		padding: 0;
		list-style: none;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.seats.refreshing {
		opacity: 0.6;
	}

	.seat-skeleton {
		block-size: 60px;
	}

	.seat {
		position: relative;
		display: flex;
		align-items: center;
		gap: 10px;
		min-block-size: 60px;
		padding: 10px 12px;
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.seat :global(.swatch) {
		inline-size: 36px;
		block-size: 36px;
		border-radius: 11px;
	}

	@media (hover: hover) {
		.seat:hover {
			background: var(--surface-hover);
			transition-duration: 0s, var(--dur-spring);
		}
	}

	.seat:active {
		transform: scale(0.96);
		transition-duration: var(--dur-instant);
	}

	.text {
		display: grid;
		flex: 1;
		min-inline-size: 0;
	}

	.name {
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.detail {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.seat :global(.outlet) {
		flex-shrink: 0;
		inline-size: 16px;
		block-size: 16px;
		color: var(--gold);
	}

	@media (prefers-reduced-motion: reduce) {
		.seat {
			transition: none;
		}
	}
</style>
