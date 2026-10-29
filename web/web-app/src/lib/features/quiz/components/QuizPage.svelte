<script lang="ts">
	import { untrack } from 'svelte';
	import type { MoodleCourseModule } from '@mybicocca/data-api/elearning';
	import IconCalendarX from '~icons/ph/calendar-x';
	import IconClock from '~icons/ph/clock';
	import IconExamDuotone from '~icons/ph/exam-duotone';
	import IconFlagCheckered from '~icons/ph/flag-checkered';
	import IconRepeat from '~icons/ph/repeat';
	import IconTimer from '~icons/ph/timer';
	import { now as minute } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { displayName } from '#lib/features/course/content.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { isRenderableHtml } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Meter from '#lib/ui/Meter.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import Stat from '#lib/ui/Stat.svelte';
	import { formatInstant, formatScore, gradingMethodLabel, quizStatusLine } from '../format.ts';
	import { quizzesPath } from '../links.ts';
	import {
		isOpenAttempt,
		maxAttempts,
		overallFeedbackShown,
		ownAttempts,
		ranOut,
		scheduleOf
	} from '../model.ts';
	import {
		accessQuery,
		attemptAccessQuery,
		attemptsQuery,
		bestGradeQuery,
		courseQuizzesQuery
	} from '../queries.ts';
	import { CombinedFeedback } from '../review.svelte.ts';
	import AttemptList from './AttemptList.svelte';
	import GradeFeedback from './GradeFeedback.svelte';
	import QuizAction from './QuizAction.svelte';

	interface Props {
		quizId: number;
	}

	let { quizId }: Props = $props();

	const course = getCourse();
	const quizzes = query(() => session.current && courseQuizzesQuery(session.current, course.id));
	const attempts = query(() => session.current && attemptsQuery(session.current, quizId));
	const best = query(() => session.current && bestGradeQuery(session.current, quizId));
	const access = query(() => session.current && accessQuery(session.current, quizId));
	const attemptAccess = query(() => session.current && attemptAccessQuery(session.current, quizId));

	const now = $derived(minute.current);

	const quiz = $derived(quizzes.data?.quizzes.find((candidate) => candidate.id === quizId));
	const module = $derived.by((): MoodleCourseModule | undefined => {
		if (quiz === undefined) return undefined;
		for (const section of course.contents.data ?? []) {
			const found = section.modules.find((candidate) => candidate.id === quiz.courseModule);
			if (found) return found;
		}
		return undefined;
	});
	const name = $derived(quiz ? displayName(quiz.name, course.language) || quiz.name : undefined);
	const own = $derived(ownAttempts(attempts.data?.attempts ?? []));

	const bestGrade = $derived(
		best.data?.hasGrade && best.data.grade !== null ? best.data.grade : undefined
	);
	const pass = $derived(best.data?.gradeToPass ? best.data.gradeToPass : undefined);
	/* Moodle words the grade so far as the teacher's band for it, when the review settings allow. */
	const combined = new CombinedFeedback(() => quiz);
	const feedbackShown = $derived(
		quiz !== undefined &&
			combined.ready &&
			overallFeedbackShown(quiz, own, now.getTime(), combined.value)
	);

	/*
	 * Moodle closes an attempt whose time ran out when asked for its access: the attempts and the
	 * grade loaded alongside are then out of date. Once per answer, so a refresh can't loop.
	 */
	let checkedAt = 0;
	$effect(() => {
		const answeredAt = attemptAccess.refreshing ? 0 : (attemptAccess.updatedAt ?? 0);
		const open = own.find(isOpenAttempt);
		if (answeredAt <= checkedAt || open === undefined) return;
		if (!ranOut(open, attemptAccess.data, answeredAt)) return;
		checkedAt = answeredAt;
		untrack(() => {
			void attempts.refresh();
			void best.refresh();
			void access.refresh();
		});
	});

	const facts = $derived.by((): Fact[] => {
		if (!quiz) return [];
		const max = maxAttempts(quiz);
		const used = own.length;
		const list: Fact[] = [
			{
				label: m.quiz_attempts_label(),
				icon: IconRepeat,
				value:
					max !== null
						? m.quiz_attempts_used({ used, max })
						: used === 0
							? m.quiz_attempts_unlimited()
							: m.quiz_attempts_used_unlimited({ count: used })
			},
			{
				label: m.quiz_time_label(),
				icon: IconTimer,
				value:
					(quiz.timeLimit ?? 0) > 0
						? m.quiz_minutes_long({ count: Math.floor((quiz.timeLimit ?? 0) / 60) })
						: m.quiz_no_time_limit()
			}
		];
		const schedule = scheduleOf(quiz, now);
		if (schedule?.kind === 'opens') {
			list.push({
				label: m.quiz_opening_label(),
				icon: IconClock,
				value: formatInstant(schedule.at)
			});
		} else if (schedule) {
			const closed = schedule.kind === 'closed';
			list.push({
				label: m.quiz_closing_label(),
				icon: closed ? IconCalendarX : IconClock,
				value: closed
					? m.quiz_closed_on_long({ date: formatInstant(schedule.at) })
					: formatInstant(schedule.at)
			});
		}
		if (pass !== undefined) {
			list.push({
				label: m.quiz_pass_label(),
				icon: IconFlagCheckered,
				value:
					quiz.grade === null
						? formatScore(pass, quiz)
						: m.quiz_score_of({
								score: formatScore(pass, quiz),
								max: formatScore(quiz.grade, quiz)
							})
			});
		}
		return list;
	});

	const subtitle = $derived(quiz ? quizStatusLine({ quiz, module }, now) : undefined);
	const intro = $derived(quiz && isRenderableHtml(quiz.intro) ? quiz.intro : null);
	const gate = new LoadingGate(() => quiz === undefined && quizzes.status !== 'error');
	const back = $derived({ href: quizzesPath(course.id), label: m.quiz_back_quizzes() });
	const updatedAt = $derived(attempts.updatedAt ? new Date(attempts.updatedAt) : undefined);
	const failure = $derived(quizzes.error ?? attempts.error);
	/* The browser may claim a connection that Moodle can't reach: the last failure says so too. */
	const online = $derived(network.online && failure?.kind !== 'offline');
	/* Data kept from before a failed refresh; attempts that never loaded say so in the action. */
	const staleFailure = $derived(
		(quizzes.data ? quizzes.error : undefined) ?? (attempts.data ? attempts.error : undefined)
	);
