<script lang="ts">
	import AssignmentPage from '#lib/features/assignment/components/AssignmentPage.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<AssignmentPage courseId={data.courseId} assignId={data.assignId} />
