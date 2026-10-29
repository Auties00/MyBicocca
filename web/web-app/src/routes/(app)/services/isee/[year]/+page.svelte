<script lang="ts">
	import { page } from '$app/state';
	import IseeDetail from '#lib/features/payments/components/IseeDetail.svelte';
</script>

<IseeDetail year={page.params.year ?? ''} />
