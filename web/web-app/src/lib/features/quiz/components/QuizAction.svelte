<script lang="ts">
	import { goto } from '$app/navigation';
	import type {
		MoodleQuiz,
		MoodleQuizAccessInformation,
		MoodleQuizAttempt
	} from '@mybicocca/data-api/elearning';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import IconPlay from '~icons/ph/play-fill';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { DataFailure, toDataError } from '#lib/data/errors.ts';
	import { mutation } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatClockLong } from '#lib/features/course/format.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import { formatInstant, quizErrorMessage } from '../format.ts';
	import { attemptPath, moodleAttemptUrl } from '../links.ts';
	import {
		attemptDeadline,
		isOverdue,
		maxAttempts,
		quizAction,
		ranOut,
		type AttemptAccess
	} from '../model.ts';
	import { attemptChanges } from '../queries.ts';

	interface Props {
		quiz: MoodleQuiz;
		attempts: readonly MoodleQuizAttempt[];
		access: MoodleQuizAccessInformation | undefined;
		/** Moodle's rules for the open attempt, else the last one. */
		attemptAccess: AttemptAccess | undefined;
		courseId: number;
		online: boolean;
		now: Date;
	}

	let { quiz, attempts, access, attemptAccess, courseId, online, now }: Props = $props();

	const action = $derived(quizAction(quiz, attempts, now, access, attemptAccess));
	/* Time ran out with a grace period: the attempt only needs submitting. */
	const overdue = $derived(action.kind === 'resume' && isOverdue(action.attempt));
	const minutes = $derived(Math.floor((quiz.timeLimit ?? 0) / 60));
	const max = $derived(maxAttempts(quiz));

	let confirming = $state(false);

	const start = mutation(
		async () => {
			const scope = session.current;
			if (scope === undefined)
				throw new DataFailure({ kind: 'auth', service: 'moodle', reason: 'reauth_required' });
			const response = await scope.api.moodle((moodle, auth) =>
				moodle.quiz.startAttempt(quiz.id, auth, { preflightData: [], forceNew: false })
			);
			return response.attempt;
		},
		() => {
			const scope = session.current;
			return { invalidate: scope ? attemptChanges(scope, courseId, quiz.id) : [] };
		}
	);

	async function begin() {
		const result = await start.run();
		if (!result.ok) throw new DataFailure(result.error);
		const { id, currentPage } = result.value;
		if (id !== null) void goto(attemptPath(courseId, quiz.id, id, currentPage ?? 0));
	}

	const label = $derived.by(() => {
		switch (action.kind) {
			case 'resume':
				return overdue ? m.quiz_submit_overdue() : m.quiz_resume();
			case 'site':
				return m.quiz_open_moodle();
			case 'start':
				return m.quiz_start();
			case 'blocked':
				return {
					upcoming: m.quiz_blocked_upcoming,
					closed: m.quiz_blocked_closed,
					exhausted: m.quiz_blocked_exhausted,
					empty: m.quiz_blocked_empty,
					denied: m.quiz_blocked_denied
				}[action.reason]();
		}
	});

	const hint = $derived.by(() => {
		if (action.kind === 'site') return m.quiz_error_access();
		if (action.kind === 'resume') {
			const { attempt } = action;
			const number = attempt.attempt ?? 0;
			if (overdue) {
				/* Overdue, Moodle's check time is when it stops waiting for the submission. */
				const until = attempt.timeCheckState ? attempt.timeCheckState * 1000 : null;
				return until === null
					? m.quiz_overdue_hint_open({ number })
					: m.quiz_overdue_hint({ number, date: formatInstant(new Date(until)) });
			}
			if (ranOut(attempt, attemptAccess, now.getTime())) {
				/* Only a grace period keeps it for the student to submit; else Moodle closes it. */
				return quiz.overdueHandling === 'graceperiod'
					? m.quiz_resume_hint_expired({ number })
					: m.quiz_resume_hint_time_up({ number });
			}
			const deadline = attemptDeadline(attempt, attemptAccess);
			if (deadline !== null && (quiz.timeLimit ?? 0) > 0) {
				return m.quiz_resume_hint_timed({
					number,
					left: formatClockLong((deadline - now.getTime()) / 1000)
				});
			}
			return m.quiz_resume_hint({
				number,
				date: attempt.timeStart ? formatInstant(new Date(attempt.timeStart * 1000)) : ''
			});
		}
		if (action.kind === 'start') {
			const number =
				max === null
					? m.quiz_attempt_n({ number: action.number })
					: m.quiz_attempt_n_of({ number: action.number, max });
			return minutes > 0 ? m.quiz_start_hint_timed({ attempt: number, minutes }) : number;
		}
		return undefined;
	});

	/* Moodle's own words for why no attempt can start now, such as the wait between attempts. */
	const reasons = $derived([
		...new Set([
			...(access?.preventAccessReasons ?? []),
			...(attemptAccess?.preventnewattemptreasons ?? [])
		])
	]);

	const message = $derived(
		[
			max === null || action.kind !== 'start'
				? null
				: m.quiz_start_message_limited({ number: action.number, max }),
			minutes > 0 ? m.quiz_start_message_timed({ minutes }) : m.quiz_start_message()
		]
			.filter(Boolean)
			.join(' ')
	);
</script>

<div class="action">
	{#if action.kind === 'site' && action.attempt.id !== null}
		<Button
			variant="solid"
			size="lg"
			full
			trailingIcon={IconArrowSquareOut}
			href={moodleAttemptUrl(action.attempt.id, quiz.courseModule, action.attempt.currentPage ?? 0)}
		>
			{label}
		</Button>
	{:else if action.kind === 'resume' && action.attempt.id !== null}
		<Button
			variant="solid"
			size="lg"
			full
			icon={overdue ? IconPaperPlane : IconPlay}
			href={online
				? attemptPath(courseId, quiz.id, action.attempt.id, action.attempt.currentPage ?? 0)
				: undefined}
			disabled={!online}
		>
			{label}
		</Button>
	{:else}
		<Button
			variant={action.kind === 'start' ? 'solid' : 'tonal'}
			size="lg"
			full
			trailingIcon={action.kind === 'start' ? IconArrowRight : undefined}
			disabled={action.kind !== 'start' || !online}
			onclick={() => (confirming = true)}
		>
			{label}
		</Button>
	{/if}
	{#if !online && action.kind !== 'blocked'}
		<p class="hint offline"><IconWifiSlash aria-hidden="true" />{m.quiz_offline_hint()}</p>
	{:else if hint}
		<p class="hint">{hint}</p>
	{/if}
	{#if action.kind === 'blocked' && action.reason === 'denied' && reasons.length > 0}
		<Banner tone="warning">
			{#each reasons as reason (reason)}<p>{reason}</p>{/each}
		</Banner>
	{/if}
</div>

<ConfirmDialog
	bind:open={confirming}
	title={m.quiz_start_title()}
	{message}
	confirmLabel={m.quiz_start()}
	onconfirm={begin}
	errorMessage={(error) => quizErrorMessage(toDataError(error))}
/>

<style>
	.action {
		display: grid;
		gap: 10px;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-align: center;
		text-wrap: balance;
	}

	.offline {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 6px;
		color: var(--warning);
	}

	.offline :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		flex-shrink: 0;
	}
</style>
