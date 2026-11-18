<script lang="ts">
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconMapPin from '~icons/ph/map-pin';
	import { countdown, kindLabel, longWhen, modeLabel, positionLabel } from '../format.ts';
	import { placeHref } from '../links.ts';
	import { callLabel, examLocation, type Booking } from '../model.ts';

	interface Props {
		booking: Booking;
		now: Date;
		student: { name: string; number: string };
		/** How many students the call has, once the slow count arrives. */
		total: number | null | undefined;
	}

	let { booking, now, student, total }: Props = $props();

	const soon = $derived(booking.at ? countdown(booking.at, now) : undefined);
	const today = $derived(
		booking.at !== null && soon !== undefined && booking.at.toDateString() === now.toDateString()
	);
	const day = $derived(booking.at ? longWhen(booking.at, false) : m.exams_time_tbc());
	const place = $derived(examLocation(booking.room, booking.building).place);
	const map = $derived(placeHref(booking.room, booking.building));
	const mode = $derived(
		[kindLabel(booking.kind, booking.callType), modeLabel(booking.mode)].filter(Boolean).join(' · ')
	);
</script>

<div class="wrap">
	<article class="ticket" aria-label={m.exams_ticket_label({ course: booking.title })}>
		<div class="main">
			<header class="top">
				<p class="call">
					{callLabel(booking.callDescription, booking.title) ?? m.exams_booking_label()}
				</p>
				{#if soon}<span class={['soon', { today }]}>{soon}</span>{/if}
			</header>
			<p class="day">{day}</p>
			<dl class="big">
				<div class="cell">
					<dt>{m.exams_ticket_time()}</dt>
					<dd class="numeral">
						{booking.at && booking.timed ? formatTime(booking.at) : '—'}
					</dd>
				</div>
				<div class="cell">
					<dt>{m.exams_ticket_room()}</dt>
					{#if booking.room && map}
						<dd class="numeral">
							<a class="room" href={map}>
								<span translate="no">{booking.room}</span><IconMapPin aria-hidden="true" />
								<span class="sr-only">· {m.exams_room_on_map()}</span>
							</a>
						</dd>
					{:else if booking.room}
						<dd class="numeral" translate="no">{booking.room}</dd>
					{:else}
						<dd class="tbc">{m.exams_room_tbc()}</dd>
					{/if}
					{#if place}<dd class="address">{place}</dd>{/if}
				</div>
			</dl>
		</div>
		<div class="stub">
			<dl class="facts">
				<div class="cell">
					<dt>{m.exams_fact_student()}</dt>
					<dd>{student.name}</dd>
				</div>
				<div class="cell">
					<dt>{m.exams_ticket_number()}</dt>
					<dd class="code" translate="no">{student.number}</dd>
				</div>
				{#if booking.position !== null && booking.position > 0}
					<div class="cell">
						<dt>{m.exams_fact_position()}</dt>
						<dd>{positionLabel(booking.position, total)}</dd>
					</div>
				{/if}
				{#if mode}
					<div class="cell">
						<dt>{m.exams_fact_mode()}</dt>
						<dd>{mode}</dd>
					</div>
				{/if}
			</dl>
		</div>
	</article>
</div>

<style>
	.wrap {
		container-type: inline-size;
	}

	.ticket {
		--notch: 11px;
		--pad: var(--sp-5);
		display: grid;
		filter: drop-shadow(0 0 0.6px var(--ticket-ring))
			drop-shadow(0 2px 3px color-mix(in oklch, var(--shadow-color) 8%, transparent));
		--ticket-ring: light-dark(oklch(0% 0 0 / 0.16), oklch(100% 0 0 / 0.14));
	}

	.main,
	.stub {
		background: var(--surface);
		mask-composite: intersect;
	}

	/* Two punched notches where the stub tears off. */
	.main {
		display: grid;
		gap: var(--sp-3);
		padding: var(--pad) var(--pad) var(--sp-6);
		border-radius: var(--r-lg) var(--r-lg) 0 0;
		mask:
			radial-gradient(circle var(--notch) at 0 100%, #0000 98%, #000),
			radial-gradient(circle var(--notch) at 100% 100%, #0000 98%, #000);
		mask-composite: intersect;
	}

	.stub {
		position: relative;
		padding: var(--sp-5) var(--pad) var(--pad);
		border-radius: 0 0 var(--r-lg) var(--r-lg);
		mask:
			radial-gradient(circle var(--notch) at 0 0, #0000 98%, #000),
			radial-gradient(circle var(--notch) at 100% 0, #0000 98%, #000);
		mask-composite: intersect;
	}

	.stub::before {
		content: '';
		position: absolute;
		inset-block-start: 0;
		inset-inline: calc(var(--notch) + 8px);
		border-block-start: 2px dashed var(--hairline);
	}

	.top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3);
	}

	.call {
		min-inline-size: 0;
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.soon {
		flex-shrink: 0;
		padding: 3px 10px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-label);
		white-space: nowrap;
	}

	.soon.today {
		background: var(--accent);
		color: var(--on-accent);
	}

	.day {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	dl {
		margin: 0;
	}

	dd {
		margin: 0;
	}

	dt {
		color: var(--text-muted);
		font: var(--type-label);
		letter-spacing: 0.01em;
	}

	.big {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: var(--sp-6);
		margin-block-start: var(--sp-2);
	}

	.big .cell {
		display: grid;
		align-content: start;
		gap: 6px;
	}

	.big .cell + .cell {
		padding-inline-start: var(--sp-6);
		border-inline-start: 1px solid var(--hairline);
	}

	.numeral {
		font: var(--type-numeral-l);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
		overflow-wrap: anywhere;
	}

	.room {
		display: inline-flex;
		align-items: baseline;
		gap: 6px;
		color: inherit;
		text-decoration: underline;
		text-decoration-color: var(--hairline-strong);
		text-decoration-thickness: 2px;
		text-underline-offset: 0.16em;
		transition: text-decoration-color var(--dur-fast) var(--ease-out);
	}

	.room :global(svg) {
		align-self: center;
		inline-size: 20px;
		block-size: 20px;
		color: var(--accent-text);
	}

	@media (hover: hover) {
		.room:hover {
			text-decoration-color: var(--accent);
		}
	}

	.tbc {
		padding-block: 8px;
		font: var(--type-title-s);
		font-style: italic;
		color: var(--text-muted);
	}

	.address {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.facts {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: var(--sp-4) var(--sp-5);
	}

	.facts .cell {
		display: grid;
		align-content: start;
		gap: 2px;
		min-inline-size: 0;
	}

	.facts dd {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		overflow-wrap: anywhere;
	}

	.code {
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.02em;
	}

	@container (width >= 560px) {
		.ticket {
			--pad: var(--sp-6);
		}

		.facts {
			grid-template-columns: repeat(4, minmax(0, 1fr));
		}
	}

	/* On paper the masks and shadows go: a plain outline with the tear line, kept on one page. */
	@media print {
		.ticket {
			filter: none;
			break-inside: avoid;
		}

		.main,
		.stub {
			mask: none;
			border: 1px solid var(--hairline-strong);
		}

		.main {
			border-block-end: none;
		}

		.stub {
			border-block-start: none;
		}

		.stub::before {
			inset-inline: 0;
		}

		.soon {
			border: 1px solid var(--hairline-strong);
		}
	}

	@container (width < 340px) {
		.big {
			grid-template-columns: minmax(0, 1fr);
			gap: var(--sp-4);
		}

		.big .cell + .cell {
			padding-inline-start: 0;
			padding-block-start: var(--sp-4);
			border-inline-start: none;
			border-block-start: 1px solid var(--hairline);
		}
	}
</style>
