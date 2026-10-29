<script lang="ts">
	import { page } from '$app/state';
	import InvoiceDetail from '#lib/features/payments/components/InvoiceDetail.svelte';
</script>

<InvoiceDetail id={page.params.id ?? ''} />
