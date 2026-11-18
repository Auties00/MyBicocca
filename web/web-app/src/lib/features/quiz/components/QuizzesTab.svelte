<script lang="ts">
	import { tick, untrack } from 'svelte';
	import { SvelteMap } from 'svelte/reactivity';
	import { goto, snapshot } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowsInLineVertical from '~icons/ph/arrows-in-line-vertical';
	import IconArrowsOutLineVertical from '~icons/ph/arrows-out-line-vertical';
	import IconExamDuotone from '~icons/ph/exam-duotone';
	import { now as minute } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { blockModules } from '#lib/features/course/content.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import ProgressBar from '#lib/ui/ProgressBar.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { quizzesPath } from '../links.ts';
	import {
		completionOf,
		groupQuizzes,
		matches,
		nextToClose,
		showsProgress,
		type Filter,
		type QuizGroup as Group,
		type QuizItem
	} from '../model.ts';
	import { courseQuizzesQuery } from '../queries.ts';
	import NextQuiz from './NextQuiz.svelte';
	import QuizGroup from './QuizGroup.svelte';
	import QuizIndex from './QuizIndex.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	/** Below this many quizzes every section starts open, and filters would only add noise. */
	const FEW = 12;
	const FILTERS_FROM = 6;
	/*
	 * Sections past the first screen render once scrolled near. The first ones never wait: drawn
	 * late, they would grow in sight and push the rest down.
	 */
	const DEFERRED_FROM = 6;
	const FILTERS: readonly Filter[] = ['all', 'todo', 'soon'];

	const course = getCourse();
	const quizzes = query(() => session.current && courseQuizzesQuery(session.current, course.id));

	const now = $derived(minute.current);

	/* The course page tells each quiz's section; if it can't load, the quizzes go ungrouped. */
	const contentsSettled = $derived(
		course.contents.data !== undefined || course.contents.status === 'error'
	);
	const sections = $derived(
		course.outline?.map((card) => ({
			key: String(card.section.id),
			ordinal: card.ordinal,
			title: card.title,
			modules: blockModules(card.blocks)
		}))
	);
	const groups = $derived(
		quizzes.data && contentsSettled ? groupQuizzes(quizzes.data.quizzes, sections) : undefined
	);
	const items = $derived(groups?.flatMap((group) => group.items) ?? []);
	const completion = $derived(completionOf(items));
	const filter = $derived.by((): Filter => {
		const value = page.url.searchParams.get('filter');
		return FILTERS.includes(value as Filter) ? (value as Filter) : 'all';
	});
	const filtering = $derived(items.length >= FILTERS_FROM);
	const shown = (item: QuizItem) => !filtering || matches(item, filter, now);
	const visibleGroups = $derived(
		(groups ?? [])
			.map((group) => ({ group, items: group.items.filter(shown) }))
			.filter((entry) => entry.items.length > 0)
	);
	/* Sections get an index beside the list on wide screens, once there are several. */
	const indexed = $derived((groups?.length ?? 0) > 1);

	/*
	 * Sections open as the student left them; untouched ones follow the default, set from the
	 * first render (no effect, so nothing opens after the page has drawn): every section of a short
	 * list, else the one holding the quiz that closes first.
	 */
	const choices = new SvelteMap<string, boolean>();
	const openByDefault = $derived.by((): ReadonlySet<string> => {
		if (groups === undefined) return new Set();
		if (items.length <= FEW) return new Set(groups.map((group) => group.key));
		const soonest = nextToClose(
			groups,
			untrack(() => now),
			undefined
		);
		const first = groups.find((group) => group.items.some((item) => item === soonest)) ?? groups[0];
		return new Set(first ? [first.key] : []);
	});
	const isOpen = (key: string) => choices.get(key) ?? openByDefault.has(key);
	const allOpen = $derived(
		visibleGroups.length > 0 && visibleGroups.every(({ group }) => isOpen(group.key))
	);
	const progress = $derived(showsProgress(completion));
	/* When Moodle tracks every quiz, "4 of 9" already says how many there are. */
	const counted = $derived(!progress || completion.tracked < completion.total);

	/* The quiz to do that closes first: what a student opens this tab for, unless it's in sight. */
	const firstRow = $derived.by(() => {
		const first = visibleGroups[0];
		return first && isOpen(first.group.key) ? first.items[0] : undefined;
	});
	const next = $derived(groups ? nextToClose(groups, now, firstRow) : undefined);

	snapshot({
		capture: () => (groups ?? []).map((group): [string, boolean] => [group.key, isOpen(group.key)]),
		restore: (state: [string, boolean][]) => {
			choices.clear();
			for (const [key, open] of state) choices.set(key, open);
		}
	});

	function toggleAll() {
		const open = !allOpen;
		for (const { group } of visibleGroups) choices.set(group.key, open);
	}

	/** Opens the section and brings it into view, as the index does. */
	async function reveal(group: Group) {
		choices.set(group.key, true);
		await tick();
		const target = document.getElementById(`quiz-group-${group.key}`);
		if (target === null) return;
		const reduced = reducedMotion.current;
		target.scrollIntoView({ block: 'start', behavior: reduced ? 'instant' : 'smooth' });
		target.querySelector<HTMLElement>('.toggle')?.focus({ preventScroll: true });
	}

	function setFilter(value: Filter) {
		void goto(quizzesPath(course.id, value === 'all' ? undefined : value), {
			replace: true,
			reset: false
		});
	}

	const filterOptions = $derived([
		{ value: 'all' as const, label: m.quiz_filter_all() },
		{ value: 'todo' as const, label: m.quiz_filter_todo() },
		{ value: 'soon' as const, label: m.quiz_filter_soon() }
	]);

	const gate = new LoadingGate(() => groups === undefined && quizzes.status !== 'error');
	const updatedAt = $derived(quizzes.updatedAt ? new Date(quizzes.updatedAt) : undefined);
