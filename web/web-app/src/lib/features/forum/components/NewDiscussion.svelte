<script lang="ts">
	import { afterNavigate, goto } from '$app/navigation';
	import IconLockSimple from '~icons/ph/lock-simple-duotone';
	import { coursePath } from '#lib/features/course/links.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { PENDING_LINE } from '../forum.ts';
	import { getForum } from '../forumState.svelte.ts';
	import Composer, { type ComposerResult } from './Composer.svelte';

	const forum = getForum();
	const wide = $derived(forum.wide);
	const forumHref = $derived(coursePath(forum.courseId, 'forum', forum.id));

	function submitted(result: ComposerResult) {
		if (result.kind !== 'new') return;
		const id = result.discussionId;
		toast.show({ message: m.forum_discussion_posted(), tone: 'success' });
		forum.arrival = id;
		void goto(coursePath(forum.courseId, 'forum', forum.id, id), { replace: true });
	}

	/* Opened from the forum, cancelling goes back to where the student was. */
	let fromForum = false;
	afterNavigate(({ from, type }) => {
		fromForum =
			(type === 'link' || type === 'goto') && from?.url.pathname.startsWith(forumHref) === true;
	});

	function cancel() {
		if (fromForum) history.back();
		else void goto(forumHref, { replace: true });
	}
</script>

<svelte:head>
	<title
		>{m.forum_new_discussion()} · {forum.name ?? m.forum_title_fallback()} · {m.app_name()}</title
	>
</svelte:head>

{#if !wide}
	<PageHeader
		eyebrow={forum.name ?? PENDING_LINE}
		title={m.forum_new_discussion()}
		back={{ href: forumHref, label: forum.name ?? m.forum_back() }}
	/>
{/if}

<section
	class={['new', { stacked: !wide }]}
	aria-labelledby={wide ? 'new-discussion-title' : undefined}
>
	{#if wide}
		<header class="head">
			<h2 class="title" id="new-discussion-title">{m.forum_new_discussion()}</h2>
			{#if forum.name}<p class="where">{m.forum_new_in({ forum: forum.name })}</p>{/if}
		</header>
	{/if}

	{#if forum.forum === undefined}
		<div class="placeholder" aria-busy="true">
			<Skeleton height="var(--control-h)" />
			<Skeleton height="200px" />
		</div>
	{:else if !forum.forum.canCreateDiscussions}
		<EmptyState
			icon={IconLockSimple}
			title={m.forum_cannot_create_title()}
			body={m.forum_cannot_create_body()}
		>
			{#snippet action()}
				<Button href={forumHref}>{m.forum_back_to_list()}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<div class="card">
			<Composer mode={{ kind: 'new' }} onsubmitted={submitted} oncancel={cancel} />
		</div>
	{/if}
</section>

<style>
	.new {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: 780px;
	}

	.stacked {
		margin-block-start: var(--sp-5);
	}

	.head {
		display: grid;
		gap: 4px;
	}

	.title {
		font: 650 1.375rem / 1.25 var(--font-ui);
		letter-spacing: -0.01em;
	}

	.where {
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.875rem;
	}

	.card {
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	@container (width < 480px) {
		.card {
			padding: var(--sp-4);
		}
	}

	.placeholder {
		display: grid;
		gap: var(--sp-4);
	}
</style>
