<script lang="ts">
	import AttemptPage from '#lib/features/quiz/components/AttemptPage.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

{#key data.attemptId}
	<AttemptPage quizId={data.quizId} attemptId={data.attemptId} />
{/key}
