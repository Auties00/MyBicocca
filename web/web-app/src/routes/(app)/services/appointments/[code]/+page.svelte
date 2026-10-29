<script lang="ts">
	import { page } from '$app/state';
	import ReservationPage from '#lib/features/appointments/components/ReservationPage.svelte';
</script>

<ReservationPage code={page.params.code ?? ''} />
