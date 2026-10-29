<script lang="ts" module>
	export interface BookingSummary {
		office: string;
		day: string;
		time: string;
		where: string;
		email: string;
	}

	/** A failed booking as the dialog tells it: what went wrong, and what became of the slot. */
	export interface BookingFailure {
		message: string;
		note?: string;
		/** The time is still free, so booking it again may work; otherwise another must be picked. */
		retry: boolean;
	}
</script>

<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCheck from '~icons/ph/check-bold';
	import IconClock from '~icons/ph/clock';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import type { BookingStep } from '../booking.ts';
	import DayTime from './DayTime.svelte';

	interface Props {
		open: boolean;
		summary: BookingSummary;
		/** Books, telling each step; rejects with what the page should say, and never resolves unbooked. */
		run: (onStep: (step: BookingStep) => void) => Promise<void>;
		describe: (error: unknown) => BookingFailure;
		/** Closed without booking: Back, Close, Choose another time, Escape or a click outside. */
		onclosed?: () => void;
	}

	let { open = $bindable(), summary, run, describe, onclosed }: Props = $props();

	const STEPS: ReadonlyArray<{ step: BookingStep; label: () => string }> = [
		{ step: 'checking', label: m.appointments_step_checking },
		{ step: 'holding', label: m.appointments_step_holding },
		{ step: 'confirming', label: m.appointments_step_confirming }
	];

	/** After this long in one step the page explains the portal's pause. */
	const SLOW_MS = 4_000;

	let phase = $state<'confirm' | 'running' | 'failed'>('confirm');
	let step = $state<BookingStep>('checking');
	let slow = $state(false);
	let failure = $state<BookingFailure>({ message: '', retry: true });
	let timer: ReturnType<typeof setTimeout> | undefined;

	const current = $derived(STEPS.findIndex((item) => item.step === step));

	function enter(next: BookingStep) {
		step = next;
		slow = false;
		clearTimeout(timer);
		timer = setTimeout(() => (slow = true), SLOW_MS);
	}

	async function book() {
		phase = 'running';
		enter('checking');
		try {
			await run(enter);
			open = false;
			phase = 'confirm';
		} catch (error) {
			failure = describe(error);
			phase = 'failed';
		} finally {
			clearTimeout(timer);
			slow = false;
		}
	}

	function reset() {
		if (phase !== 'running') phase = 'confirm';
		onclosed?.();
	}

	function close() {
		open = false;
		reset();
	}

	$effect(() => () => clearTimeout(timer));

	/*
	 * Each phase replaces the buttons, and the focused one with them: the new phase takes the
	 * focus, so the keyboard stays in the dialog.
	 */
	function focus(node: HTMLElement) {
		node.focus();
	}
</script>

<Dialog
	bind:open
	title={phase === 'running'
		? m.appointments_progress_title()
		: phase === 'failed'
			? m.appointments_booking_failed_title()
			: m.appointments_confirm_title()}
	size="sm"
	role="alertdialog"
	dismissible={phase !== 'running'}
	onclose={reset}
>
	{#if phase === 'confirm'}
		<p class="lead">{m.appointments_confirm_body()}</p>
		<dl class="summary">
			<div>
				<dt>{m.appointments_fact_office()}</dt>
				<dd>{summary.office}</dd>
			</div>
			<div>
				<dt>{m.appointments_fact_when()}</dt>
				<dd><DayTime day={summary.day} time={summary.time} /></dd>
			</div>
			<div>
				<dt>{m.appointments_fact_where()}</dt>
				<dd>{summary.where}</dd>
			</div>
			<div>
				<dt>{m.appointments_fact_email()}</dt>
				<dd translate="no">{summary.email}</dd>
			</div>
		</dl>
	{:else if phase === 'running'}
		<ol
			class="steps"
			aria-live="polite"
			aria-label={m.appointments_progress_title()}
			tabindex="-1"
			{@attach focus}
		>
			{#each STEPS as item, index (item.step)}
				<li class={['step', { done: index < current, active: index === current }]}>
					<span class="mark" aria-hidden="true">
						{#if index < current}<IconCheck />{:else if index === current}<Spinner size={18} />{/if}
					</span>
					<span>{item.label()}</span>
				</li>
			{/each}
		</ol>
		<p class={['slow', { shown: slow }]} role="status">
			{slow ? m.appointments_booking_slow() : ''}
		</p>
	{:else}
		<div class="failure">
			<Banner tone="danger" message={failure.message} role="alert" />
			{#if failure.note}<p class="lead">{failure.note}</p>{/if}
		</div>
	{/if}

	{#snippet footer()}
		{#if phase === 'confirm'}
			<Button variant="ghost" onclick={close} data-autofocus>
				{m.ui_back()}
			</Button>
			<Button variant="solid" onclick={book}>{m.appointments_confirm_action()}</Button>
		{:else if phase === 'failed' && failure.retry}
			<Button variant="ghost" onclick={close}>
				{m.ui_close()}
			</Button>
			<Button variant="solid" icon={IconArrowClockwise} onclick={book} {@attach focus}>
				{m.common_retry()}
			</Button>
		{:else if phase === 'failed'}
			<Button variant="solid" icon={IconClock} onclick={close} {@attach focus}>
				{m.appointments_choose_time()}
			</Button>
		{/if}
	{/snippet}
</Dialog>

<style>
	.lead {
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.summary {
		display: grid;
		gap: var(--sp-3);
		margin: var(--sp-4) 0 0;
		padding: var(--sp-4);
		border-radius: var(--r-md);
		background: var(--surface-sunken);
	}

	.summary div {
		display: grid;
		gap: 1px;
	}

	dt {
		color: var(--text-muted);
		font: var(--type-label);
	}

	dd {
		margin: 0;
		font: var(--type-ui-strong);
		overflow-wrap: anywhere;
	}

	.steps {
		display: grid;
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
		outline: none;
	}

	.step {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		color: var(--text-subtle);
		font: var(--type-ui);
		transition: color var(--dur-fast) var(--ease-out);
	}

	.step.active {
		color: var(--text);
		font-weight: 600;
	}

	.step.done {
		color: var(--text-muted);
	}

	.mark {
		display: grid;
		place-items: center;
		inline-size: 26px;
		block-size: 26px;
		border-radius: var(--r-pill);
		box-shadow: inset 0 0 0 1.5px var(--hairline-strong);
	}

	.done .mark {
		background: var(--success-container);
		color: var(--on-success-container);
		box-shadow: none;
	}

	.active .mark {
		box-shadow: none;
	}

	.mark :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.slow {
		min-block-size: 3lh;
		margin-block-start: var(--sp-4);
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
		opacity: 0;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.slow.shown {
		opacity: 1;
	}

	.failure {
		display: grid;
		gap: var(--sp-3);
	}
</style>
