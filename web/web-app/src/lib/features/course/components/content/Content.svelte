<script lang="ts">
	import { tick } from 'svelte';
	import { SvelteSet } from 'svelte/reactivity';
	import { afterNavigate, beforeNavigate, goto, snapshot } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowsInLineVertical from '~icons/ph/arrows-in-line-vertical';
	import IconArrowsOutLineVertical from '~icons/ph/arrows-out-line-vertical';
	import IconBooksDuotone from '~icons/ph/books-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import ProgressBar from '#lib/ui/ProgressBar.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import {
		completionSummary,
		displayName,
		findModule,
		moduleTarget,
		type OutlineSection
	} from '../../content.ts';
	import { getCourse } from '../../course.svelte.ts';
	import { FileOpener, setFileOpener } from '../../file/opener.svelte.ts';
	import { coursePath } from '../../links.ts';
	import { continueWatching } from '../../video/progress.ts';
	import FileDialogs from '../FileDialogs.svelte';
	import FolderDialog from './FolderDialog.svelte';
	import SectionCard from './SectionCard.svelte';
	import SectionIndex from './SectionIndex.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	const course = getCourse();
	setFileOpener(new FileOpener(() => session.current));

	const contents = $derived(course.contents);
	const outline = $derived(course.outline ?? []);
	const progress = $derived(course.progress.data ?? {});
	const lastSeen = $derived(continueWatching(progress, course.id)?.cmId);
	const watched = (cmId: number) => progress[cmId]?.completed === true;
	const summary = $derived(completionSummary(outline, watched));
	const sectionCompletion = $derived(
		new Map(outline.map((card) => [card.section.id, completionSummary([card], watched)]))
	);

	let now = $state(new Date());
	const expanded = new SvelteSet<number>();
	const allOpen = $derived(
		outline.length > 0 && outline.every((card) => expanded.has(card.section.id))
	);

	snapshot({
		capture: () => [...expanded],
		restore: (ids: number[]) => {
			expanded.clear();
			for (const id of ids) expanded.add(id);
		}
	});

	const gate = new LoadingGate(() => contents.data === undefined && contents.status === 'pending');
	const updatedAt = $derived(contents.updatedAt ? new Date(contents.updatedAt) : undefined);

	function toggleAll() {
		if (allOpen) expanded.clear();
		else for (const card of outline) expanded.add(card.section.id);
	}

	/**
	 * Sections past the first few render once near the screen. The first ones never wait: they show
	 * on arrival, and if they were measured late they would grow in sight and push the rest down.
	 */
	const DEFERRED_FROM = 6;
	let pinned = $state<number>();

	/** Opens the card and brings it into view, as the index and "go to lesson" do. */
	async function reveal(card: OutlineSection, element?: string) {
		pinned = card.section.id;
		expanded.add(card.section.id);
		await tick();
		const target = document.getElementById(element ?? `section-${card.section.id}`);
		if (target === null) return;
		const reduced = reducedMotion.current;
		target.scrollIntoView({
			block: element ? 'center' : 'start',
			behavior: reduced ? 'instant' : 'smooth'
		});
		target.querySelector<HTMLElement>('a, button')?.focus({ preventScroll: true });
	}

	/* "Go to lesson" lands here with the recording's row in the hash. */
	let revealed = '';
	$effect(() => {
		const hash = page.url.hash;
		if (outline.length === 0 || hash === revealed) return;
		revealed = hash;
		const cmId = /^#cm-(\d+)$/.exec(hash)?.[1];
		const found = cmId === undefined ? undefined : findModule(outline, Number(cmId));
		if (found) void reveal(found.card, `cm-${cmId}`);
	});

	const folderId = $derived(Number(page.url.searchParams.get('folder')) || null);
	const folder = $derived.by(() => {
		if (folderId === null) return undefined;
		const found = findModule(outline, folderId);
		const target = found && moduleTarget(found.module, course.language);
		return found && target?.kind === 'folder'
			? { module: found.module, files: target.files }
			: undefined;
	});

	/* The folder stays in its dialog while the dialog closes. */
	let leaving = $state.raw<typeof folder>();
	const shownFolder = $derived(folder ?? leaving);
	beforeNavigate(() => {
		leaving = folder;
	});

	/* A folder opened from its row can close by going back; one opened by its address replaces it. */
	let folderPushed = false;
	afterNavigate(({ from, to, type }) => {
		folderPushed =
			type === 'link' &&
			to?.url.searchParams.has('folder') === true &&
			from?.url.pathname === to.url.pathname;
	});

	function closeFolder() {
		if (folderPushed) history.back();
		else void goto(coursePath(course.id, 'content'), { replace: true, reset: false });
	}
