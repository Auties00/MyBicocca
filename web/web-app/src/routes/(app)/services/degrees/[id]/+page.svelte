<script lang="ts">
	import { page } from '$app/state';
	import DegreeDetail from '#lib/features/documents/components/DegreeDetail.svelte';
</script>

<DegreeDetail id={page.params.id ?? ''} />
