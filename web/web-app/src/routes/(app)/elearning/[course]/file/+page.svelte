<script lang="ts">
	import { page } from '$app/state';
	import FileViewer from '#lib/features/course/components/viewer/FileViewer.svelte';
	import { fileRequestOf } from '#lib/features/course/viewer.ts';

	const request = $derived(fileRequestOf(page.url.searchParams));
</script>

<FileViewer {request} />
