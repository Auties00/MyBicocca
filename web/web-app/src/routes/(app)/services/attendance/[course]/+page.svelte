<script lang="ts">
	import { page } from '$app/state';
	import CourseDetail from '#lib/features/attendance/components/CourseDetail.svelte';
</script>

<CourseDetail slug={page.params.course ?? ''} />
