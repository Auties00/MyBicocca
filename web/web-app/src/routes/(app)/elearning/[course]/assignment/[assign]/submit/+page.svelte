<script lang="ts">
	import SubmissionEditor from '#lib/features/assignment/components/SubmissionEditor.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<SubmissionEditor courseId={data.courseId} assignId={data.assignId} />
