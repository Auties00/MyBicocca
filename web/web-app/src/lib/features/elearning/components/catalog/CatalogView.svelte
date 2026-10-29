<script lang="ts">
	import { untrack } from 'svelte';
	import { SvelteSet } from 'svelte/reactivity';
	import { online } from 'svelte/reactivity/window';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import IconBookOpen from '~icons/ph/book-open-text';
	import IconFolder from '~icons/ph/folder-simple';
	import IconFolderDashed from '~icons/ph/folder-dashed-duotone';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass-duotone';
	import IconMapTrifold from '~icons/ph/signpost-duotone';
	import { publicKey } from '#lib/data/keys.ts';
	import { query, type QueryPolicy } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SearchField from '#lib/ui/SearchField.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { areaTint, type AreaTint } from '../../catalog/areas.ts';
	import { catalogClient } from '../../catalog/client.ts';
	import type { CatalogCourse, CatalogSearchResult } from '../../catalog/model.ts';
	import { searchRows } from '../../catalog/rows.ts';
	import { bareCode } from '../../courseCode.ts';
	import { catalogHref, coursesHref } from '../../links.ts';
	import { enrolledCoursesQuery } from '../../queries.ts';
	import { revealLater } from '../../reveal.ts';
	import AreaTile from './AreaTile.svelte';
	import EnrolButton from './EnrolButton.svelte';
	import EnrolDialog from './EnrolDialog.svelte';
	import SearchResults from './SearchResults.svelte';
	import { compact, expanded } from '#lib/ui/media.ts';

	interface Props {
		/** The category shown; null for the catalog's root. */
		node: string | null;
	}

	let { node }: Props = $props();

	/** The catalog ships with the app: what the worker answers never goes stale. */
	const FOREVER: QueryPolicy = { staleMs: Number.POSITIVE_INFINITY };
	const SEARCH_DELAY = 120;

	const root = query(() => ({
		key: publicKey('elearning.catalog', 'root'),
		load: () => catalogClient.root(),
		policy: FOREVER
	}));
	const level = query(
		() =>
			node !== null && {
				key: publicKey('elearning.catalog', 'level', node),
				load: () => catalogClient.level(node),
				policy: FOREVER
			}
	);
	const enrolledQuery = query(() => session.current && enrolledCoursesQuery(session.current));

	const offline = $derived(online.current === false);

	/* Courses enrolled here before the course list has reloaded. */
	const justEnrolled = new SvelteSet<number>();
	const enrolled = $derived(
		new Set([...(enrolledQuery.data ?? []).map((course) => course.id), ...justEnrolled])
	);

	const current = $derived(node === null ? undefined : (level.data ?? undefined));
	const tint = $derived(current ? areaTint(current.area.name) : undefined);
	const areaNames = $derived(
		new Map(
			(root.data?.sections ?? []).flatMap((section) =>
				section.areas.map((area) => [area.key, area.name] as const)
			)
		)
	);
	const tintOf = (area: string): AreaTint => areaTint(areaNames.get(area) ?? area);

	/* Search: the query lives in the URL too, so Back to this level brings its results back. */
	let text = $state(untrack(() => page.url.searchParams.get('q') ?? ''));
	const trimmed = $derived(text.trim());
	let result = $state.raw<CatalogSearchResult>();
	let searchFailed = $state(false);
	/* Bumped by Retry, to run the same search again. */
	let attempt = $state(0);

	$effect(() => {
		const q = trimmed;
		const scope = node;
		void attempt;
		let live = true;
		const timer = setTimeout(
			() => {
				mirrorQuery(q);
				if (q === '') return;
				catalogClient.search(q, scope).then(
					(answer) => {
						if (!live) return;
						result = answer;
						searchFailed = false;
					},
					() => {
						if (live) searchFailed = true;
					}
				);
			},
			q === '' ? 0 : SEARCH_DELAY
		);
		return () => {
			live = false;
			clearTimeout(timer);
		};
	});

	function mirrorQuery(q: string) {
		if ((page.url.searchParams.get('q') ?? '') === q) return;
		void goto(catalogHref(node ?? undefined, q), { replace: true, reset: false });
	}

	const searching = $derived(trimmed !== '');
	const shown = $derived(searching && result ? result : undefined);
	const settling = $derived(
		searching && (result?.query.trim() !== trimmed || result?.scope !== node)
	);
	/* Phones fold the waypoints three levels down into the hits' trails. */
	const drawn = $derived(shown && searchRows(shown, compact.current ? 3 : Infinity));
	const looksLikeCode = $derived(/\d/.test(trimmed) && !/\s/.test(trimmed));

	/*
	 * Wide screens browse in two panes: a level's siblings beside its contents, and at the root the
	 * areas holding hits beside the results. The search field heads what it searches: the contents'
	 * column on a level, the whole width at the root.
	 */
	const split = $derived(expanded.current && current !== undefined && current.siblings.length > 1);
	const rootSplit = $derived(
		expanded.current && !searchFailed && shown !== undefined && shown.branches.length > 0
	);
	/* The sticky search band's height: what focus scrolled under it must clear. */
	let bandHeight = $state(0);

	/** Android words the field by how deep the browse is. */
	const placeholder = $derived.by(() => {
		const depth = node === null ? 0 : (current?.ancestors.length ?? 0) + 1;
		if (depth === 0) return m.elearning_search_placeholder_area();
		if (depth === 1) return m.elearning_search_placeholder_category();
		if (depth === 2) return m.elearning_search_placeholder_subcategory();
		return m.elearning_search_placeholder_course();
	});

	const title = $derived(current?.name ?? m.elearning_catalog_title());
	const back = $derived.by(() => {
		if (node === null) return { href: resolve('/(app)/elearning'), label: m.elearning_title() };
		const parent = current?.ancestors.at(-1);
		return parent
			? { href: catalogHref(parent.key), label: parent.name }
			: { href: catalogHref(), label: m.elearning_catalog_root() };
	});

	const rootGate = new LoadingGate(() => root.data === undefined && root.status === 'pending');
	const levelGate = new LoadingGate(
		() => node !== null && level.data === undefined && level.status === 'pending'
	);

	let dialog = $state<EnrolDialog>();

	function askEnrol(course: CatalogCourse) {
		dialog?.show(course);
	}

	function enrolledIn(course: CatalogCourse) {
		justEnrolled.add(course.id);
		revealLater(course.id);
		toast.success(m.elearning_enrolled_success({ name: course.name }), {
			action: { label: m.elearning_show_in_courses(), onclick: () => void goto(coursesHref()) }
		});
	}

	/*
	 * A trail longer than the screen shows its end, where the reader is, and fades the edge it
	 * runs past so it reads as scrolled, not cut.
	 */
	function showEnd(trail: HTMLElement) {
		const edges = () => {
			trail.dataset.before = String(trail.scrollLeft > 1);
			trail.dataset.after = String(trail.scrollLeft + trail.clientWidth < trail.scrollWidth - 1);
		};
		trail.scrollLeft = trail.scrollWidth;
		edges();
		const resize = new ResizeObserver(edges);
		resize.observe(trail);
		trail.addEventListener('scroll', edges, { passive: true });
		return () => {
			resize.disconnect();
			trail.removeEventListener('scroll', edges);
		};
	}

	function searchEverywhere() {
		void goto(catalogHref(undefined, trimmed));
	}
