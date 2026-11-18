<script lang="ts">
	import type { MoodleForum } from '@mybicocca/data-api/elearning';
	import IconChatsDuotone from '~icons/ph/chats-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { announcementsForum, otherForums } from '../forum.ts';
	import { courseForumsQuery } from '../queries.ts';
	import AnnouncementsCard from './AnnouncementsCard.svelte';
	import ForumRow from './ForumRow.svelte';

	const course = getCourse();

	const forums = query(() => {
		const scope = session.current;
		return scope && courseForumsQuery(scope, course.id);
	});

	const featured = $derived(forums.data && announcementsForum(forums.data));
	const others = $derived<MoodleForum[]>(forums.data ? otherForums(forums.data, featured) : []);
	const gate = new LoadingGate(() => forums.data === undefined && forums.status === 'pending');
	const updatedAt = $derived(forums.updatedAt ? new Date(forums.updatedAt) : undefined);
</script>

<svelte:head>
	<title>{m.course_tab_forums()} · {course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

{#if forums.error && forums.data}
	<div class="notice">
		<ErrorState
			variant="stale"
			message={errorMessage(forums.error)}
			{updatedAt}
			onretry={forums.refresh}
			retrying={forums.refreshing}
		/>
	</div>
{/if}

{#if forums.status === 'error' && forums.error && forums.data === undefined}
	<ErrorState
		title={m.forum_list_failed()}
		message={errorMessage(forums.error)}
		onretry={forums.refresh}
		retrying={forums.refreshing}
	/>
{:else if forums.data === undefined}
	<div class="layout split" aria-busy="true" aria-label={m.forum_list_loading()}>
		<div class="card-skeleton">
			{#if gate.visible}<Skeleton height="100%" radius="var(--r-xl)" />{/if}
		</div>
		<div class="rows-skeleton">
			{#if gate.visible}
				<Skeleton variant="text" width="40%" height="1.2em" />
				{#each { length: 3 }, index (index)}
					<div class="row-skeleton">
						<Skeleton width="40px" height="40px" radius="12px" />
						<span class="lines">
							<Skeleton variant="text" width="{70 - index * 12}%" />
							<Skeleton variant="text" width="45%" height="0.8em" />
						</span>
					</div>
				{/each}
			{/if}
		</div>
	</div>
{:else if forums.data.length === 0}
	<EmptyState
		size="cover"
		icon={IconChatsDuotone}
		title={m.forum_list_empty_title()}
		body={m.forum_list_empty_body()}
	/>
{:else}
	<div class={['layout', { split: featured !== undefined && others.length > 0 }]}>
		{#if featured}
			<AnnouncementsCard courseId={course.id} forum={featured} />
		{/if}
		{#if others.length > 0}
			<ListGroup
				title={featured ? m.forum_list_other_title() : m.forum_list_title()}
				description={m.forum_list_count({ count: others.length })}
			>
				{#each others as forum (forum.id)}
					<ForumRow courseId={course.id} {forum} slot={course.slot} />
				{/each}
			</ListGroup>
		{/if}
	</div>
{/if}

<style>
	.notice {
		margin-block-end: var(--sp-5);
	}

	.layout {
		display: grid;
		gap: var(--sp-6);
		align-items: start;
	}

	@container page (width >= 820px) {
		.split {
			grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
			gap: var(--sp-7);
		}
	}

	.card-skeleton {
		block-size: 440px;
		border-radius: var(--r-xl);
		background: var(--course-container);
	}

	.rows-skeleton {
		display: grid;
		gap: 12px;
	}

	.row-skeleton {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--surface);
	}

	.lines {
		display: grid;
		flex: 1;
		gap: 6px;
	}
</style>
