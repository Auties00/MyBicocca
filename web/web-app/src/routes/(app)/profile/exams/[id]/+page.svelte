<script lang="ts">
	import { page } from '$app/state';
	import CourseDetail from '#lib/features/profile/components/CourseDetail.svelte';
</script>

<CourseDetail id={Number(page.params.id)} />