</script>

<svelte:head>
	<title>{current?.name ?? m.elearning_catalog_title()} · {m.app_name()}</title>
</svelte:head>

{#snippet levelSkeleton()}
	<div class="skeleton-list" aria-hidden="true">
		{#each [0, 1, 2, 3, 4, 5] as row (row)}
			<div class="skeleton-row">
				<Skeleton width="32px" height="32px" radius="10px" />
				<span class="skeleton-text"><Skeleton variant="text" width={`${70 - row * 6}%`} /></span>
			</div>
		{/each}
	</div>
{/snippet}

{#snippet categoryLeading()}
	<span class="chip" aria-hidden="true"><IconFolder /></span>
{/snippet}

{#snippet courseLeading()}
	<span class="chip" aria-hidden="true"><IconBookOpen /></span>
{/snippet}

{#snippet searchBand()}
	<div class="search" bind:offsetHeight={bandHeight}>
		<SearchField label={m.elearning_catalog_search()} {placeholder} size="lg" bind:value={text} />
	</div>
{/snippet}

{#snippet results()}
	<section class="results below" aria-busy={settling} aria-label={m.elearning_catalog_search()}>
		{#if searchFailed}
			<ErrorState
				message={m.elearning_catalog_load_error()}
				onretry={() => {
					searchFailed = false;
					attempt++;
				}}
			/>
		{:else if shown && drawn && drawn.rows.length > 0}
			<p class="summary" role="status">
				{m.elearning_results({ count: shown.total })}
				{#if current}
					<span class="scope">{m.elearning_results_in({ place: current.name })}</span>
				{/if}
			</p>
			<div class={['tree', { settling }]}>
				<SearchResults
					rows={drawn.rows}
					query={shown.query}
					{tintOf}
					{enrolled}
					{offline}
					onenrol={askEnrol}
					label={m.elearning_results({ count: shown.total })}
				/>
			</div>
			{#if shown.capped || drawn.capped}
				<p class="capped">
					{looksLikeCode
						? m.elearning_results_capped_code({ count: drawn.hits })
						: m.elearning_results_capped({ count: drawn.hits })}
				</p>
			{/if}
		{:else if shown && !settling}
			<EmptyState
				size="page"
				icon={IconMagnifyingGlass}
				title={m.elearning_no_results({ query: trimmed })}
				body={m.elearning_no_results_hint()}
			>
				{#snippet action()}
					{#if node !== null}
						<Button onclick={searchEverywhere}>{m.elearning_search_whole_catalog()}</Button>
					{/if}
				{/snippet}
			</EmptyState>
		{:else}
			{@render levelSkeleton()}
		{/if}
	</section>
{/snippet}

<div
	class="catalog"
	style:--area-h={tint?.hue ?? 15}
	style:--area-c={tint?.chroma ?? 0.12}
	style:--search-h="{bandHeight}px"
>
	<!-- Opening a level replaces the page's content: the reader starts again from its title. -->
	<Page width="dashboard" focusOnQuery>
		<PageHeader
			{title}
			eyebrow={current && current.ancestors.length > 0 ? current.area.name : undefined}
			subtitle={node === null ? m.elearning_select_area() : undefined}
			{back}
		>
			{#snippet meta()}
				{#if current && current.ancestors.length > 0}
					<nav class="crumbs" aria-label={m.elearning_catalog_path()} {@attach showEnd}>
						<ol>
							<li><a href={catalogHref()}>{m.elearning_catalog_root()}</a></li>
							{#each current.ancestors as ancestor (ancestor.key)}
								<li><a href={catalogHref(ancestor.key)}>{ancestor.name}</a></li>
							{/each}
							<li aria-current="page">{current.name}</li>
						</ol>
					</nav>
				{/if}
			{/snippet}
		</PageHeader>

		{#if root.status === 'error' || (node !== null && level.status === 'error')}
			<ErrorState
				title={m.elearning_catalog_load_failed()}
				message={m.elearning_catalog_load_error()}
				onretry={() => {
					void root.refresh();
					void level.refresh();
				}}
				retrying={root.refreshing || level.refreshing}
			/>
		{:else if node !== null && level.data === null}
			<EmptyState
				size="page"
				icon={IconMapTrifold}
				title={m.elearning_catalog_not_found()}
				body={m.elearning_catalog_not_found_hint()}
			>
				{#snippet action()}
					<Button href={catalogHref()}>{m.elearning_catalog_back_root()}</Button>
				{/snippet}
			</EmptyState>
		{:else if node === null}
			{@render searchBand()}
			{#if searching}
				<div class={['panes', { split: rootSplit }]}>
					{#if rootSplit && shown}
						<nav class="side" aria-label={m.elearning_results_by_area()}>
							<ListGroup label={m.elearning_results_by_area()}>
								<ListRow
									title={m.elearning_results_everywhere()}
									meta={formatNumber(shown.total)}
									metaEnd
									selected
								>
									{#snippet leading()}
										<span class="swatch all" aria-hidden="true"></span>
									{/snippet}
								</ListRow>
								{#each shown.branches as branch (branch.key)}
									{@const accent = tintOf(branch.key)}
									<ListRow
										title={branch.name}
										href={catalogHref(branch.key, trimmed)}
										meta={formatNumber(branch.count)}
										metaEnd
									>
										{#snippet leading()}
											<span
												class="swatch"
												style:--area-h={accent.hue}
												style:--area-c={accent.chroma}
												aria-hidden="true"
											></span>
										{/snippet}
									</ListRow>
								{/each}
							</ListGroup>
						</nav>
					{/if}
					{@render results()}
				</div>
			{:else if root.data}
				{#each root.data.sections as section, index (section.name)}
					<section class="section below" aria-labelledby="catalog-section-{index}">
						<h2 class="section-title" id="catalog-section-{index}">{section.name}</h2>
						<ul class="tiles" role="list">
							{#each section.areas as area (area.key)}
								<li><AreaTile {area} href={catalogHref(area.key)} /></li>
							{/each}
						</ul>
					</section>
				{/each}
				<p class="as-of">
					{m.elearning_catalog_as_of({ date: formatDate(new Date(root.data.fetchedAt), 'long') })}
				</p>
			{:else}
				<div class="tiles-skeleton" role="status">
					<span class="sr-only">{m.elearning_catalog_loading()}</span>
					{#if rootGate.visible}
						<Skeleton width="140px" height="16px" />
						<ul class="tiles" role="list" aria-hidden="true">
							{#each [0, 1, 2, 3, 4, 5, 6] as tile (tile)}
								<li><span class="tile-skeleton"></span></li>
							{/each}
						</ul>
					{/if}
				</div>
			{/if}
		{:else if current}
			<div class={['panes', { split }]}>
				{#if split}
					<nav class="side" aria-label={back.label}>
						<ListGroup label={back.label}>
							{#each current.siblings as sibling (sibling.key)}
								<!-- While searching, a sibling is another place to search. -->
								<ListRow
									title={sibling.name}
									href={catalogHref(sibling.key, searching ? trimmed : undefined)}
									selected={sibling.key === current.key}
									lines={2}
								/>
							{/each}
						</ListGroup>
					</nav>
				{/if}
				<div class="main">
					{@render searchBand()}
					{#if searching}
						{@render results()}
					{:else}
						<div class="contents below">
							{#if current.categories.length > 0}
								<ListGroup
									title={current.courses.length > 0 ? m.elearning_categories() : undefined}
									label={m.elearning_categories()}
									level={2}
								>
									{#each current.categories as category (category.key + category.name)}
										<ListRow
											title={category.name}
											description={category.courses > 0
												? m.elearning_category_courses({
														count: category.courses,
														shown: formatNumber(category.courses)
													})
												: m.elearning_category_empty()}
											href={catalogHref(category.key)}
											leading={categoryLeading}
											lines={1}
										/>
									{/each}
								</ListGroup>
							{/if}
							{#if current.courses.length > 0}
								<ListGroup
									title={current.categories.length > 0 ? m.elearning_courses() : undefined}
									label={m.elearning_courses()}
									level={2}
									lazy={current.courses.length >= 50}
								>
									{#each current.courses as course (course.id)}
										<ListRow
											title={course.name}
											description={bareCode(course.code)}
											leading={courseLeading}
										>
											{#snippet trailing()}
												<EnrolButton
													{course}
													enrolled={enrolled.has(course.id)}
													{offline}
													onenrol={askEnrol}
												/>
											{/snippet}
										</ListRow>
									{/each}
								</ListGroup>
							{/if}
							{#if current.categories.length === 0 && current.courses.length === 0}
								<EmptyState
									icon={IconFolderDashed}
									title={m.elearning_category_empty()}
									body={m.elearning_category_empty_hint()}
								>
									{#snippet action()}
										<Button href={back.href}>{back.label}</Button>
									{/snippet}
								</EmptyState>
							{/if}
						</div>
					{/if}
				</div>
			</div>
		{:else if levelGate.visible}
			<!-- Most levels have siblings: the skeleton keeps their column, so the field doesn't jump. -->
			<div class={['panes', { split: expanded.current }]}>
				{#if expanded.current}
					<div class="side">{@render levelSkeleton()}</div>
				{/if}
				<div class="main">
					{@render searchBand()}
					{@render levelSkeleton()}
				</div>
			</div>
		{/if}
	</Page>

	{#if session.current}
		<EnrolDialog bind:this={dialog} scope={session.current} {offline} onenrolled={enrolledIn} />
	{/if}
</div>

<style>
	.catalog {
		--column: 760px;
		display: contents;
	}

	/* Sized by the header, never by the trail: on phones the trail scrolls instead of widening it. */
	.crumbs {
		flex: 1 1 100%;
		contain: inline-size;
		overflow-x: auto;
		scrollbar-width: none;
	}

	.crumbs ol {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 2px 0;
		margin: 0;
		padding: 0;
		list-style: none;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.crumbs li {
		display: inline-flex;
		align-items: center;
	}

	.crumbs li + li::before {
		content: '›';
		padding-inline: 6px;
		color: var(--text-subtle);
	}

	.crumbs a {
		border-radius: var(--r-2xs);
		color: inherit;
		text-decoration: none;
	}

	@media (hover: hover) {
		.crumbs a:hover {
			color: var(--text);
			text-decoration: underline;
			text-underline-offset: 3px;
		}
	}

	.crumbs [aria-current] {
		color: var(--text);
		font-weight: 550;
	}

	@media (width < 600px) {
		.crumbs {
			--before: black;
			--after: black;
			mask-image: linear-gradient(
				to right,
				var(--before),
				black 28px,
				black calc(100% - 28px),
				var(--after)
			);
		}

		.crumbs:global([data-before='true']) {
			--before: transparent;
		}

		.crumbs:global([data-after='true']) {
			--after: transparent;
		}

		.crumbs ol {
			flex-wrap: nowrap;
		}

		.crumbs li {
			flex: none;
			white-space: nowrap;
		}
	}

	.search {
		position: sticky;
		inset-block-start: var(--shell-inset-top);
		z-index: var(--z-sticky);
		margin-block: calc(-1 * var(--sp-2));
		padding-block: var(--sp-2);
		background: var(--bg);
	}

	/* Focus moved under the sticky field (Shift+Tab, the link after enrolling) stops below it. */
	.below :global(:is(a, button, input, [tabindex])) {
		scroll-margin-block-start: calc(var(--search-h) + 4px);
	}

	.section,
	.tiles-skeleton {
		display: grid;
		gap: var(--sp-3);
		container: catalog-section / inline-size;
	}

	.section-title {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: 0.1em;
		text-transform: uppercase;
	}

	/* Two columns or four: the sections hold 7, 4 and 4 areas, which three would leave ragged. */
	.tiles {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 10px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	@container catalog-section (width >= 680px) {
		.tiles {
			grid-template-columns: repeat(4, minmax(0, 1fr));
			gap: 14px;
		}
	}

	.tiles > li {
		display: grid;
		container: area-tile / inline-size;
	}

	@container area-tile (width >= 200px) {
		.tile-skeleton {
			aspect-ratio: 1.45;
		}
	}

	@container area-tile (width >= 260px) {
		.tile-skeleton {
			aspect-ratio: 1.65;
		}
	}

	.tile-skeleton {
		display: block;
		aspect-ratio: 1.3;
		border-radius: var(--r-lg);
		background: var(--track);
		animation: breathe 1.6s var(--ease-in-out) 150ms infinite both;
	}

	@keyframes breathe {
		0%,
		100% {
			opacity: 0.55;
		}
		50% {
			opacity: 1;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.tile-skeleton {
			animation: none;
			opacity: 0.8;
		}
	}

	.as-of {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.panes {
		display: grid;
		gap: var(--sp-6);
		align-items: start;
	}

	.panes.split {
		grid-template-columns: minmax(220px, 280px) minmax(0, 1fr);
	}

	/* One column: a level's list reads at a list's width, its search field over it. */
	.panes:not(.split) > .main {
		max-inline-size: var(--column);
	}

	.main {
		display: grid;
		gap: var(--sp-5);
		min-inline-size: 0;
	}

	/* Beside a level the side column tops out with its field; at the root, under the field. */
	.side {
		--side-top: calc(var(--shell-inset-top) + var(--sp-2));
		position: sticky;
		inset-block-start: var(--side-top);
		max-block-size: calc(100dvh - var(--side-top) - var(--sp-4));
		overflow-y: auto;
		overscroll-behavior: contain;
		scrollbar-width: thin;
		border-radius: var(--r-lg);
	}

	/* Each area's accent, as its results wear it. */
	.swatch {
		display: block;
		inline-size: 12px;
		block-size: 12px;
		border-radius: 4px;
		background: light-dark(
			oklch(52% var(--area-c) var(--area-h)),
			oklch(72% calc(var(--area-c) * 0.8) var(--area-h))
		);
	}

	.swatch.all {
		background: none;
		box-shadow: inset 0 0 0 2px currentColor;
	}

	.search + .panes > .side {
		--side-top: calc(var(--shell-inset-top) + var(--search-h) + var(--sp-2));
	}

	.contents {
		display: grid;
		gap: var(--sp-5);
		min-inline-size: 0;
	}

	.chip {
		display: grid;
		place-items: center;
		inline-size: 36px;
		block-size: 36px;
		border-radius: 11px;
		background: light-dark(
			oklch(93% calc(var(--area-c) * 0.4) var(--area-h)),
			oklch(30% calc(var(--area-c) * 0.45) var(--area-h))
		);
		color: light-dark(
			oklch(44% var(--area-c) var(--area-h)),
			oklch(82% calc(var(--area-c) * 0.7) var(--area-h))
		);
	}

	.chip :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.results {
		display: grid;
		gap: var(--sp-3);
		min-inline-size: 0;
	}

	.summary {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.scope {
		font-weight: 400;
	}

	.tree {
		transition: opacity var(--dur-fast) var(--ease-out);
	}

	.tree.settling {
		opacity: 0.6;
		transition-delay: 150ms;
	}

	.capped {
		padding: var(--sp-3) var(--sp-4);
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.skeleton-list {
		display: grid;
		gap: 2px;
	}

	.skeleton-row {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 60px;
		padding-inline: 16px;
		background: var(--surface);
		font: var(--type-ui);
	}

	.skeleton-row:first-child {
		border-radius: var(--r-lg) var(--r-lg) var(--r-2xs) var(--r-2xs);
	}

	.skeleton-row:last-child {
		border-radius: var(--r-2xs) var(--r-2xs) var(--r-lg) var(--r-lg);
	}

	.skeleton-text {
		flex: 1;
	}
</style>
