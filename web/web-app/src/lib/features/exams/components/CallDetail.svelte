<script lang="ts">
	import IconCalendarCheck from '~icons/ph/calendar-check';
	import IconGraduation from '~icons/ph/graduation-cap';
	import IconGavel from '~icons/ph/gavel';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import IconNotePencil from '~icons/ph/note-pencil';
	import IconUsers from '~icons/ph/users-three';
	import { network } from '#lib/data/network.svelte.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import type { SessionScope } from '#lib/data/session.svelte.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Banner from '#lib/ui/Banner.svelte';
	import { capitalize } from '#lib/format/names.ts';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import DateBadge from '#lib/ui/DateBadge.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import Textarea from '#lib/ui/Textarea.svelte';
	import {
		callState,
		examCountdown,
		longWhen,
		sentenceWhen,
		windowRange,
		windowSentence
	} from '../format.ts';
	import { callLabel, canBook, windowStatus, type ExamCall } from '../model.ts';
	import { enrolledCountQuery } from '../queries.ts';
	import { bookCall, mentionsQuestionnaire, refusalReason, writeFailure } from '../writes.ts';

	interface Props {
		scope: SessionScope;
		call: ExamCall;
		now: Date;
	}

	let { scope, call, now }: Props = $props();

	/* Esse3 stores the student's note in a 500-character column. */
	const NOTE_MAX = 500;

	let note = $state('');
	let noting = $state(false);
	let confirming = $state(false);
	let submitting = $state(false);
	let refusal = $state<unknown>();

	const chip = $derived(callState(call, now));
	const open = $derived(windowStatus(call, now) === 'open');
	const bookable = $derived(canBook(call, now));
	const recorded = $derived((call.activityChoiceId ?? 0) > 0);
	const soon = $derived(call.date ? examCountdown(call.date, now) : undefined);
	const reason = $derived(refusal === undefined ? undefined : refusalReason(refusal));
	const when = $derived(call.date ? longWhen(call.date, call.timed) : m.exams_time_tbc());
	const label = $derived(callLabel(call.description, call.title));

	/* The list brings the count; only a call without one asks for it, which is slow. */
	const count = query(() => call.enrolled === null && enrolledCountQuery(scope, call.key));
	const enrolled = $derived(call.enrolled ?? count.data ?? null);

	const facts = $derived.by((): Fact[] => {
		const items: Fact[] = [];
		const range = windowRange(call);
		if (range) {
			items.push({ label: m.exams_fact_registrations(), value: range, icon: IconHourglass });
		}
		if (enrolled !== null && enrolled > 0) {
			items.push({
				label: m.exams_fact_enrolled(),
				value: m.exams_students({ count: enrolled }),
				icon: IconUsers
			});
		}
		if (call.bookingMode) {
			items.push({
				label: m.exams_fact_booking_mode(),
				value: call.bookingMode,
				icon: IconCalendarCheck
			});
		}
		if (call.president) {
			items.push({ label: m.exams_fact_president(), value: call.president, icon: IconGavel });
		}
		if (call.degree) {
			items.push({ label: m.exams_fact_degree(), value: call.degree, icon: IconGraduation });
		}
		return items;
	});

	const confirmMessage = $derived.by(() => {
		const at = call.date ? capitalize(sentenceWhen(call.date, call.timed)) : m.exams_time_tbc();
		return call.closesAt
			? m.exams_book_confirm_message({ when: at, deadline: formatDate(call.closesAt, 'full') })
			: m.exams_book_confirm_message_open({ when: at });
	});

	async function book() {
		if (submitting) return;
		submitting = true;
		refusal = undefined;
		try {
			await bookCall(scope, call, note);
		} catch (error) {
			// Esse3 said no for a reason of its own: retrying won't help, so the reason stays on the page.
			if (refusalReason(error) !== undefined) {
				refusal = error;
				return;
			}
			throw error;
		} finally {
			submitting = false;
		}
	}

	/* The field appears because the student asked for it: they type straight away. */
	function focusField(node: HTMLElement) {
		node.querySelector('textarea')?.focus();
	}

	function guard(event: BeforeUnloadEvent) {
		if (submitting) event.preventDefault();
	}
</script>

<svelte:window onbeforeunload={guard} />

