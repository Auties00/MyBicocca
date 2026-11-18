<script lang="ts">
	import IconMusicNotes from '~icons/ph/music-notes-duotone';
	import { objectUrl } from '../../file/save.ts';

	interface Props {
		bytes: Uint8Array;
		name: string;
		mime: string;
		kind: 'video' | 'audio';
	}

	let { bytes, name, mime, kind }: Props = $props();
</script>

<div class={['media', kind]}>
	{#if kind === 'video'}
		<video controls playsinline title={name} {@attach objectUrl(bytes, mime)}></video>
	{:else}
		<div class="audio">
			<span class="glyph" aria-hidden="true"><IconMusicNotes /></span>
			<p class="name">{name}</p>
			<audio controls title={name} {@attach objectUrl(bytes, mime)}></audio>
		</div>
	{/if}
</div>

<style>
	.media {
		display: grid;
		place-items: center;
		block-size: 100%;
	}

	.video {
		background: oklch(0% 0 0);
	}

	video {
		inline-size: 100%;
		block-size: 100%;
		object-fit: contain;
	}

	.audio {
		display: grid;
		justify-items: center;
		gap: 16px;
		inline-size: min(100% - 32px, 480px);
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 120px;
		block-size: 120px;
		border-radius: 36%;
		background: var(--container);
		color: var(--on-container);
	}

	.glyph :global(svg) {
		inline-size: 56px;
		block-size: 56px;
	}

	.name {
		font: var(--type-title-s);
		text-align: center;
		overflow-wrap: anywhere;
	}

	audio {
		inline-size: 100%;
	}
</style>
