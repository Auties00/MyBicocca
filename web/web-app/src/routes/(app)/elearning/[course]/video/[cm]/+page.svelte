<script lang="ts">
	import VideoPlayer from '#lib/features/course/components/video/VideoPlayer.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<VideoPlayer cmId={data.cmId} />
