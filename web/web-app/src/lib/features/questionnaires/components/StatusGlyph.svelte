<script lang="ts" module>
	import IconCheck from '~icons/ph/check-bold';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import IconNotePencil from '~icons/ph/note-pencil';
	import IconWarning from '~icons/ph/warning-bold';
	import type { Icon } from '#lib/ui/types.ts';
	import type { ActivityStatus } from '../model.ts';
	import { clover, scallop } from '../shapes.ts';

	/* A shape and a glyph per status, so the colour is never the only cue. */
	const LOOKS: Record<ActivityStatus, { path: string; icon: Icon }> = {
		todo: { path: scallop(6, 0.09), icon: IconNotePencil },
		partial: { path: clover(), icon: IconHourglass },
		done: { path: 'M20 0a20 20 0 1 1 0 40a20 20 0 1 1 0-40Z', icon: IconCheck },
		misconfigured: {
			path: 'M10 1h20a9 9 0 0 1 9 9v20a9 9 0 0 1-9 9H10a9 9 0 0 1-9-9V10a9 9 0 0 1 9-9Z',
			icon: IconWarning
		}
	};
</script>

<script lang="ts">
	interface Props {
		status: ActivityStatus;
		size?: number;
	}

	let { status, size = 40 }: Props = $props();

	const look = $derived(LOOKS[status]);
</script>

<span class={['glyph', status]} style:--size="{size}px" aria-hidden="true">
	<svg class="shape" viewBox="0 0 40 40"><path d={look.path} /></svg>
	<look.icon class="icon" />
</span>

<style>
	.glyph {
		position: relative;
		display: grid;
		flex: none;
		place-items: center;
		inline-size: var(--size);
		block-size: var(--size);
		color: var(--glyph-fg);
	}

	.shape {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		fill: var(--glyph-bg);
	}

	.glyph :global(.icon) {
		position: relative;
		inline-size: calc(var(--size) * 0.5);
		block-size: calc(var(--size) * 0.5);
	}

	.todo {
		--glyph-bg: var(--container-press);
		--glyph-fg: var(--on-container);
	}

	/* The plain warning container all but vanishes on a dark row. */
	.partial {
		--glyph-bg: color-mix(in oklch, var(--warning-solid) 28%, var(--warning-container));
		--glyph-fg: var(--on-warning-container);
	}

	.done {
		--glyph-bg: var(--track);
		--glyph-fg: var(--text-muted);
	}

	.misconfigured {
		--glyph-bg: var(--danger-container);
		--glyph-fg: var(--on-danger-container);
	}
</style>
