<script lang="ts">
	import { page } from '$app/state';
	import RefundDetail from '#lib/features/payments/components/RefundDetail.svelte';
</script>

<RefundDetail id={page.params.id ?? ''} />
