<script lang="ts">
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconCalendarCheck from '~icons/ph/calendar-check';
	import IconClock from '~icons/ph/clock';
	import IconExamDuotone from '~icons/ph/exam-duotone';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import { now } from '#lib/data/clock.svelte.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { displayName } from '#lib/features/course/content.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { formatClockLong } from '#lib/features/course/format.ts';
	import { formatPercent } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import {
		errorCode,
		formatInstant,
		formatScore,
		quizErrorMessage,
		verdictCountLabels
	} from '../format.ts';
	import { moodleReviewUrl, quizPath } from '../links.ts';
	import { overallFeedbackShown, scaledGrade, verdictCounts, type Verdict } from '../model.ts';
	import { attemptsQuery, courseQuizzesQuery, reviewQuery } from '../queries.ts';
	import { parseMark, parseQuestion } from '../questions.ts';
	import { CombinedFeedback } from '../review.svelte.ts';
	import GradeFeedback from './GradeFeedback.svelte';
	import QuestionCard from './QuestionCard.svelte';
	import ReviewIndex from './ReviewIndex.svelte';

	interface Props {
		quizId: number;
		attemptId: number;
	}

	let { quizId, attemptId }: Props = $props();

	const course = getCourse();
	const quizzes = query(() => session.current && courseQuizzesQuery(session.current, course.id));
	const review = query(() => session.current && reviewQuery(session.current, attemptId));
	/* The quiz page's list, cached by then: it names the attempt before its review arrives. */
	const attempts = query(() => session.current && attemptsQuery(session.current, quizId));

	const quiz = $derived(quizzes.data?.quizzes.find((candidate) => candidate.id === quizId));
	const name = $derived(quiz ? displayName(quiz.name, course.language) || quiz.name : undefined);
	const questions = $derived(review.data?.questions.map(parseQuestion) ?? []);
	const record = $derived(review.data?.attempt);
	const number = $derived(
		record?.attempt ??
			attempts.data?.attempts.find((candidate) => candidate.id === attemptId)?.attempt ??
			null
	);

	/* Moodle's grade is on the quiz's scale already; the raw marks are rescaled when it's absent. */
	const grade = $derived.by(() => {
		const stated = parseMark(review.data?.grade ?? null);
		if (stated !== null) return stated;
		return record && quiz ? scaledGrade(record.sumGrades, quiz) : null;
	});
	const combined = new CombinedFeedback(() => quiz);
	const feedbackShown = $derived(
		quiz !== undefined &&
			record?.state === 'finished' &&
			combined.ready &&
			overallFeedbackShown(quiz, [record], now.current.getTime(), combined.value)
	);
	const counts = $derived(verdictCounts(questions.map((question) => question.state)));
	const ORDER: readonly Verdict[] = ['correct', 'partial', 'wrong', 'unanswered', 'pending'];
	const tally = $derived(ORDER.filter((verdict) => counts[verdict] > 0));
	const marksSoFar = $derived(questions.reduce((sum, question) => sum + (question.mark ?? 0), 0));
	const marksMax = $derived(questions.reduce((sum, question) => sum + (question.maxMark ?? 0), 0));

	const facts = $derived.by((): Fact[] => {
		if (!record) return [];
		const list: Fact[] = [];
		if (record.timeStart) {
			list.push({
				label: m.quiz_started(),
				icon: IconClock,
				value: formatInstant(new Date(record.timeStart * 1000))
			});
		}
		if (record.timeFinish) {
			list.push({
				label: m.quiz_submitted(),
				icon: IconCalendarCheck,
				value: formatInstant(new Date(record.timeFinish * 1000))
			});
		}
		if (record.timeStart && record.timeFinish && record.timeFinish >= record.timeStart) {
			list.push({
				label: m.quiz_duration(),
				icon: IconHourglass,
				value: formatClockLong(record.timeFinish - record.timeStart)
			});
		}
		return list;
	});

	const unavailable = $derived(
		review.error !== undefined && /^noreview/.test(errorCode(review.error) ?? '')
	);
	/* The hero waits for the quiz too: its scale adds the maximum and the percentage under the grade. */
	const quizSettled = $derived(quizzes.data !== undefined || quizzes.status === 'error');
	const gate = new LoadingGate(
		() => (review.data === undefined && review.status === 'pending') || !quizSettled
	);
	const updatedAt = $derived(review.updatedAt ? new Date(review.updatedAt) : undefined);
	const back = $derived({ href: quizPath(course.id, quizId), label: name ?? m.quiz_title() });
</script>

