<script lang="ts">
	import { onMount, tick } from 'svelte';
	import { online } from 'svelte/reactivity/window';
	import { afterNavigate, beforeNavigate, goto } from '$app/navigation';
	import type { ResolvedPathname } from '$app/types';
	import IconCaretRight from '~icons/ph/caret-right-bold';
	import IconFloppyDisk from '~icons/ph/floppy-disk';
	import IconPaperPlaneTilt from '~icons/ph/paper-plane-tilt';
	import IconLockSimple from '~icons/ph/lock-simple-duotone';
	import IconTimer from '~icons/ph/timer-duotone';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import FileDialogs from '#lib/features/course/components/FileDialogs.svelte';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import RichEditor from '#lib/features/course/components/editor/RichEditor.svelte';
	import type { CourseFile } from '#lib/features/course/content.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { editorHtml } from '#lib/features/course/editor/editorHtml.ts';
	import { FileOpener, setFileOpener } from '#lib/features/course/file/opener.svelte.ts';
	import { formatTime } from '#lib/format/dates.ts';
	import { sanitizeHtml } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { AssignmentState } from '../assignment.svelte.ts';
	import { addPicked } from '../draft.ts';
	import { acceptAttribute } from '../filetypes.ts';
	import {
		editorContext,
		failureText,
		formatList,
		formatMoment,
		formatSpan,
		lateNote,
		onMoodleReason,
		problemText,
		rulesText
	} from '../labels.ts';
	import { assignmentPath, assignmentsPath } from '../links.ts';
	import {
		activityInstructions,
		awaitsStart,
		handedIn,
		onMoodleOnly,
		phaseOf,
		submittable,
		tickAfter,
		timedEnd,
		timeLimitOf,
		type Submission
	} from '../model.ts';
	import { countWords, htmlToText } from '../onlineText.ts';
	import { afterSubmissionChange } from '../queries.ts';
	import { recapOf } from '../recap.ts';
	import {
		hasChanges,
		planSave,
		saveSubmission,
		submitForGrading,
		type DraftFile,
		type EditedSubmission,
		type SaveStep
	} from '../writes.ts';
	import DraftFiles from './DraftFiles.svelte';
	import DropZone from './DropZone.svelte';
	import StartDialog from './StartDialog.svelte';
	import SubmitDialog from './SubmitDialog.svelte';

	interface Props {
		courseId: number;
		assignId: number;
	}

	let { courseId, assignId }: Props = $props();

	/** Holds the eyebrow's line while the assignment's name loads. */
	const BLANK = ' ';

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
	const detailHref = $derived(assignmentPath(courseId, assignId));

	/* The editor starts from Moodle's answer of now, not from a cached one. */
	let fresh = $state(false);
	onMount(() => {
		void model.status.refresh().finally(() => (fresh = true));
	});

	let started = $state(false);
	/** The editor's content as typed. */
	let html = $state('');
	let files = $state.raw<DraftFile[]>([]);
	/** What changes are measured against: Moodle's submission at opening, then each saved draft. */
	let original = $state.raw({
		html: null as string | null,
		format: 1,
		source: '',
		clean: '',
		count: 0
	});
	/** What the last pick did: files replaced, or left out and why. */
	let notices = $state.raw<{ text: string; warning: boolean }[]>([]);

	const kept = (file: CourseFile): DraftFile => ({ key: file.url, kind: 'kept', file });

	function start(submission: Submission) {
		const source = submission.text === null ? '' : sanitizeHtml(submission.text);
		original = {
			html: submission.text,
			format: submission.textFormat,
			source,
			clean: editorHtml(source),
			count: submission.files.length
		};
		html = source;
		files = submission.files.map(kept);
		started = true;
	}

	$effect(() => {
		if (!started && fresh && model.submission && model.config) start(model.submission);
	});

	const assignment = $derived(model.assignment);
	const submission = $derived(model.submission);
	const schedule = $derived(model.schedule);
	const config = $derived(model.config);
	const rules = $derived(config?.files ?? null);
	const isOnline = $derived(online.current !== false);
	const editable = $derived(submission !== undefined && submission.canEdit && submittable(config));
	const phase = $derived(schedule && phaseOf(schedule, clock.now, submission?.canEdit));
	const limit = $derived(timeLimitOf(config, submission));
	/* A timed assignment not begun: its clock starts here, after the student confirms. */
	const mustStart = $derived(awaitsStart(config, submission));
	let starting = $state(false);
	const timerEnd = $derived(schedule && submission ? timedEnd(schedule, submission, config) : null);
	const instructions = $derived(
		assignment ? activityInstructions(assignment, model.status.data) : { html: null, files: [] }
	);

	const clean = $derived(editorHtml(html));
	const words = $derived(countWords(htmlToText(clean)));
	const wordLimit = $derived(config?.text?.wordLimit ?? null);
	const overLimit = $derived(wordLimit !== null && words > wordLimit);
	const edit = $derived<EditedSubmission>({
		textEnabled: config?.text != null,
		filesEnabled: rules !== null,
		originalHtml: original.html,
		originalFormat: original.format,
		initialHtml: original.clean,
		html: clean,
		files,
		originalFileCount: original.count
	});
	const plan = $derived(planSave(edit));
	const dirty = $derived(started && hasChanges(edit));
	const drafts = $derived(config?.drafts ?? true);

	let textElement = $state<HTMLElement>();
	/** What is being saved: a draft keeps the editor open, a hand-in leaves it. */
	let saving = $state<'draft' | 'handin' | null>(null);
	const busy = $derived(saving !== null);
	/** When this visit last saved the draft. */
	let savedAt = $state<number | null>(null);
	/* Handed in: the form stays as it was while the page goes back, rather than turning read-only. */
	let finished = $state(false);
	let progress = $state<string | null>(null);
	let failure = $state<string | null>(null);
	let confirming = $state(false);
	let confirmError = $state<string | null>(null);
	let accepted = $state(false);

	const canSaveDraft = $derived(isOnline && !busy && dirty && plan.hasContent && !overLimit);
	const canHandIn = $derived(
		isOnline &&
			!busy &&
			plan.hasContent &&
			!overLimit &&
			(dirty || (drafts && submission?.state === 'draft' && submission.canSubmit))
	);
	const blocker = $derived.by(() => {
		if (!started || busy) return null;
		if (!isOnline) return m.assignment_offline_hint();
		if (!plan.hasContent) {
			if (!edit.filesEnabled) return m.assignment_editor_empty_text_hint();
			return edit.textEnabled
				? m.assignment_editor_empty_hint()
				: m.assignment_editor_empty_files_hint();
		}
		if (overLimit && wordLimit !== null)
			return m.assignment_editor_words_over({ limit: wordLimit });
		return null;
	});

	/** Why the submission can't be changed from here. */
	const lockedReason = $derived.by(() => {
		if (submission === undefined || config === undefined) return undefined;
		if (handedIn(submission.stage)) return m.assignment_locked_note();
		if (onMoodleOnly(config)) return onMoodleReason(config);
		if (!submittable(config)) return m.assignment_nothing_to_submit();
		if (phase === 'upcoming' && schedule?.opens) {
			return m.assignment_editor_locked_upcoming({ moment: formatMoment(schedule.opens) });
		}
		return m.assignment_editor_locked_closed();
	});

	function stepText(step: SaveStep): string {
		switch (step.kind) {
			case 'prepare':
				return m.assignment_editor_progress_prepare();
			case 'upload':
				return m.assignment_editor_progress_upload({
					index: step.index + 1,
					total: step.total,
					name: step.name
				});
			case 'save':
				return m.assignment_editor_progress_save();
			case 'submit':
				return m.assignment_editor_progress_submit();
		}
	}

	let picks = 0;
	function add(picked: File[]) {
		if (rules === null) return;
		const added = addPicked(files, picked, rules, (file) => `${file.name}:${++picks}`);
		files = added.files;
		notices = [
			...added.replaced.map((name) => ({
				text: m.assignment_editor_replaced({ name }),
				warning: false
			})),
			...added.rejected.map((item) => ({
				text: problemText(item.name, item.problem),
				warning: true
			})),
			...(added.dropped.length > 0
				? [
						{
							text: m.assignment_editor_over_count({
								count: added.dropped.length,
								names: formatList(added.dropped)
							}),
							warning: true
						}
					]
				: [])
		];
	}

	function remove(key: string) {
		files = files.filter((file) => file.key !== key);
		notices = [];
	}

	/* Leaving: back to the page the editor was opened from, else to it in place of the editor. */
	let openedFromDetail = false;
	let leaving = false;
	afterNavigate(({ from }) => {
		openedFromDetail = from?.url?.pathname === detailHref;
	});

	function close() {
		leaving = true;
		if (openedFromDetail) history.back();
		else void goto(detailHref, { replace: true });
	}

	/**
	 * After a draft save, what was saved is what changes are measured against. The text is taken
	 * as sent, so typing goes on undisturbed; the files come back from Moodle, which now has the
	 * new ones. If that reload fails they stay as they are, and the next save sends them again.
	 */
	async function rebase(saved: string, edited: boolean) {
		const before = model.status.updatedAt;
		await model.status.refresh();
		const submission = model.submission;
		const reloaded = submission !== undefined && model.status.updatedAt !== before;
		if (reloaded) files = submission.files.map(kept);
		original = {
			...original,
			html: edited ? saved || null : original.html,
			format: edited ? 1 : original.format,
			clean: saved,
			count: reloaded ? submission.files.length : original.count
		};
	}

	async function run(submit: { acceptStatement: boolean } | null) {
		const scope = session.current;
		if (scope === undefined || busy) return;
		const handIn = submit !== null || !drafts;
		/* The text stays open to typing while a draft saves: this is what goes. */
		const sent = { plan, clean, edited: clean !== original.clean };
		saving = handIn ? 'handin' : 'draft';
		failure = null;
		confirmError = null;
		try {
			if (submit !== null && !dirty) {
				progress = stepText({ kind: 'submit' });
				await submitForGrading(scope, assignId, submit.acceptStatement);
			} else {
				await saveSubmission(scope, assignId, sent.plan, submit, (step) => {
					progress = stepText(step);
				});
			}
			confirming = false;
			afterSubmissionChange(scope, courseId, assignId);
			if (handIn) {
				finished = true;
				toast.success(m.assignment_sent());
				close();
				return;
			}
			await rebase(sent.clean, sent.edited);
			savedAt = Date.now();
			await tick();
			/* The save button disables itself once there is nothing left to save. */
			const active = document.activeElement;
			if (active === null || active === document.body) {
				(textElement ?? document.getElementById('editor-files'))?.focus();
			}
		} catch (error) {
			const message = failureText(toDataError(error));
			if (confirming) confirmError = message;
			else failure = message;
			afterSubmissionChange(scope, courseId, assignId);
		} finally {
			saving = null;
			progress = null;
		}
	}

	function openConfirm() {
		confirmError = null;
		accepted = false;
		confirming = true;
	}

	/* Unsaved edits: an in-app navigation asks first; closing the tab gets the browser's prompt. */
	let discarding = $state(false);
	let pending: { delta: number } | { href: ResolvedPathname } | null = null;

	beforeNavigate((navigation) => {
		if (leaving) return;
		if (busy) {
			navigation.cancel();
			return;
		}
		if (!dirty) return;
		navigation.cancel();
		if (navigation.type === 'leave') return;
		pending =
			navigation.type === 'popstate'
				? { delta: navigation.delta }
				: navigation.to
					? {
							href: `${navigation.to.url.pathname}${navigation.to.url.search}${navigation.to.url.hash}` as ResolvedPathname
						}
					: null;
		discarding = true;
	});

	function discard() {
		leaving = true;
		discarding = false;
		if (pending === null) return;
		if ('delta' in pending) history.go(pending.delta);
		else void goto(pending.href);
	}

	registerShortcuts(
		() => drafts && [{ keys: [['mod', 's']], label: m.assignment_editor_save_draft() }]
	);

	/* Ctrl/⌘+S saves the draft, unless a dialog is open: it owns the keyboard. */
	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.key.toLowerCase() !== 's') return;
		if (!(event.ctrlKey || event.metaKey) || event.shiftKey || event.altKey) return;
		event.preventDefault();
		if (confirming || discarding) return;
		if (drafts && canSaveDraft) void run(null);
	}

	const gate = new LoadingGate(() => !started);
	const recap = $derived(
		recapOf({ files: plan.files ?? files, text: edit.textEnabled ? clean : null })
	);
	const full = $derived(rules !== null && files.length >= rules.maxFiles);
