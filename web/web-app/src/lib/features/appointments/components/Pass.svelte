<script lang="ts">
	import IconArrowsOut from '~icons/ph/arrows-out';
	import IconCopy from '~icons/ph/copy';
	import IconVideoCamera from '~icons/ph/video-camera';
	import { formatDate, formatRelative, formatTime } from '#lib/format/dates.ts';
	import { romeDate, romeDay } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import type { ExternalHref } from '#lib/ui/types.ts';
	import { durationMinutes } from '../directory.ts';
	import { dayLabel } from '../format.ts';
	import type { Reservation } from '../reservation.ts';

	interface Props {
		reservation: Reservation;
		now: number;
		/** The video call's link, for a booking held online. */
		joinHref?: ExternalHref;
		/** Shows the QR full screen, for the desk's scanner. */
		onexpand: () => void;
	}

	let { reservation, now, joinHref, onexpand }: Props = $props();

	/* Joining is the one thing left to do from a quarter of an hour before the call. */
	const JOIN_LEAD_MS = 15 * 60_000;

	const start = $derived(romeDate(reservation.start));
	const ended = $derived(reservation.end < now);
	const day = $derived(romeDay(reservation.start));
	const soon = $derived.by(() => {
		if (ended) return m.appointments_ended();
		const label = dayLabel(day, now);
		if (label === m.appointments_today() || label === m.appointments_tomorrow()) return label;
		const relative = formatRelative(reservation.start, new Date(now));
		return relative.charAt(0).toLocaleUpperCase() + relative.slice(1);
	});
	const today = $derived(!ended && soon === m.appointments_today());
	const minutes = $derived(durationMinutes((reservation.end - reservation.start) / 1000));
	const joinable = $derived(now >= reservation.start - JOIN_LEAD_MS);

	async function copy() {
		try {
			await navigator.clipboard.writeText(reservation.code);
			toast.show({ message: m.appointments_code_copied(), tone: 'success' });
		} catch {
			/* Clipboard refused (no permission, insecure context): the code stays selectable. */
		}
	}
</script>

