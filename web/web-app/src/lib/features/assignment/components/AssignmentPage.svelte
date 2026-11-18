<script lang="ts">
	import { onMount, tick } from 'svelte';
	import { online } from 'svelte/reactivity/window';
	import { goto } from '$app/navigation';
	import IconClipboardText from '~icons/ph/clipboard-text-duotone';
	import IconCaretRight from '~icons/ph/caret-right-bold';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import FileDialogs from '#lib/features/course/components/FileDialogs.svelte';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { FileOpener, setFileOpener } from '#lib/features/course/file/opener.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { AssignmentState } from '../assignment.svelte.ts';
	import { dueLine, failureText, gradeText, lateNote } from '../labels.ts';
	import { assignmentsPath, editorPath } from '../links.ts';
	import {
		activityInstructions,
		briefFiles,
		isBlankHtml,
		phaseOf,
		tickAfter,
		timedEnd,
		timeLimitOf
	} from '../model.ts';
	import { afterSubmissionChange } from '../queries.ts';
	import { recapOf } from '../recap.ts';
	import { removeSubmission, submitForGrading } from '../writes.ts';
	import FileList from './FileList.svelte';
	import StartDialog from './StartDialog.svelte';
	import StatusPanel from './StatusPanel.svelte';
	import SubmitDialog from './SubmitDialog.svelte';

	interface Props {
		courseId: number;
		assignId: number;
	}

	let { courseId, assignId }: Props = $props();

	/** Holds the title's and the subtitle's lines while the name loads. */
	const BLANK = '\u00a0';

	const course = getCourse();
	setFileOpener(new FileOpener(() => session.current));
	const model = new AssignmentState(
		() => courseId,
		() => assignId
	);
	/* It ticks by the nearer of the due date and a running timed attempt's end. */
	const clock = new Clock(() => {
		const { schedule, submission, config } = model;
		const timer = schedule && submission ? timedEnd(schedule, submission, config) : null;
		const next = [schedule?.due, timer].filter((at) => at != null);
		return next.length === 0 ? 5 * 60_000 : tickAfter(Math.min(...next) - Date.now());
	});

	/* The status may have changed on Moodle's own pages: it is reloaded on every opening. */
	onMount(() => void model.status.refresh());

	const assignment = $derived(model.assignment);
	const submission = $derived(model.submission);
	const title = $derived(assignment?.name ?? m.assignment_title_fallback());
	/* The eyebrow names the course: the way back names the tab it leads to. */
	const back = $derived({ href: assignmentsPath(courseId), label: m.course_tab_assignments() });
	const files = $derived(assignment ? briefFiles(assignment, model.status.data) : []);
	const instructions = $derived(
		assignment ? activityInstructions(assignment, model.status.data) : { html: null, files: [] }
	);
	const hasInstructions = $derived(instructions.html !== null || instructions.files.length > 0);
	const intro = $derived(isBlankHtml(assignment?.intro) ? null : (assignment?.intro ?? null));
	const handedFiles = $derived(submission?.files ?? []);
	const showSubmission = $derived(
		submission !== undefined &&
			submission.stage !== 'new' &&
			(submission.text !== null || handedFiles.length > 0)
	);
	const feedback = $derived(submission?.grade);
	const showFeedback = $derived(
		feedback != null && (feedback.feedback !== null || feedback.files.length > 0)
	);
	const previous = $derived(submission?.previous ?? null);
	/* The sections wait for the status, which decides their order: none moves once shown. */
	const statusSettled = $derived(submission !== undefined || model.statusError !== undefined);
	const late = $derived(
		model.schedule && submission
			? lateNote(model.schedule, phaseOf(model.schedule, clock.now, submission.canEdit))
			: null
	);

	const gate = new LoadingGate(
		() => model.list.data === undefined && model.list.status === 'pending'
	);
	/* A reload that failed over what is already on screen: the oldest of the two answers shown. */
	const stale = $derived.by(() => {
		const failed = [
			{ query: model.list, error: model.list.error },
			{ query: model.status, error: model.statusError }
		].filter((item) => item.query.data && item.error);
		const error = failed[0]?.error;
		if (error === undefined) return null;
		const times = failed.flatMap((item) => (item.query.updatedAt ? [item.query.updatedAt] : []));
		return { error, updatedAt: times.length > 0 ? new Date(Math.min(...times)) : undefined };
	});

	let submitting = $state(false);
	let accepted = $state(false);
	let submitProgress = $state<string | null>(null);
	let submitError = $state<string | null>(null);
	let removing = $state(false);
	let starting = $state(false);
	const limit = $derived(timeLimitOf(model.config, submission));

	/*
	 * Once the new status replaces the button that had focus, focus would fall to the page: it goes
	 * to the status heading instead, unless the student has moved on meanwhile.
	 */
	async function refocusStatus() {
		await model.status.refresh();
		await tick();
		const active = document.activeElement;
		if (active === null || active === document.body) {
			document.getElementById('assignment-status')?.focus();
		}
	}

	async function submit(acceptStatement: boolean) {
		const scope = session.current;
		if (scope === undefined) return;
		submitProgress = m.assignment_editor_progress_submit();
		submitError = null;
		let done = false;
		try {
			await submitForGrading(scope, assignId, acceptStatement);
			submitting = false;
			done = true;
			toast.success(m.assignment_sent());
		} catch (error) {
			submitError = failureText(toDataError(error));
		} finally {
			submitProgress = null;
			afterSubmissionChange(scope, courseId, assignId);
		}
		if (done) await refocusStatus();
	}

	async function remove() {
		const scope = session.current;
		if (scope === undefined) return;
		try {
			await removeSubmission(scope, assignId);
			toast.success(m.assignment_removed());
		} finally {
			afterSubmissionChange(scope, courseId, assignId);
		}
		void refocusStatus();
	}
