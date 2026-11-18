<script lang="ts">
	import type { Snippet } from 'svelte';
	import { tick } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconChatsDuotone from '~icons/ph/chats-duotone';
	import IconPencilSimpleLine from '~icons/ph/pencil-simple-line';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import FileDialogs from '#lib/features/course/components/FileDialogs.svelte';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { FileOpener, setFileOpener } from '#lib/features/course/file/opener.svelte.ts';
	import { coursePath } from '#lib/features/course/links.ts';
	import { stripMlang } from '#lib/features/elearning/mlang.ts';
	import { isRenderableHtml } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { isTypingTarget } from '#lib/ui/platform.ts';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { discussionId, forumKind, latestDiscussions, PENDING_LINE } from '../forum.ts';
	import { ForumState, setForum } from '../forumState.svelte.ts';
	import DiscussionList from './DiscussionList.svelte';
	import DiscussionsSkeleton from './DiscussionsSkeleton.svelte';
	import ForumOverview from './ForumOverview.svelte';
	import { kindLabel } from './kinds.ts';

	interface Props {
		courseId: number;
		forumId: number;
		/** Whether the route opens a discussion or the new-discussion form beside the list. */
		open: boolean;
		detail: Snippet;
	}

	let { courseId, forumId, open, detail }: Props = $props();

	const course = getCourse();
	const forum = setForum(
		new ForumState(
			() => courseId,
			() => forumId
		)
	);
	setFileOpener(new FileOpener(() => session.current));

	const selected = $derived(Number(page.params.discussion) || undefined);
	const composing = $derived(page.route.id === '/(app)/elearning/[course]/forum/[forum]/new');
	const record = $derived(forum.forum);
	const moduleName = $derived(
		(course.contents.data ?? [])
			.flatMap((section) => section.modules)
			.find((module) => module.modName === 'forum' && module.instance === forumId)?.name
	);
	const title = $derived(
		forum.name ??
			(moduleName === undefined ? undefined : stripMlang(moduleName, forum.language).trim()) ??
			m.forum_title_fallback()
	);
	const missing = $derived(forum.forums.data !== undefined && record === undefined);
	/* The discussions loaded but not the forum itself: its name, kind and permissions are missing. */
	const detailsError = $derived(record === undefined ? forum.forums.error : undefined);
	const intro = $derived(record === undefined ? '' : stripMlang(record.intro, forum.language));
	/*
	 * On one pane the forum's permissions and intro sit above the list (New discussion, About), so
	 * the list waits for them rather than being pushed down when they arrive.
	 */
	const settling = $derived(
		!forum.wide && !open && forum.forums.data === undefined && forum.forums.error === undefined
	);
	const gate = new LoadingGate(() => settling);
	const back = $derived({ href: coursePath(courseId, 'forums'), label: m.forum_back_forums() });

	/* A composer is open: a stray key must never navigate away from what is being written. */
	const writing = $derived(
		composing || page.url.searchParams.has('reply') || page.url.searchParams.has('edit')
	);

	registerShortcuts(() => [
		...(forum.discussions.length > 0 ? [{ keys: [['j'], ['k']], label: m.ui_keys_step() }] : []),
		...(record?.canCreateDiscussions ? [{ keys: [['n']], label: m.forum_new_discussion() }] : [])
	]);

	/* j and k move through the discussions, n starts a new one. */
	async function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.ctrlKey || event.metaKey || event.altKey || writing) return;
		if (isTypingTarget(event.target) || document.querySelector('dialog:modal')) return;
		const key = event.key.toLowerCase();
		if (key === 'n' && record?.canCreateDiscussions) {
			event.preventDefault();
			await goto(coursePath(courseId, 'forum', forumId, 'new'));
			return;
		}
		if (key !== 'j' && key !== 'k') return;
		const ids = forum.discussions.map(discussionId);
		if (ids.length === 0) return;
		event.preventDefault();
		const index = selected === undefined ? -1 : ids.indexOf(selected);
		const next =
			key === 'j'
				? ids[Math.min(ids.length - 1, index + 1)]
				: ids[Math.max(0, index === -1 ? 0 : index - 1)];
		if (next === selected) return;
		forum.arrival = undefined;
		await goto(coursePath(courseId, 'forum', forumId, next), {
			reset: false,
			replace: selected !== undefined
		});
		await tick();
		const row = document.querySelector<HTMLElement>(`a[data-discussion="${next}"]`);
		row?.focus({ preventScroll: true });
		row?.scrollIntoView({ block: 'nearest' });
	}
