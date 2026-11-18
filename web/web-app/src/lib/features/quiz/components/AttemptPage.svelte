<script lang="ts">
	import { tick, untrack } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconLockKey from '~icons/ph/lock-key-duotone';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { network } from '#lib/data/network.svelte.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { displayName } from '#lib/features/course/content.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { formatClockLong } from '#lib/features/course/format.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Immersive from '#lib/ui/Immersive.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Overlay from '#lib/ui/Overlay.svelte';
	import ProgressBar from '#lib/ui/ProgressBar.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { AttemptSession, type NavItem } from '../attempt.svelte.ts';
	import { AttemptClock } from '../clock.svelte.ts';
	import { AttemptEnding } from '../ending.svelte.ts';
	import { errorCode, quizErrorMessage } from '../format.ts';
	import { moodleAttemptUrl, quizPath } from '../links.ts';
	import {
		asksCheck,
		attemptDeadline,
		isOpenAttempt,
		pageCount,
		type TimerPhase
	} from '../model.ts';
	import {
		attemptDepth,
		leaveAttempt,
		LeaveGuard,
		pushAttemptPage,
		replaceAttemptPage
	} from '../navigation.svelte.ts';
	import { attemptAccessQuery, attemptsQuery, courseQuizzesQuery } from '../queries.ts';
	import { questionKey } from '../questions.ts';
	import { SubmitCheck } from '../submit.svelte.ts';
	import AttemptBar from './AttemptBar.svelte';
	import AttemptDock from './AttemptDock.svelte';
	import Navigator from './Navigator.svelte';
	import NavigatorCard from './NavigatorCard.svelte';
	import OverdueNotice from './OverdueNotice.svelte';
	import Pager from './Pager.svelte';
	import QuestionCard from './QuestionCard.svelte';
	import SubmitDialog from './SubmitDialog.svelte';
	import TimeUpDialog from './TimeUpDialog.svelte';

	interface Props {
		quizId: number;
		attemptId: number;
	}

	let { quizId, attemptId }: Props = $props();

	/** Time warnings stay long enough to be read mid-question. */
	const WARNING_MS = 10_000;

	const course = getCourse();
	const quizzes = query(() => session.current && courseQuizzesQuery(session.current, course.id));
	const attempts = query(() => session.current && attemptsQuery(session.current, quizId));
	const quiz = $derived(quizzes.data?.quizzes.find((candidate) => candidate.id === quizId));
	const listed = $derived(attempts.data?.attempts.find((candidate) => candidate.id === attemptId));
	const name = $derived(quiz ? displayName(quiz.name, course.language) || quiz.name : undefined);

	const ids = untrack(() => ({ courseId: course.id, quizId, attemptId }));
	const attempt = new AttemptSession({
		...ids,
		scope: () => session.current,
		autoSavePeriod: () => quiz?.autoSavePeriod ?? null
	});
	$effect(() => () => attempt.dispose());

	const requested = $derived.by(() => {
		const raw = page.url.searchParams.get('page');
		const value = raw === null ? Number.NaN : Number(raw);
		return Number.isInteger(value) && value >= 0 ? value : undefined;
	});
	const depth = $derived(attemptDepth(page.state));
	const sequential = $derived(quiz?.navMethod === 'sequential');
	const record = $derived(attempt.attempt ?? listed);
	const total = $derived(pageCount(record?.layout ?? null));
	const current = $derived(attempt.page ?? requested ?? 0);
	const last = $derived(attempt.data?.nextPage === -1);
	const busy = $derived(attempt.loading);

	let navigatorOpen = $state(false);

	const ending = new AttemptEnding({
		submit: (timeUp) => attempt.submit(timeUp),
		interrupt: () => {
			guard.open = false;
			submitCheck.close();
			navigatorOpen = false;
		},
		leave: (to) => {
			guard.release();
			void leaveAttempt(ids, to, depth);
		}
	});
	const submitCheck = new SubmitCheck({
		prepare: () => attempt.check(),
		submit: () => ending.submit()
	});
	/* The page being left asks no more: a submission changes what Moodle answers. */
	const access = query(
		() =>
			session.current && !ending.finished && attemptAccessQuery(session.current, quizId, attemptId)
	);

	const loadCode = $derived(errorCode(attempt.loadError));
	const saveCode = $derived(errorCode(attempt.saveError));
	const closed = $derived(
		loadCode === 'attemptalreadyclosed' || saveCode === 'attemptalreadyclosed'
	);
	/* Moodle answers an attempt that isn't the student's, or no longer exists, with a missing record. */
	const missing = $derived(
		attempt.questions.length === 0 &&
			(attempt.loadError?.kind === 'not_found' ||
				loadCode === 'invalidrecord' ||
				loadCode === 'notyourattempt')
	);
	const overdue = $derived(
		!closed &&
			(ending.overdue ||
				listed?.state === 'overdue' ||
				loadCode === 'stateoverdue' ||
				saveCode === 'stateoverdue')
	);
	/* Moodle asks a check the app can't give, such as a password: only its site goes on. */
	const onSite = $derived(
		!closed && !overdue && !ending.finished && asksCheck(record, access.data)
	);
	/* Nothing left to answer here: the page says why and leads on. */
	const over = $derived(closed || missing || overdue || onSite);

	const guard = new LeaveGuard({
		active: () => !ending.finished && !over,
		dirty: () => attempt.dirty,
		flush: () => attempt.flush(),
		inside: (url) => url.pathname === page.url.pathname
	});

	/* The timer: Moodle's end time, a warning at five minutes and one, a submit when time's up. */
	const deadline = $derived(record ? attemptDeadline(record, access.data) : null);
	const clock = new AttemptClock({
		deadline: () => deadline,
		timeLimit: () => quiz?.timeLimit ?? null,
		running: () => quiz !== undefined && !ending.finished && !over,
		onphase
	});

	function onphase(phase: TimerPhase) {
		if (phase === 'warning') {
			toast.show({ message: m.quiz_time_warning(), tone: 'warning', duration: WARNING_MS });
		} else if (phase === 'danger') {
			toast.show({ message: m.quiz_time_last_minute(), tone: 'warning', duration: WARNING_MS });
		} else if (phase === 'expired') {
			void ending.expire();
		}
	}

	const countdown = $derived(
		clock.visible && clock.left !== null && clock.phase !== null
			? { left: clock.left, phase: clock.phase }
			: null
	);

	/*
	 * No page in the address: resume where Moodle says the student stopped. An attempt already
	 * over leads to its review instead, which this would supersede.
	 */
	$effect(() => {
		if (requested !== undefined || (listed !== undefined && !isOpenAttempt(listed))) return;
		if (listed === undefined && attempts.status !== 'error' && attempts.data === undefined) return;
		const resume = listed?.currentPage ?? 0;
		untrack(() => void replaceAttemptPage(ids, resume, depth));
	});

	/* An attempt already over has nothing to answer: its review is what the student wants. */
	$effect(() => {
		if (listed?.state === 'finished' || listed?.state === 'abandoned') {
			untrack(() => ending.finish('review'));
		}
	});

	let pendingSlot: number | undefined;
	let navigated = false;
	let heading = $state<HTMLElement>();

	$effect(() => {
		const target = requested;
		if (target === undefined) return;
		untrack(async () => {
			if (ending.finished || overdue || onSite) return;
			await attempt.load(target);
			if (attempt.loadError) return handleLoadError();
			void attempt.summarize().catch(() => undefined);
			await tick();
			const slot = pendingSlot;
			pendingSlot = undefined;
			if (slot !== undefined) focusQuestion(slot);
			else if (navigated) heading?.focus({ preventScroll: true });
			navigated = false;
		});
	});

	/** Moodle refuses pages a sequential quiz has left behind, or that don't exist. */
	async function handleLoadError() {
		const code = errorCode(attempt.loadError);
		if (code === 'invalidpagenumber' || (code && /out of sequence/i.test(code))) {
			await attempts.refresh();
			const resume = listed?.currentPage ?? 0;
			if (resume !== requested) {
				void replaceAttemptPage(ids, resume, depth);
			}
		}
	}

	function goToPage(target: number, slot?: number) {
		pendingSlot = slot;
		navigated = true;
		void attempt.flush().catch(() => undefined);
		void pushAttemptPage(ids, target, depth);
	}

	function focusQuestion(slot: number) {
		const element = document.getElementById(`question-${slot}`);
		element?.scrollIntoView({ block: 'start', behavior: 'instant' });
		element?.focus({ preventScroll: true });
	}

	/* A question of this page is brought into view once the overlay that led there has closed. */
	async function visit(item: NavItem) {
		navigatorOpen = false;
		if (item.page === current) {
			await tick();
			focusQuestion(item.slot);
		} else if (!sequential) {
			goToPage(item.page, item.slot);
		}
	}

	/* The time left reads as it runs down, however long the confirmation stays open. */
	const leaveMessage = $derived.by(() => {
		const unsaved = attempt.saveState === 'offline' || attempt.saveState === 'error';
		if (unsaved) return m.quiz_leave_unsaved();
		if (clock.left !== null && (quiz?.timeLimit ?? 0) > 0) {
			return m.quiz_leave_timed({ left: formatClockLong(Math.ceil(clock.left / 1000)) });
		}
		return m.quiz_leave_body();
	});

	/* A save Moodle refused as out of sequence takes its current checks, then saves again. */
	let retrying = $state(false);
	async function retrySave() {
		retrying = true;
		try {
			if (saveCode === 'submissionoutofsequence') await attempt.resync();
			else await attempt.flush();
		} catch {
			// The failure shows where the save status does.
		} finally {
			retrying = false;
		}
	}

	const gate = new LoadingGate(() => attempt.loading && attempt.questions.length === 0);
	const pageLabel = $derived(
		total === null
			? m.quiz_page({ page: current + 1 })
			: m.quiz_page_of({ page: current + 1, total })
	);
	const position = $derived(total === null ? `${current + 1}` : `${current + 1} / ${total}`);
	const moodleUrl = $derived(
		quiz ? moodleAttemptUrl(attemptId, quiz.courseModule, current) : undefined
	);
	const navItems = $derived(attempt.navItems);
	const canFinish = $derived(network.online && !busy && !over);
	const showQuestions = $derived(!over && attempt.questions.length > 0);
