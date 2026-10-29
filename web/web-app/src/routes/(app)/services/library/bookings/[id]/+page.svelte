<script lang="ts">
	import { page } from '$app/state';
	import ReservationPage from '#lib/features/library/components/ReservationPage.svelte';
</script>

<ReservationPage id={Number(page.params.id)} />