</script>

<svelte:window onkeydown={keydown} />

<svelte:head>
	{#if !open}
		<title>{title} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
	{/if}
</svelte:head>

{#snippet header()}
	<PageHeader
		eyebrow={course.title ?? PENDING_LINE}
		{title}
		subtitle={record === undefined ? ' ' : kindLabel[forumKind(record)]()}
		{back}
	>
		{#snippet actions()}
			{#if record?.canCreateDiscussions && !composing}
				<Button
					variant="solid"
					icon={IconPencilSimpleLine}
					href={coursePath(courseId, 'forum', forumId, 'new')}
					aria-keyshortcuts="N"
				>
					{m.forum_new_discussion()}
				</Button>
			{/if}
		{/snippet}
	</PageHeader>
{/snippet}

{#snippet list()}
	{#if detailsError && (open || !forum.wide)}
		<div class="problem">
			<Banner tone="warning" title={m.forum_details_failed()} message={errorMessage(detailsError)}>
				{#snippet actions()}
					<Button
						variant="ghost"
						size="sm"
						icon={IconArrowClockwise}
						loading={forum.forums.refreshing}
						onclick={forum.forums.refresh}
					>
						{m.common_retry()}
					</Button>
				{/snippet}
			</Banner>
		</div>
	{/if}
	{#if !forum.wide && isRenderableHtml(intro)}
		<details class="about">
			<summary>{m.forum_about()}</summary>
			<TeacherHtml html={intro} size="sm" />
		</details>
	{/if}
	<DiscussionList {selected} />
{/snippet}

{#snippet overview()}
	{#if record}
		<ForumOverview forum={record} latest={latestDiscussions(forum.discussions, 1)[0]} />
	{:else if detailsError}
		<ErrorState
			title={m.forum_details_failed()}
			message={errorMessage(detailsError)}
			onretry={forum.forums.refresh}
			retrying={forum.forums.refreshing}
		/>
	{/if}
{/snippet}

<Page width="dashboard">
	<div class="forum" data-course={course.slot}>
		{#if missing}
			{@render header()}
			<EmptyState
				size="page"
				icon={IconChatsDuotone}
				title={m.forum_missing_title()}
				body={m.forum_missing_body()}
			>
				{#snippet action()}
					<Button href={back.href}>{m.forum_back_to_forums()}</Button>
				{/snippet}
			</EmptyState>
		{:else if forum.forums.status === 'error' && forum.forums.error && record === undefined && forum.pages.data === undefined}
			{@render header()}
			<ErrorState
				title={m.forum_load_failed()}
				message={errorMessage(forum.forums.error)}
				onretry={forum.refresh}
				retrying={forum.forums.refreshing}
			/>
		{:else if settling}
			<div class="settling" aria-busy="true" aria-label={m.forum_loading()}>
				{#if gate.visible}
					<Skeleton variant="text" width="45%" height="0.75em" />
					<Skeleton width="75%" height="2.25rem" radius="var(--r-sm)" />
					<Skeleton variant="text" width="35%" />
				{/if}
			</div>
			<DiscussionsSkeleton />
		{:else}
			{#if forum.wide || !open}
				{@render header()}
			{/if}
			<ListDetail
				bind:twoPanes={forum.twoPanes}
				{list}
				detail={open ? detail : undefined}
				placeholder={overview}
				listWidth={360}
				listLabel={m.forum_discussions_label()}
				detailLabel={open ? m.forum_detail_label() : undefined}
				current={open && selected !== undefined
					? coursePath(courseId, 'forum', forumId, selected)
					: undefined}
				closeHref={coursePath(courseId, 'forum', forumId)}
			/>
		{/if}
	</div>
</Page>

<FileDialogs />

<style>
	.forum {
		display: grid;
		gap: var(--page-gap);
		min-inline-size: 0;
	}

	.problem {
		margin-block-end: var(--sp-3);
	}

	.settling {
		display: grid;
		align-content: end;
		gap: 10px;
		min-block-size: 150px;
		padding-block: 8px 4px;
	}

	.about {
		margin-block-end: var(--sp-3);
		padding: 12px 16px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.about summary {
		color: var(--text-muted);
		font: var(--type-ui-strong);
		cursor: pointer;
	}

	.about summary::marker {
		color: var(--course-ink);
	}

	.about[open] summary {
		margin-block-end: 8px;
	}
</style>