</script>

<svelte:head>
	<title>{m.course_tab_content()} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

<svelte:document
	onvisibilitychange={() => {
		if (document.visibilityState === 'visible') now = new Date();
	}}
/>

{#if contents.error && contents.data}
	<div class="notice">
		<ErrorState
			variant="stale"
			message={errorMessage(contents.error)}
			{updatedAt}
			onretry={contents.refresh}
			retrying={contents.refreshing}
		/>
	</div>
{/if}

{#if contents.status === 'error' && contents.error}
	<ErrorState
		title={m.course_content_failed()}
		message={errorMessage(contents.error)}
		onretry={contents.refresh}
		retrying={contents.refreshing}
	/>
{:else if contents.data === undefined}
	<div class="sections" aria-busy="true" aria-label={m.course_loading()}>
		{#if gate.visible}
			{#each { length: 6 }, index (index)}
				<div class="skeleton">
					<Skeleton width="44px" height="44px" radius="14px" />
					<span class="lines">
						<Skeleton width="{60 - index * 4}%" />
						<Skeleton width="32%" height="0.8em" />
					</span>
				</div>
			{/each}
		{/if}
	</div>
{:else if outline.length === 0}
	<EmptyState
		size="cover"
		icon={IconBooksDuotone}
		title={m.course_content_empty_title()}
		body={m.course_content_empty_body()}
	/>
{:else}
	<div class="content">
		<SectionIndex
			{outline}
			completion={sectionCompletion}
			bind:pinned
			onselect={(card) => void reveal(card)}
		/>
		<div class="list">
			<div class="toolbar">
				<div class="summary">
					<p class="count">{m.course_sections_count({ count: outline.length })}</p>
					{#if summary.tracked > 0}
						<div class="meter">
							<ProgressBar
								value={summary.done}
								max={summary.tracked}
								label={m.course_progress_label()}
								valueText={m.course_progress_value({ done: summary.done, total: summary.tracked })}
								size="sm"
							/>
						</div>
					{/if}
				</div>
				<Button
					variant="ghost"
					size="sm"
					icon={allOpen ? IconArrowsInLineVertical : IconArrowsOutLineVertical}
					onclick={toggleAll}
				>
					{allOpen ? m.course_collapse_all() : m.course_expand_all()}
				</Button>
			</div>
			<ol class="sections">
				{#each outline as card, index (card.section.id)}
					<li>
						<SectionCard
							{card}
							bind:open={
								() => expanded.has(card.section.id),
								(open) => (open ? expanded.add(card.section.id) : expanded.delete(card.section.id))
							}
							courseId={course.id}
							language={course.language}
							{progress}
							{lastSeen}
							{now}
							completion={sectionCompletion.get(card.section.id)}
							deferred={index >= DEFERRED_FROM}
						/>
					</li>
				{/each}
			</ol>
		</div>
	</div>
{/if}

{#if shownFolder}
	<FolderDialog
		open={folder !== undefined}
		title={displayName(shownFolder.module.name, course.language)}
		courseId={course.id}
		cmId={shownFolder.module.id}
		files={shownFolder.files}
		onclose={closeFolder}
	/>
{/if}

<FileDialogs />

<style>
	.notice {
		margin-block-end: var(--sp-5);
	}

	.content {
		display: grid;
		gap: var(--sp-7);
	}

	@container page (width >= 1000px) {
		.content {
			grid-template-columns: 220px minmax(0, 1fr);
			align-items: start;
		}
	}

	.list {
		display: grid;
		gap: 14px;
		min-inline-size: 0;
	}

	.toolbar {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: 8px 16px;
		padding-inline: 4px;
	}

	.summary {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px 20px;
		flex: 1 1 260px;
	}

	.count {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	/* Completion is state, in the course's colour, as the sections' badges. */
	.meter {
		--accent: var(--course-bar);
		flex: 1 1 200px;
		max-inline-size: 320px;
	}

	.sections {
		display: grid;
		gap: 10px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.skeleton {
		display: grid;
		grid-template-columns: 44px minmax(0, 1fr);
		align-items: center;
		gap: 14px;
		padding: 14px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.lines {
		display: grid;
		gap: 6px;
	}
</style>
