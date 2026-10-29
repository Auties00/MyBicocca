<script lang="ts">
	import CareerPicker from '#lib/features/auth/components/CareerPicker.svelte';
	import { m } from '#lib/paraglide/messages.js';
</script>

<svelte:head>
	<title>{m.auth_career_title()} · {m.app_name()}</title>
</svelte:head>

<CareerPicker />
