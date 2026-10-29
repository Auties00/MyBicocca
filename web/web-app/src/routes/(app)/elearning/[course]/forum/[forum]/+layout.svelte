<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import ForumLayout from '#lib/features/forum/components/ForumLayout.svelte';
	import type { LayoutData } from './$types';

	interface Props {
		data: LayoutData;
		children: Snippet;
	}

	let { data, children }: Props = $props();
</script>

<!-- The forum alone shows its list; a discussion or the new-discussion form opens beside it.
     Each forum mounts its own layout: the shell's top bar can't follow a page header whose back
     link changes in place. -->
{#key `${data.courseId}/${data.forumId}`}
	<ForumLayout
		courseId={data.courseId}
		forumId={data.forumId}
		open={page.route.id !== '/(app)/elearning/[course]/forum/[forum]'}
	>
		{#snippet detail()}
			{@render children()}
		{/snippet}
	</ForumLayout>
{/key}
