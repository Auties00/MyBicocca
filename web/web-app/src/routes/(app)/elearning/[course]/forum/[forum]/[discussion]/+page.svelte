<script lang="ts">
	import Thread from '#lib/features/forum/components/Thread.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

{#key data.discussionId}
	<Thread discussionId={data.discussionId} />
{/key}
