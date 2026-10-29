<script lang="ts">
	import { page } from '$app/state';
	import LoginPage from '#lib/features/library/components/LoginPage.svelte';
	import { safeNext } from '#lib/features/library/links.ts';
</script>

<LoginPage next={safeNext(page.url.searchParams.get('next'))} />
