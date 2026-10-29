<script lang="ts">
	import { page } from '$app/state';
	import LibraryPage from '#lib/features/library/components/LibraryPage.svelte';
</script>

<LibraryPage siteId={page.params.site ?? ''} />
