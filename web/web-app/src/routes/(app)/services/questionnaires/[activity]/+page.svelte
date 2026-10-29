<script lang="ts">
	import { page } from '$app/state';
	import ActivityPage from '#lib/features/questionnaires/components/ActivityPage.svelte';
	import { routeNumber } from '#lib/features/questionnaires/links.ts';
</script>

<ActivityPage id={routeNumber(page.params.activity)} />
