<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { objectUrl } from '../../file/save.ts';

	interface Props {
		bytes: Uint8Array;
		name: string;
		mime: string;
		/** Natural size, scrolled; else fitted to the stage. */
		actual: boolean;
		ontoggle: () => void;
	}

	let { bytes, name, mime, actual, ontoggle }: Props = $props();
</script>

<div class={['stage', { actual }]}>
	<button
		type="button"
		class="frame"
		aria-label={actual ? m.course_image_fit() : m.course_image_actual()}
		onclick={ontoggle}
	>
		<!-- SVGs go through <img>, where their scripts never run. -->
		<img alt={name} decoding="async" {@attach objectUrl(bytes, mime)} />
	</button>
</div>

<style>
	.stage {
		display: grid;
		block-size: 100%;
		overflow: auto;
		padding: var(--sp-4);
		background-color: var(--surface-sunken);
		background-image: conic-gradient(
			var(--hairline) 25%,
			transparent 0 50%,
			var(--hairline) 0 75%,
			transparent 0
		);
		background-size: 24px 24px;
		overscroll-behavior: contain;
	}

	.frame {
		display: grid;
		place-items: center;
		min-inline-size: 0;
		min-block-size: 0;
		padding: 0;
		border: 0;
		background: none;
		cursor: zoom-in;
	}

	.actual .frame {
		place-items: start center;
		cursor: zoom-out;
	}

	img {
		max-inline-size: 100%;
		max-block-size: calc(100dvh - 160px);
		object-fit: contain;
		border-radius: var(--r-xs);
		box-shadow: var(--elev-2);
		background: oklch(100% 0 0);
	}

	.actual img {
		max-inline-size: none;
		max-block-size: none;
	}
</style>
