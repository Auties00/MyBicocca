<script lang="ts">
	import IconClock from '~icons/ph/clock';
	import IconSun from '~icons/ph/sun';
	import IconSunHorizon from '~icons/ph/sun-horizon';
	import { formatTime } from '#lib/format/dates.ts';
	import { toJsDate, type IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { pickerDay } from '../format.ts';
	import { splitByHalfDay, type Slot } from '../slots.ts';

	interface Props {
		/** The picked day; undefined until the month loads. */
		date: IsoDate | undefined;
		/** The day's slots, taken ones included; undefined while they load. */
		slots: readonly Slot[] | undefined;
		selected: Slot | undefined;
		error: string | undefined;
		onretry: () => void;
		onselect: (slot: Slot) => void;
	}

	let { date, slots, selected, error, onretry, onselect }: Props = $props();

	const id = $props.id();
	const bands = $derived.by(() => {
		if (!slots) return [];
		const { morning, afternoon } = splitByHalfDay(slots);
		return [
			{ key: 'morning', label: m.appointments_morning(), icon: IconSun, slots: morning },
			{
				key: 'afternoon',
				label: m.appointments_afternoon(),
				icon: IconSunHorizon,
				slots: afternoon
			}
		].filter((band) => band.slots.length > 0);
	});
	const full = $derived(slots !== undefined && !slots.some((slot) => slot.available));
	const time = (minutes: number) => (date ? formatTime(toJsDate(date, minutes)) : '');
</script>

<section class="card" aria-labelledby="{id}-title">
	<h2 class="title" id="{id}-title">
		<IconClock aria-hidden="true" />
		{#if date}{pickerDay(date)}{:else}<Skeleton variant="text" width="9rem" />{/if}
	</h2>

	{#if error}
		<div class="problem" role="alert">
			<p>{error}</p>
			<Button size="sm" onclick={onretry}>{m.common_retry()}</Button>
		</div>
	{:else if !slots || !date}
		<div class="slots" aria-busy="true">
			{#each { length: 9 }, index (index)}
				<Skeleton height="44px" radius="22px" />
			{/each}
		</div>
	{:else if full || bands.length === 0}
		<p class="full">{m.appointments_day_full()}</p>
	{:else}
		<div
			class="bands"
			role="radiogroup"
			aria-label={m.appointments_times_label({ date: pickerDay(date) })}
		>
			{#each bands as band (band.key)}
				<div class="band">
					<p class="band-label"><band.icon aria-hidden="true" />{band.label}</p>
					<div class="slots">
						{#each band.slots as slot (slot.start)}
							<label
								class={['slot', { taken: !slot.available, picked: selected?.start === slot.start }]}
							>
								<input
									class="sr-only"
									type="radio"
									name="{id}-slot"
									value={slot.start}
									checked={selected?.start === slot.start}
									disabled={!slot.available}
									onchange={() => onselect(slot)}
								/>
								<span aria-hidden={!slot.available}>{time(slot.start)}</span>
								{#if !slot.available}
									<span class="sr-only"
										>{m.appointments_slot_taken({ time: time(slot.start) })}</span
									>
								{/if}
							</label>
						{/each}
					</div>
				</div>
			{/each}
		</div>
	{/if}
</section>

<style>
	.card {
		display: grid;
		align-content: start;
		gap: var(--sp-4);
		padding: var(--sp-4) var(--sp-4) var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.title {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		min-block-size: 1lh;
		padding-inline-start: var(--sp-1);
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.title :global(svg) {
		flex-shrink: 0;
		inline-size: 20px;
		block-size: 20px;
		color: var(--accent-text);
	}

	.bands {
		display: grid;
		gap: var(--sp-4);
	}

	.band {
		display: grid;
		gap: var(--sp-2);
	}

	.band-label {
		display: flex;
		align-items: center;
		gap: 6px;
		padding-inline-start: var(--sp-1);
		color: var(--text-muted);
		font: var(--type-label);
	}

	.band-label :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.slots {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(5.25rem, 1fr));
		gap: var(--sp-2);
	}

	.slot {
		position: relative;
		display: grid;
		place-items: center;
		min-block-size: 44px;
		border-radius: var(--r-pill);
		background: var(--surface-hover);
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
		cursor: pointer;
		transition:
			border-radius var(--dur-spring) var(--spring-standard),
			background-color var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.slot:hover:not(.taken, .picked) {
			background: var(--container);
			color: var(--on-container);
		}
	}

	/* A taken time stays in place, so the day's real occupancy shows at a glance. */
	.slot.taken {
		background: none;
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text-subtle);
		font-weight: 400;
		text-decoration: line-through;
		text-decoration-color: color-mix(in oklch, var(--text-subtle) 70%, transparent);
		cursor: not-allowed;
	}

	/* The picked time turns from a pill into a rounded square. */
	.slot.picked {
		border-radius: var(--r-md);
		background: var(--accent);
		color: var(--on-accent);
		box-shadow: none;
	}

	.slot:has(input:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.full,
	.problem {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: var(--sp-3);
		min-block-size: 120px;
		color: var(--text-muted);
		font: var(--type-meta);
		text-align: center;
		text-wrap: pretty;
	}

	@media (prefers-reduced-motion: reduce) {
		.slot {
			transition: none;
		}
	}
</style>