</script>

<svelte:head>
	<title>{name ?? m.quiz_title()} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

<svelte:window
	ononline={() => {
		if (attempt.dirty) void attempt.flush().catch(() => undefined);
	}}
/>
<svelte:document
	onvisibilitychange={() => {
		if (document.visibilityState === 'hidden' && attempt.dirty) {
			void attempt.flush().catch(() => undefined);
		}
	}}
/>

<Immersive />

<div class="attempt" data-course={course.slot}>
	<AttemptBar
		course={course.title}
		name={name ?? m.quiz_title()}
		saveState={attempt.saveState}
		onretry={() => void retrySave()}
		{countdown}
		finish={showQuestions && !last}
		finishDisabled={!canFinish}
		onfinish={() => submitCheck.show()}
		onexit={() => goto(quizPath(course.id, quizId))}
	/>

	<div class="frame">
		<section class="main" aria-labelledby="attempt-heading">
			<div class={['page-head', { over }]}>
				<h1 class="page-label" id="attempt-heading" tabindex="-1" bind:this={heading}>
					<span class="attempt-number">
						{record?.attempt
							? m.quiz_attempt_number({ number: record.attempt })
							: m.quiz_attempt_unknown()}
					</span>
					<span>{pageLabel}</span>
				</h1>
				{#if total !== null && total > 1}
					<ProgressBar value={current + 1} max={total} label={pageLabel} hideLabel size="sm" />
				{/if}
			</div>

			{#if closed}
				<EmptyState size="page" title={m.quiz_attempt_closed()}>
					{#snippet action()}
						<Button variant="solid" onclick={() => ending.finish('review')}>
							{m.quiz_see_review()}
						</Button>
					{/snippet}
				</EmptyState>
			{:else if missing}
				<EmptyState size="page" title={m.quiz_attempt_missing()}>
					{#snippet action()}
						<Button variant="tonal" onclick={() => ending.finish('quiz')}>
							{m.quiz_back_to_quiz()}
						</Button>
					{/snippet}
				</EmptyState>
			{:else if onSite}
				<EmptyState
					size="page"
					icon={IconLockKey}
					title={m.quiz_on_site_title()}
					body={m.quiz_error_access()}
				>
					{#snippet action()}
						<Button
							variant="solid"
							trailingIcon={IconArrowSquareOut}
							href={moodleAttemptUrl(attemptId, quiz?.courseModule ?? 0, current)}
							disabled={quiz === undefined}
						>
							{m.quiz_open_moodle()}
						</Button>
					{/snippet}
				</EmptyState>
			{:else if overdue}
				<OverdueNotice
					deadline={record?.timeCheckState ? record.timeCheckState * 1000 : null}
					number={record?.attempt ?? null}
					online={network.online}
					onsubmit={() => ending.submitOverdue()}
				/>
			{:else if attempt.loadError && attempt.questions.length === 0}
				<div class="failed">
					<ErrorState
						title={m.quiz_load_attempt_failed()}
						message={quizErrorMessage(attempt.loadError)}
						onretry={() => void attempt.load(current)}
						retrying={attempt.loading}
					/>
					<Button variant="ghost" onclick={() => ending.finish('quiz')}>
						{m.quiz_back_to_quiz()}
					</Button>
				</div>
			{:else if attempt.questions.length === 0}
				<div class="questions" aria-busy="true" aria-label={m.quiz_loading_attempt()}>
					{#if gate.visible}
						{#each { length: 2 }, index (index)}
							<div class="question-skeleton">
								<Skeleton width="22%" height="0.8em" />
								<Skeleton width="85%" height="1.1em" />
								<Skeleton width="60%" height="1.1em" />
								<Skeleton width="100%" height="52px" radius="var(--r-md)" />
								<Skeleton width="100%" height="52px" radius="var(--r-md)" />
							</div>
						{/each}
					{/if}
				</div>
			{:else}
				{#if attempt.loadError}
					<ErrorState
						variant="stale"
						message={quizErrorMessage(attempt.loadError)}
						onretry={() => void attempt.load(current)}
						retrying={attempt.loading}
					/>
				{/if}
				{#if saveCode === 'submissionoutofsequence'}
					<ErrorState
						variant="stale"
						message={m.quiz_error_out_of_sequence()}
						onretry={() => void retrySave()}
						{retrying}
					/>
				{/if}
				<div class={['questions', { dimmed: busy }]} aria-busy={busy}>
					{#each attempt.questions as question (questionKey(question))}
						<QuestionCard
							{question}
							mode="attempt"
							response={attempt.responses.get(question.slot) ?? {}}
							flagged={attempt.flags.get(question.slot) ?? question.flagged}
							disabled={busy || ending.timeUp}
							{moodleUrl}
							onanswer={(field, value) => attempt.answer(question.slot, field, value)}
							onflag={() => attempt.toggleFlag(question.slot)}
						/>
					{/each}
				</div>
				<nav class="pager" aria-label={m.quiz_pages_label()}>
					<Pager
						size="lg"
						back={!sequential && current > 0}
						{last}
						ready={attempt.data !== undefined}
						{busy}
						online={network.online}
						onprevious={() => goToPage(current - 1)}
						onnext={() => goToPage(attempt.data?.nextPage ?? current + 1)}
						onfinish={() => submitCheck.show()}
					/>
				</nav>
			{/if}
		</section>

		<aside class={['side', { over }]} aria-label={m.quiz_navigator()}>
			<NavigatorCard
				items={navItems}
				page={current}
				{sequential}
				onselect={visit}
				finishDisabled={!canFinish}
				onfinish={() => submitCheck.show()}
			/>
		</aside>
	</div>

	{#if showQuestions}
		<AttemptDock
			{position}
			onnavigator={() => (navigatorOpen = true)}
			saveState={attempt.saveState}
			{retrying}
			onretry={() => void retrySave()}
		>
			<Pager
				size="md"
				back={!sequential && current > 0}
				{last}
				ready={attempt.data !== undefined}
				{busy}
				online={network.online}
				onprevious={() => goToPage(current - 1)}
				onnext={() => goToPage(attempt.data?.nextPage ?? current + 1)}
				onfinish={() => submitCheck.show()}
			/>
		</AttemptDock>
	{/if}
</div>

<Overlay bind:open={navigatorOpen} title={m.quiz_navigator()} subtitle={pageLabel} size="sm">
	<div class="sheet" data-course={course.slot}>
		<Navigator items={navItems} page={current} {sequential} onselect={visit} />
	</div>
	{#snippet footer()}
		<Button
			variant="solid"
			full
			icon={IconPaperPlane}
			disabled={!canFinish}
			onclick={() => {
				navigatorOpen = false;
				submitCheck.show();
			}}
		>
			{m.quiz_finish()}
		</Button>
	{/snippet}
</Overlay>

<SubmitDialog
	check={submitCheck}
	onvisit={visit}
	online={network.online}
	saveState={attempt.saveState}
/>

<ConfirmDialog
	bind:open={guard.open}
	title={m.quiz_leave_title()}
	message={leaveMessage}
	confirmLabel={m.quiz_leave()}
	cancelLabel={m.quiz_stay()}
	onconfirm={() => guard.leave()}
/>

<TimeUpDialog
	open={ending.timeUp}
	error={ending.timeUpError}
	onretry={() => void ending.expire()}
	onback={() => ending.finish('quiz')}
/>

<style>
	.attempt {
		--bar-h: 60px;
		display: grid;
		grid-template-rows: auto 1fr auto;
		min-block-size: 100dvh;
		background: var(--bg);
	}

	.frame {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		gap: var(--sp-7);
		inline-size: 100%;
		max-inline-size: calc(1120px + 2 * var(--gutter));
		margin-inline: auto;
		padding: var(--sp-5) var(--gutter) var(--sp-8);
	}

	.main {
		display: grid;
		align-content: start;
		gap: var(--sp-5);
		min-inline-size: 0;
		container-type: inline-size;
	}

	/* State takes the course's colour, as the navigator's answered tiles do; the app's is for actions. */
	.page-head {
		--accent: var(--course-bar);
		display: grid;
		gap: 10px;
	}

	.page-label {
		display: flex;
		flex-wrap: wrap;
		align-items: baseline;
		justify-content: space-between;
		gap: 4px 12px;
		color: var(--text-muted);
		font: var(--type-ui);
		outline: none;
	}

	.attempt-number {
		color: var(--text);
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.questions {
		display: grid;
		gap: var(--sp-4);
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.dimmed {
		opacity: 0.55;
	}

	.question-skeleton {
		display: grid;
		gap: 12px;
		padding: 24px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.pager {
		display: none;
		align-items: center;
		gap: 12px;
		padding-block-start: var(--sp-2);
	}

	.side {
		display: none;
	}

	.sheet {
		padding-block: 4px 8px;
	}

	.page-head.over,
	.side.over {
		display: none;
	}

	.failed {
		display: grid;
		justify-items: center;
		gap: var(--sp-2);
	}

	@media (width >= 1024px) {
		.frame {
			grid-template-columns: minmax(0, 1fr) 300px;
			padding-block-start: var(--sp-7);
		}

		.side {
			display: block;
		}

		.pager {
			display: flex;
		}
	}
</style>