</script>

<svelte:head>
	<title>{m.course_tab_quizzes()} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

{#if quizzes.error && quizzes.data}
	<div class="notice">
		<ErrorState
			variant="stale"
			message={errorMessage(quizzes.error)}
			{updatedAt}
			onretry={quizzes.refresh}
			retrying={quizzes.refreshing}
		/>
	</div>
{/if}

{#if quizzes.status === 'error' && quizzes.error && !quizzes.data}
	<ErrorState
		title={m.quiz_load_failed()}
		message={errorMessage(quizzes.error)}
		onretry={quizzes.refresh}
		retrying={quizzes.refreshing}
	/>
{:else if groups === undefined}
	<div class="list" aria-busy="true" aria-label={m.quiz_loading()}>
		{#if gate.visible}
			<div class="toolbar">
				<span class="skeleton-count"><Skeleton variant="text" width="80px" /></span>
			</div>
			{#each { length: 3 }, index (index)}
				<div class="skeleton">
					<Skeleton width="44px" height="44px" radius="14px" />
					<span class="lines">
						<Skeleton width="{56 - index * 8}%" />
						<Skeleton width="28%" height="0.8em" />
					</span>
				</div>
			{/each}
		{/if}
	</div>
{:else if items.length === 0}
	<EmptyState
		size="cover"
		icon={IconExamDuotone}
		title={m.quiz_empty_title()}
		body={m.quiz_empty_body()}
	/>
{:else}
	<div class={['layout', { indexed }]}>
		{#if indexed}
			<div class="aside">
				<QuizIndex groups={visibleGroups.map(({ group }) => group)} onselect={reveal} />
			</div>
		{/if}
		<div class="list">
			<div class="toolbar">
				{#if counted || progress}
					<div class="summary">
						{#if counted}
							<p class="count">{m.quiz_count({ count: items.length })}</p>
						{/if}
						{#if progress}
							<div class="meter">
								<ProgressBar
									value={completion.done}
									max={completion.tracked}
									label={m.quiz_progress_label()}
									valueText={m.quiz_progress_value({
										done: completion.done,
										total: completion.tracked
									})}
									size="sm"
								/>
							</div>
						{/if}
					</div>
				{/if}
				{#if filtering || visibleGroups.length > 1}
					<div class="tools">
						{#if filtering}
							<div class="filter">
								<SegmentedControl
									options={filterOptions}
									value={filter}
									label={m.quiz_filter_label()}
									full
									onchange={setFilter}
								/>
							</div>
						{/if}
						{#if visibleGroups.length > 1}
							<!-- Phones keep the filter and the toggle on one row: the toggle as an icon. -->
							<span class="toggle-all wide">
								<Button
									variant="ghost"
									size="sm"
									icon={allOpen ? IconArrowsInLineVertical : IconArrowsOutLineVertical}
									onclick={toggleAll}
								>
									{allOpen ? m.quiz_collapse_all() : m.quiz_expand_all()}
								</Button>
							</span>
							<span class="toggle-all compact">
								<IconButton
									icon={allOpen ? IconArrowsInLineVertical : IconArrowsOutLineVertical}
									label={allOpen ? m.quiz_collapse_all() : m.quiz_expand_all()}
									onclick={toggleAll}
								/>
							</span>
						{/if}
					</div>
				{/if}
			</div>
			{#if next}
				<NextQuiz item={next} courseId={course.id} language={course.language} {now} />
			{/if}
			{#if visibleGroups.length === 0}
				<EmptyState title={m.quiz_filter_empty()}>
					{#snippet action()}
						<Button variant="tonal" onclick={() => setFilter('all')}>{m.quiz_filter_all()}</Button>
					{/snippet}
				</EmptyState>
			{:else}
				<ol class="groups">
					{#each visibleGroups as { group, items: rows }, index (group.key)}
						<li>
							<QuizGroup
								{group}
								items={rows}
								deferred={index >= DEFERRED_FROM}
								bind:open={() => isOpen(group.key), (open) => choices.set(group.key, open)}
								courseId={course.id}
								language={course.language}
								{now}
							/>
						</li>
					{/each}
				</ol>
			{/if}
		</div>
	</div>
{/if}

<style>
	.notice {
		margin-block-end: var(--sp-5);
	}

	.layout {
		display: grid;
		gap: var(--sp-7);
	}

	.aside {
		display: none;
	}

	/* As the Content tab: the section index on the left, the list on the right. */
	@container page (width >= 1000px) {
		.layout.indexed {
			grid-template-columns: 240px minmax(0, 1fr);
			align-items: start;
		}

		.aside {
			position: sticky;
			inset-block-start: calc(var(--topbar-h) + var(--control-h-lg) + 24px);
			display: block;
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
		gap: 12px 20px;
		min-block-size: 40px;
		padding-inline: 4px;
	}

	/* Completion is state, in the course's colour, as the sections' badges. */
	.summary {
		--accent: var(--course-bar);
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px 20px;
		flex: 1 1 240px;
	}

	.count {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.meter {
		flex: 1 1 160px;
	}

	.tools {
		display: flex;
		align-items: center;
		gap: 8px;
		flex: 1 1 100%;
	}

	.filter {
		flex: 1 1 auto;
		min-inline-size: 0;
	}

	.toggle-all {
		display: inline-flex;
		flex-shrink: 0;
	}

	.toggle-all.wide {
		display: none;
	}

	@container page (width >= 560px) {
		.meter {
			max-inline-size: 280px;
		}

		.tools {
			flex: 0 1 auto;
			gap: 12px;
		}

		.filter {
			flex: 0 0 auto;
		}

		.toggle-all.wide {
			display: inline-flex;
		}

		.toggle-all.compact {
			display: none;
		}
	}

	.groups {
		display: grid;
		gap: 10px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.skeleton-count {
		font: var(--type-ui);
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
