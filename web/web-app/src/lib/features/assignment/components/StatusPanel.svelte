<script lang="ts">
	import type { MoodleAssignment } from '@mybicocca/data-api/elearning';
	import { online } from 'svelte/reactivity/window';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconCalendarBlank from '~icons/ph/calendar-blank';
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconClipboardText from '~icons/ph/clipboard-text';
	import IconClockCountdown from '~icons/ph/clock-countdown';
	import IconHourglassMedium from '~icons/ph/hourglass-medium';
	import IconListChecks from '~icons/ph/list-checks';
	import IconLockSimple from '~icons/ph/lock-simple';
	import IconMedal from '~icons/ph/medal';
	import IconNotePencil from '~icons/ph/note-pencil';
	import IconPaperPlaneTilt from '~icons/ph/paper-plane-tilt';
	import IconPencilSimple from '~icons/ph/pencil-simple';
	import IconPlus from '~icons/ph/plus';
	import IconRepeat from '~icons/ph/repeat';
	import IconSealCheck from '~icons/ph/seal-check';
	import IconTimer from '~icons/ph/timer';
	import IconTrash from '~icons/ph/trash';
	import IconWarningCircle from '~icons/ph/warning-circle';
	import IconCloudWarning from '~icons/ph/cloud-warning';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { DataError } from '#lib/data/errors.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { Icon } from '#lib/ui/types.ts';
	import { moodleAssignmentUrl } from '../assignment.svelte.ts';
	import {
		countdownOf,
		formatMoment,
		formatSpan,
		gradeParts,
		onMoodleReason,
		partsText
	} from '../labels.ts';
	import { editorPath } from '../links.ts';
	import {
		awaitsStart,
		handedIn,
		nothingOnline,
		onMoodleOnly,
		phaseOf,
		submittable,
		timedEnd,
		timeLimitOf,
		urgencyOf,
		type Schedule,
		type Submission,
		type SubmissionConfig
	} from '../model.ts';

	interface Props {
		assignment: MoodleAssignment;
		schedule: Schedule;
		submission: Submission | undefined;
		config: SubmissionConfig | undefined;
		/** The status failed to load and none is cached. */
		error: DataError | undefined;
		retrying: boolean;
		onretry: () => void;
		now: number;
		onsubmit: () => void;
		onremove: () => void;
		/** Asks to start a timed assignment's clock. */
		onstart: () => void;
	}

	let {
		assignment,
		schedule,
		submission,
		config,
		error,
		retrying,
		onretry,
		now,
		onsubmit,
		onremove,
		onstart
	}: Props = $props();

	const phase = $derived(phaseOf(schedule, now, submission?.canEdit));
	const urgency = $derived(urgencyOf(schedule, submission?.stage, now, submission?.canEdit));
	const editorHref = $derived(editorPath(assignment.course, assignment.id));
	const canSubmitHere = $derived(submittable(config));
	const editable = $derived(
		submission !== undefined && submission.canEdit && phase !== 'upcoming' && canSubmitHere
	);
	const isOnline = $derived(online.current !== false);
	const onMoodle = $derived(onMoodleOnly(config));
	/* The action already leads to Moodle's page: no second link to it. */
	const siteAction = $derived(submission !== undefined && onMoodle && !handedIn(submission.stage));

	const headline = $derived.by((): { text: string; icon: Icon; tone: string } | null => {
		if (submission === undefined || config === undefined) return null;
		switch (submission.stage) {
			case 'graded':
				return { text: m.assignment_headline_graded(), icon: IconMedal, tone: 'success' };
			case 'submitted':
				return { text: m.assignment_headline_submitted(), icon: IconSealCheck, tone: 'success' };
			case 'draft':
				return { text: m.assignment_headline_draft(), icon: IconNotePencil, tone: 'info' };
			case 'new':
				break;
		}
		if (nothingOnline(config)) {
			return { text: m.assignment_headline_none(), icon: IconClipboardText, tone: 'neutral' };
		}
		switch (phase) {
			case 'upcoming':
				return {
					text: m.assignment_headline_upcoming(),
					icon: IconHourglassMedium,
					tone: 'neutral'
				};
			case 'open':
				return {
					text: m.assignment_headline_todo(),
					icon: IconClipboardText,
					tone: urgency === 'soon' ? 'warning' : 'course'
				};
			case 'late':
				return {
					text: m.assignment_headline_late(),
					icon: IconWarningCircle,
					tone: urgency === 'overdue' ? 'danger' : 'neutral'
				};
			case 'closed':
				return { text: m.assignment_headline_missed(), icon: IconLockSimple, tone: 'neutral' };
		}
	});

	const limit = $derived(timeLimitOf(config, submission));
	const mustStart = $derived(awaitsStart(config, submission));
	const countdown = $derived(
		submission &&
			countdownOf(schedule, submission, phase, now, timedEnd(schedule, submission, config))
	);
	const grade = $derived(submission?.grade ? gradeParts(submission.grade) : null);

	interface Fact {
		label: string;
		/** A line each. */
		values: string[];
		icon: Icon;
	}

	/* The due date is the page's subtitle: only an extension, which moves it, is a fact here. */
	const facts = $derived.by((): Fact[] => {
		const rows: Fact[] = [];
		if (schedule.opens !== null && schedule.opens > now) {
			rows.push({
				label: m.assignment_fact_opens(),
				values: [formatMoment(schedule.opens)],
				icon: IconCalendarPlus
			});
		}
		if (schedule.originalDue !== null && schedule.due !== null) {
			rows.push({
				label: m.assignment_fact_extension(),
				values: [
					m.assignment_fact_extension_value({
						moment: formatMoment(schedule.due),
						original: formatMoment(schedule.originalDue)
					})
				],
				icon: IconCalendarBlank
			});
		}
		const open = submission === undefined || !handedIn(submission.stage);
		if (
			open &&
			schedule.cutoff !== null &&
			schedule.cutoff > now &&
			schedule.cutoff !== schedule.due
		) {
			rows.push({
				label: m.assignment_fact_cutoff(),
				values: [m.assignment_fact_cutoff_value({ moment: formatMoment(schedule.cutoff) })],
				icon: IconClockCountdown
			});
		}
		/* Once the status says the work is still to do: a handed-in one would drop it again. */
		if (submission !== undefined && open && phase !== 'closed' && canSubmitHere && config) {
			rows.push({
				label: m.assignment_fact_parts(),
				values: partsText(config),
				icon: IconListChecks
			});
		}
		if (limit !== null && canSubmitHere) {
			const started = submission?.startedAt ?? null;
			rows.push({
				label: m.assignment_fact_time_limit(),
				values: [
					formatSpan(limit),
					...(started === null
						? []
						: [m.assignment_fact_started({ moment: formatMoment(started) })])
				],
				icon: IconTimer
			});
		}
		const saved = submission?.savedAt;
		if (saved && (submission?.stage === 'submitted' || submission?.stage === 'graded')) {
			const late = schedule.due !== null && saved > schedule.due;
			rows.push({
				label: m.assignment_fact_submitted(),
				values: [late ? `${formatMoment(saved)} · ${m.assignment_late()}` : formatMoment(saved)],
				icon: IconCheckCircle
			});
		} else if (saved && submission?.stage === 'draft') {
			rows.push({
				label: m.assignment_fact_saved(),
				values: [formatMoment(saved)],
				icon: IconNotePencil
			});
		}
		const attempt = submission?.attempt ?? 1;
		if (attempt > 1) {
			rows.push({
				label: m.assignment_fact_attempt(),
				values: [
					assignment.maxAttempts >= 1
						? m.assignment_attempt_number({ number: attempt, max: assignment.maxAttempts })
						: m.assignment_attempt_number_open({ number: attempt })
				],
				icon: IconRepeat
			});
		} else if (assignment.maxAttempts >= 1) {
			rows.push({
				label: m.assignment_fact_attempts(),
				values: [m.assignment_attempts_value({ count: assignment.maxAttempts })],
				icon: IconRepeat
			});
		}
		return rows;
	});