<svelte:head>
	<title>{m.quiz_review()} · {name ?? m.quiz_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<div class="review" data-course={course.slot}>
		<PageHeader
			eyebrow={course.title ?? name ?? m.quiz_title()}
			title={number ? m.quiz_review_of({ number }) : m.quiz_review()}
			{back}
			sans
		/>

		{#if review.error && review.data}
			<ErrorState
				variant="stale"
				message={quizErrorMessage(review.error)}
				{updatedAt}
				onretry={review.refresh}
				retrying={review.refreshing}
			/>
		{/if}

		{#if review.data && record && quizSettled}
			<section class="hero" aria-label={m.quiz_grade()}>
				<div class="score">
					<p class="overline">{m.quiz_grade()}</p>
					{#if grade !== null}
						<p class="figure">
							<span class="value">{formatScore(grade, quiz)}</span>
							{#if quiz?.grade}
								<span class="unit">/ {formatScore(quiz.grade, quiz)}</span>
							{/if}
						</p>
						{#if quiz?.grade}
							<p class="percent">{formatPercent(grade / quiz.grade)}</p>
						{/if}
					{:else}
						<p class="pending-title">{m.quiz_not_graded_yet()}</p>
						{#if marksMax > 0}
							<p class="percent">
								{m.quiz_marks_so_far({
									marks: formatScore(marksSoFar),
									max: formatScore(marksMax)
								})}
							</p>
						{/if}
					{/if}
				</div>
				{#if tally.length > 0}
					<ul class="tally">
						{#each tally as verdict (verdict)}
							<li class={['count', verdict]}>
								<span class="dot" aria-hidden="true"></span>
								{verdictCountLabels[verdict]({ count: counts[verdict] })}
							</li>
						{/each}
					</ul>
				{/if}
				{#if feedbackShown && quiz && grade !== null}
					<div class="overall"><GradeFeedback quizId={quiz.id} {grade} /></div>
				{/if}
				<div class={['facts', { three: facts.length === 3 }]}><Facts items={facts} /></div>
			</section>

			<div class="layout">
				<div class="questions">
					{#each questions as question (question.slot)}
						<QuestionCard
							{question}
							mode="review"
							moodleUrl={quiz && question.body.kind === 'unsupported'
								? moodleReviewUrl(attemptId, quiz.courseModule, question.prefix)
								: undefined}
						/>
					{/each}
					<div class="end">
						<Button
							variant="tonal"
							size="lg"
							icon={IconArrowLeft}
							href={quizPath(course.id, quizId)}
						>
							{m.quiz_back_to_quiz()}
						</Button>
					</div>
				</div>
				<aside class="side" aria-label={m.quiz_navigator()}>
					<div class="side-card">
						<h2 class="side-title">{m.quiz_navigator()}</h2>
						<ReviewIndex {questions} />
					</div>
				</aside>
			</div>
		{:else if unavailable}
			<EmptyState
				size="page"
				icon={IconExamDuotone}
				title={m.quiz_review_unavailable_title()}
				body={m.quiz_error_no_review()}
			>
				{#snippet action()}
					<Button variant="tonal" href={quizPath(course.id, quizId)}>{m.quiz_back_to_quiz()}</Button
					>
				{/snippet}
			</EmptyState>
		{:else if review.error}
			<ErrorState
				title={m.quiz_review_failed()}
				message={quizErrorMessage(review.error)}
				onretry={review.refresh}
				retrying={review.refreshing}
			/>
		{:else}
			<div class="loading" aria-busy="true" aria-label={m.quiz_loading()}>
				{#if gate.visible}
					<div class="hero skeleton-hero">
						<Skeleton width="80px" height="0.8em" />
						<Skeleton width="160px" height="4rem" />
						<Skeleton width="60%" height="1em" />
					</div>
					{#each { length: 2 }, index (index)}
						<div class="question-skeleton">
							<Skeleton width="22%" height="0.8em" />
							<Skeleton width="85%" height="1.1em" />
							<Skeleton width="100%" height="52px" radius="var(--r-md)" />
						</div>
					{/each}
				{/if}
			</div>
		{/if}
	</div>
</Page>

<style>
	.review {
		display: grid;
		gap: var(--page-gap);
		min-inline-size: 0;
	}

	.hero {
		display: grid;
		gap: 18px;
		padding: 24px;
		border-radius: var(--r-xl);
		background: var(--course-container);
		color: var(--text);
	}

	@container page (width >= 720px) {
		.hero {
			grid-template-columns: auto minmax(0, 1fr);
			align-items: center;
			column-gap: 40px;
			padding: 28px 32px;
		}

		.overall,
		.facts {
			grid-column: 1 / -1;
		}
	}

	.score {
		display: grid;
		gap: 4px;
	}

	.overline {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.figure {
		display: flex;
		align-items: baseline;
		gap: 8px;
	}

	.value {
		font: var(--type-numeral-xl);
		font-size: clamp(3.5rem, 2.8rem + 3vw, 5rem);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.unit {
		color: var(--text-muted);
		font: var(--type-title-l);
	}

	.percent {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.pending-title {
		font: var(--type-title-l);
		font-style: italic;
		letter-spacing: var(--track-title-l);
	}

	.tally {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.count {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		padding: 6px 12px;
		border-radius: var(--r-pill);
		background: var(--surface);
		font: var(--type-ui);
	}

	.dot {
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: var(--text-subtle);
	}

	.correct .dot {
		background: var(--success);
	}

	.partial .dot {
		background: var(--warning);
	}

	.wrong .dot {
		background: var(--danger);
	}

	.pending .dot {
		background: var(--info);
	}

	.facts :global(.facts) {
		background: transparent;
	}

	/* Started, submitted and time taken share a row once they fit. */
	@container (width >= 660px) {
		.three :global(.facts) {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}
	}

	.layout {
		display: grid;
		gap: var(--sp-6);
	}

	/* The index stays in view beside the questions: the aside itself sticks, as tall as its card. */
	@container page (width >= 880px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) 240px;
			gap: var(--sp-6);
		}

		.layout .side {
			position: sticky;
			inset-block-start: calc(var(--topbar-h) + 24px);
			display: block;
			align-self: start;
		}
	}

	@container page (width >= 1100px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) 280px;
			gap: var(--sp-7);
		}
	}

	.questions {
		display: grid;
		gap: var(--sp-4);
		min-inline-size: 0;
		container-type: inline-size;
	}

	.end {
		display: flex;
		justify-content: center;
		padding-block-start: var(--sp-4);
	}

	.side {
		display: none;
	}

	.side-card {
		display: grid;
		gap: 16px;
		padding: 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.side-title {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.loading {
		display: grid;
		gap: var(--sp-4);
	}

	.skeleton-hero {
		grid-template-columns: 1fr;
	}

	.question-skeleton {
		display: grid;
		gap: 12px;
		padding: 24px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}
</style>
