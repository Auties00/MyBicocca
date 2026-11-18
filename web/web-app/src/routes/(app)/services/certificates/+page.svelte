<script lang="ts">
	import { page } from '$app/state';
	import CertificatesPage from '#lib/features/documents/components/CertificatesPage.svelte';
	import { OPEN_PARAM } from '#lib/features/documents/certificates.svelte.ts';
</script>

<CertificatesPage open={page.url.searchParams.get(OPEN_PARAM) ?? undefined} />
