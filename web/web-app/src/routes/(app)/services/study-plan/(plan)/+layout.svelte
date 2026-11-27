<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import StudyPlanPage from '#lib/features/studyplan/components/StudyPlanPage.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	const selected = $derived(page.params.exam === undefined ? undefined : Number(page.params.exam));
</script>

<!-- The plan lives here, so it keeps its place while an exam opens beside it or over it. -->
<StudyPlanPage {selected} detail={selected === undefined ? undefined : children} />