</script>

<section class="panel" aria-labelledby="assignment-status">
	<div class="body">
		<div class="status">
			<h2 class="eyebrow" id="assignment-status" tabindex="-1">{m.assignment_status_label()}</h2>

			{#if headline}
				<p class={['headline', headline.tone]}>
					<span class="glyph" aria-hidden="true"><headline.icon /></span>
					{headline.text}
				</p>
				{#if countdown}
					<p class={['countdown', urgency]}>{countdown.text}</p>
					{#if countdown.late}<p class="late">{countdown.late}</p>{/if}
				{/if}
			{:else if error}
				<div class="failure" role="alert">
					<span class="failure-glyph" aria-hidden="true"><IconCloudWarning /></span>
					<div class="failure-text">
						<p class="failure-title">{m.assignment_status_failed()}</p>
						<p class="failure-cause">{errorMessage(error)}</p>
					</div>
					<div class="failure-action">
						<Button
							variant="tonal"
							size="sm"
							icon={IconArrowClockwise}
							loading={retrying}
							onclick={onretry}
						>
							{m.common_retry()}
						</Button>
					</div>
				</div>
			{:else}
				<div class="loading" aria-busy="true">
					<Skeleton width="62%" height="1.6em" />
					<Skeleton width="44%" height="0.9em" />
				</div>
			{/if}
		</div>

		{#if grade && submission?.grade}
			<div class="grade">
				<p class="grade-label">{m.assignment_grade_label()}</p>
				<p class="grade-value">
					{grade.value}{#if grade.max}<span class="max">/{grade.max}</span>{/if}
				</p>
				{#if submission.grade.gradedAt}
					<p class="graded-at">
						{m.assignment_graded_on({ date: formatDate(submission.grade.gradedAt, 'dayMonth') })}
					</p>
				{/if}
			</div>
		{/if}

		{#if facts.length > 0}
			<dl class="facts">
				{#each facts as fact (fact.label)}
					<div class="fact">
						<dt>
							<span class="tile" aria-hidden="true"><fact.icon /></span>
							{fact.label}
						</dt>
						{#each fact.values as value, index (index)}<dd>{value}</dd>{/each}
					</div>
				{/each}
			</dl>
		{/if}

		{#if submission && config}
			{#if editable || submission.stage !== 'graded'}
				<div class="actions">
					{#if editable}
						{#if submission.state === 'draft'}
							{#if submission.canSubmit}
								<Button
									variant="solid"
									size="lg"
									full
									icon={IconPaperPlaneTilt}
									disabled={!isOnline}
									onclick={onsubmit}
								>
									{m.assignment_submit_for_grading()}
								</Button>
							{/if}
							<Button
								variant={submission.canSubmit ? 'tonal' : 'solid'}
								size="lg"
								full
								icon={IconPencilSimple}
								href={isOnline ? editorHref : undefined}
								disabled={!isOnline}
							>
								{m.assignment_edit_draft()}
							</Button>
						{:else if submission.state === 'submitted'}
							<Button
								variant="tonal"
								size="lg"
								full
								icon={IconPencilSimple}
								href={isOnline ? editorHref : undefined}
								disabled={!isOnline}
							>
								{m.assignment_edit()}
							</Button>
						{:else if mustStart && limit !== null}
							<Button
								variant="solid"
								size="lg"
								full
								icon={IconTimer}
								disabled={!isOnline}
								onclick={onstart}
							>
								{m.assignment_begin()}
							</Button>
							<p class="note">{m.assignment_begin_note({ span: formatSpan(limit) })}</p>
						{:else}
							<Button
								variant="solid"
								size="lg"
								full
								icon={phase === 'late' ? IconPaperPlaneTilt : IconPlus}
								href={isOnline ? editorHref : undefined}
								disabled={!isOnline}
							>
								{phase === 'late' ? m.assignment_add_late() : m.assignment_add()}
							</Button>
						{/if}
						{#if submission.state !== 'new'}
							<Button variant="ghost" icon={IconTrash} disabled={!isOnline} onclick={onremove}>
								{m.assignment_remove()}
							</Button>
						{/if}
						{#if !isOnline}<p class="note">{m.assignment_offline_hint()}</p>{/if}
						{#if submission.state === 'draft'}<p class="note">{m.assignment_draft_note()}</p>{/if}
					{:else if submission.stage === 'submitted'}
						<p class="note">{m.assignment_locked_note()}</p>
					{:else if onMoodle}
						<p class="note">{onMoodleReason(config)}</p>
						<Button
							variant="solid"
							size="lg"
							full
							icon={IconArrowSquareOut}
							href={moodleAssignmentUrl(assignment.cmId)}
						>
							{m.assignment_on_moodle()}
						</Button>
					{:else if !canSubmitHere}
						<p class="note">{m.assignment_nothing_to_submit()}</p>
					{:else}
						<Button variant="tonal" size="lg" full disabled>
							{phase === 'upcoming' ? m.assignment_not_open_yet() : m.assignment_closed()}
						</Button>
					{/if}
				</div>
			{/if}
		{:else if !error}
			<div class="actions" aria-hidden="true">
				<Skeleton width="100%" height="48px" radius="999px" />
			</div>
		{/if}

		<!-- Once the status is in: below the card's growing content, it would move as it arrives. -->
		{#if (submission !== undefined || error) && !siteAction}
			<a
				class="site"
				href={moodleAssignmentUrl(assignment.cmId)}
				target="_blank"
				rel="noopener external"
			>
				{m.assignment_open_on_site()}
				<IconArrowSquareOut aria-hidden="true" />
			</a>
		{/if}
	</div>
</section>

<style>
	.panel {
		container: status / inline-size;
		padding: 20px;
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.body {
		display: grid;
		gap: var(--sp-4);
	}

	.status {
		display: grid;
		align-content: start;
		gap: var(--sp-4);
	}

	.eyebrow {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	/* Where focus lands once the state changed under the button that had it. */
	.eyebrow:focus {
		outline: none;
	}

	.eyebrow:focus-visible {
		border-radius: var(--r-xs);
		outline: 2px solid var(--focus);
		outline-offset: 4px;
	}

	.headline {
		display: flex;
		align-items: center;
		gap: 10px;
		margin-block-start: -4px;
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		border-radius: 50%;
		background: var(--h-bg);
		color: var(--h-fg);
	}

	.glyph :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.course {
		--h-bg: var(--course-container);
		--h-fg: var(--course-ink);
	}

	.success {
		--h-bg: var(--success-container);
		--h-fg: var(--on-success-container);
	}

	.info {
		--h-bg: var(--info-container);
		--h-fg: var(--on-info-container);
	}

	.warning {
		--h-bg: var(--warning-container);
		--h-fg: var(--on-warning-container);
	}

	.danger {
		--h-bg: var(--danger-container);
		--h-fg: var(--on-danger-container);
	}

	.neutral {
		--h-bg: var(--surface-sunken);
		--h-fg: var(--text-muted);
	}

	.countdown {
		margin-block-start: -8px;
		color: var(--text-muted);
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	.countdown.soon {
		color: var(--warning);
	}

	.countdown.overdue {
		color: var(--danger);
	}

	.late {
		margin-block-start: -12px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.loading {
		display: grid;
		gap: 10px;
	}

	.failure {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 10px 12px;
		padding: 14px;
		border-radius: var(--r-lg);
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.failure-glyph :global(svg) {
		display: block;
		inline-size: 22px;
		block-size: 22px;
	}

	.failure-text {
		display: grid;
		gap: 2px;
	}

	.failure-title {
		font: var(--type-ui-strong);
	}

	.failure-cause {
		font: var(--type-meta);
		opacity: 0.85;
	}

	.failure-action {
		grid-column: 2;
	}

	.grade {
		display: grid;
		gap: 4px;
		padding: 16px 18px;
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
	}

	.grade-label {
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		opacity: 0.85;
	}

	.grade-value {
		font: var(--type-numeral-xl);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.max {
		margin-inline-start: 2px;
		font: 500 1.5rem / 1 var(--font-display);
		opacity: 0.7;
	}

	.graded-at {
		font: var(--type-meta);
		opacity: 0.85;
	}

	.facts {
		display: grid;
		gap: 12px;
		margin: 0;
	}

	.fact {
		position: relative;
		display: grid;
		align-content: center;
		gap: 1px;
		min-block-size: 40px;
		padding-inline-start: 54px;
	}

	.fact dt {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.tile {
		position: absolute;
		inset-block-start: calc(50% - 20px);
		inset-inline-start: 0;
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		border-radius: 13px;
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.tile :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.fact dd {
		margin: 0;
		color: var(--text);
		font: var(--type-body);
		overflow-wrap: anywhere;
	}

	.actions {
		display: grid;
		gap: 8px;
		margin-block-start: 4px;
	}

	.note {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.site {
		display: inline-flex;
		align-items: center;
		justify-self: start;
		gap: 6px;
		padding: 4px 8px;
		margin: -4px -8px;
		border-radius: var(--r-xs);
		color: var(--text-muted);
		font: var(--type-meta);
		text-decoration: none;
	}

	.site :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	@media (hover: hover) {
		.site:hover {
			background: var(--state-hover);
			color: var(--text);
		}
	}

	/*
	 * As wide as the page on medium screens: the grade sits beside the state, the facts run across
	 * in columns and the actions in a row, so the card stays as short as its content.
	 */
	@container status (width >= 560px) {
		.body {
			grid-template-columns: minmax(0, 1fr) auto;
			column-gap: var(--sp-6);
		}

		.body > * {
			grid-column: 1 / -1;
		}

		.status {
			grid-row: 1;
			grid-column: 1;
		}

		.grade {
			grid-row: 1;
			grid-column: 2;
			align-self: start;
			min-inline-size: 180px;
		}

		.facts {
			grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
			column-gap: var(--sp-5);
		}

		.actions {
			display: flex;
			flex-wrap: wrap;
			align-items: center;
		}

		.actions > :global(.btn.full) {
			inline-size: auto;
		}

		.actions > .note {
			flex-basis: 100%;
		}

		.actions[aria-hidden='true'] > :global(*) {
			max-inline-size: 280px;
		}
	}
</style>