</script>

<svelte:head>
	<title
		>{m.assignment_editor_title()} · {assignment?.name ?? m.assignment_title_fallback()} · {m.app_name()}</title
	>
</svelte:head>

<svelte:window onkeydown={keydown} />

<!-- A focused flow: on phones the tab bar steps aside, the top bar and Back stay. -->
<Page width="reading" tabBar={false}>
	<div class="editor" data-course={course.slot}>
		<PageHeader
			title={m.assignment_editor_title()}
			eyebrow={assignment?.name ?? (model.list.data === undefined ? BLANK : undefined)}
			subtitle={schedule && phase && editable
				? editorContext(schedule, phase, clock.now, timerEnd)
				: undefined}
			back={{ href: detailHref, label: m.assignment_title_fallback() }}
		/>

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
				title={m.assignment_not_found_title()}
				body={m.assignment_not_found_body()}
			>
				{#snippet action()}
					<Button href={assignmentsPath(courseId)}>{m.assignment_back_to_list()}</Button>
				{/snippet}
			</EmptyState>
		{:else if fresh && submission === undefined && model.statusError}
			<ErrorState
				title={m.assignment_status_failed()}
				message={errorMessage(model.statusError)}
				onretry={model.status.refresh}
				retrying={model.status.refreshing}
			/>
		{:else if fresh && mustStart && limit !== null}
			<EmptyState
				size="page"
				icon={IconTimer}
				title={m.assignment_editor_begin_title()}
				body={m.assignment_begin_note({ span: formatSpan(limit) })}
			>
				{#snippet action()}
					<Button
						variant="solid"
						icon={IconTimer}
						disabled={!isOnline}
						onclick={() => (starting = true)}
					>
						{m.assignment_begin()}
					</Button>
				{/snippet}
			</EmptyState>
		{:else if fresh && submission && config && !editable && !finished}
			<EmptyState
				size="page"
				icon={IconLockSimple}
				title={m.assignment_editor_not_editable()}
				body={lockedReason}
			>
				{#snippet action()}
					<Button href={detailHref}>{m.assignment_back_to_assignment()}</Button>
				{/snippet}
			</EmptyState>
		{:else if !started || config === undefined}
			<div class="form" aria-busy="true" aria-label={m.course_loading()}>
				{#if gate.visible}
					<Skeleton width="30%" height="0.8em" />
					<Skeleton width="100%" height="180px" radius="14px" />
					<Skeleton width="20%" height="0.8em" />
					<Skeleton width="100%" height="168px" radius="20px" />
				{/if}
			</div>
		{:else}
			{#if instructions.html}
				<details class="instructions">
					<summary>
						<IconCaretRight class="caret" aria-hidden="true" />
						{m.assignment_editor_instructions_toggle()}
					</summary>
					<div class="instructions-body"><TeacherHtml html={instructions.html} size="sm" /></div>
				</details>
			{/if}

			<form
				class="form"
				onsubmit={(event) => {
					event.preventDefault();
					if (drafts && canSaveDraft) void run(null);
				}}
			>
				{#if config.text}
					<section class="section">
						<RichEditor
							label={m.assignment_editor_text_label()}
							initial={original.source}
							bind:html
							bind:element={textElement}
							placeholder={m.assignment_editor_text_placeholder()}
							disabled={saving === 'handin'}
							error={overLimit && wordLimit !== null
								? m.assignment_editor_words_over({ limit: wordLimit })
								: undefined}
						/>
						<p class={['words', { over: overLimit }]}>
							{wordLimit === null
								? m.assignment_editor_words({ count: words })
								: m.assignment_editor_words_limit({ count: words, limit: wordLimit })}
						</p>
					</section>
				{/if}

				{#if rules}
					<section class="section" aria-labelledby="editor-files">
						<div class="section-head">
							<h2 class="label" id="editor-files" tabindex="-1">{m.assignment_editor_files()}</h2>
							<span class="count">
								{m.assignment_editor_count({ count: files.length, max: rules.maxFiles })}
							</span>
						</div>
						{#if files.length > 0}
							<DraftFiles
								{files}
								disabled={busy}
								label={m.assignment_editor_files_label()}
								onremove={remove}
							/>
						{/if}
						<div class="notices" role="status">
							{#each notices as notice, index (index)}
								<p class={{ warning: notice.warning }}>{notice.text}</p>
							{/each}
						</div>
						{#if full}
							<DropZone
								compact
								accept={acceptAttribute(rules.types)}
								multiple={rules.maxFiles > 1}
								disabled={busy}
								action={rules.maxFiles === 1
									? m.assignment_editor_replace()
									: m.assignment_editor_choose()}
								hint={rules.maxFiles === 1
									? rulesText(rules)
									: `${rulesText(rules)} · ${m.assignment_editor_same_name_hint()}`}
								labelledby="editor-files"
								onfiles={add}
							/>
						{:else}
							<DropZone
								accept={acceptAttribute(rules.types)}
								multiple={rules.maxFiles - files.length > 1}
								disabled={busy}
								hint={rulesText(rules)}
								labelledby="editor-files"
								onfiles={add}
							/>
						{/if}
					</section>
				{/if}
			</form>

			{#if failure}
				<Banner tone="danger" title={m.assignment_editor_failed()} message={failure} role="alert" />
			{/if}

			<div class="bar">
				<p
					class={['bar-status', { idle: !progress && !blocker && (savedAt === null || dirty) }]}
					role="status"
				>
					{#if progress}
						<Spinner size={16} />{progress}
					{:else if blocker}
						{blocker}
					{:else if savedAt !== null}
						{dirty
							? m.assignment_editor_last_saved({ time: formatTime(savedAt) })
							: m.assignment_editor_saved_at({ time: formatTime(savedAt) })}
					{:else if drafts && !dirty}
						{m.assignment_editor_unchanged_hint()}
					{/if}
				</p>
				<div class="bar-actions">
					{#if drafts}
						<Button
							icon={IconFloppyDisk}
							disabled={!canSaveDraft}
							loading={saving === 'draft'}
							aria-keyshortcuts="Control+S Meta+S"
							onclick={() => void run(null)}
						>
							{m.assignment_editor_save_draft()}
						</Button>
					{/if}
					<Button
						variant="solid"
						icon={IconPaperPlaneTilt}
						disabled={!canHandIn}
						onclick={openConfirm}
					>
						{#if drafts}
							<span class="long">{m.assignment_editor_submit_for_grading()}</span>
							<span class="short">{m.assignment_editor_submit_short()}</span>
						{:else}
							{m.assignment_editor_submit_now()}
						{/if}
					</Button>
				</div>
			</div>
		{/if}
	</div>
</Page>

{#if config}
	<SubmitDialog
		bind:open={confirming}
		bind:accepted
		title={drafts ? m.assignment_submit_title() : m.assignment_submit_now_title()}
		message={drafts ? m.assignment_submit_body({ recap }) : m.assignment_submit_now_body({ recap })}
		late={schedule && phase ? lateNote(schedule, phase) : null}
		confirmLabel={drafts ? m.assignment_submit_confirm() : m.assignment_submit_now_confirm()}
		statement={config.statement}
		{progress}
		error={confirmError}
		online={isOnline}
		onconfirm={(accept) => void run(drafts ? { acceptStatement: accept } : null)}
		onclose={() => (confirming = false)}
	/>
{/if}

<Dialog
	bind:open={discarding}
	title={m.assignment_editor_discard_title()}
	size="sm"
	role="alertdialog"
	onclose={() => (pending = null)}
>
	<p class="discard">{m.assignment_editor_discard_body()}</p>
	{#snippet footer()}
		<Button variant="ghost" onclick={() => (discarding = false)} data-autofocus>
			{m.assignment_editor_keep_editing()}
		</Button>
		<Button variant="danger" onclick={discard}>{m.assignment_editor_discard_confirm()}</Button>
	{/snippet}
</Dialog>

{#if limit !== null}
	<StartDialog
		bind:open={starting}
		{courseId}
		{assignId}
		{limit}
		onstarted={() => model.status.refresh()}
	/>
{/if}

<FileDialogs />

<style>
	.editor {
		display: grid;
		gap: var(--sp-6);
	}

	/* What takes focus or news scrolls clear of the floating action bar. */
	.editor :global(:is(button, input, textarea, summary, a, [contenteditable])),
	.notices {
		scroll-margin-block-end: 96px;
	}

	.instructions {
		border-radius: var(--r-lg);
		background: var(--course-container);
		color: var(--text);
	}

	.instructions summary {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 14px 18px;
		border-radius: inherit;
		color: var(--course-ink);
		font: var(--type-ui-strong);
		list-style: none;
		cursor: pointer;
	}

	.instructions summary::-webkit-details-marker {
		display: none;
	}

	.instructions :global(.caret) {
		inline-size: 14px;
		block-size: 14px;
		transition: transform var(--dur-base) var(--spring-standard);
	}

	.instructions[open] :global(.caret) {
		transform: rotate(90deg);
	}

	.instructions summary:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.instructions-body {
		padding: 0 18px 16px;
	}

	.form {
		display: grid;
		gap: var(--sp-7);
	}

	.section {
		display: grid;
		gap: var(--sp-3);
	}

	.section-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 12px;
	}

	.label {
		color: var(--text);
		font: var(--type-ui-strong);
		outline: none;
	}

	.count,
	.words {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.words {
		margin-block-start: -4px;
	}

	.words.over {
		color: var(--danger);
	}

	.notices:empty {
		display: none;
	}

	.notices {
		display: grid;
		gap: 4px;
		padding: 10px 14px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.notices .warning {
		color: var(--warning);
		font-weight: 550;
	}

	.bar {
		position: sticky;
		inset-block-end: calc(var(--shell-inset-bottom) + 16px);
		z-index: var(--z-sticky);
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: 10px 16px;
		padding: 10px 10px 10px 18px;
		border-radius: var(--r-xl);
		background: var(--surface-raised);
		box-shadow: var(--elev-2), var(--inner-light);
	}

	.bar-status {
		display: flex;
		flex: 1 1 200px;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.bar-actions {
		display: flex;
		flex: 1 1 auto;
		flex-wrap: wrap;
		justify-content: flex-end;
		gap: 8px;
	}

	.discard {
		color: var(--text-muted);
		font: var(--type-body);
	}

	.short {
		display: none;
	}

	/* On phones the actions float at the bottom, as slim as they can; focus stops above them. */
	@media (width < 600px) {
		.editor :global(:is(button, input, textarea, summary, a, [contenteditable])),
		.notices {
			scroll-margin-block-end: 120px;
		}

		.bar {
			padding: 10px;
		}

		.bar-status:empty,
		.bar-status.idle {
			display: none;
		}

		.bar-actions {
			flex-basis: 100%;
		}

		.bar-actions > :global(*) {
			flex: 1 1 0;
		}

		/* Half a phone's width: the dialog it opens names the action in full. */
		.long {
			display: none;
		}

		.short {
			display: inline;
		}
	}
</style>
