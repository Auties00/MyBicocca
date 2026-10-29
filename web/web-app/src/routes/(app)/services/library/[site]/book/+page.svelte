<script lang="ts">
	import { page } from '$app/state';
	import BookingWizard from '#lib/features/library/components/BookingWizard.svelte';
	import { parseChoices } from '#lib/features/library/wizard.ts';
</script>

<BookingWizard siteId={page.params.site ?? ''} choices={parseChoices(page.url.searchParams)} />