</script>

<svelte:head>
	<title>{name ?? m.quiz_title()} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<div class="quiz" data-course={course.slot}>
		{#if quiz && name}
			<PageHeader eyebrow={course.title} title={name} {subtitle} {back} sans />

			{#if staleFailure}
				<ErrorState
					variant="stale"
					message={errorMessage(staleFailure)}
					{updatedAt}
					onretry={() => {
						void quizzes.refresh();
						void attempts.refresh();
						void best.refresh();
					}}
					retrying={quizzes.refreshing || attempts.refreshing}
				/>
			{/if}

			<div class="layout">
				<section class="summary" aria-label={m.quiz_overview_label()}>
					<div class="grade">
						{#if bestGrade !== undefined}
							<Stat
								label={m.quiz_best_grade()}
								value={formatScore(bestGrade, quiz)}
								unit={quiz.grade !== null ? `/ ${formatScore(quiz.grade, quiz)}` : undefined}
								caption={gradingMethodLabel(quiz.gradeMethod) ?? undefined}
								size="lg"
							/>
							{#if quiz.grade}
								<Meter
									value={bestGrade}
									max={quiz.grade}
									label={m.quiz_best_grade()}
									valueText={m.quiz_score_of({
										score: formatScore(bestGrade, quiz),
										max: formatScore(quiz.grade, quiz)
									})}
									marks={pass !== undefined ? [pass] : []}
									hideLabel
								/>
							{/if}
							{#if feedbackShown}
								<GradeFeedback quizId={quiz.id} grade={bestGrade} />
							{/if}
						{:else}
							<p class="grade-label">{m.quiz_best_grade()}</p>
							<p class="no-grade">{m.quiz_no_grade_yet()}</p>
						{/if}
					</div>
					<div class={['facts', { even: facts.length % 2 === 0, three: facts.length === 3 }]}>
						<Facts items={facts} />
					</div>
				</section>

				<div class="side">
					<div class="action">
						<Card padding="lg">
							{#if attempts.data}
								<QuizAction
									{quiz}
									attempts={attempts.data.attempts}
									access={access.data}
									attemptAccess={attemptAccess.data}
									courseId={course.id}
									{online}
									{now}
								/>
							{:else if attempts.status === 'error'}
								<!-- Without the attempts, starting could clash with one in progress or one too many. -->
								<div class="action-failed">
									<Button variant="tonal" size="lg" full disabled>{m.quiz_start()}</Button>
									<p class="hint">{m.quiz_attempts_unknown()}</p>
									<Button
										variant="ghost"
										size="sm"
										loading={attempts.refreshing}
										onclick={attempts.refresh}
									>
										{m.common_retry()}
									</Button>
								</div>
							{:else}
								<div class="action-skeleton" aria-hidden="true">
									<Skeleton width="100%" height="var(--control-h-lg)" radius="var(--r-pill)" />
									<Skeleton width="60%" height="0.8em" />
								</div>
							{/if}
						</Card>
					</div>
					{#if own.length > 0}
						<div class="attempts">
							<AttemptList
								{quiz}
								attempts={own}
								courseId={course.id}
								reviewable={access.data?.canReviewMyAttempts !== false}
								{online}
							/>
						</div>
					{/if}
				</div>

				{#if intro}
					<section class="intro">
						<Card title={m.quiz_instructions()} level={2} padding="lg">
							<TeacherHtml html={intro} />
						</Card>
					</section>
				{/if}
			</div>
		{:else if quizzes.status === 'error' && quizzes.error && !quizzes.data}
			<PageHeader title={m.quiz_title()} {back} sans />
			<ErrorState
				title={m.quiz_load_quiz_failed()}
				message={errorMessage(quizzes.error)}
				onretry={quizzes.refresh}
				retrying={quizzes.refreshing}
			/>
		{:else if quizzes.data && !quizzes.refreshing}
			<PageHeader title={m.quiz_title()} {back} sans />
			<EmptyState
				size="page"
				icon={IconExamDuotone}
				title={m.quiz_not_found_title()}
				body={m.quiz_not_found_body()}
			>
				{#snippet action()}
					<Button variant="tonal" href={quizzesPath(course.id)}>{m.quiz_all_quizzes()}</Button>
				{/snippet}
			</EmptyState>
		{:else}
			<PageHeader title="" {back} loading={{ title: 1 }} sans />
			<div class="layout" aria-busy="true" aria-label={m.quiz_loading()}>
				{#if gate.visible}
					<div class="summary">
						<div class="grade skeleton-grade">
							<Skeleton variant="text" width="90px" />
							<span class="skeleton-numeral"><Skeleton variant="text" width="120px" /></span>
							<Skeleton height="8px" radius="var(--r-pill)" />
						</div>
						<div class="facts">
							<Skeleton width="100%" height="var(--facts-h)" radius="var(--r-lg)" />
						</div>
					</div>
					<div class="side">
						<div class="action">
							<Skeleton width="100%" height="122px" radius="var(--r-lg)" />
						</div>
						<div class="attempts">
							<Skeleton width="100%" height="200px" radius="var(--r-lg)" />
						</div>
					</div>
				{/if}
			</div>
		{/if}
	</div>
</Page>

<style>
	.quiz {
		display: grid;
		gap: var(--page-gap);
		min-inline-size: 0;
	}

	/*
	 * On phones the action follows the grade, so it shows without scrolling; from 880 px the
	 * summary and the action with the attempts sit side by side.
	 */
	.layout {
		display: grid;
		grid-template-areas: 'grade' 'action' 'facts' 'attempts' 'intro';
		gap: var(--sp-5);
		align-items: start;
	}

	.summary,
	.side {
		display: contents;
	}

	.grade {
		grid-area: grade;
		display: grid;
		gap: 16px;
		padding: 22px 24px 24px;
		border-radius: var(--r-xl);
		background: var(--course-container);
	}

	.facts {
		--facts-h: 244px;
		grid-area: facts;
		min-inline-size: 0;
	}

	/* Two or four facts pair up and three share a row, rather than leave cells empty. */
	@container (width >= 660px) {
		.three :global(.facts) {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}
	}

	@container (width >= 760px) {
		.even :global(.facts) {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}
	}

	@container page (width >= 480px) {
		.facts {
			--facts-h: 122px;
		}
	}

	.action {
		grid-area: action;
	}

	.attempts {
		grid-area: attempts;
		min-inline-size: 0;
	}

	@container page (width >= 880px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) minmax(300px, 380px);
			grid-template-areas: 'summary side' 'intro side';
			column-gap: var(--sp-7);
		}

		.summary,
		.side {
			display: grid;
			gap: var(--sp-5);
			min-inline-size: 0;
		}

		.summary {
			grid-area: summary;
		}

		.grade,
		.facts,
		.action,
		.attempts {
			grid-area: auto;
		}

		.side {
			grid-area: side;
			position: sticky;
			inset-block-start: calc(var(--topbar-h) + 24px);
		}
	}

	.grade-label {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.no-grade {
		margin-block-start: -10px;
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.grade :global(.track) {
		background: color-mix(in oklch, var(--course-bar) 18%, var(--surface));
	}

	.grade :global(.fill) {
		background: var(--course-bar);
	}

	.grade :global(.mark) {
		background: var(--text);
	}

	.intro {
		grid-area: intro;
		min-inline-size: 0;
	}

	.action-failed {
		display: grid;
		justify-items: center;
		gap: 10px;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-align: center;
		text-wrap: balance;
	}

	.action-skeleton {
		display: grid;
		justify-items: center;
		gap: 12px;
	}

	.skeleton-grade {
		gap: 10px;
	}

	.skeleton-numeral {
		font: var(--type-numeral-l);
	}
</style>
