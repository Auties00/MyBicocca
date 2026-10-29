<script lang="ts">
	import type { Snippet } from 'svelte';
	import CourseLayout from '#lib/features/course/components/CourseLayout.svelte';
	import type { LayoutData } from './$types';

	interface Props {
		data: LayoutData;
		children: Snippet;
	}

	let { data, children }: Props = $props();
</script>

<!-- The tabs (overview, content, quizzes/, assignments/, forums/) render only their panel: the
     layout owns the page, the cover and the tab bar. Activity pages render on their own. -->
<CourseLayout courseId={data.courseId}>
	{@render children()}
</CourseLayout>
