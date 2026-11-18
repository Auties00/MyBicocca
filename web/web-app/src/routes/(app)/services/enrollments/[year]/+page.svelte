<script lang="ts">
	import { page } from '$app/state';
	import EnrollmentDetail from '#lib/features/documents/components/EnrollmentDetail.svelte';
</script>

<EnrollmentDetail year={page.params.year ?? ''} />
