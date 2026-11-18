<script lang="ts">
	import ReviewPage from '#lib/features/quiz/components/ReviewPage.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<ReviewPage quizId={data.quizId} attemptId={data.attemptId} />
