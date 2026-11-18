<script lang="ts">
	import { page } from '$app/state';
	import ResultDetail from '#lib/features/results/components/ResultDetail.svelte';
</script>

<ResultDetail id={page.params.id ?? ''} />
