<script lang="ts">
	import { page } from '$app/state';
	import CancelLinkPage from '#lib/features/library/components/CancelLinkPage.svelte';
</script>

<!-- The cancellation links Affluences emails carry the token as `reservationToken`. -->
<CancelLinkPage token={page.url.searchParams.get('reservationToken')?.trim() || null} />
