<script lang="ts">
	import IconCalendarCheck from '~icons/ph/calendar-check';
	import IconCalendarDots from '~icons/ph/calendar-dots';
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { pickerDay, slotRange } from '../format.ts';
	import type { Offering, Slot } from '../slots.ts';
	import DayTime from './DayTime.svelte';

	interface Props {
		date: IsoDate | undefined;
		slot: Slot | undefined;
		offering: Offering;
		/** Goes back to the day and time, where they are on another step. */
		onchange?: () => void;
	}

	let { date, slot, offering, onchange }: Props = $props();

	const chosen = $derived(date !== undefined && slot !== undefined);
</script>

<div class={['recap', { chosen }]}>
	<span class="icon" aria-hidden="true">
		{#if chosen}<IconCalendarCheck />{:else}<IconCalendarDots />{/if}
	</span>
	<div class="text">
		<p class="label">{m.appointments_recap_label()}</p>
		<p class="when" aria-live="polite">
			{#if date && slot}
				<DayTime day={pickerDay(date)} time={slotRange(date, slot)} />
			{:else}
				{date ? m.appointments_recap_time() : m.appointments_recap_none()}
			{/if}
		</p>
		<p class="where">
			{offering.virtual ? m.appointments_video_call() : offering.areaName}
		</p>
	</div>
	{#if onchange && chosen}
		<Button size="sm" variant="ghost" onclick={onchange}>{m.appointments_change()}</Button>
	{/if}
</div>

<style>
	.recap {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: var(--sp-3);
		padding: var(--sp-3) var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
	}

	.chosen {
		background: var(--container);
		color: var(--on-container);
	}

	.icon {
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		border-radius: 12px;
		background: color-mix(in oklch, currentColor 10%, transparent);
	}

	.icon :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.text {
		display: grid;
		gap: 1px;
		min-inline-size: 0;
	}

	.label,
	.where {
		font: var(--type-meta);
		opacity: 0.8;
	}

	.when {
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
		text-wrap: pretty;
	}

	.where {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
