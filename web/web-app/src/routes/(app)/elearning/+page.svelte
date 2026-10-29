<script lang="ts">
	import ElearningPage from '#lib/features/elearning/components/ElearningPage.svelte';
	import { m } from '#lib/paraglide/messages.js';
</script>

<svelte:head>
	<title>{m.elearning_title()} · {m.app_name()}</title>
</svelte:head>

<ElearningPage />
