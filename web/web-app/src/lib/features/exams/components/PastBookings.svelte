<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import DateBadge from '#lib/ui/DateBadge.svelte';
	import Grade from '#lib/ui/Grade.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { kindLabel } from '../format.ts';
	import { bookingHref } from '../links.ts';
	import type { Booking } from '../model.ts';

	interface Props {
		bookings: readonly Booking[];
	}

	let { bookings }: Props = $props();

	/** The latest few; the rest on request, since a third-year history runs to dozens. */
	const FIRST = 5;

	let expanded = $state(false);
	const shown = $derived(expanded ? bookings : bookings.slice(0, FIRST));
	const headingId = $props.id();
</script>

<section class="past" aria-labelledby={headingId}>
	<SectionHeader
		id={headingId}
		title={m.exams_past_heading()}
		description={m.exams_past_description()}
	/>
	<ListGroup label={m.exams_past_heading()} lazy={expanded && bookings.length > 50}>
		{#each shown as booking (booking.slug)}
			<ListRow
				title={booking.title || m.exams_kind_unknown()}
				description={[kindLabel(booking.kind, booking.callType), booking.at?.getFullYear()]
					.filter(Boolean)
					.join(' · ')}
				href={bookingHref(booking.slug)}
				lines={1}
			>
				{#snippet leading()}
					{#if booking.at}<DateBadge date={booking.at} size="sm" tone="neutral" />{/if}
				{/snippet}
				{#snippet trailing()}
					{#if booking.published && booking.grade.kind !== 'unknown'}
						<Grade grade={booking.grade} />
					{:else}
						<StatusChip status="pending" label={m.exams_awaiting_result()} size="sm" />
					{/if}
				{/snippet}
			</ListRow>
		{/each}
	</ListGroup>
	{#if bookings.length > FIRST}
		<div class="more">
			<Button
				variant="ghost"
				size="sm"
				trailingIcon={IconCaretDown}
				aria-expanded={expanded}
				onclick={() => (expanded = !expanded)}
			>
				{expanded ? m.exams_show_fewer() : m.exams_show_all_past({ count: bookings.length })}
			</Button>
		</div>
	{/if}
</section>

<style>
	.past {
		display: grid;
		gap: var(--sp-4);
	}

	.more {
		display: flex;
		justify-content: center;
	}

	.more :global([aria-expanded='true'] svg:last-child) {
		transform: rotate(180deg);
	}

	.more :global(svg:last-child) {
		transition: transform var(--dur-base) var(--ease-out);
	}
</style>