</script>

<svelte:head>
	<title>{title} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<div class="assignment" data-course={course.slot}>
		{#if assignment}
			<PageHeader
				{title}
				eyebrow={course.title}
				{back}
				subtitle={model.schedule ? dueLine(model.schedule, clock.now) : undefined}
			/>
		{:else if model.list.data !== undefined || model.list.error}
			<PageHeader title={m.assignment_title_fallback()} eyebrow={course.title} {back} />
		{:else}
			<div class={['title-pending', { shown: gate.visible }]} aria-busy="true">
				<PageHeader title={BLANK} subtitle={BLANK} eyebrow={course.title} {back} />
			</div>
		{/if}

		{#if stale}
			<div class="notice">
				<ErrorState
					variant="stale"
					message={errorMessage(stale.error)}
					updatedAt={stale.updatedAt}
					onretry={model.refresh}
					retrying={model.list.refreshing || model.status.refreshing}
				/>
			</div>
		{/if}

		{#if model.list.data === undefined && model.list.error}
			<ErrorState
				title={m.assignment_load_failed()}
				message={errorMessage(model.list.error)}
				onretry={model.refresh}
				retrying={model.list.refreshing}
			/>
		{:else if model.missing}
			<EmptyState
				size="page"
				icon={IconClipboardText}
				title={m.assignment_not_found_title()}
				body={m.assignment_not_found_body()}
			>
				{#snippet action()}
					<Button href={back.href}>{m.assignment_back_to_list()}</Button>
				{/snippet}
			</EmptyState>
		{:else if assignment === undefined || model.schedule === undefined}
			<div class="layout" aria-busy="true" aria-label={m.course_loading()}>
				{#if gate.visible}
					<div class="side">
						<div class="panel-skeleton">
							<Skeleton width="40%" height="0.8em" />
							<Skeleton width="70%" height="1.6em" />
							<Skeleton width="55%" />
							<Skeleton width="100%" height="48px" radius="999px" />
						</div>
					</div>
					<div class="main">
						<Skeleton width="20%" height="0.8em" />
						<Skeleton lines={4} />
						<Skeleton width="100%" height="64px" radius="20px" />
					</div>
				{/if}
			</div>
		{:else}
			<div class="layout">
				<aside class="side">
					<StatusPanel
						{assignment}
						schedule={model.schedule}
						{submission}
						config={model.config}
						error={model.status.data === undefined ? model.statusError : undefined}
						retrying={model.status.refreshing}
						onretry={model.status.refresh}
						now={clock.now}
						onsubmit={() => {
							submitError = null;
							accepted = false;
							submitting = true;
						}}
						onremove={() => (removing = true)}
						onstart={() => (starting = true)}
					/>
				</aside>

				<div class="main">
					{#if previous}
						{@const shown = gradeText(previous.grade)}
						<section class="section" aria-labelledby="assignment-previous">
							<h2 class="heading" id="assignment-previous">{m.assignment_previous_title()}</h2>
							<details class="previous">
								<summary>
									<IconCaretRight class="caret" aria-hidden="true" />
									{shown
										? m.assignment_previous_summary({ number: previous.number, grade: shown })
										: m.assignment_previous_summary_feedback({ number: previous.number })}
								</summary>
								<div class="previous-body">
									<p class="quiet">{m.assignment_previous_note()}</p>
									{#if previous.grade.feedback}
										<TeacherHtml html={previous.grade.feedback} />
									{/if}
									{#if previous.grade.files.length > 0}
										<FileList
											files={previous.grade.files}
											{courseId}
											label={m.assignment_feedback_files()}
										/>
									{/if}
								</div>
							</details>
						</section>
					{/if}

					{#snippet brief()}
						{#if intro || files.length > 0 || !hasInstructions}
							<section class="section" aria-labelledby="assignment-brief">
								<h2 class="heading" id="assignment-brief">{m.assignment_brief()}</h2>
								{#if intro}
									<TeacherHtml html={intro} />
								{:else if files.length === 0}
									<p class="quiet">{m.assignment_no_brief()}</p>
								{/if}
								{#if files.length > 0}
									<FileList {files} {courseId} label={m.assignment_attachments()} />
								{/if}
							</section>
						{/if}
						{#if hasInstructions}
							<section class="section" aria-labelledby="assignment-instructions">
								<h2 class="heading" id="assignment-instructions">{m.assignment_instructions()}</h2>
								{#if instructions.html}<TeacherHtml html={instructions.html} />{/if}
								{#if instructions.files.length > 0}
									<FileList
										files={instructions.files}
										{courseId}
										label={m.assignment_instructions()}
									/>
								{/if}
							</section>
						{/if}
					{/snippet}

					{#snippet handedIn()}
						{#if showSubmission && submission}
							<section class="section" aria-labelledby="assignment-submission">
								<h2 class="heading" id="assignment-submission">{m.assignment_your_submission()}</h2>
								{#if submission.text}
									<div class="text-card" aria-label={m.assignment_online_text()}>
										<TeacherHtml html={submission.text} />
									</div>
								{/if}
								{#if handedFiles.length > 0}
									<FileList
										files={handedFiles}
										{courseId}
										label={m.assignment_submission_files()}
									/>
								{/if}
							</section>
						{/if}
					{/snippet}

					{#if !statusSettled}
						<div class="sections-pending" aria-busy="true">
							<Skeleton width="20%" height="0.8em" />
							<Skeleton variant="text" lines={3} />
							<Skeleton width="100%" height="64px" radius="20px" />
						</div>
					{:else if showFeedback && feedback}
						<section class="section feedback" aria-labelledby="assignment-feedback">
							<h2 class="heading" id="assignment-feedback">{m.assignment_feedback()}</h2>
							{#if feedback.feedback}
								<div class="feedback-card"><TeacherHtml html={feedback.feedback} /></div>
							{/if}
							{#if feedback.files.length > 0}
								<FileList files={feedback.files} {courseId} label={m.assignment_feedback_files()} />
							{/if}
						</section>
						{@render handedIn()}
						{@render brief()}
					{:else}
						{@render brief()}
						{@render handedIn()}
					{/if}
				</div>
			</div>
		{/if}
	</div>
</Page>

{#if submission && model.config}
	{@const recap = recapOf(submission)}
	<SubmitDialog
		bind:open={submitting}
		bind:accepted
		title={m.assignment_submit_title()}
		message={m.assignment_submit_body({ recap })}
		{late}
		confirmLabel={m.assignment_submit_confirm()}
		statement={model.config.statement}
		progress={submitProgress}
		error={submitError}
		online={online.current !== false}
		onconfirm={(accept) => void submit(accept)}
		onclose={() => (submitting = false)}
	/>
{/if}

{#if limit !== null}
	<StartDialog
		bind:open={starting}
		{courseId}
		{assignId}
		{limit}
		onstarted={() => goto(editorPath(courseId, assignId))}
	/>
{/if}

<ConfirmDialog
	bind:open={removing}
	title={m.assignment_remove_title()}
	message={m.assignment_remove_body()}
	confirmLabel={m.assignment_remove_confirm()}
	tone="danger"
	onconfirm={remove}
	errorMessage={(error) => `${m.assignment_remove_failed()}: ${failureText(toDataError(error))}`}
/>

<FileDialogs />

<style>
	.assignment {
		container: assignment / inline-size;
		display: grid;
		gap: var(--sp-6);
	}

	.notice {
		margin-block-end: calc(-1 * var(--sp-2));
	}

	.layout {
		display: grid;
		gap: var(--sp-6);
		align-items: start;
	}

	.main {
		display: grid;
		gap: var(--sp-7);
		min-inline-size: 0;
		max-inline-size: var(--w-reading);
	}

	.section {
		display: grid;
		gap: var(--sp-4);
		min-inline-size: 0;
	}

	.heading {
		display: flex;
		align-items: center;
		gap: 8px;
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	/* Android's ❀ before each section, in the course's colour. */
	.heading::before {
		content: '';
		inline-size: 12px;
		block-size: 12px;
		background: currentColor;
		clip-path: polygon(
			50% 0%,
			61% 20%,
			85% 15%,
			80% 39%,
			100% 50%,
			80% 61%,
			85% 85%,
			61% 80%,
			50% 100%,
			39% 80%,
			15% 85%,
			20% 61%,
			0% 50%,
			20% 39%,
			15% 15%,
			39% 20%
		);
	}

	.quiet {
		color: var(--text-muted);
		font: var(--type-body);
		font-style: italic;
	}

	.text-card,
	.feedback-card {
		padding: 18px 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.feedback-card {
		background: var(--course-container);
		box-shadow: var(--inner-light);
	}

	.panel-skeleton {
		display: grid;
		gap: 14px;
		padding: 20px;
		border-radius: var(--r-xl);
		background: var(--surface);
	}

	/*
	 * Only beside the card: under it, the card grows as the status arrives and would push the
	 * placeholder down.
	 */
	.sections-pending {
		display: none;
		gap: var(--sp-4);
	}

	.layout[aria-busy='true'] .main {
		gap: var(--sp-4);
	}

	.previous {
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.previous summary {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 14px 18px;
		border-radius: inherit;
		font: var(--type-ui-strong);
		list-style: none;
		cursor: pointer;
	}

	.previous summary::-webkit-details-marker {
		display: none;
	}

	.previous summary:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.previous :global(.caret) {
		inline-size: 14px;
		block-size: 14px;
		color: var(--text-muted);
		transition: transform var(--dur-base) var(--spring-standard);
	}

	.previous[open] :global(.caret) {
		transform: rotate(90deg);
	}

	.previous-body {
		display: grid;
		gap: var(--sp-4);
		padding: 0 18px 18px;
	}

	/* While the name loads, the title's line holds a placeholder bar. */
	.title-pending.shown :global(:is(h1, .subtitle)) {
		inline-size: min(60%, 26ch);
		border-radius: var(--r-sm);
		background: var(--track);
	}

	.title-pending.shown :global(.subtitle) {
		inline-size: min(30%, 14ch);
	}

	@container assignment (width >= 860px) {
		.sections-pending {
			display: grid;
		}

		.layout {
			grid-template-columns: minmax(0, var(--w-reading)) minmax(300px, 360px);
			justify-content: start;
			gap: var(--sp-8);
		}

		.side {
			position: sticky;
			inset-block-start: calc(var(--shell-inset-top) + var(--sp-5));
			grid-column: 2;
			grid-row: 1;
		}

		.main {
			grid-column: 1;
			grid-row: 1;
		}
	}
</style>