<div class="wrap">
	<article class="pass" aria-label={m.appointments_detail_label()}>
		<div class="main">
			<header class="top">
				<p class="office">{m.appointments_pass_label()}</p>
				<span class={['soon', { today, ended }]}>{soon}</span>
			</header>
			<p class="day">{formatDate(start, 'full')}</p>
			<dl class="big">
				<div class="cell">
					<dt>{m.appointments_fact_time()}</dt>
					<dd class="numeral">{formatTime(start)}</dd>
					<dd class="sub">{m.appointments_minutes({ minutes })}</dd>
				</div>
				<div class="cell">
					{#if reservation.webConferenceUrl}
						<dt>{m.appointments_fact_mode()}</dt>
						<dd class="place">{m.appointments_video_call()}</dd>
					{:else}
						<dt>{m.appointments_fact_where()}</dt>
						{#if reservation.areaName}
							<dd class="place" translate="no">{reservation.areaName}</dd>
						{:else}
							<dd class="tbc">{m.appointments_location_tbc()}</dd>
						{/if}
						{#if reservation.areaAddress}
							<dd class="sub" translate="no">{reservation.areaAddress}</dd>
						{/if}
					{/if}
				</div>
			</dl>
		</div>
		<div class={['stub', { 'no-qr': !reservation.qrCode }]}>
			{#if reservation.qrCode}
				<button
					class="qr"
					type="button"
					onclick={onexpand}
					aria-label={m.appointments_qr_fullscreen()}
				>
					<img
						src={reservation.qrCode}
						alt={m.appointments_qr_label()}
						width="132"
						height="132"
						decoding="async"
					/>
					<span class="expand" aria-hidden="true"><IconArrowsOut /></span>
				</button>
			{/if}
			<div class="code-block">
				<p class="label">{m.appointments_code_label()}</p>
				<p class="code-row">
					<span class="code" translate="no">{reservation.code}</span>
					<IconButton icon={IconCopy} label={m.appointments_copy_code()} size="sm" onclick={copy} />
				</p>
				<p class="hint">
					{reservation.webConferenceUrl
						? m.appointments_video_hint()
						: reservation.qrCode
							? m.appointments_qr_hint()
							: m.appointments_qr_missing()}
				</p>
				{#if joinHref && !ended}
					<div class="join">
						<Button variant={joinable ? 'solid' : 'tonal'} icon={IconVideoCamera} href={joinHref}>
							{m.appointments_join()}
						</Button>
					</div>
				{/if}
				<p class="label owner">{m.appointments_fact_email()}</p>
				<p class="email" translate="no">{reservation.email}</p>
			</div>
		</div>
	</article>
</div>

<style>
	.wrap {
		container-type: inline-size;
	}

	.pass {
		--notch: 11px;
		--pad: var(--sp-5);
		--ring: light-dark(oklch(0% 0 0 / 0.16), oklch(100% 0 0 / 0.14));
		display: grid;
		filter: drop-shadow(0 0 0.6px var(--ring))
			drop-shadow(0 2px 3px color-mix(in oklch, var(--shadow-color) 8%, transparent));
	}

	.main,
	.stub {
		background: var(--surface);
	}

	/* Two punched notches where the stub tears off, as on a desk ticket. */
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
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: var(--sp-5);
		padding: var(--sp-5) var(--pad) var(--pad);
		border-radius: 0 0 var(--r-lg) var(--r-lg);
		mask:
			radial-gradient(circle var(--notch) at 0 0, #0000 98%, #000),
			radial-gradient(circle var(--notch) at 100% 0, #0000 98%, #000);
		mask-composite: intersect;
	}

	.stub.no-qr {
		grid-template-columns: minmax(0, 1fr);
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

	.office {
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

	.day::first-letter {
		text-transform: uppercase;
	}

	dl,
	dd {
		margin: 0;
	}

	dt,
	.label {
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

	.cell {
		display: grid;
		align-content: start;
		gap: 6px;
		min-inline-size: 0;
	}

	.cell + .cell {
		padding-inline-start: var(--sp-6);
		border-inline-start: 1px solid var(--hairline);
	}

	.numeral {
		font: var(--type-numeral-l);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.place {
		padding-block: 4px 0;
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		overflow-wrap: anywhere;
		text-wrap: pretty;
	}

	.tbc {
		padding-block: 6px 0;
		color: var(--text-muted);
		font: var(--type-title-s);
		font-style: italic;
	}

	.sub {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	/* The scanner needs dark on light whatever the theme: the QR keeps its white ground. */
	.qr {
		position: relative;
		display: grid;
		place-items: center;
		padding: 8px;
		border: 0;
		border-radius: var(--r-md);
		background: #fff;
		box-shadow: inset 0 0 0 1px var(--hairline);
		cursor: zoom-in;
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.qr img {
		display: block;
		inline-size: 132px;
		block-size: 132px;
		image-rendering: pixelated;
	}

	.qr:active {
		transform: scale(0.97);
	}

	.qr:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.expand {
		position: absolute;
		inset-block-end: -6px;
		inset-inline-end: -6px;
		display: grid;
		place-items: center;
		inline-size: 28px;
		block-size: 28px;
		border-radius: var(--r-pill);
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-2);
	}

	.expand :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.code-block {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.code-row {
		display: flex;
		align-items: center;
		gap: 4px;
		margin-inline-start: -2px;
	}

	.code {
		font: 600 1.375rem / 1.2 var(--font-ui);
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.06em;
		user-select: all;
		overflow-wrap: anywhere;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.join {
		margin-block-start: var(--sp-3);
	}

	.owner {
		margin-block-start: var(--sp-3);
	}

	.email {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		overflow-wrap: anywhere;
	}

	@container (width >= 560px) {
		.pass {
			--pad: var(--sp-6);
		}

		.qr img {
			inline-size: 156px;
			block-size: 156px;
		}
	}

	/* Phones: the QR on its own row, as large as the scanner likes it. */
	@container (width < 400px) {
		.stub {
			grid-template-columns: minmax(0, 1fr);
			gap: var(--sp-4);
		}

		.qr {
			justify-self: center;
		}

		.qr img {
			inline-size: 168px;
			block-size: 168px;
		}
	}

	@container (width < 300px) {
		.big {
			grid-template-columns: minmax(0, 1fr);
			gap: var(--sp-4);
		}

		.cell + .cell {
			padding-inline-start: 0;
			padding-block-start: var(--sp-4);
			border-inline-start: none;
			border-block-start: 1px solid var(--hairline);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.qr {
			transition: none;
		}
	}
</style>