<article class="call" aria-label={call.title || m.exams_call_unknown()}>
	<div class="panel">
		{#if label}<p class="label">{label}</p>{/if}
		<div class="when">
			{#if call.date}
				<DateBadge date={call.date} size="lg" tone={open ? 'accent' : 'neutral'} />
			{/if}
			<div class="when-text">
				<p class="date">{when}</p>
				<p class="chips">
					<StatusChip status={chip.status} label={chip.long} size="sm" />
					{#if soon}<span class="soon">{soon}</span>{/if}
				</p>
			</div>
		</div>

		<div class="book">
			<p class="sentence">{windowSentence(call, now)}</p>
			{#if bookable}
				{#if noting}
					<div class="note" {@attach focusField}>
						<Textarea
							label={m.exams_note_label()}
							description={m.exams_note_hint()}
							optional
							rows={2}
							maxRows={6}
							maxlength={NOTE_MAX}
							bind:value={note}
							disabled={submitting}
						/>
					</div>
				{:else}
					<div class="add-note">
						<Button variant="ghost" size="sm" icon={IconNotePencil} onclick={() => (noting = true)}>
							{m.exams_add_note()}
						</Button>
					</div>
				{/if}
				{#if reason}
					<Banner tone="danger" title={m.exams_refused_title()} role="alert">
						{m.exams_refusal_quote({ reason })}
						{#snippet actions()}
							{#if mentionsQuestionnaire(refusal)}
								<Button size="sm" href={shellHref('services/questionnaires')}>
									{m.exams_questionnaires_link()}
								</Button>
							{/if}
						{/snippet}
					</Banner>
				{/if}
				<div class="cta">
					<Button
						variant="solid"
						size="lg"
						icon={IconCalendarCheck}
						disabled={!network.online}
						loading={submitting}
						onclick={() => (confirming = true)}
					>
						{m.exams_book_cta()}
					</Button>
					{#if !network.online}<p class="offline" role="status">{m.exams_offline_write()}</p>{/if}
				</div>
			{:else if !recorded}
				<p class="unavailable">{m.exams_not_bookable()}</p>
			{/if}
		</div>
	</div>

	{#if facts.length > 0}
		<Facts items={facts} label={m.exams_call_details()} />
	{/if}

	{#if call.notes}
		<section class="notes" aria-label={m.exams_call_notes()}>
			<h3 class="notes-title">{m.exams_call_notes()}</h3>
			<p class="notes-text">{call.notes}</p>
		</section>
	{/if}
</article>

<ConfirmDialog
	bind:open={confirming}
	title={m.exams_book_confirm_title({ course: call.title || m.exams_call_unknown() })}
	message={confirmMessage}
	confirmLabel={m.exams_book_confirm()}
	cancelLabel={m.exams_not_now()}
	onconfirm={book}
	errorMessage={writeFailure}
/>

<style>
	.call {
		display: grid;
		gap: var(--sp-5);
		align-content: start;
	}

	.panel {
		display: grid;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.label {
		padding: var(--sp-4) var(--sp-5) 0;
		color: var(--accent-text);
		font: var(--type-ui-strong);
		overflow-wrap: anywhere;
	}

	.label + .when {
		padding-block-start: var(--sp-3);
	}

	.when {
		display: flex;
		align-items: center;
		gap: var(--sp-4);
		padding: var(--sp-5);
	}

	.when-text {
		display: grid;
		gap: 8px;
		min-inline-size: 0;
	}

	.date {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
		font-variant-numeric: tabular-nums;
	}

	.chips {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px;
	}

	.soon {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.book {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-4) var(--sp-5) var(--sp-5);
		border-block-start: 1px solid var(--hairline);
	}

	.sentence {
		font: var(--type-body);
		text-wrap: pretty;
	}

	.add-note {
		margin-block: calc(-1 * var(--sp-2));
		margin-inline-start: -10px;
	}

	.cta {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-3);
	}

	.offline,
	.unavailable {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.notes {
		display: grid;
		gap: var(--sp-2);
		padding: var(--sp-4) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
	}

	.notes-title {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.notes-text {
		white-space: pre-line;
		overflow-wrap: anywhere;
		text-wrap: pretty;
	}

	@container (width < 420px) {
		.when {
			padding: var(--sp-4);
		}

		.book {
			padding-inline: var(--sp-4);
		}

		.cta > :global(.btn) {
			inline-size: 100%;
		}
	}

	@supports (corner-shape: squircle) {
		.panel,
		.notes {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
