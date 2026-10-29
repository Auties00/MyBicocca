<script lang="ts">
	import QuizPage from '#lib/features/quiz/components/QuizPage.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<QuizPage quizId={data.quizId} />
