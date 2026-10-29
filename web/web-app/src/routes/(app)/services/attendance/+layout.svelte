<script lang="ts">
	import type { Snippet } from 'svelte';
	import AttendanceLayout from '#lib/features/attendance/components/AttendanceLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<AttendanceLayout>{@render children()}</AttendanceLayout>
