<script lang="ts">
	import { page } from '$app/state';
	import BookingPage from '#lib/features/exams/components/BookingPage.svelte';
</script>

<BookingPage slug={page.params.id ?? ''} />
