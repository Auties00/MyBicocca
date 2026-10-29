<script lang="ts">
	import { page } from '$app/state';
	import DirectoryPage from '#lib/features/appointments/components/DirectoryPage.svelte';
	import { isSectionId } from '#lib/features/appointments/directory.ts';

	const section = $derived.by(() => {
		const value = page.url.searchParams.get('section');
		return isSectionId(value) ? value : undefined;
	});
</script>

<DirectoryPage {section} />
