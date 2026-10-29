<script lang="ts">
	import { page } from '$app/state';
	import BookingPage from '#lib/features/appointments/components/BookingPage.svelte';

	const serviceId = $derived(Number(page.params.service));
</script>

<!-- Another desk starts a booking of its own. -->
{#key serviceId}
	<BookingPage {serviceId} details={page.url.searchParams.get('step') === 'details'} />
{/key}
